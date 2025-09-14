package com.example.demo.kotest.kafka.adapter

import com.example.demo.kafka.adapter.WelcomeSignUpKafkaAdapter
import com.example.demo.mail.event.WelcomeSignUpEventHandler
import com.example.demo.mail.model.MailPayload
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.Runs
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@Tags("kotest-unit-test")
class WelcomeSignUpKafkaAdapterTests :
	BehaviorSpec({

		val welcomeSignUpEventHandler = mockk<WelcomeSignUpEventHandler>()
		val welcomeSignUpKafkaAdapter = WelcomeSignUpKafkaAdapter(welcomeSignUpEventHandler)

		Given("a MailPayload message from Kafka") {
			val mailPayload =
				MailPayload(
					to = "newuser@example.com",
					subject = "Welcome New User!",
					body = "Welcome to our service! We're glad to have you aboard."
				)

			When("consuming the message successfully") {
				every { welcomeSignUpEventHandler.handle(mailPayload) } just Runs

				welcomeSignUpKafkaAdapter.consume(mailPayload)

				Then("should delegate to WelcomeSignUpEventHandler") {
					verify(exactly = 1) {
						welcomeSignUpEventHandler.handle(mailPayload)
					}
				}
			}
		}

		Given("multiple MailPayload messages") {
			clearMocks(welcomeSignUpEventHandler)

			val mailPayloads =
				listOf(
					MailPayload(
						to = "user1@example.com",
						subject = "Welcome User 1!",
						body = "Welcome message for user 1"
					),
					MailPayload(
						to = "user2@example.com",
						subject = "Welcome User 2!",
						body = "Welcome message for user 2"
					),
					MailPayload(
						to = "user3@example.com",
						subject = "Welcome User 3!",
						body = "Welcome message for user 3"
					)
				)

			When("consuming multiple messages sequentially") {
				clearMocks(welcomeSignUpEventHandler)

				every { welcomeSignUpEventHandler.handle(any<MailPayload>()) } just Runs

				mailPayloads.forEach { payload ->
					welcomeSignUpKafkaAdapter.consume(payload)
				}

				Then("should handle each message") {
					mailPayloads.forEach { payload ->
						verify(exactly = 1) {
							welcomeSignUpEventHandler.handle(payload)
						}
					}

					verify(exactly = 3) {
						welcomeSignUpEventHandler.handle(any<MailPayload>())
					}
				}
			}

			When("one message fails in a batch") {
				clearMocks(welcomeSignUpEventHandler)

				every {
					welcomeSignUpEventHandler.handle(
						match<MailPayload> { it.to == "user1@example.com" }
					)
				} just Runs

				every {
					welcomeSignUpEventHandler.handle(
						match<MailPayload> { it.to == "user2@example.com" }
					)
				} throws RuntimeException("Failed to send email to user2")

				every {
					welcomeSignUpEventHandler.handle(
						match<MailPayload> { it.to == "user3@example.com" }
					)
				} just Runs

				Then("should process other messages independently") {
					welcomeSignUpKafkaAdapter.consume(mailPayloads[0])

					shouldThrow<RuntimeException> {
						welcomeSignUpKafkaAdapter.consume(mailPayloads[1])
					}

					welcomeSignUpKafkaAdapter.consume(mailPayloads[2])

					verify(exactly = 1) {
						welcomeSignUpEventHandler.handle(mailPayloads[0])
						welcomeSignUpEventHandler.handle(mailPayloads[1])
						welcomeSignUpEventHandler.handle(mailPayloads[2])
					}
				}
			}
		}

		Given("edge case scenarios") {
			When("consuming payload with empty fields") {
				val emptyPayload =
					MailPayload(
						to = "",
						subject = "",
						body = ""
					)

				every { welcomeSignUpEventHandler.handle(emptyPayload) } just Runs

				welcomeSignUpKafkaAdapter.consume(emptyPayload)

				Then("should still delegate to handler") {
					verify(exactly = 1) {
						welcomeSignUpEventHandler.handle(emptyPayload)
					}
				}
			}

			When("consuming payload with very long content") {
				val longContent = "A".repeat(10000)
				val longPayload =
					MailPayload(
						to = "longcontent@example.com",
						subject = "Subject: $longContent",
						body = "Body: $longContent"
					)

				every { welcomeSignUpEventHandler.handle(longPayload) } just Runs

				welcomeSignUpKafkaAdapter.consume(longPayload)

				Then("should handle long content") {
					verify(exactly = 1) {
						welcomeSignUpEventHandler.handle(
							match<MailPayload> { payload ->
								payload.subject.length > 10000 &&
									payload.body.length > 10000
							}
						)
					}
				}
			}

			When("consuming payload with unicode characters") {
				val unicodePayload =
					MailPayload(
						to = "unicode@example.com",
						subject = "Welcome 🎉",
						body = "Multi-language: body"
					)

				every { welcomeSignUpEventHandler.handle(unicodePayload) } just Runs

				welcomeSignUpKafkaAdapter.consume(unicodePayload)

				Then("should handle unicode correctly") {
					verify(exactly = 1) {
						welcomeSignUpEventHandler.handle(
							match<MailPayload> { payload ->
								payload.subject.contains("Welcome") &&
									payload.subject.contains("🎉") &&
									payload.body.contains("body")
							}
						)
					}
				}
			}
		}
	})
