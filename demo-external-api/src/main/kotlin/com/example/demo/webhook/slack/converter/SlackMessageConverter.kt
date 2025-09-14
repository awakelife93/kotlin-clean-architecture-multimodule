package com.example.demo.webhook.slack.converter

import com.example.demo.webhook.common.CommonWebHookMessage
import com.example.demo.webhook.slack.model.SlackMessage
import com.example.demo.webhook.slack.model.SlackWebHookMessage
import org.springframework.stereotype.Component

@Component
class SlackMessageConverter {
	fun convertCommonToSlackMessage(message: CommonWebHookMessage): SlackWebHookMessage {
		val slackMessage =
			SlackWebHookMessage(
				mutableListOf(SlackMessage.of(message.title, message.contents))
			)
		return slackMessage
	}

	fun convertToSlackMessage(message: SlackWebHookMessage): SlackWebHookMessage = message
}
