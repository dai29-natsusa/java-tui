plugins {
    java
    application
    // GraalVM Native Build Tools: adds nativeCompile, nativeRun, metadataCopy, ...
    id("org.graalvm.buildtools.native") version "1.1.14"
}

group = "app"
version = "0.1.0"

val tambouiVersion = "0.5.0"
val appName = "tamboui-app"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

dependencies {
    // The BOM keeps all TamboUI modules on one version
    implementation(platform("dev.tamboui:tamboui-bom:$tambouiVersion"))
    // Toolkit DSL (pulls in core, widgets, tui, CSS transitively)
    implementation("dev.tamboui:tamboui-toolkit")
    // A terminal backend is required at runtime.
    implementation("dev.tamboui:tamboui-jline3-backend")
}

application {
    mainClass = "app.HelloTui"
    // Terminal backends use native access; this avoids JDK 24+ warnings (JVM only)
    applicationDefaultJvmArgs = listOf("--enable-native-access=ALL-UNNAMED")
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release = 25
}

tasks.named<JavaExec>("run") {
    // A TUI needs the real terminal's stdin, not Gradle's empty one
    standardInput = System.`in`
}

graalvmNative {
    binaries {
        named("main") {
            imageName.set(appName)
            mainClass.set("app.HelloTui")

            javaLauncher.set(javaToolchains.launcherFor {
                languageVersion = JavaLanguageVersion.of(25)
                vendor = JvmVendorSpec.matching("GraalVM Community")
            })

            // Uncomment while diagnosing a native build:
            verbose.set(true)
            buildArgs.addAll(
                "--verbose",
                "-H:+UnlockExperimentalVMOptions",
                "-H:IncludeResources=.*\\.tcss$",
                "-H:IncludeResources=.*\\.properties$",
                "-H:IncludeResources=.*\\.caps$",
                "-H:Log=registerResource:3",
                "--enable-native-access=ALL-UNNAMED")
        }
    }

    // Pull community-maintained reachability metadata for dependencies (JLine, ...)
    metadataRepository {
        enabled.set(true)
    }
}
