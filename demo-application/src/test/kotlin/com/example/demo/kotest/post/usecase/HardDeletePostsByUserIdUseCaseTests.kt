package com.example.demo.kotest.post.usecase

import com.example.demo.post.port.PostPort
import com.example.demo.post.port.input.HardDeletePostsByUserIdInput
import com.example.demo.post.usecase.HardDeletePostsByUserIdUseCase
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@Tags("kotest-unit-test")
class HardDeletePostsByUserIdUseCaseTests :
	BehaviorSpec({
		val postPort = mockk<PostPort>()
		val hardDeletePostsByUserIdUseCase = HardDeletePostsByUserIdUseCase(postPort)

		Given("Hard delete all posts by user ID") {

			When("Hard delete posts for user") {
				Then("Hard delete service method is called") {
					val userId = 100L
					val input = HardDeletePostsByUserIdInput(userId = userId)

					justRun { postPort.hardDeleteByUserId(userId) }

					shouldNotThrow<Exception> {
						hardDeletePostsByUserIdUseCase.execute(input)
					}

					verify(exactly = 1) {
						postPort.hardDeleteByUserId(userId)
					}
				}
			}

			When("Hard delete for GDPR compliance") {
				Then("Posts are permanently deleted") {
					val userId = 200L
					val input = HardDeletePostsByUserIdInput(userId = userId)

					justRun { postPort.hardDeleteByUserId(userId) }

					shouldNotThrow<Exception> {
						hardDeletePostsByUserIdUseCase.execute(input)
					}

					verify(exactly = 1) {
						postPort.hardDeleteByUserId(userId)
					}
				}
			}
		}
	})
