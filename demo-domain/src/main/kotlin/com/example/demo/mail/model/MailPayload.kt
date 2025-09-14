package com.example.demo.mail.model

data class MailPayload(
	val to: String,
	val subject: String,
	val body: String
) {
	companion object {
		fun of(
			to: String,
			subject: String,
			body: String
		): MailPayload = MailPayload(to, subject, body)
	}
}
