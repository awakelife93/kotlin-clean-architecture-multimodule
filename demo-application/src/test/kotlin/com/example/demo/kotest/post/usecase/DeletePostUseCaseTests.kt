package com.example.demo.kotest.post.usecase

import com.example.demo.post.exception.PostNotFoundException
import com.example.demo.post.model.Post
import com.example.demo.post.port.input.DeletePostInput
import com.example.demo.post.service.PostService
import com.example.demo.post.usecase.DeletePostUseCase
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime

@ActiveProfiles("test")
@Tags("kotest-unit-test")
class DeletePostUseCaseTests :
	BehaviorSpec({

		Given("Delete post") {

			When("Post owner requests deletion") {
				Then("Post is deleted successfully") {
					val postService = mockk<PostService>()
					val deletePostUseCase = DeletePostUseCase(postService)

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
						DeletePostInput(
							postId = post.id,
							userId = post.userId
						)

					justRun { postService.deletePostByUser(post.id, post.userId) }

					shouldNotThrow<Exception> {
						deletePostUseCase.execute(input)
					}

					verify(exactly = 1) {
						postService.deletePostByUser(post.id, post.userId)
					}
				}
			}

			When("Another user tries to delete") {
				Then("PostUnAuthorizedException is thrown") {
					val postService = mockk<PostService>()
					val deletePostUseCase = DeletePostUseCase(postService)

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

					val otherUserId = 200L
					val input =
						DeletePostInput(
							postId = post.id,
							userId = otherUserId
						)

					every { postService.deletePostByUser(post.id, otherUserId) } throws IllegalArgumentException("Permission denied. You can only delete posts you have authored.")

					val exception =
						shouldThrowExactly<IllegalArgumentException> {
							deletePostUseCase.execute(input)
						}

					exception.message shouldBe "Permission denied. You can only delete posts you have authored."

					verify(exactly = 1) {
						postService.deletePostByUser(post.id, otherUserId)
					}
				}
			}

			When("Try to delete non-existent post") {
				Then("PostNotFoundException is thrown") {
					val postService = mockk<PostService>()
					val deletePostUseCase = DeletePostUseCase(postService)

					val notExistId = 999L
					val input =
						DeletePostInput(
							postId = notExistId,
							userId = 100L
						)

					every { postService.deletePostByUser(notExistId, 100L) } throws PostNotFoundException(notExistId)

					val exception =
						shouldThrowExactly<PostNotFoundException> {
							deletePostUseCase.execute(input)
						}

					exception.message shouldBe "Post not found. postId: $notExistId"

					verify(exactly = 1) {
						postService.deletePostByUser(notExistId, 100L)
					}
				}
			}
		}
	})
