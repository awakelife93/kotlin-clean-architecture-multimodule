package com.example.demo.user.model

import com.example.demo.user.constant.UserRole
import java.time.LocalDateTime

data class User(
	val id: Long = 0,
	var name: String,
	var email: String,
	var password: String,
	var role: UserRole = UserRole.USER,
	val createdDt: LocalDateTime = LocalDateTime.now(),
	var updatedDt: LocalDateTime = LocalDateTime.now(),
	var deletedDt: LocalDateTime? = null
) {
	fun update(
		name: String? = null,
		role: UserRole? = null
	): User {
		name?.let { this.name = it }
		role?.let { this.role = it }
		return this
	}

	fun encodePassword(encoder: (String) -> String): User {
		this.password = encoder(this.password)
		return this
	}

	fun validatePassword(
		rawPassword: String,
		matcher: (String, String) -> Boolean
	): Boolean = matcher(rawPassword, this.password)

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other !is User) return false
		return id == other.id
	}

	override fun hashCode(): Int = id.hashCode()
}
