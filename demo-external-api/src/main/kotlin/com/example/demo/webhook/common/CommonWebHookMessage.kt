package com.example.demo.webhook.common

data class CommonWebHookMessage(
	val title: String,
	val contents: List<String>
) : WebHookMessage {
	override fun getTarget(): String = "COMMON"

	override fun getMessages(): List<String> = contents
}
