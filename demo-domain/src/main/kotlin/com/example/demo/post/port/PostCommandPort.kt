package com.example.demo.post.port

import com.example.demo.post.model.Post

interface PostCommandPort {
	fun save(post: Post): Post

	fun deleteById(id: Long)

	fun deleteByUserId(userId: Long)

	fun hardDeleteById(id: Long)

	fun hardDeleteByUserId(userId: Long)
}
