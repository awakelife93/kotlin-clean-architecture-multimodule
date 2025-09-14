package com.example.demo.mockito.persistence.auth

import com.example.demo.persistence.auth.AuditorAwareImpl
import com.example.demo.persistence.auth.adapter.UserAdapter
import com.example.demo.security.model.SecurityUserItem
import com.example.demo.user.constant.UserRole
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.Mock
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.context.ActiveProfiles
import java.util.Optional

@ActiveProfiles("test")
@Tag("mockito-unit-test")
@DisplayName("Mockito Unit - Auditor Aware Implementation Test")
@ExtendWith(MockitoExtension::class)
class AuditorAwareImplTests {
	private lateinit var auditorAware: AuditorAwareImpl
	private lateinit var securityContextHolderMock: MockedStatic<SecurityContextHolder>

	@Mock
	private lateinit var securityContext: SecurityContext

	@Mock
	private lateinit var authentication: Authentication

	@BeforeEach
	fun setUp() {
		auditorAware = AuditorAwareImpl()
		securityContextHolderMock = Mockito.mockStatic(SecurityContextHolder::class.java)
		securityContextHolderMock
			.`when`<SecurityContext> { SecurityContextHolder.getContext() }
			.thenReturn(securityContext)
	}

	@AfterEach
	fun tearDown() {
		securityContextHolderMock.close()
	}

	@Nested
	@DisplayName("GetCurrentAuditor Tests")
	inner class GetCurrentAuditorTests {
		@Test
		@DisplayName("should return user ID when authenticated user exists")
		fun `should return user ID when authenticated`() {
			val userId = 123L
			val securityUserItem =
				SecurityUserItem(
					userId = userId,
					role = UserRole.USER,
					name = "Test User",
					email = "test@example.com"
				)
			val userAdapter = UserAdapter(securityUserItem)
			val authentication =
				UsernamePasswordAuthenticationToken(
					userAdapter,
					null,
					userAdapter.authorities
				)

			whenever(securityContext.authentication).thenReturn(authentication)

			val result = auditorAware.getCurrentAuditor()

			assertTrue(result.isPresent)
			assertEquals(userId, result.get())
		}

		@Test
		@DisplayName("should return empty when authentication is null")
		fun `should return empty when authentication is null`() {
			whenever(securityContext.authentication).thenReturn(null)

			val result = auditorAware.getCurrentAuditor()

			assertFalse(result.isPresent)
			assertEquals(Optional.empty<Long>(), result)
		}

		@Test
		@DisplayName("should return empty when authentication is not authenticated")
		fun `should return empty when not authenticated`() {
			whenever(securityContext.authentication).thenReturn(authentication)
			whenever(authentication.isAuthenticated).thenReturn(false)

			val result = auditorAware.getCurrentAuditor()

			assertFalse(result.isPresent)
		}

		@Test
		@DisplayName("should return empty when principal is anonymousUser")
		fun `should return empty when anonymous user`() {
			whenever(securityContext.authentication).thenReturn(authentication)
			whenever(authentication.isAuthenticated).thenReturn(true)
			whenever(authentication.principal).thenReturn("anonymousUser")

			val result = auditorAware.getCurrentAuditor()

			assertFalse(result.isPresent)
		}

		@ParameterizedTest
		@EnumSource(UserRole::class)
		@DisplayName("should handle different user roles correctly")
		fun `should handle different user roles`(role: UserRole) {
			val userId = role.ordinal.toLong() + 1
			val securityUserItem =
				SecurityUserItem(
					userId = userId,
					role = role,
					name = "Test $role",
					email = "$role@example.com".lowercase()
				)
			val userAdapter = UserAdapter(securityUserItem)
			val authentication =
				UsernamePasswordAuthenticationToken(
					userAdapter,
					null,
					userAdapter.authorities
				)

			whenever(securityContext.authentication).thenReturn(authentication)

			val result = auditorAware.getCurrentAuditor()

			assertTrue(result.isPresent)
			assertEquals(userId, result.get())
		}

		@Test
		@DisplayName("should handle multiple calls with different authentication states")
		fun `should handle state changes`() {
			val userId1 = 100L
			val securityUserItem1 =
				SecurityUserItem(
					userId = userId1,
					role = UserRole.USER,
					name = "User",
					email = "user@example.com"
				)
			val userAdapter1 = UserAdapter(securityUserItem1)
			val authentication1 =
				UsernamePasswordAuthenticationToken(
					userAdapter1,
					null,
					userAdapter1.authorities
				)

			whenever(securityContext.authentication).thenReturn(authentication1)

			val result1 = auditorAware.getCurrentAuditor()
			assertTrue(result1.isPresent)
			assertEquals(userId1, result1.get())

			whenever(securityContext.authentication).thenReturn(null)

			val result2 = auditorAware.getCurrentAuditor()
			assertFalse(result2.isPresent)

			val userId2 = 200L
			val securityUserItem2 =
				SecurityUserItem(
					userId = userId2,
					role = UserRole.ADMIN,
					name = "Admin",
					email = "admin@example.com"
				)
			val userAdapter2 = UserAdapter(securityUserItem2)
			val authentication2 =
				UsernamePasswordAuthenticationToken(
					userAdapter2,
					null,
					userAdapter2.authorities
				)

			whenever(securityContext.authentication).thenReturn(authentication2)

			val result3 = auditorAware.getCurrentAuditor()
			assertTrue(result3.isPresent)
			assertEquals(userId2, result3.get())
		}

		@ParameterizedTest
		@ValueSource(longs = [0, 1, 100, 1000, Long.MAX_VALUE])
		@DisplayName("should handle various user ID values")
		fun `should handle various user ID values`(userId: Long) {
			val securityUserItem =
				SecurityUserItem(
					userId = userId,
					role = UserRole.USER,
					name = "Test User",
					email = "test@example.com"
				)
			val userAdapter = UserAdapter(securityUserItem)
			val authentication =
				UsernamePasswordAuthenticationToken(
					userAdapter,
					null,
					userAdapter.authorities
				)

			whenever(securityContext.authentication).thenReturn(authentication)

			val result = auditorAware.getCurrentAuditor()

			assertTrue(result.isPresent)
			assertEquals(userId, result.get())
		}
	}

