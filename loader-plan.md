# Implementation Plan: DensityLoader

Reference: [loader.md](./loader.md)

---

## 1. Build Config + Common Code

**Status:** ✅ Complete

### 1.1 `libs.versions.toml` — add new dependency entries

**Status:** ✅ Complete

Add to `[versions]`:
```toml
kotlinx-coroutines = "1.10.2"
okio = "3.17.0"
```

Add to `[libraries]`:
```toml
kotlinx-coroutines-core = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-core", version.ref = "kotlinx-coroutines" }
okio = { module = "com.squareup.okio:okio", version.ref = "okio" }
```

### 1.2 `library/build.gradle.kts` — add dependencies + `iosMain` source set

**Status:** ✅ Complete

Add to `commonMain` dependencies:
```kotlin
val commonMain by getting {
    dependencies {
        implementation(libs.kotlinx.serialization.json)  // existing
        implementation(libs.kotlinx.coroutines.core)      // new
        implementation(libs.okio)                          // new
    }
}
```

Add `commonTest` dependency for coroutines test support:
```kotlin
val commonTest by getting {
    dependencies {
        implementation(libs.kotlin.test)                   // existing
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
    }
}
```

Create `iosMain` source set (no source set for iOS targets exists today):
```kotlin
sourceSets {
    // ...existing commonMain, commonTest...

    val iosMain by creating {
        dependsOn(commonMain)
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach {
        it.compilations["main"].defaultSourceSet.dependsOn(iosMain)
    }
}
```

> **Note:** The iOS targets are already declared at the top of the `kotlin {}` block. The `iosMain` source set just needs to wire up `dependsOn`. The target declarations (with XCFramework config) stay untouched.

### 1.3 `DensityLoadResult` — result model

**Status:** ✅ Complete (updated in 1.10)

File: `library/src/commonMain/kotlin/com/gu/recipe/loader/DensityLoadResult.kt`

```kotlin
package com.gu.recipe.loader

sealed interface DensityLoadResult {
    data class Success(val content: String) : DensityLoadResult
    data class Failure(val reason: String? = null) : DensityLoadResult
}
```

### 1.4 `DensityLoaderBridge` — bridge interface

**Status:** ✅ Complete

File: `library/src/commonMain/kotlin/com/gu/recipe/loader/DensityLoaderBridge.kt`

```kotlin
package com.gu.recipe.loader

interface DensityLoaderBridge {
    suspend fun loadDensityData(url: String, authToken: String): DensityLoadResult
}
```

### 1.5 `CACHE_FRESHNESS_MS` — freshness constant

**Status:** ✅ Complete

File: `library/src/commonMain/kotlin/com/gu/recipe/loader/CacheFreshness.kt`

```kotlin
package com.gu.recipe.loader

internal const val CACHE_FRESHNESS_MS: Long = 15 * 60 * 1000L
```

### 1.6 `logError` — expect declaration

**Status:** ❌ Removed (see 1.10)

~~Deleted in favour of `onError` callback on `DensityLoader`. See section 1.10.~~

### 1.7 `DensityLoader` — common loader

**Status:** ✅ Complete (updated in 1.10)

File: `library/src/commonMain/kotlin/com/gu/recipe/loader/DensityLoader.kt`

```kotlin
package com.gu.recipe.loader

import com.gu.recipe.TemplateSession
import com.gu.recipe.newTemplateSession
import com.gu.recipe.noCustomaryTemplateSession

class DensityLoader(
    private val bridge: DensityLoaderBridge,
    private val onError: ((String) -> Unit)? = null
) {
    suspend fun initialiseConversionSession(
        url: String,
        authToken: String
    ): TemplateSession {
        return when (val result = bridge.loadDensityData(url, authToken)) {
            is DensityLoadResult.Success -> {
                newTemplateSession(result.content).getOrElse {
                    onError?.invoke("Remote data failed validation: ${it.message}")
                    fallbackSession()
                }
            }
            is DensityLoadResult.Failure -> {
                result.reason?.let { onError?.invoke(it) }
                fallbackSession()
            }
        }
    }

    private fun fallbackSession(): TemplateSession {
        return newTemplateSession(null).getOrElse {
            onError?.invoke("Internal data also failed: ${it.message}")
            noCustomaryTemplateSession()
        }
    }
}
```

### 1.8 `DensityLoader` unit tests

**Status:** ✅ Complete (updated in 1.10)

