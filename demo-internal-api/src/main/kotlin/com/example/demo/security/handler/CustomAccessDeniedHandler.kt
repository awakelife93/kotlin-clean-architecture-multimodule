package com.example.demo.security.handler

import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerExceptionResolver
import java.io.IOException

@Component
class CustomAccessDeniedHandler(
	@Qualifier("handlerExceptionResolver")
	private val resolver: HandlerExceptionResolver
) : AccessDeniedHandler {
	@Throws(ServletException::class, IOException::class)
	override fun handle(
		request: HttpServletRequest,
		response: HttpServletResponse,
		accessDeniedException: AccessDeniedException
	) {
		resolver.resolveException(request, response, null, accessDeniedException)
	}
}
