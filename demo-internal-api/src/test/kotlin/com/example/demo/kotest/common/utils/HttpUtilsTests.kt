package com.example.demo.kotest.common.utils

import com.example.demo.common.utils.HttpUtils
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.maps.shouldContainKey
import io.kotest.matchers.maps.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import jakarta.servlet.http.HttpServletRequest
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@Tags("kotest-unit-test")
class HttpUtilsTests :
	BehaviorSpec({

		Given("getClientIp") {
			val request = mockk<HttpServletRequest>()

			When("X-Forwarded-For header exists with multiple IPs") {
				every { request.getHeader(any<String>()) } returns null
				every { request.getHeader("X-Forwarded-For") } returns "203.0.113.195, 70.41.3.18, 150.172.238.178"
				every { request.remoteAddr } returns "150.172.238.178"

				val clientIp = HttpUtils.getClientIp(request)

				Then("should return first IP") {
					clientIp shouldBe "203.0.113.195"
				}
			}

			When("X-Real-IP header exists") {
				every { request.getHeader(any<String>()) } returns null
				every { request.getHeader("X-Forwarded-For") } returns null
				every { request.getHeader("X-Real-IP") } returns "198.51.100.42"
				every { request.remoteAddr } returns "10.0.0.1"

				val clientIp = HttpUtils.getClientIp(request)

				Then("should return X-Real-IP") {
					clientIp shouldBe "198.51.100.42"
				}
			}

			When("proxy header contains invalid value") {
				every { request.getHeader(any<String>()) } returns null
				every { request.getHeader("X-Forwarded-For") } returns "unknown"
				every { request.getHeader("X-Real-IP") } returns "192.168.1.100"
				every { request.remoteAddr } returns "10.0.0.1"

				val clientIp = HttpUtils.getClientIp(request)

				Then("should skip invalid and return valid IP") {
					clientIp shouldBe "192.168.1.100"
				}
			}

			When("no proxy headers exist") {
				every { request.getHeader(any<String>()) } returns null
				every { request.remoteAddr } returns "192.168.1.100"

				val clientIp = HttpUtils.getClientIp(request)

				Then("should return remoteAddr") {
					clientIp shouldBe "192.168.1.100"
				}
			}

			When("remoteAddr is localhost") {
				every { request.getHeader(any<String>()) } returns null
				every { request.remoteAddr } returns "127.0.0.1"

				val clientIp = HttpUtils.getClientIp(request)

				Then("should return unknown") {
					clientIp shouldBe "unknown"
				}
			}
		}

		Given("getUserAgent") {
			val request = mockk<HttpServletRequest>()
			val userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"

			When("User-Agent header exists") {
				every { request.getHeader("User-Agent") } returns userAgent

				val result = HttpUtils.getUserAgent(request)

				Then("should return full User-Agent") {
					result shouldBe userAgent
				}
			}

			When("maxLength is specified") {
				every { request.getHeader("User-Agent") } returns userAgent

				val result = HttpUtils.getUserAgent(request, 20)

				Then("should truncate User-Agent") {
					result shouldBe "Mozilla/5.0 (Windows..."
				}
			}

			When("User-Agent is null") {
				every { request.getHeader("User-Agent") } returns null

				val result = HttpUtils.getUserAgent(request)

				Then("should return unknown") {
					result shouldBe "unknown"
				}
			}
		}

		Given("getFullUrl") {
			val request = mockk<HttpServletRequest>()

			When("query string exists") {
				every { request.requestURL } returns StringBuffer("https://api.example.com/users")
				every { request.queryString } returns "page=1&size=10"

				val result = HttpUtils.getFullUrl(request)

				Then("should return URL with query parameters") {
					result shouldBe "https://api.example.com/users?page=1&size=10"
				}
			}

			When("no query string") {
				every { request.requestURL } returns StringBuffer("https://api.example.com/users")
				every { request.queryString } returns null

				val result = HttpUtils.getFullUrl(request)

				Then("should return URL only") {
					result shouldBe "https://api.example.com/users"
				}
			}
		}

		Given("isAjaxRequest") {
			val request = mockk<HttpServletRequest>()

			When("X-Requested-With is XMLHttpRequest") {
				every { request.getHeader("X-Requested-With") } returns "XMLHttpRequest"

				val result = HttpUtils.isAjaxRequest(request)

				Then("should return true") {
					result shouldBe true
				}
			}

			When("X-Requested-With is missing") {
				every { request.getHeader("X-Requested-With") } returns null

				val result = HttpUtils.isAjaxRequest(request)

				Then("should return false") {
					result shouldBe false
				}
			}
		}

		Given("isJsonRequest") {
			val request = mockk<HttpServletRequest>()

			When("Content-Type is application/json") {
				every { request.contentType } returns "application/json"

				val result = HttpUtils.isJsonRequest(request)

				Then("should return true") {
					result shouldBe true
				}
			}

			When("Content-Type includes charset") {
				every { request.contentType } returns "application/json;charset=UTF-8"

				val result = HttpUtils.isJsonRequest(request)

				Then("should return true") {
					result shouldBe true
				}
			}

			When("Content-Type is null") {
				every { request.contentType } returns null

				val result = HttpUtils.isJsonRequest(request)

				Then("should return false") {
					result shouldBe false
				}
			}
		}

		Given("getReferer") {
			val request = mockk<HttpServletRequest>()

			When("Referer header exists") {
				every { request.getHeader("Referer") } returns "https://example.com/page"

				val result = HttpUtils.getReferer(request)

				Then("should return Referer URL") {
					result shouldBe "https://example.com/page"
				}
			}

			When("Referer is null") {
				every { request.getHeader("Referer") } returns null

				val result = HttpUtils.getReferer(request)

				Then("should return null") {
					result.shouldBeNull()
				}
			}
		}

		Given("isProxiedRequest") {
			val request = mockk<HttpServletRequest>()

			When("proxy headers exist") {
				every { request.getHeader("X-Forwarded-For") } returns "192.168.1.1"

				val result = HttpUtils.isProxiedRequest(request)

				Then("should return true") {
					result shouldBe true
				}
			}

			When("no proxy headers") {
				every { request.getHeader(any<String>()) } returns null

				val result = HttpUtils.isProxiedRequest(request)

				Then("should return false") {
					result shouldBe false
				}
			}
		}

		Given("getProxyHeaders") {
			val request = mockk<HttpServletRequest>()

			When("multiple proxy headers exist") {
				every { request.getHeader(any<String>()) } returns null
				every { request.getHeader("X-Forwarded-For") } returns "192.168.1.1"
				every { request.getHeader("X-Real-IP") } returns "10.0.0.1"
				every { request.getHeader("CF-Connecting-IP") } returns null

				val result = HttpUtils.getProxyHeaders(request)

				Then("should return map with existing headers") {
					result shouldHaveSize 2
					result shouldContainKey "X-Forwarded-For"
					result shouldContainKey "X-Real-IP"
					result["X-Forwarded-For"] shouldBe "192.168.1.1"
					result["X-Real-IP"] shouldBe "10.0.0.1"
				}
			}

			When("no proxy headers exist") {
				every { request.getHeader(any<String>()) } returns null

				val result = HttpUtils.getProxyHeaders(request)

				Then("should return empty map") {
					result shouldHaveSize 0
				}
			}
		}
	})
