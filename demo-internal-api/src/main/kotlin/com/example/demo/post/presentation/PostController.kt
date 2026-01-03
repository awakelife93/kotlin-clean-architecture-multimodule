package com.example.demo.post.presentation

import com.example.demo.common.response.ErrorResponse
import com.example.demo.post.presentation.dto.request.CreatePostRequest
import com.example.demo.post.presentation.dto.request.UpdatePostRequest
import com.example.demo.post.presentation.dto.response.CreatePostResponse
import com.example.demo.post.presentation.dto.response.GetPostResponse
import com.example.demo.post.presentation.dto.response.UpdatePostResponse
import com.example.demo.post.presentation.mapper.PostPresentationMapper
import com.example.demo.post.usecase.CreatePostUseCase
import com.example.demo.post.usecase.DeletePostUseCase
import com.example.demo.post.usecase.GetExcludeUsersPostsUseCase
import com.example.demo.post.usecase.GetPostByIdUseCase
import com.example.demo.post.usecase.GetPostListUseCase
import com.example.demo.post.usecase.UpdatePostUseCase
import com.example.demo.security.annotation.CurrentUser
import com.example.demo.security.model.SecurityUserItem
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Post", description = "Post API")
@RestController
@RequestMapping("/api/v1/posts")
class PostController(
	private val createPostUseCase: CreatePostUseCase,
	private val updatePostUseCase: UpdatePostUseCase,
	private val deletePostUseCase: DeletePostUseCase,
	private val getPostByIdUseCase: GetPostByIdUseCase,
	private val getPostListUseCase: GetPostListUseCase,
	private val getExcludeUsersPostsUseCase: GetExcludeUsersPostsUseCase
) {
	@Operation(operationId = "createPost", summary = "Create Post", description = "Create Post API")
	@ApiResponses(
		value = [
			ApiResponse(
				responseCode = "201",
				description = "Create Post",
				content = arrayOf(Content(schema = Schema(implementation = CreatePostResponse::class)))
			), ApiResponse(
				responseCode = "400",
				description = "Request Body Valid Error",
				content = arrayOf(Content(schema = Schema(implementation = ErrorResponse::class)))
			), ApiResponse(
				responseCode = "401",
				description = "Full authentication is required to access this resource",
				content = arrayOf(Content(schema = Schema(implementation = ErrorResponse::class)))
			)
		]
	)
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	@PutMapping
	fun createPost(
		@RequestBody @Valid createPostRequest: CreatePostRequest,
		@CurrentUser securityUserItem: SecurityUserItem
	): ResponseEntity<CreatePostResponse> {
		val input = PostPresentationMapper.toCreatePostInput(createPostRequest, securityUserItem.userId)
		val output = createPostUseCase.execute(input)
		val response = PostPresentationMapper.toCreatePostResponse(output)
		return ResponseEntity.status(HttpStatus.CREATED).body(response)
	}

	@Operation(operationId = "getPostList", summary = "Get Post List", description = "Get Post List API")
	@ApiResponses(
		value = [
			ApiResponse(
				responseCode = "200",
				description = "OK",
				content = arrayOf(Content(array = ArraySchema(schema = Schema(implementation = GetPostResponse::class))))
			)
		]
	)
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	@GetMapping
	fun getPostList(
		@PageableDefault
		@Parameter(hidden = true)
		pageable: Pageable
	): ResponseEntity<Page<GetPostResponse>> {
		val input = PostPresentationMapper.toGetPostListInput(pageable)
		val output = getPostListUseCase.execute(input)
		val response = PostPresentationMapper.toGetPostListResponse(output)
		return ResponseEntity.ok(response)
	}

	@Operation(
		operationId = "getExcludeUsersPosts",
		summary = "Get Exclude Users By Post List",
		description = "Get Exclude Users By Post List API"
	)
	@ApiResponses(
		value = [
			ApiResponse(
				responseCode = "200",
				description = "OK",
				content = arrayOf(Content(array = ArraySchema(schema = Schema(implementation = GetPostResponse::class))))
			), ApiResponse(
				responseCode = "400",
				description = "Request Body Valid Error",
				content = arrayOf(Content(schema = Schema(implementation = ErrorResponse::class)))
			), ApiResponse(
				responseCode = "401",
				description = "Full authentication is required to access this resource",
				content = arrayOf(Content(schema = Schema(implementation = ErrorResponse::class)))
			)
		]
	)
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	@GetMapping("/exclude-users")
	fun getExcludeUsersPostList(
		@RequestParam(name = "userIds", required = true) userIds: List<Long>,
		@PageableDefault
		@Parameter(hidden = true)
		pageable: Pageable
	): ResponseEntity<Page<GetPostResponse>> {
		val input = PostPresentationMapper.toGetExcludeUsersPostsInput(userIds, pageable)
		val output = getExcludeUsersPostsUseCase.execute(input)
		val response = PostPresentationMapper.toGetPostListResponse(output)
		return ResponseEntity.ok(response)
	}

	@Operation(operationId = "getPostById", summary = "Get Post", description = "Get Post By Post Id API")
	@ApiResponses(
		value = [
			ApiResponse(
				responseCode = "200",
				description = "OK",
				content = arrayOf(Content(schema = Schema(implementation = GetPostResponse::class)))
			), ApiResponse(
				responseCode = "401",
				description = "Full authentication is required to access this resource",
				content = arrayOf(Content(schema = Schema(implementation = ErrorResponse::class)))
			), ApiResponse(
				responseCode = "404",
				description = "Post Not Found postId = {postId}",
				content = arrayOf(Content(schema = Schema(implementation = ErrorResponse::class)))
			)
		]
	)
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	@GetMapping("/{postId}")
	fun getPostById(
		@PathVariable("postId", required = true) postId: Long
	): ResponseEntity<GetPostResponse> {
		val input = PostPresentationMapper.toGetPostByIdInput(postId)
		val output = getPostByIdUseCase.execute(input)
		val response = PostPresentationMapper.toGetPostResponse(output)
		return ResponseEntity.ok(response)
	}

	@Operation(operationId = "updatePost", summary = "Update Post", description = "Update Post API")
	@ApiResponses(
		value = [
			ApiResponse(
				responseCode = "200",
				description = "OK",
				content = arrayOf(Content(schema = Schema(implementation = UpdatePostResponse::class)))
			), ApiResponse(
				responseCode = "400",
				description = "Request Body Valid Error",
				content = arrayOf(Content(schema = Schema(implementation = ErrorResponse::class)))
			), ApiResponse(
				responseCode = "401",
				description = "Full authentication is required to access this resource",
				content = arrayOf(Content(schema = Schema(implementation = ErrorResponse::class)))
			), ApiResponse(
				responseCode = "404",
				description = "Post Not Found postId = {postId}",
				content = arrayOf(Content(schema = Schema(implementation = ErrorResponse::class)))
			)
		]
	)
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	@PatchMapping("/{postId}")
	fun updatePost(
		@RequestBody @Valid updatePostRequest: UpdatePostRequest,
		@PathVariable("postId", required = true) postId: Long,
		@CurrentUser securityUserItem: SecurityUserItem
	): ResponseEntity<UpdatePostResponse> {
		val input = PostPresentationMapper.toUpdatePostInput(postId, updatePostRequest, securityUserItem.userId)
		val output = updatePostUseCase.execute(input)
		val response = PostPresentationMapper.toUpdatePostResponse(output)
		return ResponseEntity.ok(response)
	}

	@Operation(operationId = "deletePost", summary = "Delete Post", description = "Delete Post API")
	@ApiResponses(
		value = [
			ApiResponse(
				responseCode = "204",
				description = "Delete Post"
			), ApiResponse(
				responseCode = "401",
				description = "Full authentication is required to access this resource",
				content = arrayOf(Content(schema = Schema(implementation = ErrorResponse::class)))
			)
		]
	)
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	@DeleteMapping("/{postId}")
	fun deletePost(
		@PathVariable("postId", required = true) postId: Long,
		@CurrentUser securityUserItem: SecurityUserItem
	): ResponseEntity<Void> {
		val input = PostPresentationMapper.toDeletePostInput(postId, securityUserItem.userId)
		deletePostUseCase.execute(input)
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
	}
}
