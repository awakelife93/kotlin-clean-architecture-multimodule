package com.example.demo.common.aop

import com.example.demo.common.response.ErrorResponse
import com.example.demo.common.response.OkResponse
import org.springframework.core.MethodParameter
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.lang.Nullable
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice

@RestControllerAdvice(basePackages = ["com.example"])
class ResponseAdvice : ResponseBodyAdvice<Any> {
	override fun supports(
		returnType: MethodParameter,
		converterType: Class<out HttpMessageConverter<*>>
	): Boolean =
		MappingJackson2HttpMessageConverter::class.java.isAssignableFrom(
			converterType
		)

	override fun beforeBodyWrite(
		@Nullable body: Any?,
		returnType: MethodParameter,
		selectedContentType: MediaType,
		selectedConverterType: Class<out HttpMessageConverter<*>>,
		request: ServerHttpRequest,
		response: ServerHttpResponse
	): Any? =
		when (body) {
			is ErrorResponse -> body
			else -> body.let { OkResponse.of(HttpStatus.OK.value(), HttpStatus.OK.name, it) }
		}
}
