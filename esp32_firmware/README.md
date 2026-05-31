# 🔌 FocusSense — ESP32 Firmware

## Library yang Dibutuhkan (Arduino IDE)

Install via **Tools → Manage Libraries**:

| Library | Author | Versi |
|---|---|---|
| `NimBLE-Arduino` | h2zero | ≥ 1.4.0 |
| `DFRobotDFPlayerMini` | DFRobot | ≥ 1.0.6 |

> **Mengapa NimBLE?** Lebih ringan dari ArduinoBLE, stabil untuk ESP32, dan mendukung Nordic UART Service (NUS) yang dipakai app Android.

---

## Board Setting di Arduino IDE

| Setting | Nilai |
|---|---|
| Board | ESP32 Dev Module |
| Upload Speed | 921600 |
| CPU Frequency | 240MHz |
| Flash Size | 4MB |
| Partition Scheme | Default 4MB with spiffs |
| PSRAM | Disabled |

---

## Wiring Ringkas

```
HC-SR04
  VCC  → 3.3V / 5V (Jalur Merah)
  GND  → GND (Jalur Biru)
  TRIG → G5
  ECHO → G18

MQ-135
  VCC  → 5V (Jalur Merah)
  GND  → GND (Jalur Biru)
  AO   → G34

DFPlayer Mini
  VCC (pin1) → 3.3V / 5V (Jalur Merah)
  GND (pin7) → GND (Jalur Biru)
  RX  (pin2) → G27  [ESP32 TX]
  TX  (pin3) → G26  [ESP32 RX]
  SPK1/SPK2  → Speaker
```

---

## Format SD Card untuk DFPlayer

- Format: **FAT32**
- Letakkan file langsung di root SD card:

```
SD Card/
  0001.mp3  → Sesi Fokus Selesai
  0002.mp3  → Istirahat Selesai
  0003.mp3  → Udara Sedang
  0004.mp3  → Udara Buruk
  0005.mp3  → Udara Berbahaya
  0006.mp3  → Pengguna Terdeteksi / Kembali
```

> ⚠️ Nama file **harus 4 digit angka** (`0001.mp3`, bukan `1.mp3`)

---

## Alur Logika Firmware

```
Loop setiap 500ms:
  1. Baca HC-SR04 → jarak (cm)
  2. Baca MQ-135  → nilai ADC (0–4095)
  3. Cek kehadiran:
       jarak < 150cm → Present  → putar 0006.mp3 jika baru datang
       jarak ≥ 150cm > 30 detik → Absent (app handle pause timer)
  4. Cek kualitas udara:
       ADC > 3000 → putar 0005.mp3 (Berbahaya) — max 1x/menit
       ADC > 2000 → putar 0004.mp3 (Buruk)
       ADC > 1000 → putar 0003.mp3 (Sedang)
  5. Kirim BLE Notify (jika app terhubung):
       {"distance": 80.5, "adc": 900, "temp": 0, "humidity": 0}
```

---

## Serial Monitor

Buka Serial Monitor di `115200 baud` untuk debug:

```
[BLE] Advertising sebagai: PomodoroSensor
[BLE] Client terhubung
[SENSOR] Jarak: 82.3 cm | ADC: 756
[PRESENCE] Pengguna terdeteksi!
[DFP] Putar track: 0006.mp3
[BLE] Notify: {"distance":82.3,"adc":756,"temp":0,"humidity":0}
```
