import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.nio.file.Path

plugins {
    id("org.springframework.boot") version "3.1.2"
    id("io.spring.dependency-management") version "1.1.2"
    kotlin("jvm") version "1.8.21"
    kotlin("plugin.spring") version "1.8.21"
    kotlin("kapt") version "1.8.21"
    id("gg.jte.gradle") version("3.0.2")
}

group = "de.tschuehly"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

jte{
    generate()
}
repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

//jte {
//    sourceDirectory.set(Path.of("src/main/kotlin"))
//    generate()
//}
dependencies {
//    implementation("de.tschuehly:spring-view-component-jte:0.6.0")
    implementation("de.tschuehly:spring-view-component-jte:0.6.2-SNAPSHOT")
    kapt("de.tschuehly:spring-view-component-core:0.6.2-SNAPSHOT")

    implementation("org.webjars.npm:htmx.org:1.9.2")
    implementation("org.webjars:webjars-locator:0.47")

    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-devtools")

    implementation("gg.jte:jte:3.0.2")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
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
//sourceSets {
//    main {
//        resources {
//            srcDir("src/main/kotlin")
//            exclude("**/*.kt")
//        }
//    }
//    test {
//        resources {
//            srcDir("src/test/kotlin")
//            exclude("**/*.kt")
//        }
//    }
//}
