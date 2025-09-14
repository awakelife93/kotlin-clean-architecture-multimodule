package com.example.demo.kotest.post.usecase

import com.example.demo.post.exception.PostNotFoundException
import com.example.demo.post.model.Post
import com.example.demo.post.port.input.GetPostByIdInput
import com.example.demo.post.port.output.PostOutput
import com.example.demo.post.service.PostService
import com.example.demo.post.usecase.GetPostByIdUseCase
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime

@ActiveProfiles("test")
@Tags("kotest-unit-test")
class GetPostByIdUseCaseTests :
	BehaviorSpec({
		val postService = mockk<PostService>()
		val getPostByIdUseCase = GetPostByIdUseCase(postService)

		val post: Post =
			Post(
				id = 1L,
				userId = 100L,
				title = "Test Title",
				subTitle = "Test SubTitle",
				content = "Test Content",
				createdDt = LocalDateTime.now(),
				updatedDt = LocalDateTime.now()
			)

		Given("Get post by ID") {

			When("Get existing post") {
				val input = GetPostByIdInput(postId = post.id)

				every { postService.findOneByIdOrThrow(post.id) } returns post

				val result = getPostByIdUseCase.execute(input)

				Then("PostOutput is returned successfully") {
					result shouldNotBeNull {
						this shouldBe PostOutput.BasePostOutput.from(post)
						id shouldBe post.id
						userId shouldBe post.userId
						title shouldBe post.title
						subTitle shouldBe post.subTitle
						content shouldBe post.content
					}
				}
			}

			When("Get non-existent post") {
				val notExistId = 999L
				val input = GetPostByIdInput(postId = notExistId)

				every { postService.findOneByIdOrThrow(notExistId) } throws PostNotFoundException(notExistId)

				Then("PostNotFoundException is thrown") {
					val exception =
						shouldThrowExactly<PostNotFoundException> {
							getPostByIdUseCase.execute(input)
						}

					exception.message shouldBe "Post not found. postId: $notExistId"
				}
			}
		}
	})
