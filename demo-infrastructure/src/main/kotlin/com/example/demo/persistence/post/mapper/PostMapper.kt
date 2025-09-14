package com.example.demo.persistence.post.mapper

import com.example.demo.persistence.post.entity.PostEntity
import com.example.demo.post.model.Post
import org.springframework.stereotype.Component

@Component
class PostMapper {
	fun toDomain(entity: PostEntity): Post =
		Post(
			id = entity.id,
			title = entity.title,
			subTitle = entity.subTitle,
			content = entity.content,
			userId = entity.userId,
			createdDt = entity.createdDt,
			updatedDt = entity.updatedDt,
			deletedDt = entity.deletedDt
		)

	fun toEntity(domain: Post): PostEntity =
		PostEntity(
			title = domain.title,
			subTitle = domain.subTitle,
			content = domain.content,
			userId = domain.userId
		).apply {
			if (domain.id != 0L) {
				this.id = domain.id
			}
			this.createdDt = domain.createdDt
			this.updatedDt = domain.updatedDt
			this.deletedDt = domain.deletedDt
		}

	fun updateEntity(
		entity: PostEntity,
		domain: Post
	): PostEntity {
		entity.title = domain.title
		entity.subTitle = domain.subTitle
		entity.content = domain.content
		entity.updatedDt = domain.updatedDt
		entity.deletedDt = domain.deletedDt
		return entity
	}
}
