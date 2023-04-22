import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "3.0.1"
	id("io.spring.dependency-management") version "1.1.0"
	id("maven-publish")
	kotlin("jvm") version "1.7.22"
	kotlin("plugin.spring") version "1.7.22"
	`maven-publish`
}

group = "de.tschuehly"
version = "0.4.2"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
	mavenCentral()
}

dependencies {
//	implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-aop")
	implementation("org.springframework.boot:spring-boot-devtools")
	implementation("io.projectreactor:reactor-core")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	testImplementation("org.springframework.boot:spring-boot-starter-test")

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
sourceSets {
    main {
        resources {
            srcDir("src/main/kotlin")
            exclude("**/*.kt")
        }
    }
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
			artifactId = "thymeleaf-view-component"
		}
		withType<MavenPublication> {
			pom {
				packaging = "jar"
				name.set("thymeleaf-view-component")
				description.set("Thymeleaf View Component")
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
			}
		}
	}
}
