package com.example.demo.user

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParameters
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.configuration.JobRegistry
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime

private val logger = KotlinLogging.logger {}

@Component
class UserScheduler(
	private val jobLauncher: JobLauncher,
	private val jobRegistry: JobRegistry
) {
	@Scheduled(cron = "0 0 01 * * ?")
	fun run() {
		runCatching {
			val job: Job = jobRegistry.getJob("userDeleteJob")
			val jobParameters: JobParameters =
				JobParametersBuilder()
					.addLocalDateTime("now", LocalDateTime.now())
					.toJobParameters()

			jobLauncher.run(job, jobParameters)
		}.onSuccess { logger.info { "Success User Scheduler Job ${it.jobId} ${it.startTime} ${it.endTime}" } }
			.onFailure { logger.error { "Error User Scheduler Job ${it.message}" } }
	}
}
