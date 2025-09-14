package com.example.demo.kafka.adapter

import com.example.demo.kafka.provider.KafkaTopicMetaProvider
import com.example.demo.user.event.UserDeleteEventHandler
import com.example.demo.user.model.UserDeleteItem
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.dao.DataAccessException
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Component
class UserDeleteKafkaAdapter(
	private val userDeleteEventHandler: UserDeleteEventHandler
) {
	@KafkaListener(
		topics = [KafkaTopicMetaProvider.USER_DELETE_TOPIC],
		groupId = KafkaTopicMetaProvider.USER_DELETE_GROUP,
		containerFactory = KafkaTopicMetaProvider.USER_DELETE_CONTAINER_FACTORY
	)
	@Retryable(
		value = [DataAccessException::class],
		maxAttempts = 3,
		backoff = Backoff(delay = 2000)
	)
	fun consume(payload: UserDeleteItem) {
		logger.debug { "Received user delete event for user: ${payload.email}" }

		runCatching {
			userDeleteEventHandler.handle(payload)
		}.onFailure { exception ->
			logger.error(exception) { "Failed to process user delete event: ${payload.email}" }
			when (exception) {
				is DataAccessException -> throw exception
				else -> {
					logger.warn { "Non-retryable error: ${exception.javaClass.simpleName}" }
					throw exception
				}
			}
		}
	}
}
