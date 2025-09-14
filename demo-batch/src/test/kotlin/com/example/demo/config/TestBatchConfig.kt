package com.example.demo.config

import com.example.demo.persistence.config.QueryDslConfig
import com.example.demo.persistence.post.entity.PostEntity
import com.example.demo.persistence.post.repository.PostJpaRepository
import com.example.demo.persistence.user.entity.UserEntity
import com.example.demo.persistence.user.repository.UserJpaRepository
import com.example.demo.user.model.UserDeleteItem
import com.example.demo.user.processor.UserDeleteItemProcessor
import com.example.demo.user.reader.UserDeleteItemReader
import com.example.demo.user.writer.UserDeleteItemWriter
import org.mockito.kotlin.mock
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Import
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.kafka.core.KafkaTemplate

@TestConfiguration
@Import(QueryDslConfig::class)
@EnableAutoConfiguration
@EnableJpaRepositories(basePackageClasses = [UserJpaRepository::class, PostJpaRepository::class])
@EntityScan(basePackageClasses = [UserEntity::class, PostEntity::class])
@ComponentScan(basePackageClasses = [UserDeleteItemReader::class, UserDeleteItemProcessor::class, UserDeleteItemWriter::class])
class TestBatchConfig {
	@Bean
	fun userDeleteKafkaTemplate(): KafkaTemplate<String, UserDeleteItem> = mock()
}
