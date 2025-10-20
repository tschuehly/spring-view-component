plugins {
    java
    id("org.springframework.boot") version "3.5.6"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "de.tschuehly"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

// build.gradle.kts
sourceSets {
    main {
        resources {
            srcDirs("src/main/java","src/main/kotlin")
            exclude("**/*.java","**/*.kt")
        }
    }

}

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("de.tschuehly:spring-view-component-thymeleaf:0.9.0")

    implementation("org.webjars.npm:htmx.org:1.9.11")
    implementation("org.webjars:webjars-locator-lite:1.1.0")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-devtools")
    testImplementation(testFixtures("de.tschuehly:spring-view-component-core:0.9.0"))
}

tasks.withType<Test> {
    useJUnitPlatform()
}