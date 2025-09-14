package com.example.demo.auth.usecase

import com.example.demo.auth.port.input.SignOutInput
import com.example.demo.auth.service.AuthService
import com.example.demo.common.UseCase
import org.springframework.stereotype.Component

@Component
class SignOutUseCase(
	private val authService: AuthService
) : UseCase<SignOutInput, Unit> {
	override fun execute(input: SignOutInput) = authService.signOut(input.userId)
}
