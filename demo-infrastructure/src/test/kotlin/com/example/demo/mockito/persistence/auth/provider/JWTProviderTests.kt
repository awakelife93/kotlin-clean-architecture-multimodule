package com.example.demo.mockito.persistence.auth.provider

import com.example.demo.persistence.auth.provider.JWTProvider
import com.example.demo.security.model.SecurityUserItem
import com.example.demo.user.constant.UserRole
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.test.context.ActiveProfiles
import java.nio.charset.StandardCharsets

@ActiveProfiles("test")
@Tag("mockito-unit-test")
@DisplayName("Mockito Unit - JWT Provider Test")
@ExtendWith(MockitoExtension::class)
class JWTProviderTests {
	@Mock
	private lateinit var userDetailsService: UserDetailsService

	@Mock
	private lateinit var mockUserDetails: UserDetails

	private lateinit var jwtProvider: JWTProvider

	private val secretKey = "test-secret-key-that-is-at-least-256-bits-long-for-hmac-sha"
	private val accessExpireTime = 3600L
	private val refreshExpireTime = 86400L

	@BeforeEach
	fun setUp() {
		jwtProvider =
			JWTProvider(
				userDetailsServiceAdapter = userDetailsService,
				secretKeyString = secretKey,
				accessExpireTime = accessExpireTime,
				refreshExpireTime = refreshExpireTime
			)
	}

	@Nested
	@DisplayName("JWT Token Creation Tests")
	inner class TokenCreationTests {
		@Test
		@DisplayName("should create Access Token")
		fun `should create access token`() {
			val securityUserItem = createSecurityUserItem()

			val token = jwtProvider.createAccessToken(securityUserItem)

			assertNotNull(token)
			assertTrue(token.isNotEmpty())
			assertEquals(3, token.split(".").size)
		}

		@Test
		@DisplayName("should create Refresh Token")
		fun `should create refresh token`() {
			val securityUserItem = createSecurityUserItem()

			val token = jwtProvider.createRefreshToken(securityUserItem)

			assertNotNull(token)
			assertTrue(token.isNotEmpty())
			assertEquals(3, token.split(".").size)
		}

		@Test
		@DisplayName("Access Token and Refresh Token should have different expiration times")
		fun `access and refresh tokens should have different expiration times`() {
			val securityUserItem = createSecurityUserItem()
			val key = Keys.hmacShaKeyFor(secretKey.toByteArray(StandardCharsets.UTF_8))

			val accessToken = jwtProvider.createAccessToken(securityUserItem)
			val refreshToken = jwtProvider.createRefreshToken(securityUserItem)

			val accessClaims =
				Jwts
					.parser()
					.verifyWith(key)
					.build()
					.parseSignedClaims(accessToken)
					.payload

			val refreshClaims =
				Jwts
					.parser()
					.verifyWith(key)
					.build()
					.parseSignedClaims(refreshToken)
					.payload

			val accessExpiration = accessClaims.expiration.time
			val refreshExpiration = refreshClaims.expiration.time

			assertEquals(
				(refreshExpireTime - accessExpireTime) * 1000,
				refreshExpiration - accessExpiration
			)
		}

		@Test
		@DisplayName("token should contain user information")
		fun `token should contain user information`() {
			val securityUserItem = createSecurityUserItem()
			val key = Keys.hmacShaKeyFor(secretKey.toByteArray(StandardCharsets.UTF_8))

			val token = jwtProvider.createAccessToken(securityUserItem)

			val claims =
				Jwts
					.parser()
					.verifyWith(key)
					.build()
					.parseSignedClaims(token)
					.payload

			assertEquals(securityUserItem.userId.toString(), claims.subject)
			assertEquals(securityUserItem.email, claims["email"])
			assertEquals(securityUserItem.role.name, claims["role"])
		}
	}

