/*
 * ============================================================
 *  FocusSense — LearnCore Pomodoro v2.0
 *  ESP32 BLE + HC-SR04 + MQ-135 + DFPlayer Mini
 * ============================================================
 *
 *  Rangkaian:
 *    DAYA
 *      Output (+) Stepdown → Breadboard (+)
 *      Output (-) Stepdown → Breadboard (-)
 *      VIN/5V ESP32        → Breadboard (+)
 *      GND ESP32           → Breadboard (-)
 *
 *    DFPlayer Mini
 *      VCC (Pin 1)  → Breadboard (+)
 *      GND (Pin 7)  → Breadboard (-)
 *      RX  (Pin 2)  → ESP32 G27
 *      TX  (Pin 3)  → ESP32 G26
 *      SPK1 & SPK2  → Speaker
 *
 *    HC-SR04 (Sensor Jarak)
 *      VCC  → Breadboard (+)
 *      GND  → Breadboard (-)
 *      Trig → ESP32 G5
 *      Echo → ESP32 G18
 *
 *    MQ-135 (Kualitas Udara)
 *      VCC → Breadboard (+)
 *      GND → Breadboard (-)
 *      AO  → ESP32 G34
 *      DO  → tidak dipakai
 *
 *  BLE UUID:
 *    Service  : abcdef00-1234-5678-1234-56789abcdef0
 *    TX (Notify) : abcdef01-...  ESP32 → App
 *    RX (Write)  : abcdef02-...  App → ESP32
 *
 *  Format JSON yang dikirim ESP32 → App:
 *    {"type":"status","running":bool,"paused":bool,"phase":"focus/short/long/idle",
 *     "secsLeft":int,"sess":int,"cycles":int,"vol":int,"adc":int,"user":bool,"dfOk":bool}
 *    {"type":"air","adc":int}
 *    {"type":"user","present":bool}
 *
 *  Perintah dari App → ESP32 (JSON):
 *    {"cmd":"start"}
 *    {"cmd":"pause"}
 *    {"cmd":"resume"}
 *    {"cmd":"reset"}
 *    {"cmd":"config","focus":25,"short":5,"long_":15,"cycles":4,"vol":20}
 *    {"cmd":"volume","value":20}
 *    {"cmd":"audio","track":1}
 *
 *  Audio SD Card (FAT32, nama 4-digit):
 *    0001.mp3 → Opening / Mulai Fokus
 *    0002.mp3 → Waktu Istirahat
 *    0003.mp3 → User Pergi / Jeda
 *    0004.mp3 → User Kembali
 *    0005.mp3 → Kualitas Udara Buruk
 *    0006.mp3 → Sesi Selesai Sempurna
 *
 *  Library (install via Library Manager):
 *    - DFRobotDFPlayerMini  by DFRobot
 *    - ArduinoJson v6       by Benoit Blanchon
 *    - ESP32 BLE Arduino    (built-in / board package)
 *
 *  DEBUG via Serial Monitor 115200 baud:
 *    Ketik angka 1-6 → putar track audio langsung
 * ============================================================
 */

#include <Arduino.h>
#include <ArduinoJson.h>
#include <HardwareSerial.h>
#include <DFRobotDFPlayerMini.h>
#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLEUtils.h>
#include <BLE2902.h>

// ─── BLE UUID ────────────────────────────────────────────────
#define SVC_UUID     "abcdef00-1234-5678-1234-56789abcdef0"
#define CHAR_TX_UUID "abcdef01-1234-5678-1234-56789abcdef0"  // ESP32 → App (notify)
#define CHAR_RX_UUID "abcdef02-1234-5678-1234-56789abcdef0"  // App → ESP32 (write)

// ─── PIN ─────────────────────────────────────────────────────
#define TRIG_PIN    5    // HC-SR04 Trigger
#define ECHO_PIN   18    // HC-SR04 Echo
#define MQ135_PIN  34    // MQ-135 Analog Output (ADC1 CH6, input-only)
#define DF_RX      16    // ESP32 RX ← DFPlayer TX (Pin 3) → G26 di rangkaian
#define DF_TX      17    // ESP32 TX → DFPlayer RX (Pin 2) → G27 di rangkaian
// CATATAN: DF_RX=16, DF_TX=17 adalah pin UART2 default ESP32.
// Sesuaikan dengan hasil test kode DFPlayer yang sudah jalan.

