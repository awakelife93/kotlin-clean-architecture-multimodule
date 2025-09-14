package com.example.demo.kotest.common

import com.example.demo.common.aop.NotifyOnRequestBodyAdvice
import com.example.demo.kotest.common.security.SecurityListenerFactory
import com.example.demo.notification.NotificationService
import com.example.demo.persistence.auth.provider.AuthProvider
import com.example.demo.persistence.auth.provider.JWTProvider
import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.BehaviorSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

abstract class BaseIntegrationController : BehaviorSpec() {
	@Autowired
	protected lateinit var webApplicationContext: WebApplicationContext

	protected lateinit var mockMvc: MockMvc

	@Autowired
	protected lateinit var objectMapper: ObjectMapper

	@Autowired(required = false)
	protected lateinit var notifyOnRequestBodyAdvice: NotifyOnRequestBodyAdvice

	@MockkBean(relaxed = true)
	protected lateinit var notificationService: NotificationService

	@MockkBean(relaxed = true)
	protected lateinit var authProvider: AuthProvider

	@MockkBean(relaxed = true)
	protected lateinit var jwtProvider: JWTProvider

	protected val commonStatus: Int = HttpStatus.OK.value()

	protected val commonMessage: String = HttpStatus.OK.name

	fun initialize() {
		listeners(SecurityListenerFactory())

		beforeTest {
			mockMvc =
				MockMvcBuilders
					.webAppContextSetup(webApplicationContext)
					.apply<DefaultMockMvcBuilder>(SecurityMockMvcConfigurers.springSecurity())
					.alwaysDo<DefaultMockMvcBuilder>(MockMvcResultHandlers.print())
					.build()
		}
	}
}
