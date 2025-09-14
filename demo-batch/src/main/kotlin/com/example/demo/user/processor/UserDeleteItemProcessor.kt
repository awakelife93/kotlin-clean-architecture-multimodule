package com.example.demo.user.processor

import com.example.demo.user.model.UserDeleteItem
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.batch.core.annotation.AfterProcess
import org.springframework.batch.core.annotation.BeforeProcess
import org.springframework.batch.core.annotation.OnProcessError
import org.springframework.batch.item.ItemProcessor
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Component
class UserDeleteItemProcessor : ItemProcessor<UserDeleteItem, UserDeleteItem> {
	override fun process(item: UserDeleteItem): UserDeleteItem {
		logger.info { "Processing user: ${item.id}" }
		return item
	}

	@BeforeProcess
	fun beforeProcess(item: UserDeleteItem) {
		logger.info { "Before processing: $item" }
	}

	@AfterProcess
	fun afterProcess(
		item: UserDeleteItem,
		result: UserDeleteItem?
	) {
		logger.info { "After processing: $item -> $result" }
	}

	@OnProcessError
	fun onError(
		item: UserDeleteItem,
		exception: Exception
	) {
		logger.error { "Error processing $item: ${exception.message}" }
	}
}
