package com.example.demo.webhook.converter

import com.example.demo.webhook.common.CommonWebHookMessage
import com.example.demo.webhook.common.WebHookMessage
import com.example.demo.webhook.constant.WebHookTarget
import com.example.demo.webhook.discord.converter.DiscordMessageConverter
import com.example.demo.webhook.discord.model.DiscordWebHookMessage
import com.example.demo.webhook.slack.converter.SlackMessageConverter
import com.example.demo.webhook.slack.model.SlackWebHookMessage
import org.springframework.stereotype.Component

@Component
class WebHookMessageConverter(
	private val slackMessageConverter: SlackMessageConverter,
	private val discordMessageConverter: DiscordMessageConverter
) {
	fun convert(
		target: WebHookTarget,
		message: WebHookMessage
	): WebHookMessage =
		when (target) {
			WebHookTarget.SLACK -> {
				when (message) {
					is SlackWebHookMessage -> slackMessageConverter.convertToSlackMessage(message)
					is CommonWebHookMessage -> slackMessageConverter.convertCommonToSlackMessage(message)
					else -> throw IllegalArgumentException("Unsupported message type: ${message::class} for Slack")
				}
			}

			WebHookTarget.DISCORD -> {
				when (message) {
					is DiscordWebHookMessage -> discordMessageConverter.convertToDiscordMessage(message)
					is CommonWebHookMessage -> discordMessageConverter.convertCommonToDiscordMessage(message)
					else -> throw IllegalArgumentException("Unsupported message type: ${message::class} for Discord")
				}
			}

			WebHookTarget.ALL -> throw IllegalArgumentException("Cannot convert for ALL target")
		}
}
