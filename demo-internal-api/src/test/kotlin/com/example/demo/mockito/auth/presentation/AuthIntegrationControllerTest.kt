package com.example.demo.mockito.auth.presentation

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
import com.example.demo.mockito.common.BaseIntegrationController
import com.example.demo.mockito.common.security.WithMockCustomUser
import com.example.demo.user.constant.UserRole
import com.example.demo.user.exception.UserNotFoundException
import com.example.demo.user.exception.UserUnAuthorizedException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.whenever
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders

@ActiveProfiles("test")
@Tag("mockito-integration-test")
@DisplayName("Mockito Integration - Auth Controller Test")
@WebMvcTest(AuthController::class)
@ExtendWith(MockitoExtension::class)
class AuthIntegrationControllerTest : BaseIntegrationController() {
	@MockitoBean
	private lateinit var signInUseCase: SignInUseCase

	@MockitoBean
	private lateinit var signOutUseCase: SignOutUseCase

	@MockitoBean
	private lateinit var refreshAccessTokenUseCase: RefreshAccessTokenUseCase

	private val defaultUserEmail = "user@example.com"
	private val defaultUserPassword = "test_password_123!@"
	private val defaultAccessToken =
		"""eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"""

	private val testUserId = 1L
	private val testUserName = "Test User"
	private val testUserRole = UserRole.USER

	@BeforeEach
	fun setUp() {
		mockMvc =
			MockMvcBuilders
				.webAppContextSetup(webApplicationContext)
				.apply<DefaultMockMvcBuilder>(SecurityMockMvcConfigurers.springSecurity())
				.alwaysDo<DefaultMockMvcBuilder>(MockMvcResultHandlers.print())
				.build()
	}

	@Nested
	@DisplayName("POST /api/v1/auth/signIn Test")
	inner class SignInTest {
		private val signInRequest =
			SignInRequest(
				email = defaultUserEmail,
				password = defaultUserPassword
			)

		@Test
		@DisplayName("POST /api/v1/auth/signIn - Success Response")
		@WithMockCustomUser
		@Throws(Exception::class)
		fun should_ExpectOKResponseToSignInResponse_when_GivenSignInRequest() {
			val authOutput =
				AuthOutput(
					userId = testUserId,
					email = defaultUserEmail,
					name = testUserName,
					role = testUserRole,
					accessToken = defaultAccessToken
				)

			whenever(signInUseCase.execute(any<SignInInput>()))
				.thenReturn(authOutput)

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
				.andExpect(MockMvcResultMatchers.jsonPath("$.data.userId").value(testUserId))
				.andExpect(MockMvcResultMatchers.jsonPath("$.data.email").value(defaultUserEmail))
				.andExpect(MockMvcResultMatchers.jsonPath("$.data.name").value(testUserName))
				.andExpect(MockMvcResultMatchers.jsonPath("$.data.role").value(testUserRole.name))
				.andExpect(MockMvcResultMatchers.jsonPath("$.data.accessToken").value(defaultAccessToken))
		}

		@Test
		@DisplayName("POST /api/v1/auth/signIn - Field Validation Error")
		@WithMockCustomUser
		@Throws(Exception::class)
		fun should_ExpectErrorResponseToValidException_when_GivenWrongSignInRequest() {
			val wrongSignInRequest =
				SignInRequest(
					email = "wrong_email_format",
					password = "1234"
				)

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

		@Test
		@DisplayName("POST /api/v1/auth/signIn - User UnAuthorized Exception")
		@WithMockCustomUser
		@Throws(Exception::class)
		fun should_ExpectErrorResponseToUserUnAuthorizedException_when_GivenSignInRequest() {
			val userUnAuthorizedException = UserUnAuthorizedException("User UnAuthorized userId = $testUserId")

			whenever(signInUseCase.execute(any<SignInInput>()))
				.thenThrow(userUnAuthorizedException)

			mockMvc
				.perform(
					MockMvcRequestBuilders
						.post("/api/v1/auth/signIn")
						.with(SecurityMockMvcRequestPostProcessors.csrf())
						.content(objectMapper.writeValueAsString(signInRequest))
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)
				).andExpect(MockMvcResultMatchers.status().isUnauthorized)
				.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(userUnAuthorizedException.message))
				.andExpect(MockMvcResultMatchers.jsonPath("$.errors").isEmpty)
		}

