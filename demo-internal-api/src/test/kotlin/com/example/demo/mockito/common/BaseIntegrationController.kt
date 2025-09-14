package com.example.demo.mockito.common

import com.example.demo.common.aop.NotifyOnRequestBodyAdvice
import com.example.demo.notification.NotificationService
import com.example.demo.persistence.auth.provider.AuthProvider
import com.example.demo.persistence.auth.provider.JWTProvider
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.web.context.WebApplicationContext

abstract class BaseIntegrationController {
	@Autowired
	protected lateinit var webApplicationContext: WebApplicationContext

	@Autowired
	protected lateinit var objectMapper: ObjectMapper

	@Autowired(required = false)
	protected lateinit var notifyOnRequestBodyAdvice: NotifyOnRequestBodyAdvice

	@MockitoBean
	protected lateinit var notificationService: NotificationService

	@MockitoBean
	protected lateinit var authProvider: AuthProvider

	@MockitoBean
	protected lateinit var jwtProvider: JWTProvider

	protected lateinit var mockMvc: MockMvc

	protected val commonStatus: Int = HttpStatus.OK.value()

	protected val commonMessage: String = HttpStatus.OK.name
}
