package com.example.demo.kotest.user.usecase

import com.example.demo.user.constant.UserRole
import com.example.demo.user.exception.UserNotFoundException
import com.example.demo.user.model.User
import com.example.demo.user.port.input.GetUserByEmailInput
import com.example.demo.user.service.UserService
import com.example.demo.user.usecase.GetUserByEmailUseCase
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
class GetUserByEmailUseCaseTests :
	BehaviorSpec({

		Given("Get user by email") {

			When("User with email exists") {
				Then("Should return user successfully") {
					val userService = mockk<UserService>()
					val getUserByEmailUseCase = GetUserByEmailUseCase(userService)

					val input = GetUserByEmailInput(email = "test@example.com")

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

					every { userService.findOneByEmailOrThrow(input.email) } returns user

					val result = getUserByEmailUseCase.execute(input)

					result.shouldNotBeNull()
					result.id shouldBe 1L
					result.email shouldBe "test@example.com"
					result.name shouldBe "Test User"
					result.role shouldBe UserRole.USER

					verify(exactly = 1) { userService.findOneByEmailOrThrow(input.email) }
				}
			}

			When("User with email does not exist") {
				Then("Should throw UserNotFoundException") {
					val userService = mockk<UserService>()
					val getUserByEmailUseCase = GetUserByEmailUseCase(userService)

					val input = GetUserByEmailInput(email = "notfound@example.com")

					every { userService.findOneByEmailOrThrow(input.email) } throws UserNotFoundException(input.email)

					val exception =
						shouldThrowExactly<UserNotFoundException> {
							getUserByEmailUseCase.execute(input)
						}

					exception.message shouldBe "User Not Found email = notfound@example.com"

					verify(exactly = 1) { userService.findOneByEmailOrThrow(input.email) }
				}
			}
		}
	})
