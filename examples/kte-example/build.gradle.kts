import org.jetbrains.kotlin.gradle.tasks.KaptGenerateStubs
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import kotlin.io.path.Path

plugins {
    id("org.springframework.boot") version "3.2.4"
    id("io.spring.dependency-management") version "1.1.4"
    kotlin("jvm") version "1.9.23"
    kotlin("plugin.spring") version "1.9.23"
    kotlin("kapt") version "1.9.23"
    id("gg.jte.gradle") version("3.1.12")
}


jte{
    generate()
    sourceDirectory = Path("src/main/kotlin")
}

tasks.withType<KaptGenerateStubs>(){
    dependsOn("generateJte")
}

group = "de.tschuehly"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation("de.tschuehly:spring-view-component-kte:0.8.4-SNAPSHOT")
    implementation("de.tschuehly:spring-view-component-core:0.8.4-SNAPSHOT")
    implementation("io.github.wimdeblauwe:htmx-spring-boot:3.1.1")

    implementation("org.webjars.npm:htmx.org:1.9.11")
    implementation("org.webjars:webjars-locator:0.52")

    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-devtools")

    implementation("org.jetbrains.kotlin:kotlin-reflect")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-devtools")
    testImplementation(testFixtures("de.tschuehly:spring-view-component-core:0.8.4-SNAPSHOT"))
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}