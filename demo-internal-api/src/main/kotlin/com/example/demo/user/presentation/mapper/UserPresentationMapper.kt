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
		with(request) {
			CreateUserInput(
				name = name,
				email = email,
				password = password
			)
		}

	fun toGetUserByIdInput(userId: Long): GetUserByIdInput = GetUserByIdInput(userId = userId)

	fun toGetUserListInput(pageable: Pageable): GetUserListInput = GetUserListInput(pageable = pageable)

	fun toDeleteUserInput(userId: Long): DeleteUserInput = DeleteUserInput(userId = userId)

	fun toUpdateUserInput(
		userId: Long,
		request: UpdateUserRequest
	): UpdateUserInput =
		with(request) {
			UpdateUserInput(
				userId = userId,
				name = name,
				role = role
			)
		}

	fun toUpdateMeInput(
		userId: Long,
		request: UpdateUserRequest
	): UpdateMeInput =
		with(request) {
			UpdateMeInput(
				userId = userId,
				name = name
			)
		}

	fun toGetUserResponse(output: UserOutput.BaseUserOutput): GetUserResponse =
		with(output) {
			GetUserResponse(
				userId = id,
				name = name,
				email = email,
				role = role,
				createDt = createdDt,
				updateDt = updatedDt
			)
		}

	fun toCreateUserResponse(output: UserOutput.AuthenticatedUserOutput): CreateUserResponse =
		with(output) {
			CreateUserResponse(
				userId = id,
				name = name,
				email = email,
				role = role,
				accessToken = accessToken,
				createDt = createdDt,
				updateDt = updatedDt
			)
		}

	fun toUpdateUserResponse(output: UserOutput.BaseUserOutput): UpdateUserResponse =
		with(output) {
			UpdateUserResponse(
				userId = id,
				name = name,
				email = email,
				role = role,
				createDt = createdDt,
				updateDt = updatedDt
			)
		}

	fun toUpdateMeResponse(output: UserOutput.AuthenticatedUserOutput): UpdateMeResponse =
		with(output) {
			UpdateMeResponse(
				userId = id,
				name = name,
				email = email,
				role = role,
				accessToken = accessToken,
				createDt = createdDt,
				updateDt = updatedDt
			)
		}

	fun toGetUserListResponse(output: UserOutput.UserPageListOutput): Page<GetUserResponse> =
		output.users.map { userOutput ->
			with(userOutput) {
				GetUserResponse(
					userId = id,
					name = name,
					email = email,
					role = role,
					createDt = createdDt,
					updateDt = updatedDt
				)
			}
		}
}
