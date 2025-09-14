package com.example.demo.kafka.config

import com.example.demo.kafka.provider.KafkaProducerFactoryProvider
import com.example.demo.mail.model.MailPayload
import com.example.demo.user.model.UserDeleteItem
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.core.KafkaTemplate

@Configuration
@EnableKafka
class KafkaTemplateConfig(
	private val factoryProvider: KafkaProducerFactoryProvider
) {
	@Bean
	fun mailKafkaTemplate(): KafkaTemplate<String, MailPayload> = factoryProvider.createKafkaTemplate(MailPayload::class.java)

	@Bean
	fun userDeleteKafkaTemplate(): KafkaTemplate<String, UserDeleteItem> = factoryProvider.createKafkaTemplate(UserDeleteItem::class.java)

	@Bean
	fun defaultKafkaTemplate(): KafkaTemplate<String, Any> = factoryProvider.createKafkaTemplate(Any::class.java)
}
