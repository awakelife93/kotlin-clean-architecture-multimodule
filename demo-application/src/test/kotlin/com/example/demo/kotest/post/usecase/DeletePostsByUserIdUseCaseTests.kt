package com.example.demo.kotest.post.usecase

import com.example.demo.post.port.input.DeletePostsByUserIdInput
import com.example.demo.post.service.PostService
import com.example.demo.post.usecase.DeletePostsByUserIdUseCase
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@Tags("kotest-unit-test")
class DeletePostsByUserIdUseCaseTests :
	BehaviorSpec({
		val postService = mockk<PostService>()
		val deletePostsByUserIdUseCase = DeletePostsByUserIdUseCase(postService)

		Given("Delete all posts by user ID") {

			When("Delete posts for user") {
				Then("Service method is called") {
					val userId = 100L
					val input = DeletePostsByUserIdInput(userId = userId)

					justRun { postService.deletePostsByUserId(userId) }

					shouldNotThrow<Exception> {
						deletePostsByUserIdUseCase.execute(input)
					}

					verify(exactly = 1) {
						postService.deletePostsByUserId(userId)
					}
				}
			}

			When("Delete posts for user with no posts") {
				Then("Service method is called even for user without posts") {
					val userId = 200L
					val input = DeletePostsByUserIdInput(userId = userId)

					justRun { postService.deletePostsByUserId(userId) }

					shouldNotThrow<Exception> {
						deletePostsByUserIdUseCase.execute(input)
					}

					verify(exactly = 1) {
						postService.deletePostsByUserId(userId)
					}
				}
			}
		}
	})
