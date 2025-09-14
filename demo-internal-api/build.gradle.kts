plugins {
	kotlin("jvm")
	kotlin("plugin.spring")
}

dependencies {
	implementation(project(":demo-application"))

	implementation(libs.bundles.spring.web)

	implementation(libs.springdoc.openapi)
}

tasks.test {
	useJUnitPlatform()
}
