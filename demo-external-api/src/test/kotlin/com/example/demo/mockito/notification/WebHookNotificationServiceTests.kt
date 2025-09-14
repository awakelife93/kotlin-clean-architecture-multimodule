package com.example.demo.mockito.notification

import com.example.demo.notification.WebHookNotificationService
import com.example.demo.webhook.service.WebHookService
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@Tag("mockito-unit-test")
@DisplayName("Mockito Unit - WebHookNotificationService Test")
@ExtendWith(MockitoExtension::class)
class WebHookNotificationServiceTests {
	@Mock
	private lateinit var mockWebHookService: WebHookService

	private lateinit var enabledService: WebHookNotificationService
	private lateinit var disabledService: WebHookNotificationService

	@BeforeEach
	fun setUp() {
		enabledService = WebHookNotificationService(mockWebHookService, true)
		disabledService = WebHookNotificationService(mockWebHookService, false)
	}

	@Nested
	@DisplayName("Notification sending tests")
	inner class NotificationTests {
		@Test
		@DisplayName("should send notification when enabled")
		fun testSendNotificationWhenEnabled() {
			val title = "Test Title"
			val messages = listOf("Message 1", "Message 2")

			enabledService.sendNotification(title, messages)

			verify(mockWebHookService).sendAll(title, messages)
		}

		@Test
		@DisplayName("should not send notification when disabled")
		fun testSendNotificationWhenDisabled() {
			val title = "Test Title"
			val messages = listOf("Message 1", "Message 2")

			disabledService.sendNotification(title, messages)

			verify(mockWebHookService, never()).sendAll(any<String>(), any<List<String>>())
		}

		@Test
		@DisplayName("should send critical alert with emoji when enabled")
		fun testSendCriticalAlertWhenEnabled() {
			val title = "Critical Alert"
			val messages = listOf("Error 1", "Error 2")

			enabledService.sendCriticalAlert(title, messages)

			verify(mockWebHookService).sendAll("🚨 $title", messages)
		}

		@Test
		@DisplayName("should not send critical alert when disabled")
		fun testSendCriticalAlertWhenDisabled() {
			val title = "Critical Alert"
			val messages = listOf("Error 1", "Error 2")

			disabledService.sendCriticalAlert(title, messages)

			verify(mockWebHookService, never()).sendAll(any<String>(), any<List<String>>())
		}

		@Test
		@DisplayName("should return enabled status correctly")
		fun testIsEnabled() {
			assertTrue(enabledService.isEnabled())
			assertFalse(disabledService.isEnabled())
		}
	}
}
