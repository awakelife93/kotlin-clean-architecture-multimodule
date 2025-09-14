package com.example.demo.mockito.auth.service

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
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime

@ActiveProfiles("test")
@Tag("mockito-unit-test")
@DisplayName("Mockito Unit - Auth Service Test")
@ExtendWith(MockitoExtension::class)
class AuthServiceTests {
	@Mock
	private lateinit var userPort: UserPort

	@Mock
	private lateinit var passwordEncoder: PasswordEncoder

	@Mock
	private lateinit var tokenProvider: TokenProvider

	@Mock
	private lateinit var jwtProvider: JWTProvider

	@InjectMocks
	private lateinit var authService: AuthService

	private fun createTestUser(
		id: Long = 1L,
		email: String = "test@example.com",
		password: String = "encoded_password",
		name: String = "Test User",
		role: UserRole = UserRole.USER
	) = User(
		id = id,
		email = email,
		password = password,
		name = name,
		role = role,
		createdDt = LocalDateTime.now(),
		updatedDt = LocalDateTime.now()
	)

	@Nested
	@DisplayName("Sign in")
	inner class SignInTest {
		@Test
		@DisplayName("Valid credentials should return auth output with token")
		fun signIn_withValidCredentials_returnsAuthOutput() {
			val user = createTestUser()
			val input = SignInInput(email = user.email, password = "password123")
			val accessToken = "test.access.token"

			whenever(userPort.findOneByEmail(user.email)) doReturn user
			whenever(passwordEncoder.matches(input.password, user.password)) doReturn true
			whenever(tokenProvider.createFullTokens(user)) doReturn accessToken

			val result = authService.signIn(input)

			assertNotNull(result)
			assertEquals(user.id, result.userId)
			assertEquals(user.email, result.email)
			assertEquals(user.name, result.name)
			assertEquals(user.role, result.role)
			assertEquals(accessToken, result.accessToken)

			verify(userPort).findOneByEmail(user.email)
			verify(passwordEncoder).matches(input.password, user.password)
			verify(tokenProvider).createFullTokens(user)
		}

		@Test
		@DisplayName("User not found should throw exception")
		fun signIn_withNonExistentUser_throwsNotFoundException() {
			val email = "notfound@example.com"
			val input = SignInInput(email = email, password = "password123")

			whenever(userPort.findOneByEmail(email)) doReturn null

			val exception =
				assertThrows<UserNotFoundException> {
					authService.signIn(input)
				}

			assertEquals("User Not Found email = $email", exception.message)

			verify(userPort).findOneByEmail(email)
			verify(passwordEncoder, never()).matches(input.password, "")
			verify(tokenProvider, never()).createFullTokens(createTestUser())
		}

		@Test
		@DisplayName("Invalid password should throw exception")
		fun signIn_withInvalidPassword_throwsUnauthorizedException() {
			val user = createTestUser()
			val input = SignInInput(email = user.email, password = "wrong")

			whenever(userPort.findOneByEmail(user.email)) doReturn user
			whenever(passwordEncoder.matches(input.password, user.password)) doReturn false

			val exception =
				assertThrows<UserUnAuthorizedException> {
					authService.signIn(input)
				}

			assertEquals("User UnAuthorized email = ${user.email}", exception.message)

			verify(userPort).findOneByEmail(user.email)
			verify(passwordEncoder).matches(input.password, user.password)
			verify(tokenProvider, never()).createFullTokens(user)
		}
	}

	@Nested
	@DisplayName("Sign out")
	inner class SignOutTest {
		@Test
		@DisplayName("Should delete refresh token")
		fun signOut_withValidUserId_deletesRefreshToken() {
			val userId = 1L
			doNothing().whenever(tokenProvider).deleteRefreshToken(userId)

			authService.signOut(userId)

			verify(tokenProvider).deleteRefreshToken(userId)
		}
	}

	@Nested
	@DisplayName("Refresh access token")
	inner class RefreshAccessTokenTest {
		@Test
		@DisplayName("Valid refresh token should return new access token")
		fun refreshAccessToken_withValidToken_returnsNewToken() {
			val refreshToken = "valid.refresh.token"
			val newAccessToken = "new.access.token"
			val input = RefreshAccessTokenInput(refreshToken = refreshToken)

			val securityUserItem =
				SecurityUserItem(
					userId = 1L,
					email = "test@example.com",
					name = "Test User",
					role = UserRole.USER
				)
			val userAdapter =
				mock<UserAdapter> {
					on { this.securityUserItem } doReturn securityUserItem
				}
			val authentication = UsernamePasswordAuthenticationToken(userAdapter, null)

			whenever(jwtProvider.getAuthentication(refreshToken, true)) doReturn authentication
			whenever(tokenProvider.refreshAccessToken(securityUserItem)) doReturn newAccessToken

			val result = authService.refreshAccessToken(input)

			assertNotNull(result)
			assertEquals(newAccessToken, result.accessToken)

			verify(jwtProvider).getAuthentication(refreshToken, true)
			verify(tokenProvider).refreshAccessToken(securityUserItem)
		}
	}
}
