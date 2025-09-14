package com.example.demo.mockito.common.utils

import com.example.demo.security.utils.SecurityUtils.sendErrorResponse
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
@DisplayName("Mockito Unit - Security Utils Test")
@ExtendWith(
	MockitoExtension::class
)
class SecurityUtilsTests {
	@Mock
	private lateinit var mockHttpServletRequest: MockHttpServletRequest

	@Mock
	private lateinit var mockHttpServletResponse: MockHttpServletResponse

	@Test
	@DisplayName("Send Error Response Test")
	@Throws(Exception::class)
	fun should_VerifyCallMethodsOfHttpServletResponse_when_GivenServletAndException() {
		val exception = RuntimeException("Test exception message")
		val stringWriter = StringWriter()
		val printWriter = PrintWriter(stringWriter)

		whenever(mockHttpServletResponse.writer).thenReturn(printWriter)

		sendErrorResponse(
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
