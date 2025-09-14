package com.example.demo.user.event

import com.example.demo.user.model.User

sealed class UserEvent {
	data class WelcomeSignUpEvent(
		val email: String,
		val name: String
	) : UserEvent() {
		companion object {
			fun from(user: User): WelcomeSignUpEvent =
				WelcomeSignUpEvent(
					email = user.email,
					name = user.name
				)
		}
	}

	data class UserUpdatedEvent(
		val userId: Long,
		val name: String,
		val email: String
	) : UserEvent() {
		companion object {
			fun from(user: User): UserUpdatedEvent =
				UserUpdatedEvent(
					userId = user.id,
					name = user.name,
					email = user.email
				)
		}
	}

	data class UserDeletedEvent(
		val userId: Long,
		val email: String
	) : UserEvent() {
		companion object {
			fun from(user: User): UserDeletedEvent =
				UserDeletedEvent(
					userId = user.id,
					email = user.email
				)
		}
	}
}
