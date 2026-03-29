package com.example.demo.user.model

import java.time.LocalDateTime

class UserDeleteItem(
	val id: Long,
	val email: String,
	val name: String,
	val role: String,
	val deletedDt: LocalDateTime
)
