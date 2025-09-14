package com.example.demo.mockito.user.usecase

import com.example.demo.persistence.auth.provider.TokenProvider
import com.example.demo.user.constant.UserRole
import com.example.demo.user.exception.UserNotFoundException
import com.example.demo.user.model.User
import com.example.demo.user.port.input.UpdateMeInput
import com.example.demo.user.service.UserService
import com.example.demo.user.usecase.UpdateMeUseCase
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
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime

@ActiveProfiles("test")
@Tag("mockito-unit-test")
@DisplayName("Mockito Unit - Update Me UseCase Test")
@ExtendWith(MockitoExtension::class)
class UpdateMeUseCaseTests {
	@Mock
	private lateinit var userService: UserService

	@Mock
	private lateinit var tokenProvider: TokenProvider

	@InjectMocks
	private lateinit var updateMeUseCase: UpdateMeUseCase

	@Nested
	@DisplayName("Update me (current user)")
	inner class UpdateMeTest {
		@Test
		@DisplayName("Update current user's information")
		fun should_update_and_return_new_token() {
			val input =
				UpdateMeInput(
					userId = 1L,
					name = "Updated Me"
				)

			val updatedUser =
				User(
					id = 1L,
					email = "updated@example.com",
					password = "encoded_password",
					name = "Updated Me",
					role = UserRole.USER,
					createdDt = LocalDateTime.now().minusDays(30),
					updatedDt = LocalDateTime.now()
				)

			val newAccessToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.updated.signature"

			whenever(userService.updateUserInfo(input.userId, input.name)) doReturn updatedUser
			whenever(tokenProvider.createFullTokens(updatedUser)) doReturn newAccessToken

			val result = updateMeUseCase.execute(input)

			assertNotNull(result)
			assertEquals(1L, result.id)
			assertEquals("Updated Me", result.name)
			assertEquals("updated@example.com", result.email)
			assertEquals(newAccessToken, result.accessToken)

			verify(userService, times(1)).updateUserInfo(input.userId, input.name)
			verify(tokenProvider, times(1)).createFullTokens(updatedUser)
		}

		@Test
		@DisplayName("Update non-existent user")
		fun should_throw_user_not_found_exception() {
			val input =
				UpdateMeInput(
					userId = 999L,
					name = "Ghost User"
				)

			whenever(userService.updateUserInfo(input.userId, input.name))
				.thenThrow(UserNotFoundException(input.userId))

			val exception =
				assertThrows<UserNotFoundException> {
					updateMeUseCase.execute(input)
				}

			assertEquals("User Not Found userId = 999", exception.message)

			verify(userService, times(1)).updateUserInfo(input.userId, input.name)
			verify(tokenProvider, never()).createFullTokens(org.mockito.kotlin.any<User>())
		}

		@Test
		@DisplayName("Update only name without changing email")
		fun should_update_name_and_keep_existing_email() {
			val input =
				UpdateMeInput(
					userId = 2L,
					name = "Name Only Update"
				)

			val updatedUser =
				User(
					id = 2L,
					email = "existing@example.com",
					password = "existing_encoded_password",
					name = "Name Only Update",
					role = UserRole.USER,
					createdDt = LocalDateTime.now().minusDays(10),
					updatedDt = LocalDateTime.now()
				)

			val newAccessToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.nameonly.signature"

			whenever(userService.updateUserInfo(input.userId, input.name)) doReturn updatedUser
			whenever(tokenProvider.createFullTokens(updatedUser)) doReturn newAccessToken

			val result = updateMeUseCase.execute(input)

			assertNotNull(result)
			assertEquals("Name Only Update", result.name)
			assertEquals("existing@example.com", result.email)
			assertEquals(newAccessToken, result.accessToken)

			verify(userService, times(1)).updateUserInfo(input.userId, input.name)
			verify(tokenProvider, times(1)).createFullTokens(updatedUser)
		}
	}
}
