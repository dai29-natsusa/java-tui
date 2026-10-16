You are a senior Java architect with deep expertise in Quarkus, modern Java (25), and terminal user interface (TUI) frameworks. I need you to generate a complete, clean, and maintainable project skeleton for a TUI application built with **Java 25**, **Quarkus** (as the backend), and **TamboUI** (for the TUI layer).

### 1. Project Overview

Create a multi-module Quarkus project that:
- Uses **Java 25** as the target language level.
- Uses **Quarkus** for dependency injection, configuration, lifecycle management, and backend services.
- Uses **TamboUI** (dev.tamboui:* artifacts) to render an interactive, full-screen terminal UI.
- Demonstrates a clean separation of concerns: the TUI layer should not contain business logic; the Quarkus backend should expose services that the TUI consumes.
- Is structured for **maintainability, testability, and future extension**.

### 2. Technical Stack & Constraints

- **Java 25**: Leverage modern features where appropriate (records, sealed classes, pattern matching, virtual threads, etc.). Do not use preview features unless strictly necessary.
- **Quarkus**: Use the latest stable Quarkus version compatible with Java 25. Use `quarkus-picocli` or `quarkus-aesh` for command-mode entry points if needed, but the primary entry point should launch the TamboUI terminal interface.
- **TamboUI**: Use the `tamboui-toolkit` (declarative DSL) as the primary UI API, with `tamboui-tui` (TuiRunner) for the managed event loop. Use `tamboui-panama-backend` for the terminal backend (Java 25 native FFM support). Include `tamboui-css` for TCSS-based styling.
- **Build Tool**: Gradle Include the OSSRH snapshots repository since TamboUI is currently distributed via snapshots.
- **GraalVM**: The project must be compatible with GraalVM native-image compilation. Include the necessary Quarkus native profile and any TamboUI annotation processor configuration needed for native image compatibility.

### 3. Project Structure

Generate a multi-module Gradle project with the following modules:

```
project-root/
├── pom.xml (parent POM with dependency management)
├── app/ # Quarkus application entry point
│ └── src/main/java/.../App.java
│ └── src/main/resources/application.yaml
├── core/ # Domain logic & services (Quarkus beans)
│ └── src/main/java/.../service/
│ └── src/main/java/.../model/
├── tui/ # TamboUI views, components, and event handling
│ └── src/main/java/.../view/
│ └── src/main/java/.../component/
│ └── src/main/resources/tcss/ # TCSS stylesheets
└── integration-tests/ # (Optional) Integration tests
```


Explain the purpose of each module and the dependency direction (e.g., `app` depends on `tui` and `core`; `tui` depends on `core`; `core` has no UI dependencies).

### 4. Dependencies (Maven Example **BUT** use Gradle for build tools)

Provide the full `<dependencyManagement>` and `<dependencies>` sections for the parent POM. Include:

- Quarkus BOM (latest compatible with Java 25).
- `quarkus-arc` (CDI)
- `quarkus-picocli` (or `quarkus-aesh` if more appropriate)
- `tamboui-toolkit`
- `tamboui-tui`
- `tamboui-panama-backend`
- `tamboui-css`
- `tamboui-annotations` + `tamboui-processor` (for native image)
  - required `reflect-config.json` entries file
- `quarkus-junit5` (test scope)
- Any other necessary artifacts.

Include the OSSRH snapshot repository configuration.

### 5. Configuration

Provide the `application.yaml` for Quarkus. Include:
- Disabling the HTTP server (since this is a CLI/TUI app, not a web app).
- Any TamboUI-specific configuration (e.g., backend selection, theme).
- Logging configuration suitable for a TUI (avoid logging to stdout/stderr that would corrupt the terminal display; use file-based logging or a Quarkus console handler that respects the TUI).

### 6. TUI Layer Design

Provide a concrete example of a TamboUI view using the Toolkit DSL. The example should:
- Use `ToolkitRunner` (or `TuiRunner`) to manage the event loop.
- Define a simple but realistic screen: e.g., a main menu with a list of options, a detail pane, and a status bar.
- Use TCSS for styling (provide the `.tcss` file content).
- Demonstrate event handling (key bindings for navigation, quit, etc.).
- Use `@OnAction` annotations if appropriate for action handling.
- Show how the TUI obtains data from a Quarkus service (inject the service into the view or a controller).

Explain the pattern for adding new screens/views and how to handle navigation between them.

### 7. Backend Services (Core Module)

Provide one or two example Quarkus services (e.g., `GreetingService`, `DataService`) that:
- Are CDI beans (`@ApplicationScoped`).
- Contain pure business logic (no TUI dependencies).
- Are consumed by the TUI layer via constructor injection or method parameters.

Show how to wire the service into the TUI view.

### 8. Build & Run

Provide:
- Gradle (kts) build commands for JVM mode and native mode.
- The command to run the TUI application (including the necessary `--enable-native-access=ALL-UNNAMED` flag for the Panama backend).
- Any JBang one-liner for quick testing, if applicable.
- Notes on running the TUI inside a real terminal (not an IDE console) to avoid "dumb terminal" fallback.

### 9. Testing Strategy

Provide:
- A unit test for the core service (using JUnit 5 + Mockito or Quarkus test framework).
- A suggestion for testing the TUI layer (e.g., using TamboUI's `core-assertj` for buffer comparison, or snapshot testing).
- Any integration test example.

### 10. Best Practices & Conventions

Include a section on:
- Package naming conventions - root package name is `jp.co.santa.soft.tui`
- How to keep the TUI layer thin (view models, presenters, or controllers).
- How to handle asynchronous operations (e.g., virtual threads or Quarkus `@Blocking`).
- How to manage state in an immediate-mode TUI without creating spaghetti code.
- How to structure TCSS files for theming.
- How to prepare for GraalVM native image (reflection config, annotation processors).
- Including Spotless Gradle Plugin and configure style to use Java Google Style.

### 11. Deliverables

Provide the complete source code for:
- Parent `build.gradle.kts`
- Each module's Gradle's files (if have or needed)
- `App.java` (Quarkus main entry point)
- One example TUI view (with Toolkit DSL)
- One example TCSS file
- One example core service
- `application.yaml`
- A `README.md` explaining how to build, run, and extend the project.

Ensure all code is syntactically correct, uses modern Java idioms, and follows Quarkus and TamboUI best practices. Include comments where the logic is non-obvious.

### 12. Additional Notes

- **IMPORTANT** when coding, go check find the actually existing resources that related TamboUI before proceeding on solutions and coding.
- TamboUI is experimental; pin a specific snapshot version and note that APIs may change.
- If Quarkus has a dedicated TamboUI extension, use it; otherwise, use the raw TamboUI artifacts.
- Avoid any HTTP server dependencies unless explicitly required.
- The final project should be runnable with native binary as first priority and `java -jar` (JVM mode) as alternative runnable method.

Generate the complete project now.
