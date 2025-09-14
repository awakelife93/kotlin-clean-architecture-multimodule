package com.example.demo.security.model

import com.example.demo.user.constant.UserRole
import com.example.demo.user.model.User

data class SecurityUserItem(
	val userId: Long,
	val role: UserRole,
	val name: String,
	val email: String
) {
	companion object {
		fun from(user: User): SecurityUserItem =
			with(user) {
				SecurityUserItem(
					userId = id,
					role = role,
					name = name,
					email = email
				)
			}
	}
}
