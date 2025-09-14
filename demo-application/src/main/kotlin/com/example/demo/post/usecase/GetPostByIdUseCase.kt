package com.example.demo.post.usecase

import com.example.demo.common.UseCase
import com.example.demo.post.port.input.GetPostByIdInput
import com.example.demo.post.port.output.PostOutput
import com.example.demo.post.service.PostService
import org.springframework.stereotype.Component

@Component
class GetPostByIdUseCase(
	private val postService: PostService
) : UseCase<GetPostByIdInput, PostOutput.BasePostOutput> {
	override fun execute(input: GetPostByIdInput): PostOutput.BasePostOutput =
		with(input) {
			postService
				.findOneByIdOrThrow(postId)
				.let(PostOutput.BasePostOutput::from)
		}
}
