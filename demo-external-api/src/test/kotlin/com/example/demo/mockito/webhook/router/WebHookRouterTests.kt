package com.example.demo.mockito.webhook.router

import com.example.demo.webhook.common.WebHookSender
import com.example.demo.webhook.constant.WebHookTarget
import com.example.demo.webhook.router.WebHookRouter
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@Tag("mockito-unit-test")
@DisplayName("Mockito Unit - WebHook Router Test")
@ExtendWith(MockitoExtension::class)
class WebHookRouterTests {
	@Mock
	private lateinit var slackSender: WebHookSender

	@Mock
	private lateinit var discordSender: WebHookSender

	private val router: WebHookRouter by lazy {
		WebHookRouter(listOf(slackSender, discordSender))
	}

	@Nested
	@DisplayName("Routing to specific targets")
	inner class RoutingTests {
		@Test
		@DisplayName("should route to Slack sender")
		fun should_route_to_slack_sender() {
			whenever(slackSender.target()).thenReturn(WebHookTarget.SLACK)

			val result = router.route(WebHookTarget.SLACK)

			assertNotNull(result)
			assertEquals(slackSender, result)
		}

		@Test
		@DisplayName("should route to Discord sender")
		fun should_route_to_discord_sender() {
			whenever(slackSender.target()).thenReturn(WebHookTarget.SLACK)
			whenever(discordSender.target()).thenReturn(WebHookTarget.DISCORD)

			val result = router.route(WebHookTarget.DISCORD)

			assertNotNull(result)
			assertEquals(discordSender, result)
		}

		@Test
		@DisplayName("should throw exception for ALL target")
		fun should_throw_exception_for_all_target() {
			val exception =
				assertThrows(IllegalArgumentException::class.java) {
					router.route(WebHookTarget.ALL)
				}
			assertEquals("Cannot route to ALL target", exception.message)
		}

		@Test
		@DisplayName("should return null when no sender matches target")
		fun should_return_null_when_no_sender_matches() {
			val emptyRouter = WebHookRouter(emptyList())

			val result = emptyRouter.route(WebHookTarget.SLACK)

			assertNull(result)
		}
	}

	@Nested
	@DisplayName("Getting all senders")
	inner class AllSendersTests {
		@Test
		@DisplayName("should return all registered senders")
		fun should_return_all_senders() {
			val result = router.all()

			assertEquals(2, result.size)
			assertTrue(result.contains(slackSender))
			assertTrue(result.contains(discordSender))
		}

		@Test
		@DisplayName("should return empty list when no senders registered")
		fun should_return_empty_list_when_no_senders() {
			val emptyRouter = WebHookRouter(emptyList())

			val result = emptyRouter.all()

			assertTrue(result.isEmpty())
		}

		@Test
		@DisplayName("should handle single sender")
		fun should_handle_single_sender() {
			val singleRouter = WebHookRouter(listOf(slackSender))

			val result = singleRouter.all()

			assertEquals(1, result.size)
			assertEquals(slackSender, result[0])
		}
	}
}
