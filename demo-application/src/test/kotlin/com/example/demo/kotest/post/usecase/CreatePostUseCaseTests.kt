package com.example.demo.kotest.post.usecase

import com.example.demo.post.model.Post
import com.example.demo.post.port.input.CreatePostInput
import com.example.demo.post.port.output.PostOutput
import com.example.demo.post.service.PostService
import com.example.demo.post.usecase.CreatePostUseCase
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime

@ActiveProfiles("test")
@Tags("kotest-unit-test")
class CreatePostUseCaseTests :
	BehaviorSpec({
		val postService = mockk<PostService>()
		val createPostUseCase = CreatePostUseCase(postService)

		Given("Create post") {

			When("Create post with valid input") {
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

				every { postService.createPost(input) } returns savedPost

				val result = createPostUseCase.execute(input)

				Then("BasePostOutput is returned successfully") {
					result shouldNotBeNull {
						this shouldBe PostOutput.BasePostOutput.from(savedPost)
						id shouldBe 1L
						userId shouldBe input.userId
						title shouldBe "New Post"
						subTitle shouldBe "New SubTitle"
						content shouldBe "New Content"
					}

					verify(exactly = 1) {
						postService.createPost(input)
					}
				}
			}

			When("Create post with empty subtitle") {
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

				every { postService.createPost(input) } returns savedPost

				val result = createPostUseCase.execute(input)

				Then("Post is created with empty subtitle") {
					result shouldNotBeNull {
						id shouldBe 2L
						title shouldBe "Title Only"
						subTitle shouldBe ""
						content shouldBe "Content"
					}
				}
			}

			When("Create post with minimal content") {
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

				every { postService.createPost(input) } returns savedPost

				val result = createPostUseCase.execute(input)

				Then("Post is created with minimal content") {
					result shouldNotBeNull {
						id shouldBe 3L
						title shouldBe "T"
						subTitle shouldBe "S"
						content shouldBe "C"
					}
				}
			}
		}
	})
