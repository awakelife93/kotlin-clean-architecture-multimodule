package com.example.demo.user.port.input

import org.springframework.data.domain.Pageable

data class GetUserListInput(
	val pageable: Pageable
)
