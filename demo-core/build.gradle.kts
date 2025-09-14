plugins {
	kotlin("jvm")
	kotlin("plugin.spring")
	`java-test-fixtures`
}

dependencies {
	api(libs.spring.boot.starter)
	api(libs.spring.boot.starter.aop)

	api(libs.bundles.kotlin.core)
	api(libs.kotlin.logging)

	testFixturesApi(libs.bundles.test.spring)
	testFixturesApi(libs.bundles.test.kotest)
	testFixturesApi(libs.bundles.test.mock)
}

tasks.test {
	useJUnitPlatform()
}
