package com.example.demo.webhook.common

import com.example.demo.webhook.constant.WebHookTarget

interface WebHookSender {
	fun send(message: WebHookMessage)

	fun target(): WebHookTarget
}
