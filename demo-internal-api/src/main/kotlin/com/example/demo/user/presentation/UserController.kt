package com.example.demo.user.presentation

import com.example.demo.common.response.ErrorResponse
import com.example.demo.notification.annotation.NotifyOnRequest
import com.example.demo.security.annotation.CurrentUser
import com.example.demo.security.model.SecurityUserItem
import com.example.demo.user.presentation.dto.request.CreateUserRequest
import com.example.demo.user.presentation.dto.request.UpdateUserRequest
import com.example.demo.user.presentation.dto.response.CreateUserResponse
import com.example.demo.user.presentation.dto.response.GetUserResponse
import com.example.demo.user.presentation.dto.response.UpdateMeResponse
import com.example.demo.user.presentation.dto.response.UpdateUserResponse
import com.example.demo.user.presentation.mapper.UserPresentationMapper
import com.example.demo.user.usecase.CreateUserUseCase
import com.example.demo.user.usecase.DeleteUserUseCase
import com.example.demo.user.usecase.GetUserByIdUseCase
import com.example.demo.user.usecase.GetUserListUseCase
import com.example.demo.user.usecase.UpdateMeUseCase
import com.example.demo.user.usecase.UpdateUserUseCase
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
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "User", description = "User API")
@RestController
@RequestMapping("/api/v1/users")
class UserController(
	private val getUserByIdUseCase: GetUserByIdUseCase,
	private val getUserListUseCase: GetUserListUseCase,
	private val createUserUseCase: CreateUserUseCase,
	private val updateUserUseCase: UpdateUserUseCase,
	private val updateMeUseCase: UpdateMeUseCase,
	private val deleteUserUseCase: DeleteUserUseCase
) {
	@Operation(operationId = "getUserById", summary = "Get User", description = "Get User By User Id API")
	@ApiResponses(
		value = [
			ApiResponse(
				responseCode = "200",
				description = "OK",
				content = arrayOf(Content(schema = Schema(implementation = GetUserResponse::class)))
			), ApiResponse(
				responseCode = "401",
				description = "Full authentication is required to access this resource",
				content = arrayOf(Content(schema = Schema(implementation = ErrorResponse::class)))
			), ApiResponse(
				responseCode = "404",
				description = "User Not Found userId = {userId} or email = {email}",
				content = arrayOf(Content(schema = Schema(implementation = ErrorResponse::class)))
			)
		]
	)
	@GetMapping("/{userId}")
	fun getUserById(
		@PathVariable("userId", required = true) userId: Long
	): ResponseEntity<GetUserResponse> {
		val input = UserPresentationMapper.toGetUserByIdInput(userId)
		val userOutput = getUserByIdUseCase.execute(input)
		val response = UserPresentationMapper.toGetUserResponse(userOutput)
		return ResponseEntity.ok(response)
	}

	@Operation(operationId = "getUserList", summary = "Get User List", description = "Get User List API")
	@ApiResponses(
		value = [
			ApiResponse(
				responseCode = "200",
				description = "OK",
				content = arrayOf(Content(array = ArraySchema(schema = Schema(implementation = GetUserResponse::class))))
			)
		]
	)
	@GetMapping
	fun getUserList(
		@PageableDefault
		@Parameter(hidden = true)
		pageable: Pageable
	): ResponseEntity<Page<GetUserResponse>> {
		val input = UserPresentationMapper.toGetUserListInput(pageable)
		val output = getUserListUseCase.execute(input)
		val response = UserPresentationMapper.toGetUserListResponse(output)
		return ResponseEntity.ok(response)
	}

	@Operation(operationId = "createUser", summary = "Create User", description = "Create User API")
	@ApiResponses(
		value = [
			ApiResponse(
				responseCode = "201",
				description = "Created",
				content = arrayOf(Content(schema = Schema(implementation = CreateUserResponse::class)))
			),
			ApiResponse(
				responseCode = "400",
				description = "Field Valid Error",
				content = arrayOf(Content(schema = Schema(implementation = ErrorResponse::class)))
			),
			ApiResponse(
				responseCode = "409",
				description = "Already User Exist userId = {userId} or email = {email}",
				content = arrayOf(Content(schema = Schema(implementation = ErrorResponse::class)))
			)
		]
	)
	@PostMapping("/register")
	fun createUser(
		@RequestBody @Valid createUserRequest: CreateUserRequest
	): ResponseEntity<CreateUserResponse> {
		val input = UserPresentationMapper.toCreateUserInput(createUserRequest)
		val userOutput = createUserUseCase.execute(input)

		val response = UserPresentationMapper.toCreateUserResponse(userOutput)
		return ResponseEntity.status(HttpStatus.CREATED).body(response)
	}

	@Operation(operationId = "updateUser", summary = "Update User", description = "Update User API")
	@ApiResponses(
		value = [
			ApiResponse(
				responseCode = "200",
				description = "OK",
				content = arrayOf(Content(schema = Schema(implementation = UpdateUserResponse::class)))
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
				description = "User Not Found userId = {userId} or email = {email}",
				content = arrayOf(Content(schema = Schema(implementation = ErrorResponse::class)))
			)
		]
	)
	@PatchMapping("/{userId}")
	fun updateUser(
		@RequestBody @Valid @NotifyOnRequest updateUserRequest: UpdateUserRequest,
		@PathVariable("userId", required = true) userId: Long
	): ResponseEntity<UpdateUserResponse> {
		val input = UserPresentationMapper.toUpdateUserInput(userId, updateUserRequest)
		val output = updateUserUseCase.execute(input)
		val response = UserPresentationMapper.toUpdateUserResponse(output)
		return ResponseEntity.ok(response)
	}

	@Operation(operationId = "updateMe", summary = "Update Me", description = "Update Me API")
	@ApiResponses(
		value = [
			ApiResponse(
				responseCode = "200",
				description = "OK",
				content = arrayOf(Content(schema = Schema(implementation = UpdateMeResponse::class)))
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
				description = "User Not Found userId = {userId} or email = {email}",
				content = arrayOf(Content(schema = Schema(implementation = ErrorResponse::class)))
			)
		]
	)
	@PatchMapping
	fun updateMe(
		@RequestBody @Valid @NotifyOnRequest updateUserRequest: UpdateUserRequest,
		@CurrentUser securityUserItem: SecurityUserItem
	): ResponseEntity<UpdateMeResponse> {
		val input = UserPresentationMapper.toUpdateMeInput(securityUserItem.userId, updateUserRequest)
		val output = updateMeUseCase.execute(input)

		val response = UserPresentationMapper.toUpdateMeResponse(output)
		return ResponseEntity.ok(response)
	}

	@Operation(operationId = "deleteUser", summary = "Delete User", description = "Delete User API")
	@ApiResponses(
		value = [
			ApiResponse(
				responseCode = "204",
				description = "No Content"
			), ApiResponse(
				responseCode = "401",
				description = "Full authentication is required to access this resource",
				content = arrayOf(Content(schema = Schema(implementation = ErrorResponse::class)))
			)
		]
	)
	@DeleteMapping("/{userId}")
	fun deleteUser(
		@PathVariable("userId", required = true) userId: Long
	): ResponseEntity<Void> {
		val input = UserPresentationMapper.toDeleteUserInput(userId)
		deleteUserUseCase.execute(input)
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
	}
}
