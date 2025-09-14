package com.example.demo.webhook.common

interface WebHookMessage {
	fun getTarget(): String

	fun getMessages(): List<String>
}
