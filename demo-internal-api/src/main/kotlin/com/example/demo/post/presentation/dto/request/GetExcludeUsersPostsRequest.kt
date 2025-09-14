package com.example.demo.post.presentation.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Size

data class GetExcludeUsersPostsRequest(
	@field:Schema(description = "User Ids to exclude", nullable = false)
	@field:Size(
		min = 1,
		message = "field userIds is empty"
	)
	val userIds: List<Long> = emptyList()
)
