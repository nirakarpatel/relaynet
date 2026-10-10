# RelayNet 📚📡

> **Lightweight, Multilingual, AI-Powered Offline Learning for Classes 6–10**  
> Initial Pilot: **Class 9 Science, Odia + English (Photosynthesis & Plant Nutrition)**  
> Curriculum Scope: **Odisha State Board (BSE), CBSE, and ICSE (CISCE)**

[![Android](https://img.shields.io/badge/Platform-Android_10+-3DDC84?logo=android&logoColor=white)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin_2.2-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/UI-Jetpack_Compose_Material_3-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Room](https://img.shields.io/badge/Storage-Room_SQLite_Database-FFA000?logo=sqlite&logoColor=white)](https://developer.android.com/training/data-storage/room)
[![Bluetooth](https://img.shields.io/badge/P2P_Mesh-Bluetooth_RFCOMM-0082FC?logo=bluetooth&logoColor=white)](https://developer.android.com/develop/connectivity/bluetooth)

---

## 📖 Product Vision & The RelayNet Promise

RelayNet helps students in Classes 6–10 understand school subjects in their preferred language (Odia + English), practise weak topics with concept-mapped diagnostic quizzes, track mastery progress, and continue learning seamlessly when internet access is unavailable.

### The RelayNet Offline Learning Journey

```
  [ Online Source ]
         │
         ▼ (Fetch & Cache Pack)
 ┌───────────────┐                  Bluetooth RFCOMM                   ┌───────────────┐
 │   Device A    │  ════════════════════════════════════════════════►  │   Device B    │
 │ (Student / Hub│            User-Approved P2P Transfer               │  (Learner B)  │
 └───────────────┘                                                     └───────────────┘
   • Caches Learning Pack in Room                                        • Receives pack via peer
   • Bilingual Lessons (Odia + Eng)                                      • Verifies SHA-256 checksum
   • Diagnostic Quizzes & AI Tutor                                       • Learns & takes quizzes offline
   • Shares with nearby students                                         • Syncs mastery when online
```

1. **Grade & Board Selection:** Student configures Class 9 → Odisha State Board (BSE) → Science → Biology.
2. **Bilingual Learning:** Reads the verified pilot lesson on **Photosynthesis (ଆଲୋକ ଶ୍ଳେଷଣ)** in Odia, English, or side-by-side Bilingual mode, with chemical formulas and key terms glossary.
3. **Multilingual AI Tutor:** Student asks questions in Odia or English (typed or speech-to-text transcript). When online, answers are fetched; when offline, answers are instantly served from local packs with explicit `[Offline Grounded]` badges.
4. **Diagnostic Quizzes & Misconception Diagnosis:** 5 concept-mapped questions identify specific gaps (e.g., *Photolysis of water vs CO₂ source of O₂*).
5. **Topic Mastery & Targeted Practice:** Tracks mastery (0–100%) and recommends targeted practice loops.
6. **Nearby Peer-to-Peer Transfer:** Students share compressed, versioned Learning Packs directly over Bluetooth without cellular or internet data.

---

## ✨ Implemented Core Features

| Feature | Description | Status |
|---|---|---|
| **Curriculum Catalogue** | Grade & board hierarchy (`Board → Class → Subject → Chapter → Topic`) supporting Odisha Board, CBSE, and ICSE. | ✅ Verified |
| **Bilingual Lesson Reader** | Pilot lesson for **Class 9 Science: Photosynthesis (ଆଲୋକ ଶ୍ଳେଷଣ)** with toggleable Odia, English, or Bilingual views, chemical formulas, and key terms. | ✅ Verified |
| **Diagnostic Quiz Engine** | 5 step-by-step questions with instant scoring, scientific explanations, and misconception hints for incorrect choices. | ✅ Verified |
| **Topic Mastery Engine** | 0–100% progress score classifying learners into *Needs Foundation*, *Developing*, *Good Progress*, and *Strong Mastery* tiers. | ✅ Verified |
| **Weak-Area Practice Loop** | Automatically diagnoses concept tags missed during quizzes and recommends targeted next activities. | ✅ Verified |
| **Multilingual AI Tutor** | Conversational tutor answering in Odia and English, featuring a simulated speech-to-text transcript workflow and offline grounded reasoning. | ✅ Verified |
| **P2P Learning Pack Sharing** | Dual-mode transport: **Demo Simulation** (for single-device presentations) and **Bluetooth Classic RFCOMM** (for physical phones) with SHA-256 integrity verification. | ✅ Verified |
| **Teacher Pipeline Preview** | Explains the lecture transcription, draft quiz generation, and approved learning pack publishing workflow. | ✅ Verified |
| **Offline Simulation Switch** | Toggle in Settings to simulate offline / airplane mode and verify full local database functionality. | ✅ Verified |

---

## 📂 Project Architecture

```text
app/src/main/java/com/nmiet/app/
├── MainActivity.kt                      # Single activity root, DI wiring & Compose setup
├── navigation/
│   ├── Routes.kt                        # Route constants (Curriculum, Tutor, Mesh, Mastery, Settings)
│   └── AppNavHost.kt                    # M3 BottomNavigationBar and navigation host graph
├── domain/
│   ├── model/
│   │   ├── LearnerProfile.kt            # Board, Class (6-10), Language preferences
│   │   ├── CurriculumNode.kt            # Multi-board syllabus nodes
│   │   ├── LearningPack.kt              # Bilingual lesson data, key terms, pack metadata
│   │   ├── QuizModels.kt                # Questions, options, concept tags, mastery tiers
│   │   ├── TutorModels.kt               # AI chat messages with Odia/English text
│   │   ├── PeerDevice.kt                # Discovered peer representation
│   │   └── TransferState.kt             # Sealed state machine for pack transfers
│   └── repository/
│       └── ContentRepository.kt         # Reactive Flow repository contract
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt               # Room database (relaynet_learning_database)
│   │   ├── LearningDao.kt               # Room DAO for packs, attempts, mastery, tutor
│   │   └── LearningEntities.kt          # SQLite entities with JSON mappers
│   ├── remote/
│   │   └── PilotCurriculumCatalog.kt    # Verified Class 9 Science Odia/English pilot pack
│   └── repository/
│       └── ContentRepositoryImpl.kt     # Offline-first repository implementation
├── nearby/
│   ├── NearbyTransferManager.kt         # Transport interface
│   ├── DemoTransferManager.kt           # Simulated transport with progress & SHA-256
│   └── BluetoothTransferManager.kt      # Real Android RFCOMM Bluetooth transport
├── permissions/
│   └── NearbyPermissionManager.kt       # Android 12+ & legacy Bluetooth permission helper
└── ui/
    ├── theme/                           # Color palette, Typography, Material 3 theme
    ├── components/                      # TopBar, StatusBadge, EmptyView, SearchBar, Chips
    └── screens/
        ├── curriculum/                  # Curriculum browser & downloaded packs list
        ├── lesson/                      # Bilingual lesson reader with Odia/English toggle
        ├── quiz/                        # Diagnostic quiz runner & misconception feedback
        ├── tutor/                       # Multilingual AI tutor with speech input transcript
        ├── mastery/                     # Mastery dashboard & weak-area practice loop
        ├── nearbyshare/                 # P2P Bluetooth pack sharing with progress bar
        └── settings/                    # Profile settings, offline simulation, teacher preview
```

---

## 🛠 Tech Stack

- **Platform:** Android (Min SDK 26, Target SDK 36)
- **Language:** Kotlin 2.2
- **UI:** Jetpack Compose with Material 3
- **Local Database:** Room 2.6 with SQLite
- **Asynchronous Flow:** Kotlin Coroutines & `StateFlow`
- **Navigation:** Navigation Compose
- **Transport:** Android Bluetooth RFCOMM + BLE Discovery
- **Integrity:** SHA-256 Checksums
- **Testing:** JUnit 4 unit test suite

---

## 🚀 Building & Testing

### Command Line Build

**Windows (PowerShell):**
```bat
.\gradlew.bat test assembleDebug
```

**macOS / Linux:**
```bash
./gradlew test assembleDebug
```

The compiled APK will be output at:
```text
app/build/outputs/apk/debug/app-debug.apk (19.7 MB)
```

---

## 🎬 5-Minute Demo Walkthrough for Evaluators

1. **Curriculum & Bilingual Lesson:**
   - Launch RelayNet. View the **Lessons** tab: Observe the Learner Profile (Class 9 • Odisha State Board • Bilingual).
   - Tap **Read Lesson** on the *Photosynthesis (ଆଲୋକ ଶ୍ଳେଷଣ)* pack.
   - Switch between **Bilingual**, **ଓଡ଼ିଆ (Odia)**, and **English** to view the chemical equation and glossary terms.
2. **Multilingual AI Tutor:**
   - Tap the **AI Tutor** tab.
   - Tap one of the quick chips or tap the **Mic** icon to simulate speech input in Odia.
   - Observe the grounded bilingual response with the `[Offline Grounded]` badge.
3. **Diagnostic Quiz & Misconception Analysis:**
   - Tap **Take Quiz**. Answer question 1 (Source of Oxygen).
   - Intentionally select CO₂ to observe the misconception feedback: *"Common misconception: Many students think O₂ comes from CO₂, but experiments prove it originates from H₂O photolysis."*
   - Complete all 5 questions to view the score and mastery tier classification.
4. **Mastery Dashboard & Weak-Area Loop:**
   - Tap the **Mastery** tab: View the 0–100% progress score and the recommended next targeted practice action based on quiz mistakes.
5. **Offline P2P Mesh Pack Transfer:**
   - Tap the **P2P Share** tab.
   - Tap **Scan** to discover nearby peer devices.
   - Tap **Send Pack** or **Simulate Receive** to view chunked progress (0%→100%) and SHA-256 verification.
   - Go to **Settings** → Toggle **Simulate Device Online** to OFF → Open lessons and quizzes completely offline!

---

## 📄 License

Developed for the RelayNet Project. Distributed for educational and evaluation purposes.
