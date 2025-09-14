package com.example.demo.kotest.webhook.discord

import com.example.demo.webhook.common.CommonWebHookMessage
import com.example.demo.webhook.discord.converter.DiscordMessageConverter
import com.example.demo.webhook.discord.model.DiscordEmbed
import com.example.demo.webhook.discord.model.DiscordEmbedField
import com.example.demo.webhook.discord.model.DiscordMessage
import com.example.demo.webhook.discord.model.DiscordWebHookMessage
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@Tags("kotest-unit-test")
class DiscordMessageConverterTests :
	DescribeSpec({
		describe("Discord message converter test") {
			val discordMessageConverter = DiscordMessageConverter()

			context("convertCommonToDiscordMessage method test") {
				it("should convert CommonWebHookMessage to DiscordWebHookMessage") {
					val commonMessage =
						CommonWebHookMessage(
							title = "Test Title",
							contents = listOf("message1", "message2")
						)

					val result = discordMessageConverter.convertCommonToDiscordMessage(commonMessage)

					result.getDiscordMessages().size shouldBe 1
					result.getDiscordMessages()[0].title shouldBe "Test Title"
					result.getDiscordMessages()[0].messages shouldBe listOf("message1", "message2")
				}
			}

			context("convertToDiscordMessage method test") {
				it("should return DiscordWebHookMessage as is") {
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

					result.getDiscordMessages().size shouldBe 1
					result.getDiscordMessages()[0].title shouldBe "Test Title"
				}

				it("should handle DiscordWebHookMessage with empty embeds") {
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

					result.getDiscordMessages().size shouldBe 1
					result.getDiscordMessages()[0].title shouldBe "Test Title"
				}

				it("should handle empty message list") {
					val discordMessage =
						DiscordWebHookMessage(
							messages = emptyList()
						)

					val result = discordMessageConverter.convertToDiscordMessage(discordMessage)

					result.getDiscordMessages().isEmpty() shouldBe true
				}

				it("should handle multiple messages") {
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

					result.getDiscordMessages().size shouldBe 2
					result.getDiscordMessages()[0].title shouldBe "Title1"
					result.getDiscordMessages()[1].title shouldBe "Title2"
				}
			}
		}
	})
