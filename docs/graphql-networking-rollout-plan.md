# GraphQL + Networking Rollout Plan

This document breaks the work into small steps so GraphQL support can be added to the KMP library with the **least possible change required in consuming projects**.

## Goals

- Keep `:library` as the only module most consumer apps need to depend on.
- Keep Retrofit and Apollo implementation details out of the public API.
- Keep the networking layer structured so Retrofit can be replaced by Ktor later with minimal downstream impact.
- Add a `:core:networking` module first as the internal foundation.
- Add a fully working `:core:graphql` module second.
- Ensure schema download + Apollo code generation happen automatically during builds.
- Keep the design multiplatform-friendly.

---

## Phase 0 — Principles and guardrails

### Task 0.1 — Keep public API stable
- Continue exposing consumer-facing APIs from `:library`.
- Keep a clear distinction between consumer-facing API and internal module-to-module API.
- Do not expose Retrofit service interfaces in public APIs.
- Do not expose Apollo generated models in public APIs.
- Return stable library-owned models or repository interfaces from `:library`.
- It is acceptable for internal-only types to live in `:core:networking` or `:core:graphql` when consuming apps will not import them directly.

### Task 0.2 — Use internal module boundaries
- Put transport/infrastructure code in `:core:networking`.
- Put GraphQL implementation/codegen in `:core:graphql`.
- Keep HTTP client construction behind internal factories/adapters so the transport implementation can move from Retrofit/OkHttp to Ktor later.
- Keep dependency direction one-way:
  - `:library` -> `:core:graphql`
  - `:core:graphql` -> `:core:networking` (only if needed)
  - avoid `:core:*` -> `:library`

### Task 0.3 — Minimize downstream changes
- Consumers should keep depending on the existing published `library` artifact.
- Consumers should not need to know whether data comes from Retrofit or Apollo.
- Any new setup needed by consumers should be limited to a small config/factory API.

Implementation note:
- if a type is only used between modules inside this repository and is never imported by consuming apps, it does not need to be promoted into `:library`
- only types that are part of the true external SDK surface should be treated as long-term public API

---

## Phase 1 — Create the networking module foundation

### Task 1.1 — Register the module in Gradle settings
- Update `settings.gradle.kts`.
- Include:
  - `:core:networking`
  - `:core:graphql`
- Keep `:library` included as before.

### Task 1.2 — Expand the version catalog
- Update `gradle/libs.versions.toml` with versions and aliases needed for networking.
- Add versions for likely dependencies:
  - Retrofit
  - OkHttp
  - OkHttp logging interceptor
  - Kotlin datetime if needed later
  - Apollo plugin/runtime entries for next phase
- Keep versions centralized in the catalog.

### Task 1.3 — Configure `core/networking/build.gradle.kts`
- Make `:core:networking` a Kotlin Multiplatform module.
- Add Android library plugin support if Android-specific code will live there.
- Configure source sets:
  - `commonMain`
  - `commonTest`
  - `androidMain`
  - `androidUnitTest` if needed
- Put only shared contracts/config in `commonMain`.
- Put Retrofit/OkHttp dependencies in `androidMain` unless there is a real multiplatform use for shared pieces.
- Keep the common API transport-agnostic so a future Ktor implementation can slot in without changing `:library`.

### Task 1.4 — Add module namespace/build settings
- Set Android namespace for `:core:networking`.
- Align compile/min SDK and JVM target with existing project values.
- Keep formatting and plugin style consistent with the existing repo.

### Task 1.5 — Add initial networking abstractions
Create minimal shared classes/interfaces in `:core:networking` so later modules depend on contracts, not implementation details.

Suggested first set:
- `NetworkEnvironment` or `BaseUrlProvider`
- `RequestHeadersProvider` or `AuthTokenProvider`
- `NetworkException` / `NetworkError`
- `NetworkResult<T>` wrapper if useful
- `NetworkLogger` abstraction if logging needs to be pluggable
- `HttpEngineFactory` or similar transport abstraction if you want the eventual Ktor move to be mostly internal

### Task 1.6 — Add Android transport implementation shell
Create Android-side classes only as infrastructure, not public API.

Suggested classes:
- `OkHttpClientFactory`
- `RetrofitFactory`
- optional interceptors:
  - auth header interceptor
  - user agent/header interceptor
  - logging interceptor

Note:
- treat these as replaceable adapters, not as the core contract of the networking module
- avoid making repository code depend directly on Retrofit service interfaces

### Task 1.7 — Decide publication strategy for internal modules
Because `:library` will depend on internal modules, decide how they are published:
- either publish `:core:networking` and `:core:graphql` as transitive artifacts
- or embed/shade implementation where appropriate

Recommended approach:
- publish them normally as internal support artifacts
- keep public entrypoint in `:library`

### Task 1.8 — Verify networking module builds cleanly
- Run Gradle sync/build for `:core:networking`.
- Confirm no configuration or source set issues.
- Confirm `:library` still builds after adding the module.

