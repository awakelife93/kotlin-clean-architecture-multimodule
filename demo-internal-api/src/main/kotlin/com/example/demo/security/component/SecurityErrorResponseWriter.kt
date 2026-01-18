package com.example.demo.security.component

import com.example.demo.common.response.ErrorResponse
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Component
class SecurityErrorResponseWriter(
	private val objectMapper: ObjectMapper
) {
	fun writeErrorResponse(
		httpServletRequest: HttpServletRequest,
		httpServletResponse: HttpServletResponse,
		exception: Throwable,
		message: String = ""
	) {
		val errorResponse =
			ErrorResponse.of(
				HttpStatus.UNAUTHORIZED.value(),
				exception.message ?: message
			)

		logger.error {
			"Security Filter Error - ${httpServletRequest.method} ${httpServletRequest.requestURI} ${exception.message ?: message}"
		}

		with(httpServletResponse) {
			status = HttpStatus.UNAUTHORIZED.value()
			contentType = MediaType.APPLICATION_JSON_VALUE

			runCatching { writer.write(objectMapper.writeValueAsString(errorResponse)) }
				.onFailure { logger.error { it.message } }
		}
	}
}
