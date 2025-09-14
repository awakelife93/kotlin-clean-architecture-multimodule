package com.example.demo.user.usecase

import com.example.demo.common.NoOutputUseCase
import com.example.demo.user.port.input.DeleteUserInput
import com.example.demo.user.service.UserDeletionService
import org.springframework.stereotype.Component

@Component
class DeleteUserUseCase(
	private val userDeletionService: UserDeletionService
) : NoOutputUseCase<DeleteUserInput> {
	override fun execute(input: DeleteUserInput) =
		with(input) {
			userDeletionService.deleteUserWithRelatedData(userId)
		}
}
