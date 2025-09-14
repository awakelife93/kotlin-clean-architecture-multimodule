package com.example.demo.post.usecase

import com.example.demo.common.UseCase
import com.example.demo.post.port.input.GetPostListInput
import com.example.demo.post.port.output.PostOutput
import com.example.demo.post.service.PostService
import org.springframework.stereotype.Component

@Component
class GetPostListUseCase(
	private val postService: PostService
) : UseCase<GetPostListInput, PostOutput.PostPageListOutput> {
	override fun execute(input: GetPostListInput): PostOutput.PostPageListOutput =
		postService
			.findAll(input.pageable)
			.let(PostOutput.PostPageListOutput::from)
}
