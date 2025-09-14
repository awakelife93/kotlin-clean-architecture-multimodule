package com.example.demo.mockito.post.usecase

import com.example.demo.post.exception.PostNotFoundException
import com.example.demo.post.model.Post
import com.example.demo.post.port.input.UpdatePostInput
import com.example.demo.post.port.output.PostOutput
import com.example.demo.post.service.PostService
import com.example.demo.post.usecase.UpdatePostUseCase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
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
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime

@ActiveProfiles("test")
@Tag("mockito-unit-test")
@DisplayName("Mockito Unit - Update Post UseCase Test")
@ExtendWith(MockitoExtension::class)
class UpdatePostUseCaseTests {
	@Mock
	private lateinit var postService: PostService

	@InjectMocks
	private lateinit var updatePostUseCase: UpdatePostUseCase

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
	@DisplayName("Update post")
	inner class UpdatePostTest {
		@Test
		@DisplayName("Update existing post")
		fun should_return_updated_post_output() {
			val input =
				UpdatePostInput(
					postId = post.id,
					userId = post.userId,
					title = "Updated Title",
					subTitle = "Updated SubTitle",
					content = "Updated Content"
				)

			val updatedPost =
				post.copy().apply {
					update(
						title = input.title,
						subTitle = input.subTitle,
						content = input.content
					)
				}

			whenever(postService.updatePostInfo(input)) doReturn updatedPost

			val result = updatePostUseCase.execute(input)

			assertNotNull(result)
			assertEquals(PostOutput.BasePostOutput.from(updatedPost), result)
			assertEquals(post.id, result.id)
			assertEquals(post.userId, result.userId)
			assertEquals("Updated Title", result.title)
			assertEquals("Updated SubTitle", result.subTitle)
			assertEquals("Updated Content", result.content)

			verify(postService, times(1)).updatePostInfo(input)
		}

		@Test
		@DisplayName("Try to update non-existent post")
		fun should_throw_not_found_exception() {
			val notExistId = 999L
			val input =
				UpdatePostInput(
					postId = notExistId,
					userId = 100L,
					title = "Updated Title",
					subTitle = "Updated SubTitle",
					content = "Updated Content"
				)

			whenever(postService.updatePostInfo(input))
				.thenThrow(PostNotFoundException(notExistId))

			val exception =
				assertThrows<PostNotFoundException> {
					updatePostUseCase.execute(input)
				}

			assertEquals("Post not found. postId: $notExistId", exception.message)
			verify(postService, times(1)).updatePostInfo(input)
		}

		@Test
		@DisplayName("Update only title")
		fun should_update_only_title() {
			val input =
				UpdatePostInput(
					postId = post.id,
					userId = post.userId,
					title = "Only Title Updated",
					subTitle = post.subTitle,
					content = post.content
				)

			val updatedPost =
				post.copy().apply {
					update(
						title = input.title,
						subTitle = input.subTitle,
						content = input.content
					)
				}

			whenever(postService.updatePostInfo(input)) doReturn updatedPost

			val result = updatePostUseCase.execute(input)

			assertNotNull(result)
			assertEquals("Only Title Updated", result.title)
			assertEquals(post.subTitle, result.subTitle)
			assertEquals(post.content, result.content)
		}
	}
}
