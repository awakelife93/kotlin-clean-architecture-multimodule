package com.example.demo.post.port

import com.example.demo.post.model.Post
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface PostQueryPort {
	fun findOneById(id: Long): Post?

	fun findAll(pageable: Pageable): Page<Post>

	fun findOneByUserId(
		userId: Long,
		pageable: Pageable
	): Page<Post>

	fun findAllByUserId(userId: Long): List<Post>

	fun findAllByUserId(
		userId: Long,
		pageable: Pageable
	): Page<Post>

	fun findExcludeUsersPosts(
		userIds: List<Long>,
		pageable: Pageable
	): Page<Post>

	fun existsById(id: Long): Boolean
}
