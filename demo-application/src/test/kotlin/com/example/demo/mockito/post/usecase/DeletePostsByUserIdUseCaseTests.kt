package com.example.demo.mockito.post.usecase

import com.example.demo.post.port.input.DeletePostsByUserIdInput
import com.example.demo.post.service.PostService
import com.example.demo.post.usecase.DeletePostsByUserIdUseCase
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
@DisplayName("Mockito Unit - Delete Posts By User ID UseCase Test")
@ExtendWith(MockitoExtension::class)
class DeletePostsByUserIdUseCaseTests {
	@Mock
	private lateinit var postService: PostService

	@InjectMocks
	private lateinit var deletePostsByUserIdUseCase: DeletePostsByUserIdUseCase

	@Nested
	@DisplayName("Delete all posts by user ID")
	inner class DeletePostsByUserIdTest {
		@Test
		@DisplayName("Delete posts for user")
		fun should_call_service_method() {
			val userId = 100L
			val input = DeletePostsByUserIdInput(userId = userId)
			doNothing().whenever(postService).deletePostsByUserId(userId)

			assertDoesNotThrow {
				deletePostsByUserIdUseCase.execute(input)
			}

			verify(postService, times(1)).deletePostsByUserId(userId)
		}

		@Test
		@DisplayName("Delete posts for user with no posts")
		fun should_call_service_even_for_user_without_posts() {
			val userId = 200L
			val input = DeletePostsByUserIdInput(userId = userId)
			doNothing().whenever(postService).deletePostsByUserId(userId)

			assertDoesNotThrow {
				deletePostsByUserIdUseCase.execute(input)
			}

			verify(postService, times(1)).deletePostsByUserId(userId)
		}
	}
}
