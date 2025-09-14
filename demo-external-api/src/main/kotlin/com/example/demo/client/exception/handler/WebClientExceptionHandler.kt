package com.example.demo.client.exception.handler

import com.example.demo.client.exception.ExternalApiException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import reactor.core.publisher.Mono

private val logger = KotlinLogging.logger {}

@Component
class WebClientExceptionHandler {
	fun errorHandlingFilter(): ExchangeFilterFunction =
		ExchangeFilterFunction.ofResponseProcessor { response ->
			if (response.statusCode().isError) {
				handleErrorResponse(response)
			} else {
				Mono.just(response)
			}
		}

	private fun handleErrorResponse(response: ClientResponse): Mono<ClientResponse> =
		response
			.bodyToMono(String::class.java)
			.defaultIfEmpty("No error message")
			.flatMap { errorBody ->
				logger.error {
					"External API error - Status: ${response.statusCode()}, Body: $errorBody"
				}

				val exception =
					when {
						response.statusCode().is4xxClientError ->
							ExternalApiException("Client error: ${response.statusCode()} - $errorBody")
						response.statusCode().is5xxServerError ->
							ExternalApiException("Server error: ${response.statusCode()} - $errorBody")
						else ->
							ExternalApiException("Unexpected error: ${response.statusCode()} - $errorBody")
					}

				Mono.error(exception)
			}
}
