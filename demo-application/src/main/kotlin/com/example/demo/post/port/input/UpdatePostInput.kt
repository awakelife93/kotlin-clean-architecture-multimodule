package com.example.demo.post.port.input

data class UpdatePostInput(
	val postId: Long,
	val title: String,
	val subTitle: String,
	val content: String,
	val userId: Long
)
