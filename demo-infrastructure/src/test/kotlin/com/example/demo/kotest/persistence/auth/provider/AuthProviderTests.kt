package com.example.demo.kotest.persistence.auth.provider

import com.example.demo.persistence.auth.provider.AuthProvider
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@Tags("kotest-unit-test")
class AuthProviderTests :
	FunSpec({

		context("Ignore List Default Endpoints") {
			test("should return correct ignore list endpoints") {
				val authProvider = AuthProvider("test-api-key")

				val ignoreList = authProvider.ignoreListDefaultEndpoints()

				ignoreList shouldHaveSize 4
				ignoreList shouldContainAll
					listOf(
						"/api-docs/**",
						"/swagger-ui/**",
						"/swagger.html",
						"/actuator/**"
					)
			}

			test("should return immutable list for ignore endpoints") {
				val authProvider = AuthProvider("test-api-key")

				val firstCall = authProvider.ignoreListDefaultEndpoints()
				val secondCall = authProvider.ignoreListDefaultEndpoints()

				firstCall shouldBe secondCall
				firstCall shouldHaveSize 4
			}

			test("should contain swagger documentation endpoints") {
				val authProvider = AuthProvider("test-api-key")

				val ignoreList = authProvider.ignoreListDefaultEndpoints()

				ignoreList shouldContain "/api-docs/**"
				ignoreList shouldContain "/swagger-ui/**"
				ignoreList shouldContain "/swagger.html"
			}

			test("should contain actuator endpoints") {
				val authProvider = AuthProvider("test-api-key")

				val ignoreList = authProvider.ignoreListDefaultEndpoints()

				ignoreList shouldContain "/actuator/**"
			}
		}

		context("White List Default Endpoints") {
			test("should return correct white list endpoints") {
				val authProvider = AuthProvider("test-api-key")

				val whiteList = authProvider.whiteListDefaultEndpoints()

				whiteList shouldHaveSize 3
				whiteList shouldContainAll
					listOf(
						"/api/v1/auth/signIn",
						"/api/v1/auth/refresh",
						"/api/v1/users/register"
					)
			}

			test("should contain auth endpoints") {
				val authProvider = AuthProvider("test-api-key")

				val whiteList = authProvider.whiteListDefaultEndpoints()

				whiteList shouldContain "/api/v1/auth/signIn"
				whiteList shouldContain "/api/v1/auth/refresh"
			}

			test("should contain user registration endpoint") {
				val authProvider = AuthProvider("test-api-key")

				val whiteList = authProvider.whiteListDefaultEndpoints()

				whiteList shouldContain "/api/v1/users/register"
			}

			test("should not contain protected endpoints") {
				val authProvider = AuthProvider("test-api-key")

				val whiteList = authProvider.whiteListDefaultEndpoints()

				whiteList shouldNotContain "/api/v1/users/me"
				whiteList shouldNotContain "/api/v1/posts"
				whiteList shouldNotContain "/api/v1/auth/signOut"
			}
		}

		context("Get All Permit Endpoints") {
			test("should combine white list and ignore list") {
				val authProvider = AuthProvider("test-api-key")

				val allEndpoints = authProvider.getAllPermitEndpoints()

				allEndpoints shouldHaveSize 7
				allEndpoints shouldContainAll
					listOf(
						"/api/v1/auth/signIn",
						"/api/v1/auth/refresh",
						"/api/v1/users/register",
						"/api-docs/**",
						"/swagger-ui/**",
						"/swagger.html",
						"/actuator/**"
					)
			}

			test("should contain all white list endpoints") {
				val authProvider = AuthProvider("test-api-key")

				val allEndpoints = authProvider.getAllPermitEndpoints()
				val whiteList = authProvider.whiteListDefaultEndpoints()

				whiteList.forEach { endpoint ->
					allEndpoints shouldContain endpoint
				}
			}

			test("should contain all ignore list endpoints") {
				val authProvider = AuthProvider("test-api-key")

				val allEndpoints = authProvider.getAllPermitEndpoints()
				val ignoreList = authProvider.ignoreListDefaultEndpoints()

				ignoreList.forEach { endpoint ->
					allEndpoints shouldContain endpoint
				}
			}

			test("should not have duplicate endpoints") {
				val authProvider = AuthProvider("test-api-key")

				val allEndpoints = authProvider.getAllPermitEndpoints()

				allEndpoints.toSet().size shouldBe allEndpoints.size
			}
		}

		context("Validate API Key") {
			test("should return true when API key matches") {
				val apiKey = "valid-api-key-123"
				val authProvider = AuthProvider(apiKey)

				val result = authProvider.validateApiKey("valid-api-key-123")

				result shouldBe true
			}

			test("should return false when API key does not match") {
				val authProvider = AuthProvider("valid-api-key-123")

				val result = authProvider.validateApiKey("invalid-api-key-456")

				result shouldBe false
			}

			test("should handle empty API key") {
				val authProvider = AuthProvider("")

				val resultEmptyInput = authProvider.validateApiKey("")
				val resultNonEmptyInput = authProvider.validateApiKey("some-key")

				resultEmptyInput shouldBe true
				resultNonEmptyInput shouldBe false
			}

			test("should be case sensitive") {
				val authProvider = AuthProvider("API-Key-123")

				val exactMatch = authProvider.validateApiKey("API-Key-123")
				val lowerCase = authProvider.validateApiKey("api-key-123")
				val upperCase = authProvider.validateApiKey("API-KEY-123")

				exactMatch shouldBe true
				lowerCase shouldBe false
				upperCase shouldBe false
			}

			test("should handle special characters in API key") {
				val specialKey = "key!@#$%^&*()_+-=[]{}|;:,.<>?"
				val authProvider = AuthProvider(specialKey)

				val result = authProvider.validateApiKey(specialKey)

				result shouldBe true
			}

			test("should handle long API keys") {
				val longKey = "a".repeat(1000)
				val authProvider = AuthProvider(longKey)

				val validResult = authProvider.validateApiKey(longKey)
				val invalidResult = authProvider.validateApiKey(longKey.dropLast(1))

				validResult shouldBe true
				invalidResult shouldBe false
			}

			test("should handle whitespace in API keys") {
				val keyWithSpaces = "key with spaces"
				val authProvider = AuthProvider(keyWithSpaces)

				val exactMatch = authProvider.validateApiKey("key with spaces")
				val trimmed = authProvider.validateApiKey("keywithspaces")
				val extraSpaces = authProvider.validateApiKey("key  with  spaces")

				exactMatch shouldBe true
				trimmed shouldBe false
				extraSpaces shouldBe false
			}
		}

		context("Multiple AuthProvider Instances") {
			test("should work independently with different API keys") {
				val authProvider1 = AuthProvider("key1")
				val authProvider2 = AuthProvider("key2")

				val provider1ValidatesKey1 = authProvider1.validateApiKey("key1")
				val provider1ValidatesKey2 = authProvider1.validateApiKey("key2")
				val provider2ValidatesKey1 = authProvider2.validateApiKey("key1")
				val provider2ValidatesKey2 = authProvider2.validateApiKey("key2")

				provider1ValidatesKey1 shouldBe true
				provider1ValidatesKey2 shouldBe false
				provider2ValidatesKey1 shouldBe false
				provider2ValidatesKey2 shouldBe true
			}

			test("should return same endpoint lists regardless of API key") {
				val authProvider1 = AuthProvider("key1")
				val authProvider2 = AuthProvider("different-key")

				val ignoreList1 = authProvider1.ignoreListDefaultEndpoints()
				val ignoreList2 = authProvider2.ignoreListDefaultEndpoints()
				val whiteList1 = authProvider1.whiteListDefaultEndpoints()
				val whiteList2 = authProvider2.whiteListDefaultEndpoints()

				ignoreList1 shouldBe ignoreList2
				whiteList1 shouldBe whiteList2
			}
		}

		context("Edge Cases") {
			test("should handle null-like strings as API key") {
				val authProvider = AuthProvider("null")

				val resultNull = authProvider.validateApiKey("null")
				val resultNullString = authProvider.validateApiKey("NULL")

				resultNull shouldBe true
				resultNullString shouldBe false
			}
		}
	})
