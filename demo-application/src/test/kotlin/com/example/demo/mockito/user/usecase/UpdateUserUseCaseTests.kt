package com.example.demo.mockito.user.usecase

import com.example.demo.user.constant.UserRole
import com.example.demo.user.exception.UserNotFoundException
import com.example.demo.user.model.User
import com.example.demo.user.port.input.UpdateUserInput
import com.example.demo.user.service.UserService
import com.example.demo.user.usecase.UpdateUserUseCase
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
@DisplayName("Mockito Unit - Update User UseCase Test")
@ExtendWith(MockitoExtension::class)
class UpdateUserUseCaseTests {
	@Mock
	private lateinit var userService: UserService

	@InjectMocks
	private lateinit var updateUserUseCase: UpdateUserUseCase

	@Nested
	@DisplayName("Update user")
	inner class UpdateUserTest {
		@Test
		@DisplayName("Update existing user")
		fun should_update_user_successfully() {
			val input =
				UpdateUserInput(
					userId = 1L,
					name = "Updated Name",
					role = "ADMIN"
				)

			val updatedUser =
				User(
					id = 1L,
					email = "updated@example.com",
					password = "encoded_password",
					name = "Updated Name",
					role = UserRole.USER,
					createdDt = LocalDateTime.now().minusDays(30),
					updatedDt = LocalDateTime.now()
				)

			whenever(userService.updateUserInfo(input.userId, input.name)) doReturn updatedUser

			val result = updateUserUseCase.execute(input)

			assertNotNull(result)
			assertEquals(1L, result.id)
			assertEquals("Updated Name", result.name)
			assertEquals("updated@example.com", result.email)
			assertEquals(UserRole.USER, result.role)

			verify(userService, times(1)).updateUserInfo(input.userId, input.name)
		}

		@Test
		@DisplayName("Update non-existent user")
		fun should_throw_user_not_found_exception() {
			val input =
				UpdateUserInput(
					userId = 999L,
					name = "Updated Name",
					role = "USER"
				)

			whenever(userService.updateUserInfo(input.userId, input.name))
				.thenThrow(UserNotFoundException(input.userId))

			val exception =
				assertThrows<UserNotFoundException> {
					updateUserUseCase.execute(input)
				}

			assertEquals("User Not Found userId = 999", exception.message)
			verify(userService, times(1)).updateUserInfo(input.userId, input.name)
		}

		@Test
		@DisplayName("Update only name")
		fun should_update_name_keeping_other_fields() {
			val input =
				UpdateUserInput(
					userId = 2L,
					name = "New Name Only"
				)

			val updatedUser =
				User(
					id = 2L,
					email = "user@example.com",
					password = "encoded_password",
					name = "New Name Only",
					role = UserRole.USER,
					createdDt = LocalDateTime.now().minusDays(10),
					updatedDt = LocalDateTime.now()
				)

			whenever(userService.updateUserInfo(input.userId, input.name)) doReturn updatedUser

			val result = updateUserUseCase.execute(input)

			assertNotNull(result)
			assertEquals("New Name Only", result.name)
			assertEquals(UserRole.USER, result.role)
			assertEquals("user@example.com", result.email)

			verify(userService, times(1)).updateUserInfo(input.userId, input.name)
		}
	}
}
