package com.example.demo.mockito.webhook.discord

import com.example.demo.webhook.common.WebHookMessage
import com.example.demo.webhook.constant.WebHookTarget
import com.example.demo.webhook.discord.client.DiscordWebHookClient
import com.example.demo.webhook.discord.model.DiscordEmbed
import com.example.demo.webhook.discord.model.DiscordMessage
import com.example.demo.webhook.discord.model.DiscordWebHookMessage
import com.example.demo.webhook.slack.model.SlackWebHookMessage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@ActiveProfiles("test")
@Tag("mockito-unit-test")
@DisplayName("Mockito Unit - Discord WebHook Client Test")
@ExtendWith(MockitoExtension::class)
class DiscordWebHookClientTests {
	@Mock
	private lateinit var webClient: WebClient

	@Mock
	private lateinit var requestBodyUriSpec: WebClient.RequestBodyUriSpec

	@Mock
	private lateinit var requestBodySpec: WebClient.RequestBodySpec

	@Mock
	private lateinit var responseSpec: WebClient.ResponseSpec

	private val client: DiscordWebHookClient by lazy {
		DiscordWebHookClient("https://discord.webhook.url", webClient)
	}

	@Test
	@DisplayName("should return DISCORD as target")
	fun should_return_discord_as_target() {
		val result = client.target()

		assertEquals(WebHookTarget.DISCORD, result)
	}

	@Nested
	@DisplayName("Sending messages")
	inner class SendingMessagesTests {
		@Test
		@DisplayName("should send DiscordWebHookMessage successfully")
		fun should_send_discord_webhook_message() {
			whenever(webClient.post()).thenReturn(requestBodyUriSpec)
			whenever(requestBodyUriSpec.uri(any<String>())).thenReturn(requestBodySpec)
			whenever(requestBodySpec.bodyValue(any<Map<String, Any>>())).thenReturn(requestBodySpec)
			whenever(requestBodySpec.retrieve()).thenReturn(responseSpec)
			whenever(responseSpec.bodyToMono(String::class.java)).thenReturn(Mono.just("OK"))

			val message =
				DiscordMessage(
					"Test Title",
					mutableListOf("msg1", "msg2"),
					listOf(
						DiscordEmbed(
							title = "Service Alert",
							description = "Service is running",
							color = 0x00FF00
						)
					)
				)
			val discordWebHookMessage = DiscordWebHookMessage(mutableListOf(message))

			client.send(discordWebHookMessage)

			verify(webClient).post()
			verify(requestBodyUriSpec).uri("https://discord.webhook.url")
			verify(requestBodySpec).bodyValue(
				argThat<Map<String, Any>> { payload ->
					payload["content"] != null && payload["embeds"] != null
				}
			)
		}

		@Test
		@DisplayName("should not send when URL is blank")
		fun should_not_send_when_url_is_blank() {
			val clientWithoutUrl = DiscordWebHookClient("", webClient)
			val message = DiscordWebHookMessage(mutableListOf())

			clientWithoutUrl.send(message)

			verify(webClient, never()).post()
		}

		@Test
		@DisplayName("should not send when message is not DiscordWebHookMessage")
		fun should_not_send_invalid_message_type() {
			val invalidMessage =
				object : WebHookMessage {
					override fun getTarget(): String = "INVALID"

					override fun getMessages(): List<String> = emptyList()
				}

			client.send(invalidMessage)

			verify(webClient, never()).post()
		}

		@Test
		@DisplayName("should not send when Slack message is passed")
		fun should_not_send_slack_message() {
			val slackMessage = SlackWebHookMessage(mutableListOf())

			client.send(slackMessage)

			verify(webClient, never()).post()
		}
	}
}
