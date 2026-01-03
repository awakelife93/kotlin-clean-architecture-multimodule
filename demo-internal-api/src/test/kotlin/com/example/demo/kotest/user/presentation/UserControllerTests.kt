package com.example.demo.kotest.user.presentation

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
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime

@ActiveProfiles("test")
@Tags("kotest-unit-test")
class UserControllerTests :
	FunSpec({

		val getUserByIdUseCase = mockk<GetUserByIdUseCase>()
		val getUserListUseCase = mockk<GetUserListUseCase>()
		val createUserUseCase = mockk<CreateUserUseCase>()
		val updateUserUseCase = mockk<UpdateUserUseCase>()
		val updateMeUseCase = mockk<UpdateMeUseCase>()
		val deleteUserUseCase = mockk<DeleteUserUseCase>()

		val userController =
			UserController(
				getUserByIdUseCase,
				getUserListUseCase,
				createUserUseCase,
				updateUserUseCase,
				updateMeUseCase,
				deleteUserUseCase
			)

		fun createTestUser(
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

		fun createSecurityUser(
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

		beforeTest {
			clearAllMocks()
		}

		test("Get Me - returns current user successfully") {
			val securityUser = createSecurityUser()
			val user = createTestUser(id = securityUser.userId)
			val output = UserOutput.BaseUserOutput.from(user)

			every { getUserByIdUseCase.execute(GetUserByIdInput(securityUser.userId)) } returns output

			val response = userController.getMe(securityUser)

			response shouldNotBeNull {
				statusCode shouldBe HttpStatus.OK
				body shouldNotBeNull {
					userId shouldBe user.id
					name shouldBe user.name
					email shouldBe user.email
					role shouldBe user.role
				}
			}

			verify { getUserByIdUseCase.execute(GetUserByIdInput(securityUser.userId)) }
		}

		test("Get User By Id - returns user successfully") {
			val userId = 1L
			val user = createTestUser(id = userId)
			val output = UserOutput.BaseUserOutput.from(user)

			every { getUserByIdUseCase.execute(GetUserByIdInput(userId)) } returns output

			val response = userController.getUserById(userId)

			response shouldNotBeNull {
				statusCode shouldBe HttpStatus.OK
				body shouldNotBeNull {
					userId shouldBe user.id
					name shouldBe user.name
					email shouldBe user.email
					role shouldBe user.role
				}
			}

			verify { getUserByIdUseCase.execute(GetUserByIdInput(userId)) }
		}

		test("Get User List - returns paginated users") {
			val user = createTestUser()
			val pageable = PageRequest.of(0, 10)
			val pageOutput =
				UserOutput.UserPageListOutput.from(
					PageImpl(listOf(user), pageable, 1)
				)

			every { getUserListUseCase.execute(GetUserListInput(pageable)) } returns pageOutput

			val response = userController.getUserList(pageable)

			response shouldNotBeNull {
				statusCode shouldBe HttpStatus.OK
				body shouldNotBeNull {
					content.size shouldBe 1
					content[0] shouldNotBeNull {
						userId shouldBe user.id
						name shouldBe user.name
						email shouldBe user.email
						role shouldBe user.role
					}
				}
			}

			verify { getUserListUseCase.execute(GetUserListInput(pageable)) }
		}

		test("Create User - creates and returns new user") {
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

			every {
				createUserUseCase.execute(
					CreateUserInput(
						name = request.name,
						email = request.email,
						password = request.password
					)
				)
			} returns output

			val response = userController.createUser(request)

			response shouldNotBeNull {
				statusCode shouldBe HttpStatus.CREATED
				body shouldNotBeNull {
					userId shouldBe createdUser.id
					name shouldBe request.name
					email shouldBe request.email
					accessToken shouldBe accessToken
				}
			}

			verify {
				createUserUseCase.execute(
					CreateUserInput(
						name = request.name,
						email = request.email,
						password = request.password
					)
				)
			}
		}

		test("Update User - updates and returns modified user") {
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

			every {
				updateUserUseCase.execute(
					UpdateUserInput(
						userId = userId,
						name = request.name,
						role = request.role
					)
				)
			} returns output

			val response = userController.updateUser(request, userId)

			response shouldNotBeNull {
				statusCode shouldBe HttpStatus.OK
				body shouldNotBeNull {
					userId shouldBe userId
					name shouldBe request.name
					role shouldBe UserRole.ADMIN
				}
			}

			verify {
				updateUserUseCase.execute(
					UpdateUserInput(
						userId = userId,
						name = request.name,
						role = request.role
					)
				)
			}
		}

		test("Update Me - updates and returns current user") {
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

			every {
				updateMeUseCase.execute(
					UpdateMeInput(
						userId = securityUser.userId,
						name = request.name
					)
				)
			} returns output

			val response = userController.updateMe(request, securityUser)

			response shouldNotBeNull {
				statusCode shouldBe HttpStatus.OK
				body shouldNotBeNull {
					userId shouldBe securityUser.userId
					name shouldBe request.name
					accessToken shouldBe accessToken
				}
			}

			verify {
				updateMeUseCase.execute(
					UpdateMeInput(
						userId = securityUser.userId,
						name = request.name
					)
				)
			}
		}

		test("Delete User - deletes user and returns no content") {
			val userId = 1L

			justRun {
				deleteUserUseCase.execute(
					DeleteUserInput(userId = userId)
				)
			}

			val response = userController.deleteUser(userId)

			response shouldNotBeNull {
				statusCode shouldBe HttpStatus.NO_CONTENT
				body.shouldBeNull()
			}

			verify {
				deleteUserUseCase.execute(
					DeleteUserInput(userId = userId)
				)
			}
		}
	})
