package com.example.demo.post.usecase

import com.example.demo.common.UseCase
import com.example.demo.post.port.input.GetUserPostsInput
import com.example.demo.post.port.output.PostOutput
import com.example.demo.post.service.PostService
import org.springframework.stereotype.Component

@Component
class GetUserPostsUseCase(
	private val postService: PostService
) : UseCase<GetUserPostsInput, PostOutput.PostPageListOutput> {
	override fun execute(input: GetUserPostsInput): PostOutput.PostPageListOutput =
		postService
			.findAllByUserId(input.userId, input.pageable)
			.let(PostOutput.PostPageListOutput::from)
}
