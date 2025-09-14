package com.example.demo.post.port.input

import org.springframework.data.domain.Pageable

data class GetUserPostsInput(
	val userId: Long,
	val pageable: Pageable
)
