package com.example.demo.post.usecase

import com.example.demo.common.UseCase
import com.example.demo.post.port.input.GetExcludeUsersPostsInput
import com.example.demo.post.port.output.PostOutput
import com.example.demo.post.service.PostService
import org.springframework.stereotype.Component

@Component
class GetExcludeUsersPostsUseCase(
	private val postService: PostService
) : UseCase<GetExcludeUsersPostsInput, PostOutput.PostPageListOutput> {
	override fun execute(input: GetExcludeUsersPostsInput): PostOutput.PostPageListOutput =
		postService
			.findExcludingUserIds(input.userIds, input.pageable)
			.let(PostOutput.PostPageListOutput::from)
}
