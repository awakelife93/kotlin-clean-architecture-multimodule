package com.example.demo.post.presentation.mapper

import com.example.demo.post.port.input.CreatePostInput
import com.example.demo.post.port.input.DeletePostInput
import com.example.demo.post.port.input.GetExcludeUsersPostsInput
import com.example.demo.post.port.input.GetPostByIdInput
import com.example.demo.post.port.input.GetPostListInput
import com.example.demo.post.port.input.UpdatePostInput
import com.example.demo.post.port.output.PostOutput
import com.example.demo.post.presentation.dto.request.CreatePostRequest
import com.example.demo.post.presentation.dto.request.UpdatePostRequest
import com.example.demo.post.presentation.dto.response.CreatePostResponse
import com.example.demo.post.presentation.dto.response.GetPostResponse
import com.example.demo.post.presentation.dto.response.UpdatePostResponse
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

object PostPresentationMapper {
	fun toCreatePostInput(
		request: CreatePostRequest,
		userId: Long
	): CreatePostInput =
		with(request) {
			CreatePostInput(
				title = title,
				subTitle = subTitle,
				content = content,
				userId = userId
			)
		}

	fun toUpdatePostInput(
		postId: Long,
		request: UpdatePostRequest,
		userId: Long
	): UpdatePostInput =
		with(request) {
			UpdatePostInput(
				postId = postId,
				title = title,
				subTitle = subTitle,
				content = content,
				userId = userId
			)
		}

	fun toGetPostListInput(pageable: Pageable): GetPostListInput = GetPostListInput(pageable = pageable)

	fun toGetExcludeUsersPostsInput(
		userIds: List<Long>,
		pageable: Pageable
	): GetExcludeUsersPostsInput =
		GetExcludeUsersPostsInput(
			userIds = userIds,
			pageable = pageable
		)

	fun toGetPostByIdInput(postId: Long): GetPostByIdInput = GetPostByIdInput(postId = postId)

	fun toDeletePostInput(
		postId: Long,
		userId: Long
	): DeletePostInput =
		DeletePostInput(
			postId = postId,
			userId = userId
		)

	fun toCreatePostResponse(output: PostOutput.BasePostOutput): CreatePostResponse =
		with(output) {
			CreatePostResponse(
				postId = id,
				title = title,
				subTitle = subTitle,
				content = content,
				userId = userId,
				createDt = createdDt,
				updateDt = updatedDt
			)
		}

	fun toUpdatePostResponse(output: PostOutput.BasePostOutput): UpdatePostResponse =
		with(output) {
			UpdatePostResponse(
				postId = id,
				title = title,
				subTitle = subTitle,
				content = content,
				userId = userId,
				createDt = createdDt,
				updateDt = updatedDt
			)
		}

	fun toGetPostResponse(output: PostOutput.BasePostOutput): GetPostResponse =
		with(output) {
			GetPostResponse(
				postId = id,
				title = title,
				subTitle = subTitle,
				content = content,
				userId = userId,
				createDt = createdDt,
				updateDt = updatedDt
			)
		}

	fun toGetPostListResponse(output: PostOutput.PostPageListOutput): Page<GetPostResponse> =
		output.posts.map { post ->
			with(post) {
				GetPostResponse(
					postId = id,
					title = title,
					subTitle = subTitle,
					content = content,
					userId = userId,
					createDt = createdDt,
					updateDt = updatedDt
				)
			}
		}
}
