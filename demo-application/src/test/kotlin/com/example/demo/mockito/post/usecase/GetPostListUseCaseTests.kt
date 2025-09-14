package com.example.demo.mockito.post.usecase

import com.example.demo.post.model.Post
import com.example.demo.post.port.input.GetPostListInput
import com.example.demo.post.service.PostService
import com.example.demo.post.usecase.GetPostListUseCase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime

@ActiveProfiles("test")
@Tag("mockito-unit-test")
@DisplayName("Mockito Unit - Get Post List UseCase Test")
@ExtendWith(MockitoExtension::class)
class GetPostListUseCaseTests {
	@Mock
	private lateinit var postService: PostService

	@InjectMocks
	private lateinit var getPostListUseCase: GetPostListUseCase

	private lateinit var testPosts: List<Post>

	@BeforeEach
	fun setUp() {
		testPosts =
			(1..3).map { id ->
				Post(
					id = id.toLong(),
					userId = 100L,
					title = "Test Title $id",
					subTitle = "Test SubTitle $id",
					content = "Test Content $id",
					createdDt = LocalDateTime.now(),
					updatedDt = LocalDateTime.now()
				)
			}
	}

	@Nested
	@DisplayName("Get post list")
	inner class GetPostListTest {
		@Test
		@DisplayName("Get paginated post list")
		fun should_return_post_list_output() {
			val pageable = PageRequest.of(0, 10)
			val input = GetPostListInput(pageable = pageable)
			val postPage = PageImpl(testPosts, pageable, testPosts.size.toLong())

			whenever(postService.findAll(pageable)) doReturn postPage

			val result = getPostListUseCase.execute(input)

			assertNotNull(result)
			assertEquals(3, result.posts.content.size)
			result.posts.content.forEachIndexed { index, post ->
				assertEquals((index + 1).toLong(), post.id)
				assertEquals("Test Title ${index + 1}", post.title)
				assertEquals("Test SubTitle ${index + 1}", post.subTitle)
			}
		}

		@Test
		@DisplayName("Get empty post list")
		fun should_return_empty_list() {
			val pageable = PageRequest.of(0, 10)
			val input = GetPostListInput(pageable = pageable)
			val emptyPage = PageImpl<Post>(emptyList(), pageable, 0)

			whenever(postService.findAll(pageable)) doReturn emptyPage

			val result = getPostListUseCase.execute(input)

			assertNotNull(result)
			assertEquals(0, result.posts.content.size)
			assertTrue(result.posts.content.isEmpty())
		}

		@Test
		@DisplayName("Get specific page")
		fun should_return_requested_page() {
			val pageable = PageRequest.of(1, 2)
			val input = GetPostListInput(pageable = pageable)
			val secondPagePosts = testPosts.subList(2, 3)
			val postPage = PageImpl(secondPagePosts, pageable, testPosts.size.toLong())

			whenever(postService.findAll(pageable)) doReturn postPage

			val result = getPostListUseCase.execute(input)

			assertNotNull(result)
			assertEquals(1, result.posts.content.size)
			assertEquals(
				"Test Title 3",
				result.posts.content
					.first()
					.title
			)
		}
	}
}
