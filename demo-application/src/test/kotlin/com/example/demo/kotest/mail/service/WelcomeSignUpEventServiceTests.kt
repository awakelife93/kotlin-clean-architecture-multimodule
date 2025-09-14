package com.example.demo.kotest.mail.service

import com.example.demo.mail.MailHelper
import com.example.demo.mail.model.MailPayload
import com.example.demo.mail.service.WelcomeSignUpEventService
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@Tags("kotest-unit-test")
class WelcomeSignUpEventServiceTests :
	BehaviorSpec({

		val mailHelper = mockk<MailHelper>()
		val welcomeSignUpEventService = WelcomeSignUpEventService(mailHelper)

		Given("a welcome mail payload") {
			val mailPayload =
				MailPayload(
					to = "newuser@example.com",
					subject = "Welcome New User!",
					body = "Welcome to our service! We're glad to have you aboard."
				)

			When("handling the welcome event successfully") {
				every { mailHelper.sendEmail(mailPayload) } just Runs

				Then("should send email through MailHelper") {
					welcomeSignUpEventService.handle(mailPayload)

					verify(exactly = 1) {
						mailHelper.sendEmail(mailPayload)
					}
				}
			}

			When("mail sending fails") {
				clearMocks(mailHelper)
				val exception = RuntimeException("SMTP connection failed")
				every { mailHelper.sendEmail(mailPayload) } throws exception

				Then("should propagate the exception") {
					val thrownException =
						shouldThrow<RuntimeException> {
							welcomeSignUpEventService.handle(mailPayload)
						}

					thrownException.message shouldBe "SMTP connection failed"

					verify(exactly = 1) {
						mailHelper.sendEmail(mailPayload)
					}
				}
			}
		}

		Given("multiple mail payloads") {
			val payloads =
				listOf(
					MailPayload("user1@example.com", "Welcome User 1!", "Welcome message 1"),
					MailPayload("user2@example.com", "Welcome User 2!", "Welcome message 2"),
					MailPayload("user3@example.com", "Welcome User 3!", "Welcome message 3")
				)

			When("processing multiple welcome emails") {
				Then("should send all emails") {
					clearMocks(mailHelper)
					every { mailHelper.sendEmail(any<MailPayload>()) } just Runs

					payloads.forEach { payload ->
						welcomeSignUpEventService.handle(payload)
					}

					payloads.forEach { payload ->
						verify(exactly = 1) {
							mailHelper.sendEmail(payload)
						}
					}

					verify(exactly = 3) {
						mailHelper.sendEmail(any<MailPayload>())
					}
				}
			}

			When("one email fails in batch processing") {
				Then("should handle failures independently") {
					clearMocks(mailHelper)

					every {
						mailHelper.sendEmail(
							match { it.to == "user1@example.com" }
						)
					} just Runs

					every {
						mailHelper.sendEmail(
							match { it.to == "user2@example.com" }
						)
					} throws RuntimeException("Failed to send to user2")

					every {
						mailHelper.sendEmail(
							match { it.to == "user3@example.com" }
						)
					} just Runs

					shouldNotThrow<Exception> {
						welcomeSignUpEventService.handle(payloads[0])
					}

					shouldThrow<RuntimeException> {
						welcomeSignUpEventService.handle(payloads[1])
					}

					shouldNotThrow<Exception> {
						welcomeSignUpEventService.handle(payloads[2])
					}

					verify(exactly = 1) {
						mailHelper.sendEmail(payloads[0])
						mailHelper.sendEmail(payloads[1])
						mailHelper.sendEmail(payloads[2])
					}
				}
			}
		}

		Given("edge case mail payloads") {
			When("handling empty email fields") {
				val emptyPayload = MailPayload("", "", "")

				every { mailHelper.sendEmail(emptyPayload) } just Runs

				Then("should still attempt to send") {
					welcomeSignUpEventService.handle(emptyPayload)

					verify(exactly = 1) {
						mailHelper.sendEmail(emptyPayload)
					}
				}
			}
		}

		Given("error scenarios") {
			When("MailHelper throws specific exceptions") {
				val payload =
					MailPayload(
						to = "test@example.com",
						subject = "Test",
						body = "Test"
					)

				val exceptions =
					listOf(
						IllegalArgumentException("Invalid email format"),
						IllegalStateException("Mail service not configured"),
						RuntimeException("Connection timeout")
					)

				Then("should propagate all exception types") {
					exceptions.forEach { exception ->
						clearMocks(mailHelper)
						every { mailHelper.sendEmail(payload) } throws exception

						val thrownException =
							shouldThrow<Exception> {
								welcomeSignUpEventService.handle(payload)
							}

						thrownException.message shouldBe exception.message
						thrownException::class shouldBe exception::class

						verify(exactly = 1) {
							mailHelper.sendEmail(payload)
						}
					}
				}
			}
		}
	})
