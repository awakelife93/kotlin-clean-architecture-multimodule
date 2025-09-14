package com.example.demo.notification

interface NotificationService {
	fun sendNotification(
		title: String,
		messages: List<String>
	)

	fun sendCriticalAlert(
		title: String,
		messages: List<String>
	)

	fun isEnabled(): Boolean
}
