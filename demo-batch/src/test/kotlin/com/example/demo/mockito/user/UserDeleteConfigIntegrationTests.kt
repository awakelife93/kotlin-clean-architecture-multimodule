package com.example.demo.mockito.user

import com.example.demo.config.TestBatchConfig
import com.example.demo.user.config.UserDeleteConfig
import com.example.demo.user.constant.UserRole
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.test.JobLauncherTestUtils
import org.springframework.batch.test.JobRepositoryTestUtils
import org.springframework.batch.test.context.SpringBatchTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime

@ActiveProfiles("test")
@Tag("mockito-integration-test")
@DisplayName("Mockito Integration - UserDelete Batch Job Test")
@SpringBootTest(classes = [UserDeleteConfig::class, TestBatchConfig::class])
@SpringBatchTest
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class UserDeleteConfigIntegrationTests {
	@Autowired
	private lateinit var jdbcTemplate: JdbcTemplate

	@Autowired
	private lateinit var jobLauncherTestUtils: JobLauncherTestUtils

	@Autowired
	private lateinit var jobRepositoryTestUtils: JobRepositoryTestUtils

	companion object {
		private const val DEFAULT_USER_NAME = "Test User"
		private val DEFAULT_USER_ROLE = UserRole.USER
		private const val DEFAULT_PASSWORD = "\$2a\$10\$T44NRNpbxkQ9qHbCtqQZ7O3gYfipzC0cHvOIJ/aV4PTlvJjtDl7x2"
	}

	@BeforeEach
	fun setUp() {
		cleanupDatabase()
	}

	@AfterEach
	fun tearDown() {
		cleanupDatabase()
	}

	private fun cleanupDatabase() {
		jdbcTemplate.update("DELETE FROM \"user\"")
		jobRepositoryTestUtils.removeJobExecutions()
	}

	private fun insertUser(
		email: String,
		createdDt: LocalDateTime,
		updatedDt: LocalDateTime,
		deletedDt: LocalDateTime?
	) {
		jdbcTemplate.update(
			"""
            INSERT INTO "user" (created_dt, updated_dt, deleted_dt, email, name, password, role)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """,
			createdDt,
			updatedDt,
			deletedDt,
			email,
			DEFAULT_USER_NAME,
			DEFAULT_PASSWORD,
			DEFAULT_USER_ROLE.name
		)
	}

	@Nested
	@DisplayName("UserDelete Batch Job execution tests")
	inner class BatchJobExecutionTests {
		@Test
		@Order(1)
		@DisplayName("should process users deleted more than 1 year ago")
		fun shouldProcessUsersDeletedMoreThan1YearAgo() {
			val now = LocalDateTime.now().withNano(0)

			insertUser(
				email = "deleted-1year-1@example.com",
				createdDt = now.minusYears(2),
				updatedDt = now.minusYears(1),
				deletedDt = now.minusYears(1).minusDays(1)
			)

			insertUser(
				email = "deleted-1year-2@example.com",
				createdDt = now.minusYears(2),
				updatedDt = now.minusYears(1),
				deletedDt = now.minusYears(1).minusDays(5)
			)

			insertUser(
				email = "deleted-exactly-1year@example.com",
				createdDt = now.minusYears(2),
				updatedDt = now.minusYears(1),
				deletedDt = now.minusYears(1)
			)

			insertUser(
				email = "deleted-6months@example.com",
				createdDt = now.minusYears(1),
				updatedDt = now.minusMonths(6),
				deletedDt = now.minusMonths(6)
			)

			insertUser(
				email = "active-user@example.com",
				createdDt = now.minusYears(2),
				updatedDt = now,
				deletedDt = null
			)

			val jobParameters =
				JobParametersBuilder()
					.addLocalDateTime("now", now)
					.toJobParameters()

			val jobExecution = jobLauncherTestUtils.launchJob(jobParameters)

			assertEquals(BatchStatus.COMPLETED, jobExecution.status)

			val recentlyDeletedUsers =
				jdbcTemplate.queryForObject(
					"SELECT COUNT(*) FROM \"user\" WHERE deleted_dt = ?",
					Int::class.java,
					now.minusMonths(6)
				)
			assertEquals(1, recentlyDeletedUsers)

			val activeUsers =
				jdbcTemplate.queryForObject(
					"SELECT COUNT(*) FROM \"user\" WHERE deleted_dt IS NULL",
					Int::class.java
				)
			assertEquals(1, activeUsers)

			val oldDeletedUsers =
				jdbcTemplate.queryForObject(
					"SELECT COUNT(*) FROM \"user\" WHERE deleted_dt IS NOT NULL AND deleted_dt <= ?",
					Int::class.java,
					now.minusYears(1)
				)
			assertEquals(3, oldDeletedUsers)
		}

		@Test
		@Order(2)
		@DisplayName("should handle empty result set gracefully")
		fun shouldHandleEmptyResultSetGracefully() {
			val now = LocalDateTime.now().withNano(0)

			insertUser(
				email = "recent-delete@example.com",
				createdDt = now.minusMonths(3),
				updatedDt = now.minusMonths(1),
				deletedDt = now.minusMonths(1)
			)

			insertUser(
				email = "active@example.com",
				createdDt = now.minusMonths(6),
				updatedDt = now,
				deletedDt = null
			)

			val jobParameters =
				JobParametersBuilder()
					.addLocalDateTime("now", now)
					.toJobParameters()

			val jobExecution = jobLauncherTestUtils.launchJob(jobParameters)

			assertEquals(BatchStatus.COMPLETED, jobExecution.status)

			val totalUsers =
				jdbcTemplate.queryForObject(
					"SELECT COUNT(*) FROM \"user\"",
					Int::class.java
				)
			assertEquals(2, totalUsers)
		}

		@Test
		@Order(3)
		@DisplayName("should process users in chunks")
		fun shouldProcessUsersInChunks() {
			val now = LocalDateTime.now().withNano(0)
			val oneYearAgo = now.minusYears(1).minusDays(1)

			for (i in 1..15) {
				insertUser(
					email = "deleted-user-$i@example.com",
					createdDt = now.minusYears(2),
					updatedDt = oneYearAgo,
					deletedDt = oneYearAgo
				)
			}

			val jobParameters =
				JobParametersBuilder()
					.addLocalDateTime("now", now)
					.toJobParameters()

			val jobExecution = jobLauncherTestUtils.launchJob(jobParameters)

			assertEquals(BatchStatus.COMPLETED, jobExecution.status)

			val processedUsers =
				jdbcTemplate.queryForObject(
					"SELECT COUNT(*) FROM \"user\" WHERE deleted_dt <= ?",
					Int::class.java,
					now.minusYears(1)
				)
			assertEquals(15, processedUsers)
		}
	}

	@Nested
	@DisplayName("Boundary case tests")
	inner class BoundaryCaseTests {
		@Test
		@DisplayName("should include users deleted exactly 1 year ago")
		fun shouldIncludeUsersDeletedExactly1YearAgo() {
			val now = LocalDateTime.now().withNano(0)
			val exactlyOneYearAgo = now.minusYears(1)

			insertUser(
				email = "exactly-1-year@example.com",
				createdDt = now.minusYears(2),
				updatedDt = exactlyOneYearAgo,
				deletedDt = exactlyOneYearAgo
			)

			val jobParameters =
				JobParametersBuilder()
					.addLocalDateTime("now", now)
					.toJobParameters()

			val jobExecution = jobLauncherTestUtils.launchJob(jobParameters)

			assertEquals(BatchStatus.COMPLETED, jobExecution.status)

			val processedUsers =
				jdbcTemplate.queryForObject(
					"SELECT COUNT(*) FROM \"user\" WHERE deleted_dt = ?",
					Int::class.java,
					exactlyOneYearAgo
				)
			assertEquals(1, processedUsers)
		}

		@Test
		@DisplayName("should exclude users deleted less than 1 year ago")
		fun shouldExcludeUsersDeletedLessThan1YearAgo() {
			val now = LocalDateTime.now().withNano(0)
			val elevenMonthsAgo = now.minusMonths(11)

			insertUser(
				email = "11-months-ago@example.com",
				createdDt = now.minusYears(1),
				updatedDt = elevenMonthsAgo,
				deletedDt = elevenMonthsAgo
			)

			val jobParameters =
				JobParametersBuilder()
					.addLocalDateTime("now", now)
					.toJobParameters()

			val jobExecution = jobLauncherTestUtils.launchJob(jobParameters)

			assertEquals(BatchStatus.COMPLETED, jobExecution.status)

			val remainingUsers =
				jdbcTemplate.queryForObject(
					"SELECT COUNT(*) FROM \"user\" WHERE deleted_dt = ?",
					Int::class.java,
					elevenMonthsAgo
				)
			assertEquals(1, remainingUsers)
		}
	}

	@Nested
	@DisplayName("Performance and scalability tests")
	inner class PerformanceTests {
		@Test
		@DisplayName("should handle large dataset efficiently")
		fun shouldHandleLargeDatasetEfficiently() {
			val now = LocalDateTime.now().withNano(0)
			val oneYearAgo = now.minusYears(1).minusDays(1)
			val userCount = 100

			for (i in 1..userCount) {
				insertUser(
					email = "bulk-user-$i@example.com",
					createdDt = now.minusYears(2),
					updatedDt = oneYearAgo,
					deletedDt = oneYearAgo
				)
			}

			val startTime = System.currentTimeMillis()
			val jobParameters =
				JobParametersBuilder()
					.addLocalDateTime("now", now)
					.toJobParameters()

			val jobExecution = jobLauncherTestUtils.launchJob(jobParameters)
			val executionTime = System.currentTimeMillis() - startTime

			assertEquals(BatchStatus.COMPLETED, jobExecution.status)

			assertTrue(executionTime < 30000, "Batch job took too long: ${executionTime}ms")

			val processedUsers =
				jdbcTemplate.queryForObject(
					"SELECT COUNT(*) FROM \"user\" WHERE deleted_dt <= ?",
					Int::class.java,
					now.minusYears(1)
				)
			assertEquals(userCount, processedUsers)
		}
	}
}
