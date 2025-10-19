package com.example.demo.post.service

import com.example.demo.post.exception.PostNotFoundException
import com.example.demo.post.model.Post
import com.example.demo.post.port.PostPort
import com.example.demo.post.port.input.CreatePostInput
import com.example.demo.post.port.input.UpdatePostInput
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class PostService(
	private val postPort: PostPort
) {
	fun findOneById(postId: Long): Post? = postPort.findOneById(postId)

	fun findOneByIdOrThrow(postId: Long): Post = findOneById(postId) ?: throw PostNotFoundException(postId)

	fun findAll(pageable: Pageable): Page<Post> = postPort.findAll(pageable)

	fun findAllByUserId(userId: Long): List<Post> = postPort.findAllByUserId(userId)

	fun findAllByUserId(
		userId: Long,
		pageable: Pageable
	): Page<Post> = postPort.findAllByUserId(userId, pageable)

	fun findExcludingUserIds(
		userIds: List<Long>,
		pageable: Pageable
	): Page<Post> = postPort.findExcludeUsersPosts(userIds, pageable)

	@Transactional
	fun createPost(input: CreatePostInput): Post {
		val post =
			Post(
				title = input.title,
				subTitle = input.subTitle,
				content = input.content,
				userId = input.userId
			)

		return postPort.save(post)
	}

	@Transactional
	fun updatePost(post: Post): Post = postPort.save(post)

	@Transactional
	fun updatePostInfo(input: UpdatePostInput): Post {
		val post = findOneByIdOrThrow(input.postId)

		require(post.isOwnedBy(input.userId)) {
			"Permission denied. You can only modify posts you have authored."
		}

		post.update(
			title = input.title,
			subTitle = input.subTitle,
			content = input.content
		)

		return postPort.save(post)
	}

	@Transactional
	fun deletePost(postId: Long) {
		postPort.findOneById(postId)?.let {
			postPort.deleteById(postId)
		}
	}

	@Transactional
	fun deletePostByUser(
		postId: Long,
		userId: Long
	) {
		val post = findOneByIdOrThrow(postId)

		require(post.isOwnedBy(userId)) {
			"Permission denied. You can only delete posts you have authored."
		}

		postPort.deleteById(postId)
	}

	@Transactional
	fun deletePostsByUserId(userId: Long) {
		postPort.deleteByUserId(userId)
	}

	fun existsById(postId: Long): Boolean = postPort.existsById(postId)

	fun countByUserId(userId: Long): Long = postPort.findAllByUserId(userId).size.toLong()
}
