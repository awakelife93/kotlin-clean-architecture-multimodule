package com.example.demo.config

import com.example.demo.exception.handler.AsyncExceptionHandler
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.AsyncConfigurer
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.Executor

private val logger = KotlinLogging.logger {}

@Configuration
@EnableAsync
class AsyncConfig : AsyncConfigurer {
	override fun getAsyncUncaughtExceptionHandler(): AsyncUncaughtExceptionHandler = AsyncExceptionHandler()

	override fun getAsyncExecutor(): Executor =
		ThreadPoolTaskExecutor()
			.apply {
				corePoolSize = 5
				maxPoolSize = 10
				queueCapacity = 100
				setThreadNamePrefix("async-exec-")
				initialize()
			}

	@Bean
	fun webhookExecutor(): Executor =
		ThreadPoolTaskExecutor()
			.apply {
				corePoolSize = 10
				maxPoolSize = 20
				queueCapacity = 100
				setThreadNamePrefix("async-webhook-exec-")
				initialize()
			}.also {
				logger.info { "WebhookExecutor initialized with core=10, max=20 queueCapacity=100" }
			}
}
