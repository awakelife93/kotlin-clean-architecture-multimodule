package com.example.demo.kotest.auth.usecase

import com.example.demo.auth.port.input.SignInInput
import com.example.demo.auth.port.output.AuthOutput
import com.example.demo.auth.service.AuthService
import com.example.demo.auth.usecase.SignInUseCase
import com.example.demo.user.constant.UserRole
import com.example.demo.user.exception.UserNotFoundException
import com.example.demo.user.exception.UserUnAuthorizedException
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@Tags("kotest-unit-test")
class SignInUseCaseTests :
	BehaviorSpec({

		Given("Sign in") {

			When("Valid credentials provided") {
				Then("Should return auth output successfully") {
					val authService = mockk<AuthService>()
					val signInUseCase = SignInUseCase(authService)

					val input =
						SignInInput(
							email = "test@example.com",
							password = "password123"
						)

					val authOutput =
						AuthOutput(
							userId = 1L,
							email = "test@example.com",
							name = "Test User",
							role = UserRole.USER,
							accessToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.signature"
						)

					every { authService.signIn(input) } returns authOutput

					val result = signInUseCase.execute(input)

					result.shouldNotBeNull()
					result.userId shouldBe 1L
					result.email shouldBe "test@example.com"
					result.name shouldBe "Test User"
					result.role shouldBe UserRole.USER
					result.accessToken shouldBe authOutput.accessToken

					verify(exactly = 1) { authService.signIn(input) }
				}
			}

			When("Invalid email provided") {
				Then("Should throw UserNotFoundException") {
					val authService = mockk<AuthService>()
					val signInUseCase = SignInUseCase(authService)

					val input =
						SignInInput(
							email = "notfound@example.com",
							password = "password123"
						)

					every { authService.signIn(input) } throws UserNotFoundException(1L)

					val exception =
						shouldThrowExactly<UserNotFoundException> {
							signInUseCase.execute(input)
						}

					verify(exactly = 1) { authService.signIn(input) }
				}
			}

			When("Invalid password provided") {
				Then("Should throw UserUnAuthorizedException") {
					val authService = mockk<AuthService>()
					val signInUseCase = SignInUseCase(authService)

					val input =
						SignInInput(
							email = "test@example.com",
							password = "wrongpassword"
						)

					every { authService.signIn(input) } throws UserUnAuthorizedException("test@example.com")

					val exception =
						shouldThrowExactly<UserUnAuthorizedException> {
							signInUseCase.execute(input)
						}

					exception.message shouldBe "test@example.com"

					verify(exactly = 1) { authService.signIn(input) }
				}
			}

			When("Admin user signs in") {
				Then("Should return auth output with ADMIN role") {
					val authService = mockk<AuthService>()
					val signInUseCase = SignInUseCase(authService)

					val input =
						SignInInput(
							email = "admin@example.com",
							password = "adminpassword"
						)

					val authOutput =
						AuthOutput(
							userId = 2L,
							email = "admin@example.com",
							name = "Admin User",
							role = UserRole.ADMIN,
							accessToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.admin.signature"
						)

					every { authService.signIn(input) } returns authOutput

					val result = signInUseCase.execute(input)

					result.shouldNotBeNull()
					result.role shouldBe UserRole.ADMIN
					result.email shouldBe "admin@example.com"
				}
			}
		}
	})
