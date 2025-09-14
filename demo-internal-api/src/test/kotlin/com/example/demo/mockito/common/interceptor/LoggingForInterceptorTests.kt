package com.example.demo.mockito.common.interceptor

import com.example.demo.common.interceptor.LoggingForInterceptor
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.mockito.quality.Strictness
import org.slf4j.MDC
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.ModelAndView

@ActiveProfiles("test")
@Tag("mockito-unit-test")
@DisplayName("Mockito Unit - LoggingForInterceptor Test")
@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LoggingForInterceptorTests {
	@InjectMocks
	private lateinit var loggingForInterceptor: LoggingForInterceptor

	@Mock
	private lateinit var request: HttpServletRequest

	@Mock
	private lateinit var response: HttpServletResponse

	@Mock
	private lateinit var handlerMethod: HandlerMethod

	@Mock
	private lateinit var modelAndView: ModelAndView

	@AfterEach
	fun tearDown() {
		MDC.clear()
	}

	private fun stubAllProxyHeaders(
		xForwardedFor: String? = null,
		xRealIp: String? = null,
		cfConnectingIp: String? = null
	) {
		whenever(request.getHeader("X-Forwarded-For")).thenReturn(xForwardedFor)
		whenever(request.getHeader("X-Real-IP")).thenReturn(xRealIp)
		whenever(request.getHeader("CF-Connecting-IP")).thenReturn(cfConnectingIp)
		whenever(request.getHeader("X-Original-Forwarded-For")).thenReturn(null)
		whenever(request.getHeader("Proxy-Client-IP")).thenReturn(null)
		whenever(request.getHeader("WL-Proxy-Client-IP")).thenReturn(null)
		whenever(request.getHeader("HTTP_X_FORWARDED_FOR")).thenReturn(null)
		whenever(request.getHeader("HTTP_X_FORWARDED")).thenReturn(null)
		whenever(request.getHeader("HTTP_X_CLUSTER_CLIENT_IP")).thenReturn(null)
		whenever(request.getHeader("HTTP_CLIENT_IP")).thenReturn(null)
		whenever(request.getHeader("HTTP_FORWARDED_FOR")).thenReturn(null)
		whenever(request.getHeader("HTTP_FORWARDED")).thenReturn(null)
	}

	@Nested
	@DisplayName("PreHandle Test")
	inner class PreHandleTest {
		@Test
		@DisplayName("Should set MDC values with existing request ID header")
		fun should_SetMDCValuesWithExistingRequestId_when_RequestHasXRequestIdHeader() {
			val requestId = "test-request-id"
			val method = "GET"
			val requestUri = "/api/v1/users"
			val clientIp = "192.168.1.1"
			val userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64)"

			whenever(request.getHeader("X-Request-Id")).thenReturn(requestId)
			stubAllProxyHeaders()
			whenever(request.getHeader("User-Agent")).thenReturn(userAgent)

			whenever(request.method).thenReturn(method)
			whenever(request.requestURI).thenReturn(requestUri)
			whenever(request.queryString).thenReturn(null)
			whenever(request.remoteAddr).thenReturn(clientIp)

			whenever(handlerMethod.beanType).thenReturn(this::class.java)
			whenever(handlerMethod.method).thenReturn(this::class.java.methods[0])

			val result = loggingForInterceptor.preHandle(request, response, handlerMethod)

			assertTrue(result)
			assertEquals(requestId, MDC.get("requestId"))
			assertEquals(method, MDC.get("method"))
			assertEquals(requestUri, MDC.get("uri"))
			assertEquals(clientIp, MDC.get("clientIp"))
			assertNotNull(MDC.get("userAgent"))
			assertNotNull(MDC.get("handler"))

			verify(response).setHeader("X-Request-Id", requestId)
			verify(request).setAttribute(eq("startTime"), argumentCaptor<Long>().capture())
		}

		@Test
		@DisplayName("Should generate request ID when header is missing")
		fun should_GenerateRequestId_when_RequestDoesNotHaveXRequestIdHeader() {
			val method = "POST"
			val requestUri = "/api/v1/posts"
			val queryString = "page=1&size=10"
			val clientIp = "10.0.0.1"
			val userAgent = "TestAgent/1.0"

			whenever(request.getHeader("X-Request-Id")).thenReturn(null)
			stubAllProxyHeaders(xForwardedFor = "$clientIp, 192.168.1.1")
			whenever(request.getHeader("User-Agent")).thenReturn(userAgent)

			whenever(request.method).thenReturn(method)
			whenever(request.requestURI).thenReturn(requestUri)
			whenever(request.queryString).thenReturn(queryString)

			whenever(handlerMethod.beanType).thenReturn(this::class.java)
			whenever(handlerMethod.method).thenReturn(this::class.java.methods[0])

			val result = loggingForInterceptor.preHandle(request, response, handlerMethod)

			assertTrue(result)
			assertNotNull(MDC.get("requestId"))
			assertEquals(8, MDC.get("requestId").length)
			assertEquals(method, MDC.get("method"))
			assertEquals("$requestUri?$queryString", MDC.get("uri"))
			assertEquals(clientIp, MDC.get("clientIp"))

			verify(response).setHeader(eq("X-Request-Id"), any<String>())
			verify(request).setAttribute(eq("startTime"), argumentCaptor<Long>().capture())
		}

		@Test
		@DisplayName("Should handle non-HandlerMethod handler")
		fun should_HandleNonHandlerMethod_when_HandlerIsNotHandlerMethod() {
			val simpleHandler = Any()
			val requestId = "test-id"

			whenever(request.getHeader("X-Request-Id")).thenReturn(requestId)
			stubAllProxyHeaders()
			whenever(request.getHeader("User-Agent")).thenReturn("Test")

			whenever(request.method).thenReturn("GET")
			whenever(request.requestURI).thenReturn("/test")
			whenever(request.queryString).thenReturn(null)
			whenever(request.remoteAddr).thenReturn("127.0.0.1")

			val result = loggingForInterceptor.preHandle(request, response, simpleHandler)

			assertTrue(result)
			assertEquals("Object", MDC.get("handler"))
		}

		@Test
		@DisplayName("Should use X-Real-IP when X-Forwarded-For is not available")
		fun should_UseXRealIP_when_XForwardedForIsNotAvailable() {
			val realIp = "203.0.113.1"

			whenever(request.getHeader("X-Request-Id")).thenReturn("test-id")
			stubAllProxyHeaders(xRealIp = realIp)
			whenever(request.getHeader("User-Agent")).thenReturn("Test")

			whenever(request.method).thenReturn("GET")
			whenever(request.requestURI).thenReturn("/api/test")
			whenever(request.queryString).thenReturn(null)

			whenever(handlerMethod.beanType).thenReturn(this::class.java)
			whenever(handlerMethod.method).thenReturn(this::class.java.methods[0])

			val result = loggingForInterceptor.preHandle(request, response, handlerMethod)

			assertTrue(result)
			assertEquals(realIp, MDC.get("clientIp"))
		}

		@Test
		@DisplayName("Should handle HandlerMethod with Any class as beanType")
		fun should_HandleAnyClassBeanType_when_HandlerMethodHasAnyClass() {
			val requestId = "test-id"

			whenever(request.getHeader("X-Request-Id")).thenReturn(requestId)
			stubAllProxyHeaders()
			whenever(request.getHeader("User-Agent")).thenReturn("Test")

			whenever(request.method).thenReturn("GET")
			whenever(request.requestURI).thenReturn("/test")
			whenever(request.queryString).thenReturn(null)
			whenever(request.remoteAddr).thenReturn("127.0.0.1")

			whenever(handlerMethod.beanType).thenReturn(Any::class.java)
			whenever(handlerMethod.method).thenReturn(this::class.java.methods[0])

			val result = loggingForInterceptor.preHandle(request, response, handlerMethod)

			assertTrue(result)
			val handlerValue = MDC.get("handler")
			assertTrue(handlerValue.startsWith("UnknownController."))
		}
	}

	@Nested
	@DisplayName("PostHandle Test")
	inner class PostHandleTest {
		@Test
		@DisplayName("Should log normal request with fast response")
		fun should_LogNormalRequest_when_ResponseIsFast() {
			val startTime = System.currentTimeMillis() - 100
			whenever(request.getAttribute("startTime")).thenReturn(startTime)
			whenever(response.status).thenReturn(200)

			loggingForInterceptor.postHandle(request, response, handlerMethod, modelAndView)

			assertEquals("200", MDC.get("status"))
			assertNotNull(MDC.get("duration"))
			assertTrue(MDC.get("duration").contains("ms"))
		}

		@Test
		@DisplayName("Should log slow API warning when response is slow")
		fun should_LogSlowAPIWarning_when_ResponseTimeExceedsThreshold() {
			val startTime = System.currentTimeMillis() - 1500
			whenever(request.getAttribute("startTime")).thenReturn(startTime)
			whenever(response.status).thenReturn(200)

			loggingForInterceptor.postHandle(request, response, handlerMethod, modelAndView)

			assertEquals("200", MDC.get("status"))
			val duration = MDC.get("duration")
			assertNotNull(duration)
			assertTrue(duration.contains("ms"))
			val durationMs = duration.replace("ms", "").toInt()
			assertTrue(durationMs >= 1000)
		}

		@Test
		@DisplayName("Should log error status for 4xx responses")
		fun should_LogErrorStatus_when_ResponseIs4xx() {
			val startTime = System.currentTimeMillis() - 50
			whenever(request.getAttribute("startTime")).thenReturn(startTime)
			whenever(response.status).thenReturn(404)

			loggingForInterceptor.postHandle(request, response, handlerMethod, modelAndView)

			assertEquals("404", MDC.get("status"))
			assertNotNull(MDC.get("duration"))
		}

		@Test
		@DisplayName("Should log error status for 5xx responses")
		fun should_LogErrorStatus_when_ResponseIs5xx() {
			val startTime = System.currentTimeMillis() - 75
			whenever(request.getAttribute("startTime")).thenReturn(startTime)
			whenever(response.status).thenReturn(500)

			loggingForInterceptor.postHandle(request, response, handlerMethod, modelAndView)

			assertEquals("500", MDC.get("status"))
			assertNotNull(MDC.get("duration"))
		}

		@Test
		@DisplayName("Should handle null start time gracefully")
		fun should_HandleNullStartTime_when_StartTimeAttributeIsNull() {
			whenever(request.getAttribute("startTime")).thenReturn(null)
			whenever(response.status).thenReturn(200)

			loggingForInterceptor.postHandle(request, response, handlerMethod, modelAndView)

			assertEquals("200", MDC.get("status"))
			assertNotNull(MDC.get("duration"))
			assertTrue(MDC.get("duration").contains("ms"))
		}
	}

	@Nested
	@DisplayName("AfterCompletion Test")
	inner class AfterCompletionTest {
		@Test
		@DisplayName("Should clear MDC after successful completion")
		fun should_ClearMDC_when_RequestCompletesSuccessfully() {
			MDC.put("requestId", "test-id")
			MDC.put("method", "GET")
			MDC.put("uri", "/test")

			loggingForInterceptor.afterCompletion(request, response, handlerMethod, null)

			assertNull(MDC.get("requestId"))
			assertNull(MDC.get("method"))
			assertNull(MDC.get("uri"))
			assertNull(MDC.get("error"))
		}

		@Test
		@DisplayName("Should log exception and clear MDC when exception occurs")
		fun should_LogExceptionAndClearMDC_when_ExceptionOccurs() {
			val exception = RuntimeException("Test exception message")
			MDC.put("requestId", "test-id")
			MDC.put("method", "POST")

			loggingForInterceptor.afterCompletion(request, response, handlerMethod, exception)

			assertNull(MDC.get("requestId"))
			assertNull(MDC.get("method"))
			assertNull(MDC.get("error"))
		}

		@Test
		@DisplayName("Should handle exception without message")
		fun should_HandleExceptionWithoutMessage_when_ExceptionHasNoMessage() {
			val exception = RuntimeException()
			MDC.put("requestId", "test-id")

			loggingForInterceptor.afterCompletion(request, response, handlerMethod, exception)

			assertNull(MDC.get("requestId"))
			assertNull(MDC.get("error"))
		}

		@Test
		@DisplayName("Should always clear MDC even when unexpected error occurs")
		fun should_AlwaysClearMDC_when_UnexpectedErrorOccurs() {
			val exception = NullPointerException("Unexpected error")
			MDC.put("requestId", "test-id")
			MDC.put("status", "500")

			loggingForInterceptor.afterCompletion(request, response, handlerMethod, exception)

			assertNull(MDC.get("requestId"))
			assertNull(MDC.get("status"))
			assertNull(MDC.get("error"))
		}
	}
}
