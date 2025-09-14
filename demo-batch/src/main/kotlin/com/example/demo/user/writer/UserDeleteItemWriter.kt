package com.example.demo.user.writer

import com.example.demo.kafka.provider.KafkaTopicMetaProvider
import com.example.demo.user.model.UserDeleteItem
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.batch.core.annotation.AfterStep
import org.springframework.batch.core.annotation.BeforeStep
import org.springframework.batch.item.Chunk
import org.springframework.batch.item.ItemWriter
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Component
class UserDeleteItemWriter(
	private val userDeleteKafkaTemplate: KafkaTemplate<String, UserDeleteItem>
) : ItemWriter<UserDeleteItem> {
	override fun write(items: Chunk<out UserDeleteItem>) {
		items.forEach {
			logger.info { "Hard Deleted User By = ${it.name} ${it.email} ${it.role} ${it.deletedDt}" }

			userDeleteKafkaTemplate.send(KafkaTopicMetaProvider.USER_DELETE_TOPIC, it)
		}
	}

	@BeforeStep
	fun beforeStep() {
		logger.info { ">>> Delete User Writer is starting" }
	}

	@AfterStep
	fun afterStep() {
		logger.info { ">>> Delete User Writer has finished" }
	}
}
