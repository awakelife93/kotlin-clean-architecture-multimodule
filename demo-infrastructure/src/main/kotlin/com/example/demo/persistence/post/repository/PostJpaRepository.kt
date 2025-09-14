package com.example.demo.persistence.post.repository

import com.example.demo.persistence.post.entity.PostEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface PostJpaRepository :
	JpaRepository<PostEntity, Long>,
	PostJpaRepositoryCustom {
	fun findByUserId(
		userId: Long,
		pageable: Pageable
	): Page<PostEntity>

	fun findAllByUserId(userId: Long): List<PostEntity>

	fun deleteByUserId(userId: Long)

	@Modifying
	@Query(
		value = """
            DELETE FROM "post"
            WHERE id = :postId
        """,
		nativeQuery = true
	)
	fun hardDeleteById(
		@Param("postId") postId: Long
	): Int

	@Modifying
	@Query(
		value = """
            DELETE FROM "post"
            WHERE user_id = :userId
        """,
		nativeQuery = true
	)
	fun hardDeleteByUserId(
		@Param("userId") userId: Long
	): Int
}
