package com.example.demo.user.port

import com.example.demo.user.model.User

interface UserCommandPort {
	fun save(user: User): User

	fun deleteById(userId: Long)

	fun hardDeleteById(userId: Long)
}
