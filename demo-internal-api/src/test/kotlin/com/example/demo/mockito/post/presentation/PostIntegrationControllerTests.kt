package com.example.demo.mockito.post.presentation

import com.example.demo.mockito.common.BaseIntegrationController
import com.example.demo.mockito.common.security.WithMockCustomUser
import com.example.demo.post.exception.PostNotFoundException
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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.whenever
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders

@ActiveProfiles("test")
@Tag("mockito-integration-test")
@DisplayName("Mockito Integration - Post Controller Test")
@WebMvcTest(PostController::class)
@ExtendWith(MockitoExtension::class)
class PostIntegrationControllerTests : BaseIntegrationController() {
	@MockitoBean
	private lateinit var createPostUseCase: CreatePostUseCase

	@MockitoBean
	private lateinit var updatePostUseCase: UpdatePostUseCase

	@MockitoBean
	private lateinit var deletePostUseCase: DeletePostUseCase

	@MockitoBean
	private lateinit var getPostByIdUseCase: GetPostByIdUseCase

	@MockitoBean
	private lateinit var getPostListUseCase: GetPostListUseCase

	@MockitoBean
	private lateinit var getExcludeUsersPostsUseCase: GetExcludeUsersPostsUseCase

	private val post =
		Post(
			id = 1L,
			title = "Test Post Title",
			subTitle = "Test Post SubTitle",
			content = "Test Post Content",
			userId = 1L
		)
	private val defaultPageable = Pageable.ofSize(1)
	private val basePostOutput = PostOutput.BasePostOutput.from(post)
	private val postPageListOutput = PostOutput.PostPageListOutput.from(PageImpl(listOf(post), defaultPageable, 1))
	private val emptyPostPageListOutput = PostOutput.PostPageListOutput.from(PageImpl(listOf(), defaultPageable, 0))

	@BeforeEach
	fun setUp() {
		mockMvc =
			MockMvcBuilders
				.webAppContextSetup(webApplicationContext)
				.apply<DefaultMockMvcBuilder>(SecurityMockMvcConfigurers.springSecurity())
				.alwaysDo<DefaultMockMvcBuilder>(MockMvcResultHandlers.print())
				.build()
	}

	@Nested
	@DisplayName("GET /api/v1/posts/{postId} Test")
	inner class GetPostByIdTest {
		@Test
		@DisplayName("GET /api/v1/posts/{postId} Response")
		@WithMockCustomUser
		@Throws(Exception::class)
		fun should_ExpectOKResponseToGetPostResponse_when_GivenPostIdAndUserIsAuthenticated() {
			whenever(getPostByIdUseCase.execute(any<GetPostByIdInput>()))
				.thenReturn(basePostOutput)

			mockMvc
				.perform(
					MockMvcRequestBuilders
						.get("/api/v1/posts/{postId}", post.id)
						.with(SecurityMockMvcRequestPostProcessors.csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)
				).andExpect(MockMvcResultMatchers.status().isOk)
				.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(commonMessage))
				.andExpect(MockMvcResultMatchers.jsonPath("$.data.postId").value(post.id))
				.andExpect(MockMvcResultMatchers.jsonPath("$.data.title").value(post.title))
				.andExpect(MockMvcResultMatchers.jsonPath("$.data.subTitle").value(post.subTitle))
				.andExpect(MockMvcResultMatchers.jsonPath("$.data.content").value(post.content))
				.andExpect(MockMvcResultMatchers.jsonPath("$.data.userId").value(post.userId))
		}

