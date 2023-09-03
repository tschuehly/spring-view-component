import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jreleaser.model.Active

plugins {
	id("org.springframework.boot") version "3.1.2"
	id("io.spring.dependency-management") version "1.1.2"
	kotlin("jvm") version "1.8.21"
	kotlin("plugin.spring") version "1.8.21"

	id("maven-publish")
	id("org.jreleaser") version "1.5.1"
	id("signing")
}

group = "de.tschuehly"
version = "0.7.2"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
	mavenCentral()
}

dependencies {
	api("de.tschuehly:spring-view-component-core:0.7.2")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
	implementation("org.springframework.boot:spring-boot-devtools")

	testImplementation("org.springframework.boot:spring-boot-starter-actuator")
	testImplementation("org.springframework.boot:spring-boot-starter-test")

	testImplementation("org.webjars:webjars-locator:0.47")
	testImplementation("org.webjars.npm:htmx.org:1.9.2")
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

publishing{
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