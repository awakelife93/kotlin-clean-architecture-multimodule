package com.example.demo.post.usecase

import com.example.demo.common.UseCase
import com.example.demo.post.port.input.UpdatePostInput
import com.example.demo.post.port.output.PostOutput
import com.example.demo.post.service.PostService
import org.springframework.stereotype.Component

@Component
class UpdatePostUseCase(
	private val postService: PostService
) : UseCase<UpdatePostInput, PostOutput.BasePostOutput> {
	override fun execute(input: UpdatePostInput): PostOutput.BasePostOutput =
		with(input) {
			postService
				.updatePostInfo(this)
				.let(PostOutput.BasePostOutput::from)
		}
}
