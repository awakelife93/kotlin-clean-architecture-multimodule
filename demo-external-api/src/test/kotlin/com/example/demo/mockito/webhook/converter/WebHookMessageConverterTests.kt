package com.example.demo.mockito.webhook.converter

import com.example.demo.webhook.common.CommonWebHookMessage
import com.example.demo.webhook.constant.WebHookTarget
import com.example.demo.webhook.converter.WebHookMessageConverter
import com.example.demo.webhook.discord.converter.DiscordMessageConverter
import com.example.demo.webhook.discord.model.DiscordMessage
import com.example.demo.webhook.discord.model.DiscordWebHookMessage
import com.example.demo.webhook.slack.converter.SlackMessageConverter
import com.example.demo.webhook.slack.model.SlackMessage
import com.example.demo.webhook.slack.model.SlackWebHookMessage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@Tag("mockito-unit-test")
@DisplayName("Mockito Unit - WebHookMessageConverter Test")
@ExtendWith(MockitoExtension::class)
class WebHookMessageConverterTests {
	@Mock
	private lateinit var slackMessageConverter: SlackMessageConverter

	@Mock
	private lateinit var discordMessageConverter: DiscordMessageConverter

	@InjectMocks
	private lateinit var converter: WebHookMessageConverter

	@Nested
	@DisplayName("Convert for Slack Tests")
	inner class ConvertForSlack {
		@Test
		@DisplayName("should convert SlackWebHookMessage")
		fun shouldConvertSlackWebHookMessage() {
			val slackMessage =
				SlackWebHookMessage(
					mutableListOf(SlackMessage("Title", listOf("message")))
				)
			val expectedMessage =
				SlackWebHookMessage(
					mutableListOf(SlackMessage("Converted", listOf("converted")))
				)

			`when`(slackMessageConverter.convertToSlackMessage(slackMessage))
				.thenReturn(expectedMessage)

			val result = converter.convert(WebHookTarget.SLACK, slackMessage)

			assertEquals(expectedMessage, result)
			verify(slackMessageConverter).convertToSlackMessage(slackMessage)
		}

		@Test
		@DisplayName("should convert CommonWebHookMessage for Slack")
		fun shouldConvertCommonWebHookMessageForSlack() {
			val commonMessage = CommonWebHookMessage("Title", listOf("message"))
			val expectedMessage =
				SlackWebHookMessage(
					mutableListOf(SlackMessage("Title", listOf("message")))
				)

			`when`(slackMessageConverter.convertCommonToSlackMessage(commonMessage))
				.thenReturn(expectedMessage)

			val result = converter.convert(WebHookTarget.SLACK, commonMessage)

			assertEquals(expectedMessage, result)
			verify(slackMessageConverter).convertCommonToSlackMessage(commonMessage)
		}

		@Test
		@DisplayName("should throw exception for unsupported message type")
		fun shouldThrowExceptionForUnsupportedMessageType() {
			val discordMessage = DiscordWebHookMessage(mutableListOf())

			val exception =
				assertThrows(IllegalArgumentException::class.java) {
					converter.convert(WebHookTarget.SLACK, discordMessage)
				}

			assertEquals("Unsupported message type: ${discordMessage::class} for Slack", exception.message)
		}
	}

	@Nested
	@DisplayName("Convert for Discord Tests")
	inner class ConvertForDiscord {
		@Test
		@DisplayName("should convert DiscordWebHookMessage")
		fun shouldConvertDiscordWebHookMessage() {
			val discordMessage =
				DiscordWebHookMessage(
					mutableListOf(DiscordMessage("Title", listOf("message"), null))
				)
			val expectedMessage =
				DiscordWebHookMessage(
					mutableListOf(DiscordMessage("Converted", listOf("converted"), null))
				)

			`when`(discordMessageConverter.convertToDiscordMessage(discordMessage))
				.thenReturn(expectedMessage)

			val result = converter.convert(WebHookTarget.DISCORD, discordMessage)

			assertEquals(expectedMessage, result)
			verify(discordMessageConverter).convertToDiscordMessage(discordMessage)
		}

		@Test
		@DisplayName("should convert CommonWebHookMessage for Discord")
		fun shouldConvertCommonWebHookMessageForDiscord() {
			val commonMessage = CommonWebHookMessage("Title", listOf("message"))
			val expectedMessage =
				DiscordWebHookMessage(
					mutableListOf(DiscordMessage("Title", listOf("message"), null))
				)

			`when`(discordMessageConverter.convertCommonToDiscordMessage(commonMessage))
				.thenReturn(expectedMessage)

			val result = converter.convert(WebHookTarget.DISCORD, commonMessage)

			assertEquals(expectedMessage, result)
			verify(discordMessageConverter).convertCommonToDiscordMessage(commonMessage)
		}

		@Test
		@DisplayName("should throw exception for unsupported message type")
		fun shouldThrowExceptionForUnsupportedMessageType() {
			val slackMessage = SlackWebHookMessage(mutableListOf())

			val exception =
				assertThrows(IllegalArgumentException::class.java) {
					converter.convert(WebHookTarget.DISCORD, slackMessage)
				}

			assertEquals("Unsupported message type: ${slackMessage::class} for Discord", exception.message)
		}
	}

	@Nested
	@DisplayName("Convert for ALL Target Tests")
	inner class ConvertForAllTarget {
		@Test
		@DisplayName("should throw exception for ALL target")
		fun shouldThrowExceptionForAllTarget() {
			val message = CommonWebHookMessage("Title", listOf("message"))

			val exception =
				assertThrows(IllegalArgumentException::class.java) {
					converter.convert(WebHookTarget.ALL, message)
				}

			assertEquals("Cannot convert for ALL target", exception.message)
		}
	}
}
