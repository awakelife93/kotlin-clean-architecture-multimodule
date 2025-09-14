package com.example.demo.mockito.mail

import com.example.demo.exception.CustomRuntimeException
import com.example.demo.mail.MailHelper
import com.example.demo.mail.model.MailPayload
import com.example.demo.notification.NotificationService
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.eq
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.mail.MailSender
import org.springframework.mail.SimpleMailMessage
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@Tag("mockito-unit-test")
@DisplayName("Mockito Unit - Mail Helper Test")
@ExtendWith(MockitoExtension::class)
class MailHelperTests {
	@Mock
	private lateinit var mailSender: MailSender

	@Mock
	private lateinit var notificationService: NotificationService

	private val mailHelper: MailHelper by lazy {
		MailHelper(mailSender, notificationService)
	}

	@Test
	fun `should send email with correct values`() {
		val payload =
			MailPayload.of(
				to = "user@example.com",
				subject = "Test Subject",
				body = "Test Body"
			)

		mailHelper.sendEmail(payload)

		val messageCaptor = argumentCaptor<SimpleMailMessage>()
		verify(mailSender, times(1)).send(messageCaptor.capture())

		val captured = messageCaptor.firstValue
		assertArrayEquals(arrayOf("user@example.com"), captured.to)
		assertEquals("Test Subject", captured.subject)
		assertEquals("Test Body", captured.text)
	}

	@Test
	fun `should throw exception for invalid email`() {
		val payload =
			MailPayload.of(
				to = "invalid-email",
				subject = "Subject",
				body = "Body"
			)

		val exception =
			assertThrows<CustomRuntimeException> {
				mailHelper.sendEmail(payload)
			}

		assertEquals(
			"Mail sending failed: Validation failed: to must be a valid email",
			exception.message
		)

		verify(mailSender, times(0)).send(any<SimpleMailMessage>())
	}

	@Test
	fun `should call sendCriticalAlert on mail sending failure`() {
		val payload =
			MailPayload.of(
				to = "user@example.com",
				subject = "Test Subject",
				body = "Test Body"
			)

		doThrow(CustomRuntimeException("SMTP error"))
			.whenever(mailSender)
			.send(any<SimpleMailMessage>())

		doNothing().whenever(notificationService).sendCriticalAlert(any<String>(), any<List<String>>())

		assertThrows<CustomRuntimeException> {
			mailHelper.sendEmail(payload)
		}

		verify(notificationService, times(1)).sendCriticalAlert(
			eq("Mail Sending Failed"),
			eq(
				listOf(
					"Mail to: ${payload.to}",
					"Mail Subject: ${payload.subject}",
					"Mail Body: ${payload.body}",
					"Error: SMTP error"
				)
			)
		)
	}

	@Test
	@DisplayName("should throw exception for empty email")
	fun `should throw exception for empty email`() {
		val payload =
			MailPayload.of(
				to = "",
				subject = "Subject",
				body = "Body"
			)

		val exception =
			assertThrows<CustomRuntimeException> {
				mailHelper.sendEmail(payload)
			}

		assertTrue(exception.message!!.contains("to cannot be empty"))
		verify(mailSender, times(0)).send(any<SimpleMailMessage>())
	}

	@Test
	@DisplayName("should throw exception for blank email with spaces")
	fun `should throw exception for blank email with spaces`() {
		val payload =
			MailPayload.of(
				to = "   ",
				subject = "Subject",
				body = "Body"
			)

		val exception =
			assertThrows<CustomRuntimeException> {
				mailHelper.sendEmail(payload)
			}

		assertTrue(exception.message!!.contains("to cannot be empty"))
		verify(mailSender, times(0)).send(any<SimpleMailMessage>())
	}

	@Test
	@DisplayName("should throw exception for empty subject")
	fun `should throw exception for empty subject`() {
		val payload =
			MailPayload.of(
				to = "test@example.com",
				subject = "",
				body = "Body"
			)

		val exception =
			assertThrows<CustomRuntimeException> {
				mailHelper.sendEmail(payload)
			}

		assertTrue(exception.message!!.contains("Subject cannot be empty"))
		verify(mailSender, times(0)).send(any<SimpleMailMessage>())
	}

	@Test
	@DisplayName("should throw exception for blank subject with spaces")
	fun `should throw exception for blank subject with spaces`() {
		val payload =
			MailPayload.of(
				to = "test@example.com",
				subject = "   ",
				body = "Body"
			)

		val exception =
			assertThrows<CustomRuntimeException> {
				mailHelper.sendEmail(payload)
			}

		assertTrue(exception.message!!.contains("Subject cannot be empty"))
		verify(mailSender, times(0)).send(any<SimpleMailMessage>())
	}

	@Test
	@DisplayName("should throw exception for empty body")
	fun `should throw exception for empty body`() {
		val payload =
			MailPayload.of(
				to = "test@example.com",
				subject = "Subject",
				body = ""
			)

		val exception =
			assertThrows<CustomRuntimeException> {
				mailHelper.sendEmail(payload)
			}

		assertTrue(exception.message!!.contains("Body cannot be empty"))
		verify(mailSender, times(0)).send(any<SimpleMailMessage>())
	}

	@Test
	@DisplayName("should throw exception for blank body with spaces")
	fun `should throw exception for blank body with spaces`() {
		val payload =
			MailPayload.of(
				to = "test@example.com",
				subject = "Subject",
				body = "   "
			)

		val exception =
			assertThrows<CustomRuntimeException> {
				mailHelper.sendEmail(payload)
			}

		assertTrue(exception.message!!.contains("Body cannot be empty"))
		verify(mailSender, times(0)).send(any<SimpleMailMessage>())
	}

