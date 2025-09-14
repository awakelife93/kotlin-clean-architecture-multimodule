package com.example.demo.kotest.user.service

import com.example.demo.persistence.auth.provider.TokenProvider
import com.example.demo.user.constant.UserRole
import com.example.demo.user.event.UserEvent
import com.example.demo.user.exception.UserAlreadyExistsException
import com.example.demo.user.exception.UserNotFoundException
import com.example.demo.user.model.User
import com.example.demo.user.port.UserPort
import com.example.demo.user.port.input.CreateUserInput
import com.example.demo.user.service.UserService
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime

@ActiveProfiles("test")
@Tags("kotest-unit-test")
class UserServiceTests :
	BehaviorSpec({

		val userPort = mockk<UserPort>()
		val bCryptPasswordEncoder = mockk<BCryptPasswordEncoder>()
		val tokenProvider = mockk<TokenProvider>()
		val applicationEventPublisher = mockk<ApplicationEventPublisher>()
		val userService = UserService(userPort, bCryptPasswordEncoder, tokenProvider, applicationEventPublisher)

		fun createTestUser(
			id: Long = 1L,
			email: String = "test@example.com",
			password: String = "encoded_password",
			name: String = "Test User",
			role: UserRole = UserRole.USER
		) = User(
			id = id,
			email = email,
			password = password,
			name = name,
			role = role,
			createdDt = LocalDateTime.now(),
			updatedDt = LocalDateTime.now()
		)

		beforeTest {
			clearAllMocks()
		}

		Given("findOneById") {
			When("User exists") {
				Then("Should return user") {
					val testUser = createTestUser()
					every { userPort.findOneById(1L) } returns testUser

					val result = userService.findOneById(1L)

					result shouldNotBeNull {
						id shouldBe 1L
						email shouldBe "test@example.com"
					}

					verify(exactly = 1) { userPort.findOneById(1L) }
				}
			}

			When("User does not exist") {
				Then("Should return null") {
					every { userPort.findOneById(999L) } returns null

					userService.findOneById(999L).shouldBeNull()

					verify(exactly = 1) { userPort.findOneById(999L) }
				}
			}
		}

		Given("findOneByEmail") {
			When("User with email exists") {
				Then("Should return user") {
					val testUser = createTestUser()
					every { userPort.findOneByEmail("test@example.com") } returns testUser

					val result = userService.findOneByEmail("test@example.com")

					result shouldNotBeNull {
						email shouldBe "test@example.com"
					}

					verify(exactly = 1) { userPort.findOneByEmail("test@example.com") }
				}
			}

			When("User with email does not exist") {
				Then("Should return null") {
					every { userPort.findOneByEmail("notfound@example.com") } returns null

					userService.findOneByEmail("notfound@example.com").shouldBeNull()

					verify(exactly = 1) { userPort.findOneByEmail("notfound@example.com") }
				}
			}
		}

		Given("findAll") {
			When("Get users with pagination") {
				Then("Should return paginated users") {
					val pageable = PageRequest.of(0, 10)
					val users = listOf(createTestUser())
					every { userPort.findAll(pageable) } returns PageImpl(users, pageable, 1)

					val result = userService.findAll(pageable)

					result shouldNotBeNull {
						content.size shouldBe 1
						totalElements shouldBe 1
					}

					verify(exactly = 1) { userPort.findAll(pageable) }
				}
			}
		}

		Given("createUser") {
			When("Email is not duplicated") {
				Then("Should create and return user") {
					val newUser = createTestUser(id = 0L, email = "new@example.com", name = "New User")
					val savedUser = newUser.copy(id = 2L)

					every { userPort.existsByEmail("new@example.com") } returns false
					every { userPort.save(newUser) } returns savedUser

					val result = userService.createUser(newUser)

					result shouldNotBeNull {
						id shouldBe 2L
						email shouldBe "new@example.com"
					}

					verify(exactly = 1) {
						userPort.existsByEmail("new@example.com")
						userPort.save(newUser)
					}
				}
			}

			When("Email is duplicated") {
				Then("Should throw exception") {
					val duplicateUser = createTestUser(id = 0L, email = "existing@example.com")
					every { userPort.existsByEmail("existing@example.com") } returns true

					val exception =
						shouldThrow<IllegalArgumentException> {
							userService.createUser(duplicateUser)
						}
					exception.message shouldBe "Email already exists: existing@example.com"

					verify(exactly = 1) { userPort.existsByEmail("existing@example.com") }
					verify(exactly = 0) { userPort.save(duplicateUser) }
				}
			}
		}

		Given("registerNewUser") {
			When("Email is not duplicated") {
				Then("Should register new user and send welcome event") {
					val input =
						CreateUserInput(
							email = "newuser@example.com",
							password = "password123",
							name = "New User"
						)
					val encodedPassword = "encoded_password"
					val savedUser =
						createTestUser(
							id = 1L,
							email = input.email,
							password = encodedPassword,
							name = input.name
						)
					val accessToken = "jwt.token.here"
					val eventSlot = slot<UserEvent.WelcomeSignUpEvent>()

					every { userService.isEmailDuplicated(input.email) } returns false
					every { bCryptPasswordEncoder.encode(input.password) } returns encodedPassword
					every { userPort.save(any<User>()) } returns savedUser
					every { tokenProvider.createFullTokens(savedUser) } returns accessToken
					justRun { applicationEventPublisher.publishEvent(capture(eventSlot)) }

					val result = userService.registerNewUser(input)

					result shouldNotBeNull {
						id shouldBe 1L
						email shouldBe "newuser@example.com"
						name shouldBe "New User"
						this.accessToken shouldBe accessToken
					}

					eventSlot.captured.email shouldBe "newuser@example.com"
					eventSlot.captured.name shouldBe "New User"

					verify(exactly = 1) {
						userPort.existsByEmail(input.email)
						bCryptPasswordEncoder.encode(input.password)
						userPort.save(any<User>())
						tokenProvider.createFullTokens(savedUser)
						applicationEventPublisher.publishEvent(any<UserEvent.WelcomeSignUpEvent>())
					}
				}
			}

			When("Email is duplicated") {
				Then("Should throw UserAlreadyExistsException") {
					val input =
						CreateUserInput(
							email = "existing@example.com",
							password = "password123",
							name = "Existing User"
						)

					every { userPort.existsByEmail(input.email) } returns true

					val exception =
						shouldThrow<UserAlreadyExistsException> {
							userService.registerNewUser(input)
						}
					exception.message shouldBe "Already User Exist email = existing@example.com"

					verify(exactly = 1) { userPort.existsByEmail(input.email) }
					verify(exactly = 0) {
						bCryptPasswordEncoder.encode(any<String>())
						userPort.save(any<User>())
						tokenProvider.createFullTokens(any<User>())
						applicationEventPublisher.publishEvent(any<UserEvent.WelcomeSignUpEvent>())
					}
				}
			}
		}

		Given("updateUser") {
			When("Update user") {
				Then("Should update and return user") {
					val updatedUser = createTestUser(name = "Updated Name")
					every { userPort.save(updatedUser) } returns updatedUser

					val result = userService.updateUser(updatedUser)

					result shouldNotBeNull {
						name shouldBe "Updated Name"
					}

					verify(exactly = 1) { userPort.save(updatedUser) }
				}
			}
		}

		Given("updateUserInfo") {
			When("User exists and email is not duplicated") {
				Then("Should update user info") {
					val userId = 1L
					val existingUser = createTestUser(id = userId, email = "old@example.com", name = "Old Name")
					val updatedName = "New Name"

					every { userPort.findOneById(userId) } returns existingUser
					every { userPort.save(any<User>()) } returns existingUser

					val result = userService.updateUserInfo(userId, updatedName)

					result shouldNotBeNull {
						id shouldBe userId
					}

					verify(exactly = 1) {
						userPort.findOneById(userId)
						userPort.save(any<User>())
					}
				}
			}

			When("User does not exist") {
				Then("Should throw UserNotFoundException") {
					val userId = 999L
					every { userPort.findOneById(userId) } returns null

					val exception =
						shouldThrow<UserNotFoundException> {
							userService.updateUserInfo(userId, "Name")
						}
					exception.message shouldBe "User Not Found userId = 999"

					verify(exactly = 1) { userPort.findOneById(userId) }
					verify(exactly = 0) { userPort.save(any<User>()) }
				}
			}
		}

		Given("deleteById") {
			When("User exists") {
				Then("Should delete user") {
					val user = createTestUser()
					every { userPort.findOneById(1L) } returns user
					justRun { userPort.deleteById(1L) }

					userService.deleteById(1L)

					verify(exactly = 1) {
						userPort.findOneById(1L)
						userPort.deleteById(1L)
					}
				}
			}

			When("User does not exist") {
				Then("Should throw UserNotFoundException") {
					every { userPort.findOneById(999L) } returns null

					val exception =
						shouldThrow<UserNotFoundException> {
							userService.deleteById(999L)
						}
					exception.message shouldBe "User Not Found userId = 999"

					verify(exactly = 1) { userPort.findOneById(999L) }
					verify(exactly = 0) { userPort.deleteById(999L) }
				}
			}
		}

		Given("existsById") {
			When("User exists") {
				Then("Should return true") {
					every { userPort.existsById(1L) } returns true

					userService.existsById(1L).shouldBeTrue()

					verify(exactly = 1) { userPort.existsById(1L) }
				}
			}

			When("User does not exist") {
				Then("Should return false") {
					every { userPort.existsById(999L) } returns false

					userService.existsById(999L).shouldBeFalse()

					verify(exactly = 1) { userPort.existsById(999L) }
				}
			}
		}

		Given("isEmailDuplicated") {
			When("Email exists") {
				Then("Should return true") {
					every { userPort.existsByEmail("existing@example.com") } returns true

					userService.isEmailDuplicated("existing@example.com").shouldBeTrue()

					verify(exactly = 1) { userPort.existsByEmail("existing@example.com") }
				}
			}

			When("Email does not exist") {
				Then("Should return false") {
					every { userPort.existsByEmail("new@example.com") } returns false

					userService.isEmailDuplicated("new@example.com").shouldBeFalse()

					verify(exactly = 1) { userPort.existsByEmail("new@example.com") }
				}
			}
		}
	})
