package com.example.demo.kafka.adapter

import com.example.demo.kafka.provider.KafkaTopicMetaProvider
import com.example.demo.mail.event.WelcomeSignUpEventHandler
import com.example.demo.mail.model.MailPayload
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.mail.MailException
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Component
class WelcomeSignUpKafkaAdapter(
	private val welcomeSignUpEventHandler: WelcomeSignUpEventHandler
) {
	@KafkaListener(
		topics = [KafkaTopicMetaProvider.MAIL_TOPIC],
		groupId = KafkaTopicMetaProvider.MAIL_GROUP,
		containerFactory = KafkaTopicMetaProvider.MAIL_CONTAINER_FACTORY
	)
	@Retryable(
		value = [MailException::class],
		maxAttempts = 3,
		backoff = Backoff(delay = 2000)
	)
	fun consume(payload: MailPayload) {
		logger.info { "=== Kafka message received: $payload ===" }
		logger.debug { "Received welcome mail event for: ${payload.to}" }

		runCatching {
			welcomeSignUpEventHandler.handle(payload)
		}.onFailure { exception ->
			logger.error(exception) { "Failed to process welcome mail event: ${payload.to}" }
			when (exception) {
				is MailException -> throw exception
				else -> {
					logger.warn { "Non-retryable error: ${exception.javaClass.simpleName}" }
					throw exception
				}
			}
		}
	}
}
