package com.example.demo.post.usecase

import com.example.demo.common.UseCase
import com.example.demo.post.port.input.CreatePostInput
import com.example.demo.post.port.output.PostOutput
import com.example.demo.post.service.PostService
import org.springframework.stereotype.Component

@Component
class CreatePostUseCase(
	private val postService: PostService
) : UseCase<CreatePostInput, PostOutput.BasePostOutput> {
	override fun execute(input: CreatePostInput): PostOutput.BasePostOutput =
		postService
			.createPost(input)
			.let(PostOutput.BasePostOutput::from)
}
