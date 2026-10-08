plugins {
    java
    application
}

group = "app"
version = "0.1.0"

val tambouiVersion = "0.5.0"

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
    // Toolkit DSL (pulls in core, widgets, tui, css transitively)
    implementation("dev.tamboui:tamboui-toolkit")
    // A terminal backend is required at runtime
    implementation("dev.tamboui:tamboui-jline3-backend")
}

application {
    mainClass = "app.HelloTui"
    // Terminal backends use native access; this avoids JDK 24+ warnings
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