// ─── AUDIO TRACK ─────────────────────────────────────────────
#define AUDIO_OPENING    1   // 0001.mp3 Opening / Mulai Fokus
#define AUDIO_ISTIRAHAT  2   // 0002.mp3 Waktu Istirahat
#define AUDIO_PERGI      3   // 0003.mp3 User Pergi / Jeda
#define AUDIO_KEMBALI    4   // 0004.mp3 User Kembali
#define AUDIO_UDARA      5   // 0005.mp3 Kualitas Udara Buruk
#define AUDIO_SELESAI    6   // 0006.mp3 Sesi Selesai Sempurna

// ─── KONFIGURASI SENSOR ───────────────────────────────────────
#define USER_ABSENT_THRESHOLD_CM  80     // cm — jika > ini, user dianggap pergi
#define USER_CONFIRM_COUNT         3     // konfirmasi N kali berturut sebelum state berubah
#define AIR_ALERT_ADC           2500     // ADC threshold udara buruk (>= 2500 = buruk)
#define AIR_HYSTERESIS           100     // hysteresis ADC agar tidak bolak-balik alert

// ─── INTERVAL TIMER ──────────────────────────────────────────
#define SR04_INTERVAL_MS    1000UL   // cek jarak tiap 1 detik
#define AIR_INTERVAL_MS     8000UL   // cek udara tiap 8 detik
#define HEARTBEAT_MS        5000UL   // kirim status ke App tiap 5 detik
#define AUDIO_GUARD_MS      4500UL   // estimasi durasi 1 audio file (guard)

// ─── OBJEK ───────────────────────────────────────────────────
HardwareSerial       dfSerial(2);    // UART2
DFRobotDFPlayerMini  dfplayer;

BLEServer*           pServer  = nullptr;
BLECharacteristic*   pCharTx  = nullptr;
bool                 bleConn  = false;
bool                 dfOk     = false;

// ─── POMODORO CONFIG (dapat diubah dari App) ─────────────────
struct PomoCFG {
  int focusSec  = 25 * 60;
  int shortSec  =  5 * 60;
  int longSec   = 15 * 60;
  int cycles    = 4;
  int volume    = 20;
} cfg;

// ─── POMODORO STATE ───────────────────────────────────────────
enum Phase { IDLE, FOCUS, SHORT_REST, LONG_REST };
Phase         phase       = IDLE;
bool          running     = false;
bool          paused      = false;
int           secsLeft    = 0;
int           sessNow     = 1;
unsigned long lastTick    = 0;

// ─── KEHADIRAN USER (HC-SR04) ─────────────────────────────────
bool          userPresent   = true;
int           absentCount   = 0;
int           presentCount  = 0;
unsigned long lastSR04Ms    = 0;

// ─── KUALITAS UDARA (MQ-135) ─────────────────────────────────
int           lastADC       = 0;    // nilai ADC raw (0–4095)
bool          airAlerted    = false;
unsigned long lastAirMs     = 0;

// ─── AUDIO ────────────────────────────────────────────────────
unsigned long lastAudioMs   = 0;
bool          audioPlaying  = false;
int           pendingTrack  = 0;

// ─── HEARTBEAT ────────────────────────────────────────────────
unsigned long lastHeartMs   = 0;

// ═════════════════════════════════════════════════════════════
//  DEKLARASI FUNGSI (forward declaration)
// ═════════════════════════════════════════════════════════════
void doStart();
void doPause();
void doResume();
void doReset();
void onFocusDone();
void onRestDone();
void playAudio(int track);
void bleSendRaw(const char* json);
void sendAir(int adc);
void sendUser(bool present);
void sendStatus();
void checkSR04();
void checkAir();
long measureCm();
int  readADC();
void handleSerialDebug();

