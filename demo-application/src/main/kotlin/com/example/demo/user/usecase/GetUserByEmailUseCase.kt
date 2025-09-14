package com.example.demo.user.usecase

import com.example.demo.common.UseCase
import com.example.demo.user.port.input.GetUserByEmailInput
import com.example.demo.user.port.output.UserOutput
import com.example.demo.user.service.UserService
import org.springframework.stereotype.Component

@Component
class GetUserByEmailUseCase(
	private val userService: UserService
) : UseCase<GetUserByEmailInput, UserOutput.BaseUserOutput> {
	override fun execute(input: GetUserByEmailInput): UserOutput.BaseUserOutput =
		with(input) {
			userService
				.findOneByEmailOrThrow(email)
				.let(UserOutput.BaseUserOutput::from)
		}
}
