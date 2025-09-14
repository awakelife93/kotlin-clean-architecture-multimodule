package com.example.demo.post.presentation.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

data class UpdatePostResponse(
	@field:Schema(description = "Post Id", nullable = false)
	val postId: Long,
	@field:Schema(description = "Post Title", nullable = false)
	val title: String,
	@field:Schema(description = "Post Sub Title", nullable = false)
	val subTitle: String,
	@field:Schema(description = "Post Content", nullable = false)
	val content: String,
	@field:Schema(description = "User Id", nullable = false)
	val userId: Long,
	@field:Schema(description = "Created Date", nullable = false)
	val createDt: LocalDateTime,
	@field:Schema(description = "Updated Date", nullable = false)
	val updateDt: LocalDateTime
)
