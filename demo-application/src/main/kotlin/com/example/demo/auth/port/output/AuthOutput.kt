package com.example.demo.auth.port.output

import com.example.demo.user.constant.UserRole
import com.example.demo.user.model.User

data class AuthOutput(
	val userId: Long,
	val email: String,
	val name: String,
	val role: UserRole,
	val accessToken: String
) {
	companion object {
		fun fromSignIn(
			user: User,
			accessToken: String
		): AuthOutput =
			AuthOutput(
				userId = user.id,
				email = user.email,
				name = user.name,
				role = user.role,
				accessToken = accessToken
			)

		fun fromRefresh(accessToken: String): AuthOutput =
			AuthOutput(
				userId = 0L,
				email = "",
				name = "",
				role = UserRole.USER,
				accessToken = accessToken
			)
	}
}