	@Nested
	@DisplayName("JWT Token Validation Tests")
	inner class TokenValidationTests {
		@Test
		@DisplayName("should validate valid token")
		fun `should validate valid token`() {
			val securityUserItem = createSecurityUserItem()
			val token = jwtProvider.createAccessToken(securityUserItem)

			whenever(mockUserDetails.authorities).thenReturn(emptyList())
			whenever(userDetailsService.loadUserByUsername(securityUserItem.userId.toString()))
				.thenReturn(mockUserDetails)

			val isValid = jwtProvider.validateToken(token)

			assertTrue(isValid)
			verify(userDetailsService).loadUserByUsername(securityUserItem.userId.toString())
		}

		@Test
		@DisplayName("should throw exception for invalid token format")
		fun `should throw exception for invalid token format`() {
			val invalidToken = "invalid.token.format"

			assertThrows<Exception> {
				jwtProvider.validateToken(invalidToken)
			}
		}

		@Test
		@DisplayName("should throw exception for expired token")
		fun `should throw exception for expired token`() {
			val expiredProvider =
				JWTProvider(
					userDetailsServiceAdapter = userDetailsService,
					secretKeyString = secretKey,
					accessExpireTime = -1,
					refreshExpireTime = refreshExpireTime
				)
			val securityUserItem = createSecurityUserItem()
			val expiredToken = expiredProvider.createAccessToken(securityUserItem)

			assertThrows<ExpiredJwtException> {
				jwtProvider.validateToken(expiredToken)
			}
		}

		@Test
		@DisplayName("should fail validation for token with different secret key")
		fun `should fail validation for token with different secret key`() {
			val differentSecretProvider =
				JWTProvider(
					userDetailsServiceAdapter = userDetailsService,
					secretKeyString = "different-secret-key-that-is-at-least-256-bits-long",
					accessExpireTime = accessExpireTime,
					refreshExpireTime = refreshExpireTime
				)
			val securityUserItem = createSecurityUserItem()
			val tokenWithDifferentKey = differentSecretProvider.createAccessToken(securityUserItem)

			assertThrows<Exception> {
				jwtProvider.validateToken(tokenWithDifferentKey)
			}
		}
	}

	@Nested
	@DisplayName("Authentication Extraction Tests")
	inner class AuthenticationExtractionTests {
		@Test
		@DisplayName("should extract Authentication from token")
		fun `should extract authentication from token`() {
			val securityUserItem = createSecurityUserItem()
			val token = jwtProvider.createAccessToken(securityUserItem)

			whenever(mockUserDetails.authorities).thenReturn(emptyList())
			whenever(userDetailsService.loadUserByUsername(securityUserItem.userId.toString()))
				.thenReturn(mockUserDetails)

			val authentication = jwtProvider.getAuthentication(token)

			assertNotNull(authentication)
			assertEquals(mockUserDetails, authentication.principal)
			verify(userDetailsService).loadUserByUsername(securityUserItem.userId.toString())
		}

		@Test
		@DisplayName("should extract Claims from expired token in refresh mode")
		fun `should extract claims from expired token in refresh mode`() {
			val expiredProvider =
				JWTProvider(
					userDetailsServiceAdapter = userDetailsService,
					secretKeyString = secretKey,
					accessExpireTime = -1,
					refreshExpireTime = refreshExpireTime
				)
			val securityUserItem = createSecurityUserItem()
			val expiredToken = expiredProvider.createAccessToken(securityUserItem)

			whenever(mockUserDetails.authorities).thenReturn(emptyList())
			whenever(userDetailsService.loadUserByUsername(securityUserItem.userId.toString()))
				.thenReturn(mockUserDetails)

			val authentication = jwtProvider.getAuthentication(expiredToken, isRefresh = true)

			assertNotNull(authentication)
			assertEquals(mockUserDetails, authentication.principal)
		}

		@Test
		@DisplayName("should throw exception for expired token in normal mode")
		fun `should throw exception for expired token in normal mode`() {
			val expiredProvider =
				JWTProvider(
					userDetailsServiceAdapter = userDetailsService,
					secretKeyString = secretKey,
					accessExpireTime = -1,
					refreshExpireTime = refreshExpireTime
				)
			val securityUserItem = createSecurityUserItem()
			val expiredToken = expiredProvider.createAccessToken(securityUserItem)

			assertThrows<ExpiredJwtException> {
				jwtProvider.getAuthentication(expiredToken, isRefresh = false)
			}
		}
	}