// ═════════════════════════════════════════════════════════════
//  BLE CALLBACK — KONEKSI
// ═════════════════════════════════════════════════════════════
class ServerCB : public BLEServerCallbacks {
  void onConnect(BLEServer*) override {
    bleConn = true;
    Serial.println("[BLE] App terhubung");
    delay(300);
    sendStatus();
  }
  void onDisconnect(BLEServer*) override {
    bleConn = false;
    Serial.println("[BLE] App terputus — advertising ulang");
    BLEDevice::startAdvertising();
  }
};

// ═════════════════════════════════════════════════════════════
//  BLE CALLBACK — TERIMA PERINTAH DARI APP
// ═════════════════════════════════════════════════════════════
class RxCB : public BLECharacteristicCallbacks {
  void onWrite(BLECharacteristic* c) override {
    String msg = c->getValue().c_str();
    if (msg.length() == 0) return;
    Serial.println("[CMD] " + msg);

    StaticJsonDocument<256> doc;
    if (deserializeJson(doc, msg) != DeserializationError::Ok) {
      Serial.println("[CMD] JSON parse error");
      return;
    }

    String cmd = doc["cmd"] | "";

    if (cmd == "start") {
      applyCfgFromDoc(doc);
      doStart();
    }
    else if (cmd == "config") {
      applyCfgFromDoc(doc);
      Serial.printf("[CFG] F=%dm S=%dm L=%dm C=%d V=%d\n",
        cfg.focusSec/60, cfg.shortSec/60, cfg.longSec/60, cfg.cycles, cfg.volume);
      sendStatus();
    }
    else if (cmd == "pause")  { doPause(); }
    else if (cmd == "resume") { doResume(); }
    else if (cmd == "reset")  { doReset(); }
    else if (cmd == "volume") {
      int vol = constrain((int)(doc["value"] | cfg.volume), 0, 30);
      cfg.volume = vol;
      if (dfOk) dfplayer.volume(vol);
      sendStatus();
    }
    else if (cmd == "audio") {
      int track = doc["track"] | 0;
      if (track >= 1 && track <= 6) playAudio(track);
    }
  }

  void applyCfgFromDoc(StaticJsonDocument<256>& doc) {
    if (doc.containsKey("focus"))  cfg.focusSec = (int)(doc["focus"])  * 60;
    if (doc.containsKey("short"))  cfg.shortSec = (int)(doc["short"])  * 60;
    if (doc.containsKey("long_"))  cfg.longSec  = (int)(doc["long_"])  * 60;
    if (doc.containsKey("cycles")) cfg.cycles   = (int)(doc["cycles"]);
    if (doc.containsKey("vol")) {
      cfg.volume = constrain((int)(doc["vol"]), 0, 30);
      if (dfOk) dfplayer.volume(cfg.volume);
    }
  }
};

