import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jreleaser.model.Active

plugins {
    id("org.springframework.boot") version "3.2.4"
    id("io.spring.dependency-management") version "1.1.4"
    kotlin("jvm") version "1.9.23"
    kotlin("plugin.spring") version "1.9.23"

    id("maven-publish")
    id("org.jreleaser") version "1.11.0"
    id("signing")
}

group = "de.tschuehly"
version = "0.7.3"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
}
dependencies {
    api("de.tschuehly:spring-view-component-core:0.7.3")
    api("de.tschuehly:spring-view-component-jte:0.7.3")
    api("gg.jte:jte-kotlin:3.1.9")

    testImplementation("org.springframework.boot:spring-boot-starter-actuator")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.webjars:webjars-locator:0.52")
    testImplementation("org.webjars.npm:htmx.org:1.9.11")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict","-Xjvm-default=all")
        jvmTarget = "17"
    }
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
tasks.jar{
    enabled = true
    // Remove `plain` postfix from jar file name
    archiveClassifier.set("")
}
publishing{
    publications {

        create<MavenPublication>("Maven") {
            from(components["java"])
            groupId = "de.tschuehly"
            artifactId = "spring-view-component-kte"
            description = "Create server rendered components with KTE"
        }
        withType<MavenPublication> {
            pom {
                packaging = "jar"
                name.set("spring-view-component-kte")
                description.set("Spring ViewComponent KTE")
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
                        name.set("Thomas Schuehly")
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
        copyright.set("Thomas Schuehly")
    }
    gitRootSearch.set(true)
    signing {
        active.set(Active.ALWAYS)
        armored.set(true)
    }
    deploy {
        maven {
            nexus2 {
                create("maven-central") {
                    active.set(Active.ALWAYS)
                    url.set("https://s01.oss.sonatype.org/service/local")
                    snapshotUrl.set("https://s01.oss.sonatype.org/content/repositories/snapshots/")
                    closeRepository.set(true)
                    releaseRepository.set(true)
                    stagingRepositories.add("build/staging-deploy")
                }
            }
        }
    }

}