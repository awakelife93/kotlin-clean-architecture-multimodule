package com.example.demo.auth.usecase

import com.example.demo.auth.port.input.SignInInput
import com.example.demo.auth.port.output.AuthOutput
import com.example.demo.auth.service.AuthService
import com.example.demo.common.UseCase
import org.springframework.stereotype.Component

@Component
class SignInUseCase(
	private val authService: AuthService
) : UseCase<SignInInput, AuthOutput> {
	override fun execute(input: SignInInput): AuthOutput = authService.signIn(input)
}
