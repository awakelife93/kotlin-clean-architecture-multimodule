package com.example.demo.post.port.output

import com.example.demo.post.model.Post
import org.springframework.data.domain.Page
import java.time.LocalDateTime

sealed class PostOutput {
	data class BasePostOutput(
		val id: Long,
		val userId: Long,
		val title: String,
		val subTitle: String,
		val content: String,
		val createdDt: LocalDateTime,
		val updatedDt: LocalDateTime
	) : PostOutput() {
		companion object {
			fun from(post: Post): BasePostOutput =
				BasePostOutput(
					id = post.id,
					userId = post.userId,
					title = post.title,
					subTitle = post.subTitle,
					content = post.content,
					createdDt = post.createdDt,
					updatedDt = post.updatedDt
				)
		}
	}

	data class PostListOutput(
		val posts: List<Post>
	) : PostOutput() {
		companion object {
			fun from(posts: List<Post>): PostListOutput = PostListOutput(posts)
		}
	}

	data class PostPageListOutput(
		val posts: Page<Post>
	) : PostOutput() {
		companion object {
			fun from(posts: Page<Post>): PostPageListOutput = PostPageListOutput(posts)
		}
	}
}
