# PRD: Cross-Platform DensityLoader for `TemplateSession`

## Purpose

Add a loading layer that fetches remote density data and returns a ready-to-use `TemplateSession`. Called once at app start. Always returns a valid session — falls back to bundled density data on any failure.

Targets: Android, iOS, JS.

---

## Architecture

```
┌──────────────── Common Code ─────────────────┐
│                                               │
│  DensityLoader(bridge)                        │
│    suspend fun initialiseConversionSession(   │
│        url, authToken                         │
│    ): TemplateSession                         │
│                                               │
│  DensityLoaderBridge (interface)              │
│    suspend fun loadDensityData(url, authToken) │
│        : DensityLoadResult                    │
│                                               │
│  DensityLoadResult                            │
│    Success(content: String)                   │
│    Failure                                    │
│                                               │
└───────────────────────────────────────────────┘
        │                │                │
┌───────┴──────┐ ┌───────┴──────┐ ┌───────┴──────┐
│  androidMain │ │    iosMain   │ │    jsMain    │
│              │ │              │ │              │
│ HttpURLConn  │ │ NSURLSession │ │  fetch()    │
│ Okio cache   │ │ Okio cache   │ │  no cache   │
└──────────────┘ └──────────────┘ └──────────────┘
```

`DensityLoader` lives in common code and is cache-agnostic. It calls the bridge, gets `Success` or `Failure`, and produces a `TemplateSession`. All HTTP, caching, freshness, and conditional-request logic lives inside the platform bridge.

### Dependencies

The library currently depends on `kotlinx-serialization-json:1.10.0`. This feature adds two dependencies:

| Dependency | Version | Purpose | CVE status |
|---|---|---|---|
| `org.jetbrains.kotlinx:kotlinx-coroutines-core` | **1.10.2** | `Dispatchers.IO` on Android, `suspendCancellableCoroutine` on iOS/JS. Standard KMP dependency — consuming apps already have this. | ✅ No known CVEs |
| `com.squareup.okio:okio` | **3.17.0** | Multiplatform file I/O for Android/iOS cache. Single `FileSystem` API across both platforms. Android consumers typically already have this via OkHttp. | ✅ No known CVEs |

Platform bridges continue to use platform-native HTTP:

- **Android:** `HttpURLConnection`
- **iOS:** `NSURLSession`
- **JS:** `fetch`, no cache, no Okio

### `libs.versions.toml` additions

```toml
[versions]
# ...existing entries...
kotlinx-coroutines = "1.10.2"
okio = "3.17.0"

[libraries]
# ...existing entries...
kotlinx-coroutines-core = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-core", version.ref = "kotlinx-coroutines" }
okio = { module = "com.squareup.okio:okio", version.ref = "okio" }
```

### `build.gradle.kts` commonMain dependency additions

```kotlin
val commonMain by getting {
    dependencies {
        implementation(libs.kotlinx.serialization.json)  // existing
        implementation(libs.kotlinx.coroutines.core)      // new
        implementation(libs.okio)                          // new
    }
}
```

---

## Common API

### Result model

```kotlin
package com.gu.recipe.loader

sealed interface DensityLoadResult {
    data class Success(val content: String) : DensityLoadResult
    data object Failure : DensityLoadResult
}
```

Common code never inspects timestamps. Bridges manage timestamps internally.

### Bridge interface

```kotlin
package com.gu.recipe.loader

interface DensityLoaderBridge {
    suspend fun loadDensityData(url: String, authToken: String): DensityLoadResult
}
```

### Loader

```kotlin
package com.gu.recipe.loader

import com.gu.recipe.TemplateSession
import com.gu.recipe.newTemplateSession
import com.gu.recipe.noCustomaryTemplateSession

class DensityLoader(private val bridge: DensityLoaderBridge) {
    /**
     * Fetches remote density data and returns a ready-to-use TemplateSession.
     * Always returns a usable session — never throws.
     * On failure, falls back to bundled internal density data.
     */
    suspend fun initialiseConversionSession(
        url: String,
        authToken: String
    ): TemplateSession {
        return when (val result = bridge.loadDensityData(url, authToken)) {
            is DensityLoadResult.Success -> {
                newTemplateSession(result.content).getOrElse {
                    logError("DensityLoader", "Remote data failed validation: ${it.message}")
                    fallbackSession()
                }
            }
            is DensityLoadResult.Failure -> fallbackSession()
        }
    }

    private fun fallbackSession(): TemplateSession {
        return newTemplateSession(null).getOrElse {
            logError("DensityLoader", "Internal data also failed: ${it.message}")
            noCustomaryTemplateSession()
        }
    }
}
```

- Returns `TemplateSession` directly, not `Result<TemplateSession>`.
- Fallback chain: remote data → bundled internal data → empty density table (scaling works, unit conversion disabled).
- Stateless — can be discarded after the call.

### Logging (`expect`/`actual`)

```kotlin
// commonMain
package com.gu.recipe.loader
internal expect fun logError(tag: String, message: String)

// androidMain — android.util.Log.e(tag, message)
// iosMain     — NSLog("[$tag] $message")
// jsMain      — console.error("[$tag] $message")
```

Used by `DensityLoader` and available to bridge implementations (same module, `internal` visibility).

### Freshness constant

```kotlin
// commonMain
package com.gu.recipe.loader
internal const val CACHE_FRESHNESS_MS: Long = 15 * 60 * 1000L
```

Defined once in common code. Used by platform bridge implementations for cache freshness checks.

---

## Bridge Behavior

### Required request headers

