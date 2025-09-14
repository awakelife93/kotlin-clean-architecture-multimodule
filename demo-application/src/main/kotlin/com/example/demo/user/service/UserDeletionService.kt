package com.example.demo.user.service

import com.example.demo.persistence.auth.provider.TokenProvider
import com.example.demo.post.service.PostService
import com.example.demo.user.exception.UserNotFoundException
import com.example.demo.user.port.output.UserDeletionSummaryOutput
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class UserDeletionService(
	private val userService: UserService,
	private val postService: PostService,
	private val tokenProvider: TokenProvider
) {
	fun deleteUserWithRelatedData(userId: Long) {
		if (!userService.existsById(userId)) {
			throw UserNotFoundException(userId)
		}

		postService.deletePostsByUserId(userId)
		tokenProvider.deleteRefreshToken(userId)
		userService.deleteByIdWithoutValidation(userId)
	}

	fun canDeleteUser(userId: Long): Boolean = userService.existsById(userId)

	fun getUserDeletionSummary(userId: Long): UserDeletionSummaryOutput {
		val postCount = postService.countByUserId(userId)

		return UserDeletionSummaryOutput(
			userId = userId,
			postCount = postCount
		)
	}
}
