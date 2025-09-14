package com.example.demo.persistence.auth.provider

import com.example.demo.security.model.SecurityUserItem
import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.util.Date
import java.util.concurrent.TimeUnit
import javax.crypto.SecretKey

@Component
class JWTProvider(
	private val userDetailsServiceAdapter: UserDetailsService,
	@Value("\${auth.jwt.secret}") private val secretKeyString: String,
	@Value("\${auth.jwt.access-expire}") private val accessExpireTime: Long,
	@Value("\${auth.jwt.refresh-expire}") val refreshExpireTime: Long = 0L
) {
	private val secretKey: SecretKey by lazy {
		Keys.hmacShaKeyFor(secretKeyString.toByteArray(StandardCharsets.UTF_8))
	}

	fun createAccessToken(securityUserItem: SecurityUserItem): String = createToken(securityUserItem, true)

	fun createRefreshToken(securityUserItem: SecurityUserItem): String = createToken(securityUserItem, false)

	private fun createToken(
		securityUserItem: SecurityUserItem,
		isAccessToken: Boolean
	): String {
		val expireTime = if (isAccessToken) accessExpireTime else refreshExpireTime
		val now = Date()
		val expiration = Date(now.time + TimeUnit.SECONDS.toMillis(expireTime))

		val claims =
			Jwts
				.claims()
				.subject(securityUserItem.userId.toString())
				.add("email", securityUserItem.email)
				.add("role", securityUserItem.role)
				.build()

		return Jwts
			.builder()
			.claims(claims)
			.issuedAt(now)
			.expiration(expiration)
			.signWith(secretKey)
			.compact()
	}

	fun refreshAccessToken(
		securityUserItem: SecurityUserItem,
		refreshToken: String
	): String {
		getAuthentication(refreshToken)
		return createAccessToken(securityUserItem)
	}

	fun validateToken(token: String): Boolean {
		val authentication = getAuthentication(token)
		return authentication.isAuthenticated
	}

	fun getAuthentication(token: String): UsernamePasswordAuthenticationToken = getAuthentication(token, false)

	fun getAuthentication(
		token: String,
		isRefresh: Boolean
	): UsernamePasswordAuthenticationToken {
		val claims = generateClaims(token, isRefresh)
		val userDetails = userDetailsServiceAdapter.loadUserByUsername(claims.subject)

		return UsernamePasswordAuthenticationToken(
			userDetails,
			null,
			userDetails.authorities
		)
	}

	private fun generateClaims(
		token: String,
		isRefresh: Boolean
	): Claims =
		runCatching {
			Jwts
				.parser()
				.verifyWith(secretKey)
				.build()
				.parseSignedClaims(token)
				.payload
		}.getOrElse {
			if (it is ExpiredJwtException && isRefresh) it.claims else throw it
		}
}