- `Authorization: Bearer <authToken>`
- `Accept: application/json`
- `If-Modified-Since: <HTTP-date>` — only on Android/iOS when cached data exists and a refresh is attempted. JS never sends this (no cache).

### Internal flow (Android/iOS)

1. Read cache. If fresh (< `CACHE_FRESHNESS_MS`) → return `Success(cached.content)`. Done.
2. Otherwise make GET request. Include `If-Modified-Since` if cache exists.
3. Handle response per table below.

### Internal flow (JS)

1. Make GET request (no cache, no `If-Modified-Since`).
2. Handle response per table below.

### Response handling

| Scenario | Action |
|---|---|
| Cache is fresh (Android/iOS only) | Return `Success(cached.content)`. No request. |
| HTTP 200 + body + `Last-Modified` | Write cache (Android/iOS). Return `Success(body)`. |
| HTTP 200 missing body or `Last-Modified` | Log. Return cached `Success` if available, else `Failure`. |
| HTTP 304 | Return cached `Success` if available, else `Failure`. |
| Any other HTTP status | Log. Return cached `Success` if available, else `Failure`. |
| Offline / timeout / transport error | Log. Return cached `Success` if available, else `Failure`. |

### Non-throwing rule

`loadDensityData(...)` must never throw. All exceptions must be caught and mapped to cached `Success` or `Failure`.

---

## Cache (Android/iOS only)

### Format

Single file. Use the file's modification time for freshness checks. Store the `Last-Modified` header value alongside content for `If-Modified-Since` requests:

```json
{
  "lastModified": "Sun, 16 Mar 2026 12:34:56 GMT",
  "content": "{...raw density json...}"
}
```

Use `kotlinx.serialization` to read/write this structure. Use Okio `FileSystem.SYSTEM` for file I/O.

### Location

| Platform | Path |
|---|---|
| Android | `<cacheDir>/recipe_data/density_cache.json` |
| iOS | `<Caches>/recipe_data/density_cache.json` |

Folder name must be exactly `recipe_data`.

### Resilience

- Malformed or unreadable cache → treat as missing.
- Write failure → log, do not crash.

---

## Platform Specifics

### Android

```kotlin
class AndroidDensityLoaderBridge(private val cacheDir: File) : DensityLoaderBridge
```

| Concern | Implementation |
|---|---|
| HTTP | `HttpURLConnection` on `Dispatchers.IO` |
| Cache | Okio `FileSystem.SYSTEM` — read/write `<cacheDir>/recipe_data/density_cache.json` |
| Freshness | Okio `FileSystem.SYSTEM.metadata(path).lastModifiedAtMillis` vs `System.currentTimeMillis()` |
| HTTP-date | `java.text.SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US)` with GMT timezone |

### iOS

```kotlin
class IosDensityLoaderBridge(private val cachesDirectory: String) : DensityLoaderBridge
```

| Concern | Implementation |
|---|---|
| HTTP | `NSURLSession.dataTaskWithRequest` wrapped in `suspendCancellableCoroutine` |
| Cache | Okio `FileSystem.SYSTEM` — read/write `<Caches>/recipe_data/density_cache.json` |
| Freshness | Okio `FileSystem.SYSTEM.metadata(path).lastModifiedAtMillis` vs `NSDate().timeIntervalSince1970.toLong() * 1000` |
| HTTP-date | `NSDateFormatter` with `"EEE, dd MMM yyyy HH:mm:ss 'GMT'"`, `en_US_POSIX` locale, GMT timezone |

> **Note:** No `iosMain` source set exists today. Create it in `build.gradle.kts` (covers `iosArm64`, `iosX64`, `iosSimulatorArm64`).

### JS

```kotlin
class JsDensityLoaderBridge : DensityLoaderBridge
```

| Concern | Implementation |
|---|---|
| HTTP | `window.fetch` or `globalThis.fetch` wrapped in `suspendCancellableCoroutine` |
| Cache | None |

No cache. No `If-Modified-Since`. On any failure → `Failure` → loader falls back to bundled data.

---

## Non-Goals

- Generic file manager or multi-file cache
- Database-backed cache
- Retry / background sync / progress tracking

---

## Acceptance Criteria

1. `initialiseConversionSession(url, authToken)` returns a usable `TemplateSession` on Android, iOS, and JS.
2. On `200` + body + `Last-Modified` → session uses remote data, cache is written (Android/iOS).
3. Fresh cache (< 15 min) → no network request (Android/iOS).
4. Stale cache → request includes `If-Modified-Since` (Android/iOS).
5. On `304` → cached content is used.
6. On network failure with cache → cached content is used.
7. On network failure without cache → bundled internal density data is used.
8. Malformed cache → treated as missing, no crash.
9. `loadDensityData(...)` never throws.
10. Cache folder is exactly `recipe_data`.

---

## Deliverables

1. **commonMain:** `DensityLoadResult`, `DensityLoaderBridge`, `DensityLoader`, `logError` expect, `CACHE_FRESHNESS_MS` — package `com.gu.recipe.loader`
2. **androidMain:** `AndroidDensityLoaderBridge` + `logError` actual
3. **iosMain:** `IosDensityLoaderBridge` + `logError` actual
4. **jsMain:** `JsDensityLoaderBridge` + `logError` actual
5. **commonTest:** Unit tests for `DensityLoader` with a mock bridge
6. **build.gradle.kts:** Add `iosMain` source set wiring + new dependencies
7. **libs.versions.toml:** Add `kotlinx-coroutines-core:1.10.2` and `okio:3.17.0`

---
