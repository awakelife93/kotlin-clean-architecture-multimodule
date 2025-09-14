package com.example.demo.kotest.user

import com.example.demo.config.TestBatchConfig
import com.example.demo.user.config.UserDeleteConfig
import com.example.demo.user.constant.UserRole
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
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
@Tags("kotest-integration-test")
@SpringBootTest(classes = [UserDeleteConfig::class, TestBatchConfig::class])
@SpringBatchTest
class UserDeleteConfigIntegrationTests(
	@Autowired
	private val jdbcTemplate: JdbcTemplate,
	@Autowired
	private val jobLauncherTestUtils: JobLauncherTestUtils,
	@Autowired
	private val jobRepositoryTestUtils: JobRepositoryTestUtils
) : FunSpec({

		val defaultUserName = "Test User"
		val defaultUserRole = UserRole.USER
		val defaultPassword = "\$2a\$10\$T44NRNpbxkQ9qHbCtqQZ7O3gYfipzC0cHvOIJ/aV4PTlvJjtDl7x2"

		fun cleanupDatabase() {
			jdbcTemplate.update("DELETE FROM \"user\"")
			jobRepositoryTestUtils.removeJobExecutions()
		}

		fun insertUser(
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
				defaultUserName,
				defaultPassword,
				defaultUserRole.name
			)
		}

		beforeEach {
			cleanupDatabase()
		}

		afterEach {
			cleanupDatabase()
		}

		test("UserDelete Batch Job should process users deleted more than 1 year ago") {
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

			jobExecution.status shouldBe BatchStatus.COMPLETED

			val recentlyDeletedUsers =
				jdbcTemplate.queryForObject(
					"SELECT COUNT(*) FROM \"user\" WHERE deleted_dt = ?",
					Int::class.java,
					now.minusMonths(6)
				)
			recentlyDeletedUsers shouldBe 1

			val activeUsers =
				jdbcTemplate.queryForObject(
					"SELECT COUNT(*) FROM \"user\" WHERE deleted_dt IS NULL",
					Int::class.java
				)
			activeUsers shouldBe 1

			val oldDeletedUsers =
				jdbcTemplate.queryForObject(
					"SELECT COUNT(*) FROM \"user\" WHERE deleted_dt IS NOT NULL AND deleted_dt <= ?",
					Int::class.java,
					now.minusYears(1)
				)
			oldDeletedUsers shouldBe 3
		}

		test("UserDelete Batch Job should handle empty result set") {
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

			jobExecution.status shouldBe BatchStatus.COMPLETED

			val totalUsers =
				jdbcTemplate.queryForObject(
					"SELECT COUNT(*) FROM \"user\"",
					Int::class.java
				)
			totalUsers shouldBe 2
		}

		test("UserDelete Batch Job should process users in chunks") {
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

			jobExecution.status shouldBe BatchStatus.COMPLETED

			val processedUsers =
				jdbcTemplate.queryForObject(
					"SELECT COUNT(*) FROM \"user\" WHERE deleted_dt <= ?",
					Int::class.java,
					now.minusYears(1)
				)
			processedUsers shouldBe 15
		}

		test("UserDelete Batch Job should include users deleted exactly 1 year ago") {
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

			jobExecution.status shouldBe BatchStatus.COMPLETED

			val processedUsers =
				jdbcTemplate.queryForObject(
					"SELECT COUNT(*) FROM \"user\" WHERE deleted_dt = ?",
					Int::class.java,
					exactlyOneYearAgo
				)
			processedUsers shouldBe 1
		}

		test("UserDelete Batch Job should exclude users deleted less than 1 year ago") {
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

			jobExecution.status shouldBe BatchStatus.COMPLETED

			val remainingUsers =
				jdbcTemplate.queryForObject(
					"SELECT COUNT(*) FROM \"user\" WHERE deleted_dt = ?",
					Int::class.java,
					elevenMonthsAgo
				)
			remainingUsers shouldBe 1
		}

		test("UserDelete Batch Job should handle large dataset efficiently") {
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

			jobExecution.status shouldBe BatchStatus.COMPLETED

			(executionTime < 30000) shouldBe true

			val processedUsers =
				jdbcTemplate.queryForObject(
					"SELECT COUNT(*) FROM \"user\" WHERE deleted_dt <= ?",
					Int::class.java,
					now.minusYears(1)
				)
			processedUsers shouldBe userCount
		}
	})
