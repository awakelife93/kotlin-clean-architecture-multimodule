package com.example.demo.mockito.persistence.auth.provider

import com.example.demo.persistence.auth.provider.AuthProvider
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@Tag("mockito-unit-test")
@DisplayName("Mockito Unit - Auth Provider Test")
@ExtendWith(MockitoExtension::class)
class AuthProviderTests {
	@Nested
	@DisplayName("Ignore List Default Endpoints Tests")
	inner class IgnoreListDefaultEndpointsTests {
		@Test
		@DisplayName("should return correct ignore list endpoints")
		fun `should return correct ignore list endpoints`() {
			val authProvider = AuthProvider("test-api-key")

			val ignoreList = authProvider.ignoreListDefaultEndpoints()

			assertEquals(4, ignoreList.size)
			assertTrue(
				ignoreList.containsAll(
					listOf(
						"/api-docs/**",
						"/swagger-ui/**",
						"/swagger.html",
						"/actuator/**"
					)
				)
			)
		}

		@Test
		@DisplayName("should return immutable list for ignore endpoints")
		fun `should return immutable list`() {
			val authProvider = AuthProvider("test-api-key")

			val firstCall = authProvider.ignoreListDefaultEndpoints()
			val secondCall = authProvider.ignoreListDefaultEndpoints()

			assertEquals(firstCall, secondCall)
			assertEquals(4, firstCall.size)
		}

		@Test
		@DisplayName("should contain swagger documentation endpoints")
		fun `should contain swagger endpoints`() {
			val authProvider = AuthProvider("test-api-key")

			val ignoreList = authProvider.ignoreListDefaultEndpoints()

			assertTrue(ignoreList.contains("/api-docs/**"))
			assertTrue(ignoreList.contains("/swagger-ui/**"))
			assertTrue(ignoreList.contains("/swagger.html"))
		}

		@Test
		@DisplayName("should contain actuator endpoints")
		fun `should contain actuator endpoints`() {
			val authProvider = AuthProvider("test-api-key")

			val ignoreList = authProvider.ignoreListDefaultEndpoints()

			assertTrue(ignoreList.contains("/actuator/**"))
		}
	}

	@Nested
	@DisplayName("White List Default Endpoints Tests")
	inner class WhiteListDefaultEndpointsTests {
		@Test
		@DisplayName("should return correct white list endpoints")
		fun `should return correct white list endpoints`() {
			val authProvider = AuthProvider("test-api-key")

			val whiteList = authProvider.whiteListDefaultEndpoints()

			assertEquals(3, whiteList.size)
			assertTrue(
				whiteList.containsAll(
					listOf(
						"/api/v1/auth/signIn",
						"/api/v1/auth/refresh",
						"/api/v1/users/register"
					)
				)
			)
		}

		@Test
		@DisplayName("should contain auth endpoints")
		fun `should contain auth endpoints`() {
			val authProvider = AuthProvider("test-api-key")

			val whiteList = authProvider.whiteListDefaultEndpoints()

			assertTrue(whiteList.contains("/api/v1/auth/signIn"))
			assertTrue(whiteList.contains("/api/v1/auth/refresh"))
		}

		@Test
		@DisplayName("should contain user registration endpoint")
		fun `should contain registration endpoint`() {
			val authProvider = AuthProvider("test-api-key")

			val whiteList = authProvider.whiteListDefaultEndpoints()

			assertTrue(whiteList.contains("/api/v1/users/register"))
		}

		@Test
		@DisplayName("should not contain protected endpoints")
		fun `should not contain protected endpoints`() {
			val authProvider = AuthProvider("test-api-key")

			val whiteList = authProvider.whiteListDefaultEndpoints()

			assertFalse(whiteList.contains("/api/v1/users/me"))
			assertFalse(whiteList.contains("/api/v1/posts"))
			assertFalse(whiteList.contains("/api/v1/auth/signOut"))
		}
	}

