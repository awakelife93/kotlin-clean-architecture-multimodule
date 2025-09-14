plugins {
	kotlin("jvm")
}

dependencies {
	compileOnly("org.springframework.data:spring-data-commons")
}

tasks.test {
	useJUnitPlatform()
}
