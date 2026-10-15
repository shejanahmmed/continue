<div align="center">

# continue

### A high-contrast, distraction-free productivity and activity management engine for Android.

Clean Architecture &bull; Jetpack Compose &bull; 100% Offline-First &bull; GPL-3.0

---

</div>

## Overview

**continue** is a native Android application engineered for focused execution, daily structure, and granular activity tracking. Built from the ground up using **Modern Android Development (MAD)** standards, it pairs strict **Clean Architecture** and reactive **Unidirectional Data Flow (UDF)** with a hardware-centric, high-contrast utilitarian design language.

Every pixel, gesture, and transition is designed to eliminate visual noise. The app is 100% offline-first, private by default, and operates entirely on local hardware storage without analytics trackers, external cloud telemetry, or third-party server dependencies.

---

## Key Capabilities

### 1. Unified Activity Workspace
- **Multimodal Entry Tracking:** Consolidates calendar events, high-priority reminders, and actionable task workflows into a cohesive daily agenda.
- **Hierarchical Workspaces:** Every activity expands into an isolated workspace featuring embedded task checklists, rich context notes, categorized web links, and local file attachments.
- **Relational Integrity:** Room Database entities enforce `CASCADE` constraints to ensure all associated sub-items (notes, links, tasks) maintain atomic persistence during life-cycle updates.

### 2. Category System & Data Migration
- **Custom Visual Identities:** Assign curated high-contrast color palettes and vector icons to categories.
- **Bi-directional Synchronization:** Edits made to category configurations immediately cascade to all historical and pending reminders across the database.
- **Safe Fallback Lifecycle:** Deleting a custom category transparently moves assigned tasks back to the default category (`My reminders`) without data loss or orphaned foreign keys.

### 3. Precision Temporal Engine & Alerts
- **Hardware-Accurate Scheduling:** Leverages `AlarmManager` with exact scheduling and `BroadcastReceiver` pipelines for precise notifications.
- **Multi-Alert Intervals:** Configure granular early warning alerts (e.g., 5 min, 15 min, 1 hour, 1 day, or custom duration units) per activity.
- **Flexible Recurrence:** Native engine supports recurrence intervals (Daily, Weekly, Monthly, Custom) with intelligent boundary handling.

### 4. Interactive Diary & Calendar Matrix
- **Chronological Ledger:** Track completed goals, reflections, and timestamped progress across days and weeks.
- **Calendar Matrix:** High-performance monthly and weekly grid navigators providing visual density markers for days with active schedules.

---

## Architectural Blueprint

The codebase enforces strict separation of concerns across three architectural tiers:

```
┌────────────────────────────────────────────────────────┐
│                   Presentation Layer                   │
│   Jetpack Compose UI • ViewModels • UDF (StateFlow)    │
└───────────────────────────┬────────────────────────────┘
                            │ (Observes States & Dispatches Events)
┌───────────────────────────▼────────────────────────────┐
│                      Domain Layer                      │
│   Entities • Value Objects • Repository Contracts      │
│   (Pure Kotlin — Zero Android Framework Couplings)     │
└───────────────────────────▲────────────────────────────┘
                            │ (Implements Contracts)
┌───────────────────────────┴────────────────────────────┐
│                       Data Layer                       │
│   Room Database • DAOs • SharedPreferences • Alarms    │
└────────────────────────────────────────────────────────┘
```

### Design System & Visual Philosophy
- **Monochrome Foundation:** Strict high-contrast palette built upon deep blacks (`#121212`, `#1E1E1E`), clean whites, and subtle hairline border dividers (`1dp`).
- **Accent Restraint:** Single-point vibrant color highlights reserved exclusively for active states, primary actions, and critical notifications.
- **Utilitarian Components:** Geometric cards, sharp pill boundaries, tactile haptic feedback, and dot-matrix visual accents for numerical displays and prominent headers.
- **Responsive Geometry:** Dynamic height calculation and explicit shape clipping preventing visual boundary clipping or rectangular touch ripple artifacts.

---

## Tech Stack & Dependencies