	@Nested
	@DisplayName("Get All Permit Endpoints Tests")
	inner class GetAllPermitEndpointsTests {
		@Test
		@DisplayName("should combine white list and ignore list")
		fun `should combine both lists`() {
			val authProvider = AuthProvider("test-api-key")

			val allEndpoints = authProvider.getAllPermitEndpoints()

			assertEquals(7, allEndpoints.size)
			assertTrue(
				allEndpoints.containsAll(
					listOf(
						"/api/v1/auth/signIn",
						"/api/v1/auth/refresh",
						"/api/v1/users/register",
						"/api-docs/**",
						"/swagger-ui/**",
						"/swagger.html",
						"/actuator/**"
					)
				)
			)
		}

		@Test
		@DisplayName("should contain all white list endpoints")
		fun `should contain all white list endpoints`() {
			val authProvider = AuthProvider("test-api-key")

			val allEndpoints = authProvider.getAllPermitEndpoints()
			val whiteList = authProvider.whiteListDefaultEndpoints()

			whiteList.forEach { endpoint ->
				assertTrue(allEndpoints.contains(endpoint))
			}
		}

		@Test
		@DisplayName("should contain all ignore list endpoints")
		fun `should contain all ignore list endpoints`() {
			val authProvider = AuthProvider("test-api-key")

			val allEndpoints = authProvider.getAllPermitEndpoints()
			val ignoreList = authProvider.ignoreListDefaultEndpoints()

			ignoreList.forEach { endpoint ->
				assertTrue(allEndpoints.contains(endpoint))
			}
		}

		@Test
		@DisplayName("should not have duplicate endpoints")
		fun `should not have duplicates`() {
			val authProvider = AuthProvider("test-api-key")

			val allEndpoints = authProvider.getAllPermitEndpoints()

			assertEquals(allEndpoints.toSet().size, allEndpoints.size)
		}
	}

	@Nested
	@DisplayName("Validate API Key Tests")
	inner class ValidateApiKeyTests {
		@Test
		@DisplayName("should return true when API key matches")
		fun `should validate matching key`() {
			val apiKey = "valid-api-key-123"
			val authProvider = AuthProvider(apiKey)

			val result = authProvider.validateApiKey("valid-api-key-123")

			assertTrue(result)
		}

		@Test
		@DisplayName("should return false when API key does not match")
		fun `should reject non-matching key`() {
			val authProvider = AuthProvider("valid-api-key-123")

			val result = authProvider.validateApiKey("invalid-api-key-456")

			assertFalse(result)
		}

		@Test
		@DisplayName("should handle empty API key")
		fun `should handle empty key`() {
			val authProvider = AuthProvider("")

			val resultEmptyInput = authProvider.validateApiKey("")
			val resultNonEmptyInput = authProvider.validateApiKey("some-key")

			assertTrue(resultEmptyInput)
			assertFalse(resultNonEmptyInput)
		}

		@Test
		@DisplayName("should be case sensitive")
		fun `should be case sensitive`() {
			val authProvider = AuthProvider("API-Key-123")

			val exactMatch = authProvider.validateApiKey("API-Key-123")
			val lowerCase = authProvider.validateApiKey("api-key-123")
			val upperCase = authProvider.validateApiKey("API-KEY-123")

			assertTrue(exactMatch)
			assertFalse(lowerCase)
			assertFalse(upperCase)
		}

		@Test
		@DisplayName("should handle special characters in API key")
		fun `should handle special characters`() {
			val specialKey = "key!@#$%^&*()_+-=[]{}|;:,.<>?"
			val authProvider = AuthProvider(specialKey)

			val result = authProvider.validateApiKey(specialKey)

			assertTrue(result)
		}

		@Test
		@DisplayName("should handle long API keys")
		fun `should handle long keys`() {
			val longKey = "a".repeat(1000)
			val authProvider = AuthProvider(longKey)

			val validResult = authProvider.validateApiKey(longKey)
			val invalidResult = authProvider.validateApiKey(longKey.dropLast(1))

			assertTrue(validResult)
			assertFalse(invalidResult)
		}

		@Test
		@DisplayName("should handle whitespace in API keys")
		fun `should handle whitespace`() {
			val keyWithSpaces = "key with spaces"
			val authProvider = AuthProvider(keyWithSpaces)

			val exactMatch = authProvider.validateApiKey("key with spaces")
			val trimmed = authProvider.validateApiKey("keywithspaces")
			val extraSpaces = authProvider.validateApiKey("key  with  spaces")

			assertTrue(exactMatch)
			assertFalse(trimmed)
			assertFalse(extraSpaces)
		}

		@ParameterizedTest
		@CsvSource(
			"test-key,test-key,true",
			"test-key,wrong-key,false",
			"123456,123456,true",
			"123456,654321,false",
			"abc!@#,abc!@#,true",
			"abc!@#,abc@#!,false"
		)
		@DisplayName("should validate various API keys")
		fun `should validate various keys`(
			configuredKey: String,
			inputKey: String,
			expected: Boolean
		) {
			val authProvider = AuthProvider(configuredKey)

			val result = authProvider.validateApiKey(inputKey)

			assertEquals(expected, result)
		}
	}