		@Test
		@DisplayName("Not Found Exception GET /api/v1/posts/{postId} Response")
		@WithMockCustomUser
		@Throws(Exception::class)
		fun should_ExpectErrorResponseToPostNotFoundException_when_GivenPostIdAndUserIsAuthenticated() {
			val postNotFoundException = PostNotFoundException(post.id)

			whenever(getPostByIdUseCase.execute(any<GetPostByIdInput>()))
				.thenThrow(postNotFoundException)

			mockMvc
				.perform(
					MockMvcRequestBuilders
						.get("/api/v1/posts/{postId}", post.id)
						.with(SecurityMockMvcRequestPostProcessors.csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)
				).andExpect(MockMvcResultMatchers.status().isNotFound)
				.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(postNotFoundException.message))
				.andExpect(MockMvcResultMatchers.jsonPath("$.errors").isEmpty)
		}

		@Test
		@DisplayName("Unauthorized Exception GET /api/v1/posts/{postId} Response")
		@Throws(Exception::class)
		fun should_ExpectErrorResponseToUnauthorizedException_when_GivenPostIdAndUserIsNotAuthenticated() {
			mockMvc
				.perform(
					MockMvcRequestBuilders
						.get("/api/v1/posts/{postId}", post.id)
						.with(SecurityMockMvcRequestPostProcessors.csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)
				).andExpect(MockMvcResultMatchers.status().isUnauthorized)
		}
	}

	@Nested
	@DisplayName("GET /api/v1/posts Test")
	inner class GetPostListTest {
		@Test
		@DisplayName("GET /api/v1/posts Response")
		@WithMockCustomUser
		@Throws(Exception::class)
		fun should_ExpectOKResponseToPageOfGetPostResponse_when_GivenDefaultPageableAndUserIsAuthenticated() {
			whenever(getPostListUseCase.execute(any<GetPostListInput>()))
				.thenReturn(postPageListOutput)

			mockMvc
				.perform(
					MockMvcRequestBuilders
						.get("/api/v1/posts")
						.with(SecurityMockMvcRequestPostProcessors.csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)
				).andExpect(MockMvcResultMatchers.status().isOk)
				.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(commonMessage))
				.andExpect(MockMvcResultMatchers.jsonPath("$.data.content[0].postId").value(post.id))
				.andExpect(MockMvcResultMatchers.jsonPath("$.data.content[0].title").value(post.title))
				.andExpect(MockMvcResultMatchers.jsonPath("$.data.content[0].subTitle").value(post.subTitle))
				.andExpect(MockMvcResultMatchers.jsonPath("$.data.content[0].content").value(post.content))
				.andExpect(MockMvcResultMatchers.jsonPath("$.data.content[0].userId").value(post.userId))
		}

		@Test
		@DisplayName("Empty GET /api/v1/posts Response")
		@WithMockCustomUser
		@Throws(Exception::class)
		fun should_ExpectOKResponseToPageOfGetPostResponseIsEmpty_when_GivenDefaultPageableAndUserIsAuthenticated() {
			whenever(getPostListUseCase.execute(any<GetPostListInput>()))
				.thenReturn(emptyPostPageListOutput)

			mockMvc
				.perform(
					MockMvcRequestBuilders
						.get("/api/v1/posts")
						.with(SecurityMockMvcRequestPostProcessors.csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)
				).andExpect(MockMvcResultMatchers.status().isOk)
				.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(commonMessage))
				.andExpect(MockMvcResultMatchers.jsonPath("$.data.content").isEmpty)
		}

		@Test
		@DisplayName("Unauthorized Exception GET /api/v1/posts Response")
		@Throws(Exception::class)
		fun should_ExpectErrorResponseToUnauthorizedException_when_GivenDefaultPageableAndUserIsNotAuthenticated() {
			mockMvc
				.perform(
					MockMvcRequestBuilders
						.get("/api/v1/posts")
						.with(SecurityMockMvcRequestPostProcessors.csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)
				).andExpect(MockMvcResultMatchers.status().isUnauthorized)
		}
	}

	@Nested
	@DisplayName("GET /api/v1/posts/exclude-users Test")
	inner class GetExcludeUsersPostListTest {
		private val userIds = listOf(1L, 2L, 3L)

		@Test
		@DisplayName("GET /api/v1/posts/exclude-users Response")
		@WithMockCustomUser
		@Throws(Exception::class)
		fun should_ExpectOKResponseToPageOfGetPostResponse_when_GivenDefaultPageableAndUserIdsAndUserIsAuthenticated() {
			whenever(getExcludeUsersPostsUseCase.execute(any<GetExcludeUsersPostsInput>()))
				.thenReturn(postPageListOutput)

			mockMvc
				.perform(
					MockMvcRequestBuilders
						.get("/api/v1/posts/exclude-users")
						.with(SecurityMockMvcRequestPostProcessors.csrf())
						.param("userIds", userIds[0].toString())
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)
				).andExpect(MockMvcResultMatchers.status().isOk)
				.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(commonMessage))
				.andExpect(MockMvcResultMatchers.jsonPath("$.data.content[0].postId").value(post.id))
				.andExpect(MockMvcResultMatchers.jsonPath("$.data.content[0].title").value(post.title))
				.andExpect(MockMvcResultMatchers.jsonPath("$.data.content[0].subTitle").value(post.subTitle))
				.andExpect(MockMvcResultMatchers.jsonPath("$.data.content[0].content").value(post.content))
				.andExpect(MockMvcResultMatchers.jsonPath("$.data.content[0].userId").value(post.userId))
		}

		@Test
		@DisplayName("Empty GET /api/v1/posts/exclude-users Response")
		@WithMockCustomUser
		@Throws(Exception::class)
		fun should_ExpectOKResponseToPageOfGetPostResponseIsEmpty_when_GivenDefaultPageableAndUserIdsAndUserIsAuthenticated() {
			whenever(getExcludeUsersPostsUseCase.execute(any<GetExcludeUsersPostsInput>()))
				.thenReturn(emptyPostPageListOutput)

			mockMvc
				.perform(
					MockMvcRequestBuilders
						.get("/api/v1/posts/exclude-users")
						.with(SecurityMockMvcRequestPostProcessors.csrf())
						.param("userIds", userIds[0].toString())
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)
				).andExpect(MockMvcResultMatchers.status().isOk)
				.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(commonMessage))
				.andExpect(MockMvcResultMatchers.jsonPath("$.data.content").isEmpty)
		}

		@Test
		@DisplayName("Unauthorized Exception GET /api/v1/posts/exclude-users Response")
		@Throws(Exception::class)
		fun should_ExpectErrorResponseToUnauthorizedException_when_GivenDefaultPageableAndUserIdsAndUserIsNotAuthenticated() {
			mockMvc
				.perform(
					MockMvcRequestBuilders
						.get("/api/v1/posts/exclude-users")
						.with(SecurityMockMvcRequestPostProcessors.csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)
				).andExpect(MockMvcResultMatchers.status().isUnauthorized)
		}
	}

	@Nested
	@DisplayName("PUT /api/v1/posts Test")
	inner class CreatePostTest {
		private val createPostRequest =
			CreatePostRequest(
				title = "New Post Title",
				subTitle = "New Post SubTitle",
				content = "New Post Content"
			)

		@Test
		@DisplayName("PUT /api/v1/posts Response")
		@WithMockCustomUser
		@Throws(Exception::class)
		fun should_ExpectOKResponseToCreatePostResponse_when_GivenUserIdAndCreatePostRequestAndUserIsAuthenticated() {
			whenever(createPostUseCase.execute(any<CreatePostInput>()))
				.thenReturn(basePostOutput)

			mockMvc
				.perform(
					MockMvcRequestBuilders
						.put("/api/v1/posts")
						.with(SecurityMockMvcRequestPostProcessors.csrf())
						.content(objectMapper.writeValueAsString(createPostRequest))
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)
				).andExpect(MockMvcResultMatchers.status().isCreated)
				.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(commonMessage))
				.andExpect(MockMvcResultMatchers.jsonPath("$.data.postId").value(post.id))
				.andExpect(MockMvcResultMatchers.jsonPath("$.data.title").value(post.title))
				.andExpect(MockMvcResultMatchers.jsonPath("$.data.subTitle").value(post.subTitle))
				.andExpect(MockMvcResultMatchers.jsonPath("$.data.content").value(post.content))
				.andExpect(MockMvcResultMatchers.jsonPath("$.data.userId").value(post.userId))
		}

		@Test
		@DisplayName("Field Valid Exception PUT /api/v1/posts Response")
		@WithMockCustomUser
		@Throws(Exception::class)
		fun should_ExpectErrorResponseToValidException_when_GivenUserIdAndWrongCreatePostRequestAndUserIsAuthenticated() {
			val wrongCreatePostRequest =
				createPostRequest.copy(
					title = "",
					subTitle = ""
				)

			mockMvc
				.perform(
					MockMvcRequestBuilders
						.put("/api/v1/posts")
						.with(SecurityMockMvcRequestPostProcessors.csrf())
						.content(objectMapper.writeValueAsString(wrongCreatePostRequest))
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)
				).andExpect(MockMvcResultMatchers.status().isBadRequest)
				.andExpect(MockMvcResultMatchers.jsonPath("$.message").isString)
				.andExpect(MockMvcResultMatchers.jsonPath("$.errors").isNotEmpty)
		}

		@Test
		@DisplayName("Unauthorized Exception PUT /api/v1/posts Response")
		@Throws(Exception::class)
		fun should_ExpectErrorResponseToUnauthorizedException_when_GivenUserIdAndCreatePostRequestAndUserIsNotAuthenticated() {
			mockMvc
				.perform(
					MockMvcRequestBuilders
						.put("/api/v1/posts")
						.with(SecurityMockMvcRequestPostProcessors.csrf())
						.content(objectMapper.writeValueAsString(createPostRequest))
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)
				).andExpect(MockMvcResultMatchers.status().isUnauthorized)
		}
	}

	@Nested
	@DisplayName("PATCH /api/v1/posts/{postId} Test")
	inner class UpdatePostTest {
		private val updatePostRequest =
			UpdatePostRequest(
				title = "Updated Post Title",
				subTitle = "Updated Post SubTitle",
				content = "Updated Post Content"
			)

		@Test
		@DisplayName("PATCH /api/v1/posts/{postId} Response")
		@WithMockCustomUser
		@Throws(Exception::class)
		fun should_ExpectOKResponseToUpdatePostResponse_when_GivenPostIdAndUpdatePostRequestAndUserIsAuthenticated() {
			whenever(updatePostUseCase.execute(any<UpdatePostInput>()))
				.thenReturn(basePostOutput)

			mockMvc
				.perform(
					MockMvcRequestBuilders
						.patch("/api/v1/posts/{postId}", post.id)
						.with(SecurityMockMvcRequestPostProcessors.csrf())
						.content(objectMapper.writeValueAsString(updatePostRequest))
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)
				).andExpect(MockMvcResultMatchers.status().isOk)
				.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(commonMessage))
				.andExpect(MockMvcResultMatchers.jsonPath("$.data.postId").value(post.id))
				.andExpect(MockMvcResultMatchers.jsonPath("$.data.title").value(post.title))
				.andExpect(MockMvcResultMatchers.jsonPath("$.data.subTitle").value(post.subTitle))
				.andExpect(MockMvcResultMatchers.jsonPath("$.data.content").value(post.content))
				.andExpect(MockMvcResultMatchers.jsonPath("$.data.userId").value(post.userId))
		}

		@Test
		@DisplayName("Field Valid Exception PATCH /api/v1/posts/{postId} Response")
		@WithMockCustomUser
		@Throws(Exception::class)
		fun should_ExpectErrorResponseToValidException_when_GivenPostIdAndWrongUpdatePostRequestAndUserIsAuthenticated() {
			val wrongUpdatePostRequest =
				updatePostRequest.copy(
					title = "",
					subTitle = ""
				)

			mockMvc
				.perform(
					MockMvcRequestBuilders
						.patch("/api/v1/posts/{postId}", post.id)
						.with(SecurityMockMvcRequestPostProcessors.csrf())
						.content(objectMapper.writeValueAsString(wrongUpdatePostRequest))
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)
				).andExpect(MockMvcResultMatchers.status().isBadRequest)
				.andExpect(MockMvcResultMatchers.jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.message").isString)
				.andExpect(MockMvcResultMatchers.jsonPath("$.errors").isNotEmpty)
		}

		@Test
		@DisplayName("Unauthorized Exception PATCH /api/v1/posts/{postId} Response")
		@Throws(Exception::class)
		fun should_ExpectErrorResponseToUnauthorizedException_when_GivenPostIdAndUpdatePostRequestAndUserIsNotAuthenticated() {
			mockMvc
				.perform(
					MockMvcRequestBuilders
						.patch("/api/v1/posts/{postId}", post.id)
						.with(SecurityMockMvcRequestPostProcessors.csrf())
						.content(objectMapper.writeValueAsString(updatePostRequest))
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)
				).andExpect(MockMvcResultMatchers.status().isUnauthorized)
		}

		@Test
		@DisplayName("Not Found Exception PATCH /api/v1/posts/{postId} Response")
		@WithMockCustomUser
		@Throws(Exception::class)
		fun should_ExpectErrorResponseToPostNotFoundException_when_GivenPostIdAndUpdatePostRequestAndUserIsAuthenticated() {
			val postNotFoundException = PostNotFoundException(post.id)

			whenever(updatePostUseCase.execute(any<UpdatePostInput>()))
				.thenThrow(postNotFoundException)

			mockMvc
				.perform(
					MockMvcRequestBuilders
						.patch("/api/v1/posts/{postId}", post.id)
						.with(SecurityMockMvcRequestPostProcessors.csrf())
						.content(objectMapper.writeValueAsString(updatePostRequest))
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)
				).andExpect(MockMvcResultMatchers.status().isNotFound)
				.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(postNotFoundException.message))
				.andExpect(MockMvcResultMatchers.jsonPath("$.errors").isEmpty)
		}
	}

	@Nested
	@DisplayName("DELETE /api/v1/posts/{postId} Test")
	inner class DeletePostTest {
		@Test
		@DisplayName("DELETE /api/v1/posts/{postId} Response")
		@WithMockCustomUser
		@Throws(Exception::class)
		fun should_ExpectOKResponse_when_GivenPostIdAndUserIsAuthenticated() {
			doNothing().whenever(deletePostUseCase).execute(any<DeletePostInput>())

			mockMvc
				.perform(
					MockMvcRequestBuilders
						.delete("/api/v1/posts/{postId}", post.id)
						.with(SecurityMockMvcRequestPostProcessors.csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)
				).andExpect(MockMvcResultMatchers.status().isNoContent)
				.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(commonMessage))
		}

		@Test
		@DisplayName("Unauthorized Error DELETE /api/v1/posts/{postId} Response")
		@Throws(Exception::class)
		fun should_ExpectErrorResponseToUnauthorizedException_when_GivenPostIdAndUserIsNotAuthenticated() {
			mockMvc
				.perform(
					MockMvcRequestBuilders
						.delete("/api/v1/posts/{postId}", post.id)
						.with(SecurityMockMvcRequestPostProcessors.csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)
				).andExpect(MockMvcResultMatchers.status().isUnauthorized)
		}
	}
}
