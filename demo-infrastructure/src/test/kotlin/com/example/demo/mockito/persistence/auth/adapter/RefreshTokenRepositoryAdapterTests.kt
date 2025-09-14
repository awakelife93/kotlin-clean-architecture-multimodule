package com.example.demo.mockito.persistence.auth.adapter

import com.example.demo.persistence.auth.adapter.RefreshTokenRepositoryAdapter
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ValueOperations
import org.springframework.test.context.ActiveProfiles
import java.util.concurrent.TimeUnit

@ActiveProfiles("test")
@Tag("mockito-unit-test")
@DisplayName("Mockito Unit - Refresh Token Repository Adapter Test")
@ExtendWith(MockitoExtension::class)
class RefreshTokenRepositoryAdapterTests {
	@Mock
	private lateinit var redisTemplate: RedisTemplate<String, String>

	@Mock
	private lateinit var valueOperations: ValueOperations<String, String>

	private lateinit var refreshTokenRepositoryAdapter: RefreshTokenRepositoryAdapter

	@BeforeEach
	fun setUp() {
		refreshTokenRepositoryAdapter = RefreshTokenRepositoryAdapter(redisTemplate)
	}

	@Nested
	@DisplayName("Save Refresh Token Tests")
	inner class SaveTests {
		@BeforeEach
		fun setUpSaveTests() {
			whenever(redisTemplate.opsForValue()).thenReturn(valueOperations)
		}

		@Test
		@DisplayName("should save refresh token with expiration")
		fun `should save refresh token with expiration`() {
			val userId = 1L
			val token = "test-refresh-token"
			val expiresIn = 86400000L
			val expectedKey = "refresh_token:$userId"

			refreshTokenRepositoryAdapter.save(userId, token, expiresIn)

			verify(valueOperations).set(expectedKey, token, expiresIn, TimeUnit.MILLISECONDS)
		}

		@Test
		@DisplayName("should overwrite existing token for same user")
		fun `should overwrite existing token for same user`() {
			val userId = 1L
			val oldToken = "old-refresh-token"
			val newToken = "new-refresh-token"
			val expiresIn = 86400000L
			val expectedKey = "refresh_token:$userId"

			refreshTokenRepositoryAdapter.save(userId, oldToken, expiresIn)
			refreshTokenRepositoryAdapter.save(userId, newToken, expiresIn)

			verify(valueOperations, times(2)).set(
				eq(expectedKey),
				any<String>(),
				eq(expiresIn),
				eq(TimeUnit.MILLISECONDS)
			)
		}

		@ParameterizedTest
		@ValueSource(longs = [3600000, 86400000, 604800000])
		@DisplayName("should handle different expiration times")
		fun `should handle different expiration times`(expiresIn: Long) {
			val userId = 1L
			val token = "test-token"
			val expectedKey = "refresh_token:$userId"

			refreshTokenRepositoryAdapter.save(userId, token, expiresIn)

			verify(valueOperations).set(expectedKey, token, expiresIn, TimeUnit.MILLISECONDS)
		}

		@Test
		@DisplayName("should save tokens for multiple users")
		fun `should save tokens for multiple users`() {
			val userId1 = 1L
			val userId2 = 2L
			val token1 = "token-user-1"
			val token2 = "token-user-2"
			val expiresIn = 86400000L

			refreshTokenRepositoryAdapter.save(userId1, token1, expiresIn)
			refreshTokenRepositoryAdapter.save(userId2, token2, expiresIn)

			verify(valueOperations).set("refresh_token:$userId1", token1, expiresIn, TimeUnit.MILLISECONDS)
			verify(valueOperations).set("refresh_token:$userId2", token2, expiresIn, TimeUnit.MILLISECONDS)
		}
	}

