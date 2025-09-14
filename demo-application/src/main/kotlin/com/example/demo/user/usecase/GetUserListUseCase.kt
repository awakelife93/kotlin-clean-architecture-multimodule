package com.example.demo.user.usecase

import com.example.demo.common.UseCase
import com.example.demo.user.port.input.GetUserListInput
import com.example.demo.user.port.output.UserOutput
import com.example.demo.user.service.UserService
import org.springframework.stereotype.Component

@Component
class GetUserListUseCase(
	private val userService: UserService
) : UseCase<GetUserListInput, UserOutput.UserPageListOutput> {
	override fun execute(input: GetUserListInput): UserOutput.UserPageListOutput =
		with(input) {
			userService
				.findAll(pageable)
				.let(UserOutput.UserPageListOutput::from)
		}
}
