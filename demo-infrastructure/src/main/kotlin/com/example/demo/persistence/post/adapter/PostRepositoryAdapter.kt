package com.example.demo.persistence.post.adapter

import com.example.demo.persistence.post.mapper.PostMapper
import com.example.demo.persistence.post.repository.PostJpaRepository
import com.example.demo.post.exception.PostNotFoundException
import com.example.demo.post.model.Post
import com.example.demo.post.port.PostPort
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class PostRepositoryAdapter(
	private val postJpaRepository: PostJpaRepository,
	private val postMapper: PostMapper
) : PostPort {
	override fun save(post: Post): Post {
		val entity =
			if (post.id != 0L) {
				val existingEntity =
					postJpaRepository.findByIdOrNull(post.id)
						?: throw PostNotFoundException(post.id)
				postMapper.updateEntity(existingEntity, post)
			} else {
				postMapper.toEntity(post)
			}

		val savedEntity = postJpaRepository.save(entity)
		return postMapper.toDomain(savedEntity)
	}

	override fun deleteById(id: Long) {
		postJpaRepository.deleteById(id)
	}

	override fun deleteByUserId(userId: Long) {
		postJpaRepository.deleteByUserId(userId)
	}

	override fun hardDeleteById(id: Long) {
		postJpaRepository.hardDeleteById(id)
	}

	override fun hardDeleteByUserId(userId: Long) {
		postJpaRepository.hardDeleteByUserId(userId)
	}

	override fun findOneById(id: Long): Post? =
		postJpaRepository.findByIdOrNull(id)?.let {
			postMapper.toDomain(it)
		}

	override fun findAll(pageable: Pageable): Page<Post> =
		postJpaRepository.findAll(pageable).map {
			postMapper.toDomain(it)
		}

	override fun findOneByUserId(
		userId: Long,
		pageable: Pageable
	): Page<Post> =
		postJpaRepository.findByUserId(userId, pageable).map {
			postMapper.toDomain(it)
		}

	override fun findAllByUserId(userId: Long): List<Post> =
		postJpaRepository.findAllByUserId(userId).map {
			postMapper.toDomain(it)
		}

	override fun findAllByUserId(
		userId: Long,
		pageable: Pageable
	): Page<Post> =
		postJpaRepository.findByUserId(userId, pageable).map {
			postMapper.toDomain(it)
		}

	override fun findExcludeUsersPosts(
		userIds: List<Long>,
		pageable: Pageable
	): Page<Post> =
		postJpaRepository.findExcludeUsersPosts(userIds, pageable).map {
			postMapper.toDomain(it)
		}

	override fun existsById(id: Long): Boolean = postJpaRepository.existsById(id)
}
