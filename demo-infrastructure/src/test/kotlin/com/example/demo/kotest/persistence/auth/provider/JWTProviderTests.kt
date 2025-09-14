package com.example.demo.kotest.persistence.auth.provider

import com.example.demo.persistence.auth.provider.JWTProvider
import com.example.demo.security.model.SecurityUserItem
import com.example.demo.user.constant.UserRole
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldNotBeEmpty
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.test.context.ActiveProfiles
import java.nio.charset.StandardCharsets

@ActiveProfiles("test")
@Tags("kotest-unit-test")
class JWTProviderTests :
	FunSpec({

		lateinit var userDetailsService: UserDetailsService
		lateinit var jwtProvider: JWTProvider

		val secretKey = "test-secret-key-that-is-at-least-256-bits-long-for-hmac-sha"
		val accessExpireTime = 3600L
		val refreshExpireTime = 86400L

		beforeTest {
			userDetailsService = mockk()
			jwtProvider =
				JWTProvider(
					userDetailsServiceAdapter = userDetailsService,
					secretKeyString = secretKey,
					accessExpireTime = accessExpireTime,
					refreshExpireTime = refreshExpireTime
				)
		}

		context("JWT Token Creation") {
			test("should create Access Token") {
				val securityUserItem = createSecurityUserItem()

				val token = jwtProvider.createAccessToken(securityUserItem)

				token.shouldNotBeEmpty()
				token.split(".").size shouldBe 3
			}

			test("should create Refresh Token") {
				val securityUserItem = createSecurityUserItem()

				val token = jwtProvider.createRefreshToken(securityUserItem)

				token.shouldNotBeEmpty()
				token.split(".").size shouldBe 3
			}

			test("Access Token and Refresh Token should have different expiration times") {
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

				(refreshExpiration - accessExpiration) shouldBe (refreshExpireTime - accessExpireTime) * 1000
			}

			test("token should contain user information") {
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

				claims.subject shouldBe securityUserItem.userId.toString()
				claims["email"] shouldBe securityUserItem.email
				claims["role"] shouldBe securityUserItem.role.name
			}
		}

		context("JWT Token Validation") {
			test("should validate valid token") {
				val securityUserItem = createSecurityUserItem()
				val token = jwtProvider.createAccessToken(securityUserItem)

				val mockUserDetails = mockk<UserDetails>()
				every { mockUserDetails.authorities } returns emptyList()
				every { userDetailsService.loadUserByUsername(securityUserItem.userId.toString()) } returns mockUserDetails

				val isValid = jwtProvider.validateToken(token)

				isValid shouldBe true
			}

			test("should throw exception for invalid token format") {
				val invalidToken = "invalid.token.format"

				shouldThrow<Exception> {
					jwtProvider.validateToken(invalidToken)
				}
			}

			test("should throw exception for expired token") {
				val expiredProvider =
					JWTProvider(
						userDetailsServiceAdapter = userDetailsService,
						secretKeyString = secretKey,
						accessExpireTime = -1,
						refreshExpireTime = refreshExpireTime
					)
				val securityUserItem = createSecurityUserItem()
				val expiredToken = expiredProvider.createAccessToken(securityUserItem)

				shouldThrow<ExpiredJwtException> {
					jwtProvider.validateToken(expiredToken)
				}
			}
		}

		context("Authentication Extraction") {
			test("should extract Authentication from token") {
				val securityUserItem = createSecurityUserItem()
				val token = jwtProvider.createAccessToken(securityUserItem)

				val mockUserDetails = mockk<UserDetails>()
				every { mockUserDetails.authorities } returns emptyList()
				every { userDetailsService.loadUserByUsername(securityUserItem.userId.toString()) } returns mockUserDetails

				val authentication = jwtProvider.getAuthentication(token)

				authentication shouldNotBe null
				authentication.principal shouldBe mockUserDetails
				verify { userDetailsService.loadUserByUsername(securityUserItem.userId.toString()) }
			}

			test("should extract Claims from expired token in refresh mode") {
				val expiredProvider =
					JWTProvider(
						userDetailsServiceAdapter = userDetailsService,
						secretKeyString = secretKey,
						accessExpireTime = -1,
						refreshExpireTime = refreshExpireTime
					)
				val securityUserItem = createSecurityUserItem()
				val expiredToken = expiredProvider.createAccessToken(securityUserItem)

				val mockUserDetails = mockk<UserDetails>()
				every { mockUserDetails.authorities } returns emptyList()
				every { userDetailsService.loadUserByUsername(securityUserItem.userId.toString()) } returns mockUserDetails

				val authentication = jwtProvider.getAuthentication(expiredToken, isRefresh = true)

				authentication shouldNotBe null
				authentication.principal shouldBe mockUserDetails
			}
		}

		context("Refresh Access Token with Refresh Token") {
			test("should create new Access Token with valid Refresh Token") {
				val securityUserItem = createSecurityUserItem()
				val refreshToken = jwtProvider.createRefreshToken(securityUserItem)

				val mockUserDetails = mockk<UserDetails>()
				every { mockUserDetails.authorities } returns emptyList()
				every { userDetailsService.loadUserByUsername(securityUserItem.userId.toString()) } returns mockUserDetails

				val newAccessToken = jwtProvider.refreshAccessToken(securityUserItem, refreshToken)

				newAccessToken.shouldNotBeEmpty()
				newAccessToken shouldNotBe refreshToken
			}

			test("should not refresh Access Token with invalid Refresh Token") {
				val securityUserItem = createSecurityUserItem()
				val invalidRefreshToken = "invalid.refresh.token"

				shouldThrow<Exception> {
					jwtProvider.refreshAccessToken(securityUserItem, invalidRefreshToken)
				}
			}
		}
	})

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
