import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jreleaser.model.Active

plugins {
	id("org.springframework.boot") version "3.0.1"
	id("io.spring.dependency-management") version "1.1.0"
	id("maven-publish")
	kotlin("jvm") version "1.7.22"
	kotlin("plugin.spring") version "1.7.22"
	id("org.jreleaser") version "1.5.1"
	id("signing")
}

group = "de.tschuehly"
version = "0.5.1"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
	mavenCentral()
}

dependencies {
	api("de.tschuehly:spring-view-component-core")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
	implementation("org.springframework.boot:spring-boot-devtools")
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
			artifactId = "spring-view-component-thymeleaf"
		}
		withType<MavenPublication> {
			pom {
				packaging = "jar"
				name.set("spring-view-component-thymeleaf")
				description.set("Spring ViewComponent Thymeleaf")
				url.set("https://github.com/tschuehly/spring-view-component/")
				licenses {
					license {
						name.set("MIT license")
						url.set("https://opensource.org/licenses/MIT")
					}
				}
				developers {
					developer {
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
}

jreleaser {
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
					closeRepository.set(false)
					releaseRepository.set(false)
					stagingRepositories.add("target/staging-deploy")
				}

			}
		}
	}

}