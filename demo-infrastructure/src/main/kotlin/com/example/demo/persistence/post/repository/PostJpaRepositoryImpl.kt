package com.example.demo.persistence.post.repository

import com.example.demo.persistence.post.entity.PostEntity
import com.example.demo.persistence.post.entity.QPostEntity
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable

class PostJpaRepositoryImpl(
	private val jpaQueryFactory: JPAQueryFactory
) : PostJpaRepositoryCustom {
	override fun findExcludeUsersPosts(
		userIds: List<Long>,
		pageable: Pageable
	): Page<PostEntity> {
		val post = QPostEntity.postEntity

		val whereCondition =
			if (userIds.isNotEmpty()) {
				post.userId
					.notIn(userIds)
					.and(post.deletedDt.isNull)
			} else {
				post.deletedDt.isNull
			}

		val content =
			jpaQueryFactory
				.selectFrom(post)
				.where(whereCondition)
				.offset(pageable.offset)
				.limit(pageable.pageSize.toLong())
				.orderBy(post.id.desc())
				.fetch()

		val totalCount =
			jpaQueryFactory
				.select(post.count())
				.from(post)
				.where(whereCondition)
				.fetchOne() ?: 0L

		return PageImpl(content, pageable, totalCount)
	}
}
