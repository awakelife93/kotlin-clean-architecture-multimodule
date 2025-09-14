package com.example.demo.auth.presentation.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

data class RefreshAccessTokenRequest(
	@field:Schema(description = "Refresh Access Token", nullable = false)
	@field:NotBlank(message = "field refreshToken is blank")
	val refreshToken: String
)
