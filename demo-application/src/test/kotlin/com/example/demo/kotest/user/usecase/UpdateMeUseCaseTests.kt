package com.example.demo.kotest.user.usecase

import com.example.demo.persistence.auth.provider.TokenProvider
import com.example.demo.user.constant.UserRole
import com.example.demo.user.exception.UserNotFoundException
import com.example.demo.user.model.User
import com.example.demo.user.port.input.UpdateMeInput
import com.example.demo.user.service.UserService
import com.example.demo.user.usecase.UpdateMeUseCase
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
class UpdateMeUseCaseTests :
	BehaviorSpec({

		Given("Update me (current user)") {

			When("Update current user's information") {
				Then("Should update and return new token") {
					val userService = mockk<UserService>()
					val tokenProvider = mockk<TokenProvider>()
					val updateMeUseCase = UpdateMeUseCase(userService, tokenProvider)

					val input =
						UpdateMeInput(
							userId = 1L,
							name = "Updated Me"
						)

					val updatedUser =
						User(
							id = 1L,
							email = "updated@example.com",
							password = "encoded_password",
							name = "Updated Me",
							role = UserRole.USER,
							createdDt = LocalDateTime.now().minusDays(30),
							updatedDt = LocalDateTime.now()
						)

					val newAccessToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.updated.signature"

					every { userService.updateUserInfo(input.userId, input.name) } returns updatedUser
					every { tokenProvider.createFullTokens(updatedUser) } returns newAccessToken

					val result = updateMeUseCase.execute(input)

					result.shouldNotBeNull()
					result.id shouldBe 1L
					result.name shouldBe "Updated Me"
					result.email shouldBe "updated@example.com"
					result.accessToken shouldBe newAccessToken

					verify(exactly = 1) {
						userService.updateUserInfo(input.userId, input.name)
						tokenProvider.createFullTokens(updatedUser)
					}
				}
			}

			When("Update non-existent user") {
				Then("Should throw UserNotFoundException") {
					val userService = mockk<UserService>()
					val tokenProvider = mockk<TokenProvider>()
					val updateMeUseCase = UpdateMeUseCase(userService, tokenProvider)

					val input =
						UpdateMeInput(
							userId = 999L,
							name = "Ghost User"
						)

					every { userService.updateUserInfo(input.userId, input.name) } throws
						UserNotFoundException(input.userId)

					val exception =
						shouldThrowExactly<UserNotFoundException> {
							updateMeUseCase.execute(input)
						}

					exception.message shouldBe "User Not Found userId = 999"

					verify(exactly = 1) { userService.updateUserInfo(input.userId, input.name) }
					verify(exactly = 0) { tokenProvider.createFullTokens(any<User>()) }
				}
			}

			When("Update only name without changing email") {
				Then("Should update name and keep existing email") {
					val userService = mockk<UserService>()
					val tokenProvider = mockk<TokenProvider>()
					val updateMeUseCase = UpdateMeUseCase(userService, tokenProvider)

					val input =
						UpdateMeInput(
							userId = 2L,
							name = "Name Only Update"
						)

					val updatedUser =
						User(
							id = 2L,
							email = "existing@example.com",
							password = "existing_encoded_password",
							name = "Name Only Update",
							role = UserRole.USER,
							createdDt = LocalDateTime.now().minusDays(10),
							updatedDt = LocalDateTime.now()
						)

					val newAccessToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.nameonly.signature"

					every { userService.updateUserInfo(input.userId, input.name) } returns updatedUser
					every { tokenProvider.createFullTokens(updatedUser) } returns newAccessToken

					val result = updateMeUseCase.execute(input)

					result.shouldNotBeNull()
					result.name shouldBe "Name Only Update"
					result.email shouldBe "existing@example.com"
					result.accessToken shouldBe newAccessToken

					verify(exactly = 1) {
						userService.updateUserInfo(input.userId, input.name)
						tokenProvider.createFullTokens(updatedUser)
					}
				}
			}
		}
	})
