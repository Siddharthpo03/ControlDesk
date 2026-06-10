# ControlDesk 🖥️📱

> Turn your Android phone into a fully-featured PC remote controller — mouse, keyboard, media controls, and gaming mode.

![Platform](https://img.shields.io/badge/Platform-Android-green)
![Language](https://img.shields.io/badge/Language-Kotlin-purple)
![Server](https://img.shields.io/badge/Server-Python-blue)
![Status](https://img.shields.io/badge/Status-In%20Development-orange)

---

## ✨ Features

### 🖥️ Standard Mode (Laptop Trackpad)

| Gesture                      | Action                  |
| ---------------------------- | ----------------------- |
| 1 finger drag                | Move cursor             |
| 1 finger tap                 | Left click              |
| 2 finger tap                 | Right click             |
| 3 finger tap                 | Middle click            |
| 2 finger scroll              | Vertical scroll         |
| 2 finger scroll (horizontal) | Horizontal scroll       |
| Pinch in/out                 | Zoom out/in             |
| 3 finger swipe up            | Task view               |
| 3 finger swipe down          | Show desktop            |
| 3 finger swipe left/right    | Switch virtual desktops |

### 🖱️ Hybrid Mode (Physical Mouse)

Everything in Standard Mode plus:
| Feature | Action |
|---------|--------|
| Left / Middle / Right buttons | Click |
| Hold & Drag button | Click and drag |

### 🎮 Gaming Mode _(Coming Soon)_

- Virtual joystick
- Customizable buttons
- Volume buttons as triggers
- Gyroscope aiming

---

## 🏗️ Architecture

```
Android App (Kotlin/Jetpack Compose)
        ↕ WebSocket (WiFi)
PC Server (Python)
        ↕ pynput
Mouse / Keyboard / Media Controls
```

---

## 🚀 Getting Started

### Prerequisites

- Android phone (Android 8.0+)
- Windows PC
- Python 3.x
- Both devices on the **same WiFi network**

### PC Server Setup

1. Install dependencies:

```bash
pip install pynput websockets
```

2. Navigate to server folder:

```bash
cd ControlDesk-Server
```

3. Run the server:

```bash
python server.py
```

Server starts on port `5000`.

### Android App Setup

1. Open project in Android Studio
2. Build and install on your Android device
3. On first launch, follow the one-time setup guide
4. Enter your PC's IP address and tap **Connect**

> 💡 Find your PC's IP by running `ipconfig` in CMD and looking for the WiFi IPv4 address.

---

## 📁 Project Structure

```
ControlDesk/
├── app/
│   └── src/main/java/com/example/controldesk/
│       ├── MainActivity.kt           # App entry, navigation, connect screen
│       ├── ControlDeskWSClient.kt    # WebSocket client
│       ├── GestureDetector.kt        # Multi-finger gesture engine
│       ├── TouchpadScreen.kt         # Main touchpad UI
│       ├── ModeSelectionScreen.kt    # Standard / Hybrid mode picker
│       └── SetupGuideScreen.kt       # First-time setup guide
└── ControlDesk-Server/
    └── server.py                     # Python WebSocket server
```

---

## 🗺️ Roadmap

- [x] WiFi connection
- [x] Mouse movement
- [x] Left / Right / Middle click
- [x] 2 finger scroll (vertical + horizontal)
- [x] Pinch to zoom
- [x] 3 finger swipe gestures
- [x] Standard and Hybrid modes
- [x] Portrait and Landscape layouts
- [x] First-time setup guide
- [ ] 4 finger gestures
- [ ] Keyboard screen
- [ ] Media controls screen
- [ ] Gaming mode
- [ ] Bluetooth support
- [ ] Auto PC discovery (no manual IP)
- [ ] Play Store release

---

## 🛠️ Tech Stack

| Component     | Technology                 |
| ------------- | -------------------------- |
| Android App   | Kotlin, Jetpack Compose    |
| PC Server     | Python, asyncio            |
| Communication | WebSocket (java-websocket) |
| Input Control | pynput                     |

---

## 👨‍💻 Developer

**Siddharth** — NIT Warangal, CSE 2027

---

## 📄 License

This project is currently private and under active development.
