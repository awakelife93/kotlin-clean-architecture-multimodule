plugins {
	kotlin("jvm")
	kotlin("plugin.spring")
}

dependencies {
	implementation(project(":demo-domain"))
	implementation(project(":demo-infrastructure"))
	implementation(project(":demo-application"))

	implementation(libs.spring.boot.starter.batch)

	testImplementation(libs.spring.batch.test)
}

tasks.test {
	useJUnitPlatform()
}
