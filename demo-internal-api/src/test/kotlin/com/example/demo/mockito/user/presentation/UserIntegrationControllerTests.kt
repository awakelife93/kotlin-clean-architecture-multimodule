package com.example.demo.mockito.user.presentation

import com.example.demo.mockito.common.BaseIntegrationController
import com.example.demo.mockito.common.security.WithMockCustomUser
import com.example.demo.user.constant.UserRole
import com.example.demo.user.exception.UserNotFoundException
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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.whenever
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders

@ActiveProfiles("test")
@Tag("mockito-integration-test")
@DisplayName("Mockito Integration - User Controller Test")
@WebMvcTest(UserController::class)
@ExtendWith(MockitoExtension::class)
class UserIntegrationControllerTests : BaseIntegrationController() {
	@MockitoBean
	private lateinit var getUserByIdUseCase: GetUserByIdUseCase

	@MockitoBean
	private lateinit var getUserListUseCase: GetUserListUseCase

	@MockitoBean
	private lateinit var createUserUseCase: CreateUserUseCase

	@MockitoBean
	private lateinit var updateUserUseCase: UpdateUserUseCase

	@MockitoBean
	private lateinit var updateMeUseCase: UpdateMeUseCase

	@MockitoBean
	private lateinit var deleteUserUseCase: DeleteUserUseCase

	private val user =
		User(
			id = 1L,
			name = "Test User",
			email = "test@example.com",
			password = "encodedPassword",
			role = UserRole.USER
		)
	private val defaultPageable = Pageable.ofSize(1)
	private val baseUserOutput = UserOutput.BaseUserOutput.from(user)
	private val userPageListOutput = UserOutput.UserPageListOutput.from(PageImpl(listOf(user), defaultPageable, 1))
	private val emptyUserPageListOutput = UserOutput.UserPageListOutput.from(PageImpl(listOf(), defaultPageable, 0))

	@BeforeEach
	fun setUp() {
		mockMvc =
			MockMvcBuilders
				.webAppContextSetup(webApplicationContext)
				.apply<DefaultMockMvcBuilder>(SecurityMockMvcConfigurers.springSecurity())
				.alwaysDo<DefaultMockMvcBuilder>(MockMvcResultHandlers.print())
				.build()
	}

	@Nested
	@DisplayName("GET /api/v1/users/me Test")
	inner class GetMeTest {
		@Test
		@DisplayName("GET /api/v1/users/me Response")
		@WithMockCustomUser
		@Throws(Exception::class)
		fun should_ExpectOKResponseToGetUserResponse_when_UserIsAuthenticated() {
			whenever(getUserByIdUseCase.execute(any<GetUserByIdInput>()))
				.thenReturn(baseUserOutput)

			mockMvc
				.perform(
					MockMvcRequestBuilders
						.get("/api/v1/users/me")
						.with(SecurityMockMvcRequestPostProcessors.csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)
				).andExpect(MockMvcResultMatchers.status().isOk)
				.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(commonMessage))
				.andExpect(MockMvcResultMatchers.jsonPath("$.data.userId").value(user.id))
				.andExpect(MockMvcResultMatchers.jsonPath("$.data.name").value(user.name))
				.andExpect(MockMvcResultMatchers.jsonPath("$.data.email").value(user.email))
				.andExpect(MockMvcResultMatchers.jsonPath("$.data.role").value(user.role.name))
		}

