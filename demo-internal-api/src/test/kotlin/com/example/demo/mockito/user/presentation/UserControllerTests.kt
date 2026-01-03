package com.example.demo.mockito.user.presentation

import com.example.demo.security.model.SecurityUserItem
import com.example.demo.user.constant.UserRole
import com.example.demo.user.model.User
import com.example.demo.user.port.input.CreateUserInput
import com.example.demo.user.port.input.DeleteUserInput
import com.example.demo.user.port.input.GetUserByIdInput
import com.example.demo.user.port.input.GetUserListInput
import com.example.demo.user.port.input.UpdateMeInput
import com.example.demo.user.port.input.UpdateUserInput
import com.example.demo.user.port.output.UserOutput
import com.example.demo.user.presentation.UserController
import com.example.demo.user.presentation.dto.request.CreateUserRequest
import com.example.demo.user.presentation.dto.request.UpdateUserRequest
import com.example.demo.user.usecase.CreateUserUseCase
import com.example.demo.user.usecase.DeleteUserUseCase
import com.example.demo.user.usecase.GetUserByIdUseCase
import com.example.demo.user.usecase.GetUserListUseCase
import com.example.demo.user.usecase.UpdateMeUseCase
import com.example.demo.user.usecase.UpdateUserUseCase
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime

@ActiveProfiles("test")
@Tag("mockito-unit-test")
@DisplayName("Mockito Unit - User Controller Test")
@ExtendWith(MockitoExtension::class)
class UserControllerTests {
	@InjectMocks
	private lateinit var userController: UserController

	@Mock
	private lateinit var getUserByIdUseCase: GetUserByIdUseCase

	@Mock
	private lateinit var getUserListUseCase: GetUserListUseCase

	@Mock
	private lateinit var createUserUseCase: CreateUserUseCase

	@Mock
	private lateinit var updateUserUseCase: UpdateUserUseCase

	@Mock
	private lateinit var updateMeUseCase: UpdateMeUseCase

	@Mock
	private lateinit var deleteUserUseCase: DeleteUserUseCase

	private fun createTestUser(
		id: Long = 1L,
		name: String = "Test User",
		email: String = "test@example.com",
		password: String = "encodedPassword",
		role: UserRole = UserRole.USER
	) = User(
		id = id,
		name = name,
		email = email,
		password = password,
		role = role,
		createdDt = LocalDateTime.now(),
		updatedDt = LocalDateTime.now()
	)

	private fun createSecurityUser(
		userId: Long = 1L,
		email: String = "test@example.com",
		name: String = "Test User",
		role: UserRole = UserRole.USER
	) = SecurityUserItem(
		userId = userId,
		email = email,
		name = name,
		role = role
	)

	@Test
	@DisplayName("Get me returns current user successfully")
	fun getMe_withSecurityUser_returnsCurrentUser() {
		val securityUser = createSecurityUser()
		val user = createTestUser(id = securityUser.userId)
		val output = UserOutput.BaseUserOutput.from(user)

		whenever(getUserByIdUseCase.execute(GetUserByIdInput(securityUser.userId))) doReturn output

		val response = userController.getMe(securityUser)

		assertNotNull(response)
		assertEquals(HttpStatus.OK, response.statusCode)

		val body =
			requireNotNull(response.body) {
				"Response body should not be null"
			}
		assertEquals(user.id, body.userId)
		assertEquals(user.name, body.name)
		assertEquals(user.email, body.email)
		assertEquals(user.role, body.role)

		verify(getUserByIdUseCase).execute(GetUserByIdInput(securityUser.userId))
	}

	@Test
	@DisplayName("Get user by ID returns user successfully")
	fun getUserById_withValidId_returnsUser() {
		val userId = 1L
		val user = createTestUser(id = userId)
		val output = UserOutput.BaseUserOutput.from(user)

		whenever(getUserByIdUseCase.execute(GetUserByIdInput(userId))) doReturn output

		val response = userController.getUserById(userId)

		assertNotNull(response)
		assertEquals(HttpStatus.OK, response.statusCode)

		val body =
			requireNotNull(response.body) {
				"Response body should not be null"
			}
		assertEquals(user.id, body.userId)
		assertEquals(user.name, body.name)
		assertEquals(user.email, body.email)
		assertEquals(user.role, body.role)

		verify(getUserByIdUseCase).execute(GetUserByIdInput(userId))
	}

