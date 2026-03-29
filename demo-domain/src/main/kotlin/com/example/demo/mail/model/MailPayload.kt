package com.example.demo.mail.model

data class MailPayload(
	val to: String,
	val subject: String,
	val body: String
)
