# 🍅 LearnCore Pomodoro

**Aplikasi Pembantu Penyelesaian Tugas yang Terintegrasi dengan Teknik Pomodoro**

[Demo Video LearnCore](https://drive.google.com/file/d/1LL6XhwTj5TjlWiqS5ulSijIbbw9QqL8M/view?usp=drivesdk)
[Laporan](https://docs.google.com/document/d/1HCo1CGuDq7qwgjyyX_Im1n3qjY29csoU1gaG1s59Xo4/edit?tab=t.0)

> Proyek IoT untuk mata kuliah **Komputasi Pervasif (IF25-40311)**  
> Program Studi Teknik Informatika — Institut Teknologi Sumatera (ITERA) · 2026

---

## 👥 Tim Pengembang

| Nama | NIM |
|---|---|
| Muhammad Fadhilah Akbar | 123140003 |
| Annisa Al-Qoriah | 123140030 |
| Sigit Kurnia Hartawan | 123140033 |
| Stevanus Cahya Anggara | 123140038 |

**Dosen Pengampu:** Arkham Zahri Rakhman, S.Kom., M.Eng.

---

## 📖 Deskripsi Proyek

LearnCore Pomodoro adalah sistem IoT berbasis **ESP32** yang membantu pengguna menerapkan teknik manajemen waktu Pomodoro secara otomatis dan cerdas. Sistem ini mendeteksi kehadiran pengguna secara real-time, memantau kualitas udara ruangan, memberikan notifikasi suara, dan dapat dikontrol sepenuhnya melalui **dashboard berbasis Bluetooth Low Energy (BLE)** di smartphone.

---

## ✨ Fitur Utama

- ⏱️ **Timer Pomodoro Otomatis** — Siklus fokus dan istirahat yang berjalan otomatis sesuai teknik Pomodoro
- 👤 **Deteksi Kehadiran** — Sensor ultrasonik HC-SR04 mendeteksi apakah pengguna hadir; timer otomatis dijeda jika pengguna pergi dan dilanjutkan saat kembali
- 🌬️ **Pemantauan Kualitas Udara** — Sensor MQ-135 memantau kualitas udara dan membunyikan peringatan jika kondisi buruk
- 🔊 **Notifikasi Suara** — Audio informatif via DFPlayer Mini untuk setiap perubahan kondisi
- 📱 **Dashboard BLE** — Kontrol dan pantau sesi dari smartphone secara nirkabel
- ⚙️ **Parameter Terkonfigurasi** — Durasi fokus, istirahat, jumlah siklus, batas jarak, dan threshold udara dapat diubah dari dashboard

---

## 🏗️ Arsitektur Sistem

Sistem dibagi menjadi tiga lapisan:

```
┌──────────────────────────────────────────┐
│         Lapisan Antarmuka Pengguna       │
│   Dashboard BLE (Web App / Smartphone)   │
└────────────────────┬─────────────────────┘
                     │ BLE (JSON)
┌────────────────────▼─────────────────────┐
│         Lapisan Pemrosesan               │
│    ESP32 WROOM-32 (Firmware LearnCore)   │
└────┬──────────┬────────────┬─────────────┘
     │          │            │
┌────▼───┐ ┌───▼────┐ ┌─────▼──────┐
│HC-SR04 │ │MQ-135  │ │DFPlayer    │
│Sensor  │ │Sensor  │ │Mini+Speaker│
│Jarak   │ │Udara   │ │            │
└────────┘ └────────┘ └────────────┘
         Lapisan Sensor & Output
```

---

## 🔩 Komponen Hardware

| No. | Komponen | Jumlah | Keterangan |
|-----|----------|--------|------------|
| 1 | ESP32 WROOM-32 | 1 | Mikrokontroler utama (BLE & Wi-Fi) |
| 2 | HC-SR04 | 1 | Sensor ultrasonik, deteksi kehadiran |
| 3 | MQ-135 | 1 | Sensor kualitas udara (gas & asap) |
| 4 | DFPlayer Mini | 1 | Modul pemutar audio MP3 via UART |
| 5 | Speaker 8Ω | 1 | Output suara notifikasi & alarm |
| 6 | Step-Down Converter (LM2596) | 1 | Penurun tegangan baterai ke 5V/3.3V |
| 7 | Baterai 9V / Pack Li-Ion | 1 | Sumber daya portabel |
| 8 | Saklar ON/OFF | 1 | Kontrol daya sistem |
| 9 | Breadboard | 1 | Media koneksi komponen |
| 10 | Kabel Jumper | Secukupnya | Penghubung antar komponen |
| 11 | MicroSD (FAT32) | 1 | Penyimpanan file audio MP3 |

---

## 🔌 Koneksi Pin

| Komponen | Pin Komponen | Pin ESP32 | Keterangan |
|----------|-------------|-----------|------------|
| HC-SR04 | VCC | 3.3V/5V | Daya |
| HC-SR04 | GND | GND | Ground |
| HC-SR04 | TRIG | GPIO 5 | Trigger pulsa |
| HC-SR04 | ECHO | GPIO 18 | Penerima pantulan |
| MQ-135 | VCC | 5V | Daya |
| MQ-135 | GND | GND | Ground |
| MQ-135 | AO | GPIO 34 | Output analog ADC |
| DFPlayer Mini | VCC (Pin 1) | 5V | Daya |
| DFPlayer Mini | GND (Pin 7) | GND | Ground |
| DFPlayer Mini | RX (Pin 2) | GPIO 17 | Serial TX dari ESP32 |
| DFPlayer Mini | TX (Pin 3) | GPIO 16 | Serial RX ke ESP32 |
| DFPlayer Mini | SPK1 & SPK2 | — | Ke Speaker 8Ω |

---

## 🎵 Daftar Track Audio

File audio disimpan di kartu MicroSD dalam folder root dengan format nama 4 digit.

| Track | Nama File | Kondisi Pemutaran |
|-------|-----------|-------------------|
| 1 | `0001.mp3` | Opening / Mulai Fokus |
| 2 | `0002.mp3` | Waktu Istirahat |
| 3 | `0003.mp3` | User Pergi / Timer Dijeda |
| 4 | `0004.mp3` | User Kembali / Timer Dilanjutkan |
| 5 | `0005.mp3` | Peringatan Kualitas Udara Buruk |
| 6 | `0006.mp3` | Sesi Selesai Sempurna |

---

## ⚙️ Parameter Konfigurasi Default

Parameter berikut dapat diubah melalui dashboard BLE:

| Parameter | Default | Rentang | Keterangan |
|-----------|---------|---------|------------|
| `focusMin` | 25 menit | 5–90 menit | Durasi satu sesi fokus |
| `shortMin` | 5 menit | 1–30 menit | Durasi istirahat pendek |
| `longMin` | 15 menit | 5–60 menit | Durasi istirahat panjang |
| `cycles` | 4 siklus | 1–8 | Jumlah sesi fokus sebelum istirahat panjang |
| `volume` | 20 | 0–30 | Volume output DFPlayer Mini |
| `presenceCm` | 80 cm | 20–300 cm | Batas jarak deteksi kehadiran |
| `airBadADC` | 3000 | 500–4095 | Threshold ADC kualitas udara buruk |

---

## 🔄 Cara Kerja Sistem

### Alur Umum

1. ESP32 menyala → kalibrasi MQ-135 (warm-up 3 detik) → inisialisasi DFPlayer → BLE advertising aktif
2. Buka dashboard di smartphone → hubungkan via BLE (nama: **LEARNCORE-POMO**)
3. Atur parameter → tekan **Mulai** → sesi Pomodoro berjalan

### Siklus Pomodoro

```
[FOKUS 25 mnt] → [ISTIRAHAT PENDEK 5 mnt] → (ulangi N siklus)
                                                      ↓
                                          [ISTIRAHAT PANJANG 15 mnt]
                                                      ↓
                                              [Reset, Mulai Ulang]
```

### Deteksi Kehadiran (HC-SR04)

- Sensor dibaca setiap **800 ms**
- Diperlukan **3 pembacaan berturut-turut** untuk mengubah status (mencegah false positive)
- Jarak ≤ `presenceCm` → pengguna **hadir**, timer berjalan
- Jarak > `presenceCm` → pengguna **pergi**, timer dijeda otomatis (hanya pada fase fokus)

### Pemantauan Kualitas Udara (MQ-135)

- Sensor dibaca setiap **3 detik** (rata-rata 8 sampel ADC)
- ADC ≥ `airBadADC` → peringatan suara diputar, notifikasi dikirim ke dashboard
- Peringatan diulang setiap **8 detik** selama udara masih buruk
- Udara kembali baik jika ADC turun di bawah `airBadADC − 200`

---

## 📡 Komunikasi BLE

ESP32 bertindak sebagai **BLE Server** dengan dua characteristic:

| Characteristic | Arah | Fungsi |
|----------------|------|--------|
| TX (Notify) | ESP32 → Smartphone | Kirim status JSON setiap 5 detik (heartbeat) atau saat ada perubahan |
| RX (Write) | Smartphone → ESP32 | Terima perintah kontrol JSON |

### Contoh Format Perintah JSON

```json
{ "cmd": "start", "focus": 25, "short": 5, "cycles": 4 }
{ "cmd": "pause" }
{ "cmd": "resume" }
{ "cmd": "reset" }
{ "cmd": "config", "presenceCm": 80, "airBadADC": 3000 }
{ "cmd": "volume", "val": 20 }
{ "cmd": "addtime", "sec": 60 }
```

---

## 🛠️ Instalasi & Setup

### Library Arduino yang Diperlukan

- `DFRobotDFPlayerMini`
- `ArduinoJson` v6
- `ESP32 BLE Arduino` (built-in di ESP32 board package)

### Langkah Upload Firmware

1. Install **Arduino IDE** dan tambahkan board ESP32 via Board Manager
2. Install library yang dibutuhkan via Library Manager
3. Buka file `LearnCorePomodoro.ino`
4. Pilih board: **ESP32 Dev Module**
5. Upload ke ESP32

### Persiapan MicroSD

1. Format MicroSD dengan format **FAT32**
2. Salin file audio ke root direktori dengan nama `0001.mp3` s.d. `0006.mp3`
3. Pasang MicroSD ke slot DFPlayer Mini

### Membuka Dashboard

1. Buka file `LearnCore_Pomodoro.html` di browser smartphone yang mendukung **Web Bluetooth API** (disarankan: Chrome/Chromium di Android)
2. Klik tombol **Hubungkan BLE** dan pilih perangkat **LEARNCORE-POMO**
3. Atur parameter dan mulai sesi

> **Catatan:** Web Bluetooth API membutuhkan HTTPS atau localhost. Gunakan browser Chromium di Android untuk kompatibilitas terbaik.

---

## 📂 Struktur File

```
LearnCore-Pomodoro/
├── LearnCorePomodoro.ino       # Firmware ESP32
├── LearnCore_Pomodoro.html     # Dashboard Web BLE
├── LearnCore.apk               # Aplikasi Android (opsional)
├── audio/
│   ├── 0001.mp3                # Opening / Mulai Fokus
│   ├── 0002.mp3                # Waktu Istirahat
│   ├── 0003.mp3                # User Pergi
│   ├── 0004.mp3                # User Kembali
│   ├── 0005.mp3                # Kualitas Udara Buruk
│   └── 0006.mp3                # Sesi Selesai Sempurna
└── docs/
    └── LearnCore-Pomodoro_Komputasi_Perpasif.pdf
```

---

## 📊 Spesifikasi Teknis

| Aspek | Detail |
|-------|--------|
| Mikrokontroler | ESP32 WROOM-32 (Xtensa LX6, 32-bit) |
| Konektivitas | Bluetooth Low Energy (BLE) 4.2 |
| Protokol Data | GATT + JSON over BLE |
| Jangkauan BLE | ± 10 meter |
| Frekuensi Sensor Jarak | 800 ms |
| Frekuensi Sensor Udara | 3 detik |
| Konfirmasi Deteksi | 3× pembacaan berturut-turut |
| Format Audio | MP3 (FAT32 MicroSD) |
| Sumber Daya | Baterai 9V / Pack Li-Ion via Step-Down 5V |

---

## 🚀 Rencana Pengembangan

- [ ] Penyimpanan histori sesi ke flash internal ESP32 (NVS) atau kartu SD
- [ ] Aplikasi mobile native Android/iOS sebagai pengganti dashboard web
- [ ] Integrasi sensor tambahan: suhu & kelembapan (DHT22), sensor cahaya (BH1750)
- [ ] Mode Multi-User dengan profil konfigurasi berbeda
- [ ] Sensor PIR sebagai konfirmasi sekunder deteksi kehadiran
- [ ] Layar OLED lokal untuk menampilkan status tanpa buka smartphone

---

## 📚 Referensi

1. F. Cirillo, *The Pomodoro Technique*. Crown Currency, 2018.
2. L. Atzori, A. Iera, G. Morabito, "The Internet of Things: A survey," *Computer Networks*, vol. 54, no. 15, 2010.
3. Espressif Systems — [ESP32 Documentation](https://www.espressif.com/sites/default/files/documentation/)
4. Espressif — [Arduino ESP32 BLE Library](https://github.com/espressif/arduino-esp32/tree/master/libraries/BLE)
5. SparkFun Electronics — [sparkfun.com](https://www.sparkfun.com/)
6. DFRobot — [DFPlayer Mini Wiki](https://wiki.dfrobot.com/DFPlayer_Mini_SKU_DFR0299)
7. Bluetooth SIG — [Bluetooth Specifications](https://www.bluetooth.com/specifications/)
8. J. Beningo, *Embedded Software Design*, Apress, 2022.
9. Arduino Reference — [arduino.cc](https://www.arduino.cc/reference/en/)
10. D. Bandyopadhyay, J. Sen, "Internet of Things: Applications and Challenges," *Wireless Personal Communications*, vol. 58, 2011.

---

<p align="center">
  Dibuat dengan ❤️ oleh Tim LearnCore · ITERA 2026
</p>
