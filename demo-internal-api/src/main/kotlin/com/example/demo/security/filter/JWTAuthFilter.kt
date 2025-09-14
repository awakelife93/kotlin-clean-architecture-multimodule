package com.example.demo.security.filter

import com.example.demo.persistence.auth.provider.JWTProvider
import com.example.demo.security.utils.SecurityUtils
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.UnsupportedJwtException
import io.jsonwebtoken.security.SecurityException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter

class JWTAuthFilter(
	private val jwtProvider: JWTProvider
) : OncePerRequestFilter() {
	companion object {
		private const val BEARER_PREFIX = "Bearer "
		private const val TOKEN_EXPIRED_MESSAGE = "Token has expired"
		private const val INVALID_SIGNATURE_MESSAGE = "Invalid token signature"
		private const val MALFORMED_TOKEN_MESSAGE = "Malformed token"
		private const val UNSUPPORTED_TOKEN_MESSAGE = "Unsupported token"
		private const val INVALID_TOKEN_MESSAGE = "Invalid token"
	}

	override fun doFilterInternal(
		request: HttpServletRequest,
		response: HttpServletResponse,
		filterChain: FilterChain
	) {
		runCatching {
			extractBearerToken(request)?.let { token ->
				if (jwtProvider.validateToken(token)) {
					SecurityContextHolder.getContext().authentication =
						jwtProvider.getAuthentication(token)
				}
			}

			filterChain.doFilter(request, response)
		}.onFailure { exception ->
			handleAuthenticationException(exception, request, response)
		}
	}

	private fun extractBearerToken(request: HttpServletRequest): String? =
		request
			.getHeader(HttpHeaders.AUTHORIZATION)
			?.takeIf { it.startsWith(BEARER_PREFIX) }
			?.substring(BEARER_PREFIX.length)

	private fun handleAuthenticationException(
		exception: Throwable,
		request: HttpServletRequest,
		response: HttpServletResponse
	) {
		SecurityContextHolder.clearContext()

		val errorMessage =
			when (exception) {
				is ExpiredJwtException -> TOKEN_EXPIRED_MESSAGE
				is SecurityException -> INVALID_SIGNATURE_MESSAGE
				is MalformedJwtException -> MALFORMED_TOKEN_MESSAGE
				is UnsupportedJwtException -> UNSUPPORTED_TOKEN_MESSAGE
				is IllegalArgumentException -> exception.message ?: INVALID_TOKEN_MESSAGE
				else -> throw exception
			}

		SecurityUtils.sendErrorResponse(request, response, exception, errorMessage)
	}
}
