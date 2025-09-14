package com.example.demo.mockito.user.service

import com.example.demo.post.port.input.HardDeletePostsByUserIdInput
import com.example.demo.post.usecase.HardDeletePostsByUserIdUseCase
import com.example.demo.user.constant.UserRole
import com.example.demo.user.model.UserDeleteItem
import com.example.demo.user.port.input.HardDeleteUserByIdInput
import com.example.demo.user.service.UserDeleteEventService
import com.example.demo.user.usecase.HardDeleteUserByIdUseCase
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
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime

@ActiveProfiles("test")
@Tag("mockito-unit-test")
@DisplayName("Mockito Unit - UserDeleteEventService Test")
@ExtendWith(MockitoExtension::class)
class UserDeleteEventServiceTests {
	@Mock
	private lateinit var hardDeletePostsByUserIdUseCase: HardDeletePostsByUserIdUseCase

	@Mock
	private lateinit var hardDeleteUserByIdUseCase: HardDeleteUserByIdUseCase

	@InjectMocks
	private lateinit var userDeleteEventService: UserDeleteEventService

	@Nested
	@DisplayName("handle method tests")
	inner class HandleTests {
		@Test
		@DisplayName("should successfully delete posts and user")
		fun shouldSuccessfullyDeletePostsAndUser() {
			val userDeleteItem =
				UserDeleteItem(
					id = 100L,
					email = "test@example.com",
					name = "Test User",
					role = UserRole.USER.name,
					deletedDt = LocalDateTime.now().minusYears(1)
				)

			doNothing()
				.whenever(hardDeletePostsByUserIdUseCase)
				.execute(any<HardDeletePostsByUserIdInput>())
			doNothing()
				.whenever(hardDeleteUserByIdUseCase)
				.execute(any<HardDeleteUserByIdInput>())

			userDeleteEventService.handle(userDeleteItem)

			verify(hardDeletePostsByUserIdUseCase, times(1))
				.execute(HardDeletePostsByUserIdInput(userId = 100L))
			verify(hardDeleteUserByIdUseCase, times(1))
				.execute(HardDeleteUserByIdInput(userId = 100L))

			val inOrder = inOrder(hardDeletePostsByUserIdUseCase, hardDeleteUserByIdUseCase)
			inOrder.verify(hardDeletePostsByUserIdUseCase).execute(any<HardDeletePostsByUserIdInput>())
			inOrder.verify(hardDeleteUserByIdUseCase).execute(any<HardDeleteUserByIdInput>())
		}

		@Test
		@DisplayName("should handle admin user deletion")
		fun shouldHandleAdminUserDeletion() {
			val adminDeleteItem =
				UserDeleteItem(
					id = 200L,
					email = "admin@example.com",
					name = "Admin User",
					role = UserRole.ADMIN.name,
					deletedDt = LocalDateTime.now().minusMonths(18)
				)

			doNothing()
				.whenever(hardDeletePostsByUserIdUseCase)
				.execute(any<HardDeletePostsByUserIdInput>())
			doNothing()
				.whenever(hardDeleteUserByIdUseCase)
				.execute(any<HardDeleteUserByIdInput>())

			userDeleteEventService.handle(adminDeleteItem)

			verify(hardDeletePostsByUserIdUseCase, times(1))
				.execute(HardDeletePostsByUserIdInput(userId = 200L))
			verify(hardDeleteUserByIdUseCase, times(1))
				.execute(HardDeleteUserByIdInput(userId = 200L))
		}

		@Test
		@DisplayName("should propagate exception when post deletion fails")
		fun shouldPropagateExceptionWhenPostDeletionFails() {
			val userDeleteItem =
				UserDeleteItem(
					id = 300L,
					email = "error@example.com",
					name = "Error User",
					role = UserRole.USER.name,
					deletedDt = LocalDateTime.now().minusYears(1)
				)

			val exception = RuntimeException("Database connection failed")
			doThrow(exception)
				.whenever(hardDeletePostsByUserIdUseCase)
				.execute(any<HardDeletePostsByUserIdInput>())

			val thrownException =
				assertThrows<RuntimeException> {
					userDeleteEventService.handle(userDeleteItem)
				}

			assertEquals("Database connection failed", thrownException.message)

			verify(hardDeletePostsByUserIdUseCase, times(1))
				.execute(HardDeletePostsByUserIdInput(userId = 300L))
			verify(hardDeleteUserByIdUseCase, never()).execute(any<HardDeleteUserByIdInput>())
		}

		@Test
		@DisplayName("should propagate exception when user deletion fails")
		fun shouldPropagateExceptionWhenUserDeletionFails() {
			val userDeleteItem =
				UserDeleteItem(
					id = 400L,
					email = "fail@example.com",
					name = "Fail User",
					role = UserRole.USER.name,
					deletedDt = LocalDateTime.now().minusYears(1)
				)

			doNothing()
				.whenever(hardDeletePostsByUserIdUseCase)
				.execute(any<HardDeletePostsByUserIdInput>())

			val exception = RuntimeException("User deletion failed")
			doThrow(exception)
				.whenever(hardDeleteUserByIdUseCase)
				.execute(any<HardDeleteUserByIdInput>())

			val thrownException =
				assertThrows<RuntimeException> {
					userDeleteEventService.handle(userDeleteItem)
				}

			assertEquals("User deletion failed", thrownException.message)

			verify(hardDeletePostsByUserIdUseCase, times(1))
				.execute(HardDeletePostsByUserIdInput(userId = 400L))
			verify(hardDeleteUserByIdUseCase, times(1))
				.execute(HardDeleteUserByIdInput(userId = 400L))
		}
	}

