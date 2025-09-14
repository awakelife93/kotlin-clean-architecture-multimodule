package com.example.demo.mockito.webhook.service

import com.example.demo.webhook.common.CommonWebHookMessage
import com.example.demo.webhook.common.WebHookSender
import com.example.demo.webhook.constant.WebHookTarget
import com.example.demo.webhook.converter.WebHookMessageConverter
import com.example.demo.webhook.discord.model.DiscordWebHookMessage
import com.example.demo.webhook.router.WebHookRouter
import com.example.demo.webhook.service.WebHookService
import com.example.demo.webhook.slack.model.SlackWebHookMessage
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@Tag("mockito-unit-test")
@DisplayName("Mockito Unit - WebHook Service Test")
@ExtendWith(MockitoExtension::class)
class WebHookServiceTests {
	@Mock
	private lateinit var webhookRouter: WebHookRouter

	@Mock
	private lateinit var webhookMessageConverter: WebHookMessageConverter

	@Mock
	private lateinit var slackSender: WebHookSender

	@Mock
	private lateinit var discordSender: WebHookSender

	private val service: WebHookService by lazy {
		WebHookService(webhookRouter, webhookMessageConverter, true)
	}

	@Test
	@DisplayName("should send to all webhooks when sendAll is called")
	fun should_send_to_all_webhooks() {
		whenever(slackSender.target()).thenReturn(WebHookTarget.SLACK)
		whenever(discordSender.target()).thenReturn(WebHookTarget.DISCORD)
		whenever(webhookRouter.all()).thenReturn(listOf(slackSender, discordSender))

		val title = "Test Title"
		val lines = listOf("line1", "line2")
		val slackMessage = SlackWebHookMessage(listOf())
		val discordMessage = DiscordWebHookMessage(listOf())

		whenever(webhookMessageConverter.convert(eq(WebHookTarget.SLACK), any<CommonWebHookMessage>()))
			.thenReturn(slackMessage)
		whenever(webhookMessageConverter.convert(eq(WebHookTarget.DISCORD), any<CommonWebHookMessage>()))
			.thenReturn(discordMessage)

		service.sendAll(title, lines)

		verify(slackSender).send(slackMessage)
		verify(discordSender).send(discordMessage)

		val commonMessageCaptor = argumentCaptor<CommonWebHookMessage>()
		verify(webhookMessageConverter).convert(eq(WebHookTarget.SLACK), commonMessageCaptor.capture())
		verify(webhookMessageConverter).convert(eq(WebHookTarget.DISCORD), commonMessageCaptor.capture())

		val capturedMessages = commonMessageCaptor.allValues
		capturedMessages.forEach { message ->
			assert(message.title == title)
			assert(message.contents == lines)
		}
	}

	@Test
	@DisplayName("should send to Slack only when sendSlack is called")
	fun should_send_to_slack_only() {
		val title = "Slack Test"
		val lines = listOf("slack message")
		val convertedMessage = SlackWebHookMessage(listOf())

		whenever(webhookRouter.route(WebHookTarget.SLACK)).thenReturn(slackSender)
		whenever(webhookMessageConverter.convert(eq(WebHookTarget.SLACK), any<SlackWebHookMessage>()))
			.thenReturn(convertedMessage)

		service.sendSlack(title, lines)

		verify(slackSender).send(convertedMessage)
		verify(discordSender, never()).send(any<SlackWebHookMessage>())

		verify(webhookMessageConverter).convert(
			eq(WebHookTarget.SLACK),
			argThat<SlackWebHookMessage> { message ->
				message.getSlackMessages().isNotEmpty() &&
					message.getSlackMessages()[0].title == title &&
					message.getSlackMessages()[0].messages == lines
			}
		)
	}

	@Test
	@DisplayName("should send to Discord only when sendDiscord is called")
	fun should_send_to_discord_only() {
		val title = "Discord Test"
		val lines = listOf("discord message")
		val convertedMessage = DiscordWebHookMessage(listOf())

		whenever(webhookRouter.route(WebHookTarget.DISCORD)).thenReturn(discordSender)
		whenever(webhookMessageConverter.convert(eq(WebHookTarget.DISCORD), any<DiscordWebHookMessage>()))
			.thenReturn(convertedMessage)

		service.sendDiscord(title, lines)

		verify(discordSender).send(convertedMessage)
		verify(slackSender, never()).send(any<DiscordWebHookMessage>())

		verify(webhookMessageConverter).convert(
			eq(WebHookTarget.DISCORD),
			argThat<DiscordWebHookMessage> { message ->
				message.getDiscordMessages().isNotEmpty() &&
					message.getDiscordMessages()[0].title == title &&
					message.getDiscordMessages()[0].messages == lines
			}
		)
	}

	@Test
	@DisplayName("should not send when webhook is disabled")
	fun should_not_send_when_disabled() {
		val disabledService = WebHookService(webhookRouter, webhookMessageConverter, false)

		disabledService.sendAll("Test", listOf("message"))

		verify(webhookRouter, never()).all()
		verify(slackSender, never()).send(any<SlackWebHookMessage>())
		verify(discordSender, never()).send(any<DiscordWebHookMessage>())
	}
}
