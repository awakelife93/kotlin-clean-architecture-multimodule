plugins {
	kotlin("jvm")
	kotlin("plugin.spring")
	kotlin("kapt")
}

dependencies {
	api(project(":demo-domain"))

	api(libs.bundles.spring.security)

	api(libs.jjwt.api)
	runtimeOnly(libs.jjwt.impl)
	runtimeOnly(libs.jjwt.jackson)

	api(libs.bundles.spring.data)

	implementation(libs.querydsl.jpa)
	kapt("${libs.querydsl.apt.get()}:jpa")
	kapt(libs.jakarta.annotation.api)
	kapt(libs.jakarta.persistence.api)

	runtimeOnly(libs.bundles.database.postgresql)

	runtimeOnly(libs.bundles.database.h2)

	api(libs.spring.kafka)

	implementation(libs.spring.boot.starter.mail)

	implementation(libs.bundles.monitoring)
	implementation(platform(libs.opentelemetry.bom))
}

tasks.test {
	useJUnitPlatform()
}
