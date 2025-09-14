package com.example.demo.user.config

import com.example.demo.user.model.UserDeleteItem
import com.example.demo.user.processor.UserDeleteItemProcessor
import com.example.demo.user.reader.UserDeleteItemReader
import com.example.demo.user.writer.UserDeleteItemWriter
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobScope
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.configuration.support.DefaultBatchConfiguration
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.database.JdbcPagingItemReader
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager
import java.time.LocalDateTime

@Configuration
class UserDeleteConfig(
	private val userDeleteItemReader: UserDeleteItemReader,
	private val userDeleteItemProcessor: UserDeleteItemProcessor,
	private val userDeleteItemWriter: UserDeleteItemWriter
) : DefaultBatchConfiguration() {
	private val chunkSize = 10

	@Bean
	fun userDelete(
		jobRepository: JobRepository,
		transactionManager: PlatformTransactionManager
	): Job =
		JobBuilder("userDeleteJob", jobRepository)
			.start(userDeleteStep(jobRepository, transactionManager))
			.build()

	@Bean
	@JobScope
	fun userDeleteStep(
		jobRepository: JobRepository,
		transactionManager: PlatformTransactionManager
	): Step =
		StepBuilder("userDeleteStep", jobRepository)
			.chunk<UserDeleteItem, UserDeleteItem>(chunkSize, transactionManager)
			.reader(userDeleteReader(null))
			.processor(userDeleteProcessor())
			.writer(userDeleteWriter())
			.build()

	@Bean
	@StepScope
	fun userDeleteReader(
		@Value("#{jobParameters[now]}") now: LocalDateTime?
	): JdbcPagingItemReader<UserDeleteItem> {
		val nowDateTime = checkNotNull(now) { "now parameter is required" }

		return userDeleteItemReader.reader(chunkSize, nowDateTime)
	}

	@Bean
	@StepScope
	fun userDeleteProcessor(): UserDeleteItemProcessor = userDeleteItemProcessor

	@Bean
	@StepScope
	fun userDeleteWriter(): UserDeleteItemWriter = userDeleteItemWriter
}
