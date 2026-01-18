package com.example.demo.mockito.security.component

import com.example.demo.security.component.SecurityErrorResponseWriter
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.test.context.ActiveProfiles
import java.io.PrintWriter
import java.io.StringWriter

@ActiveProfiles("test")
@Tag("mockito-unit-test")
@DisplayName("Mockito Unit - Security Error Response Writer Test")
@ExtendWith(
	MockitoExtension::class
)
class SecurityErrorResponseWriterTests {
	@Mock
	private lateinit var mockHttpServletRequest: MockHttpServletRequest

	@Mock
	private lateinit var mockHttpServletResponse: MockHttpServletResponse

	private lateinit var securityErrorResponseWriter: SecurityErrorResponseWriter

	@BeforeEach
	fun setUp() {
		val objectMapper = ObjectMapper()
		securityErrorResponseWriter = SecurityErrorResponseWriter(objectMapper)
	}

	@Test
	@DisplayName("Write Error Response Test")
	@Throws(Exception::class)
	fun should_VerifyCallMethodsOfHttpServletResponse_when_GivenServletAndException() {
		val exception = RuntimeException("Test exception message")
		val stringWriter = StringWriter()
		val printWriter = PrintWriter(stringWriter)

		whenever(mockHttpServletResponse.writer).thenReturn(printWriter)

		securityErrorResponseWriter.writeErrorResponse(
			mockHttpServletRequest,
			mockHttpServletResponse,
			exception,
			"test exception"
		)

		verify(mockHttpServletResponse, times(1)).status = any<Int>()
		verify(mockHttpServletResponse, times(1)).contentType = any<String>()
		verify(mockHttpServletResponse, times(1)).writer
	}
}
