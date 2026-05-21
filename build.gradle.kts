plugins {
    java
    application
}

group = "com.cipley.submission.redis"
version = "1.0.0"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
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

    implementation("org.jline:jline:4.1.0")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}