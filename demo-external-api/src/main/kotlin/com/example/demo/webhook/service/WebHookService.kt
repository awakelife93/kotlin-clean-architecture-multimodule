package com.example.demo.webhook.service

import com.example.demo.webhook.common.CommonWebHookMessage
import com.example.demo.webhook.common.WebHookMessage
import com.example.demo.webhook.constant.WebHookTarget
import com.example.demo.webhook.converter.WebHookMessageConverter
import com.example.demo.webhook.discord.model.DiscordEmbed
import com.example.demo.webhook.discord.model.DiscordMessage
import com.example.demo.webhook.discord.model.DiscordWebHookMessage
import com.example.demo.webhook.router.WebHookRouter
import com.example.demo.webhook.slack.model.SlackMessage
import com.example.demo.webhook.slack.model.SlackWebHookMessage
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class WebHookService(
	private val webhookRouter: WebHookRouter,
	private val webhookMessageConverter: WebHookMessageConverter,
	@Value("\${webhook.enabled:false}") private val enabled: Boolean
) {
	fun sendAll(
		title: String,
		lines: List<String>
	) {
		send(WebHookTarget.ALL, CommonWebHookMessage(title, lines))
	}

	fun sendSlack(
		title: String,
		lines: List<String>
	) {
		send(
			WebHookTarget.SLACK,
			SlackWebHookMessage(mutableListOf(SlackMessage.of(title, lines)))
		)
	}

	fun sendDiscord(
		title: String,
		lines: List<String>,
		embeds: List<DiscordEmbed>? = null
	) {
		send(
			WebHookTarget.DISCORD,
			DiscordWebHookMessage(mutableListOf(DiscordMessage.of(title, lines, embeds)))
		)
	}

	private fun send(
		target: WebHookTarget,
		message: WebHookMessage
	) {
		if (!enabled) {
			logger.debug { "Webhook is disabled, skipping send" }
			return
		}

		when (target) {
			WebHookTarget.ALL -> {
				require(message is CommonWebHookMessage) {
					"When using WebHookTarget.ALL, message must be of type CommonWebHookMessage"
				}
				sendToAll(message)
			}
			else -> sendToTarget(target, message)
		}
	}

	private fun sendToTarget(
		target: WebHookTarget,
		message: WebHookMessage
	) {
		val sender = webhookRouter.route(target)
		if (sender == null) {
			logger.warn { "No webhook sender found for target: $target" }
			return
		}

		val converted = webhookMessageConverter.convert(target, message)
		sender.send(converted)
	}

	private fun sendToAll(message: CommonWebHookMessage) {
		webhookRouter.all().forEach { sender ->
			runCatching {
				val converted = webhookMessageConverter.convert(sender.target(), message)
				sender.send(converted)
			}.onFailure { exception ->
				logger.error(exception) {
					"Failed to send webhook to ${sender.target()}"
				}
			}
		}
	}
}
