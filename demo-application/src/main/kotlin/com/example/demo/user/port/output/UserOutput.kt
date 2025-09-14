package com.example.demo.user.port.output

import com.example.demo.user.constant.UserRole
import com.example.demo.user.model.User
import org.springframework.data.domain.Page
import java.time.LocalDateTime

sealed class UserOutput {
	data class BaseUserOutput(
		val id: Long,
		val email: String,
		val name: String,
		val role: UserRole,
		val createdDt: LocalDateTime,
		val updatedDt: LocalDateTime
	) : UserOutput() {
		companion object {
			fun from(user: User): BaseUserOutput =
				BaseUserOutput(
					id = user.id,
					email = user.email,
					name = user.name,
					role = user.role,
					createdDt = user.createdDt,
					updatedDt = user.updatedDt
				)
		}
	}

	data class AuthenticatedUserOutput(
		val id: Long,
		val email: String,
		val name: String,
		val role: UserRole,
		val accessToken: String,
		val createdDt: LocalDateTime,
		val updatedDt: LocalDateTime
	) : UserOutput() {
		companion object {
			fun from(
				user: User,
				accessToken: String
			): AuthenticatedUserOutput =
				AuthenticatedUserOutput(
					id = user.id,
					email = user.email,
					name = user.name,
					role = user.role,
					accessToken = accessToken,
					createdDt = user.createdDt,
					updatedDt = user.updatedDt
				)
		}
	}

	data class UserListOutput(
		val users: List<BaseUserOutput>
	) : UserOutput() {
		companion object {
			fun from(users: Page<User>): UserListOutput =
				UserListOutput(
					users = users.content.map { BaseUserOutput.from(it) }
				)
		}
	}

	data class UserPageListOutput(
		val users: Page<BaseUserOutput>
	) : UserOutput() {
		companion object {
			fun from(users: Page<User>): UserPageListOutput =
				UserPageListOutput(
					users = users.map { BaseUserOutput.from(it) }
				)
		}
	}
}
