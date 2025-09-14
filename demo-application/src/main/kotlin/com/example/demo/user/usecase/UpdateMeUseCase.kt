package com.example.demo.user.usecase

import com.example.demo.common.UseCase
import com.example.demo.persistence.auth.provider.TokenProvider
import com.example.demo.user.port.input.UpdateMeInput
import com.example.demo.user.port.output.UserOutput
import com.example.demo.user.service.UserService
import org.springframework.stereotype.Component

@Component
class UpdateMeUseCase(
	private val userService: UserService,
	private val tokenProvider: TokenProvider
) : UseCase<UpdateMeInput, UserOutput.AuthenticatedUserOutput> {
	override fun execute(input: UpdateMeInput): UserOutput.AuthenticatedUserOutput =
		with(input) {
			userService
				.updateUserInfo(userId, name)
				.let { updatedUser ->
					UserOutput.AuthenticatedUserOutput.from(
						updatedUser,
						tokenProvider.createFullTokens(updatedUser)
					)
				}
		}
}
