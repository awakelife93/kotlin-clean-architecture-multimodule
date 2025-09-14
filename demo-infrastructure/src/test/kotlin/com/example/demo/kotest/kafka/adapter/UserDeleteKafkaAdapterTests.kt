package com.example.demo.kotest.kafka.adapter

import com.example.demo.kafka.adapter.UserDeleteKafkaAdapter
import com.example.demo.user.event.UserDeleteEventHandler
import com.example.demo.user.model.UserDeleteItem
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
import java.time.LocalDateTime

@ActiveProfiles("test")
@Tags("kotest-unit-test")
class UserDeleteKafkaAdapterTests :
	BehaviorSpec({

		val userDeleteEventHandler = mockk<UserDeleteEventHandler>()
		val userDeleteKafkaAdapter = UserDeleteKafkaAdapter(userDeleteEventHandler)

		Given("a UserDeleteItem message from Kafka") {
			val userDeleteItem =
				UserDeleteItem(
					id = 100L,
					email = "deleted@example.com",
					name = "Deleted User",
					role = "USER",
					deletedDt = LocalDateTime.now().minusYears(1)
				)

			When("consuming the message successfully") {
				every { userDeleteEventHandler.handle(userDeleteItem) } just Runs

				userDeleteKafkaAdapter.consume(userDeleteItem)

				Then("should delegate to UserDeleteEventHandler") {
					verify(exactly = 1) {
						userDeleteEventHandler.handle(userDeleteItem)
					}
				}
			}

			When("handler throws an exception") {
				clearMocks(userDeleteEventHandler)

				val exception = RuntimeException("Database connection failed")
				every { userDeleteEventHandler.handle(userDeleteItem) } throws exception

				Then("should propagate the exception") {
					val thrownException =
						shouldThrow<RuntimeException> {
							userDeleteKafkaAdapter.consume(userDeleteItem)
						}

					thrownException.message shouldBe "Database connection failed"

					verify(exactly = 1) {
						userDeleteEventHandler.handle(userDeleteItem)
					}
				}
			}
		}

		Given("multiple UserDeleteItem messages") {
			val userDeleteItems =
				listOf(
					UserDeleteItem(
						id = 1L,
						email = "user1@example.com",
						name = "User 1",
						role = "USER",
						deletedDt = LocalDateTime.now().minusYears(1)
					),
					UserDeleteItem(
						id = 2L,
						email = "user2@example.com",
						name = "User 2",
						role = "ADMIN",
						deletedDt = LocalDateTime.now().minusYears(2)
					),
					UserDeleteItem(
						id = 3L,
						email = "user3@example.com",
						name = "User 3",
						role = "USER",
						deletedDt = LocalDateTime.now().minusMonths(13)
					)
				)

			When("consuming multiple messages sequentially") {
				clearMocks(userDeleteEventHandler)

				every { userDeleteEventHandler.handle(any<UserDeleteItem>()) } just Runs

				userDeleteItems.forEach { item ->
					userDeleteKafkaAdapter.consume(item)
				}

				Then("should handle each message") {
					userDeleteItems.forEach { item ->
						verify(exactly = 1) {
							userDeleteEventHandler.handle(item)
						}
					}

					verify(exactly = 3) {
						userDeleteEventHandler.handle(any<UserDeleteItem>())
					}
				}
			}

			When("one message fails in a batch") {
				clearMocks(userDeleteEventHandler)

				every {
					userDeleteEventHandler.handle(
						match<UserDeleteItem> { it.id == 1L }
					)
				} just Runs

				every {
					userDeleteEventHandler.handle(
						match<UserDeleteItem> { it.id == 2L }
					)
				} throws RuntimeException("Failed to process user 2")

				every {
					userDeleteEventHandler.handle(
						match<UserDeleteItem> { it.id == 3L }
					)
				} just Runs

				Then("should process other messages independently") {
					userDeleteKafkaAdapter.consume(userDeleteItems[0])

					shouldThrow<RuntimeException> {
						userDeleteKafkaAdapter.consume(userDeleteItems[1])
					}

					userDeleteKafkaAdapter.consume(userDeleteItems[2])

					verify(exactly = 1) {
						userDeleteEventHandler.handle(userDeleteItems[0])
						userDeleteEventHandler.handle(userDeleteItems[1])
						userDeleteEventHandler.handle(userDeleteItems[2])
					}
				}
			}
		}

		Given("edge case scenarios") {
			When("consuming item with very old deletion date") {
				val veryOldItem =
					UserDeleteItem(
						id = 400L,
						email = "ancient@example.com",
						name = "Ancient User",
						role = "USER",
						deletedDt = LocalDateTime.now().minusYears(10)
					)

				every { userDeleteEventHandler.handle(veryOldItem) } just Runs

				userDeleteKafkaAdapter.consume(veryOldItem)

				Then("should handle very old items") {
					verify(exactly = 1) {
						userDeleteEventHandler.handle(veryOldItem)
					}
				}
			}

			When("consuming item with future deletion date") {
				val futureItem =
					UserDeleteItem(
						id = 600L,
						email = "future@example.com",
						name = "Future User",
						role = "ADMIN",
						deletedDt = LocalDateTime.now().plusDays(1)
					)

				every { userDeleteEventHandler.handle(futureItem) } just Runs

				userDeleteKafkaAdapter.consume(futureItem)

				Then("should handle future dates") {
					verify(exactly = 1) {
						userDeleteEventHandler.handle(futureItem)
					}
				}
			}
		}
	})
