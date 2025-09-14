package com.example.demo.mockito.user.usecase

import com.example.demo.user.constant.UserRole
import com.example.demo.user.exception.UserAlreadyExistsException
import com.example.demo.user.port.input.CreateUserInput
import com.example.demo.user.port.output.UserOutput
import com.example.demo.user.service.UserService
import com.example.demo.user.usecase.CreateUserUseCase
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
import java.time.LocalDateTime

@ActiveProfiles("test")
@Tag("mockito-unit-test")
@DisplayName("Mockito Unit - Create User UseCase Test")
@ExtendWith(MockitoExtension::class)
class CreateUserUseCaseTests {
	@Mock
	private lateinit var userService: UserService

	@InjectMocks
	private lateinit var createUserUseCase: CreateUserUseCase

	@Nested
	@DisplayName("Create user")
	inner class CreateUserTest {
		@Test
		@DisplayName("Valid user data provided")
		fun should_create_user_successfully() {
			val input =
				CreateUserInput(
					email = "newuser@example.com",
					password = "password123",
					name = "New User"
				)

			val expectedOutput =
				UserOutput.AuthenticatedUserOutput(
					id = 1L,
					email = input.email,
					name = input.name,
					role = UserRole.USER,
					accessToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.signature",
					createdDt = LocalDateTime.now(),
					updatedDt = LocalDateTime.now()
				)

			whenever(userService.registerNewUser(input)) doReturn expectedOutput

			val result = createUserUseCase.execute(input)

			assertNotNull(result)
			assertEquals(1L, result.id)
			assertEquals("newuser@example.com", result.email)
			assertEquals("New User", result.name)
			assertEquals(UserRole.USER, result.role)
			assertEquals(expectedOutput.accessToken, result.accessToken)

			verify(userService, times(1)).registerNewUser(input)
		}

		@Test
		@DisplayName("Email already exists")
		fun should_throw_already_exist_exception() {
			val input =
				CreateUserInput(
					email = "existing@example.com",
					password = "password123",
					name = "Existing User"
				)

			whenever(userService.registerNewUser(input))
				.thenThrow(UserAlreadyExistsException(input.email))

			val exception =
				assertThrows<UserAlreadyExistsException> {
					createUserUseCase.execute(input)
				}

			assertEquals("Already User Exist email = existing@example.com", exception.message)
			verify(userService, times(1)).registerNewUser(input)
		}

		@Test
		@DisplayName("Create multiple users")
		fun should_create_each_with_unique_token() {
			val input =
				CreateUserInput(
					email = "user2@example.com",
					password = "pass456",
					name = "User Two"
				)

			val expectedOutput =
				UserOutput.AuthenticatedUserOutput(
					id = 2L,
					email = input.email,
					name = input.name,
					role = UserRole.USER,
					accessToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.user2.signature",
					createdDt = LocalDateTime.now(),
					updatedDt = LocalDateTime.now()
				)

			whenever(userService.registerNewUser(input)) doReturn expectedOutput

			val result = createUserUseCase.execute(input)

			assertNotNull(result)
			assertEquals(expectedOutput.accessToken, result.accessToken)
			verify(userService, times(1)).registerNewUser(input)
		}
	}
}
