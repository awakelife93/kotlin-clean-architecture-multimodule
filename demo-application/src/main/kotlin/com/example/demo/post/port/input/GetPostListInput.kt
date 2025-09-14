package com.example.demo.post.port.input

import org.springframework.data.domain.Pageable

data class GetPostListInput(
	val pageable: Pageable
)
