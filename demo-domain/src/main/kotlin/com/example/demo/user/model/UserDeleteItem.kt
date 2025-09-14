package com.example.demo.user.model

import java.time.LocalDateTime

class UserDeleteItem(
	val id: Long,
	val email: String,
	val name: String,
	val role: String,
	val deletedDt: LocalDateTime
) {
	companion object {
		fun of(
			id: Long,
			email: String,
			name: String,
			role: String,
			deletedDt: LocalDateTime
		): UserDeleteItem =
			UserDeleteItem(
				id = id,
				email = email,
				name = name,
				role = role,
				deletedDt = deletedDt
			)
	}
}