	@Nested
	@DisplayName("Edge Case Tests")
	inner class EdgeCaseTests {
		@Test
		@DisplayName("should handle authentication with null credentials")
		fun `should handle null credentials`() {
			val userId = 999L
			val securityUserItem =
				SecurityUserItem(
					userId = userId,
					role = UserRole.USER,
					name = "Test User",
					email = "test@example.com"
				)
			val userAdapter = UserAdapter(securityUserItem)
			val authentication =
				UsernamePasswordAuthenticationToken(
					userAdapter,
					null,
					userAdapter.authorities
				)

			whenever(securityContext.authentication).thenReturn(authentication)

			val result = auditorAware.getCurrentAuditor()

			assertTrue(result.isPresent)
			assertEquals(userId, result.get())
		}

		@Test
		@DisplayName("should handle authentication with credentials")
		fun `should handle with credentials`() {
			val userId = 555L
			val securityUserItem =
				SecurityUserItem(
					userId = userId,
					role = UserRole.USER,
					name = "Test User",
					email = "test@example.com"
				)
			val userAdapter = UserAdapter(securityUserItem)
			val authentication =
				UsernamePasswordAuthenticationToken(
					userAdapter,
					"some-credentials",
					userAdapter.authorities
				)

			whenever(securityContext.authentication).thenReturn(authentication)

			val result = auditorAware.getCurrentAuditor()

			assertTrue(result.isPresent)
			assertEquals(userId, result.get())
		}

		@Test
		@DisplayName("should handle when principal is not UserAdapter type")
		fun `should handle non-UserAdapter principal`() {
			whenever(securityContext.authentication).thenReturn(authentication)
			whenever(authentication.isAuthenticated).thenReturn(true)
			whenever(authentication.principal).thenReturn("some-other-principal-type")

			val result = auditorAware.getCurrentAuditor()

			assertFalse(result.isPresent)
		}

		@Test
		@DisplayName("should handle negative user IDs")
		fun `should handle negative user IDs`() {
			val userId = -100L
			val securityUserItem =
				SecurityUserItem(
					userId = userId,
					role = UserRole.USER,
					name = "Test User",
					email = "test@example.com"
				)
			val userAdapter = UserAdapter(securityUserItem)
			val authentication =
				UsernamePasswordAuthenticationToken(
					userAdapter,
					null,
					userAdapter.authorities
				)

			whenever(securityContext.authentication).thenReturn(authentication)

			val result = auditorAware.getCurrentAuditor()

			assertTrue(result.isPresent)
			assertEquals(userId, result.get())
		}

		@Test
		@DisplayName("should handle special characters in user details")
		fun `should handle special characters in user details`() {
			val userId = 777L
			val securityUserItem =
				SecurityUserItem(
					userId = userId,
					role = UserRole.USER,
					name = "Test User !@#$%^&*()",
					email = "test+special@example.com"
				)
			val userAdapter = UserAdapter(securityUserItem)
			val authentication =
				UsernamePasswordAuthenticationToken(
					userAdapter,
					null,
					userAdapter.authorities
				)

			whenever(securityContext.authentication).thenReturn(authentication)

			val result = auditorAware.getCurrentAuditor()

			assertTrue(result.isPresent)
			assertEquals(userId, result.get())
		}
	}

	@Nested
	@DisplayName("Authentication Type Tests")
	inner class AuthenticationTypeTests {
		@Test
		@DisplayName("should handle different authentication implementations")
		fun `should handle different auth types`() {
			val userId = 42L
			val securityUserItem =
				SecurityUserItem(
					userId = userId,
					role = UserRole.USER,
					name = "Test User",
					email = "test@example.com"
				)
			val userAdapter = UserAdapter(securityUserItem)

			val authTypes =
				listOf(
					UsernamePasswordAuthenticationToken(userAdapter, null, userAdapter.authorities),
					UsernamePasswordAuthenticationToken(userAdapter, "password", userAdapter.authorities),
					UsernamePasswordAuthenticationToken(userAdapter, null, emptyList()),
					UsernamePasswordAuthenticationToken(userAdapter, "token", userAdapter.authorities)
				)

			authTypes.forEach { auth ->
				whenever(securityContext.authentication).thenReturn(auth)

				val result = auditorAware.getCurrentAuditor()

				assertTrue(result.isPresent)
				assertEquals(userId, result.get())
			}
		}
	}
}
