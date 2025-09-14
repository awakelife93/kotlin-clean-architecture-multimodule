package com.example.demo.kotest.webhook.router

import com.example.demo.webhook.common.WebHookSender
import com.example.demo.webhook.constant.WebHookTarget
import com.example.demo.webhook.router.WebHookRouter
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@Tags("kotest-unit-test")
class WebHookRouterTests :
	DescribeSpec({
		describe("WebHook router test") {
			val mockSlackSender = mockk<WebHookSender>()
			val mockDiscordSender = mockk<WebHookSender>()

			beforeTest {
				every { mockSlackSender.target() } returns WebHookTarget.SLACK
				every { mockDiscordSender.target() } returns WebHookTarget.DISCORD
			}

			context("route method test") {
				it("should route to Slack target") {
					val router = WebHookRouter(listOf(mockSlackSender, mockDiscordSender))

					val result = router.route(WebHookTarget.SLACK)

					result shouldNotBe null
					result shouldBe mockSlackSender
				}

				it("should route to Discord target") {
					val router = WebHookRouter(listOf(mockSlackSender, mockDiscordSender))

					val result = router.route(WebHookTarget.DISCORD)

					result shouldNotBe null
					result shouldBe mockDiscordSender
				}

				it("should throw exception for ALL target") {
					val router = WebHookRouter(listOf(mockSlackSender, mockDiscordSender))

					val exception =
						shouldThrow<IllegalArgumentException> {
							router.route(WebHookTarget.ALL)
						}
					exception.message shouldBe "Cannot route to ALL target"
				}

				it("should return null when no sender matches") {
					val router = WebHookRouter(emptyList())

					val result = router.route(WebHookTarget.SLACK)

					result shouldBe null
				}

				it("should handle single sender") {
					val router = WebHookRouter(listOf(mockSlackSender))

					val slackResult = router.route(WebHookTarget.SLACK)
					val discordResult = router.route(WebHookTarget.DISCORD)

					slackResult shouldBe mockSlackSender
					discordResult shouldBe null
				}
			}

			context("all method test") {
				it("should return all registered senders") {
					val router = WebHookRouter(listOf(mockSlackSender, mockDiscordSender))

					val result = router.all()

					result shouldHaveSize 2
					result shouldContain mockSlackSender
					result shouldContain mockDiscordSender
				}

				it("should return empty list when no senders") {
					val router = WebHookRouter(emptyList())

					val result = router.all()

					result shouldHaveSize 0
				}

				it("should handle single sender") {
					val router = WebHookRouter(listOf(mockDiscordSender))

					val result = router.all()

					result shouldHaveSize 1
					result[0] shouldBe mockDiscordSender
				}

				it("should handle multiple senders with same target") {
					val anotherSlackSender = mockk<WebHookSender>()
					every { anotherSlackSender.target() } returns WebHookTarget.SLACK

					val router = WebHookRouter(listOf(mockSlackSender, anotherSlackSender, mockDiscordSender))

					val allSenders = router.all()
					val slackSender = router.route(WebHookTarget.SLACK)

					allSenders shouldHaveSize 3
					slackSender shouldBe mockSlackSender
				}
			}
		}
	})
