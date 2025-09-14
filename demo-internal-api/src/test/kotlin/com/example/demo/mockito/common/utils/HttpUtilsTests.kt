package com.example.demo.mockito.common.utils

import com.example.demo.common.utils.HttpUtils
import jakarta.servlet.http.HttpServletRequest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@Tag("mockito-unit-test")
@DisplayName("HttpUtils Test")
@ExtendWith(MockitoExtension::class)
class HttpUtilsTests {
	@Nested
	@DisplayName("Get Client IP Test")
	inner class GetClientIpTest {
		private val request = mock<HttpServletRequest>()

		@Test
		@DisplayName("Success extract first IP from X-Forwarded-For header")
		fun should_ReturnFirstIp_when_XForwardedForContainsMultipleIps() {
			whenever(request.getHeader(any<String>())).thenReturn(null)
			whenever(request.getHeader("X-Forwarded-For")).thenReturn("203.0.113.195, 70.41.3.18, 150.172.238.178")
			whenever(request.remoteAddr).thenReturn("150.172.238.178")

			val clientIp = HttpUtils.getClientIp(request)

			assertEquals("203.0.113.195", clientIp)
		}

		@Test
		@DisplayName("Success extract IP from X-Real-IP header")
		fun should_ReturnXRealIp_when_XRealIpExists() {
			whenever(request.getHeader(any<String>())).thenReturn(null)
			whenever(request.getHeader("X-Forwarded-For")).thenReturn(null)
			whenever(request.getHeader("X-Real-IP")).thenReturn("198.51.100.42")
			whenever(request.remoteAddr).thenReturn("10.0.0.1")

			val clientIp = HttpUtils.getClientIp(request)

			assertEquals("198.51.100.42", clientIp)
		}

		@Test
		@DisplayName("Skip invalid value and check next header")
		fun should_SkipInvalidValue_when_HeaderContainsUnknown() {
			whenever(request.getHeader(any<String>())).thenReturn(null)
			whenever(request.getHeader("X-Forwarded-For")).thenReturn("unknown")
			whenever(request.getHeader("X-Real-IP")).thenReturn("192.168.1.100")
			whenever(request.remoteAddr).thenReturn("10.0.0.1")

			val clientIp = HttpUtils.getClientIp(request)

			assertEquals("192.168.1.100", clientIp)
		}

		@Test
		@DisplayName("Return remoteAddr when no proxy headers")
		fun should_ReturnRemoteAddr_when_NoProxyHeaders() {
			whenever(request.getHeader(any<String>())).thenReturn(null)
			whenever(request.remoteAddr).thenReturn("192.168.1.100")

			val clientIp = HttpUtils.getClientIp(request)

			assertEquals("192.168.1.100", clientIp)
		}

		@Test
		@DisplayName("Return unknown when remoteAddr is localhost")
		fun should_ReturnUnknown_when_RemoteAddrIsLocalhost() {
			whenever(request.getHeader(any<String>())).thenReturn(null)
			whenever(request.remoteAddr).thenReturn("127.0.0.1")

			val clientIp = HttpUtils.getClientIp(request)

			assertEquals("unknown", clientIp)
		}
	}

	@Nested
	@DisplayName("Get User Agent Test")
	inner class GetUserAgentTest {
		private val request = mock<HttpServletRequest>()
		private val userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"

		@Test
		@DisplayName("Success return User-Agent header")
		fun should_ReturnUserAgent_when_HeaderExists() {
			whenever(request.getHeader("User-Agent")).thenReturn(userAgent)

			val result = HttpUtils.getUserAgent(request)

			assertEquals(userAgent, result)
		}

		@Test
		@DisplayName("Truncate User-Agent when maxLength specified")
		fun should_TruncateUserAgent_when_MaxLengthSpecified() {
			whenever(request.getHeader("User-Agent")).thenReturn(userAgent)

			val result = HttpUtils.getUserAgent(request, 20)

			assertEquals("Mozilla/5.0 (Windows...", result)
		}

		@Test
		@DisplayName("Return unknown when User-Agent is null")
		fun should_ReturnUnknown_when_UserAgentIsNull() {
			whenever(request.getHeader("User-Agent")).thenReturn(null)

			val result = HttpUtils.getUserAgent(request)

			assertEquals("unknown", result)
		}
	}

	@Nested
	@DisplayName("Get Full URL Test")
	inner class GetFullUrlTest {
		private val request = mock<HttpServletRequest>()

		@Test
		@DisplayName("Success return URL with query parameters")
		fun should_ReturnFullUrl_when_QueryStringExists() {
			whenever(request.requestURL).thenReturn(StringBuffer("https://api.example.com/users"))
			whenever(request.queryString).thenReturn("page=1&size=10")

			val result = HttpUtils.getFullUrl(request)

			assertEquals("https://api.example.com/users?page=1&size=10", result)
		}

		@Test
		@DisplayName("Return URL only when no query string")
		fun should_ReturnUrlOnly_when_NoQueryString() {
			whenever(request.requestURL).thenReturn(StringBuffer("https://api.example.com/users"))
			whenever(request.queryString).thenReturn(null)

			val result = HttpUtils.getFullUrl(request)

			assertEquals("https://api.example.com/users", result)
		}
	}

