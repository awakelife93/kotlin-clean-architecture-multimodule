package com.example.demo.user.usecase

import com.example.demo.common.UseCase
import com.example.demo.user.port.input.CreateUserInput
import com.example.demo.user.port.output.UserOutput
import com.example.demo.user.service.UserService
import org.springframework.stereotype.Component

@Component
class CreateUserUseCase(
	private val userService: UserService
) : UseCase<CreateUserInput, UserOutput.AuthenticatedUserOutput> {
	override fun execute(input: CreateUserInput): UserOutput.AuthenticatedUserOutput =
		with(input) {
			userService.registerNewUser(this)
		}
}
