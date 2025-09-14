package com.example.demo.kotest.auth.presentation

import com.example.demo.auth.port.input.RefreshAccessTokenInput
import com.example.demo.auth.port.input.SignInInput
import com.example.demo.auth.port.input.SignOutInput
import com.example.demo.auth.port.output.AuthOutput
import com.example.demo.auth.presentation.AuthController
import com.example.demo.auth.presentation.dto.request.RefreshAccessTokenRequest
import com.example.demo.auth.presentation.dto.request.SignInRequest
import com.example.demo.auth.presentation.dto.response.RefreshAccessTokenResponse
import com.example.demo.auth.presentation.dto.response.SignInResponse
import com.example.demo.auth.presentation.mapper.AuthPresentationMapper
import com.example.demo.auth.usecase.RefreshAccessTokenUseCase
import com.example.demo.auth.usecase.SignInUseCase
import com.example.demo.auth.usecase.SignOutUseCase
import com.example.demo.security.model.SecurityUserItem
import com.example.demo.user.constant.UserRole
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@Tags("kotest-unit-test")
class AuthControllerTests :
	BehaviorSpec({

		val signInUseCase = mockk<SignInUseCase>()
		val signOutUseCase = mockk<SignOutUseCase>()
		val refreshAccessTokenUseCase = mockk<RefreshAccessTokenUseCase>()

		val authController =
			AuthController(
				signInUseCase = signInUseCase,
				signOutUseCase = signOutUseCase,
				refreshAccessTokenUseCase = refreshAccessTokenUseCase
			)

		val testEmail = "test@example.com"
		val testPassword = "password123"
		val testUserId = 1L
		val testUserName = "Test User"
		val testAccessToken = "test.access.token"
		val refreshToken = "test.refresh.token"
		val newAccessToken = "new.access.token"

		fun createSignInRequest(
			email: String = testEmail,
			password: String = testPassword
		) = SignInRequest(email = email, password = password)

		fun createAuthOutput(
			userId: Long = testUserId,
			email: String = testEmail,
			name: String = testUserName,
			role: UserRole = UserRole.USER,
			token: String = testAccessToken
		) = AuthOutput(
			userId = userId,
			email = email,
			name = name,
			role = role,
			accessToken = token
		)

		fun createSecurityUser(
			userId: Long = testUserId,
			email: String = testEmail,
			name: String = testUserName,
			role: UserRole = UserRole.USER
		) = SecurityUserItem(
			userId = userId,
			email = email,
			name = name,
			role = role
		)

		beforeSpec {
			mockkObject(AuthPresentationMapper)
		}

		afterSpec {
			unmockkObject(AuthPresentationMapper)
		}

		beforeTest {
			clearAllMocks()
		}

		Given("Sign In") {
			When("Valid credentials provided") {
				Then("Returns auth token successfully") {
					val request = createSignInRequest()
					val input = SignInInput(email = testEmail, password = testPassword)
					val authOutput = createAuthOutput()
					val response =
						SignInResponse(
							userId = testUserId,
							email = testEmail,
							name = testUserName,
							role = UserRole.USER,
							accessToken = testAccessToken
						)

					every { AuthPresentationMapper.toSignInInput(request) } returns input
					every { signInUseCase.execute(input) } returns authOutput
					every { AuthPresentationMapper.toSignInResponse(authOutput) } returns response

					val result = authController.signIn(request)

					result shouldNotBeNull {
						statusCode shouldBe HttpStatus.OK
						body shouldNotBeNull {
							userId shouldBe testUserId
							email shouldBe testEmail
							name shouldBe testUserName
							role shouldBe UserRole.USER
							accessToken shouldBe testAccessToken
						}
					}

					verify {
						AuthPresentationMapper.toSignInInput(request)
						signInUseCase.execute(input)
						AuthPresentationMapper.toSignInResponse(authOutput)
					}
				}
			}

			When("Invalid credentials provided") {
				Then("Throws authentication exception") {
					val request = createSignInRequest()
					val input = SignInInput(email = testEmail, password = testPassword)

					every { AuthPresentationMapper.toSignInInput(request) } returns input
					every { signInUseCase.execute(input) }
						.throws(IllegalArgumentException("Invalid credentials"))

					shouldThrow<IllegalArgumentException> {
						authController.signIn(request)
					}.message shouldBe "Invalid credentials"

					verify {
						AuthPresentationMapper.toSignInInput(request)
						signInUseCase.execute(input)
					}
					verify(exactly = 0) {
						AuthPresentationMapper.toSignInResponse(any<AuthOutput>())
					}
				}
			}

			When("User not found") {
				Then("Throws user not found exception") {
					val request = createSignInRequest()
					val input = SignInInput(email = testEmail, password = testPassword)

					every { AuthPresentationMapper.toSignInInput(request) } returns input
					every { signInUseCase.execute(input) }
						.throws(NoSuchElementException("User not found"))

					shouldThrow<NoSuchElementException> {
						authController.signIn(request)
					}.message shouldBe "User not found"

					verify {
						AuthPresentationMapper.toSignInInput(request)
						signInUseCase.execute(input)
					}
				}
			}
		}

		Given("Sign Out") {
			When("Valid user signs out") {
				Then("Successfully signs out") {
					val securityUser = createSecurityUser()
					val input = SignOutInput(userId = testUserId)

					every { AuthPresentationMapper.toSignOutInput(testUserId) } returns input
					justRun { signOutUseCase.execute(input) }

					val result = authController.signOut(securityUser)

					result shouldNotBeNull {
						statusCode shouldBe HttpStatus.OK
						body.shouldBeNull()
					}

					verify {
						AuthPresentationMapper.toSignOutInput(testUserId)
						signOutUseCase.execute(input)
					}
				}
			}

			When("Sign out fails") {
				Then("Throws exception") {
					val securityUser = createSecurityUser()
					val input = SignOutInput(userId = testUserId)

					every { AuthPresentationMapper.toSignOutInput(testUserId) } returns input
					every { signOutUseCase.execute(input) }
						.throws(RuntimeException("Sign out failed"))

					shouldThrow<RuntimeException> {
						authController.signOut(securityUser)
					}.message shouldBe "Sign out failed"

					verify {
						AuthPresentationMapper.toSignOutInput(testUserId)
						signOutUseCase.execute(input)
					}
				}
			}
		}

		Given("Refresh Access Token") {
			When("Valid refresh token provided") {
				Then("Returns new access token") {
					val request = RefreshAccessTokenRequest(refreshToken = refreshToken)
					val input = RefreshAccessTokenInput(refreshToken = refreshToken)
					val authOutput =
						AuthOutput(
							userId = 0L,
							email = "",
							name = "",
							role = UserRole.USER,
							accessToken = newAccessToken
						)
					val response = RefreshAccessTokenResponse(accessToken = newAccessToken)

					every { AuthPresentationMapper.toRefreshInput(request) } returns input
					every { refreshAccessTokenUseCase.execute(input) } returns authOutput
					every { AuthPresentationMapper.toRefreshResponse(authOutput) } returns response

					val result = authController.refreshAccessToken(request)

					result shouldNotBeNull {
						statusCode shouldBe HttpStatus.CREATED
						body shouldNotBeNull {
							accessToken shouldBe newAccessToken
						}
					}

					verify {
						AuthPresentationMapper.toRefreshInput(request)
						refreshAccessTokenUseCase.execute(input)
						AuthPresentationMapper.toRefreshResponse(authOutput)
					}
				}
			}

			When("Expired refresh token") {
				Then("Throws token expired exception") {
					val request = RefreshAccessTokenRequest(refreshToken = refreshToken)
					val input = RefreshAccessTokenInput(refreshToken = refreshToken)

					every { AuthPresentationMapper.toRefreshInput(request) } returns input
					every { refreshAccessTokenUseCase.execute(input) }
						.throws(IllegalStateException("Refresh Token is Expired"))

					shouldThrow<IllegalStateException> {
						authController.refreshAccessToken(request)
					}.message shouldBe "Refresh Token is Expired"

					verify {
						AuthPresentationMapper.toRefreshInput(request)
						refreshAccessTokenUseCase.execute(input)
					}
					verify(exactly = 0) {
						AuthPresentationMapper.toRefreshResponse(any<AuthOutput>())
					}
				}
			}

			When("Invalid refresh token") {
				Then("Throws invalid token exception") {
					val request = RefreshAccessTokenRequest(refreshToken = refreshToken)
					val input = RefreshAccessTokenInput(refreshToken = refreshToken)

					every { AuthPresentationMapper.toRefreshInput(request) } returns input
					every { refreshAccessTokenUseCase.execute(input) }
						.throws(IllegalArgumentException("Invalid refresh token"))

					shouldThrow<IllegalArgumentException> {
						authController.refreshAccessToken(request)
					}.message shouldBe "Invalid refresh token"

					verify {
						AuthPresentationMapper.toRefreshInput(request)
						refreshAccessTokenUseCase.execute(input)
					}
				}
			}
		}
	})
