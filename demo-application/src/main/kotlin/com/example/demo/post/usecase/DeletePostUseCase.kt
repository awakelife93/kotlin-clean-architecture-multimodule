package com.example.demo.post.usecase

import com.example.demo.common.NoOutputUseCase
import com.example.demo.post.port.input.DeletePostInput
import com.example.demo.post.service.PostService
import org.springframework.stereotype.Component

@Component
class DeletePostUseCase(
	private val postService: PostService
) : NoOutputUseCase<DeletePostInput> {
	override fun execute(input: DeletePostInput) =
		with(input) {
			postService.deletePostByUser(postId, userId)
		}
}
