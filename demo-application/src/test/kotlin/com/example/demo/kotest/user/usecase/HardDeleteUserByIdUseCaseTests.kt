package com.example.demo.kotest.user.usecase

import com.example.demo.user.port.UserPort
import com.example.demo.user.port.input.HardDeleteUserByIdInput
import com.example.demo.user.usecase.HardDeleteUserByIdUseCase
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify

class HardDeleteUserByIdUseCaseTests :
	BehaviorSpec({

		val userPort = mockk<UserPort>()
		val hardDeleteUserByIdUseCase = HardDeleteUserByIdUseCase(userPort)

		Given("a valid user ID") {
			val userId = 100L
			val input = HardDeleteUserByIdInput(userId = userId)

			When("execute is called") {
				every { userPort.hardDeleteById(userId) } just Runs

				hardDeleteUserByIdUseCase.execute(input)

				Then("should call userPort.hardDeleteById with correct userId") {
					verify(exactly = 1) {
						userPort.hardDeleteById(userId)
					}
				}
			}

			When("repository throws an exception") {
				clearMocks(userPort)

				val exception = RuntimeException("Database error")
				every { userPort.hardDeleteById(userId) } throws exception

				Then("should propagate the exception") {
					val thrownException =
						runCatching {
							hardDeleteUserByIdUseCase.execute(input)
						}.exceptionOrNull()

					thrownException shouldBe exception

					verify(exactly = 1) {
						userPort.hardDeleteById(userId)
					}
				}
			}
		}

		Given("multiple user IDs") {
			val userIds = listOf(1L, 2L, 3L, 4L, 5L)

			When("deleting multiple users") {
				every { userPort.hardDeleteById(any<Long>()) } just Runs

				userIds.forEach { userId ->
					hardDeleteUserByIdUseCase.execute(
						HardDeleteUserByIdInput(userId = userId)
					)
				}

				Then("should call repository for each user") {
					userIds.forEach { userId ->
						verify(exactly = 1) {
							userPort.hardDeleteById(userId)
						}
					}
				}
			}
		}

		Given("edge case user IDs") {
			When("deleting user with ID 0") {
				val input = HardDeleteUserByIdInput(userId = 0L)
				every { userPort.hardDeleteById(0L) } just Runs

				hardDeleteUserByIdUseCase.execute(input)

				Then("should still call repository") {
					verify(exactly = 1) {
						userPort.hardDeleteById(0L)
					}
				}
			}

			When("deleting user with negative ID") {
				val input = HardDeleteUserByIdInput(userId = -1L)
				every { userPort.hardDeleteById(-1L) } just Runs

				hardDeleteUserByIdUseCase.execute(input)

				Then("should still call repository") {
					verify(exactly = 1) {
						userPort.hardDeleteById(-1L)
					}
				}
			}

			When("deleting user with very large ID") {
				val largeId = Long.MAX_VALUE
				val input = HardDeleteUserByIdInput(userId = largeId)
				every { userPort.hardDeleteById(largeId) } just Runs

				hardDeleteUserByIdUseCase.execute(input)

				Then("should handle large ID correctly") {
					verify(exactly = 1) {
						userPort.hardDeleteById(largeId)
					}
				}
			}
		}
	})
