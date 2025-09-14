package com.example.demo.auth.presentation.dto.response

import com.example.demo.user.constant.UserRole
import io.swagger.v3.oas.annotations.media.Schema

data class SignInResponse(
	@field:Schema(
		description = "User Access Token",
		nullable = false
	)
	val accessToken: String,
	@field:Schema(description = "User Id", nullable = false)
	val userId: Long,
	@field:Schema(description = "User Role", nullable = false, implementation = UserRole::class)
	val role: UserRole,
	@field:Schema(description = "User Name", nullable = false)
	val name: String,
	@field:Schema(description = "User Email", nullable = false, format = "email")
	val email: String
)
