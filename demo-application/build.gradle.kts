plugins {
	kotlin("jvm")
	kotlin("plugin.spring")
}

dependencies {
	api(project(":demo-domain"))
	api(project(":demo-infrastructure"))
}

tasks.test {
	useJUnitPlatform()
}
