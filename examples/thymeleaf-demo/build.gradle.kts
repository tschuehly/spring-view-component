import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.0.2"
    id("io.spring.dependency-management") version "1.1.0"
    kotlin("jvm") version "1.7.22"
    kotlin("plugin.spring") version "1.7.22"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")

    // Use this to develop locally when you include the build locally in your settings.gradle
//    implementation("de.tschuehly:spring-view-component-thymeleaf")
    implementation("com.github.tschuehly:spring-view-component:spring-view-component-thymeleaf:ae806c7e7e")

//  Use this to develop with the version on jitpack
//    implementation("com.github.tschuehly:thymeleaf-view-component:0.4.0")

    implementation("org.springframework.boot:spring-boot-starter-actuator")

    developmentOnly("org.springframework.boot:spring-boot-devtools")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}



sourceSets {
    main {
        resources {
            srcDir("src/main/kotlin")
            exclude("**/*.kt")
        }
    }
}