	@Nested
	@DisplayName("Find Refresh Token by User ID Tests")
	inner class FindByUserIdTests {
		@BeforeEach
		fun setUpFindTests() {
			whenever(redisTemplate.opsForValue()).thenReturn(valueOperations)
		}

		@Test
		@DisplayName("should return token when exists")
		fun `should return token when exists`() {
			val userId = 1L
			val expectedToken = "existing-refresh-token"
			val expectedKey = "refresh_token:$userId"

			whenever(valueOperations.get(expectedKey)).thenReturn(expectedToken)

			val token = refreshTokenRepositoryAdapter.findByUserId(userId)

			assertEquals(expectedToken, token)
			verify(valueOperations).get(expectedKey)
		}

		@Test
		@DisplayName("should return null when token does not exist")
		fun `should return null when token does not exist`() {
			val userId = 999L
			val expectedKey = "refresh_token:$userId"

			whenever(valueOperations.get(expectedKey)).thenReturn(null)

			val token = refreshTokenRepositoryAdapter.findByUserId(userId)

			assertNull(token)
			verify(valueOperations).get(expectedKey)
		}

		@Test
		@DisplayName("should handle different user IDs")
		fun `should handle different user IDs`() {
			val userId1 = 1L
			val userId2 = 2L
			val token1 = "token-for-user-1"
			val token2 = "token-for-user-2"

			whenever(valueOperations.get("refresh_token:$userId1")).thenReturn(token1)
			whenever(valueOperations.get("refresh_token:$userId2")).thenReturn(token2)

			val result1 = refreshTokenRepositoryAdapter.findByUserId(userId1)
			val result2 = refreshTokenRepositoryAdapter.findByUserId(userId2)

			assertEquals(token1, result1)
			assertEquals(token2, result2)
			verify(valueOperations).get("refresh_token:$userId1")
			verify(valueOperations).get("refresh_token:$userId2")
		}

		@Test
		@DisplayName("should not cache results")
		fun `should not cache results`() {
			val userId = 1L
			val expectedKey = "refresh_token:$userId"
			val firstToken = "first-token"
			val secondToken = "second-token"

			whenever(valueOperations.get(expectedKey))
				.thenReturn(firstToken)
				.thenReturn(secondToken)

			val result1 = refreshTokenRepositoryAdapter.findByUserId(userId)
			val result2 = refreshTokenRepositoryAdapter.findByUserId(userId)

			assertEquals(firstToken, result1)
			assertEquals(secondToken, result2)
			verify(valueOperations, times(2)).get(expectedKey)
		}
	}

	@Nested
	@DisplayName("Delete Refresh Token by User ID Tests")
	inner class DeleteByUserIdTests {
		@Test
		@DisplayName("should delete token for user")
		fun `should delete token for user`() {
			val userId = 1L
			val expectedKey = "refresh_token:$userId"

			whenever(redisTemplate.delete(expectedKey)).thenReturn(true)

			refreshTokenRepositoryAdapter.deleteByUserId(userId)

			verify(redisTemplate).delete(expectedKey)
		}

		@Test
		@DisplayName("should handle deletion of non-existent token")
		fun `should handle deletion of non-existent token`() {
			val userId = 999L
			val expectedKey = "refresh_token:$userId"

			whenever(redisTemplate.delete(expectedKey)).thenReturn(false)

			refreshTokenRepositoryAdapter.deleteByUserId(userId)

			verify(redisTemplate).delete(expectedKey)
		}

		@Test
		@DisplayName("should delete only specified user's token")
		fun `should delete only specified user token`() {
			val userId1 = 1L
			val userId2 = 2L
			val expectedKey1 = "refresh_token:$userId1"
			val expectedKey2 = "refresh_token:$userId2"

			whenever(redisTemplate.delete(expectedKey1)).thenReturn(true)

			refreshTokenRepositoryAdapter.deleteByUserId(userId1)

			verify(redisTemplate).delete(expectedKey1)
			verify(redisTemplate, never()).delete(expectedKey2)
		}

		@Test
		@DisplayName("should be idempotent")
		fun `should be idempotent`() {
			val userId = 1L
			val expectedKey = "refresh_token:$userId"

			whenever(redisTemplate.delete(expectedKey))
				.thenReturn(true)
				.thenReturn(false)

			refreshTokenRepositoryAdapter.deleteByUserId(userId)
			refreshTokenRepositoryAdapter.deleteByUserId(userId)

			verify(redisTemplate, times(2)).delete(expectedKey)
		}
	}

