package com.example.demo.webhook.discord.model

import com.example.demo.webhook.common.WebHookMessage

data class DiscordWebHookMessage(
	private val messages: List<DiscordMessage>
) : WebHookMessage {
	override fun getTarget(): String = "DISCORD"

	override fun getMessages(): List<String> = messages.flatMap { it.messages }

	fun getDiscordMessages(): List<DiscordMessage> = messages
}
