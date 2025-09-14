package com.example.demo.user.presentation.dto.response

import com.example.demo.user.constant.UserRole
import com.example.demo.user.model.User
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

class UpdateMeResponse(
	@field:Schema(description = "User Id", nullable = false)
	val userId: Long,
	@field:Schema(description = "User Role", nullable = false, implementation = UserRole::class)
	val role: UserRole,
	@field:Schema(description = "User Name", nullable = false)
	val name: String,
	@field:Schema(description = "User Email", nullable = false, format = "email")
	val email: String,
	@field:Schema(description = "User Access Token", nullable = true)
	val accessToken: String,
	@field:Schema(description = "User Create Date", nullable = false, format = "date-time")
	val createDt: LocalDateTime,
	@field:Schema(description = "User Update Date", nullable = false, format = "date-time")
	val updateDt: LocalDateTime
) {
	companion object {
		fun from(
			user: User,
			accessToken: String
		): UpdateMeResponse =
			with(user) {
				UpdateMeResponse(
					userId = id,
					role = role,
					name = name,
					email = email,
					accessToken = accessToken,
					createDt = createdDt,
					updateDt = updatedDt
				)
			}
	}
}
