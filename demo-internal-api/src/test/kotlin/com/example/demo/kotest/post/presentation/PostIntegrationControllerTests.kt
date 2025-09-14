package com.example.demo.kotest.post.presentation

import com.example.demo.kotest.common.BaseIntegrationController
import com.example.demo.kotest.common.security.SecurityListenerFactory
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
import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.annotation.Tags
import io.mockk.every
import io.mockk.justRun
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@ActiveProfiles("test")
@Tags("kotest-integration-test")
@WebMvcTest(PostController::class)
class PostIntegrationControllerTests : BaseIntegrationController() {
	@MockkBean
	private lateinit var createPostUseCase: CreatePostUseCase

	@MockkBean
	private lateinit var updatePostUseCase: UpdatePostUseCase

	@MockkBean
	private lateinit var deletePostUseCase: DeletePostUseCase

	@MockkBean
	private lateinit var getPostByIdUseCase: GetPostByIdUseCase

	@MockkBean
	private lateinit var getPostListUseCase: GetPostListUseCase

	@MockkBean
	private lateinit var getExcludeUsersPostsUseCase: GetExcludeUsersPostsUseCase

	val post =
		Post(
			id = 1L,
			title = "Test Post Title",
			subTitle = "Test Post SubTitle",
			content = "Test Post Content",
			userId = 1L
		)
	val defaultPageable = Pageable.ofSize(1)
	val basePostOutput = PostOutput.BasePostOutput.from(post)
	val postPageListOutput = PostOutput.PostPageListOutput.from(PageImpl(listOf(post), defaultPageable, 1))
	val emptyPostPageListOutput = PostOutput.PostPageListOutput.from(PageImpl(listOf(), defaultPageable, 0))

