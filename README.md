# TasteIndia

Native Android app for discovering Indian recipes with Kotlin, Jetpack Compose and TheMealDB V1. Includes explicit offline sample mode and persistent favourites.

## Features

- Indian-area base list with thumbnails, accessible save/remove controls and stable meal IDs.
- Local case-insensitive name search; category, main ingredient and favourites-only filters; A-Z / Z-A sorting; active-filter feedback, result count and Clear all.
- Details with hero image, category, area, country/alternate name when available, ordered ingredient/measure pairs, paragraphs, tags, source/video links and image credits.
- Shared ID-based details destination from Recipes and Favourites.
- Persistent favourites through DataStore. Saved full details open without a network connection.
- Loading, empty, HTTP failure, connection failure, timeout, retry, missing-image and failed-image states.
- Synthetic fixtures for explicit offline review; live and sample favourites have separate identities.

## Toolchain and libraries

This project preserves the toolchain of the original working project. Use the Android Studio bundled JDK and install SDK 37 through SDK Manager.

| Component | Version |
| --- | --- |
| Android Gradle Plugin | 9.4.1 |
| Gradle wrapper | 9.6.0 |
| Kotlin / Compose compiler plugin | 2.2.10 |
| Java source/target compatibility | 11 |
| compileSdk / targetSdk | 37 / 37 |
| minSdk | 24 (Android 7.0) |
| Compose BOM | 2026.02.01 |
| Navigation Compose | 2.9.5 |
| DataStore Preferences | 1.1.7 |
| Retrofit / Moshi converter | 3.0.0 |
| Moshi Kotlin | 1.15.2 |
| OkHttp | 4.12.0 |
| Coil Compose | 2.7.0 |
| Coroutine test library | 1.10.2 |

Some lifecycle versions are resolved upward by transitive dependencies. Inspect the resolved graph with `./gradlew :app:dependencies` if needed; the version catalog records direct declarations.

## Setup

1. Clone the private repository with an account that has access.
2. Open the project root in Android Studio, let Gradle sync and install any requested SDK packages.
3. Configure your local SDK path through Android Studio. `local.properties` is machine-specific and must not be committed.
4. Connect a phone with USB debugging enabled or select a running emulator.
5. Select the `app` configuration and click Run.

Windows PowerShell, from the project root:

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat testDebugUnitTest
```

On macOS/Linux use `./gradlew` instead. The debug APK is generated at `app/build/outputs/apk/debug/app-debug.apk`.

## Architecture and state ownership

- `MainActivity`: activity setup and shared Material scaffold.
- `AppNavigation`: central Recipes, Favourites and Details routes. Only meal ID and sample/live flag are route arguments.
- `RecipesRoute`, `RecipesScreen`, `RecipeRow`, `RecipeFiltersUi`: lifecycle-aware flow collection and stateless rendering/events. Search and sorting run locally through `selectMeals`.
- `RecipesViewModel`: owns list loading, query, sort and applied filters. `SavedStateHandle` preserves query/sort/applied filter names and ID sets through supported Android saved-state recreation. Sample mode is restored too.
- Compose `rememberSaveable` owns draft filter inputs, expanded filters and favourites-only; `rememberLazyListState` owns list position within the navigation entry.
- `MealRepository`: checks HTTP status/body, caches successful responses and bounds detail work. The shared repository is reused across destinations and can be replaced with a fake API in tests.
- `MealDetailsViewModel`: selected detail load, cancellation and recoverable errors. Parsing is normalized before presentation.
- `DataStoreFavouritesStore`, `FavouritesViewModel`: persistent records, shared immutable state and serialized write events. Storage is injected through `FavouritesStore`; tests replace it with an in-memory implementation.
- `AssetSampleSource`: reads bundled fixture JSON on the IO dispatcher. No Activity is retained by a ViewModel.

## Routes

- `recipes` → recipe list
- `favourites` → saved recipes
- `details/{mealId}/{isSample}` → shared details destination

Back pops the destination instead of constructing another list. The existing list ViewModel and navigation saveable state retain the context. Saved state is not a promise to restore every screen after force-stop or clearing app data. The last live list is not persisted to disk.

## API and Indian boundary

Only TheMealDB public V1 with educational key `1` is used:

| Purpose | Path under `https://www.themealdb.com/api/json/v1/1/` |
| --- | --- |
| Authoritative Indian base | `filter.php?a=Indian` |
| Category IDs | `filter.php?c={category}` |
| Ingredient IDs | `filter.php?i={ingredient}` |
| Categories / ingredients | `list.php?c=list`, `list.php?i=list` |
| Selected details | `lookup.php?i={mealId}` |

Retrofit query parameters encode values. The visible result starts with the Indian base list and intersects every active ID set. A null set means inactive; an empty set means no matches. Search and favourites-only narrow the result locally, then a stable name/ID comparator sorts it. Search never launches remote requests. Filter work is cancelled and guarded by a revision check, so older responses cannot replace the latest selection.

The app attempts a real Indian-list GET on live startup or Retry. A provider failure or `meals: null` is shown honestly; the app does not substitute non-Indian live recipes to fill the list. The reviewer should capture the actual live response on their test device.

Provider documentation and terms:
- https://www.themealdb.com/api.php
- https://www.themealdb.com/terms.php