	@Nested
	@DisplayName("Validate Refresh Token Tests")
	inner class ValidateTokenTests {
		@BeforeEach
		fun setUpValidateTests() {
			whenever(redisTemplate.opsForValue()).thenReturn(valueOperations)
		}

		@Test
		@DisplayName("should return true when token matches")
		fun `should return true when token matches`() {
			val userId = 1L
			val token = "valid-refresh-token"
			val expectedKey = "refresh_token:$userId"

			whenever(valueOperations.get(expectedKey)).thenReturn(token)

			val isValid = refreshTokenRepositoryAdapter.validateToken(userId, token)

			assertTrue(isValid)
			verify(valueOperations).get(expectedKey)
		}

		@Test
		@DisplayName("should return false when token does not match")
		fun `should return false when token does not match`() {
			val userId = 1L
			val providedToken = "wrong-token"
			val storedToken = "correct-token"
			val expectedKey = "refresh_token:$userId"

			whenever(valueOperations.get(expectedKey)).thenReturn(storedToken)

			val isValid = refreshTokenRepositoryAdapter.validateToken(userId, providedToken)

			assertFalse(isValid)
			verify(valueOperations).get(expectedKey)
		}

		@Test
		@DisplayName("should return false when no token exists")
		fun `should return false when no token exists`() {
			val userId = 999L
			val token = "some-token"
			val expectedKey = "refresh_token:$userId"

			whenever(valueOperations.get(expectedKey)).thenReturn(null)

			val isValid = refreshTokenRepositoryAdapter.validateToken(userId, token)

			assertFalse(isValid)
			verify(valueOperations).get(expectedKey)
		}

		@Test
		@DisplayName("should handle case sensitivity")
		fun `should handle case sensitivity`() {
			val userId = 1L
			val storedToken = "CaseSensitiveToken123"
			val wrongCaseToken = "casesensitivetoken123"
			val expectedKey = "refresh_token:$userId"

			whenever(valueOperations.get(expectedKey)).thenReturn(storedToken)

			val isValid = refreshTokenRepositoryAdapter.validateToken(userId, wrongCaseToken)

			assertFalse(isValid)
		}

		@Test
		@DisplayName("should handle whitespace differences")
		fun `should handle whitespace differences`() {
			val userId = 1L
			val storedToken = "token-without-spaces"
			val tokenWithSpaces = " token-without-spaces "
			val expectedKey = "refresh_token:$userId"

			whenever(valueOperations.get(expectedKey)).thenReturn(storedToken)

			val isValid = refreshTokenRepositoryAdapter.validateToken(userId, tokenWithSpaces)

			assertFalse(isValid)
		}
	}

