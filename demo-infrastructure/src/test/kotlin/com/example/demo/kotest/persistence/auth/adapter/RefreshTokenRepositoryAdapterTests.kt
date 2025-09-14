package com.example.demo.kotest.persistence.auth.adapter

import com.example.demo.persistence.auth.adapter.RefreshTokenRepositoryAdapter
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ValueOperations
import org.springframework.test.context.ActiveProfiles
import java.util.concurrent.TimeUnit

@ActiveProfiles("test")
@Tags("kotest-unit-test")
class RefreshTokenRepositoryAdapterTests :
	FunSpec({

		lateinit var redisTemplate: RedisTemplate<String, String>
		lateinit var valueOperations: ValueOperations<String, String>
		lateinit var refreshTokenRepositoryAdapter: RefreshTokenRepositoryAdapter

		beforeTest {
			redisTemplate = mockk()
			valueOperations = mockk()
			every { redisTemplate.opsForValue() } returns valueOperations
			refreshTokenRepositoryAdapter = RefreshTokenRepositoryAdapter(redisTemplate)
		}

		context("Save Refresh Token") {
			test("should save refresh token with expiration") {
				val userId = 1L
				val token = "test-refresh-token"
				val expiresIn = 86400000L
				val expectedKey = "refresh_token:$userId"

				every {
					valueOperations.set(expectedKey, token, expiresIn, TimeUnit.MILLISECONDS)
				} just runs

				refreshTokenRepositoryAdapter.save(userId, token, expiresIn)

				verify(exactly = 1) {
					valueOperations.set(expectedKey, token, expiresIn, TimeUnit.MILLISECONDS)
				}
			}

			test("should overwrite existing token for same user") {
				val userId = 1L
				val oldToken = "old-refresh-token"
				val newToken = "new-refresh-token"
				val expiresIn = 86400000L
				val expectedKey = "refresh_token:$userId"

				every {
					valueOperations.set(expectedKey, any<String>(), any<Long>(), any<TimeUnit>())
				} just runs

				refreshTokenRepositoryAdapter.save(userId, oldToken, expiresIn)
				refreshTokenRepositoryAdapter.save(userId, newToken, expiresIn)

				verify(exactly = 2) {
					valueOperations.set(expectedKey, any<String>(), expiresIn, TimeUnit.MILLISECONDS)
				}
			}

			test("should handle different expiration times") {
				val userId = 1L
				val token = "test-token"
				val shortExpiration = 3600000L
				val longExpiration = 604800000L
				val expectedKey = "refresh_token:$userId"

				every {
					valueOperations.set(expectedKey, token, any<Long>(), TimeUnit.MILLISECONDS)
				} just runs

				refreshTokenRepositoryAdapter.save(userId, token, shortExpiration)

				verify {
					valueOperations.set(expectedKey, token, shortExpiration, TimeUnit.MILLISECONDS)
				}

				refreshTokenRepositoryAdapter.save(userId, token, longExpiration)

				verify {
					valueOperations.set(expectedKey, token, longExpiration, TimeUnit.MILLISECONDS)
				}
			}
		}

		context("Find Refresh Token by User ID") {
			test("should return token when exists") {
				val userId = 1L
				val expectedToken = "existing-refresh-token"
				val expectedKey = "refresh_token:$userId"

				every { valueOperations.get(expectedKey) } returns expectedToken

				val token = refreshTokenRepositoryAdapter.findByUserId(userId)

				token shouldBe expectedToken
				verify(exactly = 1) { valueOperations.get(expectedKey) }
			}

			test("should return null when token does not exist") {
				val userId = 999L
				val expectedKey = "refresh_token:$userId"

				every { valueOperations.get(expectedKey) } returns null

				val token = refreshTokenRepositoryAdapter.findByUserId(userId)

				token shouldBe null
				verify(exactly = 1) { valueOperations.get(expectedKey) }
			}

			test("should handle different user IDs") {
				val userId1 = 1L
				val userId2 = 2L
				val token1 = "token-for-user-1"
				val token2 = "token-for-user-2"

				every { valueOperations.get("refresh_token:$userId1") } returns token1
				every { valueOperations.get("refresh_token:$userId2") } returns token2

				val result1 = refreshTokenRepositoryAdapter.findByUserId(userId1)
				val result2 = refreshTokenRepositoryAdapter.findByUserId(userId2)

				result1 shouldBe token1
				result2 shouldBe token2
				result1 shouldNotBe result2
			}
		}

		context("Delete Refresh Token by User ID") {
			test("should delete token for user") {
				val userId = 1L
				val expectedKey = "refresh_token:$userId"

				every { redisTemplate.delete(expectedKey) } returns true

				refreshTokenRepositoryAdapter.deleteByUserId(userId)

				verify(exactly = 1) { redisTemplate.delete(expectedKey) }
			}

			test("should handle deletion of non-existent token") {
				val userId = 999L
				val expectedKey = "refresh_token:$userId"

				every { redisTemplate.delete(expectedKey) } returns false

				refreshTokenRepositoryAdapter.deleteByUserId(userId)

				verify(exactly = 1) { redisTemplate.delete(expectedKey) }
			}

			test("should delete only specified user's token") {
				val userId1 = 1L
				val userId2 = 2L
				val expectedKey1 = "refresh_token:$userId1"

				every { redisTemplate.delete(expectedKey1) } returns true

				refreshTokenRepositoryAdapter.deleteByUserId(userId1)

				verify(exactly = 1) { redisTemplate.delete(expectedKey1) }
				verify(exactly = 0) { redisTemplate.delete("refresh_token:$userId2") }
			}
		}

		context("Validate Refresh Token") {
			test("should return true when token matches") {
				val userId = 1L
				val token = "valid-refresh-token"
				val expectedKey = "refresh_token:$userId"

				every { valueOperations.get(expectedKey) } returns token

				val isValid = refreshTokenRepositoryAdapter.validateToken(userId, token)

				isValid shouldBe true
				verify(exactly = 1) { valueOperations.get(expectedKey) }
			}

			test("should return false when token does not match") {
				val userId = 1L
				val providedToken = "wrong-token"
				val storedToken = "correct-token"
				val expectedKey = "refresh_token:$userId"

				every { valueOperations.get(expectedKey) } returns storedToken

				val isValid = refreshTokenRepositoryAdapter.validateToken(userId, providedToken)

				isValid shouldBe false
				verify(exactly = 1) { valueOperations.get(expectedKey) }
			}

			test("should return false when no token exists") {
				val userId = 999L
				val token = "some-token"
				val expectedKey = "refresh_token:$userId"

				every { valueOperations.get(expectedKey) } returns null

				val isValid = refreshTokenRepositoryAdapter.validateToken(userId, token)

				isValid shouldBe false
				verify(exactly = 1) { valueOperations.get(expectedKey) }
			}

			test("should handle case sensitivity") {
				val userId = 1L
				val storedToken = "CaseSensitiveToken123"
				val wrongCaseToken = "casesensitivetoken123"
				val expectedKey = "refresh_token:$userId"

				every { valueOperations.get(expectedKey) } returns storedToken

				val isValid = refreshTokenRepositoryAdapter.validateToken(userId, wrongCaseToken)

				isValid shouldBe false
			}
		}

		context("Edge Cases") {
			test("should handle very long token strings") {
				val userId = 1L
				val longToken = "a".repeat(1000)
				val expiresIn = 86400000L
				val expectedKey = "refresh_token:$userId"

				every {
					valueOperations.set(expectedKey, longToken, expiresIn, TimeUnit.MILLISECONDS)
				} just runs
				every { valueOperations.get(expectedKey) } returns longToken

				refreshTokenRepositoryAdapter.save(userId, longToken, expiresIn)
				val retrievedToken = refreshTokenRepositoryAdapter.findByUserId(userId)

				retrievedToken shouldBe longToken
				retrievedToken?.length shouldBe 1000
			}

			test("should handle zero expiration time") {
				val userId = 1L
				val token = "immediate-expire-token"
				val expiresIn = 0L
				val expectedKey = "refresh_token:$userId"

				every {
					valueOperations.set(expectedKey, token, expiresIn, TimeUnit.MILLISECONDS)
				} just runs

				refreshTokenRepositoryAdapter.save(userId, token, expiresIn)

				verify {
					valueOperations.set(expectedKey, token, 0L, TimeUnit.MILLISECONDS)
				}
			}

			test("should handle negative user IDs") {
				val negativeUserId = -1L
				val token = "token-for-negative-id"
				val expiresIn = 86400000L
				val expectedKey = "refresh_token:$negativeUserId"

				every {
					valueOperations.set(expectedKey, token, expiresIn, TimeUnit.MILLISECONDS)
				} just runs
				every { valueOperations.get(expectedKey) } returns token

				refreshTokenRepositoryAdapter.save(negativeUserId, token, expiresIn)
				val retrievedToken = refreshTokenRepositoryAdapter.findByUserId(negativeUserId)

				retrievedToken shouldBe token
			}

			test("should handle very large user IDs") {
				val largeUserId = Long.MAX_VALUE
				val token = "token-for-max-id"
				val expiresIn = 86400000L
				val expectedKey = "refresh_token:$largeUserId"

				every {
					valueOperations.set(expectedKey, token, expiresIn, TimeUnit.MILLISECONDS)
				} just runs
				every { valueOperations.get(expectedKey) } returns token

				refreshTokenRepositoryAdapter.save(largeUserId, token, expiresIn)
				val retrievedToken = refreshTokenRepositoryAdapter.findByUserId(largeUserId)

				retrievedToken shouldBe token
			}
		}
	})
