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

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    implementation("de.tschuehly:spring-view-component-thymeleaf:0.7.3-SNAPSHOT")
    annotationProcessor("de.tschuehly:spring-view-component-core:0.7.3-SNAPSHOT")

    implementation("org.webjars.npm:htmx.org:1.9.2")
    implementation("org.webjars:webjars-locator-core:0.53")
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    testImplementation("org.springframework.boot:spring-boot-devtools")
    testImplementation(testFixtures("de.tschuehly:spring-view-component-core:0.7.3-SNAPSHOT"))
}

tasks.withType<Test> {
    useJUnitPlatform()
}