// ═════════════════════════════════════════════════════════════
//  SETUP
// ═════════════════════════════════════════════════════════════
void setup() {
  Serial.begin(115200);
  Serial.println("\n=============================================");
  Serial.println("  FocusSense — LearnCore Pomodoro v2.0");
  Serial.println("  HC-SR04 | MQ-135 | DFPlayer Mini | BLE");
  Serial.println("=============================================");

  // ── HC-SR04 ──
  pinMode(TRIG_PIN, OUTPUT);
  pinMode(ECHO_PIN, INPUT);
  digitalWrite(TRIG_PIN, LOW);
  Serial.println("[HC-SR04] OK  (Trig=G5, Echo=G18)");

  // ── MQ-135 ── (GPIO 34 input-only, tidak perlu pinMode)
  Serial.println("[MQ-135]  Warm-up 3 detik...");
  delay(3000);
  lastADC = readADC();
  Serial.printf("[MQ-135]  Siap. Bacaan awal ADC: %d\n", lastADC);

  // ── DFPlayer Mini — pakai cara yang sama dengan kode test ──
  // HardwareSerial(2) dengan pin RX=DF_RX, TX=DF_TX
  dfSerial.begin(9600, SERIAL_8N1, DF_RX, DF_TX);
  delay(1000);  // tunggu DFPlayer boot (lebih lama = lebih stabil)
  Serial.print("[DFPlayer] Init... ");
  if (dfplayer.begin(dfSerial)) {  // TANPA parameter extra, persis seperti kode test
    dfOk = true;
    dfplayer.volume(cfg.volume);
    dfplayer.EQ(DFPLAYER_EQ_NORMAL);
    Serial.printf("OK  (Volume=%d)\n", cfg.volume);
  } else {
    Serial.println("GAGAL!");
    Serial.println("  >> Cek: SD Card terpasang & format FAT32");
    Serial.printf("  >> Cek: DFPlayer TX(Pin3) → ESP32 G%d\n", DF_RX);
    Serial.printf("  >> Cek: DFPlayer RX(Pin2) → ESP32 G%d\n", DF_TX);
    Serial.println("  >> Cek: VCC DFPlayer ke 3.3V atau 5V");
    dfOk = false;
  }

  // ── BLE ──
  Serial.println("[BLE] Init...");
  BLEDevice::init("LEARNCORE-POMO");
  pServer = BLEDevice::createServer();
  pServer->setCallbacks(new ServerCB());

  BLEService* svc = pServer->createService(SVC_UUID);

  pCharTx = svc->createCharacteristic(CHAR_TX_UUID, BLECharacteristic::PROPERTY_NOTIFY);
  pCharTx->addDescriptor(new BLE2902());

  BLECharacteristic* pCharRx = svc->createCharacteristic(
    CHAR_RX_UUID,
    BLECharacteristic::PROPERTY_WRITE | BLECharacteristic::PROPERTY_WRITE_NR
  );
  pCharRx->setCallbacks(new RxCB());

  svc->start();

  BLEAdvertising* adv = BLEDevice::getAdvertising();
  adv->addServiceUUID(SVC_UUID);
  adv->setScanResponse(true);
  adv->setMinPreferred(0x06);
  BLEDevice::startAdvertising();

  Serial.println("[BLE] Advertising: LEARNCORE-POMO");
  Serial.println("---------------------------------------------");
  Serial.println("DEBUG: Ketik 1-6 di Serial Monitor → putar audio");
  Serial.println("---------------------------------------------\n");
}

// ═════════════════════════════════════════════════════════════
//  LOOP
// ═════════════════════════════════════════════════════════════
void loop() {
  unsigned long now = millis();

  // ── Debug serial: ketik 1-6 untuk test audio ──
  handleSerialDebug();

  // ── Audio guard: reset flag setelah estimasi durasi ──
  if (audioPlaying && now - lastAudioMs >= AUDIO_GUARD_MS) {
    audioPlaying = false;
    if (pendingTrack > 0) {
      int t = pendingTrack;
      pendingTrack = 0;
      playAudio(t);
    }
  }

  // ── HC-SR04: cek kehadiran setiap 1 detik ──
  if (now - lastSR04Ms >= SR04_INTERVAL_MS) {
    lastSR04Ms = now;
    checkSR04();
  }

  // ── MQ-135: cek kualitas udara setiap 8 detik ──
  if (now - lastAirMs >= AIR_INTERVAL_MS) {
    lastAirMs = now;
    checkAir();
  }
  // ── Pomodoro tick: setiap 1 detik ──
  if (running && !paused && now - lastTick >= 1000UL) {
    lastTick = now;
    if (secsLeft > 0) {
      secsLeft--;
    } else {
      if (phase == FOCUS) onFocusDone();
      else                onRestDone();
    }
  }

  // ── Heartbeat ke App setiap 5 detik ──
  if (bleConn && now - lastHeartMs >= HEARTBEAT_MS) {
    lastHeartMs = now;
    sendStatus();
  }
}

