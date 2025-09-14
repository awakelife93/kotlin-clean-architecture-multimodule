package com.example.demo.persistence.auth.adapter

import com.example.demo.auth.port.RefreshTokenPort
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Repository
import java.util.concurrent.TimeUnit

@Repository
class RefreshTokenRepositoryAdapter(
	private val redisTemplate: RedisTemplate<String, String>
) : RefreshTokenPort {
	companion object {
		private const val REFRESH_TOKEN_PREFIX = "refresh_token:"
	}

	override fun save(
		userId: Long,
		token: String,
		expiresIn: Long
	) {
		val key = "$REFRESH_TOKEN_PREFIX$userId"
		redisTemplate.opsForValue().set(key, token, expiresIn, TimeUnit.MILLISECONDS)
	}

	override fun findByUserId(userId: Long): String? {
		val key = "$REFRESH_TOKEN_PREFIX$userId"
		return redisTemplate.opsForValue().get(key)
	}

	override fun deleteByUserId(userId: Long) {
		val key = "$REFRESH_TOKEN_PREFIX$userId"
		redisTemplate.delete(key)
	}

	override fun validateToken(
		userId: Long,
		token: String
	): Boolean {
		val storedToken = findByUserId(userId)
		return storedToken == token
	}
}
