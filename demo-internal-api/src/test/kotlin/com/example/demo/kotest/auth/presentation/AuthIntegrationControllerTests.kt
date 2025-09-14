package com.example.demo.kotest.auth.presentation

import com.example.demo.auth.exception.RefreshTokenNotFoundException
import com.example.demo.auth.port.input.RefreshAccessTokenInput
import com.example.demo.auth.port.input.SignInInput
import com.example.demo.auth.port.input.SignOutInput
import com.example.demo.auth.port.output.AuthOutput
import com.example.demo.auth.presentation.AuthController
import com.example.demo.auth.presentation.dto.request.RefreshAccessTokenRequest
import com.example.demo.auth.presentation.dto.request.SignInRequest
import com.example.demo.auth.usecase.RefreshAccessTokenUseCase
import com.example.demo.auth.usecase.SignInUseCase
import com.example.demo.auth.usecase.SignOutUseCase
import com.example.demo.kotest.common.BaseIntegrationController
import com.example.demo.kotest.common.security.SecurityListenerFactory
import com.example.demo.user.constant.UserRole
import com.example.demo.user.exception.UserNotFoundException
import com.example.demo.user.exception.UserUnAuthorizedException
import com.example.demo.user.model.User
import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.annotation.Tags
import io.mockk.every
import io.mockk.justRun
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@ActiveProfiles("test")
@Tags("kotest-integration-test")
@WebMvcTest(AuthController::class)
class AuthIntegrationControllerTests : BaseIntegrationController() {
	@MockkBean
	private lateinit var signInUseCase: SignInUseCase

	@MockkBean
	private lateinit var signOutUseCase: SignOutUseCase

	@MockkBean
	private lateinit var refreshAccessTokenUseCase: RefreshAccessTokenUseCase

	private val user =
		User(
			id = 1L,
			email = "test@example.com",
			name = "Test User",
			password = "password123!@#",
			role = UserRole.USER
		)

	private val defaultUserEmail = "user@example.com"
	private val defaultUserPassword = "test_password_123!@"
	private val defaultAccessToken =
		"""eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"""
	private val defaultRefreshToken = "refresh_token_example"

	private val authOutput =
		AuthOutput(
			userId = user.id,
			email = user.email,
			name = user.name,
			role = user.role,
			accessToken = defaultAccessToken
		)

