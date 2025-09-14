package com.example.demo.mockito.persistence.auth.provider

import com.example.demo.auth.exception.RefreshTokenNotFoundException
import com.example.demo.persistence.auth.provider.JWTProvider
import com.example.demo.persistence.auth.provider.TokenProvider
import com.example.demo.redis.RedisFactoryProvider
import com.example.demo.security.model.SecurityUserItem
import com.example.demo.user.constant.UserRole
import com.example.demo.user.model.User
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.eq
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.test.context.ActiveProfiles
import java.util.concurrent.TimeUnit

@ActiveProfiles("test")
@Tag("mockito-unit-test")
@DisplayName("Mockito Unit - Token Provider Test")
@ExtendWith(MockitoExtension::class)
class TokenProviderTests {
	@Mock
	private lateinit var jwtProvider: JWTProvider

	@Mock
	private lateinit var redisFactoryProvider: RedisFactoryProvider

	private val refreshExpireTime = 86400L

	private val tokenProvider: TokenProvider by lazy {
		TokenProvider(jwtProvider, redisFactoryProvider)
	}

	@Nested
	@DisplayName("Get Refresh Token Tests")
	inner class GetRefreshTokenTests {
		@Test
		@DisplayName("should return refresh token when exists in Redis")
		fun `should return refresh token when exists`() {
			val userId = 1L
			val expectedKey = "Session_$userId"
			val expectedToken = "existing-refresh-token"

			whenever(redisFactoryProvider.generateSessionKey(userId)).thenReturn(expectedKey)
			whenever(redisFactoryProvider.get(expectedKey)).thenReturn(expectedToken)

			val token = tokenProvider.getRefreshToken(userId)

			assertEquals(expectedToken, token)
			verify(redisFactoryProvider).generateSessionKey(userId)
			verify(redisFactoryProvider).get(expectedKey)
		}

		@Test
		@DisplayName("should throw RefreshTokenNotFoundException when token does not exist")
		fun `should throw exception when token not found`() {
			val userId = 2L
			val expectedKey = "Session_$userId"

			whenever(redisFactoryProvider.generateSessionKey(userId)).thenReturn(expectedKey)
			whenever(redisFactoryProvider.get(expectedKey)).thenReturn(null)

			val exception =
				assertThrows<RefreshTokenNotFoundException> {
					tokenProvider.getRefreshToken(userId)
				}

			assertEquals("Refresh Token Not Found userId = $userId", exception.message)
			verify(redisFactoryProvider).generateSessionKey(userId)
			verify(redisFactoryProvider).get(expectedKey)
		}

		@Test
		@DisplayName("should handle different user IDs")
		fun `should handle different user IDs`() {
			val userId1 = 100L
			val userId2 = 200L
			val key1 = "Session_$userId1"
			val key2 = "Session_$userId2"
			val token1 = "token-100"
			val token2 = "token-200"

			whenever(redisFactoryProvider.generateSessionKey(userId1)).thenReturn(key1)
			whenever(redisFactoryProvider.generateSessionKey(userId2)).thenReturn(key2)
			whenever(redisFactoryProvider.get(key1)).thenReturn(token1)
			whenever(redisFactoryProvider.get(key2)).thenReturn(token2)

			val result1 = tokenProvider.getRefreshToken(userId1)
			val result2 = tokenProvider.getRefreshToken(userId2)

			assertEquals(token1, result1)
			assertEquals(token2, result2)
			assertNotEquals(result1, result2)
		}

		@ParameterizedTest
		@ValueSource(longs = [0, 1, 100, 1000, Long.MAX_VALUE])
		@DisplayName("should handle various user ID values")
		fun `should handle various user ID values`(userId: Long) {
			val expectedKey = "Session_$userId"
			val expectedToken = "token-$userId"

			whenever(redisFactoryProvider.generateSessionKey(userId)).thenReturn(expectedKey)
			whenever(redisFactoryProvider.get(expectedKey)).thenReturn(expectedToken)

			val token = tokenProvider.getRefreshToken(userId)

			assertEquals(expectedToken, token)
		}
	}

