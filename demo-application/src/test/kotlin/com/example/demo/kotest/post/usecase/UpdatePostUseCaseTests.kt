package com.example.demo.kotest.post.usecase

import com.example.demo.post.exception.PostNotFoundException
import com.example.demo.post.model.Post
import com.example.demo.post.port.input.UpdatePostInput
import com.example.demo.post.port.output.PostOutput
import com.example.demo.post.service.PostService
import com.example.demo.post.usecase.UpdatePostUseCase
import io.kotest.assertions.throwables.shouldThrowExactly
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
class UpdatePostUseCaseTests :
	BehaviorSpec({

		Given("Update post") {

			When("Update existing post") {
				Then("Updated PostOutput is returned") {
					val postService = mockk<PostService>()
					val updatePostUseCase = UpdatePostUseCase(postService)

					val post =
						Post(
							id = 1L,
							userId = 100L,
							title = "Test Title",
							subTitle = "Test SubTitle",
							content = "Test Content",
							createdDt = LocalDateTime.now(),
							updatedDt = LocalDateTime.now()
						)

					val input =
						UpdatePostInput(
							postId = post.id,
							userId = post.userId,
							title = "Updated Title",
							subTitle = "Updated SubTitle",
							content = "Updated Content"
						)

					val updatedPost =
						post.copy().apply {
							update(
								title = input.title,
								subTitle = input.subTitle,
								content = input.content
							)
						}

					every { postService.updatePostInfo(input) } returns updatedPost

					val result = updatePostUseCase.execute(input)

					result shouldNotBeNull {
						this shouldBe PostOutput.BasePostOutput.from(updatedPost)
						id shouldBe post.id
						userId shouldBe post.userId
						title shouldBe "Updated Title"
						subTitle shouldBe "Updated SubTitle"
						content shouldBe "Updated Content"
					}

					verify(exactly = 1) {
						postService.updatePostInfo(input)
					}
				}
			}

			When("Try to update non-existent post") {
				Then("PostNotFoundException is thrown") {
					val postService = mockk<PostService>()
					val updatePostUseCase = UpdatePostUseCase(postService)

					val notExistId = 999L
					val input =
						UpdatePostInput(
							postId = notExistId,
							userId = 100L,
							title = "Updated Title",
							subTitle = "Updated SubTitle",
							content = "Updated Content"
						)

					every { postService.updatePostInfo(input) } throws PostNotFoundException(notExistId)

					val exception =
						shouldThrowExactly<PostNotFoundException> {
							updatePostUseCase.execute(input)
						}

					exception.message shouldBe "Post not found. postId: $notExistId"

					verify(exactly = 1) {
						postService.updatePostInfo(input)
					}
				}
			}

			When("Update only title") {
				Then("Only title is updated") {
					val postService = mockk<PostService>()
					val updatePostUseCase = UpdatePostUseCase(postService)

					val post =
						Post(
							id = 1L,
							userId = 100L,
							title = "Test Title",
							subTitle = "Test SubTitle",
							content = "Test Content",
							createdDt = LocalDateTime.now(),
							updatedDt = LocalDateTime.now()
						)

					val input =
						UpdatePostInput(
							postId = post.id,
							userId = post.userId,
							title = "Only Title Updated",
							subTitle = post.subTitle,
							content = post.content
						)

					val updatedPost =
						post.copy().apply {
							update(
								title = input.title,
								subTitle = input.subTitle,
								content = input.content
							)
						}

					every { postService.updatePostInfo(input) } returns updatedPost

					val result = updatePostUseCase.execute(input)

					result shouldNotBeNull {
						title shouldBe "Only Title Updated"
						subTitle shouldBe post.subTitle
						content shouldBe post.content
					}
				}
			}
		}
	})
