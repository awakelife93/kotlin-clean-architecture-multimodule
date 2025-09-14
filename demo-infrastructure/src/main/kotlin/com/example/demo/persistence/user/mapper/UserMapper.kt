package com.example.demo.persistence.user.mapper

import com.example.demo.persistence.user.entity.UserEntity
import com.example.demo.user.model.User
import org.springframework.stereotype.Component

@Component
class UserMapper {
	fun toDomain(entity: UserEntity): User =
		User(
			id = entity.id,
			name = entity.name,
			email = entity.email,
			password = entity.password,
			role = entity.role,
			createdDt = entity.createdDt,
			updatedDt = entity.updatedDt,
			deletedDt = entity.deletedDt
		)

	fun toEntity(domain: User): UserEntity =
		UserEntity(
			name = domain.name,
			email = domain.email,
			password = domain.password,
			role = domain.role
		).apply {
			if (domain.id != 0L) {
				this.id = domain.id
			}
			this.createdDt = domain.createdDt
			this.updatedDt = domain.updatedDt
			this.deletedDt = domain.deletedDt
		}

	fun updateEntity(
		entity: UserEntity,
		domain: User
	): UserEntity {
		entity.name = domain.name
		entity.email = domain.email
		entity.password = domain.password
		entity.role = domain.role
		entity.updatedDt = domain.updatedDt
		entity.deletedDt = domain.deletedDt
		return entity
	}
}
