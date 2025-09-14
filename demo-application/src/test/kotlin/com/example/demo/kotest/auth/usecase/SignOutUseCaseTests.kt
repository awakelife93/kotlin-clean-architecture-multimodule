package com.example.demo.kotest.auth.usecase

import com.example.demo.auth.port.input.SignOutInput
import com.example.demo.auth.service.AuthService
import com.example.demo.auth.usecase.SignOutUseCase
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
class SignOutUseCaseTests :
	BehaviorSpec({

		Given("Sign out") {

			When("User signs out") {
				Then("Should successfully sign out") {
					val authService = mockk<AuthService>()
					val signOutUseCase = SignOutUseCase(authService)

					val input = SignOutInput(userId = 1L)

					justRun { authService.signOut(input.userId) }

					shouldNotThrow<Exception> {
						signOutUseCase.execute(input)
					}

					verify(exactly = 1) { authService.signOut(input.userId) }
				}
			}

			When("Multiple users sign out simultaneously") {
				Then("Each user should sign out independently") {
					val authService = mockk<AuthService>()
					val signOutUseCase = SignOutUseCase(authService)

					val input1 = SignOutInput(userId = 1L)
					val input2 = SignOutInput(userId = 2L)

					justRun { authService.signOut(any<Long>()) }

					shouldNotThrow<Exception> {
						signOutUseCase.execute(input1)
						signOutUseCase.execute(input2)
					}

					verify(exactly = 1) { authService.signOut(1L) }
					verify(exactly = 1) { authService.signOut(2L) }
				}
			}

			When("Service throws exception during sign out") {
				Then("Should propagate the exception") {
					val authService = mockk<AuthService>()
					val signOutUseCase = SignOutUseCase(authService)

					val input = SignOutInput(userId = 999L)

					every { authService.signOut(input.userId) } throws RuntimeException("Sign out failed")

					val exception =
						shouldThrowExactly<RuntimeException> {
							signOutUseCase.execute(input)
						}

					exception.message shouldBe "Sign out failed"
					verify(exactly = 1) { authService.signOut(input.userId) }
				}
			}
		}
	})
