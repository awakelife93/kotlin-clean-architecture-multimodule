package com.example.demo.common.aop

import com.example.demo.notification.NotificationService
import com.example.demo.notification.annotation.NotifyOnRequest
import org.springframework.core.MethodParameter
import org.springframework.http.HttpInputMessage
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter
import java.lang.reflect.Type

@ControllerAdvice
class NotifyOnRequestBodyAdvice(
	private val notificationService: NotificationService
) : RequestBodyAdviceAdapter() {
	override fun supports(
		methodParameter: MethodParameter,
		targetType: Type,
		converterType: Class<out HttpMessageConverter<*>>
	): Boolean = methodParameter.hasParameterAnnotation(NotifyOnRequest::class.java)

	override fun afterBodyRead(
		body: Any,
		inputMessage: HttpInputMessage,
		parameter: MethodParameter,
		targetType: Type,
		converterType: Class<out HttpMessageConverter<*>>
	): Any {
		notificationService.sendNotification(
			"Request received: ${parameter.method?.name}",
			listOf("Request Body: $body")
		)

		return super.afterBodyRead(body, inputMessage, parameter, targetType, converterType)
	}
}
