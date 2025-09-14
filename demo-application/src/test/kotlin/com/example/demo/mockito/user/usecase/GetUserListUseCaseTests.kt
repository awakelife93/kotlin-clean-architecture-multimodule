package com.example.demo.mockito.user.usecase

import com.example.demo.user.constant.UserRole
import com.example.demo.user.model.User
import com.example.demo.user.port.input.GetUserListInput
import com.example.demo.user.service.UserService
import com.example.demo.user.usecase.GetUserListUseCase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
@ActiveProfiles("test")
@Tag("mockito-unit-test")
@DisplayName("GetUserListUseCase Tests")
class GetUserListUseCaseTests {
	@Mock
	private lateinit var userService: UserService

	@InjectMocks
	private lateinit var getUserListUseCase: GetUserListUseCase

	private lateinit var testUsers: List<User>

	@BeforeEach
	fun setUp() {
		testUsers =
			(1..3).map { id ->
				User(
					id = id.toLong(),
					email = "user$id@example.com",
					password = "encoded",
					name = "User $id",
					role = if (id % 2 == 0) UserRole.ADMIN else UserRole.USER,
					createdDt = LocalDateTime.now(),
					updatedDt = LocalDateTime.now()
				)
			}
	}

	@Nested
	@DisplayName("Get user list")
	inner class GetUserListTests {
		@Test
		@DisplayName("Should return user list successfully")
		fun `should return user list successfully`() {
			val pageable = PageRequest.of(0, 10)
			val input = GetUserListInput(pageable = pageable)
			val userPage = PageImpl(testUsers, pageable, testUsers.size.toLong())

			whenever(userService.findAll(pageable)).thenReturn(userPage)

			val result = getUserListUseCase.execute(input)

			assertNotNull(result)
			assertEquals(3, result.users.content.size)
			result.users.content.forEachIndexed { index, user ->
				assertEquals((index + 1).toLong(), user.id)
				assertEquals("user${index + 1}@example.com", user.email)
			}
		}

		@Test
		@DisplayName("Should return empty list")
		fun `should return empty list`() {
			val pageable = PageRequest.of(0, 10)
			val input = GetUserListInput(pageable = pageable)
			val emptyPage = PageImpl<User>(emptyList(), pageable, 0)

			whenever(userService.findAll(pageable)).thenReturn(emptyPage)

			val result = getUserListUseCase.execute(input)

			assertNotNull(result)
			assertEquals(0, result.users.content.size)
			assertTrue(result.users.content.isEmpty())
		}

		@Test
		@DisplayName("Get specific page")
		fun should_return_requested_page() {
			val pageable = PageRequest.of(1, 2)
			val input = GetUserListInput(pageable = pageable)
			val secondPageUsers = testUsers.subList(2, 3)
			val userPage = PageImpl(secondPageUsers, pageable, testUsers.size.toLong())

			whenever(userService.findAll(pageable)) doReturn userPage

			val result = getUserListUseCase.execute(input)

			assertNotNull(result)
			assertEquals(1, result.users.content.size)
			assertEquals(
				"user3@example.com",
				result.users.content
					.first()
					.email
			)
		}
	}
}