		@Test
		@DisplayName("POST /api/v1/auth/signIn - User Not Found Exception")
		@WithMockCustomUser
		@Throws(Exception::class)
		fun should_ExpectErrorResponseToUserNotFoundException_when_GivenSignInRequest() {
			val userNotFoundException = UserNotFoundException(testUserId)

			whenever(signInUseCase.execute(any<SignInInput>()))
				.thenThrow(userNotFoundException)

			mockMvc
				.perform(
					MockMvcRequestBuilders
						.post("/api/v1/auth/signIn")
						.with(SecurityMockMvcRequestPostProcessors.csrf())
						.content(objectMapper.writeValueAsString(signInRequest))
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)
				).andExpect(MockMvcResultMatchers.status().isNotFound)
				.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(userNotFoundException.message))
				.andExpect(MockMvcResultMatchers.jsonPath("$.errors").isEmpty)
		}
	}

	@Nested
	@DisplayName("POST /api/v1/auth/signOut Test")
	inner class SignOutTest {
		@Test
		@DisplayName("POST /api/v1/auth/signOut - Success Response")
		@WithMockCustomUser
		@Throws(Exception::class)
		fun should_ExpectOKResponseToSignOutVoidResponse_when_UserIsAuthenticated() {
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

		@Test
		@DisplayName("POST /api/v1/auth/signOut - Unauthorized when not authenticated")
		@Throws(Exception::class)
		fun should_ExpectErrorResponseToUnauthorizedException_when_UserIsNotAuthenticated() {
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

	@Nested
	@DisplayName("POST /api/v1/auth/refresh Test")
	inner class RefreshAccessTokenTest {
		private val refreshAccessTokenRequest =
			RefreshAccessTokenRequest(
				refreshToken = "valid-refresh-token"
			)

		@Test
		@DisplayName("POST /api/v1/auth/refresh - Success Response")
		@WithMockCustomUser
		@Throws(Exception::class)
		fun should_ExpectCreatedResponseToRefreshAccessTokenResponse_when_GivenValidRefreshToken() {
			val authOutput = AuthOutput.fromRefresh(defaultAccessToken)

			whenever(refreshAccessTokenUseCase.execute(any<RefreshAccessTokenInput>()))
				.thenReturn(authOutput)

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
				.andExpect(MockMvcResultMatchers.jsonPath("$.data.accessToken").value(defaultAccessToken))
		}

		@Test
		@DisplayName("POST /api/v1/auth/refresh - Refresh Token Not Found Exception")
		@WithMockCustomUser
		@Throws(Exception::class)
		fun should_ExpectErrorResponseToRefreshTokenNotFoundException_when_RefreshTokenNotFound() {
			val refreshTokenNotFoundException = RefreshTokenNotFoundException(testUserId)

			whenever(refreshAccessTokenUseCase.execute(any<RefreshAccessTokenInput>()))
				.thenThrow(refreshTokenNotFoundException)

			mockMvc
				.perform(
					MockMvcRequestBuilders
						.post("/api/v1/auth/refresh")
						.with(SecurityMockMvcRequestPostProcessors.csrf())
						.content(objectMapper.writeValueAsString(refreshAccessTokenRequest))
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)
				).andExpect(MockMvcResultMatchers.status().isUnauthorized)
				.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(refreshTokenNotFoundException.message))
				.andExpect(MockMvcResultMatchers.jsonPath("$.errors").isEmpty)
		}

		@Test
		@DisplayName("POST /api/v1/auth/refresh - Field Validation Error")
		@WithMockCustomUser
		@Throws(Exception::class)
		fun should_ExpectErrorResponseToValidException_when_GivenEmptyRefreshToken() {
			val invalidRequest =
				RefreshAccessTokenRequest(
					refreshToken = ""
				)

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

		@Test
		@DisplayName("POST /api/v1/auth/refresh - Unauthorized when not authenticated")
		@Throws(Exception::class)
		fun should_ExpectErrorResponseToUnauthorizedException_when_UserIsNotAuthenticated() {
			doNothing().whenever(signOutUseCase).execute(any<SignOutInput>())

			mockMvc
				.perform(
					MockMvcRequestBuilders
						.post("/api/v1/auth/refresh")
						.with(SecurityMockMvcRequestPostProcessors.csrf())
						.content(objectMapper.writeValueAsString(refreshAccessTokenRequest))
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)
				).andExpect(MockMvcResultMatchers.status().isUnauthorized)
		}
	}
}
