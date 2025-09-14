package com.example.demo.common.interceptor

import com.example.demo.common.utils.HttpUtils
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.ModelAndView
import java.util.UUID

private val logger = KotlinLogging.logger {}

@Component
class LoggingForInterceptor : HandlerInterceptor {
	override fun preHandle(
		request: HttpServletRequest,
		response: HttpServletResponse,
		handler: Any
	): Boolean {
		val context = RequestContext.from(request, handler)

		request.setAttribute(START_TIME_ATTRIBUTE, System.currentTimeMillis())
		response.setHeader(REQUEST_ID_HEADER, context.requestId)

		context.setToMDC()

		return true
	}

	override fun postHandle(
		request: HttpServletRequest,
		response: HttpServletResponse,
		handler: Any,
		modelAndView: ModelAndView?
	) {
		val duration = calculateDuration(request)

		MDC.put("status", response.status.toString())
		MDC.put("duration", "${duration}ms")

		logRequestCompletion(response.status, duration)
	}

	override fun afterCompletion(
		request: HttpServletRequest,
		response: HttpServletResponse,
		handler: Any,
		exception: Exception?
	) {
		try {
			exception?.let {
				MDC.put("error", it.message ?: "Unknown error")
				logger.error(it) { "Request failed with exception" }
			}
		} finally {
			MDC.clear()
		}
	}

	private fun calculateDuration(request: HttpServletRequest): Long {
		val startTime =
			request.getAttribute(START_TIME_ATTRIBUTE) as? Long
				?: System.currentTimeMillis()
		return System.currentTimeMillis() - startTime
	}

	private fun logRequestCompletion(
		status: Int,
		duration: Long
	) {
		when {
			duration > SLOW_API_THRESHOLD_MS ->
				logger.warn { "Slow API detected: ${duration}ms" }

			status >= 400 ->
				logger.warn { "Request failed with status: $status" }

			else ->
				logger.info { "Request completed" }
		}
	}

	companion object {
		private const val REQUEST_ID_HEADER = "X-Request-Id"
		private const val START_TIME_ATTRIBUTE = "startTime"
		private const val SLOW_API_THRESHOLD_MS = 1000L
	}
}

private data class RequestContext(
	val requestId: String,
	val method: String,
	val uri: String,
	val clientIp: String,
	val userAgent: String,
	val handler: String
) {
	fun setToMDC() {
		MDC.put("requestId", requestId)
		MDC.put("method", method)
		MDC.put("uri", uri)
		MDC.put("clientIp", clientIp)
		MDC.put("userAgent", userAgent)
		MDC.put("handler", handler)
	}

	companion object {
		fun from(
			request: HttpServletRequest,
			handler: Any
		): RequestContext {
			fun extractRequestId(request: HttpServletRequest): String = request.getHeader("X-Request-Id") ?: UUID.randomUUID().toString().take(8)

			fun fullUri(request: HttpServletRequest): String = request.requestURI + (request.queryString?.let { "?$it" } ?: "")

			fun extractHandlerInfo(handler: Any): String =
				when (handler) {
					is HandlerMethod -> {
						val className =
							if (handler.beanType == Any::class.java) {
								"UnknownController"
							} else {
								handler.beanType.simpleName
							}
						"$className.${handler.method.name}"
					}

					else -> handler.javaClass.simpleName
				}

			return RequestContext(
				requestId = extractRequestId(request),
				method = request.method,
				uri = fullUri(request),
				clientIp = HttpUtils.getClientIp(request),
				userAgent = HttpUtils.getUserAgent(request, 50),
				handler = extractHandlerInfo(handler)
			)
		}
	}
}
