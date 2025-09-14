package com.example.demo.common.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
class CorsConfig(
	private val environment: Environment
) {
	@Bean
	fun corsConfigurationSource(): CorsConfigurationSource =
		UrlBasedCorsConfigurationSource().apply {
			registerCorsConfiguration("/**", createCorsConfiguration())
		}

	private val corsAllowedOrigins: List<String> by lazy {
		when {
			"prod" in environment.activeProfiles -> emptyList()
			"dev" in environment.activeProfiles -> emptyList()
			else ->
				listOf(
					"http://localhost:3000",
					"http://localhost:3001",
					"http://127.0.0.1:3000",
					"http://127.0.0.1:3001"
				)
		}
	}

	private fun createCorsConfiguration(): CorsConfiguration =
		CorsConfiguration().apply {
			allowedOriginPatterns = corsAllowedOrigins
			allowedMethods = listOf("HEAD", "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
			allowedHeaders = listOf("Authorization", "Cache-Control", "Content-Type")
			allowCredentials = true
			maxAge = 3600L
		}
}
