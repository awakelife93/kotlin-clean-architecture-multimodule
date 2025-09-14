package com.example.demo.kotest.post.presentation

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
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime

@ActiveProfiles("test")
@Tags("kotest-unit-test")
class PostControllerTests :
	FunSpec({

		val createPostUseCase = mockk<CreatePostUseCase>()
		val updatePostUseCase = mockk<UpdatePostUseCase>()
		val deletePostUseCase = mockk<DeletePostUseCase>()
		val getPostByIdUseCase = mockk<GetPostByIdUseCase>()
		val getPostListUseCase = mockk<GetPostListUseCase>()
		val getExcludeUsersPostsUseCase = mockk<GetExcludeUsersPostsUseCase>()

		val postController =
			PostController(
				createPostUseCase,
				updatePostUseCase,
				deletePostUseCase,
				getPostByIdUseCase,
				getPostListUseCase,
				getExcludeUsersPostsUseCase
			)

		fun createTestPost(
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

		fun createSecurityUser(
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

		beforeTest {
			clearAllMocks()
		}

		test("Get Post By Id - returns post successfully") {
			val post = createTestPost()
			val output = PostOutput.BasePostOutput.from(post)

			every { getPostByIdUseCase.execute(GetPostByIdInput(post.id)) } returns output

			val response = postController.getPostById(post.id)

			response shouldNotBeNull {
				statusCode shouldBe HttpStatus.OK
				body shouldNotBeNull {
					postId shouldBe post.id
					title shouldBe post.title
					subTitle shouldBe post.subTitle
					content shouldBe post.content
					userId shouldBe post.userId
				}
			}

			verify { getPostByIdUseCase.execute(GetPostByIdInput(post.id)) }
		}

		test("Get Post List - returns paginated posts") {
			val post = createTestPost()
			val pageable = PageRequest.of(0, 10)
			val pageOutput =
				PostOutput.PostPageListOutput.from(
					PageImpl(listOf(post), pageable, 1)
				)

			every { getPostListUseCase.execute(GetPostListInput(pageable)) } returns pageOutput

			val response = postController.getPostList(pageable)

			response shouldNotBeNull {
				statusCode shouldBe HttpStatus.OK
				body shouldNotBeNull {
					content.size shouldBe 1
					content[0] shouldNotBeNull {
						postId shouldBe post.id
						title shouldBe post.title
						subTitle shouldBe post.subTitle
						content shouldBe post.content
						userId shouldBe post.userId
					}
				}
			}

			verify { getPostListUseCase.execute(GetPostListInput(pageable)) }
		}

		test("Get Exclude Users Post List - returns filtered posts") {
			val post = createTestPost()
			val userIds = listOf(1L, 2L, 3L)
			val pageable = PageRequest.of(0, 10)
			val pageOutput =
				PostOutput.PostPageListOutput.from(
					PageImpl(listOf(post), pageable, 1)
				)

			every {
				getExcludeUsersPostsUseCase.execute(GetExcludeUsersPostsInput(userIds, pageable))
			} returns pageOutput

			val response = postController.getExcludeUsersPostList(userIds, pageable)

			response shouldNotBeNull {
				statusCode shouldBe HttpStatus.OK
				body shouldNotBeNull {
					content.size shouldBe 1
					content[0] shouldNotBeNull {
						postId shouldBe post.id
						title shouldBe post.title
					}
				}
			}

			verify { getExcludeUsersPostsUseCase.execute(GetExcludeUsersPostsInput(userIds, pageable)) }
		}

		test("Create Post - creates and returns new post") {
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

			every {
				createPostUseCase.execute(
					CreatePostInput(
						title = request.title,
						subTitle = request.subTitle,
						content = request.content,
						userId = securityUser.userId
					)
				)
			} returns output

			val response = postController.createPost(request, securityUser)

			response shouldNotBeNull {
				statusCode shouldBe HttpStatus.CREATED
				body shouldNotBeNull {
					postId shouldBe createdPost.id
					title shouldBe request.title
					subTitle shouldBe request.subTitle
					content shouldBe request.content
					userId shouldBe securityUser.userId
				}
			}

			verify {
				createPostUseCase.execute(
					CreatePostInput(
						title = request.title,
						subTitle = request.subTitle,
						content = request.content,
						userId = securityUser.userId
					)
				)
			}
		}

		test("Update Post - updates and returns modified post") {
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

			every {
				updatePostUseCase.execute(
					UpdatePostInput(
						postId = postId,
						title = request.title,
						subTitle = request.subTitle,
						content = request.content,
						userId = securityUser.userId
					)
				)
			} returns output

			val response = postController.updatePost(request, postId, securityUser)

			response shouldNotBeNull {
				statusCode shouldBe HttpStatus.OK
				body shouldNotBeNull {
					postId shouldBe postId
					title shouldBe request.title
					subTitle shouldBe request.subTitle
					content shouldBe request.content
				}
			}

			verify {
				updatePostUseCase.execute(
					UpdatePostInput(
						postId = postId,
						title = request.title,
						subTitle = request.subTitle,
						content = request.content,
						userId = securityUser.userId
					)
				)
			}
		}

		test("Delete Post - deletes post and returns no content") {
			val postId = 1L
			val securityUser = createSecurityUser()

			justRun {
				deletePostUseCase.execute(
					DeletePostInput(postId = postId, userId = securityUser.userId)
				)
			}

			val response = postController.deletePost(postId, securityUser)

			response shouldNotBeNull {
				statusCode shouldBe HttpStatus.NO_CONTENT
				body.shouldBeNull()
			}

			verify {
				deletePostUseCase.execute(
					DeletePostInput(postId = postId, userId = securityUser.userId)
				)
			}
		}
	})
