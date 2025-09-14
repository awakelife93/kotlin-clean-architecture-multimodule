package com.example.demo.kotest.webhook.converter

import com.example.demo.webhook.common.CommonWebHookMessage
import com.example.demo.webhook.constant.WebHookTarget
import com.example.demo.webhook.converter.WebHookMessageConverter
import com.example.demo.webhook.discord.converter.DiscordMessageConverter
import com.example.demo.webhook.discord.model.DiscordMessage
import com.example.demo.webhook.discord.model.DiscordWebHookMessage
import com.example.demo.webhook.slack.converter.SlackMessageConverter
import com.example.demo.webhook.slack.model.SlackMessage
import com.example.demo.webhook.slack.model.SlackWebHookMessage
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@Tags("kotest-unit-test")
class WebHookMessageConverterTests :
	DescribeSpec({
		describe("WebHookMessageConverter") {
			val slackMessageConverter = mockk<SlackMessageConverter>()
			val discordMessageConverter = mockk<DiscordMessageConverter>()
			val converter = WebHookMessageConverter(slackMessageConverter, discordMessageConverter)

			describe("convert for Slack") {
				it("should convert SlackWebHookMessage") {
					val slackMessage =
						SlackWebHookMessage(
							mutableListOf(SlackMessage("Title", listOf("message")))
						)
					val expectedMessage =
						SlackWebHookMessage(
							mutableListOf(SlackMessage("Converted", listOf("converted")))
						)

					every { slackMessageConverter.convertToSlackMessage(slackMessage) } returns expectedMessage

					val result = converter.convert(WebHookTarget.SLACK, slackMessage)

					result shouldBe expectedMessage
					verify { slackMessageConverter.convertToSlackMessage(slackMessage) }
				}

				it("should convert CommonWebHookMessage for Slack") {
					val commonMessage = CommonWebHookMessage("Title", listOf("message"))
					val expectedMessage =
						SlackWebHookMessage(
							mutableListOf(SlackMessage("Title", listOf("message")))
						)

					every { slackMessageConverter.convertCommonToSlackMessage(commonMessage) } returns expectedMessage

					val result = converter.convert(WebHookTarget.SLACK, commonMessage)

					result shouldBe expectedMessage
					verify { slackMessageConverter.convertCommonToSlackMessage(commonMessage) }
				}

				it("should throw exception for unsupported message type") {
					val discordMessage = DiscordWebHookMessage(mutableListOf())

					val exception =
						shouldThrow<IllegalArgumentException> {
							converter.convert(WebHookTarget.SLACK, discordMessage)
						}

					exception.message shouldBe "Unsupported message type: ${discordMessage::class} for Slack"
				}
			}

			describe("convert for Discord") {
				it("should convert DiscordWebHookMessage") {
					val discordMessage =
						DiscordWebHookMessage(
							mutableListOf(DiscordMessage("Title", listOf("message"), null))
						)
					val expectedMessage =
						DiscordWebHookMessage(
							mutableListOf(DiscordMessage("Converted", listOf("converted"), null))
						)

					every { discordMessageConverter.convertToDiscordMessage(discordMessage) } returns expectedMessage

					val result = converter.convert(WebHookTarget.DISCORD, discordMessage)

					result shouldBe expectedMessage
					verify { discordMessageConverter.convertToDiscordMessage(discordMessage) }
				}

				it("should convert CommonWebHookMessage for Discord") {
					val commonMessage = CommonWebHookMessage("Title", listOf("message"))
					val expectedMessage =
						DiscordWebHookMessage(
							mutableListOf(DiscordMessage("Title", listOf("message"), null))
						)

					every { discordMessageConverter.convertCommonToDiscordMessage(commonMessage) } returns expectedMessage

					val result = converter.convert(WebHookTarget.DISCORD, commonMessage)

					result shouldBe expectedMessage
					verify { discordMessageConverter.convertCommonToDiscordMessage(commonMessage) }
				}

				it("should throw exception for unsupported message type") {
					val slackMessage = SlackWebHookMessage(mutableListOf())

					val exception =
						shouldThrow<IllegalArgumentException> {
							converter.convert(WebHookTarget.DISCORD, slackMessage)
						}

					exception.message shouldBe "Unsupported message type: ${slackMessage::class} for Discord"
				}
			}

			describe("convert for ALL target") {
				it("should throw exception") {
					val message = CommonWebHookMessage("Title", listOf("message"))

					val exception =
						shouldThrow<IllegalArgumentException> {
							converter.convert(WebHookTarget.ALL, message)
						}

					exception.message shouldBe "Cannot convert for ALL target"
				}
			}
		}
	})
