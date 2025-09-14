package com.example.demo.kotest.auth.usecase

import com.example.demo.auth.exception.RefreshTokenNotFoundException
import com.example.demo.auth.port.input.RefreshAccessTokenInput
import com.example.demo.auth.port.output.AuthOutput
import com.example.demo.auth.service.AuthService
import com.example.demo.auth.usecase.RefreshAccessTokenUseCase
import com.example.demo.user.constant.UserRole
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@Tags("kotest-unit-test")
class RefreshAccessTokenUseCaseTests :
	BehaviorSpec({

		Given("Refresh access token") {

			When("Valid refresh token provided") {
				Then("Should return new access token") {
					val authService = mockk<AuthService>()
					val refreshAccessTokenUseCase = RefreshAccessTokenUseCase(authService)

					val input =
						RefreshAccessTokenInput(
							refreshToken = "valid_refresh_token"
						)

					val authOutput =
						AuthOutput.fromRefresh(
							accessToken = "new_access_token"
						)

					every { authService.refreshAccessToken(input) } returns authOutput

					val result = refreshAccessTokenUseCase.execute(input)

					result.shouldNotBeNull()
					result.accessToken shouldBe "new_access_token"
					result.userId shouldBe 0L
					result.email shouldBe ""
					result.name shouldBe ""
					result.role shouldBe UserRole.USER

					verify(exactly = 1) { authService.refreshAccessToken(input) }
				}
			}

			When("Invalid refresh token provided") {
				Then("Should throw RefreshTokenNotFoundException") {
					val authService = mockk<AuthService>()
					val refreshAccessTokenUseCase = RefreshAccessTokenUseCase(authService)

					val input =
						RefreshAccessTokenInput(
							refreshToken = "invalid_refresh_token"
						)

					every { authService.refreshAccessToken(input) } throws RefreshTokenNotFoundException(1L)

					val exception =
						shouldThrowExactly<RefreshTokenNotFoundException> {
							refreshAccessTokenUseCase.execute(input)
						}

					exception.message shouldContain "Refresh Token Not Found"

					verify(exactly = 1) { authService.refreshAccessToken(input) }
				}
			}

			When("Expired refresh token provided") {
				Then("Should throw RefreshTokenNotFoundException") {
					val authService = mockk<AuthService>()
					val refreshAccessTokenUseCase = RefreshAccessTokenUseCase(authService)

					val input =
						RefreshAccessTokenInput(
							refreshToken = "expired_refresh_token"
						)

					every { authService.refreshAccessToken(input) } throws RefreshTokenNotFoundException(2L)

					val exception =
						shouldThrowExactly<RefreshTokenNotFoundException> {
							refreshAccessTokenUseCase.execute(input)
						}

					exception.message shouldContain "Refresh Token Not Found"

					verify(exactly = 1) { authService.refreshAccessToken(input) }
				}
			}

			When("Service returns new token with additional data") {
				Then("Should return complete auth output") {
					val authService = mockk<AuthService>()
					val refreshAccessTokenUseCase = RefreshAccessTokenUseCase(authService)

					val input =
						RefreshAccessTokenInput(
							refreshToken = "refresh_token_with_data"
						)

					val authOutput =
						AuthOutput(
							userId = 5L,
							email = "refreshed@example.com",
							name = "Refreshed User",
							role = UserRole.USER,
							accessToken = "new_access_token_with_data"
						)

					every { authService.refreshAccessToken(input) } returns authOutput

					val result = refreshAccessTokenUseCase.execute(input)

					result.shouldNotBeNull()
					result.accessToken shouldBe "new_access_token_with_data"
					result.userId shouldBe 5L
					result.email shouldBe "refreshed@example.com"
					result.name shouldBe "Refreshed User"
					result.role shouldBe UserRole.USER
				}
			}
		}
	})