	@Nested
	@DisplayName("Refresh Token to Access Token Tests")
	inner class TokenRefreshTests {
		@Test
		@DisplayName("should create new Access Token with valid Refresh Token")
		fun `should create new access token with valid refresh token`() {
			val securityUserItem = createSecurityUserItem()
			val refreshToken = jwtProvider.createRefreshToken(securityUserItem)

			whenever(mockUserDetails.authorities).thenReturn(emptyList())
			whenever(userDetailsService.loadUserByUsername(securityUserItem.userId.toString()))
				.thenReturn(mockUserDetails)

			val newAccessToken = jwtProvider.refreshAccessToken(securityUserItem, refreshToken)

			assertNotNull(newAccessToken)
			assertTrue(newAccessToken.isNotEmpty())
			assertNotEquals(refreshToken, newAccessToken)
		}

		@Test
		@DisplayName("should not refresh Access Token with invalid Refresh Token")
		fun `should not refresh access token with invalid refresh token`() {
			val securityUserItem = createSecurityUserItem()
			val invalidRefreshToken = "invalid.refresh.token"

			assertThrows<Exception> {
				jwtProvider.refreshAccessToken(securityUserItem, invalidRefreshToken)
			}
		}

		@Test
		@DisplayName("should not refresh Access Token with expired Refresh Token")
		fun `should not refresh access token with expired refresh token`() {
			val expiredRefreshProvider =
				JWTProvider(
					userDetailsServiceAdapter = userDetailsService,
					secretKeyString = secretKey,
					accessExpireTime = accessExpireTime,
					refreshExpireTime = -1
				)
			val securityUserItem = createSecurityUserItem()
			val expiredRefreshToken = expiredRefreshProvider.createRefreshToken(securityUserItem)

			assertThrows<ExpiredJwtException> {
				jwtProvider.refreshAccessToken(securityUserItem, expiredRefreshToken)
			}
		}
	}

	@Nested
	@DisplayName("Edge Cases Tests")
	inner class EdgeCaseTests {
		@Test
		@DisplayName("should create tokens for different user roles")
		fun `should create tokens for different user roles`() {
			val roles = listOf(UserRole.USER, UserRole.ADMIN)
			val key = Keys.hmacShaKeyFor(secretKey.toByteArray(StandardCharsets.UTF_8))

			roles.forEach { role ->
				val securityUserItem = createSecurityUserItem(role = role)

				val token = jwtProvider.createAccessToken(securityUserItem)

				val claims =
					Jwts
						.parser()
						.verifyWith(key)
						.build()
						.parseSignedClaims(token)
						.payload

				assertEquals(role.name, claims["role"])
			}
		}

		@Test
		@DisplayName("should handle very long email addresses")
		fun `should handle very long email addresses`() {
			val longEmail = "very.long.email.address.that.exceeds.normal.length@example.com"
			val securityUserItem = createSecurityUserItem(email = longEmail)
			val key = Keys.hmacShaKeyFor(secretKey.toByteArray(StandardCharsets.UTF_8))

			val token = jwtProvider.createAccessToken(securityUserItem)

			val claims =
				Jwts
					.parser()
					.verifyWith(key)
					.build()
					.parseSignedClaims(token)
					.payload

			assertEquals(longEmail, claims["email"])
		}
	}

	private fun createSecurityUserItem(
		userId: Long = 1L,
		email: String = "test@example.com",
		role: UserRole = UserRole.USER,
		name: String = "Test User"
	): SecurityUserItem =
		SecurityUserItem(
			userId = userId,
			role = role,
			name = name,
			email = email
		)
}
