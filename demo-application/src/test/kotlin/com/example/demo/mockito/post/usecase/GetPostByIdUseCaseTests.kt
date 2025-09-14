package com.example.demo.mockito.post.usecase

import com.example.demo.post.exception.PostNotFoundException
import com.example.demo.post.model.Post
import com.example.demo.post.port.input.GetPostByIdInput
import com.example.demo.post.port.output.PostOutput
import com.example.demo.post.service.PostService
import com.example.demo.post.usecase.GetPostByIdUseCase
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
import org.mockito.kotlin.whenever
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime

@ActiveProfiles("test")
@Tag("mockito-unit-test")
@DisplayName("Mockito Unit - Get Post By ID UseCase Test")
@ExtendWith(MockitoExtension::class)
class GetPostByIdUseCaseTests {
	@Mock
	private lateinit var postService: PostService

	@InjectMocks
	private lateinit var getPostByIdUseCase: GetPostByIdUseCase

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
	@DisplayName("Get post by ID")
	inner class GetPostByIdTest {
		@Test
		@DisplayName("Get existing post")
		fun should_return_post_output() {
			val input = GetPostByIdInput(postId = post.id)
			whenever(postService.findOneByIdOrThrow(post.id)) doReturn post

			val result = getPostByIdUseCase.execute(input)

			assertNotNull(result)
			assertEquals(PostOutput.BasePostOutput.from(post), result)
			assertEquals(post.id, result.id)
			assertEquals(post.userId, result.userId)
			assertEquals(post.title, result.title)
			assertEquals(post.subTitle, result.subTitle)
			assertEquals(post.content, result.content)
		}

		@Test
		@DisplayName("Get non-existent post")
		fun should_throw_not_found_exception() {
			val notExistId = 999L
			val input = GetPostByIdInput(postId = notExistId)
			whenever(postService.findOneByIdOrThrow(notExistId))
				.thenThrow(PostNotFoundException(notExistId))

			val exception =
				assertThrows<PostNotFoundException> {
					getPostByIdUseCase.execute(input)
				}

			assertEquals("Post not found. postId: $notExistId", exception.message)
		}
	}
}
