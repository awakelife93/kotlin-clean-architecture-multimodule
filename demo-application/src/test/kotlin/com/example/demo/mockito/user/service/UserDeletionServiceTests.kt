package com.example.demo.mockito.user.service

import com.example.demo.persistence.auth.provider.TokenProvider
import com.example.demo.post.service.PostService
import com.example.demo.user.exception.UserNotFoundException
import com.example.demo.user.service.UserDeletionService
import com.example.demo.user.service.UserService
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
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
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@Tag("mockito-unit-test")
@DisplayName("Mockito Unit - User Deletion Service Test")
@ExtendWith(MockitoExtension::class)
class UserDeletionServiceTests {
	@Mock
	private lateinit var userService: UserService

	@Mock
	private lateinit var postService: PostService

	@Mock
	private lateinit var tokenProvider: TokenProvider

	@InjectMocks
	private lateinit var userDeletionService: UserDeletionService

	@Nested
	@DisplayName("Delete user with related data")
	inner class DeleteUserWithRelatedDataTest {
		@Test
		@DisplayName("Should soft delete user and all related data")
		fun should_delete_user_and_related_data_successfully() {
			val userId = 1L

			whenever(userService.existsById(userId)) doReturn true
			doNothing().whenever(postService).deletePostsByUserId(userId)
			doNothing().whenever(tokenProvider).deleteRefreshToken(userId)
			doNothing().whenever(userService).deleteByIdWithoutValidation(userId)

			assertDoesNotThrow {
				userDeletionService.deleteUserWithRelatedData(userId)
			}

			verify(userService, times(1)).existsById(userId)
			verify(postService, times(1)).deletePostsByUserId(userId)
			verify(tokenProvider, times(1)).deleteRefreshToken(userId)
			verify(userService, times(1)).deleteByIdWithoutValidation(userId)
		}

		@Test
		@DisplayName("Should throw UserNotFoundException when user not found")
		fun should_throw_exception_when_user_not_found() {
			val userId = 999L
			whenever(userService.existsById(userId)) doReturn false

			val exception =
				assertThrows<UserNotFoundException> {
					userDeletionService.deleteUserWithRelatedData(userId)
				}

			assertEquals("User Not Found userId = $userId", exception.message)

			verify(userService, times(1)).existsById(userId)
			verify(postService, never()).deletePostsByUserId(userId)
			verify(tokenProvider, never()).deleteRefreshToken(userId)
			verify(userService, never()).deleteByIdWithoutValidation(userId)
		}

		@Test
		@DisplayName("Should handle user with no posts")
		fun should_handle_user_with_no_posts() {
			val userId = 2L

			whenever(userService.existsById(userId)) doReturn true
			doNothing().whenever(postService).deletePostsByUserId(userId)
			doNothing().whenever(tokenProvider).deleteRefreshToken(userId)
			doNothing().whenever(userService).deleteByIdWithoutValidation(userId)

			assertDoesNotThrow {
				userDeletionService.deleteUserWithRelatedData(userId)
			}

			verify(userService, times(1)).existsById(userId)
			verify(postService, times(1)).deletePostsByUserId(userId)
			verify(tokenProvider, times(1)).deleteRefreshToken(userId)
			verify(userService, times(1)).deleteByIdWithoutValidation(userId)
		}
	}

	@Nested
	@DisplayName("Can delete user")
	inner class CanDeleteUserTest {
		@Test
		@DisplayName("Should return true when user exists")
		fun should_return_true_when_user_exists() {
			val userId = 1L
			whenever(userService.existsById(userId)) doReturn true

			val result = userDeletionService.canDeleteUser(userId)

			assertTrue(result)
			verify(userService, times(1)).existsById(userId)
		}

		@Test
		@DisplayName("Should return false when user does not exist")
		fun should_return_false_when_user_not_exists() {
			val userId = 999L
			whenever(userService.existsById(userId)) doReturn false

			val result = userDeletionService.canDeleteUser(userId)

			assertFalse(result)
			verify(userService, times(1)).existsById(userId)
		}
	}

	@Nested
	@DisplayName("Get user deletion summary")
	inner class GetUserDeletionSummaryTest {
		@Test
		@DisplayName("Should return correct deletion summary")
		fun should_return_correct_deletion_summary() {
			val userId = 1L
			val postCount = 5L
			whenever(postService.countByUserId(userId)) doReturn postCount

			val result = userDeletionService.getUserDeletionSummary(userId)

			assertNotNull(result)
			assertEquals(userId, result.userId)
			assertEquals(postCount, result.postCount)
			verify(postService, times(1)).countByUserId(userId)
		}

		@Test
		@DisplayName("Should return zero post count when user has no posts")
		fun should_return_zero_post_count_when_no_posts() {
			val userId = 2L
			whenever(postService.countByUserId(userId)) doReturn 0L

			val result = userDeletionService.getUserDeletionSummary(userId)

			assertNotNull(result)
			assertEquals(userId, result.userId)
			assertEquals(0L, result.postCount)
			verify(postService, times(1)).countByUserId(userId)
		}
	}
}
