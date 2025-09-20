package com.example.demo.persistence.user.adapter

import com.example.demo.persistence.user.mapper.UserMapper
import com.example.demo.persistence.user.repository.UserJpaRepository
import com.example.demo.user.exception.UserNotFoundException
import com.example.demo.user.model.User
import com.example.demo.user.port.UserPort
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class UserRepositoryAdapter(
	private val userJpaRepository: UserJpaRepository,
	private val userMapper: UserMapper
) : UserPort {
	override fun findOneById(userId: Long): User? =
		userJpaRepository.findOneById(userId)?.let {
			userMapper.toDomain(it)
		}

	override fun findOneByEmail(email: String): User? =
		userJpaRepository.findOneByEmail(email)?.let {
			userMapper.toDomain(it)
		}

	override fun findAll(pageable: Pageable): Page<User> =
		userJpaRepository.findAll(pageable).map {
			userMapper.toDomain(it)
		}

	override fun existsByEmail(email: String): Boolean = userJpaRepository.existsByEmail(email)

	override fun existsById(userId: Long): Boolean = userJpaRepository.existsById(userId)

	override fun save(user: User): User {
		val entity =
			if (user.id != 0L) {
				val existingEntity =
					userJpaRepository
						.findByIdOrNull(user.id) ?: throw UserNotFoundException(user.id)
				userMapper.updateEntity(existingEntity, user)
			} else {
				userMapper.toEntity(user)
			}

		val savedEntity = userJpaRepository.save(entity)
		return userMapper.toDomain(savedEntity)
	}

	override fun deleteById(userId: Long) {
		userJpaRepository.deleteById(userId)
	}

	override fun hardDeleteById(userId: Long) {
		userJpaRepository.hardDeleteById(userId)
	}
}
