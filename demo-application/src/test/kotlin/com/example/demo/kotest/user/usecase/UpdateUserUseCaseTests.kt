package com.example.demo.kotest.user.usecase

import com.example.demo.user.constant.UserRole
import com.example.demo.user.exception.UserNotFoundException
import com.example.demo.user.model.User
import com.example.demo.user.port.input.UpdateUserInput
import com.example.demo.user.service.UserService
import com.example.demo.user.usecase.UpdateUserUseCase
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
class UpdateUserUseCaseTests :
	BehaviorSpec({

		Given("Update user") {

			When("Update existing user") {
				Then("Should update user successfully") {
					val userService = mockk<UserService>()
					val updateUserUseCase = UpdateUserUseCase(userService)

					val input =
						UpdateUserInput(
							userId = 1L,
							name = "Updated Name",
							role = "ADMIN"
						)

					val updatedUser =
						User(
							id = 1L,
							email = "updated@example.com",
							password = "encoded_password",
							name = "Updated Name",
							role = UserRole.USER,
							createdDt = LocalDateTime.now().minusDays(30),
							updatedDt = LocalDateTime.now()
						)

					every { userService.updateUserInfo(input.userId, input.name) } returns updatedUser

					val result = updateUserUseCase.execute(input)

					result.shouldNotBeNull()
					result.id shouldBe 1L
					result.name shouldBe "Updated Name"
					result.email shouldBe "updated@example.com"
					result.role shouldBe UserRole.USER

					verify(exactly = 1) { userService.updateUserInfo(input.userId, input.name) }
				}
			}

			When("Update non-existent user") {
				Then("Should throw UserNotFoundException") {
					val userService = mockk<UserService>()
					val updateUserUseCase = UpdateUserUseCase(userService)

					val input =
						UpdateUserInput(
							userId = 999L,
							name = "Updated Name",
							role = "USER"
						)

					every { userService.updateUserInfo(input.userId, input.name) } throws
						UserNotFoundException(input.userId)

					val exception =
						shouldThrowExactly<UserNotFoundException> {
							updateUserUseCase.execute(input)
						}

					exception.message shouldBe "User Not Found userId = 999"
					verify(exactly = 1) { userService.updateUserInfo(input.userId, input.name) }
				}
			}

			When("Update only name") {
				Then("Should update name keeping other fields") {
					val userService = mockk<UserService>()
					val updateUserUseCase = UpdateUserUseCase(userService)

					val input =
						UpdateUserInput(
							userId = 2L,
							name = "New Name Only"
						)

					val updatedUser =
						User(
							id = 2L,
							email = "user@example.com",
							password = "encoded_password",
							name = "New Name Only",
							role = UserRole.USER,
							createdDt = LocalDateTime.now().minusDays(10),
							updatedDt = LocalDateTime.now()
						)

					every { userService.updateUserInfo(input.userId, input.name) } returns updatedUser

					val result = updateUserUseCase.execute(input)

					result.shouldNotBeNull()
					result.name shouldBe "New Name Only"
					result.role shouldBe UserRole.USER
					result.email shouldBe "user@example.com"

					verify(exactly = 1) { userService.updateUserInfo(input.userId, input.name) }
				}
			}
		}
	})
