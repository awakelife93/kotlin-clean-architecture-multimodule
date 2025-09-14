package com.example.demo.persistence.post.entity

import com.example.demo.persistence.common.entity.BaseCommonEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Index
import jakarta.persistence.Table
import org.hibernate.annotations.SQLDelete
import org.hibernate.annotations.SQLRestriction

@Entity
@Table(
	name = "\"post\"",
	indexes = [
		Index(name = "idx_post_user_deleted", columnList = "deleted_dt, user_id")
	]
)
@SQLDelete(sql = "UPDATE \"post\" SET deleted_dt = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_dt IS NULL")
data class PostEntity(
	@Column(nullable = false, length = 20)
	var title: String,
	@Column(nullable = false, length = 40)
	var subTitle: String,
	@Column(nullable = false, length = 500)
	var content: String,
	@Column(name = "user_id", nullable = false)
	val userId: Long
) : BaseCommonEntity()