	@Nested
	@DisplayName("batch processing tests")
	inner class BatchProcessingTests {
		@Test
		@DisplayName("should process multiple users sequentially")
		fun shouldProcessMultipleUsersSequentially() {
			val users =
				listOf(
					UserDeleteItem(
						1L,
						"user1@example.com",
						"User 1",
						UserRole.USER.name,
						LocalDateTime.now().minusYears(1)
					),
					UserDeleteItem(
						2L,
						"user2@example.com",
						"User 2",
						UserRole.ADMIN.name,
						LocalDateTime.now().minusYears(2)
					),
					UserDeleteItem(
						3L,
						"user3@example.com",
						"User 3",
						UserRole.USER.name,
						LocalDateTime.now().minusMonths(15)
					)
				)

			doNothing()
				.whenever(hardDeletePostsByUserIdUseCase)
				.execute(any<HardDeletePostsByUserIdInput>())
			doNothing()
				.whenever(hardDeleteUserByIdUseCase)
				.execute(any<HardDeleteUserByIdInput>())

			users.forEach { userDeleteEventService.handle(it) }

			users.forEach { user ->
				verify(hardDeletePostsByUserIdUseCase, times(1))
					.execute(HardDeletePostsByUserIdInput(userId = user.id))
				verify(hardDeleteUserByIdUseCase, times(1))
					.execute(HardDeleteUserByIdInput(userId = user.id))
			}

			verify(hardDeletePostsByUserIdUseCase, times(3)).execute(any<HardDeletePostsByUserIdInput>())
			verify(hardDeleteUserByIdUseCase, times(3)).execute(any<HardDeleteUserByIdInput>())
		}

		@Test
		@DisplayName("should continue processing after partial failure")
		fun shouldContinueProcessingAfterPartialFailure() {
			val users =
				listOf(
					UserDeleteItem(
						1L,
						"user1@example.com",
						"User 1",
						UserRole.USER.name,
						LocalDateTime.now().minusYears(1)
					),
					UserDeleteItem(
						2L,
						"user2@example.com",
						"User 2",
						UserRole.USER.name,
						LocalDateTime.now().minusYears(1)
					)
				)

			doNothing()
				.whenever(hardDeletePostsByUserIdUseCase)
				.execute(HardDeletePostsByUserIdInput(userId = 1L))
			doNothing()
				.whenever(hardDeleteUserByIdUseCase)
				.execute(HardDeleteUserByIdInput(userId = 1L))

			doThrow(RuntimeException("Failed"))
				.whenever(hardDeletePostsByUserIdUseCase)
				.execute(HardDeletePostsByUserIdInput(userId = 2L))

			userDeleteEventService.handle(users[0])

			assertThrows<RuntimeException> {
				userDeleteEventService.handle(users[1])
			}

			verify(hardDeletePostsByUserIdUseCase, times(1))
				.execute(HardDeletePostsByUserIdInput(userId = 1L))
			verify(hardDeleteUserByIdUseCase, times(1))
				.execute(HardDeleteUserByIdInput(userId = 1L))

			verify(hardDeletePostsByUserIdUseCase, times(1))
				.execute(HardDeletePostsByUserIdInput(userId = 2L))
			verify(hardDeleteUserByIdUseCase, never())
				.execute(HardDeleteUserByIdInput(userId = 2L))
		}
	}
}
