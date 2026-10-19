# Santa Soft TUI (`jp.co.santa.soft`)

A full-screen Terminal User Interface (TUI) application built with **Java 25**, **Quarkus**, and **TamboUI (`0.7.0-SNAPSHOT`)**.

## Prerequisites

- **Java 25** (JDK 25 or GraalVM for JDK 25)
- **Docker / Podman** (if building Linux native binaries via container)
- A real terminal emulator (e.g., Kitty, WezTerm, Alacritty, GNOME Terminal, or Windows Terminal)

---

## Build & Run Commands

### 1. Linux (Primary Deliverable)

```sh
# Compile and run unit tests
./gradlew clean build

# Build GraalVM native executable (Linux amd64 via container)
./gradlew quarkusBuild -Pnative -Dquarkus.native.container-build=true -PnativePlatform=linux

# Or build locally using your installed GraalVM 25 (faster, no Docker needed):
./gradlew quarkusBuild -Pnative

# Run the native binary in your terminal
./build/*-runner
```

## Project Structure

```txt
src/main/java/jp/co/santa/soft/
├── App.java                           # Quarkus main entry point (@QuarkusMain / Picocli)
└── tui/
    ├── config/
    │   └── TamboUiConfig.java         # @ConfigMapping for tamboui.* properties
    ├── core/
    │   ├── model/
    │   │   └── SystemTask.java        # Java 25 records & sealed interfaces
    │   └── service/
    │       └── SystemTaskService.java # Pure business logic (@ApplicationScoped)
    ├── component/
    │   └── StatusBarComponent.java    # Reusable TamboUI Toolkit Element factory
    ├── support/
    │   └── TuiPresenter.java          # Bridges CDI services and TamboUI state
    └── view/
        └── MainDashboardView.java     # Top-level screen using ToolkitRunner & Toolkit DSL

src/main/resources/
├── application.yaml                   # TUI-safe Quarkus & TamboUI configuration
└── tcss/
    └── default.tcss                   # TamboUI CSS stylesheet

src/test/java/jp/co/santa/soft/tui/
└── core/service/
    └── SystemTaskServiceTest.java     # Quarkus JUnit 5 service & buffer test

src/nativeTest/java/jp/co/santa/soft/tui/
└── NativeAppIT.java                   # GraalVM native-image smoke test
```