	@Test
	@DisplayName("should throw exception with multiple validation errors")
	fun `should throw exception with multiple validation errors`() {
		val payload =
			MailPayload.of(
				to = "invalid-email",
				subject = "",
				body = ""
			)

		val exception =
			assertThrows<CustomRuntimeException> {
				mailHelper.sendEmail(payload)
			}

		assertTrue(exception.message!!.contains("to must be a valid email"))
		assertTrue(exception.message!!.contains("Subject cannot be empty"))
		assertTrue(exception.message!!.contains("Body cannot be empty"))
		verify(mailSender, times(0)).send(any<SimpleMailMessage>())
	}

	@Test
	@DisplayName("should accept various valid email formats")
	fun `should accept various valid email formats`() {
		val validEmails =
			listOf(
				"user@example.com",
				"user.name@example.com",
				"user+tag@example.co.uk",
				"user_name@example-domain.com",
				"123@example.com"
			)

		validEmails.forEach { email ->
			val payload =
				MailPayload.of(
					to = email,
					subject = "Test",
					body = "Test"
				)

			mailHelper.sendEmail(payload)
		}

		verify(mailSender, times(validEmails.size)).send(any<SimpleMailMessage>())
	}

	@Test
	@DisplayName("should handle special characters in subject and body")
	fun `should handle special characters in subject and body`() {
		val payload =
			MailPayload.of(
				to = "test@example.com",
				subject = "Special chars: !@#$%^&*()_+-=[]{}|;':\",./<>?",
				body = "Unicode: emoji 😀🎉\nNew line\tTab"
			)

		mailHelper.sendEmail(payload)

		val messageCaptor = argumentCaptor<SimpleMailMessage>()
		verify(mailSender, times(1)).send(messageCaptor.capture())

		val captured = messageCaptor.firstValue
		assertEquals("Special chars: !@#$%^&*()_+-=[]{}|;':\",./<>?", captured.subject)
		assertEquals("Unicode: emoji 😀🎉\nNew line\tTab", captured.text)
	}

	@Test
	@DisplayName("should handle very long subject and body")
	fun `should handle very long subject and body`() {
		val longSubject = "Subject ".repeat(100)
		val longBody = "Body content ".repeat(1000)

		val payload =
			MailPayload.of(
				to = "test@example.com",
				subject = longSubject,
				body = longBody
			)

		mailHelper.sendEmail(payload)

		val messageCaptor = argumentCaptor<SimpleMailMessage>()
		verify(mailSender, times(1)).send(messageCaptor.capture())

		val captured = messageCaptor.firstValue
		assertEquals(longSubject, captured.subject)
		assertEquals(longBody, captured.text)
	}

	@Test
	@DisplayName("should handle network timeout during mail sending")
	fun `should handle network timeout during mail sending`() {
		val payload =
			MailPayload.of(
				to = "test@example.com",
				subject = "Test",
				body = "Test"
			)

		doThrow(RuntimeException("Connection timeout"))
			.whenever(mailSender)
			.send(any<SimpleMailMessage>())

		doNothing().whenever(notificationService).sendCriticalAlert(any<String>(), any<List<String>>())

		val exception =
			assertThrows<CustomRuntimeException> {
				mailHelper.sendEmail(payload)
			}

		assertTrue(exception.message!!.contains("Connection timeout"))

		verify(notificationService, times(1)).sendCriticalAlert(
			eq("Mail Sending Failed"),
			eq(
				listOf(
					"Mail to: test@example.com",
					"Mail Subject: Test",
					"Mail Body: Test",
					"Error: Connection timeout"
				)
			)
		)
	}

	@Test
	@DisplayName("should handle mail server authentication failure")
	fun `should handle mail server authentication failure`() {
		val payload =
			MailPayload.of(
				to = "test@example.com",
				subject = "Test",
				body = "Test"
			)

		doThrow(RuntimeException("Authentication failed"))
			.whenever(mailSender)
			.send(any<SimpleMailMessage>())

		doNothing().whenever(notificationService).sendCriticalAlert(any<String>(), any<List<String>>())

		val exception =
			assertThrows<CustomRuntimeException> {
				mailHelper.sendEmail(payload)
			}

		assertTrue(exception.message!!.contains("Authentication failed"))

		verify(notificationService, times(1)).sendCriticalAlert(
			eq("Mail Sending Failed"),
			eq(
				listOf(
					"Mail to: test@example.com",
					"Mail Subject: Test",
					"Mail Body: Test",
					"Error: Authentication failed"
				)
			)
		)
	}

	@Test
	@DisplayName("should reject email without domain")
	fun `should reject email without domain`() {
		val payload =
			MailPayload.of(
				to = "user@",
				subject = "Subject",
				body = "Body"
			)

		val exception =
			assertThrows<CustomRuntimeException> {
				mailHelper.sendEmail(payload)
			}

		assertTrue(exception.message!!.contains("to must be a valid email"))
		verify(mailSender, times(0)).send(any<SimpleMailMessage>())
	}

	@Test
	@DisplayName("should reject email with multiple @ symbols")
	fun `should reject email with multiple @ symbols`() {
		val payload =
			MailPayload.of(
				to = "user@@example.com",
				subject = "Subject",
				body = "Body"
			)

		val exception =
			assertThrows<CustomRuntimeException> {
				mailHelper.sendEmail(payload)
			}

		assertTrue(exception.message!!.contains("to must be a valid email"))
		verify(mailSender, times(0)).send(any<SimpleMailMessage>())
	}
}