	@Nested
	@DisplayName("Edge Cases Tests")
	inner class EdgeCaseTests {
		@BeforeEach
		fun setUpEdgeCaseTests() {
			whenever(redisTemplate.opsForValue()).thenReturn(valueOperations)
		}

		@Test
		@DisplayName("should handle very long token strings")
		fun `should handle very long token strings`() {
			val userId = 1L
			val longToken = "a".repeat(1000)
			val expiresIn = 86400000L
			val expectedKey = "refresh_token:$userId"

			whenever(valueOperations.get(expectedKey)).thenReturn(longToken)

			refreshTokenRepositoryAdapter.save(userId, longToken, expiresIn)
			val retrievedToken = refreshTokenRepositoryAdapter.findByUserId(userId)

			assertEquals(longToken, retrievedToken)
			assertEquals(1000, retrievedToken?.length)
			verify(valueOperations).set(expectedKey, longToken, expiresIn, TimeUnit.MILLISECONDS)
		}

		@Test
		@DisplayName("should handle zero expiration time")
		fun `should handle zero expiration time`() {
			val userId = 1L
			val token = "immediate-expire-token"
			val expiresIn = 0L
			val expectedKey = "refresh_token:$userId"

			refreshTokenRepositoryAdapter.save(userId, token, expiresIn)

			verify(valueOperations).set(expectedKey, token, 0L, TimeUnit.MILLISECONDS)
		}

		@Test
		@DisplayName("should handle negative user IDs")
		fun `should handle negative user IDs`() {
			val negativeUserId = -1L
			val token = "token-for-negative-id"
			val expiresIn = 86400000L
			val expectedKey = "refresh_token:$negativeUserId"

			whenever(valueOperations.get(expectedKey)).thenReturn(token)

			refreshTokenRepositoryAdapter.save(negativeUserId, token, expiresIn)
			val retrievedToken = refreshTokenRepositoryAdapter.findByUserId(negativeUserId)

			assertEquals(token, retrievedToken)
			verify(valueOperations).set(expectedKey, token, expiresIn, TimeUnit.MILLISECONDS)
		}

		@Test
		@DisplayName("should handle very large user IDs")
		fun `should handle very large user IDs`() {
			val largeUserId = Long.MAX_VALUE
			val token = "token-for-max-id"
			val expiresIn = 86400000L
			val expectedKey = "refresh_token:$largeUserId"

			whenever(valueOperations.get(expectedKey)).thenReturn(token)

			refreshTokenRepositoryAdapter.save(largeUserId, token, expiresIn)
			val retrievedToken = refreshTokenRepositoryAdapter.findByUserId(largeUserId)

			assertEquals(token, retrievedToken)
			verify(valueOperations).set(expectedKey, token, expiresIn, TimeUnit.MILLISECONDS)
		}

		@Test
		@DisplayName("should handle empty token string")
		fun `should handle empty token string`() {
			val userId = 1L
			val emptyToken = ""
			val expiresIn = 86400000L
			val expectedKey = "refresh_token:$userId"

			whenever(valueOperations.get(expectedKey)).thenReturn(emptyToken)

			refreshTokenRepositoryAdapter.save(userId, emptyToken, expiresIn)
			val isValid = refreshTokenRepositoryAdapter.validateToken(userId, emptyToken)

			assertTrue(isValid)
			verify(valueOperations).set(expectedKey, emptyToken, expiresIn, TimeUnit.MILLISECONDS)
		}

		@ParameterizedTest
		@ValueSource(longs = [0, 1, 10, 100, 1000, Long.MAX_VALUE])
		@DisplayName("should handle various user IDs")
		fun `should handle various user IDs`(userId: Long) {
			val token = "test-token-$userId"
			val expiresIn = 86400000L
			val expectedKey = "refresh_token:$userId"

			refreshTokenRepositoryAdapter.save(userId, token, expiresIn)

			verify(valueOperations).set(expectedKey, token, expiresIn, TimeUnit.MILLISECONDS)
		}
	}

	@Nested
	@DisplayName("Concurrent Operations Tests")
	inner class ConcurrentOperationsTests {
		@BeforeEach
		fun setUpConcurrentTests() {
			whenever(redisTemplate.opsForValue()).thenReturn(valueOperations)
		}

		@Test
		@DisplayName("should handle concurrent saves for different users")
		fun `should handle concurrent saves for different users`() {
			val users = listOf(1L, 2L, 3L, 4L, 5L)
			val expiresIn = 86400000L

			users.forEach { userId ->
				val token = "token-$userId"
				refreshTokenRepositoryAdapter.save(userId, token, expiresIn)
			}

			users.forEach { userId ->
				verify(valueOperations).set(
					"refresh_token:$userId",
					"token-$userId",
					expiresIn,
					TimeUnit.MILLISECONDS
				)
			}
		}

		@Test
		@DisplayName("should handle save and delete operations")
		fun `should handle save and delete operations`() {
			val userId = 1L
			val token = "test-token"
			val expiresIn = 86400000L
			val expectedKey = "refresh_token:$userId"

			whenever(redisTemplate.delete(expectedKey)).thenReturn(true)

			refreshTokenRepositoryAdapter.save(userId, token, expiresIn)
			refreshTokenRepositoryAdapter.deleteByUserId(userId)

			verify(valueOperations).set(expectedKey, token, expiresIn, TimeUnit.MILLISECONDS)
			verify(redisTemplate).delete(expectedKey)
		}
	}
}
