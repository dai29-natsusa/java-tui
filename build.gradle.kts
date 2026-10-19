plugins {
    application
    java
    id("io.quarkus")
    id("com.diffplug.spotless") version "8.10.4"
}

val quarkusPlatformGroupId = project.property("quarkusPlatformGroupId")
val quarkusPlatformArtifactId = project.property("quarkusPlatformArtifactId")
val quarkusPlatformVersion = project.property("quarkusPlatformVersion")
val tambouiVersion = project.property("tambouiVersion")
val nativePlatform: String = project.findProperty("nativePlatform")?.toString() ?: "linux"

group = "jp.co.santa.soft"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
    // TamboUI snapshots are distributed via Sonatype Central Portal / OSSRH snapshots
    maven {
        url = uri("https://central.sonatype.com/repository/maven-snapshots/")
        mavenContent {
            snapshotsOnly()
        }
    }
    maven {
        url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
        mavenContent {
            snapshotsOnly()
        }
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

dependencies {
    // Quarkus BOM
    implementation(enforcedPlatform("$quarkusPlatformGroupId:$quarkusPlatformArtifactId:$quarkusPlatformVersion"))

    // TamboUI BOM (pinned to 0.7.0-SNAPSHOT per documentation)
    implementation(platform("dev.tamboui:tamboui-bom:$tambouiVersion"))

    // Quarkus Core, DI, YAML Config, and CLI entry point
    implementation("io.quarkus:quarkus-arc")
    implementation("io.quarkus:quarkus-picocli")
    implementation("io.quarkus:quarkus-config-yaml")

    // TamboUI Layers: Toolkit DSL, TuiRunner, Panama FFM Backend (Java 25), and TCSS
    implementation("dev.tamboui:tamboui-toolkit")
    implementation("dev.tamboui:tamboui-tui")
    implementation("dev.tamboui:tamboui-panama-backend")
    implementation("dev.tamboui:tamboui-css")

    // TamboUI Native Image & Action Binding Compile-Time Generation
    implementation("dev.tamboui:tamboui-annotations")
    annotationProcessor("dev.tamboui:tamboui-processor:$tambouiVersion")

    // Testing (Quarkus JUnit 5 + TamboUI AssertJ buffer assertions)
    testImplementation("io.quarkus:quarkus-junit")
    testImplementation("org.assertj:assertj-core:4.0.0-M1")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-parameters")
}

tasks.withType<Test> {
    systemProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager")
    jvmArgs("--enable-native-access=ALL-UNNAMED")
}

// Ensure interactive terminal execution works when running via Gradle/Quarkus
tasks.quarkusDev {
    jvmArgs = listOf("--enable-native-access=ALL-UNNAMED")
}

// Check if native build is requested via -Pnative, -Dnative, -Dquarkus.package.type=native, or buildNative task
val isNative =
    project.hasProperty("native") ||
        System.getProperty("native") != null ||
        project.findProperty("quarkus.package.type") == "native" ||
        System.getProperty("quarkus.package.type") == "native" ||
        gradle.startParameter.taskNames.any { it.equals("buildNative", ignoreCase = true) }

// Configure GraalVM Native Image build based on -PnativePlatform=linux|windows
quarkus {
    if (isNative) {
        quarkusBuildProperties.put("quarkus.native.enabled", "true")
        quarkusBuildProperties.put("quarkus.package.jar.enabled", "false")
        quarkusBuildProperties.put("quarkus.native.auto-service-loader-registration", "true")
    }

    // Required by TamboUI Panama FFM backend and TamboUI JFR native image support
    val nativeArgs =
        mutableListOf(
            "--enable-native-access=ALL-UNNAMED",
            "--enable-monitoring=jfr",
            "-H:+UnlockExperimentalVMOptions",
            "-H:IncludeResources=.*\\.tcss$,META-INF/services/.*",
            "--initialize-at-run-time=dev.tamboui.backend.panama",
        )

    if (nativePlatform.equals("windows", ignoreCase = true)) {
        // Windows native build adjustments
        nativeArgs.add("-Dquarkus.native.container-build=false")
    } else {
        // Default container runtime if container-build is requested
        quarkusBuildProperties.put("quarkus.native.container-runtime", "docker")
    }

    quarkusBuildProperties.put(
        "quarkus.native.additional-build-args",
        nativeArgs.joinToString(","),
    )
}

spotless {
    java {
        target("src/**/*.java")
        googleJavaFormat()
        removeUnusedImports()
        trimTrailingWhitespace()
        endWithNewline()
    }
    kotlinGradle {
        target("*.gradle.kts")
        ktlint()
        trimTrailingWhitespace()
        endWithNewline()
    }
    format("yaml") {
        target("**/*.yaml", "**/*.yml")
        prettier()
    }
}
