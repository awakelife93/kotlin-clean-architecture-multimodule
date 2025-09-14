package com.example.demo.post.port.input

import org.springframework.data.domain.Pageable

data class GetExcludeUsersPostsInput(
	val userIds: List<Long>,
	val pageable: Pageable
)
