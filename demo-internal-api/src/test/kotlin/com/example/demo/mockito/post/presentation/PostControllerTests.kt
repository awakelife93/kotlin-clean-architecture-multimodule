package com.example.demo.mockito.post.presentation

import com.example.demo.post.model.Post
import com.example.demo.post.port.input.CreatePostInput
import com.example.demo.post.port.input.DeletePostInput
import com.example.demo.post.port.input.GetExcludeUsersPostsInput
import com.example.demo.post.port.input.GetPostByIdInput
import com.example.demo.post.port.input.GetPostListInput
import com.example.demo.post.port.input.UpdatePostInput
import com.example.demo.post.port.output.PostOutput
import com.example.demo.post.presentation.PostController
import com.example.demo.post.presentation.dto.request.CreatePostRequest
import com.example.demo.post.presentation.dto.request.UpdatePostRequest
import com.example.demo.post.usecase.CreatePostUseCase
import com.example.demo.post.usecase.DeletePostUseCase
import com.example.demo.post.usecase.GetExcludeUsersPostsUseCase
import com.example.demo.post.usecase.GetPostByIdUseCase
import com.example.demo.post.usecase.GetPostListUseCase
import com.example.demo.post.usecase.UpdatePostUseCase
import com.example.demo.security.model.SecurityUserItem
import com.example.demo.user.constant.UserRole
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime

@ActiveProfiles("test")
@Tag("mockito-unit-test")
@DisplayName("Mockito Unit - Post Controller Test")
@ExtendWith(MockitoExtension::class)
class PostControllerTests {
	@InjectMocks
	private lateinit var postController: PostController

	@Mock
	private lateinit var createPostUseCase: CreatePostUseCase

	@Mock
	private lateinit var updatePostUseCase: UpdatePostUseCase

	@Mock
	private lateinit var deletePostUseCase: DeletePostUseCase

	@Mock
	private lateinit var getPostByIdUseCase: GetPostByIdUseCase

	@Mock
	private lateinit var getPostListUseCase: GetPostListUseCase

	@Mock
	private lateinit var getExcludeUsersPostsUseCase: GetExcludeUsersPostsUseCase

	private fun createTestPost(
		id: Long = 1L,
		userId: Long = 100L,
		title: String = "Test Title",
		subTitle: String = "Test SubTitle",
		content: String = "Test Content"
	) = Post(
		id = id,
		userId = userId,
		title = title,
		subTitle = subTitle,
		content = content,
		createdDt = LocalDateTime.now(),
		updatedDt = LocalDateTime.now()
	)

	private fun createSecurityUser(
		userId: Long = 1L,
		email: String = "test@example.com",
		name: String = "Test User",
		role: UserRole = UserRole.USER
	) = SecurityUserItem(
		userId = userId,
		email = email,
		name = name,
		role = role
	)

	@Test
	@DisplayName("Get post by ID returns post successfully")
	fun getPostById_withValidId_returnsPost() {
		val post = createTestPost()
		val output = PostOutput.BasePostOutput.from(post)

		whenever(getPostByIdUseCase.execute(GetPostByIdInput(post.id))) doReturn output

		val response = postController.getPostById(post.id)

		assertNotNull(response)
		assertEquals(HttpStatus.OK, response.statusCode)

		val body =
			requireNotNull(response.body) {
				"Response body should not be null"
			}
		assertEquals(post.id, body.postId)
		assertEquals(post.title, body.title)
		assertEquals(post.subTitle, body.subTitle)
		assertEquals(post.content, body.content)
		assertEquals(post.userId, body.userId)

		verify(getPostByIdUseCase).execute(GetPostByIdInput(post.id))
	}

	@Test
	@DisplayName("Get post list returns paginated posts")
	fun getPostList_withPageable_returnsPaginatedPosts() {
		val post = createTestPost()
		val pageable = PageRequest.of(0, 10)
		val pageOutput =
			PostOutput.PostPageListOutput.from(
				PageImpl(listOf(post), pageable, 1)
			)

		whenever(getPostListUseCase.execute(GetPostListInput(pageable))) doReturn pageOutput

		val response = postController.getPostList(pageable)

		assertNotNull(response)
		assertEquals(HttpStatus.OK, response.statusCode)

		val body =
			requireNotNull(response.body) {
				"Response body should not be null"
			}
		assertThat(body.content).hasSize(1)

		val firstPost = body.content[0]
		assertEquals(post.id, firstPost.postId)
		assertEquals(post.title, firstPost.title)
		assertEquals(post.subTitle, firstPost.subTitle)
		assertEquals(post.content, firstPost.content)
		assertEquals(post.userId, firstPost.userId)

		verify(getPostListUseCase).execute(GetPostListInput(pageable))
	}

