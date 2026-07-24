# GraphQL Rollout Plan

This document reflects the current GraphQL architecture in the KMP library. The GraphQL-related work is now centered around **two modules only**:

- `:core:graphql`
- `:core:api`

The old `:core:networking` layer is no longer part of the GraphQL rollout plan. Endpoint ownership, schema/codegen wiring, and GraphQL client setup now live in `:core:graphql`, while consumer-facing setup and repository access live in `:core:api`.

## Goals

- Keep GraphQL implementation details out of the consumer-facing API.
- Keep Apollo, generated query models, and Koin wiring hidden behind library-owned APIs where practical.
- Keep `:core:graphql` focused on GraphQL-specific concerns only:
  - schema download
  - Apollo code generation
  - Apollo client creation
  - GraphQL endpoint/base URL ownership
  - GraphQL-specific mapping and error handling
- Keep `:core:api` focused on the SDK-facing surface:
  - environment/config entrypoints
  - bootstrap/wiring for Android and iOS
  - repository interfaces and implementations exposed to consumers
- Minimize change required in consuming Android and iOS projects.
- Preserve freedom to evolve the transport implementation later without reintroducing unnecessary module boundaries too early.
- Keep the design multiplatform-friendly.

---

## Current module responsibilities

### `:core:graphql`
Owns GraphQL implementation concerns.

Responsibilities:
- Apollo plugin configuration
- schema refresh task
- `.graphql` operations and generated Apollo models
- GraphQL environment and base URL ownership
- Apollo client factory/wrapper
- GraphQL DI modules internal to the implementation
- GraphQL result/error translation
- GraphQL-specific integration tests and mapping tests

### `:core:api`
Owns the consumer-facing GraphQL SDK surface.

Responsibilities:
- public-facing config entrypoint
- mapping from API environment/config to GraphQL config
- Koin/bootstrap entrypoints for Android and iOS
- repository implementations used by consuming apps
- keeping consumer setup small and stable

### Relationship between the two modules
- `:core:api` depends on `:core:graphql`
- `:core:graphql` does **not** depend on `:core:api`
- GraphQL-related consumers should ideally need only `:core:api`
- `:core:graphql` may remain published temporarily, but the long-term direction is to hide it from consumers when the public API surface is fully stabilized in `:core:api`

---

## Phase 0 — Principles and guardrails

### Task 0.1 — Keep public API intentional
- Keep a clear distinction between SDK-facing APIs and internal implementation APIs.
- Do not expose Apollo generated models in the long-term consumer contract.
- Do not expose Apollo client setup details to Android/iOS consuming apps unless there is a deliberate advanced-use case.
- Prefer library-owned configs, repositories, and models in `:core:api` for any surface that consumers are expected to call directly.

### Task 0.2 — Keep module boundaries simple
- Put GraphQL implementation/codegen/configuration in `:core:graphql`.
- Put consumer-facing GraphQL entrypoints in `:core:api`.
- Avoid reintroducing extra infrastructure modules unless there is a clear benefit that outweighs the added complexity.
- Keep dependency direction one-way:
  - `:core:api` -> `:core:graphql`
  - avoid `:core:graphql` -> `:core:api`

### Task 0.3 — Minimize downstream changes
- Consumers should not need to know whether Apollo wiring, schema refresh, caching, or DI changes internally.
- Any new required setup should be limited to a small configuration/bootstrap API.
- Android and iOS consumers should move toward depending only on `:core:api`.

Implementation note:
- if a type is only used inside this repository and is not part of the intended SDK contract, it should stay internal to `:core:graphql` or `:core:api`
- only true consumer-facing types should be treated as long-term public API

---

## Phase 1 — Stabilize the two-module architecture

### Task 1.1 — Keep Gradle/module registration aligned
- Ensure `settings.gradle.kts` reflects the active module layout.
- GraphQL-related work should assume only:
  - `:core:graphql`
  - `:core:api`
- Remove rollout assumptions that require a dedicated GraphQL networking module.

### Task 1.2 — Keep version catalog focused on actual usage
- Keep Apollo, Koin, Kotlin, and platform dependencies centralized in `gradle/libs.versions.toml`.
- Only keep dependency aliases that are still needed by `:core:graphql` and `:core:api`.
- Avoid documenting or planning around unused transport-layer dependencies as if they are part of the current GraphQL design.

### Task 1.3 — Keep endpoint ownership in `:core:graphql`
- Define GraphQL environments and base URLs in `:core:graphql`.
- Keep GraphQL URL resolution close to Apollo client creation.
- Avoid extra mapping layers for endpoints unless they provide real value.
- Keep `GraphQlConfig` GraphQL-native rather than delegating endpoint ownership elsewhere.

