package com.example.demo.kotest.webhook.slack

import com.example.demo.webhook.common.WebHookMessage
import com.example.demo.webhook.constant.WebHookTarget
import com.example.demo.webhook.slack.client.SlackWebHookClient
import com.example.demo.webhook.slack.model.SlackMessage
import com.example.demo.webhook.slack.model.SlackWebHookMessage
import com.slack.api.Slack
import com.slack.api.webhook.Payload
import com.slack.api.webhook.WebhookResponse
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@Tags("kotest-unit-test")
class SlackWebHookClientTests :
	DescribeSpec({
		describe("SlackWebHookClient test") {
			lateinit var slackClient: Slack
			lateinit var client: SlackWebHookClient

			beforeTest {
				slackClient = mockk()
				mockkStatic(Slack::class)
				every { Slack.getInstance() } returns slackClient
			}

			afterTest {
				unmockkAll()
			}

			context("target method test") {
				it("should return SLACK target") {
					client = SlackWebHookClient("https://slack.webhook.url")
					val result = client.target()
					result shouldBe WebHookTarget.SLACK
				}
			}

			context("send method test") {
				it("should send SlackWebHookMessage successfully") {
					val response = mockk<WebhookResponse>()
					every { response.code } returns 200
					every { response.message } returns "ok"
					every { slackClient.send(any<String>(), any<Payload>()) } returns response

					val message = SlackMessage("Test Title", listOf("msg1", "msg2"))
					val slackWebHookMessage = SlackWebHookMessage(listOf(message))

					client = SlackWebHookClient("https://slack.webhook.url")

					client.send(slackWebHookMessage)

					verify { slackClient.send(eq("https://slack.webhook.url"), any<Payload>()) }
				}

				it("should not send when URL is blank") {
					val message = SlackWebHookMessage(emptyList())
					client = SlackWebHookClient("")

					client.send(message)

					verify(exactly = 0) { slackClient.send(any<String>(), any<Payload>()) }
				}

				it("should throw exception when response code is not 200") {
					val response = mockk<WebhookResponse>()
					every { response.code } returns 400
					every { response.message } returns "bad request"
					every { slackClient.send(any<String>(), any<Payload>()) } returns response

					val message = SlackMessage("Test", listOf("msg"))
					val slackWebHookMessage = SlackWebHookMessage(listOf(message))

					client = SlackWebHookClient("https://slack.webhook.url")

					shouldThrow<IllegalArgumentException> {
						client.send(slackWebHookMessage)
					}
				}

				it("should handle multiple messages") {
					val response = mockk<WebhookResponse>()
					every { response.code } returns 200
					every { response.message } returns "ok"
					every { slackClient.send(any<String>(), any<Payload>()) } returns response

					val messages =
						listOf(
							SlackMessage("Title 1", listOf("msg1")),
							SlackMessage("Title 2", listOf("msg2", "msg3")),
							SlackMessage("Title 3", emptyList())
						)
					val slackWebHookMessage =
						SlackWebHookMessage(messages)

					client = SlackWebHookClient("https://slack.webhook.url")

					client.send(slackWebHookMessage)

					verify { slackClient.send(eq("https://slack.webhook.url"), any<Payload>()) }
				}

				it("should handle empty message list") {
					val response = mockk<WebhookResponse>()
					every { response.code } returns 200
					every { response.message } returns "ok"
					every { slackClient.send(any<String>(), any<Payload>()) } returns response

					val slackWebHookMessage = SlackWebHookMessage(emptyList())

					client = SlackWebHookClient("https://slack.webhook.url")

					client.send(slackWebHookMessage)

					verify { slackClient.send(eq("https://slack.webhook.url"), any<Payload>()) }
				}

				it("should handle wrong message type") {
					val wrongMessage = mockk<WebHookMessage>()

					client = SlackWebHookClient("https://slack.webhook.url")

					client.send(wrongMessage)

					verify(exactly = 0) { slackClient.send(any<String>(), any<Payload>()) }
				}

				it("should propagate exception when slack client throws exception") {
					every { slackClient.send(any<String>(), any<Payload>()) } throws RuntimeException("Network error")

					val message = SlackMessage("Test", listOf("msg"))
					val slackWebHookMessage = SlackWebHookMessage(listOf(message))

					client = SlackWebHookClient("https://slack.webhook.url")

					shouldThrow<RuntimeException> {
						client.send(slackWebHookMessage)
					}.message shouldBe "Network error"
				}
			}
		}
	})
