# TillFlow

An offline-first point-of-sale app for small cafés and shops, built with Kotlin and Jetpack Compose.
Staff can browse and sell even when the Wi-Fi drops; data syncs when the connection returns.

> Status: step 4 of 5. Staff can browse, build a cart, take payment and review every sale with no
> connection. Sales upload in the background whenever the device is online, with automatic retry.

## Tech stack

Kotlin · Jetpack Compose · Material 3 · MVVM with unidirectional data flow · Coroutines & Flow ·
Hilt · Room · Paging 3 · WorkManager · Navigation Compose · JUnit + kotlinx-coroutines-test

## Architecture

```
ui/          Compose screens + ViewModels. Screens are stateless; ViewModels expose one StateFlow<UiState>.
domain/      Plain Kotlin: Product, Cart and Order models, repository interfaces. No Android imports.
data/        Room entities and DAOs, repository implementations, CartStore (the shared basket),
             and sync/: the WorkManager worker, scheduler and upload logic.
di/          Hilt modules that wire data/ into domain/.
```

The UI never talks to Room directly. It observes the repository, and the repository reads from the
local database, which is the single source of truth. When network sync arrives, it will write into
Room and the UI will update automatically.

## Design decisions

- **Money as pence (`Long`), not `Double`.** Floating point can't represent 0.10 exactly, so
  totals drift. Formatting to "£12.50" happens only at the edge of the UI.
- **Stateless screens.** `CatalogueScreen` takes state and returns events, so it has a Compose
  preview and can be UI-tested without a ViewModel. `CatalogueRoute` connects it to Hilt.
- **`SharingStarted.WhileSubscribed(5_000)`.** The database query stays alive through a rotation
  but stops when the app is in the background.
- **Checkout is one Room transaction.** The order, its lines and the stock changes are saved
  together or not at all, so a crash mid-sale can't leave a half-recorded order.
- **Order lines copy the name and price at the time of sale.** Changing a price later never
  rewrites past receipts.
- **The cart is immutable.** Every change returns a new `Cart`, which is safe to share through a
  `StateFlow` and easy to test. A double tap on "Charge" can't create two orders.
- **One-off events live in state.** A saved order or a failure is a field in `CartUiState`; the
  screen shows it, then tells the ViewModel it has been handled.
- **Order history is paged from Room.** Room generates the `PagingSource`, so the list loads 30
  orders at a time, and a new sale appears at the top without a manual refresh. `cachedIn`
  keeps loaded pages through a rotation.
- **Navigation arguments are read from `SavedStateHandle`.** The order detail screen gets its
  order id from the route, so it survives process death.
- **Sales never wait for the network.** Checkout writes to Room and returns; WorkManager uploads
  pending orders in the background, only when online. A one-time sync runs after each sale, and a
  15-minute periodic sync is the safety net.
- **Retries are safe.** An order is marked synced only after the server confirms it, and uploads
  are keyed by order id, so a retry after a lost response doesn't create a duplicate. Failed
  uploads retry with exponential backoff (10s, 20s, 40s, …), and one failure doesn't block the rest.
- **Sync logic is separate from the Worker.** `OrderSyncer` is plain Kotlin with unit tests;
  `OrderSyncWorker` is a thin wrapper that maps its result to success or retry.
- **Repository interfaces in `domain/`.** ViewModel tests use in-memory fakes, so they run on
  the JVM in milliseconds without an emulator.

## About the backend

There is no real server yet. `DemoOrderApi` stands in for one: each upload takes about a second
and fails 20% of the time, so you can watch retries happen. To use a real API, write a
Retrofit-backed `OrderApi` and change one line in `SyncModule`.

## Run it

1. Open the folder in Android Studio (Ladybug or newer) and let Gradle sync.
2. Run the `app` configuration on an emulator or a device with Android 8.0+.
3. Run the unit tests: `./gradlew test`

## Roadmap

- [x] **1. Catalogue:** products in Room, search and category filters, stock levels
- [x] **2. Cart and checkout:** Compose Navigation, add/remove items, totals, record orders in a transaction
- [x] **3. Order history:** Paging 3 over completed orders, plus an order detail screen
- [x] **4. Offline sync:** WorkManager pushes unsynced orders when online, with retry and backoff
- [ ] **5. Barcode scanning:** CameraX + ML Kit to add items to the cart
- [ ] Multi-module split (`:core:data`, `:feature:catalogue`, …) and CI with GitHub Actions
