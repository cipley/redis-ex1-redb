import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar


plugins {
    java
    application
    id("io.github.goooler.shadow") version "8.1.8"
    id("org.graalvm.buildtools.native") version "0.10.2"
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

    implementation("com.redis:redisvl:0.13.1")

    implementation("tools.jackson.core:jackson-databind:3.1.3")

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
            listOf(
                "-Djline.terminal=jline.AnsiWindowsTerminal",
                "-Djansi.passthrough=true",
                "-Djdk.httpclient.HttpClient.log=all"
            )
        } else {
            listOf(
                "-Djline.terminal=jline.UnixTerminal",
                "-Djline.internal.Log.debug=false",
                "-Djdk.httpclient.HttpClient.log=all"
            )
        }

        val command = listOf("java") + jvmArgs + listOf("-cp", classpath, mainClass)

        println(">>> Launching: ${command.joinToString(" ")}")

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

tasks.register<Exec>("jlinkRuntime") {
    dependsOn("shadowJar")

    // Point to musl JDK instead of the system JDK
    val muslJavaHome = System.getProperty("musl.java.home")
        ?: error("Set -Dmusl.java.home=<path to Alpine JDK>")
    val outputDir = layout.buildDirectory.dir("runtime").get().asFile

    commandLine(
        "$muslJavaHome/bin/jlink",
        "--module-path", "$muslJavaHome/jmods",
        "--add-modules", "java.base,java.logging,java.xml,java.naming,java.net.http,jdk.net,java.management,jdk.crypto.ec",
        "--output", outputDir.absolutePath,
        "--strip-debug",
        "--compress=2",
        "--no-header-files",
        "--no-man-pages"
    )
}

tasks.register("distPortable") {
    dependsOn("jlinkRuntime", "shadowJar")
    doLast {
        val distDir = layout.buildDirectory.dir("portable").get().asFile
        val runtimeDir = layout.buildDirectory.dir("runtime").get().asFile
        val jar = layout.buildDirectory.file("libs/redis-ex1-redb.jar").get().asFile

        distDir.deleteRecursively()
        distDir.mkdirs()

        // Copy bundled JRE
        runtimeDir.copyRecursively(File(distDir, "runtime"))

        // Copy fat jar
        jar.copyTo(File(distDir, "redis-ex1-redb.jar"))

        // Create launcher script (Linux/macOS)
        val launcher = File(distDir, "run.sh")
        launcher.writeText("""
            #!/bin/sh
            DIR=${'$'}(cd "${'$'}(dirname "${'$'}0")" && pwd)
            "${'$'}DIR/runtime/bin/java" -jar "${'$'}DIR/redis-ex1-redb.jar"
        """.trimIndent())
        launcher.setExecutable(true)

        // Create launcher script (Windows)
        File(distDir, "run.bat").writeText("""
            @echo off
            set DIR=%~dp0
            "%DIR%runtime\bin\java" -jar "%DIR%redis-ex1-redb.jar"
        """.trimIndent())

        println("Portable distribution created at: ${distDir.absolutePath}")
    }
}

tasks.test {
    useJUnitPlatform()
}