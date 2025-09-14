package com.example.demo.kotest.auth.service

import com.example.demo.auth.port.input.RefreshAccessTokenInput
import com.example.demo.auth.port.input.SignInInput
import com.example.demo.auth.service.AuthService
import com.example.demo.persistence.auth.adapter.UserAdapter
import com.example.demo.persistence.auth.provider.JWTProvider
import com.example.demo.persistence.auth.provider.TokenProvider
import com.example.demo.security.model.SecurityUserItem
import com.example.demo.user.constant.UserRole
import com.example.demo.user.exception.UserNotFoundException
import com.example.demo.user.exception.UserUnAuthorizedException
import com.example.demo.user.model.User
import com.example.demo.user.port.UserPort
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime

@ActiveProfiles("test")
@Tags("kotest-unit-test")
class AuthServiceTests :
	BehaviorSpec({

		val userPort = mockk<UserPort>()
		val passwordEncoder = mockk<PasswordEncoder>()
		val tokenProvider = mockk<TokenProvider>()
		val jwtProvider = mockk<JWTProvider>()
		val authService = AuthService(userPort, passwordEncoder, tokenProvider, jwtProvider)

		val testUser =
			User(
				id = 1L,
				email = "test@example.com",
				password = "encoded_password",
				name = "Test User",
				role = UserRole.USER,
				createdDt = LocalDateTime.now(),
				updatedDt = LocalDateTime.now()
			)

		beforeTest {
			clearAllMocks()
		}

		Given("signIn") {
			When("Valid credentials") {
				Then("Should return auth output with token") {
					val input =
						SignInInput(
							email = "test@example.com",
							password = "password123"
						)
					val accessToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.signature"

					every { userPort.findOneByEmail(input.email) } returns testUser
					every { passwordEncoder.matches(input.password, testUser.password) } returns true
					every { tokenProvider.createFullTokens(testUser) } returns accessToken

					val result = authService.signIn(input)

					result.shouldNotBeNull()
					result.userId shouldBe 1L
					result.email shouldBe "test@example.com"
					result.name shouldBe "Test User"
					result.role shouldBe UserRole.USER
					result.accessToken shouldBe accessToken

					verify(exactly = 1) {
						userPort.findOneByEmail(input.email)
						passwordEncoder.matches(input.password, testUser.password)
						tokenProvider.createFullTokens(testUser)
					}
				}
			}

			When("User not found") {
				Then("Should throw UserNotFoundException") {
					val input =
						SignInInput(
							email = "notfound@example.com",
							password = "password123"
						)

					every { userPort.findOneByEmail(input.email) } returns null

					val exception =
						shouldThrowExactly<UserNotFoundException> {
							authService.signIn(input)
						}

					exception.message shouldBe "User Not Found email = notfound@example.com"

					verify(exactly = 1) { userPort.findOneByEmail(input.email) }
				}
			}

			When("Invalid password") {
				Then("Should throw UserUnAuthorizedException") {
					val input =
						SignInInput(
							email = "test@example.com",
							password = "wrongpassword"
						)

					every { userPort.findOneByEmail(input.email) } returns testUser
					every { passwordEncoder.matches(input.password, testUser.password) } returns false

					val exception =
						shouldThrowExactly<UserUnAuthorizedException> {
							authService.signIn(input)
						}

					exception.message shouldBe "User UnAuthorized email = test@example.com"

					verify(exactly = 1) {
						userPort.findOneByEmail(input.email)
						passwordEncoder.matches(input.password, testUser.password)
					}
					verify(exactly = 0) { tokenProvider.createFullTokens(any<User>()) }
				}
			}
		}

		Given("signOut") {
			When("Sign out user") {
				Then("Should delete refresh token and clear context") {
					val userId = 1L

					justRun { tokenProvider.deleteRefreshToken(userId) }
					mockkStatic(SecurityContextHolder::class)
					justRun { SecurityContextHolder.clearContext() }

					authService.signOut(userId)

					verify(exactly = 1) {
						tokenProvider.deleteRefreshToken(userId)
						SecurityContextHolder.clearContext()
					}

					unmockkStatic(SecurityContextHolder::class)
				}
			}
		}

		Given("refreshAccessToken") {
			When("Valid refresh token") {
				Then("Should return new access token") {
					val input =
						RefreshAccessTokenInput(
							refreshToken = "valid_refresh_token"
						)
					val newAccessToken = "new_access_token"

					val securityUserItem =
						SecurityUserItem(
							userId = 1L,
							email = "test@example.com",
							name = "Test User",
							role = UserRole.USER
						)
					val userAdapter = mockk<UserAdapter>()
					val authentication = UsernamePasswordAuthenticationToken(userAdapter, null)

					every { jwtProvider.getAuthentication(input.refreshToken, true) } returns authentication
					every { userAdapter.securityUserItem } returns securityUserItem
					every { tokenProvider.refreshAccessToken(securityUserItem) } returns newAccessToken

					val result = authService.refreshAccessToken(input)

					result.shouldNotBeNull()
					result.accessToken shouldBe newAccessToken

					verify(exactly = 1) {
						jwtProvider.getAuthentication(input.refreshToken, true)
						tokenProvider.refreshAccessToken(securityUserItem)
					}
				}
			}
		}
	})
