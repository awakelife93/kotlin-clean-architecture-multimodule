package com.example.demo.mockito.auth.usecase

import com.example.demo.auth.exception.RefreshTokenNotFoundException
import com.example.demo.auth.port.input.RefreshAccessTokenInput
import com.example.demo.auth.port.output.AuthOutput
import com.example.demo.auth.service.AuthService
import com.example.demo.auth.usecase.RefreshAccessTokenUseCase
import com.example.demo.user.constant.UserRole
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@Tag("mockito-unit-test")
@DisplayName("Mockito Unit - Refresh Access Token UseCase Test")
@ExtendWith(MockitoExtension::class)
class RefreshAccessTokenUseCaseTests {
	@Mock
	private lateinit var authService: AuthService

	@InjectMocks
	private lateinit var refreshAccessTokenUseCase: RefreshAccessTokenUseCase

	@Nested
	@DisplayName("Refresh access token")
	inner class RefreshAccessTokenTest {
		@Test
		@DisplayName("Valid refresh token provided")
		fun should_return_new_access_token() {
			val input =
				RefreshAccessTokenInput(
					refreshToken = "valid_refresh_token"
				)

			val authOutput =
				AuthOutput.fromRefresh(
					accessToken = "new_access_token"
				)

			whenever(authService.refreshAccessToken(input)) doReturn authOutput

			val result = refreshAccessTokenUseCase.execute(input)

			assertNotNull(result)
			assertEquals("new_access_token", result.accessToken)
			assertEquals(0L, result.userId)
			assertEquals("", result.email)
			assertEquals("", result.name)
			assertEquals(UserRole.USER, result.role)

			verify(authService, times(1)).refreshAccessToken(input)
		}

		@Test
		@DisplayName("Invalid refresh token provided")
		fun should_throw_refresh_token_not_found_exception() {
			val input =
				RefreshAccessTokenInput(
					refreshToken = "invalid_refresh_token"
				)

			whenever(authService.refreshAccessToken(input)).thenThrow(RefreshTokenNotFoundException(1L))

			val exception =
				assertThrows<RefreshTokenNotFoundException> {
					refreshAccessTokenUseCase.execute(input)
				}

			assertTrue(exception.message?.contains("Refresh Token Not Found") ?: false)

			verify(authService, times(1)).refreshAccessToken(input)
		}

		@Test
		@DisplayName("Expired refresh token provided")
		fun should_throw_exception_for_expired_token() {
			val input =
				RefreshAccessTokenInput(
					refreshToken = "expired_refresh_token"
				)

			whenever(authService.refreshAccessToken(input)).thenThrow(RefreshTokenNotFoundException(2L))

			val exception =
				assertThrows<RefreshTokenNotFoundException> {
					refreshAccessTokenUseCase.execute(input)
				}

			assertTrue(exception.message?.contains("Refresh Token Not Found") ?: false)

			verify(authService, times(1)).refreshAccessToken(input)
		}

		@Test
		@DisplayName("Service returns new token with additional data")
		fun should_return_complete_auth_output() {
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

			whenever(authService.refreshAccessToken(input)) doReturn authOutput

			val result = refreshAccessTokenUseCase.execute(input)

			assertNotNull(result)
			assertEquals("new_access_token_with_data", result.accessToken)
			assertEquals(5L, result.userId)
			assertEquals("refreshed@example.com", result.email)
			assertEquals("Refreshed User", result.name)
			assertEquals(UserRole.USER, result.role)
		}

		@Test
		@DisplayName("Empty refresh token provided")
		fun should_handle_empty_refresh_token() {
			val input =
				RefreshAccessTokenInput(
					refreshToken = ""
				)

			whenever(authService.refreshAccessToken(input)).thenThrow(RefreshTokenNotFoundException(0L))

			val exception =
				assertThrows<RefreshTokenNotFoundException> {
					refreshAccessTokenUseCase.execute(input)
				}

			assertNotNull(exception)
			verify(authService, times(1)).refreshAccessToken(input)
		}

		@Test
		@DisplayName("Very long refresh token provided")
		fun should_handle_long_refresh_token() {
			val longToken = "a".repeat(1000)
			val input =
				RefreshAccessTokenInput(
					refreshToken = longToken
				)

			val authOutput =
				AuthOutput.fromRefresh(
					accessToken = "new_access_token_for_long"
				)

			whenever(authService.refreshAccessToken(input)) doReturn authOutput

			val result = refreshAccessTokenUseCase.execute(input)

			assertNotNull(result)
			assertEquals("new_access_token_for_long", result.accessToken)

			verify(authService, times(1)).refreshAccessToken(input)
		}
	}
}