	init {
		initialize()

		Given("POST /api/v1/auth/signIn") {
			val signInRequest =
				SignInRequest(
					email = defaultUserEmail,
					password = defaultUserPassword
				)

			When("Success POST /api/v1/auth/signIn") {

				every { signInUseCase.execute(any<SignInInput>()) } returns authOutput

				Then("Call POST /api/v1/auth/signIn") {
					mockMvc
						.perform(
							MockMvcRequestBuilders
								.post("/api/v1/auth/signIn")
								.with(SecurityMockMvcRequestPostProcessors.csrf())
								.content(objectMapper.writeValueAsString(signInRequest))
								.contentType(MediaType.APPLICATION_JSON)
								.accept(MediaType.APPLICATION_JSON)
						).andExpect(MockMvcResultMatchers.status().isOk)
						.andExpect(MockMvcResultMatchers.jsonPath("$.code").value(commonStatus))
						.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(commonMessage))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.userId").value(user.id))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.email").value(user.email))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.name").value(user.name))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.role").value(user.role.name))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.accessToken").value(defaultAccessToken))
				}
			}

			When("Field Valid Exception POST /api/v1/auth/signIn") {
				val wrongSignInRequest =
					SignInRequest(
						email = "wrong_email_format",
						password = "1234"
					)

				Then("POST /api/v1/auth/signIn with invalid fields") {
					mockMvc
						.perform(
							MockMvcRequestBuilders
								.post("/api/v1/auth/signIn")
								.with(SecurityMockMvcRequestPostProcessors.csrf())
								.content(objectMapper.writeValueAsString(wrongSignInRequest))
								.contentType(MediaType.APPLICATION_JSON)
								.accept(MediaType.APPLICATION_JSON)
						).andExpect(MockMvcResultMatchers.status().isBadRequest)
						.andExpect(MockMvcResultMatchers.jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
						.andExpect(MockMvcResultMatchers.jsonPath("$.message").isString)
						.andExpect(MockMvcResultMatchers.jsonPath("$.errors").isNotEmpty)
				}
			}

			When("UnAuthorized Exception POST /api/v1/auth/signIn") {
				val userUnAuthorizedException =
					UserUnAuthorizedException(
						"User UnAuthorized email = ${user.email}"
					)

				every { signInUseCase.execute(any<SignInInput>()) } throws userUnAuthorizedException

				Then("Call POST /api/v1/auth/signIn with unauthorized") {
					mockMvc
						.perform(
							MockMvcRequestBuilders
								.post("/api/v1/auth/signIn")
								.with(SecurityMockMvcRequestPostProcessors.csrf())
								.content(objectMapper.writeValueAsString(signInRequest))
								.contentType(MediaType.APPLICATION_JSON)
								.accept(MediaType.APPLICATION_JSON)
						).andExpect(MockMvcResultMatchers.status().isUnauthorized)
						.andExpect(MockMvcResultMatchers.jsonPath("$.code").value(HttpStatus.UNAUTHORIZED.value()))
						.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(userUnAuthorizedException.message))
						.andExpect(MockMvcResultMatchers.jsonPath("$.errors").isEmpty)
				}
			}

			When("Not Found Exception POST /api/v1/auth/signIn") {
				val userNotFoundException = UserNotFoundException(user.id)

				every { signInUseCase.execute(any<SignInInput>()) } throws userNotFoundException

				Then("Call POST /api/v1/auth/signIn with user not found") {
					mockMvc
						.perform(
							MockMvcRequestBuilders
								.post("/api/v1/auth/signIn")
								.with(SecurityMockMvcRequestPostProcessors.csrf())
								.content(objectMapper.writeValueAsString(signInRequest))
								.contentType(MediaType.APPLICATION_JSON)
								.accept(MediaType.APPLICATION_JSON)
						).andExpect(MockMvcResultMatchers.status().isNotFound)
						.andExpect(MockMvcResultMatchers.jsonPath("$.code").value(HttpStatus.NOT_FOUND.value()))
						.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(userNotFoundException.message))
						.andExpect(MockMvcResultMatchers.jsonPath("$.errors").isEmpty)
				}
			}
		}

		Given("POST /api/v1/auth/signOut") {
			When("Success POST /api/v1/auth/signOut") {
				justRun { signOutUseCase.execute(any<SignOutInput>()) }

				Then("Call POST /api/v1/auth/signOut") {
					mockMvc
						.perform(
							MockMvcRequestBuilders
								.post("/api/v1/auth/signOut")
								.with(SecurityMockMvcRequestPostProcessors.csrf())
								.contentType(MediaType.APPLICATION_JSON)
								.accept(MediaType.APPLICATION_JSON)
						).andExpect(MockMvcResultMatchers.status().isOk)
						.andExpect(MockMvcResultMatchers.jsonPath("$.code").value(commonStatus))
						.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(commonMessage))
				}
			}
		}

		Given("POST /api/v1/auth/refresh") {
			val refreshAccessTokenRequest =
				RefreshAccessTokenRequest(
					refreshToken = defaultRefreshToken
				)

			When("Success POST /api/v1/auth/refresh") {
				val newAccessToken = "new_access_token_example"
				val authOutput = AuthOutput.fromRefresh(newAccessToken)

				every { refreshAccessTokenUseCase.execute(any<RefreshAccessTokenInput>()) } returns authOutput

				Then("Call POST /api/v1/auth/refresh") {
					mockMvc
						.perform(
							MockMvcRequestBuilders
								.post("/api/v1/auth/refresh")
								.with(SecurityMockMvcRequestPostProcessors.csrf())
								.content(objectMapper.writeValueAsString(refreshAccessTokenRequest))
								.contentType(MediaType.APPLICATION_JSON)
								.accept(MediaType.APPLICATION_JSON)
						).andExpect(MockMvcResultMatchers.status().isCreated)
						.andExpect(MockMvcResultMatchers.jsonPath("$.code").value(commonStatus))
						.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(commonMessage))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.accessToken").value(newAccessToken))
				}
			}

			When("Not Found Exception POST /api/v1/auth/refresh") {
				val refreshTokenNotFoundException = RefreshTokenNotFoundException(user.id)

				every { refreshAccessTokenUseCase.execute(any<RefreshAccessTokenInput>()) } throws refreshTokenNotFoundException

				Then("Call POST /api/v1/auth/refresh with token not found") {
					mockMvc
						.perform(
							MockMvcRequestBuilders
								.post("/api/v1/auth/refresh")
								.with(SecurityMockMvcRequestPostProcessors.csrf())
								.content(objectMapper.writeValueAsString(refreshAccessTokenRequest))
								.contentType(MediaType.APPLICATION_JSON)
								.accept(MediaType.APPLICATION_JSON)
						).andExpect(MockMvcResultMatchers.status().isUnauthorized)
						.andExpect(MockMvcResultMatchers.jsonPath("$.code").value(HttpStatus.UNAUTHORIZED.value()))
						.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(refreshTokenNotFoundException.message))
						.andExpect(MockMvcResultMatchers.jsonPath("$.errors").isEmpty)
				}
			}

			When("Field Valid Exception POST /api/v1/auth/refresh") {
				val invalidRequest =
					RefreshAccessTokenRequest(
						refreshToken = ""
					)

				Then("Call POST /api/v1/auth/refresh with empty token") {
					mockMvc
						.perform(
							MockMvcRequestBuilders
								.post("/api/v1/auth/refresh")
								.with(SecurityMockMvcRequestPostProcessors.csrf())
								.content(objectMapper.writeValueAsString(invalidRequest))
								.contentType(MediaType.APPLICATION_JSON)
								.accept(MediaType.APPLICATION_JSON)
						).andExpect(MockMvcResultMatchers.status().isBadRequest)
						.andExpect(MockMvcResultMatchers.jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
						.andExpect(MockMvcResultMatchers.jsonPath("$.errors").isNotEmpty)
				}
			}
		}

		Given("Spring Security Context is not set") {
			When("Unauthorized Exception POST /api/v1/auth/signOut") {
				Then("Call POST /api/v1/auth/signOut without authentication")
					.config(tags = setOf(SecurityListenerFactory.NonSecurityOption)) {
						mockMvc
							.perform(
								MockMvcRequestBuilders
									.post("/api/v1/auth/signOut")
									.with(SecurityMockMvcRequestPostProcessors.csrf())
									.contentType(MediaType.APPLICATION_JSON)
									.accept(MediaType.APPLICATION_JSON)
							).andExpect(MockMvcResultMatchers.status().isUnauthorized)
					}
			}

			When("Unauthorized Exception POST /api/v1/auth/refresh") {
				Then("Call POST /api/v1/auth/refresh without authentication")
					.config(tags = setOf(SecurityListenerFactory.NonSecurityOption)) {
						mockMvc
							.perform(
								MockMvcRequestBuilders
									.post("/api/v1/auth/refresh")
									.with(SecurityMockMvcRequestPostProcessors.csrf())
									.contentType(MediaType.APPLICATION_JSON)
									.accept(MediaType.APPLICATION_JSON)
							).andExpect(MockMvcResultMatchers.status().isUnauthorized)
					}
			}
		}
	}
}