File: `library/src/commonTest/kotlin/com/gu/recipe/loader/DensityLoaderTest.kt`

Test cases using a mock `DensityLoaderBridge`:

| # | Test case | Expected |
|---|---|---|
| 1 | Bridge returns `Success` with valid density JSON | Session created from remote data |
| 2 | Bridge returns `Success` with invalid/malformed JSON | Falls back to bundled internal data |
| 3 | Bridge returns `Failure()` (no reason) | Falls back to bundled internal data |
| 4 | Bridge returns `Failure("Network timeout")` (with reason) | Falls back to bundled internal data |
| 5 | Bridge returns `Success` with empty string | Falls back to bundled internal data |
| 6 | `onError` called when bridge returns invalid JSON | Callback receives validation message |
| 7 | `onError` called when bridge returns `Failure` with reason | Callback receives reason string |
| 8 | `onError` not called when bridge returns `Failure` without reason | No callback invocation |
| 9 | `onError` not called on successful data load | No callback invocation |

### 1.9 Build verification

**Status:** ✅ Complete

- [x] `./gradlew :library:compileKotlinMetadata` passes (common code compiles)
- [x] `./gradlew :library:testReleaseUnitTest` passes (all green)
- [x] `./gradlew :library:compileKotlinIosSimulatorArm64` passes (iOS compiles)
- [x] `./gradlew :library:compileKotlinJs` passes (JS compiles)

> **Note:** `allTests` has a pre-existing JS test failure in `ScaleRecipeJsContractTest` (unrelated — `createTemplateSession()` called without required parameter).

### 1.10 Error handling refactor: `logError` → `onError` callback

**Status:** ✅ Complete

Replaced the `expect`/`actual` `logError` pattern with a client-provided `onError` callback on `DensityLoader`. This lets consumers wire errors to their own logging infrastructure (e.g. Firebase/Crashlytics) without the library depending on platform logging APIs.

#### Changes

| Action | Files |
|---|---|
| **Deleted** | `commonMain/.../loader/Log.kt` (expect) |
| **Deleted** | `androidMain/.../loader/Log.android.kt` (actual — `android.util.Log`) |
| **Deleted** | `iosMain/.../loader/Log.ios.kt` (actual — `NSLog`) |
| **Deleted** | `jsMain/.../loader/Log.js.kt` (actual — `console.error`) |
| **Updated** | `DensityLoadResult.kt` — `Failure` changed from `data object` to `data class Failure(val reason: String? = null)` |
| **Updated** | `DensityLoader.kt` — added `onError: ((String) -> Unit)? = null` constructor param |
| **Updated** | `DensityLoaderTest.kt` — updated `Failure` refs, added 5 new `onError` callback tests |
| **Updated** | `build.gradle.kts` — removed `testOptions.unitTests.isReturnDefaultValues = true` (no longer needed) |

#### Consumer usage

```kotlin
val loader = DensityLoader(
    bridge = bridge,
    onError = { message -> Firebase.crashlytics.log(message) }
)
```

Bridge implementations silently return `Failure(reason)` — no logging needed inside them.

---

## 2. Android Implementation

**Status:** 🔲 Not started

### 2.1 `logError` — actual declaration

**Status:** ❌ Removed (see 1.10 — replaced by `onError` callback on `DensityLoader`)

### 2.2 `DensityCacheEntry` — cache serialization model

**Status:** 🔲 Not started

File: `library/src/androidMain/kotlin/com/gu/recipe/loader/AndroidDensityLoaderBridge.kt` (private class within bridge file)

```kotlin
@Serializable
private data class DensityCacheEntry(
    val lastModified: String,
    val content: String
)
```

### 2.3 `AndroidDensityLoaderBridge` — bridge implementation

**Status:** 🔲 Not started

File: `library/src/androidMain/kotlin/com/gu/recipe/loader/AndroidDensityLoaderBridge.kt`

Constructor: `AndroidDensityLoaderBridge(private val cacheDir: java.io.File)`

#### Managing Android `Context`

The bridge needs `cacheDir: File` — **not** a `Context` directly. This is deliberate:

- The library is a pure Kotlin module; it should not hold a `Context` reference.
- The consuming Android app passes `context.cacheDir` when constructing the bridge.
- This avoids `Context` leaks and keeps the library's API clean.