	@Nested
	@DisplayName("Multiple AuthProvider Instances Tests")
	inner class MultipleInstancesTests {
		@Test
		@DisplayName("should work independently with different API keys")
		fun `should work independently`() {
			val authProvider1 = AuthProvider("key1")
			val authProvider2 = AuthProvider("key2")

			val provider1ValidatesKey1 = authProvider1.validateApiKey("key1")
			val provider1ValidatesKey2 = authProvider1.validateApiKey("key2")
			val provider2ValidatesKey1 = authProvider2.validateApiKey("key1")
			val provider2ValidatesKey2 = authProvider2.validateApiKey("key2")

			assertTrue(provider1ValidatesKey1)
			assertFalse(provider1ValidatesKey2)
			assertFalse(provider2ValidatesKey1)
			assertTrue(provider2ValidatesKey2)
		}

		@Test
		@DisplayName("should return same endpoint lists regardless of API key")
		fun `should return same endpoints`() {
			val authProvider1 = AuthProvider("key1")
			val authProvider2 = AuthProvider("different-key")

			val ignoreList1 = authProvider1.ignoreListDefaultEndpoints()
			val ignoreList2 = authProvider2.ignoreListDefaultEndpoints()
			val whiteList1 = authProvider1.whiteListDefaultEndpoints()
			val whiteList2 = authProvider2.whiteListDefaultEndpoints()

			assertEquals(ignoreList1, ignoreList2)
			assertEquals(whiteList1, whiteList2)
		}
	}

	@Nested
	@DisplayName("Edge Cases Tests")
	inner class EdgeCaseTests {
		@Test
		@DisplayName("should handle null-like strings as API key")
		fun `should handle null-like strings`() {
			val authProvider = AuthProvider("null")

			val resultNull = authProvider.validateApiKey("null")
			val resultNullUpperCase = authProvider.validateApiKey("NULL")

			assertTrue(resultNull)
			assertFalse(resultNullUpperCase)
		}

		@Test
		@DisplayName("should handle very short and very long keys")
		fun `should handle extreme key lengths`() {
			val shortKey = "a"
			val veryLongKey = "b".repeat(10000)
			val authProviderShort = AuthProvider(shortKey)
			val authProviderLong = AuthProvider(veryLongKey)

			val shortValid = authProviderShort.validateApiKey(shortKey)
			val shortInvalid = authProviderShort.validateApiKey("b")
			val longValid = authProviderLong.validateApiKey(veryLongKey)
			val longInvalid = authProviderLong.validateApiKey(veryLongKey.dropLast(1))

			assertTrue(shortValid)
			assertFalse(shortInvalid)
			assertTrue(longValid)
			assertFalse(longInvalid)
		}
	}

	@Nested
	@DisplayName("Endpoint Configuration Tests")
	inner class EndpointConfigurationTests {
		@Test
		@DisplayName("should maintain endpoint order consistency")
		fun `should maintain endpoint order`() {
			val authProvider = AuthProvider("test-key")

			val firstCallIgnore = authProvider.ignoreListDefaultEndpoints()
			val secondCallIgnore = authProvider.ignoreListDefaultEndpoints()
			val firstCallWhite = authProvider.whiteListDefaultEndpoints()
			val secondCallWhite = authProvider.whiteListDefaultEndpoints()

			assertEquals(firstCallIgnore, secondCallIgnore)
			assertEquals(firstCallWhite, secondCallWhite)
			assertEquals(firstCallIgnore[0], secondCallIgnore[0])
			assertEquals(firstCallIgnore[1], secondCallIgnore[1])
		}

		@Test
		@DisplayName("should correctly identify public vs protected endpoints")
		fun `should identify endpoint types`() {
			val authProvider = AuthProvider("test-key")
			val allPermitted = authProvider.getAllPermitEndpoints()

			val publicEndpoints =
				listOf(
					"/api/v1/auth/signIn",
					"/api/v1/auth/refresh",
					"/api/v1/users/register",
					"/swagger-ui/**"
				)

			val protectedEndpoints =
				listOf(
					"/api/v1/users/me",
					"/api/v1/posts",
					"/api/v1/auth/signOut",
					"/api/v1/users/1"
				)

			publicEndpoints.forEach { endpoint ->
				assertTrue(allPermitted.contains(endpoint), "Should contain public endpoint: $endpoint")
			}

			protectedEndpoints.forEach { endpoint ->
				assertFalse(allPermitted.contains(endpoint), "Should not contain protected endpoint: $endpoint")
			}
		}
	}
}
