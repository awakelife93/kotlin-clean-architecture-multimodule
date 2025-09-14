package com.example.demo.mockito.mail.service

import com.example.demo.mail.MailHelper
import com.example.demo.mail.model.MailPayload
import com.example.demo.mail.service.WelcomeSignUpEventService
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
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.reset
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@Tag("mockito-unit-test")
@ExtendWith(MockitoExtension::class)
@DisplayName("Mockito Unit - WelcomeSignUpEventService Test")
class WelcomeSignUpEventServiceTests {
	@Mock
	private lateinit var mailHelper: MailHelper

	@InjectMocks
	private lateinit var welcomeSignUpEventService: WelcomeSignUpEventService

	@Nested
	@DisplayName("handle method tests")
	inner class HandleMethodTests {
		@Test
		@DisplayName("should successfully send welcome email")
		fun shouldSuccessfullySendWelcomeEmail() {
			val mailPayload =
				MailPayload(
					to = "newuser@example.com",
					subject = "Welcome New User!",
					body = "Welcome to our service! We're glad to have you aboard."
				)

			doNothing().whenever(mailHelper).sendEmail(mailPayload)

			welcomeSignUpEventService.handle(mailPayload)

			verify(mailHelper, times(1)).sendEmail(mailPayload)
			verifyNoMoreInteractions(mailHelper)
		}

		@Test
		@DisplayName("should propagate exception from MailHelper")
		fun shouldPropagateExceptionFromMailHelper() {
			val mailPayload =
				MailPayload(
					to = "error@example.com",
					subject = "Welcome!",
					body = "Welcome message"
				)

			val exception = RuntimeException("SMTP connection failed")
			doThrow(exception).whenever(mailHelper).sendEmail(mailPayload)

			val thrownException =
				assertThrows<RuntimeException> {
					welcomeSignUpEventService.handle(mailPayload)
				}

			assertEquals("SMTP connection failed", thrownException.message)
			verify(mailHelper, times(1)).sendEmail(mailPayload)
		}
	}

	@Nested
	@DisplayName("batch processing tests")
	inner class BatchProcessingTests {
		@Test
		@DisplayName("should process multiple emails sequentially")
		fun shouldProcessMultipleEmailsSequentially() {
			val payloads =
				listOf(
					MailPayload("user1@example.com", "Welcome User 1!", "Message 1"),
					MailPayload("user2@example.com", "Welcome User 2!", "Message 2"),
					MailPayload("user3@example.com", "Welcome User 3!", "Message 3")
				)

			doNothing().whenever(mailHelper).sendEmail(any<MailPayload>())

			payloads.forEach { payload ->
				welcomeSignUpEventService.handle(payload)
			}

			payloads.forEach { payload ->
				verify(mailHelper, times(1)).sendEmail(payload)
			}

			verify(mailHelper, times(3)).sendEmail(any<MailPayload>())
		}

		@Test
		@DisplayName("should handle partial failures in batch")
		fun shouldHandlePartialFailuresInBatch() {
			val payload1 = MailPayload("success1@example.com", "Welcome!", "Message 1")
			val payload2 = MailPayload("fail@example.com", "Welcome!", "Message 2")
			val payload3 = MailPayload("success2@example.com", "Welcome!", "Message 3")

			doNothing().whenever(mailHelper).sendEmail(payload1)
			doThrow(RuntimeException("Failed")).whenever(mailHelper).sendEmail(payload2)
			doNothing().whenever(mailHelper).sendEmail(payload3)

			assertDoesNotThrow { welcomeSignUpEventService.handle(payload1) }
			assertThrows<RuntimeException> { welcomeSignUpEventService.handle(payload2) }
			assertDoesNotThrow { welcomeSignUpEventService.handle(payload3) }

			verify(mailHelper, times(1)).sendEmail(payload1)
			verify(mailHelper, times(1)).sendEmail(payload2)
			verify(mailHelper, times(1)).sendEmail(payload3)
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

			doNothing().whenever(mailHelper).sendEmail(emptyPayload)

			welcomeSignUpEventService.handle(emptyPayload)

			verify(mailHelper, times(1)).sendEmail(emptyPayload)
		}
	}

	@Nested
	@DisplayName("exception handling tests")
	inner class ExceptionHandlingTests {
		@Test
		@DisplayName("should propagate IllegalArgumentException")
		fun shouldPropagateIllegalArgumentException() {
			val payload = MailPayload("test@example.com", "Test", "Test")
			val exception = IllegalArgumentException("Invalid email format")

			doThrow(exception).whenever(mailHelper).sendEmail(payload)

			val thrownException =
				assertThrows<IllegalArgumentException> {
					welcomeSignUpEventService.handle(payload)
				}

			assertEquals("Invalid email format", thrownException.message)
			verify(mailHelper, times(1)).sendEmail(payload)
		}

		@Test
		@DisplayName("should propagate IllegalStateException")
		fun shouldPropagateIllegalStateException() {
			val payload = MailPayload("test@example.com", "Test", "Test")
			val exception = IllegalStateException("Mail service not configured")

			doThrow(exception).whenever(mailHelper).sendEmail(payload)

			val thrownException =
				assertThrows<IllegalStateException> {
					welcomeSignUpEventService.handle(payload)
				}

			assertEquals("Mail service not configured", thrownException.message)
			verify(mailHelper, times(1)).sendEmail(payload)
		}

		@Test
		@DisplayName("should handle multiple exception types")
		fun shouldHandleMultipleExceptionTypes() {
			val payload = MailPayload("test@example.com", "Test", "Test")
			val exceptions =
				listOf(
					RuntimeException("Connection timeout"),
					IllegalArgumentException("Invalid parameter"),
					IllegalStateException("Service unavailable")
				)

			exceptions.forEach { exception ->
				reset(mailHelper)
				doThrow(exception).whenever(mailHelper).sendEmail(payload)

				val thrownException =
					assertThrows<Exception> {
						welcomeSignUpEventService.handle(payload)
					}

				assertEquals(exception.message, thrownException.message)
				assertEquals(exception.javaClass, thrownException.javaClass)
				verify(mailHelper, times(1)).sendEmail(payload)
			}
		}
	}
}