### Task 1.4 — Keep `:core:api` as the consumer bridge
- `:core:api` should accept consumer-facing environment/config values.
- `:core:api` should translate those values into `:core:graphql` configuration.
- Consumers should not need to understand the details of `GraphQlConfig` unless explicitly intended.

### Task 1.5 — Keep bootstrap APIs multiplatform-friendly
- Android and iOS setup should be small, explicit, and native-first where appropriate.
- Shared bootstrap helpers may exist in `:core:api`, but platform apps should still own lifecycle decisions.

---

## Phase 2 — Define and refine the public API in `core:api`

### Task 2.1 — Decide the intended consumer surface
Recommended shape:
- a small `FeastApiConfig`-style config object
- environment selection owned by `:core:api`
- repository interfaces or SDK entrypoints in `:core:api`
- platform-friendly bootstrap helpers for Android and iOS

### Task 2.2 — Keep configuration stable
The public-facing config should be simple and durable.

Recommended characteristics:
- environment-based by default
- optional future extension points only when justified
- avoid leaking Apollo-specific names into the public contract

### Task 2.3 — Move toward API-owned consumer models
- Do not mirror the full GraphQL schema 1:1.
- Introduce API-owned models only for the subset that is intentionally exposed to consumers.
- Keep Apollo generated types behind the `:core:graphql` boundary wherever possible.

### Task 2.4 — Keep mapping responsibilities explicit
- `:core:graphql` should map raw GraphQL responses into shapes that are safe for upstream use.
- `:core:api` should own any final translation needed for the public SDK surface.
- Nullability and GraphQL-specific field quirks should stay out of consumer code.

---

## Phase 3 — Implement and maintain `core:graphql`

### Task 3.1 — Configure `core/graphql/build.gradle.kts`
- Keep `:core:graphql` as a Kotlin Multiplatform module.
- Apply and maintain Apollo Kotlin plugin configuration.
- Keep Android/iOS source sets aligned with actual supported targets.
- Keep Apollo runtime and GraphQL-specific dependencies in the module.
- Avoid unnecessary dependencies on extra infrastructure modules for endpoint configuration.

### Task 3.2 — Keep package structure clear
Recommended structure inside `:core:graphql`:
- `src/commonMain/graphql/` for GraphQL operations and schema
- `src/commonMain/kotlin/...` for config, client wrappers, DI, repositories, and mappers
- `src/androidMain/...` and `src/iosMain/...` only for platform-specific GraphQL concerns such as caching or platform DI wiring

### Task 3.3 — Keep Apollo service configuration stable
- Use a stable package name for generated code.
- Keep generated package churn low.
- Ensure Apollo generation is reproducible and predictable.

### Task 3.4 — Keep the GraphQL client wrapper thin
Suggested responsibilities:
- create the Apollo client
- execute queries/mutations
- map low-level GraphQL/Apollo errors into project-level results
- resolve the final GraphQL server URL from GraphQL-owned config

Design note:
- future transport changes should be handled inside `:core:graphql` unless there is a strong reason to re-extract a separate transport layer later

### Task 3.5 — Keep operations focused
- Add `.graphql` files only for required use cases.
- Organize operations by feature/domain where useful.
- Keep operations version-controlled.

### Task 3.6 — Keep mapping and caching platform-aware
- Keep all GraphQL-specific field-name quirks inside this module.
- Handle nullability carefully.
- Allow Android/iOS cache behavior to differ when necessary, as long as the consumer-facing API remains stable.

### Task 3.7 — Test GraphQL behavior thoroughly
- Add or maintain tests for response mapping.
- Cover partial/null response scenarios.
- Cover GraphQL and transport-level error conversion.
- Validate platform-specific GraphQL setup where possible.

---

## Phase 4 — Automate schema download and code generation

### Task 4.1 — Keep schema refresh configurable
Recommended inputs:
- Gradle properties
- environment variables
- local developer overrides via `local.properties` or CI secrets

Avoid hardcoding secrets in the repo.

### Task 4.2 — Keep schema refresh owned by `:core:graphql`
Implement and maintain a Gradle task that:
- contacts the GraphQL server or introspection endpoint
- downloads the latest schema when configured
- writes it to the `:core:graphql` schema location
- declares proper task inputs/outputs as much as practical

### Task 4.3 — Wire schema refresh before Apollo codegen
Ensure build/codegen order is:
1. refresh or validate schema source
2. generate Apollo models
3. compile sources

### Task 4.4 — Keep failure strategy explicit
Recommended behavior:
- default: allow fallback to the checked-in schema when no remote refresh is configured or when non-strict refresh is used
- CI or strict mode: fail when schema refresh is required and cannot be completed

