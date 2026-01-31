import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektCreateBaselineTask
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jlleitschuh.gradle.ktlint.KtlintExtension
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType
import org.springframework.boot.gradle.tasks.bundling.BootJar

val JVM_TARGET_VERSION = "21"

plugins {
	alias(libs.plugins.kotlin.jvm)
	alias(libs.plugins.kotlin.spring) apply false
	alias(libs.plugins.kotlin.jpa) apply false
	alias(libs.plugins.kotlin.kapt) apply false
	alias(libs.plugins.spring.boot) apply false
	alias(libs.plugins.spring.dependency.management) apply false
	alias(libs.plugins.ktlint) apply false
	alias(libs.plugins.detekt) apply false
}

allprojects {
	group = "com.example"
	version = "0.0.1-SNAPSHOT"

	repositories {
		mavenCentral()
	}
}

subprojects {
	val libs = rootProject.libs

	apply {
		plugin(libs.plugins.kotlin.jvm.get().pluginId)
		plugin(libs.plugins.spring.boot.get().pluginId)
		plugin(libs.plugins.kotlin.jpa.get().pluginId)
		plugin(libs.plugins.spring.dependency.management.get().pluginId)
		plugin(libs.plugins.kotlin.spring.get().pluginId)
		plugin(libs.plugins.ktlint.get().pluginId)
		plugin(libs.plugins.detekt.get().pluginId)
	}

	java {
		toolchain {
			languageVersion.set(JavaLanguageVersion.of(JVM_TARGET_VERSION.toInt()))
		}
	}

	fun DependencyResolveDetails.applyCveFixes() {
		when {
			requested.group == "org.apache.commons" && requested.name == "commons-lang3" -> {
				useVersion("3.18.0")
				because("CVE-2025-48924")
			}

			// CVE-2025-66566 fix: org.lz4 is deprecated, use at.yawk.lz4 instead
			requested.group == "org.lz4" && requested.name == "lz4-java" -> {
				useTarget("at.yawk.lz4:lz4-java:1.10.3")
				because("CVE-2025-66566 - Migrate to maintained fork")
			}

			requested.group == "ch.qos.logback" && requested.name.startsWith("logback") -> {
				useVersion("1.5.26")
				because("CVE-2026-1225")
			}

			requested.group == "io.netty" && requested.name.startsWith("netty-") -> {
				useVersion("4.2.8.Final")
				because("CVE-2025-67735")
			}

			requested.group == "org.springframework.security" && requested.name.startsWith("spring-security-") -> {
				useVersion("6.5.7")
				because("CVE-2025-41248")
			}

			requested.group == "org.apache.tomcat.embed" && requested.name.startsWith("tomcat-embed-") -> {
				useVersion("11.0.18")
				because("CVE-2025-55754")
			}

			requested.group == "org.springframework" && requested.name.startsWith("spring-") -> {
				useVersion("6.2.15")
				because("CVE-2025-41249")
			}
		}
	}

	// CVE fixes - must be outside dependencies block
	configurations.all {
		resolutionStrategy.eachDependency {
			applyCveFixes()
		}
	}

	dependencies {
		if (project.name != "demo-core") {
			api(project(":demo-core"))

			val modulesUsingTestFixtures = listOf(
				"demo-application",
				"demo-infrastructure",
				"demo-internal-api",
				"demo-external-api",
				"demo-batch",
				"demo-domain"
			)

			if (project.name in modulesUsingTestFixtures) {
				testImplementation(testFixtures(project(":demo-core")))

				val projectName = project.name
				val kotestPropertiesSource = rootProject.file("demo-core/src/testFixtures/resources/kotest.properties")

				tasks.named<ProcessResources>("processTestResources") {
					from(kotestPropertiesSource) {
						into(".")
					}

					doLast {
						if (kotestPropertiesSource.exists()) {
							println("✅ Copied kotest.properties to $projectName/build/resources/test/")
						} else {
							println("⚠️ kotest.properties not found in demo-core/src/testFixtures/resources/")
						}
					}
				}
			}
		}

		implementation(libs.spring.boot.devtools)

		if (org.gradle.internal.os.OperatingSystem.current().isMacOsX
			&& System.getProperty("os.arch") == "aarch64"
		) {
			runtimeOnly("io.netty:netty-resolver-dns-native-macos:4.1.94.Final:osx-aarch_64")
		}
	}

	tasks.withType<KotlinCompile> {
		compilerOptions {
			jvmTarget.set(
				when (JVM_TARGET_VERSION) {
					"21" -> JvmTarget.JVM_21
					"17" -> JvmTarget.JVM_17
					"11" -> JvmTarget.JVM_11
					"8" -> JvmTarget.JVM_1_8
					else -> JvmTarget.JVM_21
				}
			)
			freeCompilerArgs.add("-Xjsr305=strict")
		}
	}

	tasks.withType<Test> {
		useJUnitPlatform()

		failFast = false

		reports {
			html.required.set(true)
			junitXml.required.set(true)
		}

		testLogging {
			events("passed", "skipped", "failed")
			exceptionFormat = TestExceptionFormat.FULL
			showStandardStreams = false
		}
	}

	val executableModules = listOf("demo-bootstrap")
	if (project.name !in executableModules) {
		tasks.named<BootJar>("bootJar") {
			enabled = false
		}
		tasks.named<Jar>("jar") {
			enabled = true
		}
	}

	configure<KtlintExtension> {
		version.set("1.5.0")
		reporters {
			reporter(ReporterType.JSON)
		}
	}

	configure<DetektExtension> {
		config.setFrom("$rootDir/detekt.yml")
		allRules = true
		buildUponDefaultConfig = true
		ignoreFailures = false
		parallel = true

		source.setFrom(
			"src/main/kotlin",
			"src/test/kotlin"
		)
	}

	tasks.withType<Detekt> {
		reports {
			html.required.set(true)
			xml.required.set(true)
			sarif.required.set(false)
			md.required.set(true)
		}
		jvmTarget = JVM_TARGET_VERSION
		exclude("**/generated/**", "**/build/**")
	}

	tasks.withType<DetektCreateBaselineTask> {
		jvmTarget = JVM_TARGET_VERSION
	}

	tasks.withType<ProcessResources> {
		duplicatesStrategy = DuplicatesStrategy.WARN
	}

	tasks.withType<Jar> {
		isPreserveFileTimestamps = false
		isReproducibleFileOrder = true
	}
}

tasks.register("checkDependencies") {
	doLast {
		subprojects {
			println("Checking ${project.name}...")
			configurations.findByName("runtimeClasspath")?.let { config ->
				val resolved = config.resolvedConfiguration
				if (resolved.hasError()) {
					println("  ❌ Conflicts found:")
					resolved.lenientConfiguration.unresolvedModuleDependencies.forEach {
						println("    - ${it.selector}")
					}
				} else {
					println("  ✅ No conflicts")
				}
			}
		}
	}
}
