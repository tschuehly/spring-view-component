import org.jreleaser.model.Active

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
            classpath("org.apache.tika:tika-core:3.2.3")
            classpath("org.codehaus.plexus:plexus-utils:3.6.2")
        }
    }
}

plugins {
    id("org.springframework.boot") version "4.0.8"
    id("io.spring.dependency-management") version "1.1.7"
    kotlin("jvm") version "2.2.21"
    kotlin("plugin.spring") version "2.2.21"

    id("maven-publish")
    id("org.jreleaser") version "1.26.0"
    id("signing")
    id("java-test-fixtures")
}

group = "de.tschuehly"
version = "0.9.1"
java.sourceCompatibility = JavaVersion.VERSION_17

// Security overrides until Spring Boot 4.0.x manages patched versions (Dependabot).
// resolutionStrategy only affects this build's classpaths; unlike extra["..."] BOM
// properties it is not written into the published POM/.module, so consumers keep their own versions.
configurations.configureEach {
    resolutionStrategy.eachDependency {
        when {
            requested.group == "org.apache.tomcat.embed" -> useVersion("11.0.27")
            requested.group == "com.fasterxml.jackson.core" && requested.name != "jackson-annotations" -> useVersion("2.21.8")
            requested.group == "tools.jackson.core" -> useVersion("3.1.8")
            requested.group == "org.bouncycastle" -> useVersion("1.85") // Kotlin plugin's internal kotlinBouncyCastleConfiguration
        }
    }
}

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-aspectj")
    compileOnly("org.springframework.boot:spring-boot-devtools")
    implementation("io.projectreactor:reactor-core")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    testFixturesImplementation("org.springframework.boot:spring-boot-starter-test")
    testFixturesImplementation("org.springframework.boot:spring-boot-resttestclient")
    testFixturesImplementation("org.springframework.boot:spring-boot-starter-webmvc")
    testFixturesImplementation("org.springframework.boot:spring-boot-devtools")

}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
    jvmToolchain(17)
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks {
    bootJar {
        enabled = false
    }
}
java {
    withJavadocJar()
    withSourcesJar()
}


tasks.jar {
    enabled = true
    // Remove `plain` postfix from jar file name
    archiveClassifier.set("")
}
publishing {
    publications {

        create<MavenPublication>("Maven") {
            from(components["java"])
            groupId = "de.tschuehly"
            artifactId = "spring-view-component-core"
            description = "Create server rendered components with spring"
        }
        withType<MavenPublication> {
            pom {
                packaging = "jar"
                name.set("spring-view-component-core")
                description.set("Spring ViewComponent Core")
                url.set("https://github.com/tschuehly/spring-view-component/")
                inceptionYear.set("2023")
                licenses {
                    license {
                        name.set("MIT license")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                developers {
                    developer {
                        id.set("tschuehly")
                        name.set("Thomas Schilling")
                        email.set("thomas.schuehly@outlook.com")
                    }
                }
                scm {
                    connection.set("scm:git:git@github.com:tschuehly/spring-view-component.git")
                    developerConnection.set("scm:git:ssh:git@github.com:tschuehly/spring-view-component.git")
                    url.set("https://github.com/tschuehly/spring-view-component")
                }
            }
        }
    }
    repositories {
        maven {
            url = layout.buildDirectory.dir("staging-deploy").get().asFile.toURI()
        }
    }
}

jreleaser {
    project {
        copyright.set("Thomas Schilling")
    }
    gitRootSearch.set(true)
    signing {
        active.set(Active.ALWAYS)
        armored.set(true)
    }
    deploy {
        maven {
            mavenCentral {

                create("release-deploy") {
                    active.set(Active.ALWAYS)
                    url.set("https://central.sonatype.com/api/v1/publisher")
                    stagingRepositories.add("build/staging-deploy")
                }
            }
            nexus2 {
                create("snapshot-deploy") {
                    active.set(Active.SNAPSHOT)
                    snapshotSupported.set(true)
                    url.set("https://central.sonatype.com/api/v1/publisher")
                    snapshotUrl.set("https://central.sonatype.com/repository/maven-snapshots")

                    closeRepository.set(true)
                    releaseRepository.set(true)
                    stagingRepositories.add("build/staging-deploy")
                    applyMavenCentralRules = true
                }
            }
        }
    }


}