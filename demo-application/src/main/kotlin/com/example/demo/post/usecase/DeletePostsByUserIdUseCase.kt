package com.example.demo.post.usecase

import com.example.demo.common.NoOutputUseCase
import com.example.demo.post.port.input.DeletePostsByUserIdInput
import com.example.demo.post.service.PostService
import org.springframework.stereotype.Component

@Component
class DeletePostsByUserIdUseCase(
	private val postService: PostService
) : NoOutputUseCase<DeletePostsByUserIdInput> {
	override fun execute(input: DeletePostsByUserIdInput) =
		with(input) {
			postService.deletePostsByUserId(userId)
		}
}
