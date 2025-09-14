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
		CreatePostInput(
			title = request.title,
			subTitle = request.subTitle,
			content = request.content,
			userId = userId
		)

	fun toUpdatePostInput(
		postId: Long,
		request: UpdatePostRequest,
		userId: Long
	): UpdatePostInput =
		UpdatePostInput(
			postId = postId,
			title = request.title,
			subTitle = request.subTitle,
			content = request.content,
			userId = userId
		)

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
		CreatePostResponse(
			postId = output.id,
			title = output.title,
			subTitle = output.subTitle,
			content = output.content,
			userId = output.userId,
			createDt = output.createdDt,
			updateDt = output.updatedDt
		)

	fun toUpdatePostResponse(output: PostOutput.BasePostOutput): UpdatePostResponse =
		UpdatePostResponse(
			postId = output.id,
			title = output.title,
			subTitle = output.subTitle,
			content = output.content,
			userId = output.userId,
			createDt = output.createdDt,
			updateDt = output.updatedDt
		)

	fun toGetPostResponse(output: PostOutput.BasePostOutput): GetPostResponse =
		GetPostResponse(
			postId = output.id,
			title = output.title,
			subTitle = output.subTitle,
			content = output.content,
			userId = output.userId,
			createDt = output.createdDt,
			updateDt = output.updatedDt
		)

	fun toGetPostListResponse(output: PostOutput.PostPageListOutput): Page<GetPostResponse> =
		output.posts.map { post ->
			GetPostResponse(
				postId = post.id,
				title = post.title,
				subTitle = post.subTitle,
				content = post.content,
				userId = post.userId,
				createDt = post.createdDt,
				updateDt = post.updatedDt
			)
		}
}
