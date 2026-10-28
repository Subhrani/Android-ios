# TasteIndia

An Android recipe app built using Kotlin and Jetpack Compose.

## Features

- Browse Indian recipes using TheMealDB
- Search recipes by name
- Sort recipes A–Z or Z–A
- Filter by category and ingredient
- View recipe details, ingredients, and cooking instructions
- Loading, empty, and error states with retry
- Clearly labelled sample data for offline review

## Technology

Kotlin, Jetpack Compose, Material 3, Navigation Compose,
ViewModel, StateFlow, Coroutines, Retrofit, Moshi, and JUnit.

## Setup

1. Clone this repository.
2. Open the project in Android Studio.
3. Wait for Gradle synchronization.
4. Connect an Android phone with USB debugging enabled,
   or start an Android emulator.
5. Select the app configuration and click Run.

Minimum supported Android version: Android 7.0 (API 24).

## Build

From the project root in Windows PowerShell:

    .\gradlew.bat assembleDebug

## Run unit tests

    .\gradlew.bat testDebugUnitTest

Tests cover meal response parsing, filter intersection,
and recipe detail normalization.

## API

The app uses TheMealDB public V1 API with test key 1:

https://www.themealdb.com/api/json/v1/1/

Category and ingredient results are intersected with the
Indian meal list using meal IDs.

## Offline demonstration

When the live service is unavailable or returns no recipes,
tap "Try sample data".

Synthetic fixtures support offline review and are labelled
"Sample data — offline review".

## Current limitations

Live data requires a working internet connection and depends
on TheMealDB availability. Sample data demonstrates app behaviour
and is not a downloaded cache of live recipes.

## Attribution

Live recipe content: TheMealDB
https://www.themealdb.com/

## AI assistance

ChatGPT assisted with implementation guidance, code drafts,
debugging, and documentation. Development included incremental
builds, manual checks, and unit tests.