// ═════════════════════════════════════════════════════════════
//  DEBUG SERIAL — TEST AUDIO (ketik 1-6)
// ═════════════════════════════════════════════════════════════
void handleSerialDebug() {
  if (!Serial.available()) return;

  int track = Serial.parseInt();
  while (Serial.available()) Serial.read();  // bersihkan buffer

  if (track >= 1 && track <= 6) {
    Serial.printf("[DEBUG] Putar track %d: 000%d.mp3\n", track, track);
    // Bypass audio guard untuk test langsung
    if (dfOk) {
      dfplayer.play(track);
      audioPlaying  = true;
      lastAudioMs   = millis();
      pendingTrack  = 0;
    } else {
      Serial.println("[DEBUG] DFPlayer tidak siap!");
    }
  } else if (track > 0) {
    Serial.println("[DEBUG] Track tidak valid! Masukkan 1-6");
    Serial.println("  1=Opening  2=Istirahat  3=Pergi");
    Serial.println("  4=Kembali  5=UdaraBuruk 6=Selesai");
  }
}

// ═════════════════════════════════════════════════════════════
//  POMODORO CONTROLS
// ═════════════════════════════════════════════════════════════
void doStart() {
  phase    = FOCUS;
  secsLeft = cfg.focusSec;
  sessNow  = 1;
  running  = true;
  paused   = false;
  lastTick = millis();
  playAudio(AUDIO_OPENING);
  sendStatus();
  Serial.printf("[POMO] START — Fokus %dm, %d sesi\n", cfg.focusSec/60, cfg.cycles);
}

void doPause() {
  if (!running || paused) return;
  paused = true;
  sendStatus();
  Serial.println("[POMO] PAUSE");
}

void doResume() {
  if (!running || !paused) return;
  paused   = false;
  lastTick = millis();
  sendStatus();
  Serial.println("[POMO] RESUME");
}

void doReset() {
  running  = false;
  paused   = false;
  phase    = IDLE;
  secsLeft = 0;
  sessNow  = 1;
  sendStatus();
  Serial.println("[POMO] RESET");
}

void onFocusDone() {
  Serial.printf("[POMO] Fokus selesai — Sesi %d/%d\n", sessNow, cfg.cycles);
  if (sessNow >= cfg.cycles) {
    // Semua siklus selesai → istirahat panjang
    phase    = LONG_REST;
    secsLeft = cfg.longSec;
    sessNow  = 1;
    playAudio(AUDIO_SELESAI);
    pendingTrack = AUDIO_ISTIRAHAT;
    Serial.printf("[POMO] Semua siklus selesai! Istirahat panjang %dm\n", cfg.longSec/60);
  } else {
    // Sesi selesai → istirahat pendek
    sessNow++;
    phase    = SHORT_REST;
    secsLeft = cfg.shortSec;
    playAudio(AUDIO_ISTIRAHAT);
    Serial.printf("[POMO] Istirahat pendek %dm, sesi berikutnya: %d\n", cfg.shortSec/60, sessNow);
  }
  sendStatus();
}

void onRestDone() {
  phase    = FOCUS;
  secsLeft = cfg.focusSec;
  playAudio(AUDIO_OPENING);
  sendStatus();
  Serial.printf("[POMO] Istirahat selesai — mulai fokus sesi %d\n", sessNow);
}

// ═════════════════════════════════════════════════════════════
//  HC-SR04 — DETEKSI KEHADIRAN (konfirmasi 3x)
// ═════════════════════════════════════════════════════════════
long measureCm() {
  digitalWrite(TRIG_PIN, LOW);
  delayMicroseconds(2);
  digitalWrite(TRIG_PIN, HIGH);
  delayMicroseconds(10);
  digitalWrite(TRIG_PIN, LOW);

  long duration = pulseIn(ECHO_PIN, HIGH, 30000UL);  // timeout 30ms
  if (duration == 0) return 999;                       // tidak ada pantulan

  long cm = duration * 0.034 / 2;
  return (cm >= 2 && cm <= 400) ? cm : 999;
}

void checkSR04() {
  long cm = measureCm();
  Serial.printf("[HC-SR04] %ld cm\n", cm);

  bool detected = (cm > 0 && cm <= USER_ABSENT_THRESHOLD_CM);

  if (detected) {
    presentCount++;
    absentCount = 0;
    if (!userPresent && presentCount >= USER_CONFIRM_COUNT) {
      userPresent  = true;
      presentCount = 0;
      Serial.printf("[USER] KEMBALI — %ld cm\n", cm);
      playAudio(AUDIO_KEMBALI);
      sendUser(true);
      // Resume timer jika sedang paused karena pergi
      if (running && paused) doResume();
    }
  } else {
    absentCount++;
    presentCount = 0;
    if (userPresent && absentCount >= USER_CONFIRM_COUNT) {
      userPresent = false;
      absentCount = 0;
      Serial.printf("[USER] PERGI — %ld cm\n", cm);
      playAudio(AUDIO_PERGI);
      sendUser(false);
      // Jeda timer otomatis
      if (running && !paused) doPause();
    }
  }
}