	@Nested
	@DisplayName("Delete Refresh Token Tests")
	inner class DeleteRefreshTokenTests {
		@Test
		@DisplayName("should delete refresh token from Redis")
		fun `should delete refresh token`() {
			val userId = 1L
			val expectedKey = "Session_$userId"

			whenever(redisFactoryProvider.generateSessionKey(userId)).thenReturn(expectedKey)

			tokenProvider.deleteRefreshToken(userId)

			verify(redisFactoryProvider).generateSessionKey(userId)
			verify(redisFactoryProvider).delete(expectedKey)
		}

		@Test
		@DisplayName("should delete only specified user's token")
		fun `should delete only specified user token`() {
			val userId1 = 10L
			val userId2 = 20L
			val key1 = "Session_$userId1"

			whenever(redisFactoryProvider.generateSessionKey(userId1)).thenReturn(key1)

			tokenProvider.deleteRefreshToken(userId1)

			verify(redisFactoryProvider).generateSessionKey(userId1)
			verify(redisFactoryProvider).delete(key1)
			verify(redisFactoryProvider, never()).generateSessionKey(userId2)
		}

		@Test
		@DisplayName("should handle deletion of non-existent token gracefully")
		fun `should handle deletion of non-existent token`() {
			val userId = 999L
			val expectedKey = "Session_$userId"

			whenever(redisFactoryProvider.generateSessionKey(userId)).thenReturn(expectedKey)

			tokenProvider.deleteRefreshToken(userId)

			verify(redisFactoryProvider).generateSessionKey(userId)
			verify(redisFactoryProvider).delete(expectedKey)
		}

		@Test
		@DisplayName("should be idempotent")
		fun `should be idempotent`() {
			val userId = 1L
			val expectedKey = "Session_$userId"

			whenever(redisFactoryProvider.generateSessionKey(userId)).thenReturn(expectedKey)

			tokenProvider.deleteRefreshToken(userId)
			tokenProvider.deleteRefreshToken(userId)

			verify(redisFactoryProvider, times(2)).generateSessionKey(userId)
			verify(redisFactoryProvider, times(2)).delete(expectedKey)
		}
	}

	@Nested
	@DisplayName("Create Refresh Token Tests")
	inner class CreateRefreshTokenTests {
		@BeforeEach
		fun setUpCreateRefreshTokenTests() {
			whenever(jwtProvider.refreshExpireTime).thenReturn(refreshExpireTime)
		}

		@Test
		@DisplayName("should create refresh token and store in Redis")
		fun `should create and store refresh token`() {
			val user = createUser()
			val expectedKey = "Session_${user.id}"
			val refreshToken = "new-refresh-token"
			val securityUserItem = SecurityUserItem.from(user)

			whenever(redisFactoryProvider.generateSessionKey(user.id)).thenReturn(expectedKey)
			whenever(
				jwtProvider.createRefreshToken(
					argThat {
						this.userId == securityUserItem.userId &&
							this.email == securityUserItem.email
					}
				)
			).thenReturn(refreshToken)

			tokenProvider.createRefreshToken(user)

			verify(redisFactoryProvider).generateSessionKey(user.id)
			verify(jwtProvider).createRefreshToken(any<SecurityUserItem>())
			verify(redisFactoryProvider).set(
				expectedKey,
				refreshToken,
				refreshExpireTime,
				TimeUnit.SECONDS
			)
		}

		@Test
		@DisplayName("should overwrite existing token for same user")
		fun `should overwrite existing token`() {
			val user = createUser()
			val expectedKey = "Session_${user.id}"
			val firstToken = "first-refresh-token"
			val secondToken = "second-refresh-token"

			whenever(redisFactoryProvider.generateSessionKey(user.id)).thenReturn(expectedKey)
			whenever(jwtProvider.createRefreshToken(any<SecurityUserItem>()))
				.thenReturn(firstToken)
				.thenReturn(secondToken)

			tokenProvider.createRefreshToken(user)
			tokenProvider.createRefreshToken(user)

			verify(jwtProvider, times(2)).createRefreshToken(any<SecurityUserItem>())
			verify(redisFactoryProvider, times(2)).set(
				eq(expectedKey),
				any<String>(),
				eq(refreshExpireTime),
				eq(TimeUnit.SECONDS)
			)
		}

		@Test
		@DisplayName("should handle users with different roles")
		fun `should handle different user roles`() {
			val adminUser = createUser(id = 1L, role = UserRole.ADMIN)
			val normalUser = createUser(id = 2L, role = UserRole.USER)

			val adminKey = "Session_${adminUser.id}"
			val userKey = "Session_${normalUser.id}"
			val adminToken = "admin-token"
			val userToken = "user-token"

			whenever(redisFactoryProvider.generateSessionKey(adminUser.id)).thenReturn(adminKey)
			whenever(redisFactoryProvider.generateSessionKey(normalUser.id)).thenReturn(userKey)
			whenever(
				jwtProvider.createRefreshToken(
					argThat {
						this.role == UserRole.ADMIN
					}
				)
			).thenReturn(adminToken)
			whenever(
				jwtProvider.createRefreshToken(
					argThat {
						this.role == UserRole.USER
					}
				)
			).thenReturn(userToken)

			tokenProvider.createRefreshToken(adminUser)
			tokenProvider.createRefreshToken(normalUser)

			verify(redisFactoryProvider).set(adminKey, adminToken, refreshExpireTime, TimeUnit.SECONDS)
			verify(redisFactoryProvider).set(userKey, userToken, refreshExpireTime, TimeUnit.SECONDS)
		}
	}

