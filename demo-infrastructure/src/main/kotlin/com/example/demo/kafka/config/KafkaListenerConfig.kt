package com.example.demo.kafka.config

import com.example.demo.kafka.provider.KafkaConsumerFactoryProvider
import com.example.demo.mail.model.MailPayload
import com.example.demo.user.model.UserDeleteItem
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory

@Configuration
class KafkaListenerConfig(
	private val factoryProvider: KafkaConsumerFactoryProvider
) {
	@Bean
	fun mailKafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, MailPayload> = factoryProvider.createFactory(MailPayload::class.java)

	@Bean
	fun userDeleteKafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, UserDeleteItem> =
		factoryProvider.createFactory(UserDeleteItem::class.java)
}
