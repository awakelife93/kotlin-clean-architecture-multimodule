package com.example.demo.mail.event

import com.example.demo.mail.model.MailPayload

interface WelcomeSignUpEventHandler {
	fun handle(payload: MailPayload)
}
