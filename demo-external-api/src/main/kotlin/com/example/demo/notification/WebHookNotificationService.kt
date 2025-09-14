package com.example.demo.notification

import com.example.demo.webhook.service.WebHookService
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class WebHookNotificationService(
	private val webHookService: WebHookService,
	@Value("\${webhook.enabled:false}") private val enabled: Boolean
) : NotificationService {
	override fun sendNotification(
		title: String,
		messages: List<String>
	) {
		if (enabled) {
			webHookService.sendAll(title, messages)
		}
	}

	override fun sendCriticalAlert(
		title: String,
		messages: List<String>
	) {
		if (enabled) {
			webHookService.sendAll("🚨 $title", messages)
		}
	}

	override fun isEnabled(): Boolean = enabled
}
