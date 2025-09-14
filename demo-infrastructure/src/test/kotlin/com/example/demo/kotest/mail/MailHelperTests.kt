package com.example.demo.kotest.mail

import com.example.demo.exception.CustomRuntimeException
import com.example.demo.mail.MailHelper
import com.example.demo.mail.model.MailPayload
import com.example.demo.notification.NotificationService
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.springframework.mail.MailSender
import org.springframework.mail.SimpleMailMessage
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@Tags("kotest-unit-test")
class MailHelperTests :
	BehaviorSpec({
		Given("a valid mail payload") {
			val payload =
				MailPayload.of(
					to = "user@example.com",
					subject = "Test Subject",
					body = "Test Body"
				)

			val mailSender = mockk<MailSender>(relaxed = true)
			val notificationService = mockk<NotificationService>(relaxed = true)
			val mailHelper = MailHelper(mailSender, notificationService)

			When("sending an email") {

				Then("mailSender.send should be called with correct values") {
					mailHelper.sendEmail(payload)

					verify(exactly = 1) {
						mailSender.send(
							withArg<SimpleMailMessage> {
								it.to shouldBe arrayOf("user@example.com")
								it.subject shouldBe "Test Subject"
								it.text shouldBe "Test Body"
							}
						)
					}
				}
			}
		}

		Given("an invalid email address") {
			val payload =
				MailPayload.of(
					to = "invalid-email",
					subject = "Subject",
					body = "Body"
				)

			val mailSender = mockk<MailSender>(relaxed = true)
			val notificationService = mockk<NotificationService>(relaxed = true)
			val mailHelper = MailHelper(mailSender, notificationService)

			When("sending an email") {

				Then("a CustomRuntimeException should be thrown") {
					val exception =
						shouldThrow<CustomRuntimeException> {
							mailHelper.sendEmail(payload)
						}

					exception.message shouldContain "Validation failed: to must be a valid email"
					verify(exactly = 0) { mailSender.send(any<SimpleMailMessage>()) }
				}
			}
		}

		Given("a mail sending failure") {
			val payload =
				MailPayload(
					to = "user@example.com",
					subject = "Test Subject",
					body = "Test Body"
				)

			val mailSender = mockk<MailSender>()
			val notificationService = mockk<NotificationService>(relaxed = true)
			val mailHelper = MailHelper(mailSender, notificationService)

			every { mailSender.send(any<SimpleMailMessage>()) } throws RuntimeException("SMTP error")

			justRun {
				notificationService.sendCriticalAlert(any<String>(), any<List<String>>())
			}

			When("sending an email") {

				Then("notificationService.sendCriticalAlert should be invoked") {
					val exception =
						shouldThrowExactly<CustomRuntimeException> {
							mailHelper.sendEmail(payload)
						}

					exception.message shouldContain "SMTP error"

					verify(exactly = 1) {
						notificationService.sendCriticalAlert(
							"Mail Sending Failed",
							listOf(
								"Mail to: ${payload.to}",
								"Mail Subject: ${payload.subject}",
								"Mail Body: ${payload.body}",
								"Error: SMTP error"
							)
						)
					}
				}
			}
		}

		Given("empty email address") {
			val payload =
				MailPayload.of(
					to = "",
					subject = "Subject",
					body = "Body"
				)

			val mailSender = mockk<MailSender>(relaxed = true)
			val notificationService = mockk<NotificationService>(relaxed = true)
			val mailHelper = MailHelper(mailSender, notificationService)

			When("sending an email") {
				Then("should throw CustomRuntimeException with empty email error") {
					val exception =
						shouldThrow<CustomRuntimeException> {
							mailHelper.sendEmail(payload)
						}

					exception.message shouldContain "to cannot be empty"
					verify(exactly = 0) { mailSender.send(any<SimpleMailMessage>()) }
				}
			}
		}

		Given("blank email address with spaces") {
			val payload =
				MailPayload.of(
					to = "   ",
					subject = "Subject",
					body = "Body"
				)

			val mailSender = mockk<MailSender>(relaxed = true)
			val notificationService = mockk<NotificationService>(relaxed = true)
			val mailHelper = MailHelper(mailSender, notificationService)

			When("sending an email") {
				Then("should throw CustomRuntimeException with empty email error") {
					val exception =
						shouldThrow<CustomRuntimeException> {
							mailHelper.sendEmail(payload)
						}

					exception.message shouldContain "to cannot be empty"
					verify(exactly = 0) { mailSender.send(any<SimpleMailMessage>()) }
				}
			}
		}

		Given("empty subject") {
			val payload =
				MailPayload.of(
					to = "test@example.com",
					subject = "",
					body = "Body"
				)

			val mailSender = mockk<MailSender>(relaxed = true)
			val notificationService = mockk<NotificationService>(relaxed = true)
			val mailHelper = MailHelper(mailSender, notificationService)

			When("sending an email") {
				Then("should throw CustomRuntimeException with empty subject error") {
					val exception =
						shouldThrow<CustomRuntimeException> {
							mailHelper.sendEmail(payload)
						}

					exception.message shouldContain "Subject cannot be empty"
					verify(exactly = 0) { mailSender.send(any<SimpleMailMessage>()) }
				}
			}
		}

		Given("empty body") {
			val payload =
				MailPayload.of(
					to = "test@example.com",
					subject = "Subject",
					body = ""
				)

			val mailSender = mockk<MailSender>(relaxed = true)
			val notificationService = mockk<NotificationService>(relaxed = true)
			val mailHelper = MailHelper(mailSender, notificationService)

			When("sending an email") {
				Then("should throw CustomRuntimeException with empty body error") {
					val exception =
						shouldThrow<CustomRuntimeException> {
							mailHelper.sendEmail(payload)
						}

					exception.message shouldContain "Body cannot be empty"
					verify(exactly = 0) { mailSender.send(any<SimpleMailMessage>()) }
				}
			}
		}

		Given("multiple validation errors") {
			val payload =
				MailPayload.of(
					to = "invalid-email",
					subject = "",
					body = ""
				)

			val mailSender = mockk<MailSender>(relaxed = true)
			val notificationService = mockk<NotificationService>(relaxed = true)
			val mailHelper = MailHelper(mailSender, notificationService)

			When("sending an email") {
				Then("should throw CustomRuntimeException with all validation errors") {
					val exception =
						shouldThrow<CustomRuntimeException> {
							mailHelper.sendEmail(payload)
						}

					exception.message shouldContain "to must be a valid email"
					exception.message shouldContain "Subject cannot be empty"
					exception.message shouldContain "Body cannot be empty"
					verify(exactly = 0) { mailSender.send(any<SimpleMailMessage>()) }
				}
			}
		}

		Given("various valid email formats") {
			val validEmails =
				listOf(
					"user@example.com",
					"user.name@example.com",
					"user+tag@example.co.uk",
					"user_name@example-domain.com",
					"123@example.com"
				)

			val mailSender = mockk<MailSender>(relaxed = true)
			val notificationService = mockk<NotificationService>(relaxed = true)
			val mailHelper = MailHelper(mailSender, notificationService)

			When("sending emails with valid formats") {
				Then("all emails should be sent successfully") {
					validEmails.forEach { email ->
						val payload =
							MailPayload.of(
								to = email,
								subject = "Test",
								body = "Test"
							)

						mailHelper.sendEmail(payload)
					}

					verify(exactly = validEmails.size) {
						mailSender.send(any<SimpleMailMessage>())
					}
				}
			}
		}

		Given("special characters in subject and body") {
			val payload =
				MailPayload.of(
					to = "test@example.com",
					subject = "Special chars: !@#$%^&*()_+-=[]{}|;':\",./<>?",
					body = "Unicode: emoji 😀🎉\nNew line\tTab"
				)

			val mailSender = mockk<MailSender>(relaxed = true)
			val notificationService = mockk<NotificationService>(relaxed = true)
			val mailHelper = MailHelper(mailSender, notificationService)

			When("sending an email with special characters") {
				Then("should handle special characters correctly") {
					val messageSlot = slot<SimpleMailMessage>()
					every { mailSender.send(capture(messageSlot)) } returns Unit

					mailHelper.sendEmail(payload)

					val capturedMessage = messageSlot.captured
					capturedMessage.subject shouldBe "Special chars: !@#$%^&*()_+-=[]{}|;':\",./<>?"
					capturedMessage.text shouldBe "Unicode: emoji 😀🎉\nNew line\tTab"
				}
			}
		}

		Given("network timeout during mail sending") {
			val payload =
				MailPayload.of(
					to = "test@example.com",
					subject = "Test",
					body = "Test"
				)

			val mailSender = mockk<MailSender>()
			val notificationService = mockk<NotificationService>(relaxed = true)
			val mailHelper = MailHelper(mailSender, notificationService)

			every { mailSender.send(any<SimpleMailMessage>()) } throws
				RuntimeException("Connection timeout")

			When("mail sending times out") {
				Then("should handle timeout and notify via notification service") {
					val exception =
						shouldThrowExactly<CustomRuntimeException> {
							mailHelper.sendEmail(payload)
						}

					exception.message shouldContain "Connection timeout"

					verify(exactly = 1) {
						notificationService.sendCriticalAlert(
							"Mail Sending Failed",
							listOf(
								"Mail to: test@example.com",
								"Mail Subject: Test",
								"Mail Body: Test",
								"Error: Connection timeout"
							)
						)
					}
				}
			}
		}
	})
