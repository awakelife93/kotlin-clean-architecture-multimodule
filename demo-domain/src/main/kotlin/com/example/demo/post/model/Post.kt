package com.example.demo.post.model

import java.time.LocalDateTime

data class Post(
	val id: Long = 0,
	var title: String,
	var subTitle: String,
	var content: String,
	val userId: Long,
	val createdDt: LocalDateTime = LocalDateTime.now(),
	var updatedDt: LocalDateTime = LocalDateTime.now(),
	var deletedDt: LocalDateTime? = null
) {
	fun update(
		title: String? = null,
		subTitle: String? = null,
		content: String? = null
	): Post {
		title?.let { this.title = it }
		subTitle?.let { this.subTitle = it }
		content?.let { this.content = it }
		return this
	}

	fun isOwnedBy(userId: Long): Boolean = this.userId == userId

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other !is Post) return false
		return id == other.id
	}

	override fun hashCode(): Int = id.hashCode()
}
