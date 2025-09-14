package com.example.demo.auth.port

interface RefreshTokenPort {
	fun save(
		userId: Long,
		token: String,
		expiresIn: Long
	)

	fun findByUserId(userId: Long): String?

	fun deleteByUserId(userId: Long)

	fun validateToken(
		userId: Long,
		token: String
	): Boolean
}
