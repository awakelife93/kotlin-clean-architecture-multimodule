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
		with(request) {
			SignInInput(
				email = email,
				password = password
			)
		}

	fun toSignInResponse(output: AuthOutput): SignInResponse =
		with(output) {
			SignInResponse(
				accessToken = accessToken,
				userId = userId,
				role = role,
				name = name,
				email = email
			)
		}

	fun toRefreshInput(request: RefreshAccessTokenRequest): RefreshAccessTokenInput =
		with(request) {
			RefreshAccessTokenInput(
				refreshToken = refreshToken
			)
		}

	fun toRefreshResponse(output: AuthOutput): RefreshAccessTokenResponse =
		with(output) {
			RefreshAccessTokenResponse(
				accessToken = accessToken
			)
		}

	fun toSignOutInput(userId: Long): SignOutInput = SignOutInput(userId = userId)
}