	@Test
	@DisplayName("Get user list returns paginated users")
	fun getUserList_withPageable_returnsPaginatedUsers() {
		val user = createTestUser()
		val pageable = PageRequest.of(0, 10)
		val pageOutput =
			UserOutput.UserPageListOutput.from(
				PageImpl(listOf(user), pageable, 1)
			)

		whenever(getUserListUseCase.execute(GetUserListInput(pageable))) doReturn pageOutput

		val response = userController.getUserList(pageable)

		assertNotNull(response)
		assertEquals(HttpStatus.OK, response.statusCode)

		val body =
			requireNotNull(response.body) {
				"Response body should not be null"
			}
		assertThat(body.content).hasSize(1)

		val firstUser = body.content[0]
		assertEquals(user.id, firstUser.userId)
		assertEquals(user.name, firstUser.name)
		assertEquals(user.email, firstUser.email)
		assertEquals(user.role, firstUser.role)

		verify(getUserListUseCase).execute(GetUserListInput(pageable))
	}

	@Test
	@DisplayName("Create user returns created user")
	fun createUser_withValidRequest_returnsCreatedUser() {
		val request =
			CreateUserRequest(
				name = "New User",
				email = "newuser@example.com",
				password = "password123"
			)
		val createdUser =
			createTestUser(
				id = 2L,
				name = request.name,
				email = request.email,
				password = "encodedPassword"
			)
		val accessToken = "test-access-token"
		val output = UserOutput.AuthenticatedUserOutput.from(createdUser, accessToken)

		whenever(
			createUserUseCase.execute(
				CreateUserInput(
					name = request.name,
					email = request.email,
					password = request.password
				)
			)
		) doReturn output

		val response = userController.createUser(request)

		assertNotNull(response)
		assertEquals(HttpStatus.CREATED, response.statusCode)

		val body =
			requireNotNull(response.body) {
				"Response body should not be null"
			}
		assertEquals(createdUser.id, body.userId)
		assertEquals(request.name, body.name)
		assertEquals(request.email, body.email)
		assertEquals(accessToken, body.accessToken)

		verify(createUserUseCase).execute(
			CreateUserInput(
				name = request.name,
				email = request.email,
				password = request.password
			)
		)
	}

	@Test
	@DisplayName("Update user returns updated user")
	fun updateUser_withValidRequest_returnsUpdatedUser() {
		val userId = 1L
		val request =
			UpdateUserRequest(
				name = "Updated Name",
				role = "ADMIN"
			)
		val updatedUser =
			createTestUser(
				id = userId,
				name = request.name,
				role = UserRole.ADMIN
			)
		val output = UserOutput.BaseUserOutput.from(updatedUser)

		whenever(
			updateUserUseCase.execute(
				UpdateUserInput(
					userId = userId,
					name = request.name,
					role = request.role
				)
			)
		) doReturn output

		val response = userController.updateUser(request, userId)

		assertNotNull(response)
		assertEquals(HttpStatus.OK, response.statusCode)

		val body =
			requireNotNull(response.body) {
				"Response body should not be null"
			}
		assertEquals(userId, body.userId)
		assertEquals(request.name, body.name)
		assertEquals(UserRole.ADMIN, body.role)

		verify(updateUserUseCase).execute(
			UpdateUserInput(
				userId = userId,
				name = request.name,
				role = request.role
			)
		)
	}

	@Test
	@DisplayName("Update me returns updated current user")
	fun updateMe_withValidRequest_returnsUpdatedCurrentUser() {
		val securityUser = createSecurityUser()
		val request =
			UpdateUserRequest(
				name = "Updated My Name",
				role = "USER"
			)
		val updatedUser =
			createTestUser(
				id = securityUser.userId,
				name = request.name
			)
		val accessToken = "test-access-token"
		val output = UserOutput.AuthenticatedUserOutput.from(updatedUser, accessToken)

		whenever(
			updateMeUseCase.execute(
				UpdateMeInput(
					userId = securityUser.userId,
					name = request.name
				)
			)
		) doReturn output

		val response = userController.updateMe(request, securityUser)

		assertNotNull(response)
		assertEquals(HttpStatus.OK, response.statusCode)

		val body =
			requireNotNull(response.body) {
				"Response body should not be null"
			}
		assertEquals(securityUser.userId, body.userId)
		assertEquals(request.name, body.name)
		assertEquals(accessToken, body.accessToken)

		verify(updateMeUseCase).execute(
			UpdateMeInput(
				userId = securityUser.userId,
				name = request.name
			)
		)
	}

	@Test
	@DisplayName("Delete user returns no content")
	fun deleteUser_withValidId_returnsNoContent() {
		val userId = 1L

		doNothing().whenever(deleteUserUseCase).execute(
			DeleteUserInput(userId = userId)
		)

		val response = userController.deleteUser(userId)

		assertNotNull(response)
		assertEquals(HttpStatus.NO_CONTENT, response.statusCode)
		assertNull(response.body)

		verify(deleteUserUseCase).execute(
			DeleteUserInput(userId = userId)
		)
	}
}
