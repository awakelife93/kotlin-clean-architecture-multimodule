package com.example.demo.auth.presentation.mapper

import com.example.demo.auth.port.input.RefreshAccessTokenInput
import com.example.demo.auth.port.input.SignInInput
import com.example.demo.auth.port.input.SignOutInput
import com.example.demo.auth.port.output.AuthOutput
import com.example.demo.auth.presentation.dto.request.RefreshAccessTokenRequest
import com.example.demo.auth.presentation.dto.request.SignInRequest
import com.example.demo.auth.presentation.dto.response.RefreshAccessTokenResponse
import com.example.demo.auth.presentation.dto.response.SignInResponse

object AuthPresentationMapper {
	fun toSignInInput(request: SignInRequest): SignInInput =
		SignInInput(
			email = request.email,
			password = request.password
		)

	fun toSignInResponse(output: AuthOutput): SignInResponse =
		SignInResponse(
			accessToken = output.accessToken,
			userId = output.userId,
			role = output.role,
			name = output.name,
			email = output.email
		)

	fun toRefreshInput(request: RefreshAccessTokenRequest): RefreshAccessTokenInput =
		RefreshAccessTokenInput(
			refreshToken = request.refreshToken
		)

	fun toRefreshResponse(output: AuthOutput): RefreshAccessTokenResponse =
		RefreshAccessTokenResponse(
			accessToken = output.accessToken
		)

	fun toSignOutInput(userId: Long): SignOutInput =
		SignOutInput(
			userId = userId
		)
}
