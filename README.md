# PulseFit

A modern fitness tracking application built with Jetpack Compose.

## Features
- **Authentication**: Login/SignUp screens with social sign-in placeholders and a debug bypass.
- **Health Metrics**: Track Resting Heart Rate and Heart Rate Variability (HRV) in a clean, scrollable interface.
- **Workouts**: Support for prebuilt routines and custom workout creation.
- **Profile Management**: Customize username, height, and weight.
- **Modern UI**: Built using Material 3 with support for Dark Mode and customizable units.

## Tech Stack
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Navigation**: Compose Navigation
- **Architecture**: Material 3

## Getting Started
1. Clone the repository.
2. Open in Android Studio.
3. **Authentication Setup (Mandatory for Social Login)**:
    - **Google**: Create a project in Google Cloud Console, get an OAuth Web Client ID, and update `MainActivity.kt`.
    - **Facebook**: Create an app in Meta for Developers, get the App ID and Client Token, and update `strings.xml`.
    - **SHA-1**: Add your local SHA-1 fingerprint to both developer consoles.
4. Build and Run on an emulator or physical device.
