package com.example.demo.kotest.user.usecase

import com.example.demo.user.port.input.DeleteUserInput
import com.example.demo.user.service.UserDeletionService
import com.example.demo.user.usecase.DeleteUserUseCase
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

@ActiveProfiles("test")
@Tags("kotest-unit-test")
class DeleteUserUseCaseTests :
	BehaviorSpec({

		Given("Delete user (Soft Delete)") {

			When("Soft delete existing user") {
				Then("Should delete user successfully") {
					val userDeletionService = mockk<UserDeletionService>()
					val deleteUserUseCase = DeleteUserUseCase(userDeletionService)

					val input = DeleteUserInput(userId = 1L)

					justRun { userDeletionService.deleteUserWithRelatedData(input.userId) }

					shouldNotThrow<Exception> {
						deleteUserUseCase.execute(input)
					}

					verify(exactly = 1) { userDeletionService.deleteUserWithRelatedData(input.userId) }
				}
			}

			When("Delete non-existent user") {
				Then("Should throw exception") {
					val userDeletionService = mockk<UserDeletionService>()
					val deleteUserUseCase = DeleteUserUseCase(userDeletionService)

					val input = DeleteUserInput(userId = 999L)

					every { userDeletionService.deleteUserWithRelatedData(input.userId) } throws
						IllegalArgumentException("User not found: 999")

					val exception =
						shouldThrowExactly<IllegalArgumentException> {
							deleteUserUseCase.execute(input)
						}

					exception.message shouldBe "User not found: 999"

					verify(exactly = 1) { userDeletionService.deleteUserWithRelatedData(input.userId) }
				}
			}

			When("Delete already soft-deleted user") {
				Then("Should handle gracefully") {
					val userDeletionService = mockk<UserDeletionService>()
					val deleteUserUseCase = DeleteUserUseCase(userDeletionService)

					val input = DeleteUserInput(userId = 2L)

					justRun { userDeletionService.deleteUserWithRelatedData(input.userId) }

					shouldNotThrow<Exception> {
						deleteUserUseCase.execute(input)
					}

					verify(exactly = 1) { userDeletionService.deleteUserWithRelatedData(input.userId) }
				}
			}
		}
	})
