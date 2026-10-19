# PaperCut — Smart Document & Expense Scanner

[![Android](https://img.shields.io/badge/Platform-Android-green.svg?logo=android)](https://www.android.com/)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-purple.svg?logo=kotlin)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20%2F%20Material%203-blue.svg?logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![ML Kit](https://img.shields.io/badge/AI%2FML-Google%20ML%20Kit%20OCR-orange.svg?logo=google)](https://developers.google.com/ml-kit)
[![Room Database](https://img.shields.io/badge/Storage-Room%20SQLite-yellow.svg)](https://developer.android.com/training/data-storage/room)

**PaperCut** is a modern, privacy-focused Android mobile application designed for instant receipt scanning, optical character recognition (OCR), expense tracking, budget target analytics, and automated PDF/CSV report generation. Built 100% on-device using Jetpack Compose, Material 3, Google ML Kit, and Room Database.

---

## Quick Download

You can test the pre-built, ready-to-install Android APK directly from this repository:

* **[Download PaperCut.apk (Direct Link)](./release_apk/PaperCut.apk)**  
*(Also located at `app/build/outputs/apk/debug/PaperCut.apk`)*

---

## Features

### 1. On-Device Receipt & Document Scanning (OCR)
* **ML Kit Document Scanner API:** Built-in edge detection, perspective correction, contrast enhancement, and auto-cropping.
* **On-Device Text Recognition:** Instant local OCR that automatically parses:
  * Merchant / Vendor name
  * Total transaction amount
  * Transaction date
  * Keyword-based category auto-assignment (e.g., *Food & Dining, Groceries, Travel, Utilities, Office*)

### 2. Searchable World Currency Picker
* Search across **40+ global currencies** by ISO code (e.g. `USD`, `EUR`, `INR`, `GBP`, `JPY`, `CAD`, `AUD`, `AED`, `KES`, `ZAR`), currency name, or country.
* Symbols side-by-side (`USD ($)`, `EUR (€)`, `INR (₹)`).
* Pinned popular currencies section for fast selection.

### 3. Custom Categories & Custom Color Badges
* Create custom categories with tailored names.
* Assign custom badge colors from a color palette swatch to organize expenses visually.

### 4. Paginated Expense Management & Analytics
* **Optimized Database Pagination:** Paginated Room SQL queries (`20 records per page`) ensuring smooth performance even with thousands of receipts.
* **Interactive Pagination Bar:** Page navigation (`Prev` / `Next` with total record counter).
* **Monthly Budget Target & Spending Gauge:** Set a custom monthly spending target with a live percentage gauge bar that alerts you when approaching or exceeding your limit.

### 5. Supplier Contact Tagging & Sharing
* Tag supplier, merchant, or friend phone numbers directly to any receipt.
* **One-Tap Dialing:** Call suppliers directly from the receipt details screen.
* **Instant Receipt Sharing:** Share receipt summaries and receipt images via WhatsApp, Gmail, Messages, or PDF share sheet.

### 6. Local PDF & CSV Report Generation
* Export formatted PDF and CSV expense reports directly to `Documents/PaperCut/` on your device.
* **One-Tap Report Opening:** Instant notification with an `OPEN` button to view or send reports immediately.

### 7. Strict On-Device Privacy (Zero Cloud Dependencies)
* All OCR, image processing, database storage, and report generation occur strictly **on your device**.
* Zero external cloud servers, tracking, or memory leaks.

---

## Architecture & Tech Stack

PaperCut follows modern Android architecture guidelines adhering to Clean Architecture & MVVM principles:

```
com.example.new_app/
├── data/
│   ├── db/                 # Room Database, Category & Expense Entities, DAOs
│   ├── ocr/                # ML Kit Text Recognition & Regex Parser
│   ├── scanner/            # ML Kit Document Scanner API Launcher & Image Storage
│   ├── export/             # Native PDF Canvas & CSV Exporter
│   └── repository/         # Expense & Category Repository Layer
├── ui/
│   ├── theme/              # Material 3 Color Schemes & Typography
│   ├── viewmodel/          # ExpenseViewModel & Reactive StateFlow
│   ├── components/         # M3 Expense Cards & Custom Badges
│   └── screens/            # Jetpack Compose Screens & Search Modals
└── MainActivity.kt          # Single Activity hosting Compose UI
```

* **Language:** Kotlin
* **UI Framework:** Jetpack Compose + Material 3 Design System
* **Async & State:** Kotlin Coroutines + StateFlow + Flow
* **Database:** Room Database (SQLite) with KSP
* **Vision & AI:** Google ML Kit Text Recognition & Document Scanner
* **Image Loading:** Coil Compose
* **Exporting:** Native Android `PdfDocument` & FileProvider

---

## Prerequisites & Building from Source

### Requirements
* Android Studio Ladybug (2024.2.1) or newer
* JDK 17+
* Android SDK 35+
* Gradle 8.5+

### Steps to Build
1. Clone this repository:
   ```bash
   git clone https://github.com/sp2772/PaperCut---Smart-Document-Expense-Scanner.git
   cd PaperCut
   ```
2. Open the project in Android Studio.
3. Build the debug APK via Gradle CLI:
   ```bash
   ./gradlew assembleDebug
   ```
4. The generated APK will be available at:
   `release_apk/PaperCut.apk` or `app/build/outputs/apk/debug/PaperCut.apk`.
