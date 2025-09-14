package com.example.demo.mockito.auth.presentation

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
import com.example.demo.security.model.SecurityUserItem
import com.example.demo.user.constant.UserRole
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@Tag("mockito-unit-test")
@DisplayName("Mockito Unit - Auth Controller Test")
@ExtendWith(MockitoExtension::class)
class AuthControllerTests {
	@InjectMocks
	private lateinit var authController: AuthController

	@Mock
	private lateinit var signInUseCase: SignInUseCase

	@Mock
	private lateinit var signOutUseCase: SignOutUseCase

	@Mock
	private lateinit var refreshAccessTokenUseCase: RefreshAccessTokenUseCase

	private val defaultAccessToken =
		"""eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"""

	@Test
	@DisplayName("Sign in - should return SignInResponse when given valid SignInRequest")
	fun should_ReturnSignInResponse_when_GivenValidSignInRequest() {
		val signInRequest =
			SignInRequest(
				email = "test@example.com",
				password = "password123"
			)

		val authOutput =
			AuthOutput(
				userId = 1L,
				email = "test@example.com",
				name = "Test User",
				role = UserRole.USER,
				accessToken = defaultAccessToken
			)

		whenever(
			signInUseCase.execute(
				SignInInput(
					email = signInRequest.email,
					password = signInRequest.password
				)
			)
		).thenReturn(authOutput)

		val response = authController.signIn(signInRequest)

		assertNotNull(response)
		assertNotNull(response.body)
		assertEquals(HttpStatus.OK, response.statusCode)

		val body =
			requireNotNull(response.body) {
				"Response body must not be null"
			}

		assertEquals(authOutput.userId, body.userId)
		assertEquals(authOutput.email, body.email)
		assertEquals(authOutput.name, body.name)
		assertEquals(authOutput.role, body.role)
		assertEquals(authOutput.accessToken, body.accessToken)

		verify(signInUseCase).execute(
			SignInInput(
				email = signInRequest.email,
				password = signInRequest.password
			)
		)
	}

	@Test
	@DisplayName("Sign out - should return void response when given SecurityUserItem")
	fun should_ReturnVoidResponse_when_GivenSecurityUserItem() {
		val securityUserItem =
			SecurityUserItem(
				userId = 1L,
				email = "test@example.com",
				name = "Test User",
				role = UserRole.USER
			)

		val response = authController.signOut(securityUserItem)

		assertNotNull(response)
		assertNull(response.body)
		assertEquals(HttpStatus.OK, response.statusCode)

		verify(signOutUseCase).execute(
			SignOutInput(userId = securityUserItem.userId)
		)
	}

	@Test
	@DisplayName("Refresh access token - should return new access token when given valid refresh token")
	fun should_ReturnRefreshAccessTokenResponse_when_GivenValidRefreshToken() {
		val refreshAccessTokenRequest =
			RefreshAccessTokenRequest(
				refreshToken = "valid-refresh-token"
			)

		val authOutput = AuthOutput.fromRefresh(defaultAccessToken)

		whenever(
			refreshAccessTokenUseCase.execute(
				RefreshAccessTokenInput(
					refreshToken = refreshAccessTokenRequest.refreshToken
				)
			)
		).thenReturn(authOutput)

		val response = authController.refreshAccessToken(refreshAccessTokenRequest)

		assertNotNull(response)
		assertNotNull(response.body)
		assertEquals(HttpStatus.CREATED, response.statusCode)

		val body =
			requireNotNull(response.body) {
				"Response body must not be null"
			}

		assertEquals(defaultAccessToken, body.accessToken)

		verify(refreshAccessTokenUseCase).execute(
			RefreshAccessTokenInput(
				refreshToken = refreshAccessTokenRequest.refreshToken
			)
		)
	}

	@Test
	@DisplayName("Sign in - should correctly map request to input and output to response")
	fun should_CorrectlyMapRequestAndResponse_when_SignIn() {
		val signInRequest =
			SignInRequest(
				email = "mapping@example.com",
				password = "mappingPassword123"
			)
		val authOutput =
			AuthOutput(
				userId = 999L,
				email = "mapping@example.com",
				name = "Mapping User",
				role = UserRole.ADMIN,
				accessToken = "mapping-access-token"
			)

		whenever(
			signInUseCase.execute(
				SignInInput(
					email = signInRequest.email,
					password = signInRequest.password
				)
			)
		).thenReturn(authOutput)

		val response = authController.signIn(signInRequest)

		assertNotNull(response)
		assertEquals(HttpStatus.OK, response.statusCode)

		val body = requireNotNull(response.body)

		assertEquals(authOutput.userId, body.userId)
		assertEquals(authOutput.email, body.email)
		assertEquals(authOutput.name, body.name)
		assertEquals(authOutput.role, body.role)
		assertEquals(authOutput.accessToken, body.accessToken)
	}

	@Test
	@DisplayName("Refresh token - should correctly map request to input and output to response")
	fun should_CorrectlyMapRequestAndResponse_when_RefreshToken() {
		val refreshToken = "refresh-token-${System.currentTimeMillis()}"
		val newAccessToken = "new-access-token-${System.currentTimeMillis()}"

		val refreshAccessTokenRequest =
			RefreshAccessTokenRequest(
				refreshToken = refreshToken
			)

		val authOutput = AuthOutput.fromRefresh(newAccessToken)

		whenever(
			refreshAccessTokenUseCase.execute(
				RefreshAccessTokenInput(
					refreshToken = refreshAccessTokenRequest.refreshToken
				)
			)
		).thenReturn(authOutput)

		val response = authController.refreshAccessToken(refreshAccessTokenRequest)

		assertNotNull(response)
		assertEquals(HttpStatus.CREATED, response.statusCode)

		val body = requireNotNull(response.body)

		assertEquals(newAccessToken, body.accessToken)

		verify(refreshAccessTokenUseCase).execute(
			RefreshAccessTokenInput(refreshToken = refreshToken)
		)
	}
}
