package com.example.demo.mockito.auth.usecase

import com.example.demo.auth.port.input.SignInInput
import com.example.demo.auth.port.output.AuthOutput
import com.example.demo.auth.service.AuthService
import com.example.demo.auth.usecase.SignInUseCase
import com.example.demo.user.constant.UserRole
import com.example.demo.user.exception.UserNotFoundException
import com.example.demo.user.exception.UserUnAuthorizedException
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
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@Tag("mockito-unit-test")
@DisplayName("Mockito Unit - Sign In UseCase Test")
@ExtendWith(MockitoExtension::class)
class SignInUseCaseTests {
	@Mock
	private lateinit var authService: AuthService

	@InjectMocks
	private lateinit var signInUseCase: SignInUseCase

	@Nested
	@DisplayName("Sign in")
	inner class SignInTest {
		@Test
		@DisplayName("Valid credentials provided")
		fun should_return_auth_output_successfully() {
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

			whenever(authService.signIn(input)) doReturn authOutput

			val result = signInUseCase.execute(input)

			assertNotNull(result)
			assertEquals(1L, result.userId)
			assertEquals("test@example.com", result.email)
			assertEquals("Test User", result.name)
			assertEquals(UserRole.USER, result.role)
			assertEquals(authOutput.accessToken, result.accessToken)

			verify(authService, times(1)).signIn(input)
		}

		@Test
		@DisplayName("Invalid email provided")
		fun should_throw_user_not_found_exception() {
			val input =
				SignInInput(
					email = "notfound@example.com",
					password = "password123"
				)

			whenever(authService.signIn(input)).thenThrow(UserNotFoundException(1L))

			assertThrows<UserNotFoundException> {
				signInUseCase.execute(input)
			}

			verify(authService, times(1)).signIn(input)
		}

		@Test
		@DisplayName("Invalid password provided")
		fun should_throw_user_unauthorized_exception() {
			val input =
				SignInInput(
					email = "test@example.com",
					password = "wrongpassword"
				)

			whenever(authService.signIn(input)).thenThrow(UserUnAuthorizedException("test@example.com"))

			val exception =
				assertThrows<UserUnAuthorizedException> {
					signInUseCase.execute(input)
				}

			assertEquals("test@example.com", exception.message)

			verify(authService, times(1)).signIn(input)
		}

		@Test
		@DisplayName("Admin user signs in")
		fun should_return_auth_output_with_admin_role() {
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

			whenever(authService.signIn(input)) doReturn authOutput

			val result = signInUseCase.execute(input)

			assertNotNull(result)
			assertEquals(UserRole.ADMIN, result.role)
			assertEquals("admin@example.com", result.email)
		}
	}
}