**Consumer usage:**
```kotlin
// In Application.onCreate() or DI module
val bridge = AndroidDensityLoaderBridge(cacheDir = applicationContext.cacheDir)
val loader = DensityLoader(bridge)

// In a coroutine scope
val session = loader.initialiseConversionSession(url, token)
```

#### Internal flow

```
loadDensityData(url, authToken)
│
├─ Read cache file: <cacheDir>/recipe_data/density_cache.json
│  └─ via Okio FileSystem.SYSTEM.read(path)
│  └─ deserialize with kotlinx.serialization → DensityCacheEntry?
│  └─ on any failure → treat as null (no cache)
│
├─ If cache exists and file mod time < 15 min old:
│  └─ return Success(cached.content) — no network call
│
├─ withContext(Dispatchers.IO):
│  ├─ Open HttpURLConnection to url
│  ├─ Set headers: Authorization, Accept
│  ├─ If cache exists → set If-Modified-Since: cached.lastModified
│  ├─ Connect + read response
│  │
│  ├─ 200 + body + Last-Modified header:
│  │  ├─ Write cache via Okio FileSystem.SYSTEM.write(path)
│  │  └─ return Success(body)
│  │
│  ├─ 304:
│  │  └─ return Success(cached.content) if cache exists, else Failure
│  │
│  └─ Any error / other status:
│     └─ return Success(cached.content) if cache exists, else Failure(reason)
│
└─ Catch all exceptions:
   └─ return Success(cached.content) if cache exists, else Failure(reason)
```

#### Key details

| Concern | Detail |
|---|---|
| Cache path | `cacheDir / "recipe_data" / "density_cache.json"` as Okio `Path` |
| Directory creation | `FileSystem.SYSTEM.createDirectories(cachePath.parent!!)` before write |
| Freshness check | `FileSystem.SYSTEM.metadata(cachePath).lastModifiedAtMillis?.let { System.currentTimeMillis() - it < CACHE_FRESHNESS_MS }` |
| HTTP-date format | `SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US)` with `timeZone = TimeZone.getTimeZone("GMT")` |
| Thread safety | All I/O inside `withContext(Dispatchers.IO)` |

### 2.4 Build verification

**Status:** 🔲 Not started

- [ ] `./gradlew :library:compileReleaseKotlinAndroid` passes
- [ ] `./gradlew :library:testReleaseUnitTest` passes

---

## 3. iOS Implementation

**Status:** 🔲 Not started

### 3.1 `logError` — actual declaration

**Status:** ❌ Removed (see 1.10 — replaced by `onError` callback on `DensityLoader`)

### 3.2 `IosDensityLoaderBridge` — bridge implementation

**Status:** 🔲 Not started

File: `library/src/iosMain/kotlin/com/gu/recipe/loader/IosDensityLoaderBridge.kt`

Constructor: `IosDensityLoaderBridge(private val cachesDirectory: String)`

The consumer passes the Caches directory path. On iOS, obtained via:
```swift
let cachesDir = FileManager.default.urls(for: .cachesDirectory, in: .userDomainMask).first!.path
let bridge = IosDensityLoaderBridge(cachesDirectory: cachesDir)
let loader = DensityLoader(bridge: bridge)
```

#### Internal flow

Same logic as Android, with these platform-specific differences:

| Concern | Implementation |
|---|---|
| Cache path | `cachesDirectory / "recipe_data" / "density_cache.json"` as Okio `Path` |
| File I/O | Okio `FileSystem.SYSTEM` (same API as Android) |
| Freshness | Okio `FileSystem.SYSTEM.metadata(path).lastModifiedAtMillis` vs `NSDate().timeIntervalSince1970.toLong() * 1000` |
| HTTP | `NSURLSession.sharedSession.dataTaskWithRequest` wrapped in `suspendCancellableCoroutine` |
| HTTP-date format | `NSDateFormatter()` with `dateFormat = "EEE, dd MMM yyyy HH:mm:ss 'GMT'"`, `locale = NSLocale("en_US_POSIX")`, `timeZone = NSTimeZone.timeZoneForSecondsFromGMT(0)` |
| Thread | `NSURLSession` callback-based → `suspendCancellableCoroutine` bridges to coroutine; file I/O is synchronous (small file, acceptable) |

#### `NSURLSession` suspend wrapper

