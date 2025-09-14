plugins {
	kotlin("jvm")
	kotlin("plugin.spring")
	id("org.springframework.boot")
	id("io.spring.dependency-management")
}

dependencies {
	implementation(project(":demo-domain"))
	implementation(project(":demo-application"))
	implementation(project(":demo-infrastructure"))
	implementation(project(":demo-internal-api"))
	implementation(project(":demo-external-api"))
	implementation(project(":demo-batch"))

	implementation(libs.bundles.sentry)
}
