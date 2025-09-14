package com.example.demo.user.port.input

data class UpdateUserInput(
	val userId: Long,
	val name: String? = null,
	val role: String? = null
)
