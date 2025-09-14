package com.example.demo.auth.service

import com.example.demo.auth.port.input.RefreshAccessTokenInput
import com.example.demo.auth.port.input.SignInInput
import com.example.demo.auth.port.output.AuthOutput
import com.example.demo.persistence.auth.adapter.UserAdapter
import com.example.demo.persistence.auth.provider.JWTProvider
import com.example.demo.persistence.auth.provider.TokenProvider
import com.example.demo.user.exception.UserNotFoundException
import com.example.demo.user.exception.UserUnAuthorizedException
import com.example.demo.user.port.UserPort
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class AuthService(
	private val userPort: UserPort,
	private val passwordEncoder: PasswordEncoder,
	private val tokenProvider: TokenProvider,
	private val jwtProvider: JWTProvider
) {
	@Transactional
	fun signIn(input: SignInInput): AuthOutput {
		val user =
			userPort.findOneByEmail(input.email)
				?: throw UserNotFoundException(input.email)

		if (!user.validatePassword(input.password) { rawPassword, encodedPassword ->
				passwordEncoder.matches(rawPassword, encodedPassword)
			}
		) {
			throw UserUnAuthorizedException("User UnAuthorized email = ${input.email}")
		}

		val accessToken = tokenProvider.createFullTokens(user)

		return AuthOutput.fromSignIn(user, accessToken)
	}

	@Transactional
	fun signOut(userId: Long) {
		tokenProvider.deleteRefreshToken(userId)
		SecurityContextHolder.clearContext()
	}

	@Transactional
	fun refreshAccessToken(input: RefreshAccessTokenInput): AuthOutput {
		val authentication = jwtProvider.getAuthentication(input.refreshToken, true)
		val userAdapter = authentication.principal as UserAdapter

		val newAccessToken = tokenProvider.refreshAccessToken(userAdapter.securityUserItem)

		return AuthOutput.fromRefresh(newAccessToken)
	}
}
