package com.example.demo.mockito.user.usecase

import com.example.demo.user.constant.UserRole
import com.example.demo.user.exception.UserNotFoundException
import com.example.demo.user.model.User
import com.example.demo.user.port.input.GetUserByIdInput
import com.example.demo.user.service.UserService
import com.example.demo.user.usecase.GetUserByIdUseCase
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
@DisplayName("Mockito Unit - Get User By ID UseCase Test")
@ExtendWith(MockitoExtension::class)
class GetUserByIdUseCaseTests {
	@Mock
	private lateinit var userService: UserService

	@InjectMocks
	private lateinit var getUserByIdUseCase: GetUserByIdUseCase

	@Nested
	@DisplayName("Get user by ID")
	inner class GetUserByIdTest {
		@Test
		@DisplayName("User exists")
		fun should_return_user_successfully() {
			val input = GetUserByIdInput(userId = 1L)

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

			whenever(userService.findOneByIdOrThrow(input.userId)) doReturn user

			val result = getUserByIdUseCase.execute(input)

			assertNotNull(result)
			assertEquals(1L, result.id)
			assertEquals("test@example.com", result.email)
			assertEquals("Test User", result.name)
			assertEquals(UserRole.USER, result.role)

			verify(userService, times(1)).findOneByIdOrThrow(input.userId)
		}

		@Test
		@DisplayName("User does not exist")
		fun should_throw_user_not_found_exception() {
			val input = GetUserByIdInput(userId = 999L)

			whenever(userService.findOneByIdOrThrow(input.userId))
				.thenThrow(UserNotFoundException(input.userId))

			val exception =
				assertThrows<UserNotFoundException> {
					getUserByIdUseCase.execute(input)
				}

			assertEquals("User Not Found userId = 999", exception.message)

			verify(userService, times(1)).findOneByIdOrThrow(input.userId)
		}

		@Test
		@DisplayName("Get deleted user")
		fun should_return_deleted_user_data() {
			val input = GetUserByIdInput(userId = 2L)

			val deletedUser =
				User(
					id = 2L,
					email = "deleted@example.com",
					password = "encoded_password",
					name = "Deleted User",
					role = UserRole.USER,
					createdDt = LocalDateTime.now().minusDays(30),
					updatedDt = LocalDateTime.now().minusDays(1),
					deletedDt = LocalDateTime.now()
				)

			whenever(userService.findOneByIdOrThrow(input.userId)) doReturn deletedUser

			val result = getUserByIdUseCase.execute(input)

			assertNotNull(result)
			assertEquals(2L, result.id)
			assertEquals("deleted@example.com", result.email)
		}
	}
}
