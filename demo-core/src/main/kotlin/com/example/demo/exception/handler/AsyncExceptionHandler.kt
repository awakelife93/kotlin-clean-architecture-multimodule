package com.example.demo.exception.handler

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler
import java.lang.reflect.Method

private val logger = KotlinLogging.logger {}

class AsyncExceptionHandler : AsyncUncaughtExceptionHandler {
	override fun handleUncaughtException(
		ex: Throwable,
		method: Method,
		vararg params: Any?
	) {
		logger.error(ex) {
			"Async exception occurred - Method: ${method.name}, " +
				"Class: ${method.declaringClass.simpleName}, " +
				"Parameters: ${params.joinToString()}"
		}
	}
}
