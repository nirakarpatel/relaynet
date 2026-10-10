# RelayNet 📡

> **Offline-First Opportunistic Content Dissemination & Nearby Sharing for Android**  
> Built for the NMIET Project using Kotlin, Jetpack Compose, Material 3, Room, and Bluetooth.

---

## 📖 Overview

**RelayNet** is an offline-first mobile application prototype designed for educational campuses and network-constrained environments. It addresses connectivity divides where students in basements, rural zones, or during network disruptions cannot access critical academic announcements, exam schedules, or campus notices.

### The RelayNet Core Workflow

```
[ Internet Source ]
       │
       ▼ (Fetch & Cache)
 ┌──────────────┐                  Bluetooth / RFCOMM                  ┌──────────────┐
 │   Device A   │  ═════════════════════════════════════════════════►  │   Device B   │
 │   (Online)   │                 Opportunistic Transfer               │  (OFFLINE)   │
 └──────────────┘                                                      └──────────────┘
   • Caches in Room                                                      • Receives via peer
   • Verifies SHA-256                                                    • Saved in Offline Library
   • Shares with peers                                                   • Readable with NO internet
```

1. **Online Synchronization:** Device A connects to the internet and refreshes official campus notices.
2. **Local Persistence:** Data is validated and stored locally in a Room SQLite database with cryptographic content hashes.
3. **Opportunistic Mesh Share:** Device B, completely offline, connects to Device A via Bluetooth and receives eligible cached notices.
4. **Offline Browsing:** Device B browses, searches, and reads the transferred notices offline without requiring internet access.

---

## ✨ Features

- **📱 Jetpack Compose & Material 3 UI:** Clean, responsive design featuring adaptive light/dark themes, category chips, status pills, and bottom navigation.
- **💾 Offline-First Local Storage (Room Database):** All announcements are cached locally in SQLite. Saved and received documents persist through app restarts and force-quits.
- **🔍 Real-Time Search & Filtering:** Filter announcements instantly by category (*Exams*, *Academics*, *Campus Life*, *Notices*, *Research*) or search across titles, summaries, and departments.
- **🤝 Nearby Sharing (Dual Mode):**
  - **Demo Simulation Mode:** Interactive simulation of device discovery, Bluetooth handshakes, and chunked transfer progress (0% → 100%) for single-device demonstrations and presentations.
  - **Real Bluetooth RFCOMM:** Direct peer-to-peer RFCOMM socket transmission between physical Android devices.
- **🔐 Integrity & Verification:** Every notice is tagged with a SHA-256 cryptographic snippet to ensure content integrity during peer-to-peer propagation.
- **📚 Offline Library:** Dedicated library for bookmarked articles and announcements received over nearby peer transfers.
- **🛠 Diagnostics & Simulation Tools:**
  - Toggle simulated offline mode to inspect behavior when internet access is severed.
  - Bluetooth hardware and runtime permission auditor.
  - Reset and restore default demo catalog or wipe local cache.

---

## 🛠 Tech Stack

