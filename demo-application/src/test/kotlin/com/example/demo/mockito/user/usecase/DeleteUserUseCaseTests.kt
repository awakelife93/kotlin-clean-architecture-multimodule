package com.example.demo.mockito.user.usecase

import com.example.demo.user.port.input.DeleteUserInput
import com.example.demo.user.service.UserDeletionService
import com.example.demo.user.usecase.DeleteUserUseCase
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
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
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@Tag("mockito-unit-test")
@DisplayName("Mockito Unit - Delete User UseCase Test")
@ExtendWith(MockitoExtension::class)
class DeleteUserUseCaseTests {
	@Mock
	private lateinit var userDeletionService: UserDeletionService

	@InjectMocks
	private lateinit var deleteUserUseCase: DeleteUserUseCase

	@Nested
	@DisplayName("Delete user (Soft Delete)")
	inner class DeleteUserTest {
		@Test
		@DisplayName("Soft delete existing user")
		fun should_soft_delete_user_successfully() {
			val input = DeleteUserInput(userId = 1L)

			doNothing().whenever(userDeletionService).deleteUserWithRelatedData(input.userId)

			assertDoesNotThrow {
				deleteUserUseCase.execute(input)
			}

			verify(userDeletionService, times(1)).deleteUserWithRelatedData(input.userId)
		}

		@Test
		@DisplayName("Delete non-existent user")
		fun should_throw_exception_when_user_not_found() {
			val input = DeleteUserInput(userId = 999L)

			whenever(userDeletionService.deleteUserWithRelatedData(input.userId))
				.thenThrow(IllegalArgumentException("User not found: 999"))

			val exception =
				assertThrows<IllegalArgumentException> {
					deleteUserUseCase.execute(input)
				}

			assertEquals("User not found: 999", exception.message)

			verify(userDeletionService, times(1)).deleteUserWithRelatedData(input.userId)
		}

		@Test
		@DisplayName("Delete already soft-deleted user")
		fun should_handle_gracefully() {
			val input = DeleteUserInput(userId = 2L)

			doNothing().whenever(userDeletionService).deleteUserWithRelatedData(input.userId)

			assertDoesNotThrow {
				deleteUserUseCase.execute(input)
			}

			verify(userDeletionService, times(1)).deleteUserWithRelatedData(input.userId)
		}
	}
}
