package com.example.demo.post.usecase

import com.example.demo.common.NoOutputUseCase
import com.example.demo.post.port.PostPort
import com.example.demo.post.port.input.HardDeletePostsByUserIdInput
import org.springframework.stereotype.Component

@Component
class HardDeletePostsByUserIdUseCase(
	private val postPort: PostPort
) : NoOutputUseCase<HardDeletePostsByUserIdInput> {
	override fun execute(input: HardDeletePostsByUserIdInput) =
		with(input) {
			postPort.hardDeleteByUserId(userId)
		}
}