	@Nested
	@DisplayName("Create Access Token Tests")
	inner class CreateAccessTokenTests {
		@Test
		@DisplayName("should create access token for user")
		fun `should create access token`() {
			val user = createUser()
			val expectedToken = "access-token"
			val securityUserItem = SecurityUserItem.from(user)

			whenever(
				jwtProvider.createAccessToken(
					argThat {
						this.userId == securityUserItem.userId
					}
				)
			).thenReturn(expectedToken)

			val token = tokenProvider.createAccessToken(user)

			assertEquals(expectedToken, token)
			verify(jwtProvider).createAccessToken(any<SecurityUserItem>())
		}

		@Test
		@DisplayName("should create different tokens for different users")
		fun `should create different tokens for different users`() {
			val user1 = createUser(id = 1L, email = "user1@example.com")
			val user2 = createUser(id = 2L, email = "user2@example.com")
			val token1 = "token-user-1"
			val token2 = "token-user-2"

			whenever(jwtProvider.createAccessToken(argThat { this.userId == 1L })).thenReturn(token1)
			whenever(jwtProvider.createAccessToken(argThat { this.userId == 2L })).thenReturn(token2)

			val result1 = tokenProvider.createAccessToken(user1)
			val result2 = tokenProvider.createAccessToken(user2)

			assertEquals(token1, result1)
			assertEquals(token2, result2)
			assertNotEquals(result1, result2)
		}

		@Test
		@DisplayName("should handle users with different roles correctly")
		fun `should handle users with different roles`() {
			val adminUser = createUser(role = UserRole.ADMIN)
			val normalUser = createUser(role = UserRole.USER)
			val adminToken = "admin-access-token"
			val userToken = "user-access-token"

			whenever(jwtProvider.createAccessToken(argThat { this.role == UserRole.ADMIN }))
				.thenReturn(adminToken)
			whenever(jwtProvider.createAccessToken(argThat { this.role == UserRole.USER }))
				.thenReturn(userToken)

			val adminResult = tokenProvider.createAccessToken(adminUser)
			val userResult = tokenProvider.createAccessToken(normalUser)

			assertEquals(adminToken, adminResult)
			assertEquals(userToken, userResult)
		}
	}

