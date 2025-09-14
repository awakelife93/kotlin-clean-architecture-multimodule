package com.example.demo.user.presentation.mapper

import com.example.demo.user.port.input.CreateUserInput
import com.example.demo.user.port.input.DeleteUserInput
import com.example.demo.user.port.input.GetUserByIdInput
import com.example.demo.user.port.input.GetUserListInput
import com.example.demo.user.port.input.UpdateMeInput
import com.example.demo.user.port.input.UpdateUserInput
import com.example.demo.user.port.output.UserOutput
import com.example.demo.user.presentation.dto.request.CreateUserRequest
import com.example.demo.user.presentation.dto.request.UpdateUserRequest
import com.example.demo.user.presentation.dto.response.CreateUserResponse
import com.example.demo.user.presentation.dto.response.GetUserResponse
import com.example.demo.user.presentation.dto.response.UpdateMeResponse
import com.example.demo.user.presentation.dto.response.UpdateUserResponse
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

object UserPresentationMapper {
	fun toCreateUserInput(request: CreateUserRequest): CreateUserInput =
		CreateUserInput(
			name = request.name,
			email = request.email,
			password = request.password
		)

	fun toGetUserByIdInput(userId: Long): GetUserByIdInput = GetUserByIdInput(userId = userId)

	fun toGetUserListInput(pageable: Pageable): GetUserListInput = GetUserListInput(pageable = pageable)

	fun toDeleteUserInput(userId: Long): DeleteUserInput = DeleteUserInput(userId = userId)

	fun toUpdateUserInput(
		userId: Long,
		request: UpdateUserRequest
	): UpdateUserInput =
		UpdateUserInput(
			userId = userId,
			name = request.name,
			role = request.role
		)

	fun toUpdateMeInput(
		userId: Long,
		request: UpdateUserRequest
	): UpdateMeInput =
		UpdateMeInput(
			userId = userId,
			name = request.name
		)

	fun toGetUserResponse(output: UserOutput.BaseUserOutput): GetUserResponse =
		GetUserResponse(
			userId = output.id,
			name = output.name,
			email = output.email,
			role = output.role,
			createDt = output.createdDt,
			updateDt = output.updatedDt
		)

	fun toCreateUserResponse(output: UserOutput.AuthenticatedUserOutput): CreateUserResponse =
		CreateUserResponse(
			userId = output.id,
			name = output.name,
			email = output.email,
			role = output.role,
			accessToken = output.accessToken,
			createDt = output.createdDt,
			updateDt = output.updatedDt
		)

	fun toUpdateUserResponse(output: UserOutput.BaseUserOutput): UpdateUserResponse =
		UpdateUserResponse(
			userId = output.id,
			name = output.name,
			email = output.email,
			role = output.role,
			createDt = output.createdDt,
			updateDt = output.updatedDt
		)

	fun toUpdateMeResponse(output: UserOutput.AuthenticatedUserOutput): UpdateMeResponse =
		UpdateMeResponse(
			userId = output.id,
			name = output.name,
			email = output.email,
			role = output.role,
			accessToken = output.accessToken,
			createDt = output.createdDt,
			updateDt = output.updatedDt
		)

	fun toGetUserListResponse(output: UserOutput.UserPageListOutput): Page<GetUserResponse> =
		output.users.map { userOutput ->
			GetUserResponse(
				userId = userOutput.id,
				name = userOutput.name,
				email = userOutput.email,
				role = userOutput.role,
				createDt = userOutput.createdDt,
				updateDt = userOutput.updatedDt
			)
		}
}
