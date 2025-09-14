package com.example.demo.user.event

import com.example.demo.kafka.provider.KafkaTopicMetaProvider
import com.example.demo.mail.model.MailPayload
import com.example.demo.notification.NotificationService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

private val logger = KotlinLogging.logger {}

@Component
class UserEventHandler(
	private val notificationService: NotificationService,
	private val mailKafkaTemplate: KafkaTemplate<String, MailPayload>
) {
	@Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	fun handleWelcomeSignUpEvent(welcomeSignUpEvent: UserEvent.WelcomeSignUpEvent) {
		logger.info { "=== WelcomeSignUpEvent received: $welcomeSignUpEvent ===" }

		runCatching {
			val payload =
				MailPayload.of(
					to = welcomeSignUpEvent.email,
					subject = "Welcome ${welcomeSignUpEvent.name}!",
					body = "Welcome to our service! Thank you for signing up."
				)

			logger.info { "Sending mail payload to Kafka: $payload" }
			val future = mailKafkaTemplate.send(KafkaTopicMetaProvider.MAIL_TOPIC, payload)

			future.whenComplete { result, exception ->
				if (exception != null) {
					logger.error(exception) { "Failed to send to Kafka (Async): ${exception.message}" }
				} else {
					logger.info { "Successfully sent to Kafka: ${result?.recordMetadata}" }
				}
			}
		}.onFailure { exception ->
			logger.error(exception) { "Failed to send message to Kafka (Sync): ${exception.message}" }

			notificationService.sendCriticalAlert(
				title = "Failed to send message to Kafka (handleWelcomeSignUpEvent)",
				messages = listOf("Failed to send message to Kafka: ${exception.message} / $welcomeSignUpEvent")
			)

			throw exception
		}
	}
}