		@Test
		@DisplayName("Not Found Exception GET /api/v1/users/me Response")
		@WithMockCustomUser
		@Throws(Exception::class)
		fun should_ExpectErrorResponseToUserNotFoundException_when_UserIsAuthenticated() {
			val userNotFoundException = UserNotFoundException(user.id)

			whenever(getUserByIdUseCase.execute(any<GetUserByIdInput>()))
				.thenThrow(userNotFoundException)

			mockMvc
				.perform(
					MockMvcRequestBuilders
						.get("/api/v1/users/me")
						.with(SecurityMockMvcRequestPostProcessors.csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)
				).andExpect(MockMvcResultMatchers.status().isNotFound)
				.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(userNotFoundException.message))
				.andExpect(MockMvcResultMatchers.jsonPath("$.errors").isEmpty)
		}

		@Test
		@DisplayName("Unauthorized Exception GET /api/v1/users/me Response")
		@Throws(Exception::class)
		fun should_ExpectErrorResponseToUnauthorizedException_when_UserIsNotAuthenticated() {
			mockMvc
				.perform(
					MockMvcRequestBuilders
						.get("/api/v1/users/me")
						.with(SecurityMockMvcRequestPostProcessors.csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)
				).andExpect(MockMvcResultMatchers.status().isUnauthorized)
		}
	}

	@Nested
	@DisplayName("GET /api/v1/users/{userId} Test")
	inner class GetUserByIdTest {
		@Test
		@DisplayName("GET /api/v1/users/{userId} Response")
		@WithMockCustomUser(role = "ADMIN")
		@Throws(Exception::class)
		fun should_ExpectOKResponseToGetUserResponse_when_GivenUserIdAndUserIsAdmin() {
			whenever(getUserByIdUseCase.execute(any<GetUserByIdInput>()))
				.thenReturn(baseUserOutput)

			mockMvc
				.perform(
					MockMvcRequestBuilders
						.get("/api/v1/users/{userId}", user.id)
						.with(SecurityMockMvcRequestPostProcessors.csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)
				).andExpect(MockMvcResultMatchers.status().isOk)
				.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(commonMessage))
				.andExpect(MockMvcResultMatchers.jsonPath("$.data.userId").value(user.id))
				.andExpect(MockMvcResultMatchers.jsonPath("$.data.name").value(user.name))
				.andExpect(MockMvcResultMatchers.jsonPath("$.data.email").value(user.email))
				.andExpect(MockMvcResultMatchers.jsonPath("$.data.role").value(user.role.name))
		}

		@Test
		@DisplayName("Not Found Exception GET /api/v1/users/{userId} Response")
		@WithMockCustomUser(role = "ADMIN")
		@Throws(Exception::class)
		fun should_ExpectErrorResponseToUserNotFoundException_when_GivenUserIdAndUserIsAdmin() {
			val userNotFoundException = UserNotFoundException(user.id)

			whenever(getUserByIdUseCase.execute(any<GetUserByIdInput>()))
				.thenThrow(userNotFoundException)

			mockMvc
				.perform(
					MockMvcRequestBuilders
						.get("/api/v1/users/{userId}", user.id)
						.with(SecurityMockMvcRequestPostProcessors.csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)
				).andExpect(MockMvcResultMatchers.status().isNotFound)
				.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(userNotFoundException.message))
				.andExpect(MockMvcResultMatchers.jsonPath("$.errors").isEmpty)
		}

		@Test
		@DisplayName("Unauthorized Exception GET /api/v1/users/{userId} Response")
		@Throws(Exception::class)
		fun should_ExpectErrorResponseToUnauthorizedException_when_GivenUserIdAndUserIsNotAuthenticated() {
			mockMvc
				.perform(
					MockMvcRequestBuilders
						.get("/api/v1/users/{userId}", user.id)
						.with(SecurityMockMvcRequestPostProcessors.csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)
				).andExpect(MockMvcResultMatchers.status().isUnauthorized)
		}
	}

	@Nested
	@DisplayName("GET /api/v1/users Test")
	inner class GetUserListTest {
		@Test
		@DisplayName("GET /api/v1/users Response")
		@WithMockCustomUser(role = "ADMIN")
		@Throws(Exception::class)
		fun should_ExpectOKResponseToPageOfGetUserResponse_when_GivenDefaultPageableAndUserIsAdmin() {
			whenever(getUserListUseCase.execute(any<GetUserListInput>()))
				.thenReturn(userPageListOutput)

			mockMvc
				.perform(
					MockMvcRequestBuilders
						.get("/api/v1/users")
						.with(SecurityMockMvcRequestPostProcessors.csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)
				).andExpect(MockMvcResultMatchers.status().isOk)
				.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(commonMessage))
				.andExpect(MockMvcResultMatchers.jsonPath("$.data.content[0].userId").value(user.id))
				.andExpect(MockMvcResultMatchers.jsonPath("$.data.content[0].name").value(user.name))
				.andExpect(MockMvcResultMatchers.jsonPath("$.data.content[0].email").value(user.email))
				.andExpect(MockMvcResultMatchers.jsonPath("$.data.content[0].role").value(user.role.name))
		}

		@Test
		@DisplayName("Empty GET /api/v1/users Response")
		@WithMockCustomUser(role = "ADMIN")
		@Throws(Exception::class)
		fun should_ExpectOKResponseToPageOfGetUserResponseIsEmpty_when_GivenDefaultPageableAndUserIsAdmin() {
			whenever(getUserListUseCase.execute(any<GetUserListInput>()))
				.thenReturn(emptyUserPageListOutput)

			mockMvc
				.perform(
					MockMvcRequestBuilders
						.get("/api/v1/users")
						.with(SecurityMockMvcRequestPostProcessors.csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)
				).andExpect(MockMvcResultMatchers.status().isOk)
				.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(commonMessage))
				.andExpect(MockMvcResultMatchers.jsonPath("$.data.content").isEmpty)
		}

		@Test
		@DisplayName("Unauthorized Exception GET /api/v1/users Response")
		@Throws(Exception::class)
		fun should_ExpectErrorResponseToUnauthorizedException_when_GivenDefaultPageableAndUserIsNotAuthenticated() {
			mockMvc
				.perform(
					MockMvcRequestBuilders
						.get("/api/v1/users")
						.with(SecurityMockMvcRequestPostProcessors.csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)
				).andExpect(MockMvcResultMatchers.status().isUnauthorized)
		}
	}

	@Nested
	@DisplayName("POST /api/v1/users/register Test")
	inner class CreateUserTest {
		private val createUserRequest =
			CreateUserRequest(
				name = "New User",
				email = "newuser@example.com",
				password = "password123"
			)

		@Test
		@DisplayName("POST /api/v1/users/register Response")
		@WithMockCustomUser
		@Throws(Exception::class)
		fun should_ExpectCreatedResponseToCreateUserResponse_when_GivenCreateUserRequest() {
			val accessToken = "test-access-token"
			val authenticatedUserOutput = UserOutput.AuthenticatedUserOutput.from(user, accessToken)

			whenever(createUserUseCase.execute(any<CreateUserInput>()))
				.thenReturn(authenticatedUserOutput)

			mockMvc
				.perform(
					MockMvcRequestBuilders
						.post("/api/v1/users/register")
						.with(SecurityMockMvcRequestPostProcessors.csrf())
						.content(objectMapper.writeValueAsString(createUserRequest))
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)
				).andExpect(MockMvcResultMatchers.status().isCreated)
				.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(commonMessage))
				.andExpect(MockMvcResultMatchers.jsonPath("$.data.userId").value(user.id))
				.andExpect(MockMvcResultMatchers.jsonPath("$.data.name").value(user.name))
				.andExpect(MockMvcResultMatchers.jsonPath("$.data.email").value(user.email))
		}

		@Test
		@DisplayName("Field Valid Exception POST /api/v1/users/register Response")
		@WithMockCustomUser
		@Throws(Exception::class)
		fun should_ExpectErrorResponseToValidException_when_GivenWrongCreateUserRequest() {
			val wrongCreateUserRequest =
				CreateUserRequest(
					name = "",
					email = "invalid-email",
					password = "short"
				)

			mockMvc
				.perform(
					MockMvcRequestBuilders
						.post("/api/v1/users/register")
						.with(SecurityMockMvcRequestPostProcessors.csrf())
						.content(objectMapper.writeValueAsString(wrongCreateUserRequest))
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)
				).andExpect(MockMvcResultMatchers.status().isBadRequest)
				.andExpect(MockMvcResultMatchers.jsonPath("$.message").isString)
				.andExpect(MockMvcResultMatchers.jsonPath("$.errors").isNotEmpty)
		}
	}

	@Nested
	@DisplayName("PATCH /api/v1/users/{userId} Test")
	inner class UpdateUserTest {
		private val updateUserRequest =
			UpdateUserRequest(
				name = "Updated User",
				role = "ADMIN"
			)

		@Test
		@DisplayName("PATCH /api/v1/users/{userId} Response")
		@WithMockCustomUser(role = "ADMIN")
		@Throws(Exception::class)
		fun should_ExpectOKResponseToUpdateUserResponse_when_GivenUserIdAndUpdateUserRequestAndUserIsAdmin() {
			whenever(updateUserUseCase.execute(any<UpdateUserInput>()))
				.thenReturn(baseUserOutput)

			mockMvc
				.perform(
					MockMvcRequestBuilders
						.patch("/api/v1/users/{userId}", user.id)
						.with(SecurityMockMvcRequestPostProcessors.csrf())
						.content(objectMapper.writeValueAsString(updateUserRequest))
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)
				).andExpect(MockMvcResultMatchers.status().isOk)
				.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(commonMessage))
				.andExpect(MockMvcResultMatchers.jsonPath("$.data.userId").value(user.id))
				.andExpect(MockMvcResultMatchers.jsonPath("$.data.name").value(user.name))
				.andExpect(MockMvcResultMatchers.jsonPath("$.data.email").value(user.email))
		}

		@Test
		@DisplayName("Not Found Exception PATCH /api/v1/users/{userId} Response")
		@WithMockCustomUser(role = "ADMIN")
		@Throws(Exception::class)
		fun should_ExpectErrorResponseToUserNotFoundException_when_GivenUserIdAndUpdateUserRequestAndUserIsAdmin() {
			val userNotFoundException = UserNotFoundException(user.id)

			whenever(updateUserUseCase.execute(any<UpdateUserInput>()))
				.thenThrow(userNotFoundException)

			mockMvc
				.perform(
					MockMvcRequestBuilders
						.patch("/api/v1/users/{userId}", user.id)
						.with(SecurityMockMvcRequestPostProcessors.csrf())
						.content(objectMapper.writeValueAsString(updateUserRequest))
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)
				).andExpect(MockMvcResultMatchers.status().isNotFound)
				.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(userNotFoundException.message))
				.andExpect(MockMvcResultMatchers.jsonPath("$.errors").isEmpty)
		}

		@Test
		@DisplayName("Unauthorized Exception PATCH /api/v1/users/{userId} Response")
		@Throws(Exception::class)
		fun should_ExpectErrorResponseToUnauthorizedException_when_GivenUserIdAndUpdateUserRequestAndUserIsNotAuthenticated() {
			mockMvc
				.perform(
					MockMvcRequestBuilders
						.patch("/api/v1/users/{userId}", user.id)
						.with(SecurityMockMvcRequestPostProcessors.csrf())
						.content(objectMapper.writeValueAsString(updateUserRequest))
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)
				).andExpect(MockMvcResultMatchers.status().isUnauthorized)
		}
	}

	@Nested
	@DisplayName("PATCH /api/v1/users Test")
	inner class UpdateMeTest {
		private val updateUserRequest =
			UpdateUserRequest(
				name = "Updated My Name",
				role = "USER"
			)

		@Test
		@DisplayName("PATCH /api/v1/users Response")
		@WithMockCustomUser
		@Throws(Exception::class)
		fun should_ExpectOKResponseToUpdateMeResponse_when_GivenUpdateUserRequestAndUserIsAuthenticated() {
			val accessToken = "test-access-token"
			val authenticatedUserOutput = UserOutput.AuthenticatedUserOutput.from(user, accessToken)

			whenever(updateMeUseCase.execute(any<UpdateMeInput>()))
				.thenReturn(authenticatedUserOutput)

			mockMvc
				.perform(
					MockMvcRequestBuilders
						.patch("/api/v1/users")
						.with(SecurityMockMvcRequestPostProcessors.csrf())
						.content(objectMapper.writeValueAsString(updateUserRequest))
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)
				).andExpect(MockMvcResultMatchers.status().isOk)
				.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(commonMessage))
				.andExpect(MockMvcResultMatchers.jsonPath("$.data.userId").value(user.id))
				.andExpect(MockMvcResultMatchers.jsonPath("$.data.name").value(user.name))
				.andExpect(MockMvcResultMatchers.jsonPath("$.data.accessToken").value(accessToken))
		}

		@Test
		@DisplayName("Unauthorized Exception PATCH /api/v1/users Response")
		@Throws(Exception::class)
		fun should_ExpectErrorResponseToUnauthorizedException_when_GivenUpdateUserRequestAndUserIsNotAuthenticated() {
			mockMvc
				.perform(
					MockMvcRequestBuilders
						.patch("/api/v1/users")
						.with(SecurityMockMvcRequestPostProcessors.csrf())
						.content(objectMapper.writeValueAsString(updateUserRequest))
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)
				).andExpect(MockMvcResultMatchers.status().isUnauthorized)
		}
	}

	@Nested
	@DisplayName("DELETE /api/v1/users/{userId} Test")
	inner class DeleteUserTest {
		@Test
		@DisplayName("DELETE /api/v1/users/{userId} Response")
		@WithMockCustomUser(role = "ADMIN")
		@Throws(Exception::class)
		fun should_ExpectNoContentResponse_when_GivenUserIdAndUserIsAdmin() {
			doNothing().whenever(deleteUserUseCase).execute(any<DeleteUserInput>())

			mockMvc
				.perform(
					MockMvcRequestBuilders
						.delete("/api/v1/users/{userId}", user.id)
						.with(SecurityMockMvcRequestPostProcessors.csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)
				).andExpect(MockMvcResultMatchers.status().isNoContent)
				.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(commonMessage))
		}

		@Test
		@DisplayName("Unauthorized Error DELETE /api/v1/users/{userId} Response")
		@Throws(Exception::class)
		fun should_ExpectErrorResponseToUnauthorizedException_when_GivenUserIdAndUserIsNotAuthenticated() {
			mockMvc
				.perform(
					MockMvcRequestBuilders
						.delete("/api/v1/users/{userId}", user.id)
						.with(SecurityMockMvcRequestPostProcessors.csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)
				).andExpect(MockMvcResultMatchers.status().isUnauthorized)
		}
	}
}
