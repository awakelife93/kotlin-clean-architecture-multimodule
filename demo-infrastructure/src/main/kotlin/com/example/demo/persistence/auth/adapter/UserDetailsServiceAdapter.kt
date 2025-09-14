package com.example.demo.persistence.auth.adapter

import com.example.demo.persistence.user.entity.UserEntity
import com.example.demo.persistence.user.mapper.UserMapper
import com.example.demo.persistence.user.repository.UserJpaRepository
import com.example.demo.security.model.SecurityUserItem
import com.example.demo.user.exception.UserNotFoundException
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service

@Service
class UserDetailsServiceAdapter(
	private val userJpaRepository: UserJpaRepository,
	private val userMapper: UserMapper
) : UserDetailsService {
	@Throws(UserNotFoundException::class)
	override fun loadUserByUsername(userId: String): UserDetails {
		val userIdLong = userId.toLong()
		val user: UserEntity =
			userJpaRepository
				.findOneById(userIdLong)
				?: throw UserNotFoundException(userIdLong)

		return UserAdapter(
			SecurityUserItem.from(
				userMapper.toDomain(
					user
				)
			)
		)
	}
}
