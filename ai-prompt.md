You are a senior Java architect with deep expertise in Quarkus, modern Java (25), Gradle Kotlin DSL, and terminal user interface (TUI) frameworks. I need you to generate a complete, clean, and maintainable **single-module** project skeleton for a TUI application built with **Java 25**, **Quarkus** (as the backend), and **TamboUI** (for the TUI layer).

### 0. CRITICAL — Consult the TamboUI JavaDoc API First

This project is being created inside a Gemini Notebook that already contains the **TamboUI JavaDoc API reference** as a source document.

**Before you generate any code**, you MUST:
1. **Read and analyze the TamboUI JavaDoc API** available in the Notebook's source documents.
2. Use **only** classes, methods, annotations, and constructors that actually exist in that JavaDoc. Do NOT invent APIs, guess method names, or rely on memorized/assumed TamboUI signatures.
3. If you are unsure whether an API exists, explicitly state so and either:
   - Search the JavaDoc again, or
   - Provide a clearly marked placeholder with a `// TODO: verify against TamboUI JavaDoc` comment.
4. In your final output, include a short section titled **"TamboUI API References Used"** that lists each TamboUI class/interface used and where in the JavaDoc it was sourced from.

Violating this rule (inventing APIs) is the single most important failure mode to avoid.

### 1. Project Overview

Create a **single-module** Gradle project that:
- Uses **Java 25** as the target language level.
- Uses **Quarkus** for dependency injection, configuration, lifecycle management, and backend services.
- Uses **TamboUI** to render an interactive, full-screen terminal UI.
- Clearly separates concerns inside the module using packages (see §3): the TUI layer must not contain business logic; the Quarkus backend exposes services the TUI consumes.
- Is structured for **maintainability, testability, and future extension**.
- Produces **one runnable JAR** (JVM mode) and **one native binary** (GraalVM native-image mode).

### 2. Technical Stack & Constraints

- **Java 25**: Leverage modern features (records, sealed classes, pattern matching, virtual threads) where appropriate. Do not use preview features unless strictly necessary.
- **Quarkus**: Latest stable version compatible with Java 25. Use `quarkus-picocli` or `quarkus-aesh` for a command-mode entry point if needed, but the primary entry point must launch the TamboUI terminal interface.
- **TamboUI**: Use `tamboui-toolkit` (declarative DSL) as the primary UI API, with `tamboui-tui` (TuiRunner) for the managed event loop. Use `tamboui-panama-backend` for the terminal backend (Java 25 FFM). Include `tamboui-css` for TCSS styling, and `tamboui-annotations` + `tamboui-processor` for native-image support.
- **Build Tool**: **Gradle with Kotlin DSL (`build.gradle.kts` and `settings.gradle.kts`)**. Use the Gradle Wrapper. Prefer the `io.quarkus` Gradle plugin. Justify any non-obvious plugin or configuration choice in comments.
- **Config File**: Use **`application.yaml`** (NOT `application.properties`) for Quarkus configuration. Ensure `quarkus-config-yaml` is included.
- **Base Package**: `jp.co.santa.soft.vsmc` — all application code must live under this package (see §3 for sub-packages).
- **Native Binary Priority**: The **primary** deliverable is a **Linux native binary** built with GraalVM. A Windows native binary is the **secondary** deliverable. JVM-mode JAR is a fallback for development.
- **Cross-platform awareness**: Ensure the build is parameterized (Gradle `-PnativePlatform=linux|windows`) so the same `build.gradle.kts` can produce either target without modification. Document any platform-specific considerations.

### 3. Package Structure (Single Module)

Use a single Gradle module with package-based separation under the base package `jp.co.santa.soft.vsmc`:

```sh
src/main/java/jp/co/santa/soft/tui/
├── App.java # Quarkus main entry point
├── config/ # Configuration mappings (@ConfigMapping), Quarkus producers
├── core/ # Domain logic & services (Quarkus beans)
│ ├── model/ # Records, sealed types, domain models
│ └── service/ # @ApplicationScoped services
├── tui/ # TamboUI views, components, event handling
│ ├── view/ # Screens / top-level views
│ ├── component/ # Reusable UI components
│ └── support/ # TUI helpers (navigation, theming, key bindings)

src/main/resources/
├── application.yaml # Quarkus config
└── tcss/ # TamboUI TCSS stylesheets

src/nativeTest/java/ # (Optional) native-mode smoke test
```


Explain the purpose of each package and the dependency direction:
- `App` → depends on `tui` and `config`
- `tui` → depends on `core` and `config`
- `core` → no UI dependencies
- `config` → may depend on `core` for types, but not on `tui`

### 4. Gradle Kotlin DSL Build

Provide the complete `settings.gradle.kts` and `build.gradle.kts`. Include:

- The Gradle Wrapper version compatible with Java 25.
- The `io.quarkus` Gradle plugin with the correct version.
- Dependency declarations for:
  - Quarkus BOM (via `enforcedPlatform`)
  - `quarkus-arc`
  - `quarkus-picocli` (or `quarkus-aesh`)
  - `quarkus-config-yaml`
  - `tamboui-toolkit`
  - `tamboui-tui`
  - `tamboui-panama-backend`
  - `tamboui-css`
  - `tamboui-annotations` + `annotationProcessor("...:tamboui-processor")`
  - `quarkus-junit5` (test scope)
