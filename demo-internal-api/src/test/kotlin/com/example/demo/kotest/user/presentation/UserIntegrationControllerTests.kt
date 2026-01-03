package com.example.demo.kotest.user.presentation

import com.example.demo.kotest.common.BaseIntegrationController
import com.example.demo.kotest.common.security.SecurityListenerFactory
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
import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.annotation.Tags
import io.mockk.every
import io.mockk.justRun
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@ActiveProfiles("test")
@Tags("kotest-integration-test")
@WebMvcTest(UserController::class)
class UserIntegrationControllerTests : BaseIntegrationController() {
	@MockkBean
	private lateinit var getUserByIdUseCase: GetUserByIdUseCase

	@MockkBean
	private lateinit var getUserListUseCase: GetUserListUseCase

	@MockkBean
	private lateinit var createUserUseCase: CreateUserUseCase

	@MockkBean
	private lateinit var updateUserUseCase: UpdateUserUseCase

	@MockkBean
	private lateinit var updateMeUseCase: UpdateMeUseCase

	@MockkBean
	private lateinit var deleteUserUseCase: DeleteUserUseCase

	val user =
		User(
			id = 1L,
			name = "Test User",
			email = "test@example.com",
			password = "encodedPassword",
			role = UserRole.USER
		)
	val accessToken = "test-access-token"
	val defaultPageable = Pageable.ofSize(1)
	val baseUserOutput = UserOutput.BaseUserOutput.from(user)
	val authenticatedUserOutput = UserOutput.AuthenticatedUserOutput.from(user, accessToken)
	val userPageListOutput = UserOutput.UserPageListOutput.from(PageImpl(listOf(user), defaultPageable, 1))
	val emptyUserPageListOutput = UserOutput.UserPageListOutput.from(PageImpl(listOf(), defaultPageable, 0))

