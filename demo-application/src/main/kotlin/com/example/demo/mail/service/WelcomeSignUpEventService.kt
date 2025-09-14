package com.example.demo.mail.service

import com.example.demo.mail.MailHelper
import com.example.demo.mail.event.WelcomeSignUpEventHandler
import com.example.demo.mail.model.MailPayload
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class WelcomeSignUpEventService(
	private val mailHelper: MailHelper
) : WelcomeSignUpEventHandler {
	override fun handle(payload: MailPayload) {
		logger.info { "Processing welcome email for: ${payload.to}" }
		logger.debug { "Email subject: ${payload.subject}" }

		mailHelper.sendEmail(payload)

		logger.info { "Successfully sent welcome email to: ${payload.to}" }
	}
}