### Task 4.5 — Keep the schema committed
Recommended approach:
- commit the schema file for reproducibility and offline fallback
- do not commit generated Apollo classes unless there is a strong reason

### Task 4.6 — Document developer workflow
Document:
- required env vars/properties
- how to force-refresh schema
- how to run Apollo generation manually
- expected generated output location

---

## Phase 5 — Publishing and consumer impact

### Task 5.1 — Keep `core:api` as the intended entrypoint
- Document `:core:api` as the module Android/iOS consumers should prefer.
- Treat `:core:graphql` as an implementation/support module unless a specific advanced use case requires direct consumption.

### Task 5.2 — Keep publication metadata correct
- Ensure `:core:api` and `:core:graphql` publish with correct metadata and transitive dependencies.
- Ensure published artifacts no longer assume a GraphQL networking support module.
- Verify that consumer runtime classpaths do not require removed implementation modules.

### Task 5.3 — Verify consumer resolution paths
Conceptually validate:
- Android consumer setup
- iOS framework/XCFramework consumption
- any other target that is expected to use the GraphQL stack

### Task 5.4 — Check public API compatibility intentionally
- Review `api.txt` baselines when public API changes are introduced.
- Keep breaking changes intentional and documented.
- Prefer additive change where possible, but update baselines when architectural cleanup requires an intentional break.

### Task 5.5 — Keep consumer setup small
- Consumers should not need Apollo types, GraphQL endpoint enums, or DI internals.
- Consumers should not need to add an extra module just to satisfy an internal runtime dependency.

---

## Phase 6 — Future transport evolution

This phase is not required immediately, but the architecture should leave room for it.

### Task 6.1 — Audit transport-specific assumptions inside `:core:graphql`
- Identify any direct assumptions about the current transport stack.
- Keep those assumptions localized to client creation and low-level adapters.
- Confirm no consumer-facing API references transport-specific concepts.

### Task 6.2 — Prefer internal refactoring before new modules
- If transport behavior changes later, first try to evolve the implementation inside `:core:graphql`.
- Re-extract a separate transport/infrastructure module only if shared complexity actually justifies it.

### Task 6.3 — Keep future migration consumer-transparent
- Any later move in the underlying HTTP stack should not force consumer API changes in `:core:api`.
- Public config naming should remain generic enough to survive internal implementation swaps.

---

## Phase 7 — Validation and hardening

### Task 7.1 — Build each affected module independently
Run builds for:
- `:core:graphql`
- `:core:api`
- full project build as needed

### Task 7.2 — Test code generation multiple times
Validate these cases:
- clean build with schema refresh
- rebuild with no schema changes
- schema refresh failure fallback path
- strict mode failure behavior

### Task 7.3 — Test consumer-facing stability
- instantiate the public API from `:core:api`
- confirm Android and iOS setup remains small and clear
- confirm Apollo/generated GraphQL types are not unintentionally required by consumers

### Task 7.4 — Test publication/runtime behavior
- publish local artifacts
- verify transitive dependency resolution
- verify that consuming apps do not hit runtime classpath failures from removed internal module links

### Task 7.5 — Update repository documentation continuously
Update `README.md` and related docs to explain:
- the two-module GraphQL layout
- high-level architecture
- how schema/codegen automation works
- what Android/iOS consumers need to depend on
- the long-term direction of hiding `:core:graphql` behind `:core:api`

---

## Recommended execution order

1. Keep `:core:graphql` and `:core:api` module registration/build configuration aligned
2. Define or refine the public configuration/repository shape in `:core:api`
3. Keep `:core:graphql` configuration, endpoint ownership, and Apollo setup self-contained
4. Maintain schema download and Apollo codegen wiring in `:core:graphql`
5. Keep GraphQL wrapper/mapping logic inside `:core:graphql`
6. Keep consumer-facing bootstrapping and repositories in `:core:api`
7. Validate publication metadata and transitive runtime behavior
8. Build/test all affected modules and edge cases
9. Update docs/README for consumers
10. Only reintroduce a separate lower-level transport module later if there is a proven need

---

## What to do next

### Immediate next step
Focus on these continuously:
- keep `:core:graphql` implementation self-contained
- keep `:core:api` as the intended consumer entrypoint
- continue moving any exposed GraphQL implementation details behind API-owned contracts where it is worth the maintenance cost

### After that
The next meaningful follow-up is to further reduce direct consumer need for `:core:graphql` by:
- exposing only stable repository/config surfaces from `:core:api`
- introducing API-owned models for the subset of data intentionally shared with consumers
- documenting `:core:graphql` as an implementation detail over time
