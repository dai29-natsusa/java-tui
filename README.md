# tamboui-app

TamboUI (Toolkit DSL) starter. Java 25, Gradle Kotlin DSL.

## JVM

```bash
./gradlew installDist
./build/install/tamboui-app/bin/tamboui-app     # run from a real terminal
./gradlew run                                   # may not get a real TTY (see notes)
```

## Native binary (GraalVM)

```bash
./gradlew nativeCompile
./build/native/nativeCompile/tamboui-app        # tamboui-app.exe on Windows
```

Native images cannot be cross-compiled. Build on each target OS/CPU (see `.github/workflows/native.yml`).

### Prerequisites per OS

| OS      | Needs                                                                                                   |
|---------|---------------------------------------------------------------------------------------------------------|
| Linux   | `build-essential zlib1g-dev` (or distro equivalent)                                                     |
| macOS   | Xcode command line tools: `xcode-select --install`                                                      |
| Windows | Visual Studio Build Tools, "Desktop development with C++" workload; run from an x64 Native Tools prompt |

The GraalVM JDK itself is fetched by Gradle's toolchain support (Foojay) on first build.

### If the native binary fails at runtime

1. See which dependencies lack reachability metadata:
   `./gradlew listLibrariesMissingMetadata`
2. Record metadata by exercising the app on the JVM with the tracing agent (use the installed launcher so you have a
   real terminal):

   ```bash
   ./gradlew installDist
   JAVA_OPTS="-agentlib:native-image-agent=config-merge-dir=src/main/resources/META-INF/native-image/app/tamboui-app" \
     ./build/install/tamboui-app/bin/tamboui-app
   ```

   Run the JVM from a GraalVM 25 JDK (`JAVA_HOME`), use every feature of the app, quit,
   then rebuild with `./gradlew nativeCompile`.
