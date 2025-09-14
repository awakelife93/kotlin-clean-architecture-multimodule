plugins {
	kotlin("jvm")
	kotlin("plugin.spring")
}

dependencies {
	implementation(project(":demo-domain"))

	implementation(libs.spring.boot.starter.webflux)

	implementation(libs.slack.api.client)
	
	implementation(libs.spring.boot.starter.web)
}

tasks.test {
	useJUnitPlatform()
}
