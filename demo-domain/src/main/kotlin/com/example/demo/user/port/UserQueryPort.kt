package com.example.demo.user.port

import com.example.demo.user.model.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface UserQueryPort {
	fun findOneById(userId: Long): User?

	fun findOneByEmail(email: String): User?

	fun findAll(pageable: Pageable): Page<User>

	fun existsByEmail(email: String): Boolean

	fun existsById(userId: Long): Boolean
}
