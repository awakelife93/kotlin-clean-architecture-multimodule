package com.example.demo.webhook.discord.converter

import com.example.demo.webhook.common.CommonWebHookMessage
import com.example.demo.webhook.discord.model.DiscordMessage
import com.example.demo.webhook.discord.model.DiscordWebHookMessage
import org.springframework.stereotype.Component

@Component
class DiscordMessageConverter {
	fun convertCommonToDiscordMessage(message: CommonWebHookMessage): DiscordWebHookMessage {
		val discordMessage =
			DiscordWebHookMessage(
				mutableListOf(DiscordMessage.of(message.title, message.contents))
			)
		return discordMessage
	}

	fun convertToDiscordMessage(message: DiscordWebHookMessage): DiscordWebHookMessage = message
}
