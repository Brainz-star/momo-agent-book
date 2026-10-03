# MoMo Agent Book

MoMo Agent Book is an offline Android application designed to replace the paper exercise book used by a Mobile Money agent to keep transaction records. It records cash-in and cash-out transactions, calculates daily summaries, and provides fast transaction retrieval for customer-service and dispute-support situations.

## Core features

- Record cash-in / cash-out transactions with amount, commission, required customer MoMo number, optional customer name and note.
- Automatic transaction reference, date and time.
- Validation for transaction type, positive amount, commission and Ghana mobile numbers.
- Dashboard showing today's cash-in, cash-out, transaction value, commission and transaction count.
- Search history by transaction ID, MoMo number or customer name, with transaction type, date and amount filters.
- Matching-result summaries for filtered searches.
- Transaction details screen for reviewing the complete record before editing or deleting.
- Edit and delete existing transactions with confirmation before deletion.
- Daily summary for previous days, today and the next day.
- Shareable daily report as a professional PDF or plain-text report through Android's sharing interface.
- Offline only: no MTN API, no money movement and no external account connection.

## Tech stack

Kotlin, Jetpack Compose (Material 3), Navigation Compose, Room (SQLite), Kotlin coroutines/Flow.
minSdk 24, targetSdk 34.

## Build

```
./gradlew assembleDebug
./gradlew testDebugUnitTest
```

The debug APK is generated at `app/build/outputs/apk/debug/app-debug.apk`.

## Project layout

```
app/src/main/java/com/momobook/agent
├── data           Room entity, DAO, database, repository
├── ui             ViewModel and shared Compose components
│   └── screens    Dashboard, transaction form, details, history, daily summary
└── util           Date/currency formatting, phone normalization, validation, reports
```

## Reporting
- Daily summaries can be exported as a professional PDF report using Android's built-in `PdfDocument`.
- PDF reports can be shared through Android's standard share sheet (WhatsApp, email, Drive, Bluetooth, etc., depending on installed apps).
- Customer MoMo numbers are masked in shared PDFs for privacy; the full number remains available inside the app's transaction details.
- Text sharing remains available for quick reporting.

## Phase 3 professional finishing

Version 1.5 combines the completed dashboard, transaction history, transaction details, and daily reporting UI polish into one build-ready source package. Existing transaction recording, search, edit/delete, local storage, and PDF reporting functionality is preserved.


## Version 1.8 — Navigation flash and PDF responsiveness

- Added a root Material `Surface` around the navigation host so destination changes do not expose a blank/window-background frame during navigation.
- Moved PDF document generation to `Dispatchers.IO` so report generation does not block the Compose UI thread.
- Added a small generating state to prevent duplicate PDF requests while a report is being created.
- No database architecture or transaction features were changed.
