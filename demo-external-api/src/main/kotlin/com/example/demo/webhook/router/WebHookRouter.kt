package com.example.demo.webhook.router

import com.example.demo.webhook.common.WebHookSender
import com.example.demo.webhook.constant.WebHookTarget
import org.springframework.stereotype.Component

@Component
class WebHookRouter(
	private val webHookSenders: List<WebHookSender>
) {
	fun route(webHookTarget: WebHookTarget): WebHookSender? =
		when (webHookTarget) {
			WebHookTarget.ALL -> throw IllegalArgumentException("Cannot route to ALL target")
			else -> webHookSenders.firstOrNull { it.target() == webHookTarget }
		}

	fun all(): List<WebHookSender> = webHookSenders
}
