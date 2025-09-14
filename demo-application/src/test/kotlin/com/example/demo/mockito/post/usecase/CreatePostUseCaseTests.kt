package com.example.demo.mockito.post.usecase

import com.example.demo.post.model.Post
import com.example.demo.post.port.input.CreatePostInput
import com.example.demo.post.port.output.PostOutput
import com.example.demo.post.service.PostService
import com.example.demo.post.usecase.CreatePostUseCase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime

@ActiveProfiles("test")
@Tag("mockito-unit-test")
@DisplayName("Mockito Unit - Create Post UseCase Test")
@ExtendWith(MockitoExtension::class)
class CreatePostUseCaseTests {
	@Mock
	private lateinit var postService: PostService

	@InjectMocks
	private lateinit var createPostUseCase: CreatePostUseCase

	@Nested
	@DisplayName("Create post")
	inner class CreatePostTest {
		@Test
		@DisplayName("Create post with valid input")
		fun should_return_post_output_when_created() {
			val input =
				CreatePostInput(
					userId = 100L,
					title = "New Post",
					subTitle = "New SubTitle",
					content = "New Content"
				)

			val savedPost =
				Post(
					id = 1L,
					userId = input.userId,
					title = input.title,
					subTitle = input.subTitle,
					content = input.content,
					createdDt = LocalDateTime.now(),
					updatedDt = LocalDateTime.now()
				)

			val inputCaptor = argumentCaptor<CreatePostInput>()
			whenever(postService.createPost(inputCaptor.capture())) doReturn savedPost

			val result = createPostUseCase.execute(input)

			assertNotNull(result)
			assertEquals(PostOutput.BasePostOutput.from(savedPost), result)
			assertEquals(1L, result.id)
			assertEquals(input.userId, result.userId)
			assertEquals("New Post", result.title)
			assertEquals("New SubTitle", result.subTitle)
			assertEquals("New Content", result.content)

			verify(postService, times(1)).createPost(inputCaptor.firstValue)

			val capturedInput = inputCaptor.firstValue
			assertEquals(input.userId, capturedInput.userId)
			assertEquals(input.title, capturedInput.title)
			assertEquals(input.subTitle, capturedInput.subTitle)
			assertEquals(input.content, capturedInput.content)
		}

		@Test
		@DisplayName("Create post with empty subtitle")
		fun should_create_post_with_empty_subtitle() {
			val input =
				CreatePostInput(
					userId = 100L,
					title = "Title Only",
					subTitle = "",
					content = "Content"
				)

			val savedPost =
				Post(
					id = 2L,
					userId = input.userId,
					title = input.title,
					subTitle = input.subTitle,
					content = input.content,
					createdDt = LocalDateTime.now(),
					updatedDt = LocalDateTime.now()
				)

			val inputCaptor = argumentCaptor<CreatePostInput>()
			whenever(postService.createPost(inputCaptor.capture())) doReturn savedPost

			val result = createPostUseCase.execute(input)

			assertNotNull(result)
			assertEquals(2L, result.id)
			assertEquals("Title Only", result.title)
			assertEquals("", result.subTitle)
			assertEquals("Content", result.content)

			val capturedInput = inputCaptor.firstValue
			assertEquals(input.userId, capturedInput.userId)
			assertEquals(input.title, capturedInput.title)
			assertEquals(input.subTitle, capturedInput.subTitle)
			assertEquals(input.content, capturedInput.content)
		}

		@Test
		@DisplayName("Create post with minimal content")
		fun should_create_post_with_minimal_content() {
			val input =
				CreatePostInput(
					userId = 100L,
					title = "T",
					subTitle = "S",
					content = "C"
				)

			val savedPost =
				Post(
					id = 3L,
					userId = input.userId,
					title = input.title,
					subTitle = input.subTitle,
					content = input.content,
					createdDt = LocalDateTime.now(),
					updatedDt = LocalDateTime.now()
				)

			val inputCaptor = argumentCaptor<CreatePostInput>()
			whenever(postService.createPost(inputCaptor.capture())) doReturn savedPost

			val result = createPostUseCase.execute(input)

			assertNotNull(result)
			assertEquals(3L, result.id)
			assertEquals("T", result.title)
			assertEquals("S", result.subTitle)
			assertEquals("C", result.content)

			val capturedInput = inputCaptor.firstValue
			assertEquals(input.userId, capturedInput.userId)
			assertEquals(input.title, capturedInput.title)
			assertEquals(input.subTitle, capturedInput.subTitle)
			assertEquals(input.content, capturedInput.content)
		}
	}
}
