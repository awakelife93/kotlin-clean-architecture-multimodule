package com.example.demo.client.config

import com.example.demo.client.exception.handler.WebClientExceptionHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WebClientConfig(
	private val webClientExceptionHandler: WebClientExceptionHandler
) {
	@Bean
	fun webClient(builder: WebClient.Builder): WebClient =
		builder
			.defaultHeaders { it.contentType = MediaType.APPLICATION_JSON }
			.filter(webClientExceptionHandler.errorHandlingFilter())
			.build()
}
