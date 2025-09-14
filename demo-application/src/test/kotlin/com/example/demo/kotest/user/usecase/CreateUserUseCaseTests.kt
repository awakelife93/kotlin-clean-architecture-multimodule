package com.example.demo.kotest.user.usecase

import com.example.demo.user.constant.UserRole
import com.example.demo.user.exception.UserAlreadyExistsException
import com.example.demo.user.port.input.CreateUserInput
import com.example.demo.user.port.output.UserOutput
import com.example.demo.user.service.UserService
import com.example.demo.user.usecase.CreateUserUseCase
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
class CreateUserUseCaseTests :
	BehaviorSpec({

		Given("Create user") {

			When("Valid user data provided") {
				Then("Should create user successfully") {
					val userService = mockk<UserService>()
					val createUserUseCase = CreateUserUseCase(userService)

					val input =
						CreateUserInput(
							email = "newuser@example.com",
							password = "password123",
							name = "New User"
						)

					val expectedOutput =
						UserOutput.AuthenticatedUserOutput(
							id = 1L,
							email = input.email,
							name = input.name,
							role = UserRole.USER,
							accessToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.signature",
							createdDt = LocalDateTime.now(),
							updatedDt = LocalDateTime.now()
						)

					every { userService.registerNewUser(input) } returns expectedOutput

					val result = createUserUseCase.execute(input)

					result.shouldNotBeNull()
					result.id shouldBe expectedOutput.id
					result.email shouldBe expectedOutput.email
					result.name shouldBe expectedOutput.name
					result.role shouldBe expectedOutput.role
					result.accessToken shouldBe expectedOutput.accessToken

					verify(exactly = 1) { userService.registerNewUser(input) }
				}
			}

			When("Email already exists") {
				Then("Should throw UserAlreadyExistsException") {
					val userService = mockk<UserService>()
					val createUserUseCase = CreateUserUseCase(userService)

					val input =
						CreateUserInput(
							email = "existing@example.com",
							password = "password123",
							name = "Existing User"
						)

					every { userService.registerNewUser(input) } throws UserAlreadyExistsException(input.email)

					val exception =
						shouldThrowExactly<UserAlreadyExistsException> {
							createUserUseCase.execute(input)
						}

					exception.message shouldBe "Already User Exist email = existing@example.com"
					verify(exactly = 1) { userService.registerNewUser(input) }
				}
			}

			When("Create multiple users") {
				Then("Each should have unique token") {
					val userService = mockk<UserService>()
					val createUserUseCase = CreateUserUseCase(userService)

					val input =
						CreateUserInput(
							email = "user2@example.com",
							password = "pass456",
							name = "User Two"
						)

					val expectedOutput =
						UserOutput.AuthenticatedUserOutput(
							id = 2L,
							email = input.email,
							name = input.name,
							role = UserRole.USER,
							accessToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.user2.signature",
							createdDt = LocalDateTime.now(),
							updatedDt = LocalDateTime.now()
						)

					every { userService.registerNewUser(input) } returns expectedOutput

					val result = createUserUseCase.execute(input)

					result.shouldNotBeNull()
					result.accessToken shouldBe expectedOutput.accessToken
					verify(exactly = 1) { userService.registerNewUser(input) }
				}
			}
		}
	})
