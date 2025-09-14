package com.example.demo.auth.presentation.dto.response

import io.swagger.v3.oas.annotations.media.Schema

data class RefreshAccessTokenResponse(
	@field:Schema(
		description = "User AccessToken",
		nullable = false
	)
	val accessToken: String
)
