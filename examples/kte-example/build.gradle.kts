import org.jetbrains.kotlin.gradle.tasks.KaptGenerateStubs
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import kotlin.io.path.Path

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
    id("gg.jte.gradle") version("3.2.1")
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

// Security overrides until Spring Boot 4.0.x manages patched versions (Dependabot)
extra["tomcat.version"] = "11.0.27"
extra["jackson-2-bom.version"] = "2.21.8"
extra["jackson-bom.version"] = "3.1.8"
// Kotlin plugin's internal kotlinBouncyCastleConfiguration (Dependabot)
configurations.configureEach {
    resolutionStrategy.eachDependency {
        if (requested.group == "org.bouncycastle") useVersion("1.85")
    }
}

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation("de.tschuehly:spring-view-component-kte:0.9.1")
    implementation("de.tschuehly:spring-view-component-core:0.9.1")
    implementation("io.github.wimdeblauwe:htmx-spring-boot:5.0.0")

    implementation("org.webjars.npm:htmx.org:1.9.11")
    implementation("org.webjars:webjars-locator-lite:1.1.0")

    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-devtools")

    implementation("org.jetbrains.kotlin:kotlin-reflect")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.boot:spring-boot-restclient")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
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