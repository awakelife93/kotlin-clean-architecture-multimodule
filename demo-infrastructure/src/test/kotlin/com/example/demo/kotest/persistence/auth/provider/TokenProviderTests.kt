package com.example.demo.kotest.persistence.auth.provider

import com.example.demo.auth.exception.RefreshTokenNotFoundException
import com.example.demo.persistence.auth.provider.JWTProvider
import com.example.demo.persistence.auth.provider.TokenProvider
import com.example.demo.redis.RedisFactoryProvider
import com.example.demo.security.model.SecurityUserItem
import com.example.demo.user.constant.UserRole
import com.example.demo.user.model.User
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.springframework.test.context.ActiveProfiles
import java.util.concurrent.TimeUnit

@ActiveProfiles("test")
@Tags("kotest-unit-test")
class TokenProviderTests :
	FunSpec({

		lateinit var jwtProvider: JWTProvider
		lateinit var redisFactoryProvider: RedisFactoryProvider
		lateinit var tokenProvider: TokenProvider

		val refreshExpireTime = 86400L

		beforeTest {
			jwtProvider = mockk()
			redisFactoryProvider = mockk()

			every { jwtProvider.refreshExpireTime } returns refreshExpireTime

			tokenProvider = TokenProvider(jwtProvider, redisFactoryProvider)
		}

		afterTest {
			clearAllMocks()
		}

		context("Get Refresh Token") {
			test("should return refresh token when exists in Redis") {
				val userId = 1L
				val expectedKey = "Session_$userId"
				val expectedToken = "existing-refresh-token"

				every { redisFactoryProvider.generateSessionKey(userId) } returns expectedKey
				every { redisFactoryProvider.get(expectedKey) } returns expectedToken

				val token = tokenProvider.getRefreshToken(userId)

				token shouldBe expectedToken
				verify(exactly = 1) {
					redisFactoryProvider.generateSessionKey(userId)
					redisFactoryProvider.get(expectedKey)
				}
			}

			test("should throw RefreshTokenNotFoundException when token does not exist") {
				val userId = 2L
				val expectedKey = "Session_$userId"

				every { redisFactoryProvider.generateSessionKey(userId) } returns expectedKey
				every { redisFactoryProvider.get(expectedKey) } returns null

				val exception =
					shouldThrow<RefreshTokenNotFoundException> {
						tokenProvider.getRefreshToken(userId)
					}

				exception.message shouldBe "Refresh Token Not Found userId = $userId"
				verify(exactly = 1) {
					redisFactoryProvider.generateSessionKey(userId)
					redisFactoryProvider.get(expectedKey)
				}
			}

			test("should handle different user IDs") {
				val userId1 = 100L
				val userId2 = 200L
				val key1 = "Session_$userId1"
				val key2 = "Session_$userId2"
				val token1 = "token-100"
				val token2 = "token-200"

				every { redisFactoryProvider.generateSessionKey(userId1) } returns key1
				every { redisFactoryProvider.generateSessionKey(userId2) } returns key2
				every { redisFactoryProvider.get(key1) } returns token1
				every { redisFactoryProvider.get(key2) } returns token2

				val result1 = tokenProvider.getRefreshToken(userId1)
				val result2 = tokenProvider.getRefreshToken(userId2)

				result1 shouldBe token1
				result2 shouldBe token2
				result1 shouldNotBe result2
			}
		}

		context("Delete Refresh Token") {
			test("should delete refresh token from Redis") {
				val userId = 1L
				val expectedKey = "Session_$userId"

				every { redisFactoryProvider.generateSessionKey(userId) } returns expectedKey
				every { redisFactoryProvider.delete(expectedKey) } just runs

				tokenProvider.deleteRefreshToken(userId)

				verify(exactly = 1) {
					redisFactoryProvider.generateSessionKey(userId)
					redisFactoryProvider.delete(expectedKey)
				}
			}

			test("should delete only specified user's token") {
				val userId1 = 10L
				val userId2 = 20L
				val key1 = "Session_$userId1"

				every { redisFactoryProvider.generateSessionKey(userId1) } returns key1
				every { redisFactoryProvider.delete(key1) } just runs

				tokenProvider.deleteRefreshToken(userId1)

				verify(exactly = 1) {
					redisFactoryProvider.generateSessionKey(userId1)
					redisFactoryProvider.delete(key1)
				}
				verify(exactly = 0) {
					redisFactoryProvider.generateSessionKey(userId2)
				}
			}

			test("should handle deletion of non-existent token") {
				val userId = 999L
				val expectedKey = "Session_$userId"

				every { redisFactoryProvider.generateSessionKey(userId) } returns expectedKey
				every { redisFactoryProvider.delete(expectedKey) } just runs

				tokenProvider.deleteRefreshToken(userId)

				verify(exactly = 1) {
					redisFactoryProvider.generateSessionKey(userId)
					redisFactoryProvider.delete(expectedKey)
				}
			}
		}

		context("Create Refresh Token") {
			test("should create refresh token and store in Redis") {
				val user = createUser()
				val expectedKey = "Session_${user.id}"
				val refreshToken = "new-refresh-token"
				val securityUserItem = SecurityUserItem.from(user)

				every { redisFactoryProvider.generateSessionKey(user.id) } returns expectedKey
				every { jwtProvider.createRefreshToken(securityUserItem) } returns refreshToken
				every {
					redisFactoryProvider.set(expectedKey, refreshToken, refreshExpireTime, TimeUnit.SECONDS)
				} just runs

				tokenProvider.createRefreshToken(user)

				verify(exactly = 1) {
					redisFactoryProvider.generateSessionKey(user.id)
					jwtProvider.createRefreshToken(securityUserItem)
					redisFactoryProvider.set(expectedKey, refreshToken, refreshExpireTime, TimeUnit.SECONDS)
				}
			}

			test("should overwrite existing token for same user") {
				val user = createUser()
				val expectedKey = "Session_${user.id}"
				val firstToken = "first-refresh-token"
				val secondToken = "second-refresh-token"
				val securityUserItem = SecurityUserItem.from(user)

				every { redisFactoryProvider.generateSessionKey(user.id) } returns expectedKey
				every { jwtProvider.createRefreshToken(securityUserItem) } returnsMany listOf(firstToken, secondToken)
				every {
					redisFactoryProvider.set(any<String>(), any<String>(), any<Long>(), any<TimeUnit>())
				} just runs

				tokenProvider.createRefreshToken(user)
				tokenProvider.createRefreshToken(user)

				verify(exactly = 2) {
					jwtProvider.createRefreshToken(securityUserItem)
					redisFactoryProvider.set(expectedKey, any<String>(), refreshExpireTime, TimeUnit.SECONDS)
				}
			}

			test("should handle users with different roles") {
				val adminUser = createUser(id = 1L, role = UserRole.ADMIN)
				val normalUser = createUser(id = 2L, role = UserRole.USER)

				val adminKey = "Session_${adminUser.id}"
				val userKey = "Session_${normalUser.id}"
				val adminToken = "admin-token"
				val userToken = "user-token"

				every { redisFactoryProvider.generateSessionKey(adminUser.id) } returns adminKey
				every { redisFactoryProvider.generateSessionKey(normalUser.id) } returns userKey
				every { jwtProvider.createRefreshToken(SecurityUserItem.from(adminUser)) } returns adminToken
				every { jwtProvider.createRefreshToken(SecurityUserItem.from(normalUser)) } returns userToken
				every { redisFactoryProvider.set(any<String>(), any<String>(), any<Long>(), any<TimeUnit>()) } just runs

				tokenProvider.createRefreshToken(adminUser)
				tokenProvider.createRefreshToken(normalUser)

				verify(exactly = 1) {
					redisFactoryProvider.set(adminKey, adminToken, refreshExpireTime, TimeUnit.SECONDS)
					redisFactoryProvider.set(userKey, userToken, refreshExpireTime, TimeUnit.SECONDS)
				}
			}
		}

		context("Create Access Token") {
			test("should create access token for user") {
				val user = createUser()
				val expectedToken = "access-token"
				val securityUserItem = SecurityUserItem.from(user)

				every { jwtProvider.createAccessToken(securityUserItem) } returns expectedToken

				val token = tokenProvider.createAccessToken(user)

				token shouldBe expectedToken
				verify(exactly = 1) { jwtProvider.createAccessToken(securityUserItem) }
			}

			test("should create different tokens for different users") {
				val user1 = createUser(id = 1L, email = "user1@example.com")
				val user2 = createUser(id = 2L, email = "user2@example.com")
				val token1 = "token-user-1"
				val token2 = "token-user-2"

				every { jwtProvider.createAccessToken(SecurityUserItem.from(user1)) } returns token1
				every { jwtProvider.createAccessToken(SecurityUserItem.from(user2)) } returns token2

				val result1 = tokenProvider.createAccessToken(user1)
				val result2 = tokenProvider.createAccessToken(user2)

				result1 shouldBe token1
				result2 shouldBe token2
				result1 shouldNotBe result2
			}

			test("should handle users with different roles correctly") {
				val adminUser = createUser(role = UserRole.ADMIN)
				val normalUser = createUser(role = UserRole.USER)
				val adminToken = "admin-access-token"
				val userToken = "user-access-token"

				every { jwtProvider.createAccessToken(SecurityUserItem.from(adminUser)) } returns adminToken
				every { jwtProvider.createAccessToken(SecurityUserItem.from(normalUser)) } returns userToken

				val adminResult = tokenProvider.createAccessToken(adminUser)
				val userResult = tokenProvider.createAccessToken(normalUser)

				adminResult shouldBe adminToken
				userResult shouldBe userToken
			}
		}

		context("Refresh Access Token") {
			test("should refresh access token successfully") {
				val securityUserItem =
					SecurityUserItem(
						userId = 1L,
						role = UserRole.USER,
						name = "Test User",
						email = "test@example.com"
					)
				val refreshToken = "valid-refresh-token"
				val newAccessToken = "new-access-token"
				val redisKey = "Session_${securityUserItem.userId}"

				every { redisFactoryProvider.generateSessionKey(securityUserItem.userId) } returns redisKey
				every { redisFactoryProvider.get(redisKey) } returns refreshToken
				every { jwtProvider.refreshAccessToken(securityUserItem, refreshToken) } returns newAccessToken

				val token = tokenProvider.refreshAccessToken(securityUserItem)

				token shouldBe newAccessToken
				verify(exactly = 1) {
					redisFactoryProvider.generateSessionKey(securityUserItem.userId)
					redisFactoryProvider.get(redisKey)
					jwtProvider.refreshAccessToken(securityUserItem, refreshToken)
				}
			}

			test("should throw exception when refresh token not found") {
				val securityUserItem =
					SecurityUserItem(
						userId = 2L,
						role = UserRole.USER,
						name = "Test User",
						email = "test@example.com"
					)
				val redisKey = "Session_${securityUserItem.userId}"

				every { redisFactoryProvider.generateSessionKey(securityUserItem.userId) } returns redisKey
				every { redisFactoryProvider.get(redisKey) } returns null

				shouldThrow<RefreshTokenNotFoundException> {
					tokenProvider.refreshAccessToken(securityUserItem)
				}

				verify(exactly = 0) {
					jwtProvider.refreshAccessToken(any<SecurityUserItem>(), any<String>())
				}
			}

			test("should handle different security user items") {
				val userItem = SecurityUserItem(1L, UserRole.USER, "User", "user@example.com")
				val adminItem = SecurityUserItem(2L, UserRole.ADMIN, "Admin", "admin@example.com")

				val userRefreshToken = "user-refresh"
				val adminRefreshToken = "admin-refresh"
				val userAccessToken = "user-access"
				val adminAccessToken = "admin-access"

				every { redisFactoryProvider.generateSessionKey(1L) } returns "Session_1"
				every { redisFactoryProvider.generateSessionKey(2L) } returns "Session_2"
				every { redisFactoryProvider.get("Session_1") } returns userRefreshToken
				every { redisFactoryProvider.get("Session_2") } returns adminRefreshToken
				every { jwtProvider.refreshAccessToken(userItem, userRefreshToken) } returns userAccessToken
				every { jwtProvider.refreshAccessToken(adminItem, adminRefreshToken) } returns adminAccessToken

				val userResult = tokenProvider.refreshAccessToken(userItem)
				val adminResult = tokenProvider.refreshAccessToken(adminItem)

				userResult shouldBe userAccessToken
				adminResult shouldBe adminAccessToken
			}
		}

		context("Create Full Tokens") {
			test("should create both refresh and access tokens") {
				val user = createUser()
				val expectedKey = "Session_${user.id}"
				val refreshToken = "refresh-token"
				val accessToken = "access-token"
				val securityUserItem = SecurityUserItem.from(user)

				every { redisFactoryProvider.generateSessionKey(user.id) } returns expectedKey
				every { jwtProvider.createRefreshToken(securityUserItem) } returns refreshToken
				every { jwtProvider.createAccessToken(securityUserItem) } returns accessToken
				every {
					redisFactoryProvider.set(expectedKey, refreshToken, refreshExpireTime, TimeUnit.SECONDS)
				} just runs

				val token = tokenProvider.createFullTokens(user)

				token shouldBe accessToken
				verify(exactly = 1) {
					redisFactoryProvider.generateSessionKey(user.id)
					jwtProvider.createRefreshToken(securityUserItem)
					jwtProvider.createAccessToken(securityUserItem)
					redisFactoryProvider.set(expectedKey, refreshToken, refreshExpireTime, TimeUnit.SECONDS)
				}
			}

			test("should return only access token but store refresh token") {
				val user = createUser()
				val refreshToken = "refresh-token-not-returned"
				val accessToken = "access-token-returned"
				val securityUserItem = SecurityUserItem.from(user)

				every { redisFactoryProvider.generateSessionKey(user.id) } returns "Session_${user.id}"
				every { jwtProvider.createRefreshToken(securityUserItem) } returns refreshToken
				every { jwtProvider.createAccessToken(securityUserItem) } returns accessToken
				every { redisFactoryProvider.set(any<String>(), any<String>(), any<Long>(), any<TimeUnit>()) } just runs

				val returnedToken = tokenProvider.createFullTokens(user)

				returnedToken shouldBe accessToken
				returnedToken shouldNotBe refreshToken
			}

			test("should handle multiple users creating full tokens") {
				val user1 = createUser(id = 1L, email = "user1@example.com")
				val user2 = createUser(id = 2L, email = "user2@example.com")

				every { redisFactoryProvider.generateSessionKey(any<Long>()) } answers {
					"Session_${firstArg<Long>()}"
				}
				every { jwtProvider.createRefreshToken(any<SecurityUserItem>()) } returns "refresh-token"
				every { jwtProvider.createAccessToken(SecurityUserItem.from(user1)) } returns "access-token-1"
				every { jwtProvider.createAccessToken(SecurityUserItem.from(user2)) } returns "access-token-2"
				every { redisFactoryProvider.set(any<String>(), any<String>(), any<Long>(), any<TimeUnit>()) } just runs

				val token1 = tokenProvider.createFullTokens(user1)
				val token2 = tokenProvider.createFullTokens(user2)

				token1 shouldBe "access-token-1"
				token2 shouldBe "access-token-2"
				verify(exactly = 2) {
					jwtProvider.createRefreshToken(any<SecurityUserItem>())
					jwtProvider.createAccessToken(any<SecurityUserItem>())
					redisFactoryProvider.set(any<String>(), any<String>(), refreshExpireTime, TimeUnit.SECONDS)
				}
			}
		}

		context("Edge Cases") {
			test("should handle very large user IDs") {
				val largeUserId = Long.MAX_VALUE
				val expectedKey = "Session_$largeUserId"
				val token = "token-for-max-id"

				every { redisFactoryProvider.generateSessionKey(largeUserId) } returns expectedKey
				every { redisFactoryProvider.get(expectedKey) } returns token

				val result = tokenProvider.getRefreshToken(largeUserId)

				result shouldBe token
			}

			test("should handle negative user IDs") {
				val negativeUserId = -1L
				val expectedKey = "Session_$negativeUserId"

				every { redisFactoryProvider.generateSessionKey(negativeUserId) } returns expectedKey
				every { redisFactoryProvider.delete(expectedKey) } just runs

				tokenProvider.deleteRefreshToken(negativeUserId)

				verify { redisFactoryProvider.delete(expectedKey) }
			}

			test("should handle very long token strings") {
				val user = createUser()
				val longToken = "t".repeat(1000)
				val securityUserItem = SecurityUserItem.from(user)

				every { redisFactoryProvider.generateSessionKey(user.id) } returns "Session_${user.id}"
				every { jwtProvider.createRefreshToken(securityUserItem) } returns longToken
				every { redisFactoryProvider.set(any<String>(), any<String>(), any<Long>(), any<TimeUnit>()) } just runs

				tokenProvider.createRefreshToken(user)

				verify {
					redisFactoryProvider.set(any<String>(), longToken, any<Long>(), any<TimeUnit>())
				}
			}
		}
	})

private fun createUser(
	id: Long = 1L,
	email: String = "test@example.com",
	role: UserRole = UserRole.USER,
	name: String = "Test User"
): User =
	User(
		id = id,
		email = email,
		password = "hashed-password",
		name = name,
		role = role
	)