```kotlin
private suspend fun httpGet(
    url: String,
    headers: Map<String, String>
): HttpResult = suspendCancellableCoroutine { cont ->
    val nsUrl = NSURL.URLWithString(url) ?: run {
        cont.resume(HttpResult.Error("Invalid URL: $url"))
        return@suspendCancellableCoroutine
    }
    val request = NSMutableURLRequest.requestWithURL(nsUrl).apply {
        setHTTPMethod("GET")
        headers.forEach { (k, v) -> setValue(v, forHTTPHeaderField = k) }
    }
    val task = NSURLSession.sharedSession.dataTaskWithRequest(request) { data, response, error ->
        // Map to HttpResult (status, body, lastModified) or Error
        // Resume continuation
    }
    cont.invokeOnCancellation { task.cancel() }
    task.resume()
}
```

### 3.3 Build verification

**Status:** 🔲 Not started

- [ ] `./gradlew :library:compileKotlinIosArm64` passes
- [ ] `./gradlew :library:compileKotlinIosSimulatorArm64` passes
- [ ] `./gradlew :library:iosSimulatorArm64Test` passes (if test infra available)

---

## 4. JS Implementation

**Status:** 🔲 Not started

### 4.1 `logError` — actual declaration

**Status:** ❌ Removed (see 1.10 — replaced by `onError` callback on `DensityLoader`)

### 4.2 `JsDensityLoaderBridge` — bridge implementation

**Status:** 🔲 Not started

File: `library/src/jsMain/kotlin/com/gu/recipe/loader/JsDensityLoaderBridge.kt`

Constructor: `JsDensityLoaderBridge()` — no arguments (no cache, no cacheDir).

#### Internal flow

JS is the simplest bridge — no cache, no freshness, no `If-Modified-Since`:

```
loadDensityData(url, authToken)
│
├─ fetch(url, headers: Authorization + Accept)
│
├─ 200 + body:
│  └─ return Success(body)
│
└─ Any error / non-200:
   └─ return Failure
```

#### `fetch` suspend wrapper

JS `fetch` returns a `Promise`. Wrap it:

```kotlin
private suspend fun httpGet(
    url: String,
    headers: Map<String, String>
): HttpResult = suspendCancellableCoroutine { cont ->
    val init = js("({})")
    init.method = "GET"
    val jsHeaders = js("({})")
    headers.forEach { (k, v) -> jsHeaders[k] = v }
    init.headers = jsHeaders

    window.fetch(url, init)
        .then { response -> response.text().then { body -> Pair(response, body) } }
        .then { (response, body) ->
            // Map to HttpResult based on response.status
            cont.resume(...)
        }
        .catch { error ->
            cont.resume(HttpResult.Error(error.message ?: "fetch failed"))
        }
}
```

> **Alternative:** Use `kotlinx.coroutines` `await()` extension on `Promise` for cleaner code:
> ```kotlin
> val response = window.fetch(url, init).await()
> val body = response.text().await()
> ```

### 4.3 JS export consideration

**Status:** 🔲 Not started

The existing JS API exposes `createTemplateSession(rawDensityData)` as a `@JsExport` function in `ScaleRecipeJsContract.kt`. Consider whether to:

- **Option A:** Add a `@JsExport` suspend wrapper that JS consumers can call (returns a `Promise` automatically). This would require the JS consumer to change their init code.
- **Option B:** Leave the existing `createTemplateSession` unchanged. JS consumers who want the loader call it from Kotlin JS code, not from raw JS.

Recommendation: **Option B** — keep existing JS contract intact. The loader is primarily for native apps (Android/iOS) where app-start init is natural. JS consumers can adopt it later if needed.

### 4.4 Build verification

**Status:** 🔲 Not started

- [ ] `./gradlew :library:compileKotlinJs` passes
- [ ] `./gradlew :library:jsTest` passes
- [ ] Existing JS tests still pass (`ScaleRecipeJsContractTest`)

---

## Summary

| Section | Tasks | Status |
|---|---|---|
| 1. Build Config + Common Code | 9 sub-tasks | ✅ Complete |
| 2. Android Implementation | 4 sub-tasks | 🔲 Not started |
| 3. iOS Implementation | 3 sub-tasks | 🔲 Not started |
| 4. JS Implementation | 4 sub-tasks | 🔲 Not started |

### Suggested implementation order

1. **Build Config + Common Code** (1.1–1.9) — everything else depends on this
2. **Android** (2.1–2.4) — most familiar platform, good to validate the pattern
3. **iOS** (3.1–3.3) — same cache logic via Okio, different HTTP
4. **JS** (4.1–4.4) — simplest bridge, no cache

