#   AGENTS.md

You are working on **Notes**, a local-first Markdown notes app for Android and iOS, built from one Kotlin Multiplatform + Compose Multiplatform codebase. This file is the system prompt for any coding agent in this repo. `CLAUDE.md` is a symlink to it, so edit only this file.

## Core directives

These rules apply to every task in every session. They override any other guidance in this file.

1. **Requirements come from `requirements/`.** Before any feature or change, read the spec in `requirements/` (currently `notes-feature-specification.md`). Build only what it describes. If something isn't in the spec, or the spec lists it under *Out of scope*, don't build it; ask the user instead.
2. **Design stays within `design/`.** The UI must match the screens, tokens, and components in `design/`. Don't invent screens, components, colors, spacing, or layouts that `design/` doesn't define. If a state you need isn't designed, ask.
3. **This is a Kotlin Multiplatform (KMM) app for Android and iOS only.** Shared code goes in `commonMain`, and both platforms must keep building after every change.
4. **Use clean architecture.** Dependencies point `presentation → domain ← data`, and the domain layer is pure Kotlin. See [Architecture](#architecture).
5. **Unit-test all business logic.** Every piece of business logic ships with unit tests in the same change. Don't write instrumentation or UI tests for now: no `androidDeviceTest`, Espresso, or XCUITest.
6. **Gradle must pass.** End every session with the [session verification](#session-verification-required) passing. Never leave the build broken. Never make it pass by deleting, skipping, or `@Ignore`-ing tests, or by weakening build config.

## Sources of truth

These define the binding scope of the app. Read them before you build or change a feature. They take precedence over your own assumptions.

| What | Where |
|---|---|
| Product behavior, edge cases, acceptance criteria | `requirements/notes-feature-specification.md` |
| Visual design: screens, tokens, components | `design/` (open `design/index.html`; tokens in `design/assets/tokens.css`) |
| How to write the code | `.claude/skills/<skill>/SKILL.md` (see [Skills](#skills)) |

If the spec, the designs, and a skill disagree, follow the spec for behavior, the designs for appearance, and the skill for code structure. Point out the conflict instead of guessing silently.

Anything listed under **Out of scope** in the spec (sync, accounts, tags, folders, attachments, bulk import/export, trash, encryption, and so on) is not built. Don't add it, even partly, unless the user asks.

## Non-negotiable product rules

These come from the spec and outrank everything else:

1. **Never lose a word.** Autosave is debounced at 500 ms and flushed whenever the note screen leaves the foreground. Drafts survive process death through `SavedStateHandle`. No change may weaken this.
2. **Store Markdown exactly as typed.** Never rewrite, reformat, or normalize the user's text. The only exception is line endings on import (`\r\n` and `\r` become `\n`).
3. **Fully offline.** No network permission, analytics, tracking, or third-party SDKs that send data off the device. The only outbound action is handing a link to the system browser.
4. **Imported content is untrusted.** Raw HTML is never rendered or run, and only `http`, `https`, and `mailto` links open.
5. **Nothing runs on the main thread** except UI: database, file, and Markdown-parsing work runs off it. Respect the performance budgets in the spec (for example, a search update in ≤ 100 ms with 10,000 notes, and typing within one frame).
6. **Accessible by default.** Every interactive element has an accessible name, role, and state. Touch targets are at least 48 dp on Android and 44 pt on iOS. Meaning never relies on color alone. Text scales to 200%, and animations respect reduce-motion.
7. **No hard-coded user-facing text.** All strings go in Compose resources. Dates and times use the device locale. Layouts support right-to-left.

## Project layout

```
Code/                        ← Gradle root (run all commands from here)
├── androidApp/              ← Thin Android shell: MainActivity, manifest, launcher icons
├── shared/                  ← KMP library: all shared Compose UI and logic
│   └── src/
│       ├── commonMain/      ← Shared code (default home for everything)
│       ├── androidMain/     ← Android `actual`s
│       ├── iosMain/         ← iOS `actual`s, MainViewController
│       ├── commonTest/      ← Shared unit tests (kotlin.test)
│       ├── androidHostTest/ ← Android JVM host tests
│       └── iosTest/         ← iOS simulator tests
├── iosApp/                  ← Xcode project, SwiftUI entry point hosting the shared UI
├── gradle/libs.versions.toml← Version catalog: every dependency and plugin version
├── requirements/            ← Feature specification
└── design/                  ← HTML design mockups and design tokens
```

- Package root: `com.istudio.noteskmp`.
- Targets: Android (minSdk 24, compile/target SDK 37) and iOS (`iosArm64`, `iosSimulatorArm64`). There is no desktop or web target.
- The project is still the starter template. `:shared` plays the role the skills call `:composeApp`. When you split code into `core` and `feature` modules, follow `kmp-module-structure`, and introduce `build-logic` convention plugins at that point instead of copying Gradle config between modules.

## Tech stack

- Kotlin 2.4, AGP 9 (`com.android.kotlin.multiplatform.library` for KMP modules), Gradle with configuration cache and build cache enabled.
- Compose Multiplatform 1.12 with Material 3, JetBrains lifecycle ViewModel and runtime-compose.
- Planned per the skills and spec: Koin for DI, type-safe Compose Navigation, Room (KMP) with an FTS index for search, and kotlinx-coroutines and Flow.
- Add every new dependency through `gradle/libs.versions.toml`. Never hard-code versions in build files.

## Commands

Run these from `Code/`:

| Task | Command |
|---|---|
| Build the Android debug app | `./gradlew :androidApp:assembleDebug` |
| Run shared tests on the JVM (fastest) | `./gradlew :shared:testAndroidHostTest` |
| Run shared tests on the iOS simulator | `./gradlew :shared:iosSimulatorArm64Test` |
| Compile all targets | `./gradlew build` |
| iOS app | Open `iosApp/iosApp.xcodeproj` in Xcode and run |

### Session verification (required)

After the last change of every session, run these from `Code/` and make sure all of them pass:

```
./gradlew build
./gradlew :shared:testAndroidHostTest
./gradlew :shared:iosSimulatorArm64Test
```

Report the actual output. Don't call a change done while any of these fail. If you can't run one of them (for example, iOS on a machine without Xcode), say so plainly.

## Architecture

- **Clean Architecture, feature-first:** `presentation → domain ← data`. Domain is pure Kotlin and depends on nothing platform- or framework-specific. Features never depend on each other.
- **MVI presentation:** each screen has a `State`, an `Action` sealed interface, an `Event` sealed interface for one-off effects, and a ViewModel exposing `StateFlow<State>`. Split each screen into a `Root` composable (wires the ViewModel and navigation) and a stateless `Screen` composable (previewable).
- **Typed errors:** use the project `Result<T, E>` / `EmptyResult<E>` wrapper with `DataError` and similar types. Don't throw exceptions across layer boundaries. Map errors to `UiText` in presentation.
- **Repositories** return domain models and own the mapping from Room entities. The database is the single source of truth, and the UI observes it through `Flow`.
- **Inject dispatchers** so tests can control them. Never use `Dispatchers.Main` or `GlobalScope` directly inside logic classes.
- Prefer `commonMain`. Use `expect`/`actual` (or an interface plus a platform implementation provided through DI) only for real platform APIs: the file picker, the share/save flow, opening URLs, and the database builder.

## Skills

This repo ships skills in `.claude/skills/`. Claude Code loads them automatically. Other agents should open the matching `SKILL.md` before writing that kind of code.

| Skill | Use it when |
|---|---|
| `kmp-module-structure` | Adding or restructuring modules, Gradle, convention plugins, the version catalog |
| `kmp-presentation-mvi` | ViewModels, State/Action/Event, Root/Screen composables, `UiText`, `SavedStateHandle` |
| `kmp-compose-architecture` | Screen-level structure, recomposition, stability, side effects, lazy lists |
| `kmp-compose-components` | Any composable that isn't a Root/Screen: buttons, list items, toolbars, dialogs |
| `kmp-data-layer` | Repositories, data sources, Room entities and DAOs, mappers |
| `kmp-error-handling` | `Result`, error types, `onSuccess`/`onFailure`, error mapping |
| `kmp-di-koin` | Koin modules, ViewModel injection, `startKoin` on Android and iOS |
| `kmp-navigation` | Routes, nav graphs, cross-feature navigation |
| `kmp-unit-testing` | Any unit test |

The skills mention Ktor, token storage, and auth. Those parts don't apply here because the app has no network layer.

## UI and design

- Map the design tokens in `design/assets/tokens.css` (palette: paper, ink, cobalt, marker, graphite) into the Compose theme once, in a design-system location. Read colors from `MaterialTheme.colorScheme` or the project theme, never from raw hex values in components.
- Light and dark themes follow the system setting and must meet WCAG 2.2 AA contrast, including muted Markdown markers and search highlights.
- Search highlights use a background color **and** bold.
- Follow platform conventions where the spec requires them: on Android, a FAB for create, predictive back, and edge-to-edge. On iOS, swipe-back, native share sheet and file picker, and iOS-style alerts. Everything else shares one look.
- Every reusable component gets a `@Preview` for each meaningful state, in light and dark.

## Testing

- **Unit tests are required for all business logic.** That covers domain use cases, validators, parsers and formatters, mappers, repositories (tested against fakes), and ViewModels. Each test ships in the same change as the logic it covers.
- **Don't write instrumentation tests for now.** Add nothing under `androidDeviceTest`, and write no Espresso, Compose UI, or XCUITest tests. Leave the `withDeviceTestBuilder` config in `shared/build.gradle.kts` as it is, unused.
- Write unit tests in `commonTest` with `kotlin.test`. Use fakes, not mocks. Use Turbine for flows. Keep tests free of I/O: no real disk, database, network, or clock.
- Test spec-driven logic as pure functions, with edge cases taken from the spec: fallback titles, Markdown stripping for previews, list continuation, toolbar wrap and unwrap, search matching (diacritics, prefix, AND), import decoding (BOM, line endings, 5 MB limit), export file-name sanitizing, and relative time formatting.
- The spec requires tests for database migrations. Since instrumentation tests are off for now, write them as JVM host tests if possible. If that isn't feasible, flag it to the user instead of skipping it silently.

## Working style

- End every session with the session verification passing (see [Core directives](#core-directives)).
- Keep changes scoped to the request. Don't refactor unrelated code or add features the spec doesn't ask for.
- Match the surrounding code's naming, structure, and comment density. Use the official Kotlin code style (`kotlin.code.style=official`).
- Remove the template placeholders (`Greeting`, `GreetingUtil`, the "Click me!" `App`) when the real feature replaces them, not before.
- Don't commit, push, or open PRs unless asked. Work on a branch, never directly on `main`.
