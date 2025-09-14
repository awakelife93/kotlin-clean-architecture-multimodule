package com.example.demo.kotest.webhook.slack

import com.example.demo.webhook.common.CommonWebHookMessage
import com.example.demo.webhook.slack.converter.SlackMessageConverter
import com.example.demo.webhook.slack.model.SlackMessage
import com.example.demo.webhook.slack.model.SlackWebHookMessage
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@Tags("kotest-unit-test")
class SlackMessageConverterTests :
	DescribeSpec({
		describe("Slack message converter test") {
			val slackMessageConverter = SlackMessageConverter()

			context("convertCommonToSlackMessage method test") {
				it("should convert CommonWebHookMessage to SlackWebHookMessage") {
					val commonMessage =
						CommonWebHookMessage(
							title = "Test Title",
							contents = listOf("message1", "message2")
						)

					val result = slackMessageConverter.convertCommonToSlackMessage(commonMessage)

					result.getSlackMessages().size shouldBe 1
					result.getSlackMessages()[0].title shouldBe "Test Title"
					result.getSlackMessages()[0].messages shouldBe listOf("message1", "message2")
				}

				it("should handle empty contents in CommonWebHookMessage") {
					val commonMessage =
						CommonWebHookMessage(
							title = "Empty Content",
							contents = emptyList()
						)

					val result = slackMessageConverter.convertCommonToSlackMessage(commonMessage)

					result.getSlackMessages().size shouldBe 1
					result.getSlackMessages()[0].title shouldBe "Empty Content"
					result.getSlackMessages()[0].messages shouldBe emptyList()
				}

				it("should convert multi-line contents message") {
					val contents = listOf("Line 1", "Line 2", "Line 3", "Line 4")
					val commonMessage =
						CommonWebHookMessage(
							title = "Multi-line Message",
							contents = contents
						)

					val result = slackMessageConverter.convertCommonToSlackMessage(commonMessage)

					result.getSlackMessages().size shouldBe 1
					result.getSlackMessages()[0].messages shouldBe contents
				}
			}

			context("convertToSlackMessage method test") {
				it("should return SlackWebHookMessage as is") {
					val message =
						SlackMessage(
							title = "Test Title",
							messages = listOf("message1", "message2")
						)

					val slackMessage =
						SlackWebHookMessage(
							messages = listOf(message)
						)

					val result = slackMessageConverter.convertToSlackMessage(slackMessage)

					result shouldBe slackMessage
					result.getSlackMessages().size shouldBe 1
					result.getSlackMessages()[0] shouldBe message
				}

				it("should handle empty message list") {
					val slackMessage =
						SlackWebHookMessage(
							messages = emptyList()
						)

					val result = slackMessageConverter.convertToSlackMessage(slackMessage)

					result shouldBe slackMessage
					result.getSlackMessages().isEmpty() shouldBe true
				}

				it("should handle multiple messages") {
					val message1 =
						SlackMessage(
							title = "Title1",
							messages = listOf("msg1")
						)
					val message2 =
						SlackMessage(
							title = "Title2",
							messages = listOf("msg2", "msg3")
						)
					val message3 =
						SlackMessage(
							title = "Title3",
							messages = listOf("msg4")
						)

					val slackMessage =
						SlackWebHookMessage(
							messages = listOf(message1, message2, message3)
						)

					val result = slackMessageConverter.convertToSlackMessage(slackMessage)

					result shouldBe slackMessage
					result.getSlackMessages().size shouldBe 3
					result.getSlackMessages() shouldBe listOf(message1, message2, message3)
				}

				it("should preserve message order") {
					val messages =
						(1..5).map {
							SlackMessage("Title $it", listOf("Message $it"))
						}

					val slackMessage =
						SlackWebHookMessage(messages)

					val result = slackMessageConverter.convertToSlackMessage(slackMessage)

					result.getSlackMessages() shouldBe messages
				}
			}
		}
	})