	@Nested
	@DisplayName("Is Ajax Request Test")
	inner class IsAjaxRequestTest {
		private val request = mock<HttpServletRequest>()

		@Test
		@DisplayName("Return true for Ajax request")
		fun should_ReturnTrue_when_XRequestedWithIsXMLHttpRequest() {
			whenever(request.getHeader("X-Requested-With")).thenReturn("XMLHttpRequest")

			val result = HttpUtils.isAjaxRequest(request)

			assertTrue(result)
		}

		@Test
		@DisplayName("Return false when header missing")
		fun should_ReturnFalse_when_HeaderMissing() {
			whenever(request.getHeader("X-Requested-With")).thenReturn(null)

			val result = HttpUtils.isAjaxRequest(request)

			assertFalse(result)
		}
	}

	@Nested
	@DisplayName("Is JSON Request Test")
	inner class IsJsonRequestTest {
		private val request = mock<HttpServletRequest>()

		@Test
		@DisplayName("Return true for JSON content type")
		fun should_ReturnTrue_when_ContentTypeIsJson() {
			whenever(request.contentType).thenReturn("application/json")

			val result = HttpUtils.isJsonRequest(request)

			assertTrue(result)
		}

		@Test
		@DisplayName("Return true for JSON with charset")
		fun should_ReturnTrue_when_JsonWithCharset() {
			whenever(request.contentType).thenReturn("application/json;charset=UTF-8")

			val result = HttpUtils.isJsonRequest(request)

			assertTrue(result)
		}

		@Test
		@DisplayName("Return false when content type is null")
		fun should_ReturnFalse_when_ContentTypeIsNull() {
			whenever(request.contentType).thenReturn(null)

			val result = HttpUtils.isJsonRequest(request)

			assertFalse(result)
		}
	}

	@Nested
	@DisplayName("Get Referer Test")
	inner class GetRefererTest {
		private val request = mock<HttpServletRequest>()

		@Test
		@DisplayName("Success return Referer header")
		fun should_ReturnReferer_when_HeaderExists() {
			whenever(request.getHeader("Referer")).thenReturn("https://example.com/page")

			val result = HttpUtils.getReferer(request)

			assertEquals("https://example.com/page", result)
		}

		@Test
		@DisplayName("Return null when Referer missing")
		fun should_ReturnNull_when_RefererMissing() {
			whenever(request.getHeader("Referer")).thenReturn(null)

			val result = HttpUtils.getReferer(request)

			assertNull(result)
		}
	}

	@Nested
	@DisplayName("Is Proxied Request Test")
	inner class IsProxiedRequestTest {
		private val request = mock<HttpServletRequest>()

		@Test
		@DisplayName("Return true when proxy headers exist")
		fun should_ReturnTrue_when_ProxyHeadersExist() {
			whenever(request.getHeader(any<String>())).thenReturn(null)
			whenever(request.getHeader("X-Forwarded-For")).thenReturn("192.168.1.1")

			val result = HttpUtils.isProxiedRequest(request)

			assertTrue(result)
		}

		@Test
		@DisplayName("Return false when no proxy headers")
		fun should_ReturnFalse_when_NoProxyHeaders() {
			whenever(request.getHeader(any<String>())).thenReturn(null)

			val result = HttpUtils.isProxiedRequest(request)

			assertFalse(result)
		}
	}

	@Nested
	@DisplayName("Get Proxy Headers Test")
	inner class GetProxyHeadersTest {
		private val request = mock<HttpServletRequest>()

		@Test
		@DisplayName("Return map with existing proxy headers")
		fun should_ReturnMap_when_ProxyHeadersExist() {
			whenever(request.getHeader(any<String>())).thenReturn(null)
			whenever(request.getHeader("X-Forwarded-For")).thenReturn("192.168.1.1")
			whenever(request.getHeader("X-Real-IP")).thenReturn("10.0.0.1")
			whenever(request.getHeader("CF-Connecting-IP")).thenReturn(null)

			val result = HttpUtils.getProxyHeaders(request)

			assertEquals(2, result.size)
			assertTrue(result.containsKey("X-Forwarded-For"))
			assertTrue(result.containsKey("X-Real-IP"))
			assertEquals("192.168.1.1", result["X-Forwarded-For"])
			assertEquals("10.0.0.1", result["X-Real-IP"])
		}

		@Test
		@DisplayName("Return empty map when no proxy headers")
		fun should_ReturnEmptyMap_when_NoProxyHeaders() {
			whenever(request.getHeader(any<String>())).thenReturn(null)

			val result = HttpUtils.getProxyHeaders(request)

			assertTrue(result.isEmpty())
		}
	}
}
