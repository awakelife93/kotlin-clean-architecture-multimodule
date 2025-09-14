package com.example.demo.user.presentation.dto.request

import com.example.demo.common.annotation.ValidEnum
import com.example.demo.user.constant.UserRole
import io.swagger.v3.oas.annotations.media.Schema

data class UpdateUserRequest(
	@field:Schema(description = "User Name", nullable = true)
	val name: String,
	@field:Schema(description = "User Role", nullable = true, implementation = UserRole::class)
	@field:ValidEnum(enumClass = UserRole::class, message = "field role is invalid", ignoreCase = true)
	val role: String
)
