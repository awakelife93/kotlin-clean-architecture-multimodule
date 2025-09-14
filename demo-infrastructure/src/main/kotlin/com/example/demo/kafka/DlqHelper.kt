package com.example.demo.kafka

import org.apache.kafka.common.TopicPartition
import org.springframework.kafka.core.KafkaTemplate

object DlqHelper {
	private fun resolveDlqPartition(
		kafkaTemplate: KafkaTemplate<*, *>,
		dlqTopic: String,
		originalPartition: Int,
		fallbackPartition: Int
	): Int =
		runCatching {
			kafkaTemplate.producerFactory.createProducer().use { producer ->
				producer.partitionsFor(dlqTopic)?.let { partitions ->
					if (originalPartition < partitions.size) originalPartition else fallbackPartition
				} ?: fallbackPartition
			}
		}.getOrDefault(fallbackPartition)

	fun resolveDlqTopicPartition(
		kafkaTemplate: KafkaTemplate<*, *>,
		originalTopic: String,
		originalPartition: Int,
		fallbackPartition: Int = 1
	): TopicPartition {
		val dlqTopic = "$originalTopic.DLQ"
		val partition = resolveDlqPartition(kafkaTemplate, dlqTopic, originalPartition, fallbackPartition)

		return TopicPartition(dlqTopic, partition)
	}
}
