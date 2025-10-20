import kotlin.io.path.Path

plugins {
    java
    id("org.springframework.boot") version "3.5.6"
    id("io.spring.dependency-management") version "1.1.7"
    id("gg.jte.gradle") version("3.2.1")
}

group = "de.tschuehly"
version = "0.0.1-SNAPSHOT"
jte{
    generate()
    sourceDirectory = Path("src/main/java")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")

    implementation("de.tschuehly:spring-view-component-jte:0.9.0")

    implementation("org.webjars.npm:htmx.org:1.9.11")
    implementation("org.webjars:webjars-locator-lite:1.1.0")
    runtimeOnly("org.springframework.boot:spring-boot-devtools")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.springframework.boot:spring-boot-devtools")
    testImplementation(testFixtures("de.tschuehly:spring-view-component-core:0.9.0"))
}

tasks.withType<Test> {
    useJUnitPlatform()
}