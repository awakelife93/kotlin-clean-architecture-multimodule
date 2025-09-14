package com.example.demo.mockito.webhook.discord

import com.example.demo.webhook.common.CommonWebHookMessage
import com.example.demo.webhook.discord.converter.DiscordMessageConverter
import com.example.demo.webhook.discord.model.DiscordEmbed
import com.example.demo.webhook.discord.model.DiscordEmbedField
import com.example.demo.webhook.discord.model.DiscordMessage
import com.example.demo.webhook.discord.model.DiscordWebHookMessage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@Tag("mockito-unit-test")
@DisplayName("Mockito Unit - Discord Message Converter Test")
@ExtendWith(MockitoExtension::class)
class DiscordMessageConverterTests {
	private lateinit var discordMessageConverter: DiscordMessageConverter

	@BeforeEach
	fun setUp() {
		discordMessageConverter = DiscordMessageConverter()
	}

	@Test
	@DisplayName("should convert CommonWebHookMessage to DiscordWebHookMessage")
	fun testConvertCommonToDiscordMessage() {
		val commonMessage =
			CommonWebHookMessage(
				title = "Test Title",
				contents = listOf("message1", "message2")
			)

		val result = discordMessageConverter.convertCommonToDiscordMessage(commonMessage)

		assertEquals(1, result.getDiscordMessages().size)
		assertEquals("Test Title", result.getDiscordMessages()[0].title)
		assertEquals(listOf("message1", "message2"), result.getDiscordMessages()[0].messages)
	}

	@Test
	@DisplayName("should return DiscordWebHookMessage as is")
	fun testConvertToDiscordMessage() {
		val embed =
			DiscordEmbed(
				title = "Test Title",
				description = "Test Description",
				color = 123456,
				fields =
					listOf(
						DiscordEmbedField(name = "Field1", value = "Value1", inline = false)
					)
			)

		val message =
			DiscordMessage(
				title = "Test Title",
				messages = listOf("message1"),
				embeds = listOf(embed)
			)

		val discordMessage =
			DiscordWebHookMessage(
				messages = listOf(message)
			)

		val result = discordMessageConverter.convertToDiscordMessage(discordMessage)

		assertEquals(1, result.getDiscordMessages().size)
		assertEquals("Test Title", result.getDiscordMessages()[0].title)
	}

	@Test
	@DisplayName("should handle DiscordWebHookMessage with empty embeds")
	fun testConvertToDiscordMessageWithEmptyMessage() {
		val message =
			DiscordMessage(
				title = "Test Title",
				messages = listOf("message1")
			)

		val discordMessage =
			DiscordWebHookMessage(
				messages = listOf(message)
			)

		val result = discordMessageConverter.convertToDiscordMessage(discordMessage)

		assertEquals(1, result.getDiscordMessages().size)
		assertEquals("Test Title", result.getDiscordMessages()[0].title)
	}

	@Test
	@DisplayName("should handle empty message list")
	fun testConvertEmptyMessageList() {
		val discordMessage =
			DiscordWebHookMessage(
				messages = emptyList()
			)

		val result = discordMessageConverter.convertToDiscordMessage(discordMessage)

		assertEquals(0, result.getDiscordMessages().size)
	}

	@Test
	@DisplayName("should handle multiple messages")
	fun testConvertMultipleMessages() {
		val message1 =
			DiscordMessage(
				title = "Title1",
				messages = listOf("msg1")
			)
		val message2 =
			DiscordMessage(
				title = "Title2",
				messages = listOf("msg2")
			)

		val discordMessage =
			DiscordWebHookMessage(
				messages = listOf(message1, message2)
			)

		val result = discordMessageConverter.convertToDiscordMessage(discordMessage)

		assertEquals(2, result.getDiscordMessages().size)
		assertEquals("Title1", result.getDiscordMessages()[0].title)
		assertEquals("Title2", result.getDiscordMessages()[1].title)
	}
}