	init {
		initialize()

		Given("GET /api/v1/users/me") {

			When("Success GET /api/v1/users/me") {

				every { getUserByIdUseCase.execute(any<GetUserByIdInput>()) } returns baseUserOutput

				Then("Call GET /api/v1/users/me") {
					mockMvc
						.perform(
							MockMvcRequestBuilders
								.get("/api/v1/users/me")
								.with(SecurityMockMvcRequestPostProcessors.csrf())
								.contentType(MediaType.APPLICATION_JSON)
								.accept(MediaType.APPLICATION_JSON)
						).andExpect(MockMvcResultMatchers.status().isOk)
						.andExpect(MockMvcResultMatchers.jsonPath("$.code").value(commonStatus))
						.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(commonMessage))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.userId").value(user.id))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.name").value(user.name))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.email").value(user.email))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.role").value(user.role.name))
				}
			}

			When("Not Found Exception GET /api/v1/users/me") {
				val userNotFoundException = UserNotFoundException(user.id)

				every { getUserByIdUseCase.execute(any<GetUserByIdInput>()) } throws userNotFoundException

				Then("Call GET /api/v1/users/me") {
					mockMvc
						.perform(
							MockMvcRequestBuilders
								.get("/api/v1/users/me")
								.with(SecurityMockMvcRequestPostProcessors.csrf())
								.contentType(MediaType.APPLICATION_JSON)
								.accept(MediaType.APPLICATION_JSON)
						).andExpect(MockMvcResultMatchers.status().isNotFound)
						.andExpect(MockMvcResultMatchers.jsonPath("$.code").value(HttpStatus.NOT_FOUND.value()))
						.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(userNotFoundException.message))
						.andExpect(MockMvcResultMatchers.jsonPath("$.errors").isEmpty)
				}
			}
		}

		Given("GET /api/v1/users/{userId}") {

			When("Success GET /api/v1/users/{userId}") {

				every { getUserByIdUseCase.execute(any<GetUserByIdInput>()) } returns baseUserOutput

				Then("Call GET /api/v1/users/{userId}") {
					mockMvc
						.perform(
							MockMvcRequestBuilders
								.get("/api/v1/users/{userId}", user.id)
								.with(SecurityMockMvcRequestPostProcessors.csrf())
								.contentType(MediaType.APPLICATION_JSON)
								.accept(MediaType.APPLICATION_JSON)
						).andExpect(MockMvcResultMatchers.status().isOk)
						.andExpect(MockMvcResultMatchers.jsonPath("$.code").value(commonStatus))
						.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(commonMessage))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.userId").value(user.id))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.name").value(user.name))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.email").value(user.email))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.role").value(user.role.name))
				}
			}

			When("Not Found Exception GET /api/v1/users/{userId}") {
				val userNotFoundException = UserNotFoundException(user.id)

				every { getUserByIdUseCase.execute(any<GetUserByIdInput>()) } throws userNotFoundException

				Then("Call GET /api/v1/users/{userId}") {
					mockMvc
						.perform(
							MockMvcRequestBuilders
								.get("/api/v1/users/{userId}", user.id)
								.with(SecurityMockMvcRequestPostProcessors.csrf())
								.contentType(MediaType.APPLICATION_JSON)
								.accept(MediaType.APPLICATION_JSON)
						).andExpect(MockMvcResultMatchers.status().isNotFound)
						.andExpect(MockMvcResultMatchers.jsonPath("$.code").value(HttpStatus.NOT_FOUND.value()))
						.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(userNotFoundException.message))
						.andExpect(MockMvcResultMatchers.jsonPath("$.errors").isEmpty)
				}
			}
		}

		Given("GET /api/v1/users") {

			When("Success GET /api/v1/users") {

				every { getUserListUseCase.execute(any<GetUserListInput>()) } returns
					userPageListOutput

				Then("Call GET /api/v1/users") {
					mockMvc
						.perform(
							MockMvcRequestBuilders
								.get("/api/v1/users")
								.with(SecurityMockMvcRequestPostProcessors.csrf())
								.contentType(MediaType.APPLICATION_JSON)
								.accept(MediaType.APPLICATION_JSON)
						).andExpect(MockMvcResultMatchers.status().isOk)
						.andExpect(MockMvcResultMatchers.jsonPath("$.code").value(commonStatus))
						.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(commonMessage))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.content[0].userId").value(user.id))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.content[0].name").value(user.name))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.content[0].email").value(user.email))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.content[0].role").value(user.role.name))
				}
			}

			When("Empty GET /api/v1/users") {

				every { getUserListUseCase.execute(any<GetUserListInput>()) } returns
					emptyUserPageListOutput

				Then("Call GET /api/v1/users") {
					mockMvc
						.perform(
							MockMvcRequestBuilders
								.get("/api/v1/users")
								.with(SecurityMockMvcRequestPostProcessors.csrf())
								.contentType(MediaType.APPLICATION_JSON)
								.accept(MediaType.APPLICATION_JSON)
						).andExpect(MockMvcResultMatchers.status().isOk)
						.andExpect(MockMvcResultMatchers.jsonPath("$.code").value(commonStatus))
						.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(commonMessage))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.content").isEmpty)
				}
			}
		}

		Given("POST /api/v1/users/register") {
			val createUserRequest =
				CreateUserRequest(
					name = "New User",
					email = "newuser@example.com",
					password = "password123"
				)

			When("Success POST /api/v1/users/register") {

				every { createUserUseCase.execute(any<CreateUserInput>()) } returns authenticatedUserOutput

				Then("Call POST /api/v1/users/register") {
					mockMvc
						.perform(
							MockMvcRequestBuilders
								.post("/api/v1/users/register")
								.with(SecurityMockMvcRequestPostProcessors.csrf())
								.content(objectMapper.writeValueAsString(createUserRequest))
								.contentType(MediaType.APPLICATION_JSON)
								.accept(MediaType.APPLICATION_JSON)
						).andExpect(MockMvcResultMatchers.status().isCreated)
						.andExpect(MockMvcResultMatchers.jsonPath("$.code").value(commonStatus))
						.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(commonMessage))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.userId").value(user.id))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.name").value(user.name))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.email").value(user.email))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.accessToken").value(accessToken))
				}
			}

			When("Field Valid Exception POST /api/v1/users/register") {
				val wrongCreateUserRequest =
					CreateUserRequest(
						name = "",
						email = "invalid-email",
						password = "short"
					)

				Then("Call POST /api/v1/users/register") {
					mockMvc
						.perform(
							MockMvcRequestBuilders
								.post("/api/v1/users/register")
								.with(SecurityMockMvcRequestPostProcessors.csrf())
								.content(objectMapper.writeValueAsString(wrongCreateUserRequest))
								.contentType(MediaType.APPLICATION_JSON)
								.accept(MediaType.APPLICATION_JSON)
						).andExpect(MockMvcResultMatchers.status().isBadRequest)
						.andExpect(MockMvcResultMatchers.jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
						.andExpect(MockMvcResultMatchers.jsonPath("$.message").isString)
						.andExpect(MockMvcResultMatchers.jsonPath("$.errors").isNotEmpty)
				}
			}
		}

		Given("PATCH /api/v1/users/{userId}") {
			val updateUserRequest =
				UpdateUserRequest(
					name = "Updated User",
					role = "ADMIN"
				)

			When("Success PATCH /api/v1/users/{userId}") {

				every { updateUserUseCase.execute(any<UpdateUserInput>()) } returns baseUserOutput

				Then("Call PATCH /api/v1/users/{userId}") {
					mockMvc
						.perform(
							MockMvcRequestBuilders
								.patch("/api/v1/users/{userId}", user.id)
								.with(SecurityMockMvcRequestPostProcessors.csrf())
								.content(objectMapper.writeValueAsString(updateUserRequest))
								.contentType(MediaType.APPLICATION_JSON)
								.accept(MediaType.APPLICATION_JSON)
						).andExpect(MockMvcResultMatchers.status().isOk)
						.andExpect(MockMvcResultMatchers.jsonPath("$.code").value(commonStatus))
						.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(commonMessage))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.userId").value(user.id))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.name").value(user.name))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.email").value(user.email))
				}
			}

			When("Not Found Exception PATCH /api/v1/users/{userId}") {
				val userNotFoundException = UserNotFoundException(user.id)

				every { updateUserUseCase.execute(any<UpdateUserInput>()) } throws userNotFoundException

				Then("Call PATCH /api/v1/users/{userId}") {
					mockMvc
						.perform(
							MockMvcRequestBuilders
								.patch("/api/v1/users/{userId}", user.id)
								.with(SecurityMockMvcRequestPostProcessors.csrf())
								.content(objectMapper.writeValueAsString(updateUserRequest))
								.contentType(MediaType.APPLICATION_JSON)
								.accept(MediaType.APPLICATION_JSON)
						).andExpect(MockMvcResultMatchers.status().isNotFound)
						.andExpect(MockMvcResultMatchers.jsonPath("$.code").value(HttpStatus.NOT_FOUND.value()))
						.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(userNotFoundException.message))
						.andExpect(MockMvcResultMatchers.jsonPath("$.errors").isEmpty)
				}
			}
		}

		Given("PATCH /api/v1/users") {
			val updateUserRequest =
				UpdateUserRequest(
					name = "Updated My Name",
					role = "USER"
				)

			When("Success PATCH /api/v1/users") {

				every { updateMeUseCase.execute(any<UpdateMeInput>()) } returns authenticatedUserOutput

				Then("Call PATCH /api/v1/users") {
					mockMvc
						.perform(
							MockMvcRequestBuilders
								.patch("/api/v1/users")
								.with(SecurityMockMvcRequestPostProcessors.csrf())
								.content(objectMapper.writeValueAsString(updateUserRequest))
								.contentType(MediaType.APPLICATION_JSON)
								.accept(MediaType.APPLICATION_JSON)
						).andExpect(MockMvcResultMatchers.status().isOk)
						.andExpect(MockMvcResultMatchers.jsonPath("$.code").value(commonStatus))
						.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(commonMessage))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.userId").value(user.id))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.name").value(user.name))
						.andExpect(MockMvcResultMatchers.jsonPath("$.data.accessToken").value(accessToken))
				}
			}
		}

		Given("DELETE /api/v1/users/{userId}") {

			When("Success DELETE /api/v1/users/{userId}") {

				justRun { deleteUserUseCase.execute(any<DeleteUserInput>()) }

				Then("Call DELETE /api/v1/users/{userId}") {
					mockMvc
						.perform(
							MockMvcRequestBuilders
								.delete("/api/v1/users/{userId}", user.id)
								.with(SecurityMockMvcRequestPostProcessors.csrf())
								.contentType(MediaType.APPLICATION_JSON)
								.accept(MediaType.APPLICATION_JSON)
						).andExpect(MockMvcResultMatchers.status().isNoContent)
						.andExpect(MockMvcResultMatchers.jsonPath("$.code").value(commonStatus))
						.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(commonMessage))
				}
			}
		}

		Given("Spring Security Context is not set.") {

			When("UnAuthorized Exception GET /api/v1/users/me") {

				Then("Call GET /api/v1/users/me").config(tags = setOf(SecurityListenerFactory.NonSecurityOption)) {
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

			When("UnAuthorized Exception GET /api/v1/users/{userId}") {

				Then("Call GET /api/v1/users/{userId}").config(tags = setOf(SecurityListenerFactory.NonSecurityOption)) {
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

			When("UnAuthorized Exception GET /api/v1/users") {

				Then("Call GET /api/v1/users").config(tags = setOf(SecurityListenerFactory.NonSecurityOption)) {
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

			When("UnAuthorized Exception PATCH /api/v1/users/{userId}") {

				Then("Call PATCH /api/v1/users/{userId}").config(tags = setOf(SecurityListenerFactory.NonSecurityOption)) {
					mockMvc
						.perform(
							MockMvcRequestBuilders
								.patch("/api/v1/users/{userId}", user.id)
								.with(SecurityMockMvcRequestPostProcessors.csrf())
								.contentType(MediaType.APPLICATION_JSON)
								.accept(MediaType.APPLICATION_JSON)
						).andExpect(MockMvcResultMatchers.status().isUnauthorized)
				}
			}

			When("UnAuthorized Exception PATCH /api/v1/users") {

				Then("Call PATCH /api/v1/users").config(tags = setOf(SecurityListenerFactory.NonSecurityOption)) {
					mockMvc
						.perform(
							MockMvcRequestBuilders
								.patch("/api/v1/users")
								.with(SecurityMockMvcRequestPostProcessors.csrf())
								.contentType(MediaType.APPLICATION_JSON)
								.accept(MediaType.APPLICATION_JSON)
						).andExpect(MockMvcResultMatchers.status().isUnauthorized)
				}
			}

			When("UnAuthorized Exception DELETE /api/v1/users/{userId}") {

				Then("Call DELETE /api/v1/users/{userId}").config(tags = setOf(SecurityListenerFactory.NonSecurityOption)) {
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
	}
}
