import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// Patched versions for the Gradle plugin classpath (build-time only; Dependabot)
buildscript {
    dependencies {
        constraints {
            classpath("tools.jackson.core:jackson-core:3.1.8")
            classpath("tools.jackson.core:jackson-databind:3.1.8")
            classpath("org.apache.httpcomponents.core5:httpcore5:5.4.4")
            classpath("org.apache.httpcomponents.core5:httpcore5-h2:5.4.4")
            classpath("org.apache.httpcomponents.client5:httpclient5:5.6.4")
            classpath("org.apache.commons:commons-lang3:3.20.0")
        }
    }
}

plugins {
    id("org.springframework.boot") version "4.0.8"
    id("io.spring.dependency-management") version "1.1.7"
    kotlin("jvm") version "2.2.21"
    kotlin("plugin.spring") version "2.2.21"
    kotlin("kapt") version "2.2.21"
}

group = "de.tschuehly.example"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

// Security overrides until Spring Boot 4.0.x manages patched versions (Dependabot)
extra["tomcat.version"] = "11.0.27"
extra["jackson-2-bom.version"] = "2.21.8"
extra["jackson-bom.version"] = "3.1.8"

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation("de.tschuehly:spring-view-component-thymeleaf:0.9.1")
    implementation("de.tschuehly:spring-view-component-core:0.9.1")

    implementation("org.webjars.npm:htmx.org:1.9.2")
    implementation("org.webjars:webjars-locator-lite:1.1.0")
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    developmentOnly("org.springframework.boot:spring-boot-devtools")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.boot:spring-boot-restclient")
    testImplementation("org.springframework.boot:spring-boot-devtools")
    testImplementation(testFixtures("de.tschuehly:spring-view-component-core:0.9.1"))
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
    jvmToolchain(17)
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}
sourceSets {
    main {
        resources {
            srcDirs("src/main/java", "src/main/kotlin")
            exclude("**/*.java", "**/*.kt")
        }
    }

    test {
        resources {
            srcDirs("src/main/java", "src/main/kotlin")
            exclude("**/*.java", "**/*.kt")
        }
    }

}