	@Test
	@DisplayName("Get exclude users post list returns filtered posts")
	fun getExcludeUsersPostList_withUserIds_returnsFilteredPosts() {
		val post = createTestPost()
		val userIds = listOf(1L, 2L, 3L)
		val pageable = PageRequest.of(0, 10)
		val pageOutput =
			PostOutput.PostPageListOutput.from(
				PageImpl(listOf(post), pageable, 1)
			)

		whenever(
			getExcludeUsersPostsUseCase.execute(GetExcludeUsersPostsInput(userIds, pageable))
		) doReturn pageOutput

		val response = postController.getExcludeUsersPostList(userIds, pageable)

		assertNotNull(response)
		assertEquals(HttpStatus.OK, response.statusCode)

		val body =
			requireNotNull(response.body) {
				"Response body should not be null"
			}
		assertThat(body.content).hasSize(1)
		assertEquals(post.id, body.content[0].postId)

		verify(getExcludeUsersPostsUseCase).execute(GetExcludeUsersPostsInput(userIds, pageable))
	}

	@Test
	@DisplayName("Create post returns created post")
	fun createPost_withValidRequest_returnsCreatedPost() {
		val request =
			CreatePostRequest(
				title = "New Post",
				subTitle = "New SubTitle",
				content = "New Content"
			)
		val securityUser = createSecurityUser()
		val createdPost =
			createTestPost(
				id = 2L,
				userId = securityUser.userId,
				title = request.title,
				subTitle = request.subTitle,
				content = request.content
			)
		val output = PostOutput.BasePostOutput.from(createdPost)

		whenever(
			createPostUseCase.execute(
				CreatePostInput(
					title = request.title,
					subTitle = request.subTitle,
					content = request.content,
					userId = securityUser.userId
				)
			)
		) doReturn output

		val response = postController.createPost(request, securityUser)

		assertNotNull(response)
		assertEquals(HttpStatus.CREATED, response.statusCode)

		val body =
			requireNotNull(response.body) {
				"Response body should not be null"
			}
		assertEquals(createdPost.id, body.postId)
		assertEquals(request.title, body.title)
		assertEquals(request.subTitle, body.subTitle)
		assertEquals(request.content, body.content)
		assertEquals(securityUser.userId, body.userId)

		verify(createPostUseCase).execute(
			CreatePostInput(
				title = request.title,
				subTitle = request.subTitle,
				content = request.content,
				userId = securityUser.userId
			)
		)
	}

	@Test
	@DisplayName("Update post returns updated post")
	fun updatePost_withValidRequest_returnsUpdatedPost() {
		val postId = 1L
		val request =
			UpdatePostRequest(
				title = "Updated Title",
				subTitle = "Updated SubTitle",
				content = "Updated Content"
			)
		val securityUser = createSecurityUser()
		val updatedPost =
			createTestPost(
				id = postId,
				title = request.title,
				subTitle = request.subTitle,
				content = request.content
			)
		val output = PostOutput.BasePostOutput.from(updatedPost)

		whenever(
			updatePostUseCase.execute(
				UpdatePostInput(
					postId = postId,
					title = request.title,
					subTitle = request.subTitle,
					content = request.content,
					userId = securityUser.userId
				)
			)
		) doReturn output

		val response = postController.updatePost(request, postId, securityUser)

		assertNotNull(response)
		assertEquals(HttpStatus.OK, response.statusCode)

		val body =
			requireNotNull(response.body) {
				"Response body should not be null"
			}
		assertEquals(postId, body.postId)
		assertEquals(request.title, body.title)
		assertEquals(request.subTitle, body.subTitle)
		assertEquals(request.content, body.content)

		verify(updatePostUseCase).execute(
			UpdatePostInput(
				postId = postId,
				title = request.title,
				subTitle = request.subTitle,
				content = request.content,
				userId = securityUser.userId
			)
		)
	}

	@Test
	@DisplayName("Delete post returns no content")
	fun deletePost_withValidId_returnsNoContent() {
		val postId = 1L
		val securityUser = createSecurityUser()

		doNothing().whenever(deletePostUseCase).execute(
			DeletePostInput(postId = postId, userId = securityUser.userId)
		)

		val response = postController.deletePost(postId, securityUser)

		assertNotNull(response)
		assertEquals(HttpStatus.NO_CONTENT, response.statusCode)
		assertNull(response.body)

		verify(deletePostUseCase).execute(
			DeletePostInput(postId = postId, userId = securityUser.userId)
		)
	}
}