| Area | Technologies |
| :--- | :--- |
| **Language** | [Kotlin 2.0.21](https://kotlinlang.org/) (100% Kotlin DSL) |
| **User Interface** | [Jetpack Compose](https://developer.android.com/jetpack/compose) + [Material Design 3](https://m3.material.io/) |
| **Navigation** | [AndroidX Navigation Compose](https://developer.android.com/guide/navigation) |
| **Local Persistence** | [Room Database 2.6.1](https://developer.android.com/training/data-storage/room) via KSP |
| **Concurrency** | [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) + [Kotlin Flow (StateFlow / SharedFlow)](https://kotlinlang.org/docs/flow.html) |
| **Scheduling** | Android `AlarmManager` + BroadcastReceivers for deterministic exact notifications |
| **Min / Target SDK** | Min SDK: API 24 (Android 7.0 "Nougat") • Target / Compile SDK: API 37 |
| **R8 / ProGuard** | Minification and resource shrinking enabled with mapping preservation for release profiling |
| **Testing** | JUnit 4, Turbine, Coroutines Test (`StandardTestDispatcher`), Mock Repositories |

---

## Repository Structure

```
continue/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/shejan/acontinue/
│   │   │   │   ├── core/                  # Notification schedulers, receivers, shared utilities
│   │   │   │   │   ├── designsystem/      # Utilitarian design tokens, navigation bars, buttons
│   │   │   │   │   └── notification/      # Alarm orchestration and system broadcasts
│   │   │   │   ├── data/                  # Room databases, DAOs, entities, repository implementations
│   │   │   │   │   ├── local/             # SQLite Room schema, DAOs, and type converters
│   │   │   │   │   └── repository/        # Repository implementations mapping domain models
│   │   │   │   ├── domain/                # Enterprise domain contracts and business entities
│   │   │   │   │   ├── model/             # Immutable data structures (ActivityItem, ReminderCategory)
│   │   │   │   │   └── repository/        # Repository abstract interfaces
│   │   │   │   ├── presentation/          # Jetpack Compose UI screens, ViewModels, navigation graph
│   │   │   │   │   ├── calendar/          # Monthly and daily calendar matrix
│   │   │   │   │   ├── create/            # Unified entry creation flow and category management
│   │   │   │   │   ├── diary/             # Journal timeline and chronological logs
│   │   │   │   │   ├── home/              # Main feed, daily task lists, and summary metrics
│   │   │   │   │   ├── navigation/        # Compose Navigation host and destination definitions
│   │   │   │   │   ├── settings/          # System preferences, data export, and legal disclosures
│   │   │   │   │   └── workspace/         # Granular activity details, subtasks, notes, and links
│   │   │   │   └── ui/theme/              # High-contrast utilitarian color tokens and typography
│   │   │   └── res/                       # Vector drawables, mipmaps, strings, and system themes
│   │   └── test/                          # Unit tests verifying ViewModels, repositories, and state
│   ├── build.gradle.kts                   # Module-level Gradle configuration
│   └── proguard-rules.pro                 # Deobfuscation and release optimization rules
├── gradle/                                # Gradle wrapper binaries and version catalog
├── build.gradle.kts                       # Root build script
├── settings.gradle.kts                    # Project repositories and plugin definitions
└── README.md
```

---

## Building and Verification

### Prerequisites
- **JDK 17** or **JDK 21** configured (`JAVA_HOME` pointed to a compliant runtime or Android Studio bundled JBR).
- **Android SDK Platform 37** installed via Android SDK Manager.
- **Android Studio Ladybug (2024.2.1)** or newer recommended.

### 1. Clone the repository
```bash
git clone https://github.com/shejanahmmed/continue.git
cd continue
```

### 2. Run unit tests
To run the automated test suite across all ViewModels and repository fakes:
```powershell
# PowerShell
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
.\gradlew.bat test
```
```bash
# Bash
./gradlew test
```

### 3. Compile debug build
To compile and assemble the local debug APK:
```powershell
# PowerShell
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
.\gradlew.bat assembleDebug
```
The compiled APK will be generated at:
`app/build/outputs/apk/debug/app-debug.apk`

---

## Privacy & Security

- **Zero Cloud Storage:** All user data is stored strictly in on-device SQLite databases managed by Android Room.
- **No Telemetry:** No user tracking, crash reporting trackers, or usage analytics are packaged into the application.
- **Sandboxed File Handling:** Custom file and image attachments are copied directly into the app's internal private storage directory via ContentResolvers, preventing external media tampering.

---

## License

This project is licensed under the **GNU General Public License v3.0 (GPL-3.0)**.  
You are free to view, modify, and distribute this software according to the terms of the license. See the [LICENSE](LICENSE) file for the full legal text.

---

## Author & Maintainer

**Farjan Ahmmed**  
*Senior Software Engineer*

- **GitHub:** [@shejanahmmed](https://github.com/shejanahmmed)
- **LinkedIn:** [Farjan Ahmmed](https://www.linkedin.com/in/farjan-ahmmed/)
- **Email:** [farjan.swe@gmail.com](mailto:farjan.swe@gmail.com)
- **Instagram:** [@iamshejan](https://www.instagram.com/iamshejan/)
- **Facebook:** [beingshejan](https://www.facebook.com/beingshejan/)
