package com.example.demo.kotest.webhook.service

import com.example.demo.webhook.common.CommonWebHookMessage
import com.example.demo.webhook.common.WebHookMessage
import com.example.demo.webhook.common.WebHookSender
import com.example.demo.webhook.constant.WebHookTarget
import com.example.demo.webhook.converter.WebHookMessageConverter
import com.example.demo.webhook.discord.model.DiscordWebHookMessage
import com.example.demo.webhook.router.WebHookRouter
import com.example.demo.webhook.service.WebHookService
import com.example.demo.webhook.slack.model.SlackWebHookMessage
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.core.test.TestCase
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@Tags("kotest-unit-test")
class WebHookServiceTests : DescribeSpec() {
	private lateinit var webhookRouter: WebHookRouter
	private lateinit var webhookMessageConverter: WebHookMessageConverter
	private lateinit var slackSender: WebHookSender
	private lateinit var discordSender: WebHookSender
	private lateinit var service: WebHookService

	override suspend fun beforeTest(testCase: TestCase) {
		webhookRouter = mockk()
		webhookMessageConverter = mockk()
		slackSender = mockk()
		discordSender = mockk()

		every { slackSender.target() } returns WebHookTarget.SLACK
		every { discordSender.target() } returns WebHookTarget.DISCORD
		every { webhookRouter.all() } returns listOf(slackSender, discordSender)

		service = WebHookService(webhookRouter, webhookMessageConverter, true)
	}

	override suspend fun afterTest(
		testCase: TestCase,
		result: io.kotest.core.test.TestResult
	) {
		clearAllMocks()
	}

	init {
		describe("WebHookService") {
			describe("sendAll") {
				it("should send to all webhooks") {
					val title = "Test Title"
					val lines = listOf("line1", "line2")
					val slackMessage = mockk<SlackWebHookMessage>()
					val discordMessage = mockk<DiscordWebHookMessage>()

					every { webhookMessageConverter.convert(WebHookTarget.SLACK, any<CommonWebHookMessage>()) } returns slackMessage
					every { webhookMessageConverter.convert(WebHookTarget.DISCORD, any<CommonWebHookMessage>()) } returns discordMessage
					every { slackSender.send(slackMessage) } just runs
					every { discordSender.send(discordMessage) } just runs

					service.sendAll(title, lines)

					verify { slackSender.send(slackMessage) }
					verify { discordSender.send(discordMessage) }
				}
			}

			describe("sendSlack") {
				it("should send to Slack only") {
					val title = "Slack Test"
					val lines = listOf("slack message")
					val convertedMessage = mockk<SlackWebHookMessage>()

					every { webhookRouter.route(WebHookTarget.SLACK) } returns slackSender
					every { webhookMessageConverter.convert(WebHookTarget.SLACK, any<SlackWebHookMessage>()) } returns convertedMessage
					every { slackSender.send(convertedMessage) } just runs

					service.sendSlack(title, lines)

					verify { slackSender.send(convertedMessage) }
					verify(exactly = 0) { discordSender.send(any<WebHookMessage>()) }
				}
			}

			describe("sendDiscord") {
				it("should send to Discord only") {
					val title = "Discord Test"
					val lines = listOf("discord message")
					val convertedMessage = mockk<DiscordWebHookMessage>()

					every { webhookRouter.route(WebHookTarget.DISCORD) } returns discordSender
					every { webhookMessageConverter.convert(WebHookTarget.DISCORD, any<DiscordWebHookMessage>()) } returns convertedMessage
					every { discordSender.send(convertedMessage) } just runs

					service.sendDiscord(title, lines)

					verify { discordSender.send(convertedMessage) }
					verify(exactly = 0) { slackSender.send(any<WebHookMessage>()) }
				}
			}

			describe("when webhook is disabled") {
				it("should not send any messages") {
					val disabledService = WebHookService(webhookRouter, webhookMessageConverter, false)

					disabledService.sendAll("Test", listOf("message"))

					verify(exactly = 0) { slackSender.send(any<WebHookMessage>()) }
					verify(exactly = 0) { discordSender.send(any<WebHookMessage>()) }
				}
			}
		}
	}
}
