package com.example.demo.persistence.user.adapter

import com.example.demo.persistence.user.mapper.UserMapper
import com.example.demo.persistence.user.repository.UserJpaRepository
import com.example.demo.user.exception.UserNotFoundException
import com.example.demo.user.model.User
import com.example.demo.user.port.UserPort
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository

@Repository
class UserRepositoryAdapter(
	private val jpaRepository: UserJpaRepository,
	private val userMapper: UserMapper
) : UserPort {
	override fun findOneById(userId: Long): User? =
		jpaRepository.findOneById(userId)?.let {
			userMapper.toDomain(it)
		}

	override fun findOneByEmail(email: String): User? =
		jpaRepository.findOneByEmail(email)?.let {
			userMapper.toDomain(it)
		}

	override fun findAll(pageable: Pageable): Page<User> =
		jpaRepository.findAll(pageable).map {
			userMapper.toDomain(it)
		}

	override fun existsByEmail(email: String): Boolean = jpaRepository.existsByEmail(email)

	override fun existsById(userId: Long): Boolean = jpaRepository.existsById(userId)

	override fun save(user: User): User {
		val entity =
			if (user.id != 0L) {
				val existingEntity =
					jpaRepository
						.findById(user.id)
						.orElseThrow { UserNotFoundException(user.id) }
				userMapper.updateEntity(existingEntity, user)
			} else {
				userMapper.toEntity(user)
			}

		val savedEntity = jpaRepository.save(entity)
		return userMapper.toDomain(savedEntity)
	}

	override fun deleteById(userId: Long) {
		jpaRepository.deleteById(userId)
	}

	override fun hardDeleteById(userId: Long) {
		jpaRepository.hardDeleteById(userId)
	}
}
