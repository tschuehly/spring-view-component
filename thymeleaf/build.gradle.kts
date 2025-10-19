import org.jreleaser.model.Active

plugins {
    id("org.springframework.boot") version "3.5.6"
    id("io.spring.dependency-management") version "1.1.7"
    kotlin("jvm") version "2.2.20"
    kotlin("plugin.spring") version "2.2.20"

    id("maven-publish")
    id("org.jreleaser") version "1.20.0"
    id("signing")
}

group = "de.tschuehly"
version = "0.9.0-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
}

dependencies {
    api("de.tschuehly:spring-view-component-core:0.9.0-SNAPSHOT")
    api("org.springframework.boot:spring-boot-devtools")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")

    testImplementation("org.springframework.boot:spring-boot-starter-actuator")
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    testImplementation("org.webjars:webjars-locator-lite:1.1.0")
    testImplementation("org.webjars.npm:htmx.org:1.9.11")
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


sourceSets {
    test {
        resources {
            srcDir("src/test/kotlin")
            exclude("**/*.kt")
        }
    }
    main {
        resources {
            srcDir("src/main/kotlin")
            exclude("**/*.kt")
        }
    }

}

publishing {
    publications {

        create<MavenPublication>("Maven") {
            from(components["java"])
            groupId = "de.tschuehly"
            artifactId = "spring-view-component-thymeleaf"
            description = "Create server rendered components with thymeleaf"
        }
        withType<MavenPublication> {
            pom {
                packaging = "jar"
                name.set("spring-view-component-thymeleaf")
                description.set("Spring ViewComponent Thymeleaf")
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