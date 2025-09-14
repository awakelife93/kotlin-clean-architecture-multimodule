package com.example.demo.mockito.post.usecase

import com.example.demo.post.exception.PostNotFoundException
import com.example.demo.post.model.Post
import com.example.demo.post.port.input.DeletePostInput
import com.example.demo.post.service.PostService
import com.example.demo.post.usecase.DeletePostUseCase
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
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
import java.time.LocalDateTime

@ActiveProfiles("test")
@Tag("mockito-unit-test")
@DisplayName("Mockito Unit - Delete Post UseCase Test")
@ExtendWith(MockitoExtension::class)
class DeletePostUseCaseTests {
	@Mock
	private lateinit var postService: PostService

	@InjectMocks
	private lateinit var deletePostUseCase: DeletePostUseCase

	private lateinit var post: Post

	@BeforeEach
	fun setUp() {
		post =
			Post(
				id = 1L,
				userId = 100L,
				title = "Test Title",
				subTitle = "Test SubTitle",
				content = "Test Content",
				createdDt = LocalDateTime.now(),
				updatedDt = LocalDateTime.now()
			)
	}

	@Nested
	@DisplayName("Delete post")
	inner class DeletePostTest {
		@Test
		@DisplayName("Post owner requests deletion")
		fun should_delete_post_successfully() {
			val input =
				DeletePostInput(
					postId = post.id,
					userId = post.userId
				)

			doNothing().whenever(postService).deletePostByUser(post.id, post.userId)

			assertDoesNotThrow {
				deletePostUseCase.execute(input)
			}

			verify(postService, times(1)).deletePostByUser(post.id, post.userId)
		}

		@Test
		@DisplayName("Another user tries to delete")
		fun should_throw_unauthorized_exception() {
			val otherUserId = 200L
			val input =
				DeletePostInput(
					postId = post.id,
					userId = otherUserId
				)

			whenever(postService.deletePostByUser(post.id, otherUserId))
				.thenThrow(IllegalArgumentException("Permission denied. You can only delete posts you have authored."))

			val exception =
				assertThrows<IllegalArgumentException> {
					deletePostUseCase.execute(input)
				}

			assertEquals("Permission denied. You can only delete posts you have authored.", exception.message)
			verify(postService, times(1)).deletePostByUser(post.id, otherUserId)
		}

		@Test
		@DisplayName("Try to delete non-existent post")
		fun should_throw_not_found_exception() {
			val notExistId = 999L
			val input =
				DeletePostInput(
					postId = notExistId,
					userId = 100L
				)

			whenever(postService.deletePostByUser(notExistId, 100L))
				.thenThrow(PostNotFoundException(notExistId))

			val exception =
				assertThrows<PostNotFoundException> {
					deletePostUseCase.execute(input)
				}

			assertEquals("Post not found. postId: $notExistId", exception.message)
			verify(postService, times(1)).deletePostByUser(notExistId, 100L)
		}
	}
}
