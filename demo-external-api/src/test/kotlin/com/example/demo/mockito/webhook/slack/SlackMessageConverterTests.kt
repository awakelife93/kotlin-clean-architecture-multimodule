package com.example.demo.mockito.webhook.slack

import com.example.demo.webhook.common.CommonWebHookMessage
import com.example.demo.webhook.slack.converter.SlackMessageConverter
import com.example.demo.webhook.slack.model.SlackMessage
import com.example.demo.webhook.slack.model.SlackWebHookMessage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@Tag("mockito-unit-test")
@DisplayName("Mockito Unit - Slack Message Converter Test")
class SlackMessageConverterTests {
	private lateinit var converter: SlackMessageConverter

	@BeforeEach
	fun setup() {
		converter = SlackMessageConverter()
	}

	@Test
	@DisplayName("should convert CommonWebHookMessage to SlackWebHookMessage")
	fun should_convert_common_to_slack_message() {
		val title = "Test Title"
		val contents = listOf("Line 1", "Line 2", "Line 3")
		val commonMessage = CommonWebHookMessage(title, contents)

		val result = converter.convertCommonToSlackMessage(commonMessage)

		assertEquals(1, result.getSlackMessages().size)
		val slackMessage = result.getSlackMessages()[0]
		assertEquals(title, slackMessage.title)
		assertEquals(contents, slackMessage.messages)
	}

	@Test
	@DisplayName("should return SlackWebHookMessage as is")
	fun should_return_slack_message_as_is() {
		val slackMessage = SlackMessage("Slack Title", listOf("Message 1", "Message 2"))
		val webhookMessage = SlackWebHookMessage(mutableListOf(slackMessage))

		val result = converter.convertToSlackMessage(webhookMessage)

		assertEquals(webhookMessage, result)
		assertEquals(1, result.getSlackMessages().size)
		assertEquals(slackMessage, result.getSlackMessages()[0])
	}

	@Test
	@DisplayName("should handle empty contents in CommonWebHookMessage")
	fun should_handle_empty_contents() {
		val commonMessage = CommonWebHookMessage("Empty", emptyList())

		val result = converter.convertCommonToSlackMessage(commonMessage)

		assertEquals(1, result.getSlackMessages().size)
		val slackMessage = result.getSlackMessages()[0]
		assertEquals("Empty", slackMessage.title)
		assertEquals(emptyList<String>(), slackMessage.messages)
	}

	@Test
	@DisplayName("should handle multiple Slack messages")
	fun should_handle_multiple_slack_messages() {
		val messages =
			mutableListOf(
				SlackMessage("Title 1", listOf("Msg 1")),
				SlackMessage("Title 2", listOf("Msg 2")),
				SlackMessage("Title 3", listOf("Msg 3"))
			)
		val webhookMessage = SlackWebHookMessage(messages)

		val result = converter.convertToSlackMessage(webhookMessage)

		assertEquals(3, result.getSlackMessages().size)
		assertEquals(messages, result.getSlackMessages())
	}

	@Test
	@DisplayName("should preserve message order")
	fun should_preserve_message_order() {
		val contents = listOf("First", "Second", "Third", "Fourth")
		val commonMessage = CommonWebHookMessage("Ordered", contents)

		val result = converter.convertCommonToSlackMessage(commonMessage)

		val slackMessage = result.getSlackMessages()[0]
		assertEquals(contents, slackMessage.messages)
	}
}