- **Language:** [Kotlin 2.2](https://kotlinlang.org/)
- **UI Toolkit:** [Jetpack Compose](https://developer.android.com/jetpack/compose) with [Material 3](https://m3.material.io/)
- **Architecture:** Single Activity, MVVM (Model-View-ViewModel), Unidirectional Data Flow (UDF), Repository Pattern
- **Local Persistence:** [Room 2.6](https://developer.android.com/training/data-storage/room) with KSP
- **Asynchronous Operations:** Kotlin Coroutines & `StateFlow`
- **Navigation:** [Navigation Compose](https://developer.android.com/jetpack/compose/navigation)
- **Transport Layer:** Android Bluetooth RFCOMM Classic + BLE Discovery & Permissions
- **Testing:** JUnit 4 unit test suite

---

## 📂 Project Structure

```text
app/src/main/java/com/nmiet/app/
├── MainActivity.kt                  # Entry point, dependency wiring & Compose root
├── navigation/
│   ├── Routes.kt                    # Screen definitions and route constants
│   └── AppNavHost.kt                # Bottom navigation bar & NavHost graph
├── domain/
│   ├── model/
│   │   ├── ContentItem.kt           # Domain entity for campus announcements
│   │   ├── PeerDevice.kt            # Discovered peer model (address, RSSI)
│   │   ├── TransferState.kt         # Sealed state machine for peer transfer
│   │   └── SyncStatus.kt            # Network & synchronization diagnostics
│   └── repository/
│       └── ContentRepository.kt     # Repository interface contract
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt           # Room Database definition
│   │   ├── ContentDao.kt            # Room DAO (queries & persistence)
│   │   └── ContentEntity.kt         # SQLite database entity
│   ├── remote/
│   │   ├── RemoteContentSource.kt   # Remote boundary contract
│   │   └── FakeRemoteContentSource.kt# Realistic campus announcements & hashes
│   └── repository/
│       └── ContentRepositoryImpl.kt # Offline-first sync manager
├── nearby/
│   ├── NearbyTransferManager.kt     # Transport interface
│   ├── DemoTransferManager.kt       # Simulated transport for demos
│   └── BluetoothTransferManager.kt  # Real Android RFCOMM Bluetooth transport
├── permissions/
│   └── NearbyPermissionManager.kt   # Android 12+ & legacy runtime permissions
└── ui/
    ├── theme/                       # Color palette, Typography, and M3 Theme
    ├── components/                  # Reusable TopBar, SearchBar, Chips, Cards, Badges
    └── screens/
        ├── home/                    # Campus feed & category filter screen
        ├── contentdetail/           # Announcement reading view & hash verification
        ├── nearbyshare/             # Peer discovery, transfer progress & mode toggle
        ├── library/                 # Offline-saved & peer-received library
        └── settings/                # Hardware status, offline toggle & diagnostics
```

---

## 🚀 Building & Running

### Prerequisites

- Android SDK installed (`platforms/android-36.1` or compatible)
- JDK 21+ configured on the host machine
- Gradle 9.4+ (included via Gradle Wrapper)

### Command Line Build

To build the debug APK:

**Windows (PowerShell / CMD):**
```bat
.\gradlew.bat assembleDebug
```

**macOS / Linux:**
```bash
./gradlew assembleDebug
```

The compiled APK will be generated at:
```text
app/build/outputs/apk/debug/app-debug.apk
```

### Running Unit Tests

```bat
.\gradlew.bat test
```

### Installing via ADB

With an emulator running or a physical device connected via USB debugging:
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## 🎬 Step-by-Step Demo Guide

1. **Online Refresh (Device A):**
   - Launch RelayNet and view the **Feed** tab.
   - Tap the **Refresh** button in the top bar to fetch the latest announcements. Data is immediately stored in Room SQLite.
2. **Offline Mode Simulation:**
   - Go to **Settings** and toggle **Simulate Device Online** to OFF.
   - Return to the **Feed**: Note the amber offline indicator. All announcements remain fully searchable and readable from local storage.
3. **Save for Offline Reading:**
   - Tap any card to open its detail view. Tap **Save Offline**.
   - Navigate to the **Library** tab: The item is verified and stored with the "Saved Offline" badge.
4. **Nearby Share (Peer-to-Peer Transfer):**
   - Open the **Nearby** tab.
   - Tap **Scan** to search for nearby nodes.
   - In **Demo Mode**, select items and tap **Send** to view real-time chunked transfer progress (0% → 100%).
   - Tap **Simulate Receive** to simulate Device B receiving a batch of announcements from another student device over the mesh.
5. **View Received Items Offline:**
   - Navigate to the **Library** tab: Newly received items display with the **Via: Peer** tag, verifying offline store-and-forward dissemination without internet access.

---

## 📄 License

This prototype was developed for the NMIET project. Distributed for academic and demonstration purposes.
