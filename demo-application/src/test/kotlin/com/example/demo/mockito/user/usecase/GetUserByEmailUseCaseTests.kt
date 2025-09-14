package com.example.demo.mockito.user.usecase

import com.example.demo.user.constant.UserRole
import com.example.demo.user.exception.UserNotFoundException
import com.example.demo.user.model.User
import com.example.demo.user.port.input.GetUserByEmailInput
import com.example.demo.user.service.UserService
import com.example.demo.user.usecase.GetUserByEmailUseCase
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
@DisplayName("Mockito Unit - Get User By Email UseCase Test")
@ExtendWith(MockitoExtension::class)
class GetUserByEmailUseCaseTests {
	@Mock
	private lateinit var userService: UserService

	@InjectMocks
	private lateinit var getUserByEmailUseCase: GetUserByEmailUseCase

	@Nested
	@DisplayName("Get user by email")
	inner class GetUserByEmailTest {
		@Test
		@DisplayName("User with email exists")
		fun should_return_user_successfully() {
			val input = GetUserByEmailInput(email = "test@example.com")

			val user =
				User(
					id = 1L,
					email = "test@example.com",
					password = "encoded_password",
					name = "Test User",
					role = UserRole.USER,
					createdDt = LocalDateTime.now(),
					updatedDt = LocalDateTime.now()
				)

			whenever(userService.findOneByEmailOrThrow(input.email)) doReturn user

			val result = getUserByEmailUseCase.execute(input)

			assertNotNull(result)
			assertEquals(1L, result.id)
			assertEquals("test@example.com", result.email)
			assertEquals("Test User", result.name)
			assertEquals(UserRole.USER, result.role)

			verify(userService, times(1)).findOneByEmailOrThrow(input.email)
		}

		@Test
		@DisplayName("User with email does not exist")
		fun should_throw_user_not_found_exception() {
			val input = GetUserByEmailInput(email = "notfound@example.com")

			whenever(userService.findOneByEmailOrThrow(input.email))
				.thenThrow(UserNotFoundException(input.email))

			val exception =
				assertThrows<UserNotFoundException> {
					getUserByEmailUseCase.execute(input)
				}

			assertEquals("User Not Found email = notfound@example.com", exception.message)

			verify(userService, times(1)).findOneByEmailOrThrow(input.email)
		}
	}
}
