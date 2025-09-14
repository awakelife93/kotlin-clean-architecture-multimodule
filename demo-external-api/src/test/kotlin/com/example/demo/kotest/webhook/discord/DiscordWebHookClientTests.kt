package com.example.demo.kotest.webhook.discord

import com.example.demo.webhook.common.WebHookMessage
import com.example.demo.webhook.constant.WebHookTarget
import com.example.demo.webhook.discord.client.DiscordWebHookClient
import com.example.demo.webhook.discord.model.DiscordEmbed
import com.example.demo.webhook.discord.model.DiscordMessage
import com.example.demo.webhook.discord.model.DiscordWebHookMessage
import com.example.demo.webhook.slack.model.SlackWebHookMessage
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.core.test.TestCase
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@ActiveProfiles("test")
@Tags("kotest-unit-test")
class DiscordWebHookClientTests : DescribeSpec() {
	private lateinit var webClient: WebClient
	private lateinit var requestBodyUriSpec: WebClient.RequestBodyUriSpec
	private lateinit var requestBodySpec: WebClient.RequestBodySpec
	private lateinit var responseSpec: WebClient.ResponseSpec
	private lateinit var client: DiscordWebHookClient

	override suspend fun beforeTest(testCase: TestCase) {
		webClient = mockk()
		requestBodyUriSpec = mockk()
		requestBodySpec = mockk()
		responseSpec = mockk()

		every { webClient.post() } returns requestBodyUriSpec
		every { requestBodyUriSpec.uri(any<String>()) } returns requestBodySpec
		every { requestBodySpec.bodyValue(any<Map<String, Any>>()) } returns requestBodySpec
		every { requestBodySpec.retrieve() } returns responseSpec
		every { responseSpec.bodyToMono(String::class.java) } returns Mono.just("OK")

		client = DiscordWebHookClient("https://discord.webhook.url", webClient)
	}

	override suspend fun afterTest(
		testCase: TestCase,
		result: io.kotest.core.test.TestResult
	) {
		clearAllMocks()
	}

	init {
		describe("DiscordWebHookClient") {
			it("should return DISCORD as target") {
				val result = client.target()
				result shouldBe WebHookTarget.DISCORD
			}

			describe("when sending messages") {
				it("should send DiscordWebHookMessage successfully") {
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

					verify { webClient.post() }
					verify { requestBodyUriSpec.uri("https://discord.webhook.url") }
					verify {
						requestBodySpec.bodyValue(
							match<Map<String, Any>> {
								it["content"] != null && it["embeds"] != null
							}
						)
					}
				}

				it("should not send when URL is blank") {
					val clientWithoutUrl = DiscordWebHookClient("", webClient)
					val message = DiscordWebHookMessage(mutableListOf())

					clientWithoutUrl.send(message)

					verify(exactly = 0) { webClient.post() }
				}

				it("should not send when message is not DiscordWebHookMessage") {
					val invalidMessage =
						object : WebHookMessage {
							override fun getTarget(): String = "INVALID"

							override fun getMessages(): List<String> = emptyList()
						}

					client.send(invalidMessage)

					verify(exactly = 0) { webClient.post() }
				}

				it("should not send when Slack message is passed") {
					val slackMessage = SlackWebHookMessage(mutableListOf())

					client.send(slackMessage)

					verify(exactly = 0) { webClient.post() }
				}
			}
		}
	}
}