// ═════════════════════════════════════════════════════════════
//  MQ-135 — KUALITAS UDARA (ADC raw, tanpa konversi PPM)
// ═════════════════════════════════════════════════════════════
int readADC() {
  // Rata-rata 8 sampel untuk stabilitas
  long sum = 0;
  for (int i = 0; i < 8; i++) {
    sum += analogRead(MQ135_PIN);
    delay(5);
  }
  int raw = (int)(sum / 8);
  Serial.printf("[MQ-135]  ADC: %d\n", raw);
  return raw;
}

void checkAir() {
  lastADC = readADC();
  sendAir(lastADC);

  // ADC >= 2500 → udara buruk
  if (lastADC >= AIR_ALERT_ADC && !airAlerted) {
    airAlerted = true;
    Serial.printf("[AIR] BURUK! ADC=%d — putar audio udara buruk\n", lastADC);
    playAudio(AUDIO_UDARA);
  } else if (lastADC < (AIR_ALERT_ADC - AIR_HYSTERESIS)) {
    airAlerted = false;
  }
}

// ═════════════════════════════════════════════════════════════
//  AUDIO — antrian 1 track, tidak tumpang tindih
// ═════════════════════════════════════════════════════════════
void playAudio(int track) {
  if (!dfOk) {
    Serial.printf("[AUDIO] Skip track %d — DFPlayer tidak siap\n", track);
    return;
  }
  if (audioPlaying) {
    pendingTrack = track;
    Serial.printf("[AUDIO] Antri track %d\n", track);
    return;
  }
  dfplayer.play(track);
  audioPlaying  = true;
  lastAudioMs   = millis();
  pendingTrack  = 0;
  Serial.printf("[AUDIO] Putar: 000%d.mp3\n", track);
}

// ═════════════════════════════════════════════════════════════
//  BLE SEND
// ═════════════════════════════════════════════════════════════
void bleSendRaw(const char* json) {
  if (!bleConn || !pCharTx) return;
  pCharTx->setValue(json);
  pCharTx->notify();
}

// Kirim data sensor udara
void sendAir(int adc) {
  StaticJsonDocument<96> doc;
  doc["type"] = "air";
  doc["adc"]  = adc;
  char buf[96];
  serializeJson(doc, buf);
  bleSendRaw(buf);
  Serial.printf("[BLE TX] air adc=%d\n", adc);
}

// Kirim event kehadiran user
void sendUser(bool present) {
  StaticJsonDocument<96> doc;
  doc["type"]    = "user";
  doc["present"] = present;
  char buf[96];
  serializeJson(doc, buf);
  bleSendRaw(buf);
  Serial.printf("[BLE TX] user present=%s\n", present ? "true" : "false");
}

// Kirim status lengkap (heartbeat)
void sendStatus() {
  StaticJsonDocument<256> doc;
  doc["type"]     = "status";
  doc["running"]  = running;
  doc["paused"]   = paused;

  const char* phaseStr;
  switch (phase) {
    case FOCUS:      phaseStr = "focus"; break;
    case SHORT_REST: phaseStr = "short"; break;
    case LONG_REST:  phaseStr = "long";  break;
    default:         phaseStr = "idle";  break;
  }
  doc["phase"]    = phaseStr;
  doc["secsLeft"] = secsLeft;
  doc["sess"]     = sessNow;
  doc["cycles"]   = cfg.cycles;
  doc["vol"]      = cfg.volume;
  doc["adc"]      = lastADC;
  doc["user"]     = userPresent;
  doc["dfOk"]     = dfOk;

  char buf[256];
  serializeJson(doc, buf);
  bleSendRaw(buf);
}