	@Nested
	@DisplayName("Refresh Access Token Tests")
	inner class RefreshAccessTokenTests {
		@Test
		@DisplayName("should refresh access token successfully")
		fun `should refresh access token`() {
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

			whenever(redisFactoryProvider.generateSessionKey(securityUserItem.userId)).thenReturn(redisKey)
			whenever(redisFactoryProvider.get(redisKey)).thenReturn(refreshToken)
			whenever(jwtProvider.refreshAccessToken(securityUserItem, refreshToken)).thenReturn(newAccessToken)

			val token = tokenProvider.refreshAccessToken(securityUserItem)

			assertEquals(newAccessToken, token)
			verify(redisFactoryProvider).generateSessionKey(securityUserItem.userId)
			verify(redisFactoryProvider).get(redisKey)
			verify(jwtProvider).refreshAccessToken(securityUserItem, refreshToken)
		}

		@Test
		@DisplayName("should throw exception when refresh token not found")
		fun `should throw exception when refresh token not found`() {
			val securityUserItem =
				SecurityUserItem(
					userId = 2L,
					role = UserRole.USER,
					name = "Test User",
					email = "test@example.com"
				)
			val redisKey = "Session_${securityUserItem.userId}"

			whenever(redisFactoryProvider.generateSessionKey(securityUserItem.userId)).thenReturn(redisKey)
			whenever(redisFactoryProvider.get(redisKey)).thenReturn(null)

			assertThrows<RefreshTokenNotFoundException> {
				tokenProvider.refreshAccessToken(securityUserItem)
			}

			verify(jwtProvider, never()).refreshAccessToken(any<SecurityUserItem>(), any<String>())
		}

		@Test
		@DisplayName("should handle different security user items")
		fun `should handle different security user items`() {
			val userItem = SecurityUserItem(1L, UserRole.USER, "User", "user@example.com")
			val adminItem = SecurityUserItem(2L, UserRole.ADMIN, "Admin", "admin@example.com")

			val userRefreshToken = "user-refresh"
			val adminRefreshToken = "admin-refresh"
			val userAccessToken = "user-access"
			val adminAccessToken = "admin-access"

			whenever(redisFactoryProvider.generateSessionKey(1L)).thenReturn("Session_1")
			whenever(redisFactoryProvider.generateSessionKey(2L)).thenReturn("Session_2")
			whenever(redisFactoryProvider.get("Session_1")).thenReturn(userRefreshToken)
			whenever(redisFactoryProvider.get("Session_2")).thenReturn(adminRefreshToken)
			whenever(jwtProvider.refreshAccessToken(userItem, userRefreshToken)).thenReturn(userAccessToken)
			whenever(jwtProvider.refreshAccessToken(adminItem, adminRefreshToken)).thenReturn(adminAccessToken)

			val userResult = tokenProvider.refreshAccessToken(userItem)
			val adminResult = tokenProvider.refreshAccessToken(adminItem)

			assertEquals(userAccessToken, userResult)
			assertEquals(adminAccessToken, adminResult)
		}
	}

	@Nested
	@DisplayName("Create Full Tokens Tests")
	inner class CreateFullTokensTests {
		@BeforeEach
		fun setUpCreateFullTokensTests() {
			whenever(jwtProvider.refreshExpireTime).thenReturn(refreshExpireTime)
		}

		@Test
		@DisplayName("should create both refresh and access tokens")
		fun `should create full tokens`() {
			val user = createUser()
			val expectedKey = "Session_${user.id}"
			val refreshToken = "refresh-token"
			val accessToken = "access-token"

			whenever(redisFactoryProvider.generateSessionKey(user.id)).thenReturn(expectedKey)
			whenever(jwtProvider.createRefreshToken(any<SecurityUserItem>())).thenReturn(refreshToken)
			whenever(jwtProvider.createAccessToken(any<SecurityUserItem>())).thenReturn(accessToken)

			val token = tokenProvider.createFullTokens(user)

			assertEquals(accessToken, token)
			verify(redisFactoryProvider).generateSessionKey(user.id)
			verify(jwtProvider).createRefreshToken(any<SecurityUserItem>())
			verify(jwtProvider).createAccessToken(any<SecurityUserItem>())
			verify(redisFactoryProvider).set(expectedKey, refreshToken, refreshExpireTime, TimeUnit.SECONDS)
		}

		@Test
		@DisplayName("should return only access token but store refresh token")
		fun `should return access token only`() {
			val user = createUser()
			val refreshToken = "refresh-token-not-returned"
			val accessToken = "access-token-returned"

			whenever(redisFactoryProvider.generateSessionKey(user.id)).thenReturn("Session_${user.id}")
			whenever(jwtProvider.createRefreshToken(any<SecurityUserItem>())).thenReturn(refreshToken)
			whenever(jwtProvider.createAccessToken(any<SecurityUserItem>())).thenReturn(accessToken)

			val returnedToken = tokenProvider.createFullTokens(user)

			assertEquals(accessToken, returnedToken)
			assertNotEquals(refreshToken, returnedToken)
		}

		@Test
		@DisplayName("should handle multiple users creating full tokens")
		fun `should handle multiple users`() {
			val user1 = createUser(id = 1L, email = "user1@example.com")
			val user2 = createUser(id = 2L, email = "user2@example.com")

			whenever(redisFactoryProvider.generateSessionKey(any<Long>())).thenAnswer {
				"Session_${it.arguments[0]}"
			}
			whenever(jwtProvider.createRefreshToken(any<SecurityUserItem>())).thenReturn("refresh-token")
			whenever(jwtProvider.createAccessToken(argThat { this.userId == 1L }))
				.thenReturn("access-token-1")
			whenever(jwtProvider.createAccessToken(argThat { this.userId == 2L }))
				.thenReturn("access-token-2")

			val token1 = tokenProvider.createFullTokens(user1)
			val token2 = tokenProvider.createFullTokens(user2)

			assertEquals("access-token-1", token1)
			assertEquals("access-token-2", token2)
			verify(jwtProvider, times(2)).createRefreshToken(any<SecurityUserItem>())
			verify(jwtProvider, times(2)).createAccessToken(any<SecurityUserItem>())
			verify(redisFactoryProvider, times(2)).set(any<String>(), any<String>(), eq(refreshExpireTime), eq(TimeUnit.SECONDS))
		}

		@Test
		@DisplayName("should execute operations in correct order")
		fun `should execute operations in correct order`() {
			val user = createUser()
			val refreshToken = "refresh-token"
			val accessToken = "access-token"

			whenever(redisFactoryProvider.generateSessionKey(user.id)).thenReturn("Session_${user.id}")
			whenever(jwtProvider.createRefreshToken(any<SecurityUserItem>())).thenReturn(refreshToken)
			whenever(jwtProvider.createAccessToken(any<SecurityUserItem>())).thenReturn(accessToken)

			val token = tokenProvider.createFullTokens(user)

			val inOrder = inOrder(redisFactoryProvider, jwtProvider)

			inOrder.verify(redisFactoryProvider).generateSessionKey(user.id)
			inOrder.verify(jwtProvider).createRefreshToken(any<SecurityUserItem>())
			inOrder.verify(redisFactoryProvider).set(any<String>(), any<String>(), any<Long>(), any<TimeUnit>())

			inOrder.verify(jwtProvider).createAccessToken(any<SecurityUserItem>())

			assertEquals(accessToken, token)
		}
	}

