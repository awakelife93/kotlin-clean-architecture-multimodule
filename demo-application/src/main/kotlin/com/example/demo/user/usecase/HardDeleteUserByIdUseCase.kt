package com.example.demo.user.usecase

import com.example.demo.common.NoOutputUseCase
import com.example.demo.user.port.UserPort
import com.example.demo.user.port.input.HardDeleteUserByIdInput
import org.springframework.stereotype.Component

@Component
class HardDeleteUserByIdUseCase(
	private val userPort: UserPort
) : NoOutputUseCase<HardDeleteUserByIdInput> {
	override fun execute(input: HardDeleteUserByIdInput) =
		with(input) {
			userPort.hardDeleteById(userId)
		}
}