	init {
		initialize()

		Given("GET /api/v1/posts/{postId}") {

			When("Success GET /api/v1/posts/{postId}") {

				every { getPostByIdUseCase.execute(any<GetPostByIdInput>()) } returns basePostOutput

				Then("Call GET /api/v1/posts/{postId}") {
					mockMvc
						.perform(
							MockMvcRequestBuilders
								.get("/api/v1/posts/{postId}", post.id)
								.with(SecurityMockMvcRequestPostProcessors.csrf())
								.contentType(MediaType.APPLICATION_JSON)
								.accept(MediaType.APPLICATION_JSON)
						).andExpect(MockMvcResultMatchers.status().isOk)
						.andExpect(MockMvcResultMatchers.jsonPath("$.code").value(commonStatus))
						.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(commonMessage))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.postId").value(post.id))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.title").value(post.title))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.subTitle").value(post.subTitle))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.content").value(post.content))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.userId").value(post.userId))
				}
			}

			When("Not Found Exception GET /api/v1/posts/{postId}") {
				val postNotFoundException = PostNotFoundException(post.id)

				every { getPostByIdUseCase.execute(any<GetPostByIdInput>()) } throws postNotFoundException

				Then("Call GET /api/v1/posts/{postId}") {
					mockMvc
						.perform(
							MockMvcRequestBuilders
								.get("/api/v1/posts/{postId}", post.id)
								.with(SecurityMockMvcRequestPostProcessors.csrf())
								.contentType(MediaType.APPLICATION_JSON)
								.accept(MediaType.APPLICATION_JSON)
						).andExpect(MockMvcResultMatchers.status().isNotFound)
						.andExpect(MockMvcResultMatchers.jsonPath("$.code").value(HttpStatus.NOT_FOUND.value()))
						.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(postNotFoundException.message))
						.andExpect(MockMvcResultMatchers.jsonPath("$.errors").isEmpty)
				}
			}
		}

		Given("GET /api/v1/posts") {

			When("Success GET /api/v1/posts") {

				every { getPostListUseCase.execute(any<GetPostListInput>()) } returns
					postPageListOutput

				Then("Call GET /api/v1/posts") {
					mockMvc
						.perform(
							MockMvcRequestBuilders
								.get("/api/v1/posts")
								.with(SecurityMockMvcRequestPostProcessors.csrf())
								.contentType(MediaType.APPLICATION_JSON)
								.accept(MediaType.APPLICATION_JSON)
						).andExpect(MockMvcResultMatchers.status().isOk)
						.andExpect(MockMvcResultMatchers.jsonPath("$.code").value(commonStatus))
						.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(commonMessage))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.content[0].postId").value(post.id))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.content[0].title").value(post.title))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.content[0].subTitle").value(post.subTitle))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.content[0].content").value(post.content))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.content[0].userId").value(post.userId))
				}
			}

			When("Empty GET /api/v1/posts") {

				every { getPostListUseCase.execute(any<GetPostListInput>()) } returns
					emptyPostPageListOutput

				Then("Call GET /api/v1/posts") {
					mockMvc
						.perform(
							MockMvcRequestBuilders
								.get("/api/v1/posts")
								.with(SecurityMockMvcRequestPostProcessors.csrf())
								.contentType(MediaType.APPLICATION_JSON)
								.accept(MediaType.APPLICATION_JSON)
						).andExpect(MockMvcResultMatchers.status().isOk)
						.andExpect(MockMvcResultMatchers.jsonPath("$.code").value(commonStatus))
						.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(commonMessage))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.content").isEmpty)
				}
			}
		}

		Given("GET /api/v1/posts/exclude-users") {
			val userIds = listOf(1L, 2L, 3L)

			When("Success GET /api/v1/posts/exclude-users") {

				every { getExcludeUsersPostsUseCase.execute(any<GetExcludeUsersPostsInput>()) } returns
					postPageListOutput

				Then("Call GET /api/v1/posts/exclude-users") {
					mockMvc
						.perform(
							MockMvcRequestBuilders
								.get("/api/v1/posts/exclude-users")
								.with(SecurityMockMvcRequestPostProcessors.csrf())
								.param("userIds", userIds[0].toString())
								.contentType(MediaType.APPLICATION_JSON)
								.accept(MediaType.APPLICATION_JSON)
						).andExpect(MockMvcResultMatchers.status().isOk)
						.andExpect(MockMvcResultMatchers.jsonPath("$.code").value(commonStatus))
						.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(commonMessage))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.content[0].postId").value(post.id))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.content[0].title").value(post.title))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.content[0].subTitle").value(post.subTitle))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.content[0].content").value(post.content))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.content[0].userId").value(post.userId))
				}
			}

			When("Empty GET /api/v1/posts/exclude-users") {

				every { getExcludeUsersPostsUseCase.execute(any<GetExcludeUsersPostsInput>()) } returns
					emptyPostPageListOutput

				Then("Call GET /api/v1/posts/exclude-users") {
					mockMvc
						.perform(
							MockMvcRequestBuilders
								.get("/api/v1/posts/exclude-users")
								.with(SecurityMockMvcRequestPostProcessors.csrf())
								.param("userIds", userIds[0].toString())
								.contentType(MediaType.APPLICATION_JSON)
								.accept(MediaType.APPLICATION_JSON)
						).andExpect(MockMvcResultMatchers.status().isOk)
						.andExpect(MockMvcResultMatchers.jsonPath("$.code").value(commonStatus))
						.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(commonMessage))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.content").isEmpty)
				}
			}
		}

		Given("PUT /api/v1/posts") {
			val createPostRequest =
				CreatePostRequest(
					title = "New Post Title",
					subTitle = "New Post SubTitle",
					content = "New Post Content"
				)

			When("Success PUT /api/v1/posts") {

				every { createPostUseCase.execute(any<CreatePostInput>()) } returns basePostOutput

				Then("Call PUT /api/v1/posts") {
					mockMvc
						.perform(
							MockMvcRequestBuilders
								.put("/api/v1/posts")
								.with(SecurityMockMvcRequestPostProcessors.csrf())
								.content(objectMapper.writeValueAsString(createPostRequest))
								.contentType(MediaType.APPLICATION_JSON)
								.accept(MediaType.APPLICATION_JSON)
						).andExpect(MockMvcResultMatchers.status().isCreated)
						.andExpect(MockMvcResultMatchers.jsonPath("$.code").value(commonStatus))
						.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(commonMessage))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.postId").value(post.id))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.title").value(post.title))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.subTitle").value(post.subTitle))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.content").value(post.content))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.userId").value(post.userId))
				}
			}

			When("Field Valid Exception PUT /api/v1/posts") {
				val wrongCreatePostRequest =
					CreatePostRequest(
						title = "",
						subTitle = "",
						content = "Content"
					)

				Then("Call PUT /api/v1/posts") {
					mockMvc
						.perform(
							MockMvcRequestBuilders
								.put("/api/v1/posts")
								.with(SecurityMockMvcRequestPostProcessors.csrf())
								.content(objectMapper.writeValueAsString(wrongCreatePostRequest))
								.contentType(MediaType.APPLICATION_JSON)
								.accept(MediaType.APPLICATION_JSON)
						).andExpect(MockMvcResultMatchers.status().isBadRequest)
						.andExpect(MockMvcResultMatchers.jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
						.andExpect(MockMvcResultMatchers.jsonPath("$.message").isString)
						.andExpect(MockMvcResultMatchers.jsonPath("$.errors").isNotEmpty)
				}
			}
		}

		Given("PATCH /api/v1/posts/{postId}") {
			val updatePostRequest =
				UpdatePostRequest(
					title = "Updated Post Title",
					subTitle = "Updated Post SubTitle",
					content = "Updated Post Content"
				)

			When("Success PATCH /api/v1/posts/{postId}") {

				every { updatePostUseCase.execute(any<UpdatePostInput>()) } returns basePostOutput

				Then("Call PATCH /api/v1/posts/{postId}") {
					mockMvc
						.perform(
							MockMvcRequestBuilders
								.patch("/api/v1/posts/{postId}", post.id)
								.with(SecurityMockMvcRequestPostProcessors.csrf())
								.content(objectMapper.writeValueAsString(updatePostRequest))
								.contentType(MediaType.APPLICATION_JSON)
								.accept(MediaType.APPLICATION_JSON)
						).andExpect(MockMvcResultMatchers.status().isOk)
						.andExpect(MockMvcResultMatchers.jsonPath("$.code").value(commonStatus))
						.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(commonMessage))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.postId").value(post.id))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.title").value(post.title))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.subTitle").value(post.subTitle))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.content").value(post.content))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.userId").value(post.userId))
				}
			}

			When("Field Valid Exception PATCH /api/v1/posts/{postId}") {
				val wrongUpdatePostRequest =
					UpdatePostRequest(
						title = "",
						subTitle = "",
						content = "Content"
					)

				Then("Call PATCH /api/v1/posts/{postId}") {
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
			}

			When("Not Found Exception PATCH /api/v1/posts/{postId}") {
				val postNotFoundException = PostNotFoundException(post.id)

				every { updatePostUseCase.execute(any<UpdatePostInput>()) } throws postNotFoundException

				Then("Call PATCH /api/v1/posts/{postId}") {
					mockMvc
						.perform(
							MockMvcRequestBuilders
								.patch("/api/v1/posts/{postId}", post.id)
								.with(SecurityMockMvcRequestPostProcessors.csrf())
								.content(objectMapper.writeValueAsString(updatePostRequest))
								.contentType(MediaType.APPLICATION_JSON)
								.accept(MediaType.APPLICATION_JSON)
						).andExpect(MockMvcResultMatchers.status().isNotFound)
						.andExpect(MockMvcResultMatchers.jsonPath("$.code").value(HttpStatus.NOT_FOUND.value()))
						.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(postNotFoundException.message))
						.andExpect(MockMvcResultMatchers.jsonPath("$.errors").isEmpty)
				}
			}
		}

		Given("DELETE /api/v1/posts/{postId}") {

			When("Success DELETE /api/v1/posts/{postId}") {

				justRun { deletePostUseCase.execute(any<DeletePostInput>()) }

				Then("Call DELETE /api/v1/posts/{postId}") {
					mockMvc
						.perform(
							MockMvcRequestBuilders
								.delete("/api/v1/posts/{postId}", post.id)
								.with(SecurityMockMvcRequestPostProcessors.csrf())
								.contentType(MediaType.APPLICATION_JSON)
								.accept(MediaType.APPLICATION_JSON)
						).andExpect(MockMvcResultMatchers.status().isNoContent)
						.andExpect(MockMvcResultMatchers.jsonPath("$.code").value(commonStatus))
						.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(commonMessage))
				}
			}
		}

		Given("Spring Security Context is not set.") {

			When("UnAuthorized Exception GET /api/v1/posts/{postId}") {

				Then("Call GET /api/v1/posts/{postId}").config(tags = setOf(SecurityListenerFactory.NonSecurityOption)) {
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

			When("UnAuthorized Exception GET /api/v1/posts") {

				Then("Call GET /api/v1/posts").config(tags = setOf(SecurityListenerFactory.NonSecurityOption)) {
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

			When("UnAuthorized Exception GET /api/v1/posts/exclude-users") {

				Then("Call GET /api/v1/posts/exclude-users").config(tags = setOf(SecurityListenerFactory.NonSecurityOption)) {
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

			When("UnAuthorized Exception PUT /api/v1/posts") {

				Then("Call PUT /api/v1/posts").config(tags = setOf(SecurityListenerFactory.NonSecurityOption)) {
					mockMvc
						.perform(
							MockMvcRequestBuilders
								.put("/api/v1/posts")
								.with(SecurityMockMvcRequestPostProcessors.csrf())
								.contentType(MediaType.APPLICATION_JSON)
								.accept(MediaType.APPLICATION_JSON)
						).andExpect(MockMvcResultMatchers.status().isUnauthorized)
				}
			}

			When("UnAuthorized Exception PATCH /api/v1/posts/{postId}") {

				Then("Call PATCH /api/v1/posts/{postId}").config(tags = setOf(SecurityListenerFactory.NonSecurityOption)) {
					mockMvc
						.perform(
							MockMvcRequestBuilders
								.patch("/api/v1/posts/{postId}", post.id)
								.with(SecurityMockMvcRequestPostProcessors.csrf())
								.contentType(MediaType.APPLICATION_JSON)
								.accept(MediaType.APPLICATION_JSON)
						).andExpect(MockMvcResultMatchers.status().isUnauthorized)
				}
			}

			When("UnAuthorized Exception DELETE /api/v1/posts/{postId}") {

				Then("Call DELETE /api/v1/posts/{postId}").config(tags = setOf(SecurityListenerFactory.NonSecurityOption)) {
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
	}
}
