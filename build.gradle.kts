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
    val muslJavaHome = System.getProperty("glibc.java.home")
        ?: System.getenv("JAVA_HOME")
        ?: error("Set -Dglibc.java.home=<path to glibc JDK>")
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

        // 1. Runtime + jar
        runtimeDir.copyRecursively(File(distDir, "runtime"))
        jar.copyTo(File(distDir, "redis-ex1-redb.jar"))

        // 2. Glibc libs
        val glibcDir = File(distDir, "glibc")
        glibcDir.mkdirs()
        listOf(
            "/lib64/ld-linux-x86-64.so.2",
            "/usr/lib/libc.so.6",
            "/usr/lib/libm.so.6",
            "/usr/lib/libdl.so.2",
            "/usr/lib/libpthread.so.0",
            "/usr/lib/librt.so.1",
            "/usr/lib/libresolv.so.2",
            "/usr/lib/libstdc++.so.6",
            "/usr/lib/libgcc_s.so.1",
            "/usr/lib/libz.so.1",
            "/usr/lib/libnss_dns.so.2",
            "/usr/lib/libnss_files.so.2",
            "/usr/lib/libutil.so.1",
            "/usr/lib/libcrypt.so.1",
            "/usr/lib/libatomic.so.1"
        ).forEach { path ->
            val src = File(path)
            if (src.exists()) {
                src.copyTo(File(glibcDir, src.name), overwrite = true)
            } else {
                println("  (skip, not present on host): $path")
            }
        }

        // 3. Make everything in glibc dir executable
        exec {
            commandLine("chmod", "-R", "755", glibcDir.absolutePath)
        }

        // 4. Patch runtime/bin/* — set interpreter and rpath (belt-and-suspenders;
        //    the launcher uses the loader explicitly, but this helps tools that
        //    might exec these binaries directly)
        File(distDir, "runtime/bin").listFiles()?.filter { it.isFile && isElf(it) }?.forEach { bin ->
            exec {
                commandLine("patchelf",
                    "--set-interpreter", "\$ORIGIN/../../glibc/ld-linux-x86-64.so.2",
                    "--set-rpath", "\$ORIGIN/../../glibc:\$ORIGIN/../lib:\$ORIGIN/../lib/server",
                    bin.absolutePath)
            }
            println("  patched: ${bin.relativeTo(distDir)}")
        }

        // 5. Patch runtime/lib/**.so — rpath only
        File(distDir, "runtime/lib").walkTopDown()
            .filter { it.isFile && it.name.endsWith(".so") }
            .forEach { so ->
                val depth = so.relativeTo(distDir).toPath().nameCount - 1
                val up = (1..depth).joinToString("/") { ".." }
                exec {
                    commandLine("patchelf",
                        "--set-rpath", "\$ORIGIN/$up/glibc:\$ORIGIN",
                        so.absolutePath)
                }
            }

        // 6. Pre-extract ONNX + tokenizers native libs and patch their rpaths
        val nativeDir = File(distDir, "native")
        nativeDir.mkdirs()
        copy {
            from(zipTree(jar)) {
                include("ai/onnxruntime/native/linux-x64/**")
                include("native/lib/linux-x86_64/**")
                eachFile { relativePath = RelativePath(true, file.name) }
                includeEmptyDirs = false
            }
            into(nativeDir)
        }
        nativeDir.listFiles()?.filter { it.name.endsWith(".so") }?.forEach { so ->
            exec {
                commandLine("patchelf",
                    "--set-rpath", "\$ORIGIN/../glibc:\$ORIGIN",
                    so.absolutePath)
            }
            println("  patched native: ${so.name}")
        }

        // 7. Launcher script — invokes loader explicitly, bypassing INTERP/$ORIGIN
        val launcher = File(distDir, "run.sh")
        launcher.writeText(""" 
            #!/bin/sh 
            DIR=${'$'}(cd "${'$'}(dirname "${'$'}0")" && pwd) 
            exec "${'$'}DIR/glibc/ld-linux-x86-64.so.2" --library-path "${'$'}DIR/glibc:${'$'}DIR/runtime/lib:${'$'}DIR/runtime/lib/server" "${'$'}DIR/runtime/bin/java" -jar "${'$'}DIR/redis-ex1-redb.jar" 
        """.trimIndent())
        launcher.setExecutable(true)

        println("Portable distribution created at: ${distDir.absolutePath}")
    }
}

// Helper: quick ELF magic check
fun isElf(file: File): Boolean {
    if (!file.isFile || file.length() < 4) return false
    return file.inputStream().use { stream ->
        val magic = ByteArray(4)
        stream.read(magic)
        magic[0] == 0x7F.toByte() && magic[1] == 'E'.code.toByte() &&
                magic[2] == 'L'.code.toByte() && magic[3] == 'F'.code.toByte()
    }
}

tasks.test {
    useJUnitPlatform()
}