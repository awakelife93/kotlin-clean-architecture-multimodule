package com.example.demo.post.port.input

data class CreatePostInput(
	val title: String,
	val subTitle: String,
	val content: String,
	val userId: Long
)
