package com.example.demo.persistence.user.repository

import com.example.demo.persistence.user.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface UserJpaRepository : JpaRepository<UserEntity, Long> {
	fun findOneById(userId: Long): UserEntity?

	fun findOneByEmail(email: String): UserEntity?

	fun existsByEmail(email: String): Boolean

	@Modifying
	@Query(
		value = """
            DELETE FROM "user"
            WHERE id = :userId
        """,
		nativeQuery = true
	)
	fun hardDeleteById(
		@Param("userId") userId: Long
	): Int
}
