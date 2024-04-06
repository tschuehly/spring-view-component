import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.2.4"
    id("io.spring.dependency-management") version "1.1.4"
    kotlin("jvm") version "1.9.23"
    kotlin("plugin.spring") version "1.9.23"
    kotlin("kapt") version "1.9.23"
}



group = "de.tschuehly"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation("de.tschuehly:spring-view-component-kte:0.7.3-SNAPSHOT")
    kapt("de.tschuehly:spring-view-component-core:0.7.3-SNAPSHOT")
    implementation("io.github.wimdeblauwe:htmx-spring-boot:3.0.0")

    implementation("org.webjars.npm:htmx.org:1.9.2")
    implementation("org.webjars:webjars-locator:0.47")

    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-devtools")

    implementation("org.jetbrains.kotlin:kotlin-reflect")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-devtools")
    testImplementation(testFixtures("de.tschuehly:spring-view-component-core:0.7.3-SNAPSHOT"))
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
sourceSets.main.get().java.srcDir("build/generated-sources/jte")
