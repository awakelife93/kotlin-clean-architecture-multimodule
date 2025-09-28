package com.example.demo.common.utils

import jakarta.servlet.http.HttpServletRequest

object HttpUtils {
	private val PROXY_HEADERS =
		listOf(
			"X-Forwarded-For",
			"X-Real-IP",
			"CF-Connecting-IP",
			"X-Original-Forwarded-For",
			"Proxy-Client-IP",
			"WL-Proxy-Client-IP",
			"HTTP_X_FORWARDED_FOR",
			"HTTP_X_FORWARDED",
			"HTTP_X_CLUSTER_CLIENT_IP",
			"HTTP_CLIENT_IP",
			"HTTP_FORWARDED_FOR",
			"HTTP_FORWARDED"
		)

	private val INVALID_IP_VALUES =
		setOf(
			"unknown",
			"localhost",
			"127.0.0.1",
			"0:0:0:0:0:0:0:1",
			"::1"
		)

	fun getClientIp(request: HttpServletRequest): String = getIpFromProxyHeaders(request) ?: request.remoteAddr.takeIf(::isValidIp) ?: "unknown"

	private fun getIpFromProxyHeaders(request: HttpServletRequest): String? =
		PROXY_HEADERS.firstNotNullOfOrNull { header ->
			request.getHeader(header)?.let { extractFirstIp(it) }?.takeIf(::isValidIp)
		}

	private fun extractFirstIp(headerValue: String): String = headerValue.substringBefore(",").trim()

	private fun isValidIp(ip: String?): Boolean = ip?.isNotBlank() == true && ip.lowercase() !in INVALID_IP_VALUES

	fun getUserAgent(
		request: HttpServletRequest,
		maxLength: Int? = null
	): String =
		request.getHeader("User-Agent")?.let { ua ->
			maxLength?.let { if (ua.length > it) ua.take(it) + "..." else ua } ?: ua
		} ?: "unknown"

	fun getFullUrl(request: HttpServletRequest): String =
		buildString {
			append(request.requestURL)
			request.queryString?.let { append("?$it") }
		}

	fun isAjaxRequest(request: HttpServletRequest): Boolean = request.getHeader("X-Requested-With") == "XMLHttpRequest"

	fun isJsonRequest(request: HttpServletRequest): Boolean = request.contentType?.contains("application/json", ignoreCase = true) == true

	fun getReferer(request: HttpServletRequest): String? = request.getHeader("Referer")

	fun isProxiedRequest(request: HttpServletRequest): Boolean = PROXY_HEADERS.any { request.getHeader(it) != null }

	fun getProxyHeaders(request: HttpServletRequest): Map<String, String> =
		PROXY_HEADERS
			.mapNotNull { header ->
				request.getHeader(header)?.let { header to it }
			}.toMap()
}
