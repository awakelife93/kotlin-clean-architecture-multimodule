package com.example.demo.mockito.post.service

import com.example.demo.post.exception.PostNotFoundException
import com.example.demo.post.model.Post
import com.example.demo.post.port.PostPort
import com.example.demo.post.port.input.UpdatePostInput
import com.example.demo.post.service.PostService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
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
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime

@ActiveProfiles("test")
@Tag("mockito-unit-test")
@DisplayName("Mockito Unit - Post Service Test")
@ExtendWith(MockitoExtension::class)
class PostServiceTests {
	@Mock
	private lateinit var postPort: PostPort

	@InjectMocks
	private lateinit var postService: PostService

	private lateinit var post: Post
	private lateinit var defaultPageable: Pageable

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
				updatedDt = LocalDateTime.now(),
				deletedDt = null
			)
		defaultPageable = PageRequest.of(0, 10)
	}

	@Nested
	@DisplayName("Find post")
	inner class FindPostTest {
		@Test
		@DisplayName("Find post by ID successfully")
		fun should_return_post_when_found() {
			whenever(postPort.findOneById(post.id)) doReturn post

			val result = postService.findOneById(post.id)

			assertNotNull(result)
			assertEquals(post.id, result?.id)
			assertEquals(post.title, result?.title)
			assertEquals(post.subTitle, result?.subTitle)
			assertEquals(post.content, result?.content)
			assertEquals(post.userId, result?.userId)
		}

		@Test
		@DisplayName("Find non-existent post")
		fun should_return_null_when_not_found() {
			val notExistId = 999L
			whenever(postPort.findOneById(notExistId)) doReturn null

			val result = postService.findOneById(notExistId)

			assertNull(result)
		}

		@Test
		@DisplayName("Find post by ID or throw when exists")
		fun findOneByIdOrThrow_whenPostExists_returnsPost() {
			whenever(postPort.findOneById(post.id)) doReturn post

			val result = postService.findOneByIdOrThrow(post.id)

			assertNotNull(result)
			assertEquals(post.id, result.id)
			verify(postPort).findOneById(post.id)
		}

		@Test
		@DisplayName("Find post by ID or throw when not exists throws exception")
		fun findOneByIdOrThrow_whenPostNotExists_throwsException() {
			val notExistId = 999L
			whenever(postPort.findOneById(notExistId)) doReturn null

			val exception =
				assertThrows(PostNotFoundException::class.java) {
					postService.findOneByIdOrThrow(notExistId)
				}

			assertEquals("Post not found. postId: 999", exception.message)
			verify(postPort).findOneById(notExistId)
		}

		@Test
		@DisplayName("Find all posts with pagination")
		fun should_return_paginated_post_list() {
			val posts = listOf(post)
			whenever(postPort.findAll(defaultPageable)) doReturn PageImpl(posts, defaultPageable, 1)

			val result = postService.findAll(defaultPageable)

			assertEquals(1, result.content.size)
			assertEquals(post, result.content[0])
			assertEquals(1, result.totalElements)
		}

		@Test
		@DisplayName("Find posts by user ID")
		fun should_return_user_posts() {
			val userId = 100L
			val userPosts = listOf(post)
			whenever(postPort.findAllByUserId(userId)) doReturn userPosts

			val result = postService.findAllByUserId(userId)

			assertEquals(1, result.size)
			assertEquals(post, result[0])
		}
	}

	@Nested
	@DisplayName("Create post")
	inner class CreatePostTest {
		@Test
		@DisplayName("Create new post successfully")
		fun should_return_saved_post() {
			val input =
				com.example.demo.post.port.input.CreatePostInput(
					userId = 100L,
					title = "New Post",
					subTitle = "New SubTitle",
					content = "New Content"
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

			argumentCaptor<Post>().apply {
				whenever(postPort.save(capture())) doReturn savedPost
			}

			val result = postService.createPost(input)

			assertNotNull(result)
			assertEquals(2L, result.id)
			assertEquals("New Post", result.title)
			assertEquals("New SubTitle", result.subTitle)
			assertEquals("New Content", result.content)

			argumentCaptor<Post>().apply {
				verify(postPort).save(capture())
				assertEquals(input.userId, firstValue.userId)
				assertEquals(input.title, firstValue.title)
				assertEquals(input.subTitle, firstValue.subTitle)
				assertEquals(input.content, firstValue.content)
			}
		}
	}

	@Nested
	@DisplayName("Update post")
	inner class UpdatePostTest {
		@Test
		@DisplayName("Update post successfully")
		fun should_return_updated_post() {
			val updatedPost =
				post.copy(
					title = "Updated Title",
					subTitle = "Updated SubTitle",
					content = "Updated Content"
				)

			whenever(postPort.save(updatedPost)) doReturn updatedPost

			val result = postService.updatePost(updatedPost)

			assertNotNull(result)
			assertEquals(post.id, result.id)
			assertEquals("Updated Title", result.title)
			assertEquals("Updated SubTitle", result.subTitle)
			assertEquals("Updated Content", result.content)
		}

		@Test
		@DisplayName("Update post info successfully when user is owner")
		fun updatePostInfo_whenUserIsOwner_updatesAndReturnsPost() {
			val input =
				UpdatePostInput(
					postId = post.id,
					userId = post.userId,
					title = "Updated Title",
					subTitle = "Updated SubTitle",
					content = "Updated Content"
				)

			whenever(postPort.findOneById(post.id)) doReturn post
			whenever(postPort.save(any<Post>())) doReturn post

			val result = postService.updatePostInfo(input)

			assertNotNull(result)
			assertEquals(post.id, result.id)

			verify(postPort).findOneById(post.id)
			verify(postPort).save(any<Post>())
		}

		@Test
		@DisplayName("Update post info throws exception when post not found")
		fun updatePostInfo_whenPostNotFound_throwsException() {
			val input =
				UpdatePostInput(
					postId = 999L,
					userId = 100L,
					title = "Updated Title",
					subTitle = "Updated SubTitle",
					content = "Updated Content"
				)

			whenever(postPort.findOneById(999L)) doReturn null

			val exception =
				assertThrows(PostNotFoundException::class.java) {
					postService.updatePostInfo(input)
				}

			assertEquals("Post not found. postId: 999", exception.message)

			verify(postPort).findOneById(999L)
			verify(postPort, never()).save(any<Post>())
		}

		@Test
		@DisplayName("Update post info throws exception when user is not owner")
		fun updatePostInfo_whenUserIsNotOwner_throwsException() {
			val otherUserId = 200L
			val input =
				UpdatePostInput(
					postId = post.id,
					userId = otherUserId,
					title = "Updated Title",
					subTitle = "Updated SubTitle",
					content = "Updated Content"
				)

			whenever(postPort.findOneById(post.id)) doReturn post

			val exception =
				assertThrows(IllegalArgumentException::class.java) {
					postService.updatePostInfo(input)
				}

			assertEquals("Permission denied. You can only modify posts you have authored.", exception.message)

			verify(postPort).findOneById(post.id)
			verify(postPort, never()).save(any<Post>())
		}
	}

	@Nested
	@DisplayName("Delete post")
	inner class DeletePostTest {
		@Test
		@DisplayName("Soft delete post successfully")
		fun should_delete_post() {
			whenever(postPort.findOneById(post.id)) doReturn post
			doNothing().whenever(postPort).deleteById(post.id)

			postService.deletePost(post.id)

			verify(postPort, times(1)).findOneById(post.id)
			verify(postPort, times(1)).deleteById(post.id)
		}

		@Test
		@DisplayName("Try to delete non-existent post")
		fun should_do_nothing_when_post_not_found() {
			val notExistId = 999L
			whenever(postPort.findOneById(notExistId)) doReturn null

			postService.deletePost(notExistId)

			verify(postPort, times(1)).findOneById(notExistId)
			verify(postPort, never()).deleteById(notExistId)
		}

		@Test
		@DisplayName("Delete post by user when user is owner")
		fun deletePostByUser_whenUserIsOwner_deletesPost() {
			whenever(postPort.findOneById(post.id)) doReturn post
			doNothing().whenever(postPort).deleteById(post.id)

			postService.deletePostByUser(post.id, post.userId)

			verify(postPort).findOneById(post.id)
			verify(postPort).deleteById(post.id)
		}

		@Test
		@DisplayName("Delete post by user throws exception when post not found")
		fun deletePostByUser_whenPostNotFound_throwsException() {
			val notExistId = 999L
			whenever(postPort.findOneById(notExistId)) doReturn null

			val exception =
				assertThrows(PostNotFoundException::class.java) {
					postService.deletePostByUser(notExistId, 100L)
				}

			assertEquals("Post not found. postId: 999", exception.message)

			verify(postPort).findOneById(notExistId)
			verify(postPort, never()).deleteById(notExistId)
		}

		@Test
		@DisplayName("Delete post by user throws exception when user is not owner")
		fun deletePostByUser_whenUserIsNotOwner_throwsException() {
			val otherUserId = 200L
			whenever(postPort.findOneById(post.id)) doReturn post

			val exception =
				assertThrows(IllegalArgumentException::class.java) {
					postService.deletePostByUser(post.id, otherUserId)
				}

			assertEquals("Permission denied. You can only delete posts you have authored.", exception.message)

			verify(postPort).findOneById(post.id)
			verify(postPort, never()).deleteById(post.id)
		}

		@Test
		@DisplayName("Delete all posts by user ID")
		fun should_delete_all_user_posts() {
			val userId = 100L
			val post1 = post.copy(id = 1L)
			val post2 = post.copy(id = 2L)
			val userPosts = listOf(post1, post2)

			whenever(postPort.findAllByUserId(userId)) doReturn userPosts
			doNothing().whenever(postPort).deleteById(1L)
			doNothing().whenever(postPort).deleteById(2L)

			postService.deletePostsByUserId(userId)

			verify(postPort, times(1)).findAllByUserId(userId)
			verify(postPort, times(1)).deleteById(1L)
			verify(postPort, times(1)).deleteById(2L)
		}
	}

	@Nested
	@DisplayName("Check post existence")
	inner class ExistsByIdTest {
		@Test
		@DisplayName("Post exists")
		fun should_return_true_when_exists() {
			val postId = 1L
			whenever(postPort.existsById(postId)) doReturn true

			val result = postService.existsById(postId)

			assertTrue(result)
		}

		@Test
		@DisplayName("Post does not exist")
		fun should_return_false_when_not_exists() {
			val notExistId = 999L
			whenever(postPort.existsById(notExistId)) doReturn false

			val result = postService.existsById(notExistId)

			assertFalse(result)
		}
	}

	@Nested
	@DisplayName("Count user's posts")
	inner class CountByUserIdTest {
		@Test
		@DisplayName("User has posts")
		fun should_return_post_count() {
			val userId = 100L
			val post1 = post.copy(id = 1L)
			val post2 = post.copy(id = 2L)
			val post3 = post.copy(id = 3L)
			val userPosts = listOf(post1, post2, post3)

			whenever(postPort.findAllByUserId(userId)) doReturn userPosts

			val result = postService.countByUserId(userId)

			assertEquals(3L, result)
		}

		@Test
		@DisplayName("User has no posts")
		fun should_return_zero() {
			val userId = 200L
			whenever(postPort.findAllByUserId(userId)) doReturn emptyList()

			val result = postService.countByUserId(userId)

			assertEquals(0L, result)
		}
	}
}
