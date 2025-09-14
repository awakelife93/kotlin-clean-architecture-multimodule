package com.example.demo.mockito.post.usecase

import com.example.demo.post.port.PostPort
import com.example.demo.post.port.input.HardDeletePostsByUserIdInput
import com.example.demo.post.usecase.HardDeletePostsByUserIdUseCase
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@Tag("mockito-unit-test")
@DisplayName("Mockito Unit - Hard Delete Posts By User ID UseCase Test")
@ExtendWith(MockitoExtension::class)
class HardDeletePostsByUserIdUseCaseTests {
	@Mock
	private lateinit var postPort: PostPort

	@InjectMocks
	private lateinit var hardDeletePostsByUserIdUseCase: HardDeletePostsByUserIdUseCase

	@Nested
	@DisplayName("Hard delete all posts by user ID")
	inner class HardDeletePostsByUserIdTest {
		@Test
		@DisplayName("Hard delete posts for user")
		fun should_call_hard_delete_service_method() {
			val userId = 100L
			val input = HardDeletePostsByUserIdInput(userId = userId)
			doNothing().whenever(postPort).hardDeleteByUserId(userId)

			assertDoesNotThrow {
				hardDeletePostsByUserIdUseCase.execute(input)
			}

			verify(postPort, times(1)).hardDeleteByUserId(userId)
		}

		@Test
		@DisplayName("Hard delete for GDPR compliance")
		fun should_permanently_delete_posts() {
			val userId = 200L
			val input = HardDeletePostsByUserIdInput(userId = userId)
			doNothing().whenever(postPort).hardDeleteByUserId(userId)

			assertDoesNotThrow {
				hardDeletePostsByUserIdUseCase.execute(input)
			}

			verify(postPort, times(1)).hardDeleteByUserId(userId)
		}
	}
}
