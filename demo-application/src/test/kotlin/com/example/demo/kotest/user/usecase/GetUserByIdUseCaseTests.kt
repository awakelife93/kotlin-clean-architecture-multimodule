package com.example.demo.kotest.user.usecase

import com.example.demo.user.constant.UserRole
import com.example.demo.user.exception.UserNotFoundException
import com.example.demo.user.model.User
import com.example.demo.user.port.input.GetUserByIdInput
import com.example.demo.user.service.UserService
import com.example.demo.user.usecase.GetUserByIdUseCase
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
class GetUserByIdUseCaseTests :
	BehaviorSpec({

		Given("Get user by ID") {

			When("User exists") {
				Then("Should return user successfully") {
					val userService = mockk<UserService>()
					val getUserByIdUseCase = GetUserByIdUseCase(userService)

					val input = GetUserByIdInput(userId = 1L)

					val user =
						User(
							id = 1L,
							email = "test@example.com",
							password = "encoded_password",
							name = "Test User",
							role = UserRole.USER,
							createdDt = LocalDateTime.now(),
							updatedDt = LocalDateTime.now()
						)

					every { userService.findOneByIdOrThrow(input.userId) } returns user

					val result = getUserByIdUseCase.execute(input)

					result.shouldNotBeNull()
					result.id shouldBe 1L
					result.email shouldBe "test@example.com"
					result.name shouldBe "Test User"
					result.role shouldBe UserRole.USER

					verify(exactly = 1) { userService.findOneByIdOrThrow(input.userId) }
				}
			}

			When("User does not exist") {
				Then("Should throw UserNotFoundException") {
					val userService = mockk<UserService>()
					val getUserByIdUseCase = GetUserByIdUseCase(userService)

					val input = GetUserByIdInput(userId = 999L)

					every { userService.findOneByIdOrThrow(input.userId) } throws UserNotFoundException(input.userId)

					shouldThrowExactly<UserNotFoundException> {
						getUserByIdUseCase.execute(input)
					}

					verify(exactly = 1) { userService.findOneByIdOrThrow(input.userId) }
				}
			}

			When("Get deleted user") {
				Then("Should return deleted user data") {
					val userService = mockk<UserService>()
					val getUserByIdUseCase = GetUserByIdUseCase(userService)

					val input = GetUserByIdInput(userId = 2L)

					val deletedUser =
						User(
							id = 2L,
							email = "deleted@example.com",
							password = "encoded_password",
							name = "Deleted User",
							role = UserRole.USER,
							createdDt = LocalDateTime.now().minusDays(30),
							updatedDt = LocalDateTime.now().minusDays(1),
							deletedDt = LocalDateTime.now()
						)

					every { userService.findOneByIdOrThrow(input.userId) } returns deletedUser

					val result = getUserByIdUseCase.execute(input)

					result.shouldNotBeNull()
					result.id shouldBe 2L
					result.email shouldBe "deleted@example.com"
				}
			}
		}
	})