---

## Phase 2 — Define the public API shape in `library`

### Task 2.1 — Decide repository surface before implementation
Before wiring GraphQL, define what consumers should call.

Recommended shape:
- one or more repository interfaces in `:library`
- a small config object for environment/auth setup
- one factory/builder to construct repositories/clients

### Task 2.2 — Introduce config entrypoint in `:library`
Add a stable public configuration API, for example:
- base API URL / environment
- auth token provider or header provider
- timeout/logging flags if required

Keep this config owned by `:library`, even if values are passed down internally.

### Task 2.3 — Define stable consumer-facing models
- Reuse existing models if they fit.
- Add new public models in `:library` only where needed.
- Avoid leaking Apollo generated response types into public method signatures.

### Task 2.4 — Define internal-to-public mapping boundary
- Decide where API models are transformed into library models.
- Recommended location: `:core:graphql` maps GraphQL responses into stable library/domain shapes or internal DTOs that `:library` can safely use.

---

## Phase 3 — Implement the GraphQL module fully

### Task 3.1 — Configure `core/graphql/build.gradle.kts`
- Make `:core:graphql` a Kotlin Multiplatform module.
- Apply Apollo Kotlin plugin.
- Configure Android/iOS/JS-compatible source sets as needed.
- Add Apollo runtime dependencies in `commonMain` if GraphQL is shared across platforms.
- Add dependency on `:core:networking` only for shared config/error abstractions if needed.

### Task 3.2 — Choose the GraphQL package structure
Recommended structure inside `:core:graphql`:
- `src/commonMain/graphql/` for `.graphql` operations/fragments
- `src/commonMain/kotlin/...` for client wrappers, mappers, and repository-facing APIs
- schema stored inside the module

### Task 3.3 — Add Apollo service configuration
- Configure one Apollo service in Gradle.
- Set package name for generated code.
- Enable codegen options appropriate for the project.
- Keep generated package names stable to reduce churn.

### Task 3.4 — Implement a thin GraphQL client wrapper
Create a small wrapper around Apollo so the rest of the project does not depend directly on Apollo APIs.

Suggested responsibilities:
- create the Apollo client
- execute queries/mutations
- map low-level errors into project-level errors
- inject auth headers and endpoint config

Design note:
- prefer passing Apollo an abstracted HTTP/config layer from `:core:networking` where practical so a future Ktor-based implementation can be introduced behind the same library-facing API

### Task 3.5 — Add query/mutation files
- Add `.graphql` files for the initial set of required operations.
- Keep operations focused and version-controlled.
- Organize by feature/domain if multiple APIs are added later.

### Task 3.6 — Add model mapping layer
- Map Apollo generated models into stable library/internal models.
- Handle nullability carefully.
- Keep all GraphQL-specific field-name quirks inside this module.

### Task 3.7 — Add tests for mapping and error handling
- Add unit tests for GraphQL response mapping.
- Add tests for partial/null response scenarios.
- Add tests for transport and GraphQL error conversion.

### Task 3.8 — Connect `:library` to `:core:graphql`
- Add dependency from `:library` to `:core:graphql`.
- Implement repository implementations in `:library` using GraphQL-facing abstractions.
- Keep repository interfaces and public entrypoints in `:library`.

---

## Phase 4 — Automate schema download and code generation

### Task 4.1 — Decide where schema credentials come from
Choose how schema download is authenticated/configured.

Recommended inputs:
- Gradle properties
- environment variables
- local developer overrides via `local.properties` or CI secrets

Avoid hardcoding secrets in the repo.

### Task 4.2 — Add a schema download task
Implement a Gradle task that:
- contacts the GraphQL server or introspection endpoint
- downloads the latest schema
- writes it to the `:core:graphql` module schema location
- declares proper task inputs/outputs for Gradle caching

### Task 4.3 — Wire schema download before Apollo codegen
- Make Apollo code generation depend on the schema refresh task.
- Ensure build/codegen order is:
  1. download schema
  2. generate Apollo models
  3. compile sources

### Task 4.4 — Add failure strategy
Recommended behavior:
- default: try to refresh schema, but allow fallback to last saved schema if remote call fails
- CI or strict mode: fail build when schema refresh fails

This keeps local development resilient while allowing CI enforcement.

### Task 4.5 — Decide whether schema file is committed
Recommended approach:
- commit the schema file for reproducibility and offline fallback
- do not commit generated Apollo classes unless there is a strong reason

### Task 4.6 — Add developer documentation for schema refresh
Document:
- required env vars/properties
- how to force-refresh schema
- how to run Apollo generation manually
- expected generated output location

---

## Phase 5 — Publishing and consumer impact

### Task 5.1 — Update publication configuration
Because `:library` will now depend on internal modules:
- ensure `:core:networking` and `:core:graphql` are also publishable artifacts
- ensure metadata and transitive dependencies resolve correctly for KMP consumers

