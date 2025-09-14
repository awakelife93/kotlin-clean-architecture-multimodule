package com.example.demo.kotest.notification

import com.example.demo.notification.WebHookNotificationService
import com.example.demo.webhook.service.WebHookService
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@Tags("kotest-unit-test")
class WebHookNotificationServiceTests :
	DescribeSpec({
		describe("WebHookNotificationService") {
			val webHookService = mockk<WebHookService>()

			beforeTest {
				clearMocks(webHookService)
				every { webHookService.sendAll(any<String>(), any<List<String>>()) } just runs
			}

			describe("when enabled") {
				val service = WebHookNotificationService(webHookService, true)

				it("should send notification via webhook service") {
					val title = "Test Notification"
					val messages = listOf("Message 1", "Message 2")

					service.sendNotification(title, messages)

					verify { webHookService.sendAll(title, messages) }
				}

				it("should send critical alert with emoji prefix") {
					val title = "Critical Issue"
					val messages = listOf("Error occurred", "Please check immediately")

					service.sendCriticalAlert(title, messages)

					verify { webHookService.sendAll("🚨 $title", messages) }
				}

				it("should report enabled status") {
					service.isEnabled() shouldBe true
				}

				it("should handle empty messages list") {
					val title = "Empty Notification"
					val messages = emptyList<String>()

					service.sendNotification(title, messages)

					verify { webHookService.sendAll(title, messages) }
				}

				it("should preserve message order") {
					val title = "Ordered Messages"
					val messages = listOf("First", "Second", "Third", "Fourth")

					service.sendNotification(title, messages)

					verify { webHookService.sendAll(title, messages) }
				}
			}

			describe("when disabled") {
				val service = WebHookNotificationService(webHookService, false)

				it("should not send notification") {
					val title = "Test Notification"
					val messages = listOf("Message 1", "Message 2")

					service.sendNotification(title, messages)

					verify(exactly = 0) { webHookService.sendAll(any<String>(), any<List<String>>()) }
				}

				it("should not send critical alert") {
					val title = "Critical Issue"
					val messages = listOf("Error occurred")

					service.sendCriticalAlert(title, messages)

					verify(exactly = 0) { webHookService.sendAll(any<String>(), any<List<String>>()) }
				}

				it("should report disabled status") {
					service.isEnabled() shouldBe false
				}
			}

			describe("edge cases") {
				val service = WebHookNotificationService(webHookService, true)

				it("should handle null-like strings in title") {
					val messages = listOf("message")

					service.sendNotification("null", messages)
					service.sendNotification("", messages)

					verify { webHookService.sendAll("null", messages) }
					verify { webHookService.sendAll("", messages) }
				}

				it("should handle very long message lists") {
					val title = "Long List"
					val messages = (1..1000).map { "Message $it" }

					service.sendNotification(title, messages)

					verify { webHookService.sendAll(title, messages) }
				}

				it("should handle special characters in critical alerts") {
					val title = "Critical: @#$%^&*() Issue 😀"
					val messages = listOf("Error with special chars")

					service.sendCriticalAlert(title, messages)

					verify { webHookService.sendAll("🚨 $title", messages) }
				}

				it("should handle multiple critical alerts in sequence") {
					repeat(10) { index ->
						service.sendCriticalAlert("Alert $index", listOf("Message $index"))
					}

					verify(exactly = 10) { webHookService.sendAll(any<String>(), any<List<String>>()) }
				}
			}
		}
	})
