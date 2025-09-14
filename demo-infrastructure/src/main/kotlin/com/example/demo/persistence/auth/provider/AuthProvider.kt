package com.example.demo.persistence.auth.provider

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class AuthProvider(
	@Value("\${auth.x-api-key}") private val apiKey: String
) {
	fun ignoreListDefaultEndpoints(): List<String> =
		listOf(
			"/api-docs/**",
			"/swagger-ui/**",
			"/swagger.html",
			"/actuator/**"
		)

	fun whiteListDefaultEndpoints(): List<String> =
		listOf(
			"/api/v1/auth/signIn",
			"/api/v1/auth/refresh",
			"/api/v1/users/register"
		)

	fun getAllPermitEndpoints(): List<String> = whiteListDefaultEndpoints() + ignoreListDefaultEndpoints()

	fun validateApiKey(requestAPIKey: String): Boolean = apiKey == requestAPIKey
}
