package com.example.demo.security.filter

import com.example.demo.auth.exception.APIKeyNotFoundException
import com.example.demo.persistence.auth.provider.AuthProvider
import com.example.demo.security.component.SecurityErrorResponseWriter
import io.micrometer.common.lang.NonNull
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.filter.OncePerRequestFilter

class APIKeyAuthFilter(
	private val authProvider: AuthProvider,
	private val securityErrorResponseWriter: SecurityErrorResponseWriter
) : OncePerRequestFilter() {
	override fun doFilterInternal(
		@NonNull httpServletRequest: HttpServletRequest,
		@NonNull httpServletResponse: HttpServletResponse,
		@NonNull filterChain: FilterChain
	) {
		runCatching {
			val requestAPIKey = httpServletRequest.getHeader("X-API-KEY")

			requestAPIKey?.let {
				if (!authProvider.validateApiKey(it)) {
					throw APIKeyNotFoundException(httpServletRequest.requestURI)
				}
			} ?: throw APIKeyNotFoundException(httpServletRequest.requestURI)
		}.onSuccess { filterChain.doFilter(httpServletRequest, httpServletResponse) }
			.onFailure {
				securityErrorResponseWriter.writeErrorResponse(
					httpServletRequest,
					httpServletResponse,
					it
				)
			}
	}
}
