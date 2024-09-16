plugins {
    java
    id("org.springframework.boot") version "3.2.4"
    id("io.spring.dependency-management") version "1.1.4"
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
    implementation("de.tschuehly:spring-view-component-thymeleaf:0.8.4")

    implementation("org.webjars.npm:htmx.org:1.9.11")
    implementation("org.webjars:webjars-locator-core:0.58")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-devtools")
    testImplementation(testFixtures("de.tschuehly:spring-view-component-core:0.8.4"))
}

tasks.withType<Test> {
    useJUnitPlatform()
}