Test key 1 is for development/educational use; public app-store release has separate provider requirements.

## Cache and request policy

- Successful list/choice/filter/detail responses have a five-minute in-memory TTL.
- A shared access-order cache is limited to 100 response entries; process exit clears it.
- Detail requests use one mutex: at most one live detail lookup runs at a time. Waiting same-ID requests recheck the cache and reuse a successful response. Failures are not cached and can be retried.
- There is no eager detail enrichment, batch scraping or network request from a row's render function. Only a selected details route loads a record.
- Live list Retry bypasses the base-list cache, including a cached empty result.
- Connection timeout: 10 seconds; read/write: 15 seconds; whole call: 25 seconds. Image loading shares this HTTP client.
- Coil manages image memory/disk caching. Images are best-effort cached and may be evicted; saved recipe text does not require an image download.
- Favourites are persistent user-selected snapshots, not TTL cache entries. Saving on the list stores a summary immediately without requiring a network call. Opening that saved recipe successfully fills its offline details automatically. Saving directly on details stores the full record.
- A summary that has never been opened may still require a connection for its first details load. The Favourites screen states whether details have been saved.

## Details mapping and intentional omissions

`strIngredient1..20` and `strMeasure1..20` are trimmed and paired in order. Only pairs with both values blank are omitted; a useful partial pair is retained. Instruction line breaks are preserved. Blank tags/metadata are omitted.

Image source/credit, Creative Commons confirmation and modification date are retained when present. Additional scalar response fields are converted to readable labels; nested structures are intentionally omitted instead of showing raw JSON. `idMeal` is identity, not primary content. Empty fields and unsafe/malformed source/video URLs are intentionally not rendered as actions. Only HTTP(S) URLs with a valid host and no embedded credentials can open an external app.

## Offline fixtures

Select **Try sample data** from the live empty/error state. Sample mode is explicitly labelled and has a **Try live recipes** action.

- `app/src/main/assets/indian_meals.json`: two synthetic list records.
- `app/src/main/assets/sample_meal_details.json`: matching invented details.
- `app/src/test/resources/fixtures/indian_meals.json`: parsing fixture.

These fixtures are demonstrations, not claims about TheMealDB data. The missing thumbnail and example.com image deliberately exercise missing/failure placeholders. They are not recipe photographs and do not require downloaded image assets.

## Tests and validation

Run `./gradlew testDebugUnitTest` (or the Windows command above). The suite does not require TheMealDB to be online.

Focused coverage includes:
- JSON decoding, null lists, ingredient pairing and line breaks.
- Indian-boundary intersection and combined local search/favourites/sorting.
- Latest-filter response safety and restoration of query/sort/filter state.
- Detail cache reuse, concurrency bound and retry after HTTP failure.
- Favourite identity, preservation of offline details, JSON round-trip and shared ViewModel restoration/removal.
- Safe web URLs and additional detail metadata.

Preparation verification: 17 platform-independent tests were compiled with Kotlin 2.2.10 and passed in a standalone JVM harness. This is not an Android Gradle build. Android-dependent ViewModel tests, Gradle dependency resolution, Compose compilation and device checks must be run in the original Android project after applying the update. Record those results before submission.

## Manual acceptance check

1. Start online and observe live loading/response; use explicit sample mode if the provider is unavailable.
2. Search rapidly; combine category and ingredient; reverse sort; check active labels, count, no matches and Clear all.
3. Scroll, open details and go Back; confirm query, filters, favourites-only, sort and list position.
4. Save/remove from both list and details; confirm Favourites updates.
5. Open a saved recipe once to cache full details; close the app, disconnect, reopen Favourites and read its details.
6. Verify missing/broken images leave readable rows and a stable hero placeholder.
7. Check large font size, TalkBack labels, light/dark mode, rotation and error/retry.

## Recording and submission

Prepare a 2-4 minute recording showing live loading, search, combined filters, details, favourites, error/retry and Back/relaunch restoration. Identify sample mode when used. Keep the repository private, grant the reviewer access and preserve incremental Git history.

Time spent: **add your actual total and dates before submission**. Do not estimate from generated output. This update is a review candidate until the full Android build, tests and acceptance checks pass.

## Attribution and AI assistance

Live recipe content and live image URLs: [TheMealDB](https://www.themealdb.com/). Synthetic fixtures were created for this assignment. Third-party libraries retain their own licences: AndroidX/Compose, Retrofit, OkHttp, Moshi, Coil, Kotlin Coroutines and JUnit.

ChatGPT assisted with implementation drafts, architecture suggestions, debugging, test ideas, documentation and this bulk update. The candidate must review and adapt the code, run the Android checks, understand the tradeoffs and be able to explain and modify the submission. Do not claim unreviewed generated work as independently completed.

Author: Gopika Arumugam

## Bundled sample images

The two known sample recipes now show bundled, AI-generated illustrative food images offline in the list, details and saved favourites. Each is visibly labelled "AI-generated sample image". See IMAGE-CREDITS.md for attribution and generation prompts. The fixture JSON still contains missing/invalid URLs for tests; these two explicit sample IDs use local resources at runtime instead of those URLs. Live recipes continue to use TheMealDB's strMealThumb, with missing/error placeholders when needed. This supersedes the earlier description of placeholder images in runtime sample mode. No API data or saved favourites need to be changed.
