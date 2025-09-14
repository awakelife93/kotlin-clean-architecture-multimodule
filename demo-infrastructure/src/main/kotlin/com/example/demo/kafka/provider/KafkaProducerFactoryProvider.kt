package com.example.demo.kafka.provider

import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.support.serializer.JsonSerializer
import org.springframework.stereotype.Component

@Component
class KafkaProducerFactoryProvider {
	private fun <T : Any> createProducerFactory(valueType: Class<T>): ProducerFactory<String, T> {
		val config =
			mutableMapOf(
				ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to "localhost:9092",
				ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
				ProducerConfig.ACKS_CONFIG to "1",
				ProducerConfig.RETRIES_CONFIG to 3,
				ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to getValueSerializer(valueType)
			)

		return DefaultKafkaProducerFactory(config as Map<String, Any>)
	}

	private fun <T : Any> getValueSerializer(valueType: Class<T>) =
		when (valueType) {
			String::class.java -> StringSerializer::class.java
			else -> JsonSerializer::class.java
		}

	fun <T : Any> createKafkaTemplate(valueType: Class<T>): KafkaTemplate<String, T> = KafkaTemplate(createProducerFactory(valueType))
}
