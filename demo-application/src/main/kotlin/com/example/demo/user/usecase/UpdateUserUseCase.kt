package com.example.demo.user.usecase

import com.example.demo.common.UseCase
import com.example.demo.user.port.input.UpdateUserInput
import com.example.demo.user.port.output.UserOutput
import com.example.demo.user.service.UserService
import org.springframework.stereotype.Component

@Component
class UpdateUserUseCase(
	private val userService: UserService
) : UseCase<UpdateUserInput, UserOutput.BaseUserOutput> {
	override fun execute(input: UpdateUserInput): UserOutput.BaseUserOutput =
		with(input) {
			userService
				.updateUserInfo(userId, name)
				.let(UserOutput.BaseUserOutput::from)
		}
}
