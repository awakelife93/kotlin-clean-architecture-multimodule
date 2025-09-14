package com.example.demo.listener

import io.github.oshai.kotlinlogging.KotlinLogging
import org.slf4j.MDC
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.JobExecutionListener
import org.springframework.batch.core.StepExecution
import org.springframework.batch.core.StepExecutionListener
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.LocalDateTime

private val logger = KotlinLogging.logger {}

@Component
class JobLoggingListener : JobExecutionListener {
	override fun beforeJob(jobExecution: JobExecution) {
		val jobName = jobExecution.jobInstance.jobName
		val jobExecutionId = jobExecution.id.toString()

		MDC.put("jobName", jobName)
		MDC.put("jobExecutionId", jobExecutionId)
		MDC.put("jobStartTime", LocalDateTime.now().toString())

		logger.info {
			"Job started - Name: $jobName, ExecutionId: $jobExecutionId, Parameters: ${jobExecution.jobParameters}"
		}
	}

	override fun afterJob(jobExecution: JobExecution) {
		val jobName = jobExecution.jobInstance.jobName
		val jobExecutionId = jobExecution.id.toString()
		val status = jobExecution.status.toString()
		val duration = calculateDuration(jobExecution)

		MDC.put("jobStatus", status)
		MDC.put("jobDuration", "${duration}ms")
		MDC.put("jobEndTime", LocalDateTime.now().toString())

		when {
			jobExecution.status.isUnsuccessful -> {
				val failureExceptions = jobExecution.allFailureExceptions
				MDC.put("jobError", failureExceptions.firstOrNull()?.message ?: "Unknown error")

				logger.error {
					"Job failed - Name: $jobName, ExecutionId: $jobExecutionId, Status: $status, Duration: ${duration}ms"
				}
				failureExceptions.forEach { exception ->
					logger.error(exception) { "Job failure exception" }
				}
			}

			else -> {
				logger.info {
					"Job completed - Name: $jobName, ExecutionId: $jobExecutionId, Status: $status, Duration: ${duration}ms"
				}
			}
		}

		logStepExecutionSummary(jobExecution)

		MDC.clear()
	}

	private fun calculateDuration(jobExecution: JobExecution): Long {
		val startTime = jobExecution.startTime
		val endTime = jobExecution.endTime ?: LocalDateTime.now()

		return Duration.between(startTime, endTime).toMillis()
	}

	private fun logStepExecutionSummary(jobExecution: JobExecution) {
		val stepExecutions = jobExecution.stepExecutions

		stepExecutions.forEach { stepExecution ->
			logger.info {
				"Step summary - Name: ${stepExecution.stepName}, " +
					"Status: ${stepExecution.status}, " +
					"Read: ${stepExecution.readCount}, " +
					"Write: ${stepExecution.writeCount}, " +
					"Skip: ${stepExecution.skipCount}, " +
					"Commit: ${stepExecution.commitCount}, " +
					"Rollback: ${stepExecution.rollbackCount}"
			}
		}
	}
}

@Component
class StepLoggingListener : StepExecutionListener {
	override fun beforeStep(stepExecution: StepExecution) {
		val stepName = stepExecution.stepName
		val jobName = stepExecution.jobExecution.jobInstance.jobName
		val jobExecutionId = stepExecution.jobExecution.id.toString()

		MDC.put("stepName", stepName)
		MDC.put("jobName", jobName)
		MDC.put("jobExecutionId", jobExecutionId)
		MDC.put("stepStartTime", LocalDateTime.now().toString())

		logger.info {
			"Step started - Name: $stepName, Job: $jobName"
		}
	}

	override fun afterStep(stepExecution: StepExecution): org.springframework.batch.core.ExitStatus? {
		val stepName = stepExecution.stepName
		val status = stepExecution.status.toString()
		val duration = calculateDuration(stepExecution)

		MDC.put("stepStatus", status)
		MDC.put("stepDuration", "${duration}ms")
		MDC.put("recordsProcessed", stepExecution.readCount.toString())
		MDC.put("recordsWritten", stepExecution.writeCount.toString())
		MDC.put("recordsSkipped", stepExecution.skipCount.toString())

		logPerformanceMetrics(stepExecution, duration)

		when {
			stepExecution.status.isUnsuccessful -> {
				val failureExceptions = stepExecution.failureExceptions
				MDC.put("stepError", failureExceptions.firstOrNull()?.message ?: "Unknown error")

				logger.error {
					"Step failed - Name: $stepName, Status: $status, Duration: ${duration}ms"
				}
			}

			else -> {
				logger.info {
					"Step completed - Name: $stepName, Status: $status, Duration: ${duration}ms, " +
						"Read: ${stepExecution.readCount}, Write: ${stepExecution.writeCount}"
				}
			}
		}

		return stepExecution.exitStatus
	}

	private fun calculateDuration(stepExecution: StepExecution): Long {
		val startTime = stepExecution.startTime
		val endTime = stepExecution.endTime ?: LocalDateTime.now()

		return Duration.between(startTime, endTime).toMillis()
	}

	private fun logPerformanceMetrics(
		stepExecution: StepExecution,
		duration: Long
	) {
		val readCount = stepExecution.readCount
		val writeCount = stepExecution.writeCount

		if (readCount > 0) {
			val throughput = (readCount * 1000.0 / duration).toLong()
			MDC.put("throughput", "$throughput records/sec")

			logger.info {
				"Step performance - Throughput: $throughput records/sec, " +
					"Total duration: ${duration}ms"
			}
		}
	}
}

@Component
class ChunkLoggingListener : org.springframework.batch.core.ChunkListener {
	private var chunkStartTime: Long = 0

	override fun beforeChunk(context: org.springframework.batch.core.scope.context.ChunkContext) {
		chunkStartTime = System.currentTimeMillis()

		val stepName = context.stepContext.stepName
		val jobName = context.stepContext.jobName

		MDC.put("stepName", stepName)
		MDC.put("jobName", jobName)
		MDC.put(
			"chunkSize",
			context.stepContext.stepExecution.commitCount
				.toString()
		)
	}

	override fun afterChunk(context: org.springframework.batch.core.scope.context.ChunkContext) {
		val processTime = System.currentTimeMillis() - chunkStartTime

		MDC.put("chunkProcessTime", "${processTime}ms")

		if (processTime > SLOW_CHUNK_THRESHOLD_MS) {
			logger.warn { "Slow chunk detected: ${processTime}ms" }
		}
	}

	override fun afterChunkError(context: org.springframework.batch.core.scope.context.ChunkContext) {
		val error =
			context.stepContext.stepExecution.failureExceptions
				.firstOrNull()

		MDC.put("chunkError", error?.message ?: "Unknown error")
		logger.error(error) { "Chunk processing failed" }
	}

	companion object {
		private const val SLOW_CHUNK_THRESHOLD_MS = 5000L
	}
}
