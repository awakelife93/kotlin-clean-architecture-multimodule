package com.example.demo.persistence.auth.provider

import com.example.demo.auth.exception.RefreshTokenNotFoundException
import com.example.demo.redis.RedisFactoryProvider
import com.example.demo.security.model.SecurityUserItem
import com.example.demo.user.model.User
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class TokenProvider(
	private val jwtProvider: JWTProvider,
	private val redisFactoryProvider: RedisFactoryProvider
) {
	fun getRefreshToken(userId: Long): String {
		val redisKey: String = redisFactoryProvider.generateSessionKey(userId)
		val refreshToken: String = redisFactoryProvider.get(redisKey) ?: throw RefreshTokenNotFoundException(userId)

		return refreshToken
	}

	fun deleteRefreshToken(userId: Long) {
		val redisKey: String = redisFactoryProvider.generateSessionKey(userId)

		redisFactoryProvider.delete(redisKey)
	}

	fun createRefreshToken(user: User) {
		val redisKey: String = redisFactoryProvider.generateSessionKey(user.id)

		redisFactoryProvider.set(
			redisKey,
			jwtProvider.createRefreshToken(SecurityUserItem.from(user)),
			jwtProvider.refreshExpireTime,
			TimeUnit.SECONDS
		)
	}

	fun createAccessToken(user: User): String = jwtProvider.createAccessToken(SecurityUserItem.from(user))

	fun refreshAccessToken(securityUserItem: SecurityUserItem): String =
		jwtProvider.refreshAccessToken(
			securityUserItem,
			getRefreshToken(securityUserItem.userId)
		)

	fun createFullTokens(user: User): String {
		createRefreshToken(user)
		return createAccessToken(user)
	}
}
