package com.example.demo.user.usecase

import com.example.demo.common.UseCase
import com.example.demo.user.port.input.GetUserByIdInput
import com.example.demo.user.port.output.UserOutput
import com.example.demo.user.service.UserService
import org.springframework.stereotype.Component

@Component
class GetUserByIdUseCase(
	private val userService: UserService
) : UseCase<GetUserByIdInput, UserOutput.BaseUserOutput> {
	override fun execute(input: GetUserByIdInput): UserOutput.BaseUserOutput =
		with(input) {
			userService
				.findOneByIdOrThrow(userId)
				.let(UserOutput.BaseUserOutput::from)
		}
}
