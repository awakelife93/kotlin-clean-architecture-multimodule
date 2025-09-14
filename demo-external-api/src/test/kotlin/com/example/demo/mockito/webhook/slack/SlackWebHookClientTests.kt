package com.example.demo.mockito.webhook.slack

import com.example.demo.webhook.constant.WebHookTarget
import com.example.demo.webhook.slack.client.SlackWebHookClient
import com.example.demo.webhook.slack.model.SlackMessage
import com.example.demo.webhook.slack.model.SlackWebHookMessage
import com.slack.api.Slack
import com.slack.api.webhook.Payload
import com.slack.api.webhook.WebhookResponse
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@Tag("mockito-unit-test")
@DisplayName("Mockito Unit - Slack WebHook Client Test")
@ExtendWith(MockitoExtension::class)
class SlackWebHookClientTests {
	@Mock
	private lateinit var slackClient: Slack

	@Mock
	private lateinit var response: WebhookResponse

	@Mock
	private lateinit var wrongMessage: com.example.demo.webhook.common.WebHookMessage

	private lateinit var slackStatic: MockedStatic<Slack>

	private val client: SlackWebHookClient by lazy {
		SlackWebHookClient("https://slack.webhook.url")
	}

	@BeforeEach
	fun setup() {
		slackStatic = Mockito.mockStatic(Slack::class.java)
		slackStatic.`when`<Slack> { Slack.getInstance() }.thenReturn(slackClient)
	}

	@AfterEach
	fun tearDown() {
		slackStatic.close()
	}

	@Test
	@DisplayName("should return SLACK as target")
	fun should_return_slack_as_target() {
		val result = client.target()

		assertEquals(WebHookTarget.SLACK, result)
	}

	@Test
	@DisplayName("should send SlackWebHookMessage successfully")
	fun should_send_slack_webhook_message() {
		whenever(response.code).thenReturn(200)
		whenever(slackClient.send(any<String>(), any<Payload>())).thenReturn(response)

		val message = SlackMessage("Test Title", listOf("msg1", "msg2"))
		val slackWebHookMessage = SlackWebHookMessage(listOf(message))

		client.send(slackWebHookMessage)

		verify(slackClient).send(eq("https://slack.webhook.url"), any<Payload>())
	}

	@Test
	@DisplayName("should not send when URL is blank")
	fun should_not_send_when_url_is_blank() {
		val clientWithoutUrl = SlackWebHookClient("")
		val message = SlackWebHookMessage(emptyList())

		clientWithoutUrl.send(message)

		verify(slackClient, never()).send(any<String>(), any<Payload>())
	}

	@Test
	@DisplayName("should throw exception when response code is not 200")
	fun should_throw_exception_when_response_not_ok() {
		whenever(response.code).thenReturn(400)
		whenever(response.message).thenReturn("Bad Request")
		whenever(slackClient.send(any<String>(), any<Payload>())).thenReturn(response)

		val message = SlackMessage("Test Title", listOf("msg1"))
		val slackWebHookMessage = SlackWebHookMessage(listOf(message))

		assertThrows(IllegalArgumentException::class.java) {
			client.send(slackWebHookMessage)
		}
	}

	@Test
	@DisplayName("should handle multiple messages")
	fun should_handle_multiple_messages() {
		whenever(response.code).thenReturn(200)
		whenever(slackClient.send(any<String>(), any<Payload>())).thenReturn(response)

		val messages =
			listOf(
				SlackMessage("Title 1", listOf("msg1", "msg2")),
				SlackMessage("Title 2", listOf("msg3")),
				SlackMessage("Title 3", emptyList())
			)
		val slackWebHookMessage =
			SlackWebHookMessage(messages)

		client.send(slackWebHookMessage)

		verify(slackClient).send(eq("https://slack.webhook.url"), any<Payload>())
	}

	@Test
	@DisplayName("should handle empty message list")
	fun should_handle_empty_message_list() {
		whenever(response.code).thenReturn(200)
		whenever(slackClient.send(any<String>(), any<Payload>())).thenReturn(response)

		val slackWebHookMessage = SlackWebHookMessage(emptyList())

		client.send(slackWebHookMessage)

		verify(slackClient).send(eq("https://slack.webhook.url"), any<Payload>())
	}

	@Test
	@DisplayName("should not send for non-SlackWebHookMessage types")
	fun should_not_send_for_wrong_message_type() {
		client.send(wrongMessage)

		verify(slackClient, never()).send(any<String>(), any<Payload>())
	}

	@Test
	@DisplayName("should throw exception when slack client throws exception")
	fun should_throw_when_slack_client_throws() {
		whenever(slackClient.send(any<String>(), any<Payload>())).thenThrow(RuntimeException("Network error"))

		val message = SlackMessage("Test", listOf("msg"))
		val slackWebHookMessage = SlackWebHookMessage(listOf(message))

		assertThrows(RuntimeException::class.java) {
			client.send(slackWebHookMessage)
		}
	}
}
