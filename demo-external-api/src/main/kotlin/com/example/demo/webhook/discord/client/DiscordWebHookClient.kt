package com.example.demo.webhook.discord.client

import com.example.demo.webhook.common.EmojiResolver
import com.example.demo.webhook.common.WebHookMessage
import com.example.demo.webhook.common.WebHookSender
import com.example.demo.webhook.constant.WebHookTarget
import com.example.demo.webhook.discord.model.DiscordMessage
import com.example.demo.webhook.discord.model.DiscordWebHookMessage
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

private val logger = KotlinLogging.logger {}

@Component
class DiscordWebHookClient(
	@Value("\${webhook.discord.url:}") private val url: String,
	private val webClient: WebClient
) : WebHookSender {
	override fun send(message: WebHookMessage) {
		if (url.isBlank()) {
			logger.warn { "Discord webhook URL is not configured" }
			return
		}

		if (message !is DiscordWebHookMessage) {
			logger.error { "Invalid message type for Discord: ${message::class.simpleName}" }
			return
		}

		webClient
			.post()
			.uri(url)
			.bodyValue(generatePayload(message.getDiscordMessages()))
			.retrieve()
			.bodyToMono(String::class.java)
			.doOnNext {
				logger.info { "Discord webhook sent successfully on thread: ${Thread.currentThread().name}" }
			}.doOnError { exception ->
				logger.error(exception) { "Failed to send message to Discord" }
			}.subscribe()
	}

	override fun target(): WebHookTarget = WebHookTarget.DISCORD

	private fun generatePayload(messages: List<DiscordMessage>): Map<String, Any> {
		val content = messages.joinToString("\n\n", transform = ::formatMessage)
		val embeds = messages.mapNotNull { it.embeds }.flatten()

		return buildMap {
			put("content", content)
			if (embeds.isNotEmpty()) put("embeds", embeds)
		}
	}

	private fun formatMessage(message: DiscordMessage): String {
		val titleEmoji = EmojiResolver.resolveTitleEmoji(message.title)
		val header = "$titleEmoji **[${message.title}]**"

		val body =
			message.messages.joinToString("\n") { line ->
				"${EmojiResolver.resolveLineEmoji(line)} $line"
			}

		return "$header\n$body"
	}
}
