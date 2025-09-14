package com.example.demo.mockito.auth.usecase

import com.example.demo.auth.port.input.SignOutInput
import com.example.demo.auth.service.AuthService
import com.example.demo.auth.usecase.SignOutUseCase
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@Tag("mockito-unit-test")
@DisplayName("Mockito Unit - Sign Out UseCase Test")
@ExtendWith(MockitoExtension::class)
class SignOutUseCaseTests {
	@Mock
	private lateinit var authService: AuthService

	@InjectMocks
	private lateinit var signOutUseCase: SignOutUseCase

	@Nested
	@DisplayName("Sign out")
	inner class SignOutTest {
		@Test
		@DisplayName("User signs out")
		fun should_successfully_sign_out() {
			val input = SignOutInput(userId = 1L)

			doNothing().whenever(authService).signOut(input.userId)

			assertDoesNotThrow {
				signOutUseCase.execute(input)
			}

			verify(authService, times(1)).signOut(input.userId)
		}

		@Test
		@DisplayName("Multiple users sign out simultaneously")
		fun should_sign_out_each_user_independently() {
			val input1 = SignOutInput(userId = 1L)
			val input2 = SignOutInput(userId = 2L)

			doNothing().whenever(authService).signOut(any<Long>())

			assertDoesNotThrow {
				signOutUseCase.execute(input1)
				signOutUseCase.execute(input2)
			}

			verify(authService, times(1)).signOut(1L)
			verify(authService, times(1)).signOut(2L)
		}

		@Test
		@DisplayName("Service throws exception during sign out")
		fun should_propagate_exception() {
			val input = SignOutInput(userId = 999L)

			doThrow(RuntimeException("Sign out failed")).whenever(authService).signOut(input.userId)

			val exception =
				assertThrows<RuntimeException> {
					signOutUseCase.execute(input)
				}

			assertEquals("Sign out failed", exception.message)
			verify(authService, times(1)).signOut(input.userId)
		}

		@Test
		@DisplayName("Sign out with zero user ID")
		fun should_process_zero_user_id() {
			val input = SignOutInput(userId = 0L)

			doNothing().whenever(authService).signOut(input.userId)

			assertDoesNotThrow {
				signOutUseCase.execute(input)
			}

			verify(authService, times(1)).signOut(0L)
		}

		@Test
		@DisplayName("Sign out with negative user ID")
		fun should_process_negative_user_id() {
			val input = SignOutInput(userId = -1L)

			doNothing().whenever(authService).signOut(input.userId)

			assertDoesNotThrow {
				signOutUseCase.execute(input)
			}

			verify(authService, times(1)).signOut(-1L)
		}
	}
}
