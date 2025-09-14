package com.example.demo.persistence.post.repository

import com.example.demo.persistence.post.entity.PostEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface PostJpaRepositoryCustom {
	fun findExcludeUsersPosts(
		userIds: List<Long>,
		pageable: Pageable
	): Page<PostEntity>
}
