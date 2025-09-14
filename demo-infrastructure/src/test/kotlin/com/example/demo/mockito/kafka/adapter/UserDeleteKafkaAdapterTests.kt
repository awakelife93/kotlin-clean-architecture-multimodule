package com.example.demo.mockito.kafka.adapter

import com.example.demo.kafka.adapter.UserDeleteKafkaAdapter
import com.example.demo.user.event.UserDeleteEventHandler
import com.example.demo.user.model.UserDeleteItem
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.argThat
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime

@ActiveProfiles("test")
@Tag("mockito-unit-test")
@DisplayName("Mockito Unit - User Delete Kafka Adapter Test")
@ExtendWith(MockitoExtension::class)
class UserDeleteKafkaAdapterTests {
	@Mock
	private lateinit var userDeleteEventHandler: UserDeleteEventHandler

	@InjectMocks
	private lateinit var userDeleteKafkaAdapter: UserDeleteKafkaAdapter

	@Nested
	@DisplayName("consume method tests")
	inner class ConsumeMethodTests {
		@Test
		@DisplayName("should successfully delegate to event handler")
		fun shouldSuccessfullyDelegateToEventHandler() {
			val userDeleteItem =
				UserDeleteItem(
					id = 100L,
					email = "deleted@example.com",
					name = "Deleted User",
					role = "USER",
					deletedDt = LocalDateTime.now().minusYears(1)
				)

			doNothing().whenever(userDeleteEventHandler).handle(userDeleteItem)

			userDeleteKafkaAdapter.consume(userDeleteItem)

			verify(userDeleteEventHandler, times(1)).handle(userDeleteItem)
			verifyNoMoreInteractions(userDeleteEventHandler)
		}

		@Test
		@DisplayName("should propagate exception from handler")
		fun shouldPropagateExceptionFromHandler() {
			val userDeleteItem =
				UserDeleteItem(
					id = 200L,
					email = "error@example.com",
					name = "Error User",
					role = "ADMIN",
					deletedDt = LocalDateTime.now().minusYears(1)
				)

			val exception = RuntimeException("Database connection failed")
			doThrow(exception).whenever(userDeleteEventHandler).handle(userDeleteItem)

			val thrownException =
				assertThrows<RuntimeException> {
					userDeleteKafkaAdapter.consume(userDeleteItem)
				}

			assertEquals("Database connection failed", thrownException.message)
			verify(userDeleteEventHandler, times(1)).handle(userDeleteItem)
		}
	}

	@Nested
	@DisplayName("batch processing tests")
	inner class BatchProcessingTests {
		@Test
		@DisplayName("should process multiple messages sequentially")
		fun shouldProcessMultipleMessagesSequentially() {
			val userDeleteItems =
				listOf(
					UserDeleteItem(
						1L,
						"user1@example.com",
						"User 1",
						"USER",
						LocalDateTime.now().minusYears(1)
					),
					UserDeleteItem(
						2L,
						"user2@example.com",
						"User 2",
						"ADMIN",
						LocalDateTime.now().minusYears(2)
					),
					UserDeleteItem(
						3L,
						"user3@example.com",
						"User 3",
						"USER",
						LocalDateTime.now().minusMonths(13)
					)
				)

			userDeleteItems.forEach { item ->
				doNothing().whenever(userDeleteEventHandler).handle(item)
			}

			userDeleteItems.forEach { item ->
				userDeleteKafkaAdapter.consume(item)
			}

			userDeleteItems.forEach { item ->
				verify(userDeleteEventHandler, times(1)).handle(item)
			}

			verifyNoMoreInteractions(userDeleteEventHandler)
		}

		@Test
		@DisplayName("should handle partial failures in batch")
		fun shouldHandlePartialFailuresInBatch() {
			val item1 =
				UserDeleteItem(
					1L,
					"success1@example.com",
					"Success 1",
					"USER",
					LocalDateTime.now().minusYears(1)
				)
			val item2 =
				UserDeleteItem(
					2L,
					"fail@example.com",
					"Fail",
					"USER",
					LocalDateTime.now().minusYears(1)
				)
			val item3 =
				UserDeleteItem(
					3L,
					"success2@example.com",
					"Success 2",
					"USER",
					LocalDateTime.now().minusYears(1)
				)

			doNothing().whenever(userDeleteEventHandler).handle(item1)
			doThrow(RuntimeException("Failed")).whenever(userDeleteEventHandler).handle(item2)
			doNothing().whenever(userDeleteEventHandler).handle(item3)

			assertDoesNotThrow { userDeleteKafkaAdapter.consume(item1) }

			val exception =
				assertThrows<RuntimeException> {
					userDeleteKafkaAdapter.consume(item2)
				}
			assertEquals("Failed", exception.message)

			assertDoesNotThrow { userDeleteKafkaAdapter.consume(item3) }

			verify(userDeleteEventHandler, times(1)).handle(item1)
			verify(userDeleteEventHandler, times(1)).handle(item2)
			verify(userDeleteEventHandler, times(1)).handle(item3)
		}
	}

	@Nested
	@DisplayName("edge case tests")
	inner class EdgeCaseTests {
		@Test
		@DisplayName("should handle very old deletion dates")
		fun shouldHandleVeryOldDeletionDates() {
			val veryOldItem =
				UserDeleteItem(
					id = 400L,
					email = "ancient@example.com",
					name = "Ancient User",
					role = "USER",
					deletedDt = LocalDateTime.now().minusYears(10)
				)

			doNothing().whenever(userDeleteEventHandler).handle(veryOldItem)

			userDeleteKafkaAdapter.consume(veryOldItem)

			verify(userDeleteEventHandler, times(1)).handle(veryOldItem)
			assertTrue(veryOldItem.deletedDt.isBefore(LocalDateTime.now().minusYears(5)))
		}

		@Test
		@DisplayName("should handle future deletion dates")
		fun shouldHandleFutureDeletionDates() {
			val futureItem =
				UserDeleteItem(
					id = 600L,
					email = "future@example.com",
					name = "Future User",
					role = "ADMIN",
					deletedDt = LocalDateTime.now().plusDays(1)
				)

			doNothing().whenever(userDeleteEventHandler).handle(futureItem)

			userDeleteKafkaAdapter.consume(futureItem)

			verify(userDeleteEventHandler, times(1)).handle(futureItem)
			assertTrue(futureItem.deletedDt.isAfter(LocalDateTime.now()))
		}

		@ParameterizedTest
		@ValueSource(strings = ["USER", "ADMIN", "MANAGER", "GUEST"])
		@DisplayName("should handle various user roles")
		fun shouldHandleVariousUserRoles(role: String) {
			val itemWithSpecialChars =
				UserDeleteItem(
					id = 700L,
					email = "role-test@example.com",
					name = "Role Test User",
					role = role,
					deletedDt = LocalDateTime.now().minusYears(1)
				)

			doNothing().whenever(userDeleteEventHandler).handle(itemWithSpecialChars)

			userDeleteKafkaAdapter.consume(itemWithSpecialChars)

			verify(userDeleteEventHandler, times(1)).handle(
				argThat<UserDeleteItem> { payload -> payload.role == role }
			)
		}
	}
}
