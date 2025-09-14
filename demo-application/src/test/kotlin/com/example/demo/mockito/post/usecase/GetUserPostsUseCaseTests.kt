package com.example.demo.mockito.post.usecase

import com.example.demo.post.model.Post
import com.example.demo.post.port.input.GetUserPostsInput
import com.example.demo.post.service.PostService
import com.example.demo.post.usecase.GetUserPostsUseCase
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
import org.springframework.data.domain.Pageable
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime

@ActiveProfiles("test")
@Tag("mockito-unit-test")
@DisplayName("Mockito Unit - Get User Posts UseCase Test")
@ExtendWith(MockitoExtension::class)
class GetUserPostsUseCaseTests {
	@Mock
	private lateinit var postService: PostService

	@InjectMocks
	private lateinit var getUserPostsUseCase: GetUserPostsUseCase

	private lateinit var testPosts: List<Post>
	private lateinit var defaultPageable: Pageable

	@BeforeEach
	fun setUp() {
		defaultPageable = PageRequest.of(0, 10)
		testPosts =
			(1..3).map { id ->
				Post(
					id = id.toLong(),
					userId = 100L,
					title = "User Post $id",
					subTitle = "SubTitle $id",
					content = "Content $id",
					createdDt = LocalDateTime.now(),
					updatedDt = LocalDateTime.now()
				)
			}
	}

	@Nested
	@DisplayName("Get user posts")
	inner class GetUserPostsTest {
		@Test
		@DisplayName("Get posts for user with multiple posts")
		fun should_return_user_posts() {
			val userId = 100L
			val input =
				GetUserPostsInput(
					userId = userId,
					pageable = defaultPageable
				)
			val page = PageImpl(testPosts, defaultPageable, testPosts.size.toLong())
			whenever(postService.findAllByUserId(userId, defaultPageable)) doReturn page

			val result = getUserPostsUseCase.execute(input)

			assertNotNull(result)
			assertEquals(3, result.posts.content.size)
			result.posts.content.forEachIndexed { index, post ->
				assertEquals((index + 1).toLong(), post.id)
				assertEquals("User Post ${index + 1}", post.title)
				assertEquals(userId, post.userId)
			}
		}

		@Test
		@DisplayName("Get posts for user with no posts")
		fun should_return_empty_list() {
			val userId = 200L
			val input =
				GetUserPostsInput(
					userId = userId,
					pageable = defaultPageable
				)
			val emptyPage = PageImpl(emptyList<Post>(), defaultPageable, 0)
			whenever(postService.findAllByUserId(userId, defaultPageable)) doReturn emptyPage

			val result = getUserPostsUseCase.execute(input)

			assertNotNull(result)
			assertEquals(0, result.posts.content.size)
			assertTrue(result.posts.content.isEmpty())
		}

		@Test
		@DisplayName("Get posts for user with single post")
		fun should_return_single_post() {
			val userId = 100L
			val singlePost = listOf(testPosts[0])
			val input =
				GetUserPostsInput(
					userId = userId,
					pageable = defaultPageable
				)
			val page = PageImpl(singlePost, defaultPageable, 1)
			whenever(postService.findAllByUserId(userId, defaultPageable)) doReturn page

			val result = getUserPostsUseCase.execute(input)

			assertNotNull(result)
			assertEquals(1, result.posts.content.size)
			assertEquals(1L, result.posts.content[0].id)
			assertEquals("User Post 1", result.posts.content[0].title)
		}

		@Test
		@DisplayName("Get posts with different pagination")
		fun should_return_posts_with_pagination() {
			val userId = 100L
			val pageable = PageRequest.of(1, 5)
			val input =
				GetUserPostsInput(
					userId = userId,
					pageable = pageable
				)
			val page = PageImpl(testPosts, pageable, testPosts.size.toLong())
			whenever(postService.findAllByUserId(userId, pageable)) doReturn page

			val result = getUserPostsUseCase.execute(input)

			assertNotNull(result)
			assertEquals(3, result.posts.content.size)
		}
	}
}
