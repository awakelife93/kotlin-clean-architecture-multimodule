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

	fun getClientIp(request: HttpServletRequest): String {
		val proxyIp = getIpFromProxyHeaders(request)
		if (proxyIp != null) {
			return proxyIp
		}

		val remoteAddr = request.remoteAddr
		return if (isValidIp(remoteAddr)) {
			remoteAddr
		} else {
			"unknown"
		}
	}

	private fun getIpFromProxyHeaders(request: HttpServletRequest): String? {
		for (headerName in PROXY_HEADERS) {
			val headerValue = request.getHeader(headerName) ?: continue

			val ip = extractFirstIp(headerValue)

			if (isValidIp(ip)) {
				return ip
			}
		}
		return null
	}

	private fun extractFirstIp(headerValue: String): String =
		if (headerValue.contains(",")) {
			headerValue.split(",")[0].trim()
		} else {
			headerValue.trim()
		}

	private fun isValidIp(ip: String?): Boolean {
		if (ip.isNullOrBlank()) return false
		return !INVALID_IP_VALUES.contains(ip.lowercase())
	}

	fun getUserAgent(
		request: HttpServletRequest,
		maxLength: Int? = null
	): String {
		val userAgent = request.getHeader("User-Agent") ?: return "unknown"
		return if (maxLength != null && userAgent.length > maxLength) {
			userAgent.take(maxLength) + "..."
		} else {
			userAgent
		}
	}

	fun getFullUrl(request: HttpServletRequest): String =
		buildString {
			append(request.requestURL)
			request.queryString?.let { append("?$it") }
		}

	fun isAjaxRequest(request: HttpServletRequest): Boolean {
		val requestedWith = request.getHeader("X-Requested-With")
		return "XMLHttpRequest" == requestedWith
	}

	fun isJsonRequest(request: HttpServletRequest): Boolean {
		val contentType = request.contentType ?: return false
		return contentType.contains("application/json", ignoreCase = true)
	}

	fun getReferer(request: HttpServletRequest): String? = request.getHeader("Referer")

	fun isProxiedRequest(request: HttpServletRequest): Boolean =
		PROXY_HEADERS.any { headerName ->
			request.getHeader(headerName) != null
		}

	fun getProxyHeaders(request: HttpServletRequest): Map<String, String> =
		PROXY_HEADERS
			.mapNotNull { headerName ->
				request.getHeader(headerName)?.let { headerValue ->
					headerName to headerValue
				}
			}.toMap()
}
