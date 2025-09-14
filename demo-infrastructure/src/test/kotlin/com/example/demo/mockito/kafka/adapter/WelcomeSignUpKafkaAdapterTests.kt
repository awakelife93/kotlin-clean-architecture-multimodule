package com.example.demo.mockito.kafka.adapter

import com.example.demo.kafka.adapter.WelcomeSignUpKafkaAdapter
import com.example.demo.mail.event.WelcomeSignUpEventHandler
import com.example.demo.mail.model.MailPayload
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@Tag("mockito-unit-test")
@DisplayName("Mockito Unit - Welcome Sign Up Kafka Adapter Test")
@ExtendWith(MockitoExtension::class)
class WelcomeSignUpKafkaAdapterTests {
	@Mock
	private lateinit var welcomeSignUpEventHandler: WelcomeSignUpEventHandler

	@InjectMocks
	private lateinit var welcomeSignUpKafkaAdapter: WelcomeSignUpKafkaAdapter

	@Nested
	@DisplayName("consume method tests")
	inner class ConsumeMethodTests {
		@Test
		@DisplayName("should successfully delegate to event handler")
		fun shouldSuccessfullyDelegateToEventHandler() {
			val mailPayload =
				MailPayload(
					to = "newuser@example.com",
					subject = "Welcome New User!",
					body = "Welcome to our service! We're glad to have you aboard."
				)

			doNothing().whenever(welcomeSignUpEventHandler).handle(mailPayload)

			welcomeSignUpKafkaAdapter.consume(mailPayload)

			verify(welcomeSignUpEventHandler, times(1)).handle(mailPayload)
			verifyNoMoreInteractions(welcomeSignUpEventHandler)
		}

		@Test
		@DisplayName("should propagate exception from handler")
		fun shouldPropagateExceptionFromHandler() {
			val mailPayload =
				MailPayload(
					to = "error@example.com",
					subject = "Welcome!",
					body = "Welcome message"
				)

			val exception = RuntimeException("Mail service unavailable")
			doThrow(exception).whenever(welcomeSignUpEventHandler).handle(mailPayload)

			val thrownException =
				assertThrows<RuntimeException> {
					welcomeSignUpKafkaAdapter.consume(mailPayload)
				}

			assertEquals("Mail service unavailable", thrownException.message)
			verify(welcomeSignUpEventHandler, times(1)).handle(mailPayload)
		}
	}

	@Nested
	@DisplayName("batch processing tests")
	inner class BatchProcessingTests {
		@Test
		@DisplayName("should process multiple messages sequentially")
		fun shouldProcessMultipleMessagesSequentially() {
			val mailPayloads =
				listOf(
					MailPayload("user1@example.com", "Welcome User 1!", "Welcome message 1"),
					MailPayload("user2@example.com", "Welcome User 2!", "Welcome message 2"),
					MailPayload("user3@example.com", "Welcome User 3!", "Welcome message 3")
				)

			doNothing().whenever(welcomeSignUpEventHandler).handle(any<MailPayload>())

			mailPayloads.forEach { payload ->
				welcomeSignUpKafkaAdapter.consume(payload)
			}

			mailPayloads.forEach { payload ->
				verify(welcomeSignUpEventHandler, times(1)).handle(payload)
			}

			verify(welcomeSignUpEventHandler, times(3)).handle(any<MailPayload>())
		}

		@Test
		@DisplayName("should handle partial failures in batch")
		fun shouldHandlePartialFailuresInBatch() {
			val payload1 = MailPayload("success1@example.com", "Welcome!", "Message 1")
			val payload2 = MailPayload("fail@example.com", "Welcome!", "Message 2")
			val payload3 = MailPayload("success2@example.com", "Welcome!", "Message 3")

			doNothing().whenever(welcomeSignUpEventHandler).handle(payload1)
			doThrow(RuntimeException("Failed")).whenever(welcomeSignUpEventHandler).handle(payload2)
			doNothing().whenever(welcomeSignUpEventHandler).handle(payload3)

			assertDoesNotThrow { welcomeSignUpKafkaAdapter.consume(payload1) }
			assertThrows<RuntimeException> { welcomeSignUpKafkaAdapter.consume(payload2) }
			assertDoesNotThrow { welcomeSignUpKafkaAdapter.consume(payload3) }

			verify(welcomeSignUpEventHandler, times(1)).handle(payload1)
			verify(welcomeSignUpEventHandler, times(1)).handle(payload2)
			verify(welcomeSignUpEventHandler, times(1)).handle(payload3)
		}
	}

	@Nested
	@DisplayName("edge case tests")
	inner class EdgeCaseTests {
		@Test
		@DisplayName("should handle empty fields")
		fun shouldHandleEmptyFields() {
			val emptyPayload =
				MailPayload(
					to = "",
					subject = "",
					body = ""
				)

			doNothing().whenever(welcomeSignUpEventHandler).handle(emptyPayload)

			welcomeSignUpKafkaAdapter.consume(emptyPayload)

			verify(welcomeSignUpEventHandler, times(1)).handle(emptyPayload)
		}

		@Test
		@DisplayName("should handle very long content")
		fun shouldHandleVeryLongContent() {
			val longContent = "A".repeat(10000)
			val longPayload =
				MailPayload(
					to = "longcontent@example.com",
					subject = "Subject: $longContent",
					body = "Body: $longContent"
				)

			doNothing().whenever(welcomeSignUpEventHandler).handle(any<MailPayload>())

			welcomeSignUpKafkaAdapter.consume(longPayload)

			verify(welcomeSignUpEventHandler, times(1)).handle(
				argThat<MailPayload> { payload ->
					payload.subject.length > 10000 && payload.body.length > 10000
				}
			)
		}

		@Test
		@DisplayName("should handle unicode characters")
		fun shouldHandleUnicodeCharacters() {
			val unicodePayload =
				MailPayload(
					to = "unicode@example.com",
					subject = "Welcome Hello 🎉",
					body = "body"
				)

			doNothing().whenever(welcomeSignUpEventHandler).handle(any<MailPayload>())

			welcomeSignUpKafkaAdapter.consume(unicodePayload)

			verify(welcomeSignUpEventHandler, times(1)).handle(
				argThat<MailPayload> { payload ->
					payload.subject.contains("Hello") &&
						payload.subject.contains("🎉") &&
						payload.body.contains("body")
				}
			)
		}

		@Test
		@DisplayName("should handle payloads with line breaks")
		fun shouldHandlePayloadsWithLineBreaks() {
			val multilinePayload =
				MailPayload(
					to = "multiline@example.com",
					subject = "Welcome to our service",
					body =
						"""
						Dear User,

						Welcome to our service!
						We're excited to have you.

						Best regards,
						The Team
						""".trimIndent()
				)

			doNothing().whenever(welcomeSignUpEventHandler).handle(any<MailPayload>())

			welcomeSignUpKafkaAdapter.consume(multilinePayload)

			verify(welcomeSignUpEventHandler, times(1)).handle(
				argThat<MailPayload> { payload ->
					payload.body.contains("Dear User") &&
						payload.body.contains("Best regards")
				}
			)
		}
	}
}
