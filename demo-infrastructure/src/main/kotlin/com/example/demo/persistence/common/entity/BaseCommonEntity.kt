package com.example.demo.persistence.common.entity

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.persistence.Column
import jakarta.persistence.EntityListeners
import jakarta.persistence.MappedSuperclass
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class BaseCommonEntity(
	@CreatedBy
	@Column(nullable = false, updatable = false)
	var createdBy: Long = 0L,
	@LastModifiedBy
	@Column(nullable = false)
	var updatedBy: Long = 0L,
	@Column
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
	var deletedDt: LocalDateTime? = null
) : BaseEntity()
