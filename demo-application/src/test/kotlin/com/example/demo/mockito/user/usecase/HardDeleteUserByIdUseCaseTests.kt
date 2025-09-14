package com.example.demo.mockito.user.usecase

import com.example.demo.user.port.UserPort
import com.example.demo.user.port.input.HardDeleteUserByIdInput
import com.example.demo.user.usecase.HardDeleteUserByIdUseCase
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@Tag("mockito-unit-test")
@DisplayName("Mockito Unit - HardDeleteUserByIdUseCase Test")
@ExtendWith(MockitoExtension::class)
class HardDeleteUserByIdUseCaseTests {
	@Mock
	private lateinit var userPort: UserPort

	@InjectMocks
	private lateinit var hardDeleteUserByIdUseCase: HardDeleteUserByIdUseCase

	@Nested
	@DisplayName("execute method tests")
	inner class ExecuteTests {
		@Test
		@DisplayName("should successfully hard delete user by ID")
		fun shouldSuccessfullyHardDeleteUserById() {
			val userId = 100L
			val input = HardDeleteUserByIdInput(userId = userId)

			doNothing().whenever(userPort).hardDeleteById(userId)

			hardDeleteUserByIdUseCase.execute(input)

			verify(userPort, times(1)).hardDeleteById(userId)
			verifyNoMoreInteractions(userPort)
		}

		@Test
		@DisplayName("should propagate exception from repository")
		fun shouldPropagateExceptionFromRepository() {
			val userId = 200L
			val input = HardDeleteUserByIdInput(userId = userId)
			val exception = RuntimeException("Database connection failed")

			doThrow(exception).whenever(userPort).hardDeleteById(userId)

			val thrownException =
				assertThrows<RuntimeException> {
					hardDeleteUserByIdUseCase.execute(input)
				}

			assertEquals("Database connection failed", thrownException.message)
			verify(userPort, times(1)).hardDeleteById(userId)
		}

		@ParameterizedTest
		@ValueSource(longs = [1L, 10L, 100L, 1000L, 10000L])
		@DisplayName("should handle various user IDs")
		fun shouldHandleVariousUserIds(userId: Long) {
			val input = HardDeleteUserByIdInput(userId = userId)
			doNothing().whenever(userPort).hardDeleteById(userId)

			hardDeleteUserByIdUseCase.execute(input)

			verify(userPort, times(1)).hardDeleteById(userId)
		}
	}

	@Nested
	@DisplayName("edge case tests")
	inner class EdgeCaseTests {
		@Test
		@DisplayName("should handle user ID 0")
		fun shouldHandleUserIdZero() {
			val input = HardDeleteUserByIdInput(userId = 0L)
			doNothing().whenever(userPort).hardDeleteById(0L)

			hardDeleteUserByIdUseCase.execute(input)

			verify(userPort, times(1)).hardDeleteById(0L)
		}

		@Test
		@DisplayName("should handle negative user ID")
		fun shouldHandleNegativeUserId() {
			val input = HardDeleteUserByIdInput(userId = -1L)
			doNothing().whenever(userPort).hardDeleteById(-1L)

			hardDeleteUserByIdUseCase.execute(input)

			verify(userPort, times(1)).hardDeleteById(-1L)
		}

		@Test
		@DisplayName("should handle maximum Long value")
		fun shouldHandleMaximumLongValue() {
			val maxId = Long.MAX_VALUE
			val input = HardDeleteUserByIdInput(userId = maxId)
			doNothing().whenever(userPort).hardDeleteById(maxId)

			hardDeleteUserByIdUseCase.execute(input)

			verify(userPort, times(1)).hardDeleteById(maxId)
		}
	}

	@Nested
	@DisplayName("batch deletion tests")
	inner class BatchDeletionTests {
		@Test
		@DisplayName("should handle multiple sequential deletions")
		fun shouldHandleMultipleSequentialDeletions() {
			val userIds = listOf(1L, 2L, 3L, 4L, 5L)
			userIds.forEach { userId ->
				doNothing().whenever(userPort).hardDeleteById(userId)
			}

			userIds.forEach { userId ->
				hardDeleteUserByIdUseCase.execute(
					HardDeleteUserByIdInput(userId = userId)
				)
			}

			userIds.forEach { userId ->
				verify(userPort, times(1)).hardDeleteById(userId)
			}
			verifyNoMoreInteractions(userPort)
		}

		@Test
		@DisplayName("should handle partial failure in batch")
		fun shouldHandlePartialFailureInBatch() {
			doNothing().whenever(userPort).hardDeleteById(1L)
			doThrow(RuntimeException("Failed")).whenever(userPort).hardDeleteById(2L)
			doNothing().whenever(userPort).hardDeleteById(3L)

			assertDoesNotThrow {
				hardDeleteUserByIdUseCase.execute(
					HardDeleteUserByIdInput(userId = 1L)
				)
			}

			assertThrows<RuntimeException> {
				hardDeleteUserByIdUseCase.execute(
					HardDeleteUserByIdInput(userId = 2L)
				)
			}

			assertDoesNotThrow {
				hardDeleteUserByIdUseCase.execute(
					HardDeleteUserByIdInput(userId = 3L)
				)
			}

			verify(userPort, times(1)).hardDeleteById(1L)
			verify(userPort, times(1)).hardDeleteById(2L)
			verify(userPort, times(1)).hardDeleteById(3L)
		}
	}
}
