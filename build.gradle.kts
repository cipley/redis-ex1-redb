import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar


plugins {
    java
    application
    id("io.github.goooler.shadow") version "8.1.8"
}

group = "com.cipley.submission.redis"
version = "1.0.0"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

application {
    mainClass = "com.cipley.submission.redis.Application"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.lettuce:lettuce-core:7.5.2.RELEASE")
    implementation("org.yaml:snakeyaml:2.6")

    implementation("org.slf4j:slf4j-api:2.0.18")
    implementation("ch.qos.logback:logback-classic:1.5.32")

    implementation("org.jline:jline:3.30.13")
    // ANSI support for Windows
    implementation("org.jline:jline-terminal-jansi:3.30.13")
    implementation("org.fusesource.jansi:jansi:2.4.3")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("-Xlint:deprecation")
}

tasks.named<JavaExec>("run") {
    standardInput = System.`in`
    isIgnoreExitValue = true

    val os = System.getProperty("os.name").lowercase()
    if (os.contains("win")) {
        jvmArgs(
            "-Djline.terminal=jline.AnsiWindowsTerminal",
            "-Djansi.passthrough=true"
        )
    } else {
        jvmArgs(
            "-Djline.terminal=jline.UnixTerminal",
            "-Djline.internal.Log.debug=false"
        )
    }
}

tasks.register<JavaExec>("runApp") {
    dependsOn("classes")
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass = "com.cipley.submission.redis.Application"
    standardInput = System.`in`
    isIgnoreExitValue = true

    val os = System.getProperty("os.name").lowercase()
    if (os.contains("win")) {
        jvmArgs("-Djline.terminal=jline.AnsiWindowsTerminal", "-Djansi.passthrough=true")
    } else {
        jvmArgs("-Djline.terminal=jline.UnixTerminal", "-Djline.internal.Log.debug=false")
    }
}

tasks.register("runDev") {
    dependsOn("classes")
    doLast {
        val classpath = sourceSets.main.get().runtimeClasspath.asPath
        val mainClass = "com.cipley.submission.redis.Application"
        val os = System.getProperty("os.name").lowercase()

        val jvmArgs = if (os.contains("win")) {
            listOf("-Djline.terminal=jline.AnsiWindowsTerminal", "-Djansi.passthrough=true")
        } else {
            listOf("-Djline.terminal=jline.UnixTerminal", "-Djline.internal.Log.debug=false")
        }

        val command = listOf("java") + jvmArgs + listOf("-cp", classpath, mainClass)

        ProcessBuilder(command)
            .inheritIO()
            .start()
            .waitFor()
    }
}

tasks.named<ShadowJar>("shadowJar") {
    archiveBaseName.set("redis-ex1-redb")
    archiveClassifier.set("")
    archiveVersion.set("")
    manifest {
        attributes["Main-Class"] = "com.example.App"
    }
}

tasks.test {
    useJUnitPlatform()
}