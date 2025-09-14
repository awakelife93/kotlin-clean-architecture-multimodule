package com.example.demo.kotest.user.service

import com.example.demo.post.port.input.HardDeletePostsByUserIdInput
import com.example.demo.post.usecase.HardDeletePostsByUserIdUseCase
import com.example.demo.user.constant.UserRole
import com.example.demo.user.model.UserDeleteItem
import com.example.demo.user.port.input.HardDeleteUserByIdInput
import com.example.demo.user.service.UserDeleteEventService
import com.example.demo.user.usecase.HardDeleteUserByIdUseCase
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime

@ActiveProfiles("test")
@Tags("kotest-unit-test")
class UserDeleteEventServiceTests :
	BehaviorSpec({

		val hardDeletePostsByUserIdUseCase = mockk<HardDeletePostsByUserIdUseCase>()
		val hardDeleteUserByIdUseCase = mockk<HardDeleteUserByIdUseCase>()

		val userDeleteEventService =
			UserDeleteEventService(
				hardDeletePostsByUserIdUseCase,
				hardDeleteUserByIdUseCase
			)

		Given("a UserDeleteItem with valid data") {
			val userDeleteItem =
				UserDeleteItem(
					id = 100L,
					email = "test@example.com",
					name = "Test User",
					role = UserRole.USER.name,
					deletedDt = LocalDateTime.now().minusYears(1)
				)

			When("handle is called") {
				every { hardDeletePostsByUserIdUseCase.execute(any<HardDeletePostsByUserIdInput>()) } returns Unit
				every { hardDeleteUserByIdUseCase.execute(any<HardDeleteUserByIdInput>()) } returns Unit

				userDeleteEventService.handle(userDeleteItem)

				Then("should execute HardDeletePostsByUserIdUseCase with correct input") {
					verify(exactly = 1) {
						hardDeletePostsByUserIdUseCase.execute(
							HardDeletePostsByUserIdInput(userId = 100L)
						)
					}
				}

				Then("should execute HardDeleteUserByIdUseCase with correct input") {
					verify(exactly = 1) {
						hardDeleteUserByIdUseCase.execute(
							HardDeleteUserByIdInput(userId = 100L)
						)
					}
				}

				Then("should call use cases in correct order") {
					verifyOrder {
						hardDeletePostsByUserIdUseCase.execute(any<HardDeletePostsByUserIdInput>())
						hardDeleteUserByIdUseCase.execute(any<HardDeleteUserByIdInput>())
					}
				}
			}

			When("HardDeletePostsByUserIdUseCase throws an exception") {
				clearMocks(hardDeleteUserByIdUseCase, hardDeletePostsByUserIdUseCase)

				val exception = RuntimeException("Database error")
				every { hardDeletePostsByUserIdUseCase.execute(any<HardDeletePostsByUserIdInput>()) } throws exception

				Then("should propagate the exception") {
					val thrownException =
						runCatching {
							userDeleteEventService.handle(userDeleteItem)
						}.exceptionOrNull()

					thrownException shouldBe exception

					verify(exactly = 1) {
						hardDeletePostsByUserIdUseCase.execute(any<HardDeletePostsByUserIdInput>())
					}

					verify(exactly = 0) {
						hardDeleteUserByIdUseCase.execute(any<HardDeleteUserByIdInput>())
					}
				}
			}

			When("HardDeleteUserByIdUseCase throws an exception") {
				clearMocks(hardDeleteUserByIdUseCase, hardDeletePostsByUserIdUseCase)

				val exception = RuntimeException("User deletion failed")
				every { hardDeletePostsByUserIdUseCase.execute(any<HardDeletePostsByUserIdInput>()) } returns Unit
				every { hardDeleteUserByIdUseCase.execute(any<HardDeleteUserByIdInput>()) } throws exception

				Then("should propagate the exception after deleting posts") {
					val thrownException =
						runCatching {
							userDeleteEventService.handle(userDeleteItem)
						}.exceptionOrNull()

					thrownException shouldBe exception

					verify(exactly = 1) {
						hardDeletePostsByUserIdUseCase.execute(any<HardDeletePostsByUserIdInput>())
						hardDeleteUserByIdUseCase.execute(any<HardDeleteUserByIdInput>())
					}
				}
			}
		}

		Given("multiple UserDeleteItems") {
			val userDeleteItems =
				listOf(
					UserDeleteItem(
						id = 1L,
						email = "user1@example.com",
						name = "User 1",
						role = UserRole.USER.name,
						deletedDt = LocalDateTime.now().minusYears(1)
					),
					UserDeleteItem(
						id = 2L,
						email = "user2@example.com",
						name = "User 2",
						role = UserRole.ADMIN.name,
						deletedDt = LocalDateTime.now().minusYears(2)
					),
					UserDeleteItem(
						id = 3L,
						email = "user3@example.com",
						name = "User 3",
						role = UserRole.USER.name,
						deletedDt = LocalDateTime.now().minusMonths(13)
					)
				)

			When("processing multiple users") {
				every { hardDeletePostsByUserIdUseCase.execute(any<HardDeletePostsByUserIdInput>()) } returns Unit
				every { hardDeleteUserByIdUseCase.execute(any<HardDeleteUserByIdInput>()) } returns Unit

				userDeleteItems.forEach { userDeleteEventService.handle(it) }

				Then("should process each user correctly") {
					userDeleteItems.forEach { item ->
						verify(exactly = 1) {
							hardDeletePostsByUserIdUseCase.execute(
								HardDeletePostsByUserIdInput(userId = item.id)
							)
							hardDeleteUserByIdUseCase.execute(
								HardDeleteUserByIdInput(userId = item.id)
							)
						}
					}
				}
			}
		}
	})