- The **OSSRH snapshot repository** (`https://s01.oss.sonatype.org/content/repositories/snapshots/`) since TamboUI is distributed via snapshots. Pin a specific snapshot version.
- The Quarkus `native` profile tasks, including any `native-image` arguments required by the TamboUI Panama backend (e.g., `--enable-native-access=ALL-UNNAMED`).
- A Gradle property `nativePlatform` (default `linux`) that toggles native-image target between `linux-amd64` and `windows-amd64`. Document how to override with `-PnativePlatform=windows`.
- JVM args for the `run` task: `--enable-native-access=ALL-UNNAMED --enable-preview` (only if preview features are genuinely needed; otherwise omit `--enable-preview`).
- Gradle toolchain configuration to enforce Java 25.
- Install/Setup for auto-formatting with Spotless Gradle plugin and use "Google Java Coding Style", if it's available use the style that fit with Modern Java or Java 25 compatible.

### 5. Configuration (`application.yaml`)

Provide `src/main/resources/application.yaml` including:
- Disabling the HTTP server (this is a CLI/TUI app):
  ```yaml
  quarkus:
    http:
      host-enabled: false
  ```

- Logging that does **not** write to stdout/stderr (which would corrupt the TUI). Route logs to a file (e.g., `./logs/tui.log`) or configure the Quarkus console handler to be TUI-safe.
- TamboUI-specific configuration under a `tamboui:` root key (e.g., default backend, theme name) using `@ConfigMapping` in `config/`.
- Any Quarkus native-image settings.

### 6. TUI Layer Design

Provide a concrete example of a TamboUI view using the Toolkit DSL. The example must:

- Use the managed event loop (ToolkitRunner / TuiRunner — whichever the JavaDoc confirms).
- Define a realistic screen: a main menu with a list, a detail pane, and a status bar.
- Use TCSS for styling (provide the `.tcss` content).
- Demonstrate key bindings for navigation and quit.
- Show how the TUI obtains data from a Quarkus service (injection into a controller/presenter, not directly into the view if the JavaDoc encourages that pattern).
- Explain the pattern for adding new screens and handling navigation.

**All API usages must be traced to the TamboUI JavaDoc.**

### 7. Backend Services (Core Package)

Provide one or two example Quarkus services (e.g., `GreetingService`, `DataService`) that:

- Are CDI beans (`@ApplicationScoped`).
- Contain pure business logic (no TUI dependencies).
- Are consumed by the TUI layer via CDI injection.
- Use Java 25 features (records, sealed interfaces) where they improve clarity.

### 8. Build & Run

Provide exact commands for:

**Linux (primary):**

```sh
./gradlew clean build
./gradlew quarkusBuild --native -Dquarkus.native.container-build=true -PnativePlatform=linux
./build/*-runner
```

**Windows (secondary):**

```sh
.\gradlew.bat clean build
.\gradlew.bat quarkusBuild --native -PnativePlatform=windows
.\build\*-runner.exe
```

**JVM mode (fallback / dev):**

```sh
./gradlew quarkusDev
# or
java --enable-native-access=ALL-UNNAMED -jar build/quarkus-app/quarkus-run.jar
```

Also provide:

- Notes on running the TUI inside a real terminal (not an IDE console) to avoid "dumb terminal" fallback.
- Notes on `--enable-native-access=ALL-UNNAMED` (required by the Panama backend).
- If applicable, a JBang one-liner for quick experiments.

### 9. Testing Strategy

Provide:

- A unit test for a core service using JUnit 5 + Quarkus test framework.
- A suggestion for TUI testing (TamboUI `core-assertj` buffer comparison or snapshot tests), based on the JavaDoc.
- A note on how to run the native smoke test (`./gradlew nativeTest`).

### 10. Best Practices & Conventions

Include a section covering:

- Package conventions under `jp.co.santa.soft.vsmc`.
- Keeping the TUI layer thin (presenters/view models if TamboUI supports them).
- Asynchronous operations (virtual threads, `@Blocking`, `@RunOnVirtualThread`).
- Managing state in an immediate-mode TUI without spaghetti code.
- Structuring TCSS files for theming.
- GraalVM native-image preparation (reflection config, annotation processors, reachability metadata).
- Cross-platform considerations for Linux vs Windows native builds (terminal capabilities, ANSI support, path handling).
- Always format the codes with Spotless Gradle plugin

### 11. Deliverables

Provide complete, syntactically correct source code and configuration for:

- `settings.gradle.kts`
- `build.gradle.kts`
- `gradle.properties` (if needed)
- `gradle/wrapper/gradle-wrapper.properties`
- `App.java` (Quarkus main entry point)
- One example TUI view (Toolkit DSL) + one reusable component
- One example TCSS file
- One example core service
- `application.yaml`
- A `README.md` explaining how to build, run, and extend the project on both Linux (primary) and Windows (secondary)

### 12. TamboUI API References Used (mandatory closing section)

End your response with a section that lists:

- Every TamboUI class/interface/annotation used in the generated code.
- A short quote or paraphrase of its purpose, sourced from the JavaDoc in the Notebook.
- Any API you were unable to verify and therefore marked with `// TODO: verify against TamboUI JavaDoc`.

### 13. Additional Notes

TamboUI is experimental. Pin a specific snapshot version and note that APIs may change.

- If Quarkus provides a dedicated TamboUI extension, use it; otherwise use raw TamboUI artifacts.
- Do not add HTTP server dependencies unless explicitly required.
- The final project must be runnable as:
  - A JVM-mode JAR: `java --enable-native-access=ALL-UNNAMED -jar build/quarkus-app/quarkus-run.jar`
  - A Linux native binary: `./build/*-runner`
  - A Windows native binary: `.\build\*-runner.exe`

Generate the complete single-module Gradle (Kotlin DSL) project now, following every constraint above.