	@Nested
	@DisplayName("Edge Cases Tests")
	inner class EdgeCaseTests {
		@Test
		@DisplayName("should handle very large user IDs")
		fun `should handle very large user IDs`() {
			val largeUserId = Long.MAX_VALUE
			val expectedKey = "Session_$largeUserId"
			val token = "token-for-max-id"

			whenever(redisFactoryProvider.generateSessionKey(largeUserId)).thenReturn(expectedKey)
			whenever(redisFactoryProvider.get(expectedKey)).thenReturn(token)

			val result = tokenProvider.getRefreshToken(largeUserId)

			assertEquals(token, result)
		}

		@Test
		@DisplayName("should handle negative user IDs")
		fun `should handle negative user IDs`() {
			val negativeUserId = -1L
			val expectedKey = "Session_$negativeUserId"

			whenever(redisFactoryProvider.generateSessionKey(negativeUserId)).thenReturn(expectedKey)

			tokenProvider.deleteRefreshToken(negativeUserId)

			verify(redisFactoryProvider).delete(expectedKey)
		}

		@Test
		@DisplayName("should handle very long token strings")
		fun `should handle very long token strings`() {
			val user = createUser()
			val longToken = "t".repeat(1000)

			whenever(jwtProvider.refreshExpireTime).thenReturn(refreshExpireTime)
			whenever(redisFactoryProvider.generateSessionKey(user.id)).thenReturn("Session_${user.id}")
			whenever(jwtProvider.createRefreshToken(any<SecurityUserItem>())).thenReturn(longToken)

			tokenProvider.createRefreshToken(user)

			verify(redisFactoryProvider).set(any<String>(), eq(longToken), any<Long>(), any<TimeUnit>())
		}

		@Test
		@DisplayName("should handle empty token string")
		fun `should handle empty token string`() {
			val userId = 1L
			val emptyToken = ""
			val expectedKey = "Session_$userId"

			whenever(redisFactoryProvider.generateSessionKey(userId)).thenReturn(expectedKey)
			whenever(redisFactoryProvider.get(expectedKey)).thenReturn(emptyToken)

			val result = tokenProvider.getRefreshToken(userId)

			assertEquals(emptyToken, result)
		}

		@ParameterizedTest
		@ValueSource(longs = [-100, -1, 0, 1, 100, 1000, Long.MAX_VALUE])
		@DisplayName("should handle various edge case user IDs")
		fun `should handle various edge case user IDs`(userId: Long) {
			val expectedKey = "Session_$userId"

			whenever(redisFactoryProvider.generateSessionKey(userId)).thenReturn(expectedKey)

			tokenProvider.deleteRefreshToken(userId)

			verify(redisFactoryProvider).delete(expectedKey)
		}
	}

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
}
