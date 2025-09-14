package com.example.demo.auth.usecase

import com.example.demo.auth.port.input.RefreshAccessTokenInput
import com.example.demo.auth.port.output.AuthOutput
import com.example.demo.auth.service.AuthService
import com.example.demo.common.UseCase
import org.springframework.stereotype.Component

@Component
class RefreshAccessTokenUseCase(
	private val authService: AuthService
) : UseCase<RefreshAccessTokenInput, AuthOutput> {
	override fun execute(input: RefreshAccessTokenInput): AuthOutput = authService.refreshAccessToken(input)
}
