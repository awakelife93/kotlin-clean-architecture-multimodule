package com.example.demo.kotest.persistence.auth

import com.example.demo.persistence.auth.AuditorAwareImpl
import com.example.demo.persistence.auth.adapter.UserAdapter
import com.example.demo.security.model.SecurityUserItem
import com.example.demo.user.constant.UserRole
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.optional.shouldBeEmpty
import io.kotest.matchers.optional.shouldBePresent
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@Tags("kotest-unit-test")
class AuditorAwareImplTests :
	FunSpec({

		lateinit var auditorAware: AuditorAwareImpl

		beforeTest {
			auditorAware = AuditorAwareImpl()
			mockkStatic(SecurityContextHolder::class)
		}

		afterTest {
			unmockkStatic(SecurityContextHolder::class)
		}

		context("getCurrentAuditor") {
			test("should return user ID when authenticated user exists") {
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
				val securityContext = mockk<SecurityContext>()

				every { SecurityContextHolder.getContext() } returns securityContext
				every { securityContext.authentication } returns authentication

				val result = auditorAware.getCurrentAuditor()

				result shouldBePresent {
					it shouldBe userId
				}
			}

			test("should return empty when authentication is null") {
				val securityContext = mockk<SecurityContext>()

				every { SecurityContextHolder.getContext() } returns securityContext
				every { securityContext.authentication } returns null

				val result = auditorAware.getCurrentAuditor()

				result.shouldBeEmpty()
			}

			test("should return empty when authentication is not authenticated") {
				val authentication = mockk<Authentication>()
				val securityContext = mockk<SecurityContext>()

				every { SecurityContextHolder.getContext() } returns securityContext
				every { securityContext.authentication } returns authentication
				every { authentication.isAuthenticated } returns false

				val result = auditorAware.getCurrentAuditor()

				result.shouldBeEmpty()
			}

			test("should return empty when principal is anonymousUser") {
				val authentication = mockk<Authentication>()
				val securityContext = mockk<SecurityContext>()

				every { SecurityContextHolder.getContext() } returns securityContext
				every { securityContext.authentication } returns authentication
				every { authentication.isAuthenticated } returns true
				every { authentication.principal } returns "anonymousUser"

				val result = auditorAware.getCurrentAuditor()

				result.shouldBeEmpty()
			}

			test("should handle different user roles correctly") {
				val testCases =
					listOf(
						Triple(1L, UserRole.USER, "user@example.com"),
						Triple(2L, UserRole.ADMIN, "admin@example.com"),
						Triple(3L, UserRole.MEMBER, "member@example.com")
					)

				testCases.forEach { (userId, role, email) ->
					val securityUserItem =
						SecurityUserItem(
							userId = userId,
							role = role,
							name = "Test $role",
							email = email
						)
					val userAdapter = UserAdapter(securityUserItem)
					val authentication =
						UsernamePasswordAuthenticationToken(
							userAdapter,
							null,
							userAdapter.authorities
						)
					val securityContext = mockk<SecurityContext>()

					every { SecurityContextHolder.getContext() } returns securityContext
					every { securityContext.authentication } returns authentication

					val result = auditorAware.getCurrentAuditor()

					result shouldBePresent {
						it shouldBe userId
					}
				}
			}

			test("should handle multiple calls with different authentication states") {
				val securityContext = mockk<SecurityContext>()
				every { SecurityContextHolder.getContext() } returns securityContext

				val userId = 100L
				val securityUserItem =
					SecurityUserItem(
						userId = userId,
						role = UserRole.USER,
						name = "User",
						email = "user@example.com"
					)
				val userAdapter = UserAdapter(securityUserItem)
				val authentication =
					UsernamePasswordAuthenticationToken(
						userAdapter,
						null,
						userAdapter.authorities
					)

				every { securityContext.authentication } returns authentication

				val result1 = auditorAware.getCurrentAuditor()
				result1 shouldBePresent {
					it shouldBe userId
				}

				every { securityContext.authentication } returns null

				val result2 = auditorAware.getCurrentAuditor()
				result2.shouldBeEmpty()

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

				every { securityContext.authentication } returns authentication2

				val result3 = auditorAware.getCurrentAuditor()
				result3 shouldBePresent {
					it shouldBe userId2
				}
			}

			test("should handle edge case with very large user ID") {
				val userId = Long.MAX_VALUE
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
				val securityContext = mockk<SecurityContext>()

				every { SecurityContextHolder.getContext() } returns securityContext
				every { securityContext.authentication } returns authentication

				val result = auditorAware.getCurrentAuditor()

				result shouldBePresent {
					it shouldBe userId
				}
			}

			test("should handle SecurityContext with various authentication implementations") {
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
						UsernamePasswordAuthenticationToken(userAdapter, "credentials", userAdapter.authorities)
					)

				authTypes.forEach { auth ->
					val securityContext = mockk<SecurityContext>()
					every { SecurityContextHolder.getContext() } returns securityContext
					every { securityContext.authentication } returns auth

					val result = auditorAware.getCurrentAuditor()

					result shouldBePresent {
						it shouldBe userId
					}
				}
			}

			test("should handle authentication with null credentials") {
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
				val securityContext = mockk<SecurityContext>()

				every { SecurityContextHolder.getContext() } returns securityContext
				every { securityContext.authentication } returns authentication

				val result = auditorAware.getCurrentAuditor()

				result shouldBePresent {
					it shouldBe userId
				}
			}

			test("should return empty when principal is not UserAdapter type") {
				val authentication = mockk<Authentication>()
				val securityContext = mockk<SecurityContext>()

				every { SecurityContextHolder.getContext() } returns securityContext
				every { securityContext.authentication } returns authentication
				every { authentication.isAuthenticated } returns true
				every { authentication.principal } returns "not-a-user-adapter"

				val result = auditorAware.getCurrentAuditor()

				result.shouldBeEmpty()
			}
		}
	})