### Task 5.2 — Keep `library` as the main consumer artifact
- Maintain `:library` as the documented dependency for downstream projects.
- Do not ask consuming apps to depend directly on `:core:*` modules unless there is a very specific need.

### Task 5.3 — Verify transitive resolution in sample consumers
Test at least conceptually for:
- Android consumer
- iOS framework/XCFramework consumer
- JS consumer if GraphQL support should work there

### Task 5.4 — Check API compatibility impact
- Review `library/api.txt` if public API changes are introduced.
- Keep breaking public API changes to a minimum.
- Prefer additive APIs for first rollout.

### Task 5.5 — Preserve migration freedom for the transport layer
- Review any newly added public APIs and remove references to Retrofit/OkHttp-specific concepts.
- Ensure config objects use generic names like `baseUrl`, `headers`, `authProvider`, and `timeout`, not transport-specific terminology.
- Ensure internal modules can be republished later with Ktor replacing Retrofit without forcing consumer API changes.

---

## Phase 6 — Future migration path from Retrofit to Ktor

This phase is not required for the initial rollout, but the initial implementation should make it easy.

### Task 6.1 — Audit Retrofit-specific assumptions
- Identify any classes in `:core:networking`, `:core:graphql`, or `:library` that directly depend on Retrofit types.
- Move those dependencies behind internal adapters or factories.
- Confirm no public API references Retrofit, OkHttp interceptors, or Retrofit response wrappers.

### Task 6.2 — Introduce Ktor-compatible transport abstractions
- Define or refine transport-neutral contracts for:
  - endpoint resolution
  - auth/header injection
  - request execution
  - error mapping
  - logging hooks
- Keep these contracts in shared code where possible.

### Task 6.3 — Add Ktor implementation in `:core:networking`
- Add Ktor client dependencies.
- Implement a Ktor-based client factory.
- Recreate existing auth/header/logging behavior in Ktor plugins/interceptors.
- Ensure platform support can expand more naturally than Retrofit if needed.

### Task 6.4 — Swap GraphQL/network integrations to the new transport path
- Update internal wiring so GraphQL and any REST clients use the Ktor-backed implementation.
- Keep repository and consumer-facing APIs unchanged.
- Verify request/response behavior matches the Retrofit-based baseline.

### Task 6.5 — Run side-by-side validation
- Compare Retrofit and Ktor implementations for:
  - headers
  - timeouts
  - error mapping
  - serialization behavior
  - performance-sensitive paths
- Fix mismatches before removing Retrofit.

### Task 6.6 — Remove Retrofit only after parity is proven
- Remove Retrofit/OkHttp dependencies only after Ktor is functionally equivalent.
- Update docs to reflect the internal transport migration.
- Confirm that consuming projects require no code change beyond upgrading the published library version.

---

## Phase 7 — Validation and hardening

### Task 7.1 — Build each affected module independently
Run builds for:
- `:core:networking`
- `:core:graphql`
- `:library`
- full project build

### Task 7.2 — Test code generation path multiple times
Validate these cases:
- clean build with schema refresh
- rebuild with no schema changes
- schema refresh failure fallback path
- strict mode failure behavior

### Task 7.3 — Test auth/header propagation
- verify headers are applied correctly to GraphQL requests
- verify token refresh/provider hooks behave correctly if implemented

### Task 7.4 — Test consumer-facing API stability
- instantiate the public API from `:library`
- confirm consumer setup is small and clear
- confirm no Apollo/Retrofit types leak into external usage

### Task 7.5 — Update repository documentation
Update `README.md` and/or docs to explain:
- new module layout
- high-level architecture
- how GraphQL support works
- how schema/codegen automation works
- what consumer projects need to change
- how the current Retrofit-based module is intentionally structured to allow a later move to Ktor

---

## Recommended execution order

1. Register modules in `settings.gradle.kts`
2. Update `gradle/libs.versions.toml`
3. Implement `core/networking/build.gradle.kts`
4. Add basic networking abstractions and Android transport shell
5. Ensure the networking contracts stay transport-agnostic so Retrofit can later be replaced with Ktor
6. Define public config/repository shape in `:library`
7. Implement `core/graphql/build.gradle.kts`
8. Add Apollo service config and schema location
9. Add schema download task and wire it into build/codegen
10. Add `.graphql` operations
11. Generate Apollo models
12. Add GraphQL wrapper + mapping layer
13. Connect `:library` repository implementations to GraphQL
14. Update publication config for transitive modules
15. Build/test all modules and edge cases
16. Update docs/README for consumers
17. When needed later, introduce a Ktor implementation behind the same contracts and validate parity before removing Retrofit

---

## What to do next

### Immediate next step
Implement **Phase 1** only:
- update `settings.gradle.kts`
- update `gradle/libs.versions.toml`
- implement `core/networking/build.gradle.kts`

### After that
Move to **Phase 3 and Phase 4** together for `:core:graphql`, because schema download and Apollo codegen are best designed as part of the initial GraphQL module setup rather than bolted on later.


