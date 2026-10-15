# BLE Radar

A modern Android application for scanning, monitoring, and tracking nearby Bluetooth Low Energy (BLE) devices in real-time with distance estimation and telemetry diagnostics.

## Features
- **Active BLE Discovery**: Discovers nearby Bluetooth Low Energy peripherals with live RSSI signal telemetry.
- **Distance Estimation**: Categorizes signal strength into calibrated proximity zones (< 1m, 1–3m, 3–10m, 10–20m, > 20m, Disconnected).
- **Offline Persistence**: Automatically deduplicates and persists discovered devices in a local Room SQLite database keyed by MAC address.
- **Real-Time Search & Filtering**: Multi-field search (All, Device Name, MAC Address, Signal Strength RSSI) with modal bottom sheet criteria selection.
- **Clean Architecture & MVVM**: Unidirectional data flow (UDF) powered by Kotlin Coroutines, StateFlow, and Jetpack Compose.
- **Comprehensive Testing**: Unit tests for domain models, Room DAO, and ViewModel, plus Roborazzi screenshot regression tests.

## Tech Stack
- **Kotlin**: 2.1.10
- **Jetpack Compose**: Declarative UI with Material 3 styling
- **Architecture**: MVVM + Clean Architecture (Data Source -> Repository -> ViewModel -> Compose Screen)
- **Room Database**: 2.8.5 with SQLite deduplication
- **Testing**: JUnit 4, Robolectric 4.16, Coroutines Test, Roborazzi 1.76.0
