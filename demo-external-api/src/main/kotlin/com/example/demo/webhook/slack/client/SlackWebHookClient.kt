package com.example.demo.webhook.slack.client

import com.example.demo.webhook.common.EmojiResolver
import com.example.demo.webhook.common.WebHookMessage
import com.example.demo.webhook.common.WebHookSender
import com.example.demo.webhook.constant.WebHookTarget
import com.example.demo.webhook.slack.model.SlackMessage
import com.example.demo.webhook.slack.model.SlackWebHookMessage
import com.slack.api.Slack
import com.slack.api.model.block.DividerBlock
import com.slack.api.model.block.LayoutBlock
import com.slack.api.model.block.SectionBlock
import com.slack.api.model.block.composition.MarkdownTextObject
import com.slack.api.webhook.Payload
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Component
class SlackWebHookClient(
	@Value("\${webhook.slack.url:}") private val url: String
) : WebHookSender {
	private val slackClient: Slack = Slack.getInstance()

	@Async("webhookExecutor")
	override fun send(message: WebHookMessage) {
		if (url.isBlank()) {
			logger.warn { "Slack webhook URL is not configured" }
			return
		}

		if (message !is SlackWebHookMessage) {
			logger.error { "Invalid message type for Slack: ${message::class.simpleName}" }
			return
		}

		runCatching {
			slackClient.send(url, generatePayload(message.getSlackMessages())).also {
				logger.info { "Slack webhook sent successfully on thread: ${Thread.currentThread().name}" }

				require(it.code == 200) {
					"Slack response code: ${it.code}, message: ${it.message}"
				}
			}
		}.onFailure { exception ->
			logger.error(exception) { "Failed to send message to Slack" }
			throw exception
		}
	}

	override fun target(): WebHookTarget = WebHookTarget.SLACK

	private fun generatePayload(slackMessages: List<SlackMessage>): Payload {
		val blocks =
			slackMessages.flatMapIndexed { index, message ->
				buildMessageBlock(message, isLast = index == slackMessages.lastIndex)
			}

		val text =
			slackMessages.joinToString("\n\n") { message ->
				"*${message.title}*\n" + message.messages.joinToString("\n") { "• $it" }
			}

		return Payload
			.builder()
			.text(text)
			.blocks(blocks)
			.build()
	}

	private fun buildMessageBlock(
		message: SlackMessage,
		isLast: Boolean
	): List<LayoutBlock> =
		mutableListOf<LayoutBlock>().apply {
			add(sectionBlock(EmojiResolver.resolveTitleEmoji(message.title) + " *${message.title}*"))
			add(dividerBlock())
			addAll(message.messages.map { sectionBlock(EmojiResolver.resolveLineEmoji(it) + " $it") })

			if (!isLast) add(dividerBlock())
		}

	private fun sectionBlock(text: String) =
		SectionBlock
			.builder()
			.text(markdownText(text))
			.build()

	private fun dividerBlock() = DividerBlock()

	private fun markdownText(text: String) =
		MarkdownTextObject
			.builder()
			.text(text)
			.build()
}
