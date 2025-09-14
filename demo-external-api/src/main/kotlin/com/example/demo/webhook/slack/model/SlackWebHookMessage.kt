package com.example.demo.webhook.slack.model

import com.example.demo.webhook.common.WebHookMessage

data class SlackWebHookMessage(
	private val messages: List<SlackMessage>
) : WebHookMessage {
	override fun getTarget(): String = "SLACK"

	override fun getMessages(): List<String> = messages.flatMap { it.messages }

	fun getSlackMessages(): List<SlackMessage> = messages
}
