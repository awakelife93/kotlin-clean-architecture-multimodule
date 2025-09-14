package com.example.demo.kotest.common.interceptor

import com.example.demo.common.interceptor.LoggingForInterceptor
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.ModelAndView
import java.util.UUID

@ActiveProfiles("test")
@Tags("kotest-unit-test")
class LoggingForInterceptorTests :
	BehaviorSpec({

		val loggingForInterceptor = LoggingForInterceptor()

		afterTest {
			MDC.clear()
		}

		Given("LoggingForInterceptor preHandle") {

			When("Request with X-Request-Id header") {
				val request = mockk<HttpServletRequest>(relaxed = true)
				val response = mockk<HttpServletResponse>(relaxed = true)
				val handlerMethod = mockk<HandlerMethod>(relaxed = true)

				val requestId = "test-request-id"
				val method = "GET"
				val requestUri = "/api/v1/users"
				val clientIp = "192.168.1.1"
				val userAgent = "Mozilla/5.0"

				every { request.getHeader("X-Request-Id") } returns requestId
				every { request.method } returns method
				every { request.requestURI } returns requestUri
				every { request.queryString } returns null
				every { request.getHeader("X-Forwarded-For") } returns null
				every { request.getHeader("X-Real-IP") } returns null
				every { request.remoteAddr } returns clientIp
				every { request.getHeader("User-Agent") } returns userAgent
				every { handlerMethod.beanType } returns this::class.java
				every { handlerMethod.method.name } returns "testMethod"

				val result = loggingForInterceptor.preHandle(request, response, handlerMethod)

				Then("Should set MDC values and return true") {
					result shouldBe true
					MDC.get("requestId") shouldBe requestId
					MDC.get("method") shouldBe method
					MDC.get("uri") shouldBe requestUri
					MDC.get("clientIp") shouldBe clientIp
					MDC.get("userAgent") shouldNotBe null

					verify { response.setHeader("X-Request-Id", requestId) }
					verify { request.setAttribute("startTime", any<Long>()) }
				}
			}

			When("Request without X-Request-Id header") {
				val request = mockk<HttpServletRequest>(relaxed = true)
				val response = mockk<HttpServletResponse>(relaxed = true)
				val handlerMethod = mockk<HandlerMethod>(relaxed = true)

				mockkStatic(UUID::class)
				val generatedUuid = UUID.randomUUID()
				every { UUID.randomUUID() } returns generatedUuid
				every { request.getHeader("X-Request-Id") } returns null
				every { request.method } returns "POST"
				every { request.requestURI } returns "/api/v1/posts"
				every { request.queryString } returns "page=1&size=10"
				every { request.getHeader("X-Forwarded-For") } returns "10.0.0.1, 192.168.1.1"
				every { request.getHeader("User-Agent") } returns "TestAgent/1.0"
				every { request.remoteAddr } returns "127.0.0.1"
				every { handlerMethod.beanType } returns this::class.java
				every { handlerMethod.method.name } returns "testMethod"

				val result = loggingForInterceptor.preHandle(request, response, handlerMethod)

				Then("Should generate requestId and set MDC values") {
					result shouldBe true
					MDC.get("requestId") shouldNotBe null
					MDC.get("requestId").length shouldBe 8
					MDC.get("method") shouldBe "POST"
					MDC.get("uri") shouldBe "/api/v1/posts?page=1&size=10"
					MDC.get("clientIp") shouldBe "10.0.0.1"

					verify { response.setHeader("X-Request-Id", any<String>()) }
				}
			}

			When("Handler is not HandlerMethod") {
				val request = mockk<HttpServletRequest>(relaxed = true)
				val response = mockk<HttpServletResponse>(relaxed = true)
				val simpleHandler = Any()

				every { request.getHeader("X-Request-Id") } returns "test-id"
				every { request.method } returns "GET"
				every { request.requestURI } returns "/test"
				every { request.queryString } returns null
				every { request.getHeader("X-Forwarded-For") } returns null
				every { request.getHeader("X-Real-IP") } returns null
				every { request.remoteAddr } returns "127.0.0.1"
				every { request.getHeader("User-Agent") } returns "Test"

				val result = loggingForInterceptor.preHandle(request, response, simpleHandler)

				Then("Should handle non-HandlerMethod handlers") {
					result shouldBe true
					val handlerValue = MDC.get("handler")
					handlerValue shouldBe "Object"
				}
			}

			When("HandlerMethod with Any class as beanType") {
				val request = mockk<HttpServletRequest>(relaxed = true)
				val response = mockk<HttpServletResponse>(relaxed = true)
				val handlerMethod = mockk<HandlerMethod>(relaxed = true)

				every { request.getHeader("X-Request-Id") } returns "test-id"
				every { request.method } returns "GET"
				every { request.requestURI } returns "/test"
				every { request.queryString } returns null
				every { request.getHeader("X-Forwarded-For") } returns null
				every { request.getHeader("X-Real-IP") } returns null
				every { request.remoteAddr } returns "127.0.0.1"
				every { request.getHeader("User-Agent") } returns "Test"
				every { handlerMethod.beanType } returns Any::class.java
				every { handlerMethod.method.name } returns "testMethod"

				val result = loggingForInterceptor.preHandle(request, response, handlerMethod)

				Then("Should handle Any class beanType gracefully") {
					result shouldBe true
					val handlerValue = MDC.get("handler")
					handlerValue shouldBe "UnknownController.testMethod"
				}
			}
		}

		Given("LoggingForInterceptor postHandle") {

			When("Normal request (fast response)") {
				val request = mockk<HttpServletRequest>(relaxed = true)
				val response = mockk<HttpServletResponse>(relaxed = true)
				val handlerMethod = mockk<HandlerMethod>(relaxed = true)
				val modelAndView = mockk<ModelAndView>(relaxed = true)

				val startTime = System.currentTimeMillis() - 100
				every { request.getAttribute("startTime") } returns startTime
				every { response.status } returns 200

				loggingForInterceptor.postHandle(request, response, handlerMethod, modelAndView)

				Then("Should set status and duration in MDC") {
					MDC.get("status") shouldBe "200"
					MDC.get("duration") shouldNotBe null
					MDC.get("duration") shouldContain "ms"
				}
			}

			When("Slow API request (over 1000ms)") {
				val request = mockk<HttpServletRequest>(relaxed = true)
				val response = mockk<HttpServletResponse>(relaxed = true)
				val handlerMethod = mockk<HandlerMethod>(relaxed = true)
				val modelAndView = mockk<ModelAndView>(relaxed = true)

				val startTime = System.currentTimeMillis() - 1500
				every { request.getAttribute("startTime") } returns startTime
				every { response.status } returns 200

				loggingForInterceptor.postHandle(request, response, handlerMethod, modelAndView)

				Then("Should log as slow API") {
					MDC.get("status") shouldBe "200"
					val duration = MDC.get("duration")
					duration shouldNotBe null
					val durationValue = duration?.replace("ms", "")?.toIntOrNull()
					durationValue shouldNotBe null
					(durationValue!! >= 1000) shouldBe true
				}
			}

			When("Error response (4xx or 5xx)") {
				val request = mockk<HttpServletRequest>(relaxed = true)
				val response = mockk<HttpServletResponse>(relaxed = true)
				val handlerMethod = mockk<HandlerMethod>(relaxed = true)
				val modelAndView = mockk<ModelAndView>(relaxed = true)

				val startTime = System.currentTimeMillis() - 50
				every { request.getAttribute("startTime") } returns startTime
				every { response.status } returns 404

				loggingForInterceptor.postHandle(request, response, handlerMethod, modelAndView)

				Then("Should log error status") {
					MDC.get("status") shouldBe "404"
					MDC.get("duration") shouldNotBe null
				}
			}

			When("Start time is null") {
				val request = mockk<HttpServletRequest>(relaxed = true)
				val response = mockk<HttpServletResponse>(relaxed = true)
				val handlerMethod = mockk<HandlerMethod>(relaxed = true)
				val modelAndView = mockk<ModelAndView>(relaxed = true)

				every { request.getAttribute("startTime") } returns null
				every { response.status } returns 200

				loggingForInterceptor.postHandle(request, response, handlerMethod, modelAndView)

				Then("Should handle null start time gracefully") {
					MDC.get("status") shouldBe "200"
					MDC.get("duration") shouldNotBe null
				}
			}
		}

		Given("LoggingForInterceptor afterCompletion") {

			When("Request completes without exception") {
				val request = mockk<HttpServletRequest>(relaxed = true)
				val response = mockk<HttpServletResponse>(relaxed = true)
				val handlerMethod = mockk<HandlerMethod>(relaxed = true)

				MDC.put("requestId", "test-id")
				MDC.put("method", "GET")

				loggingForInterceptor.afterCompletion(request, response, handlerMethod, null)

				Then("Should clear MDC") {
					MDC.get("requestId") shouldBe null
					MDC.get("method") shouldBe null
					MDC.get("error") shouldBe null
				}
			}

			When("Request completes with exception") {
				val request = mockk<HttpServletRequest>(relaxed = true)
				val response = mockk<HttpServletResponse>(relaxed = true)
				val handlerMethod = mockk<HandlerMethod>(relaxed = true)

				val exception = RuntimeException("Test exception")
				MDC.put("requestId", "test-id")

				loggingForInterceptor.afterCompletion(request, response, handlerMethod, exception)

				Then("Should log exception and clear MDC") {
					MDC.get("requestId") shouldBe null
					MDC.get("error") shouldBe null
				}
			}

			When("Request completes with exception without message") {
				val request = mockk<HttpServletRequest>(relaxed = true)
				val response = mockk<HttpServletResponse>(relaxed = true)
				val handlerMethod = mockk<HandlerMethod>(relaxed = true)

				val exception = RuntimeException()
				MDC.put("requestId", "test-id")

				loggingForInterceptor.afterCompletion(request, response, handlerMethod, exception)

				Then("Should handle exception without message and clear MDC") {
					MDC.get("requestId") shouldBe null
					MDC.get("error") shouldBe null
				}
			}
		}
	})
