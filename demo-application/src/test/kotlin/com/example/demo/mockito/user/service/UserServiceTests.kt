package com.example.demo.mockito.user.service

import com.example.demo.persistence.auth.provider.TokenProvider
import com.example.demo.user.constant.UserRole
import com.example.demo.user.event.UserEvent
import com.example.demo.user.exception.UserAlreadyExistsException
import com.example.demo.user.exception.UserNotFoundException
import com.example.demo.user.model.User
import com.example.demo.user.port.UserPort
import com.example.demo.user.port.input.CreateUserInput
import com.example.demo.user.service.UserService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime

@ActiveProfiles("test")
@Tag("mockito-unit-test")
@DisplayName("Mockito Unit - User Service Test")
@ExtendWith(MockitoExtension::class)
class UserServiceTests {
	@Mock
	private lateinit var userPort: UserPort

	@Mock
	private lateinit var bCryptPasswordEncoder: BCryptPasswordEncoder

	@Mock
	private lateinit var tokenProvider: TokenProvider

	@Mock
	private lateinit var applicationEventPublisher: ApplicationEventPublisher

	private lateinit var userService: UserService

	@BeforeEach
	fun setUp() {
		userService = UserService(userPort, bCryptPasswordEncoder, tokenProvider, applicationEventPublisher)
	}

	private fun createTestUser(
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

	@Nested
	@DisplayName("Find user")
	inner class FindUserTest {
		@Test
		@DisplayName("Find by ID returns user when exists")
		fun findOneById_whenUserExists_returnsUser() {
			val user = createTestUser()
			whenever(userPort.findOneById(1L)) doReturn user

			val result = userService.findOneById(1L)

			assertNotNull(result)
			assertEquals(user.id, result?.id)
			assertEquals(user.email, result?.email)
			verify(userPort).findOneById(1L)
		}

		@Test
		@DisplayName("Find by ID returns null when not exists")
		fun findOneById_whenUserNotExists_returnsNull() {
			whenever(userPort.findOneById(999L)) doReturn null

			val result = userService.findOneById(999L)

			assertNull(result)
			verify(userPort).findOneById(999L)
		}

		@Test
		@DisplayName("Find by email returns user when exists")
		fun findOneByEmail_whenUserExists_returnsUser() {
			val user = createTestUser()
			whenever(userPort.findOneByEmail(user.email)) doReturn user

			val result = userService.findOneByEmail(user.email)

			assertNotNull(result)
			assertEquals(user.email, result?.email)
			verify(userPort).findOneByEmail(user.email)
		}

		@Test
		@DisplayName("Find by email returns null when not exists")
		fun findOneByEmail_whenUserNotExists_returnsNull() {
			val email = "notfound@example.com"
			whenever(userPort.findOneByEmail(email)) doReturn null

			val result = userService.findOneByEmail(email)

			assertNull(result)
			verify(userPort).findOneByEmail(email)
		}

		@Test
		@DisplayName("Find all returns paginated users")
		fun findAll_withPagination_returnsPaginatedUsers() {
			val pageable = PageRequest.of(0, 10)
			val users = listOf(createTestUser())
			val userPage = PageImpl(users, pageable, 1)

			whenever(userPort.findAll(pageable)) doReturn userPage

			val result = userService.findAll(pageable)

			assertNotNull(result)
			assertEquals(1, result.content.size)
			assertEquals(1, result.totalElements)
			verify(userPort).findAll(pageable)
		}
	}

	@Nested
	@DisplayName("Create user")
	inner class CreateUserTest {
		@Test
		@DisplayName("Creates user when email is unique")
		fun createUser_withUniqueEmail_createsAndReturnsUser() {
			val newUser = createTestUser(id = 0L, email = "new@example.com")
			val savedUser = newUser.copy(id = 2L)

			whenever(userPort.existsByEmail(newUser.email)) doReturn false
			whenever(userPort.save(newUser)) doReturn savedUser

			val result = userService.createUser(newUser)

			assertNotNull(result)
			assertEquals(savedUser.id, result.id)
			assertEquals(savedUser.email, result.email)

			verify(userPort).existsByEmail(newUser.email)
			verify(userPort).save(newUser)
		}

		@Test
		@DisplayName("Throws exception when email is duplicated")
		fun createUser_withDuplicateEmail_throwsException() {
			val email = "existing@example.com"
			val duplicateUser = createTestUser(id = 0L, email = email)

			whenever(userPort.existsByEmail(email)) doReturn true

			val exception =
				assertThrows<IllegalArgumentException> {
					userService.createUser(duplicateUser)
				}

			assertEquals("Email already exists: $email", exception.message)

			verify(userPort).existsByEmail(email)
			verify(userPort, never()).save(duplicateUser)
		}
	}

	@Nested
	@DisplayName("Register new user")
	inner class RegisterNewUserTest {
		@Test
		@DisplayName("Registers new user successfully")
		fun registerNewUser_withValidInput_createsUserAndSendsEvent() {
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
			val eventCaptor = argumentCaptor<UserEvent.WelcomeSignUpEvent>()

			whenever(userPort.existsByEmail(input.email)) doReturn false
			whenever(bCryptPasswordEncoder.encode(input.password)) doReturn encodedPassword
			whenever(userPort.save(any<User>())) doReturn savedUser
			whenever(tokenProvider.createFullTokens(savedUser)) doReturn accessToken
			doNothing().whenever(applicationEventPublisher).publishEvent(eventCaptor.capture())

			val result = userService.registerNewUser(input)

			assertNotNull(result)
			assertEquals(1L, result.id)
			assertEquals("newuser@example.com", result.email)
			assertEquals("New User", result.name)
			assertEquals(accessToken, result.accessToken)

			val capturedEvent = eventCaptor.firstValue
			assertEquals("newuser@example.com", capturedEvent.email)
			assertEquals("New User", capturedEvent.name)

			verify(userPort).existsByEmail(input.email)
			verify(bCryptPasswordEncoder).encode(input.password)
			verify(userPort).save(any<User>())
			verify(tokenProvider).createFullTokens(savedUser)
			verify(applicationEventPublisher).publishEvent(any<UserEvent.WelcomeSignUpEvent>())
		}

		@Test
		@DisplayName("Throws exception when email is duplicated")
		fun registerNewUser_withDuplicateEmail_throwsException() {
			val input =
				CreateUserInput(
					email = "existing@example.com",
					password = "password123",
					name = "Existing User"
				)

			whenever(userPort.existsByEmail(input.email)) doReturn true

			val exception =
				assertThrows<UserAlreadyExistsException> {
					userService.registerNewUser(input)
				}

			assertEquals("Already User Exist email = existing@example.com", exception.message)

			verify(userPort).existsByEmail(input.email)
			verify(bCryptPasswordEncoder, never()).encode(any<String>())
			verify(userPort, never()).save(any<User>())
			verify(tokenProvider, never()).createFullTokens(any<User>())
			verify(applicationEventPublisher, never()).publishEvent(any<UserEvent.WelcomeSignUpEvent>())
		}
	}

	@Nested
	@DisplayName("Update user")
	inner class UpdateUserTest {
		@Test
		@DisplayName("Updates and returns user")
		fun updateUser_withValidUser_updatesAndReturnsUser() {
			val updatedUser = createTestUser(name = "Updated Name")
			whenever(userPort.save(updatedUser)) doReturn updatedUser

			val result = userService.updateUser(updatedUser)

			assertNotNull(result)
			assertEquals("Updated Name", result.name)
			verify(userPort).save(updatedUser)
		}
	}

	@Nested
	@DisplayName("Update user info")
	inner class UpdateUserInfoTest {
		@Test
		@DisplayName("Updates user info successfully")
		fun updateUserInfo_withValidInput_updatesAndReturnsUser() {
			val userId = 1L
			val existingUser = createTestUser(id = userId, email = "old@example.com", name = "Old Name")
			val updatedName = "New Name"

			whenever(userPort.findOneById(userId)) doReturn existingUser
			whenever(userPort.save(any<User>())) doReturn existingUser

			val result = userService.updateUserInfo(userId, updatedName)

			assertNotNull(result)
			assertEquals(userId, result.id)

			verify(userPort).findOneById(userId)
			verify(userPort).save(any<User>())
		}

		@Test
		@DisplayName("Throws exception when user not found")
		fun updateUserInfo_whenUserNotFound_throwsException() {
			val userId = 999L
			whenever(userPort.findOneById(userId)) doReturn null

			val exception =
				assertThrows<UserNotFoundException> {
					userService.updateUserInfo(userId, "Name")
				}

			assertEquals("User Not Found userId = 999", exception.message)

			verify(userPort).findOneById(userId)
			verify(userPort, never()).save(any<User>())
		}
	}

	@Nested
	@DisplayName("Delete user")
	inner class DeleteUserTest {
		@Test
		@DisplayName("Deletes user by ID when user exists")
		fun deleteById_withExistingUser_callsDeleteById() {
			val userId = 1L
			val user = createTestUser(id = userId)

			whenever(userPort.findOneById(userId)) doReturn user
			doNothing().whenever(userPort).deleteById(userId)

			userService.deleteById(userId)

			verify(userPort).findOneById(userId)
			verify(userPort).deleteById(userId)
		}

		@Test
		@DisplayName("Throws exception when user not found")
		fun deleteById_whenUserNotFound_throwsException() {
			val userId = 999L

			whenever(userPort.findOneById(userId)) doReturn null

			val exception =
				assertThrows<UserNotFoundException> {
					userService.deleteById(userId)
				}

			assertEquals("User Not Found userId = 999", exception.message)
			verify(userPort).findOneById(userId)
			verify(userPort, never()).deleteById(userId)
		}
	}

	@Nested
	@DisplayName("Check existence")
	inner class ExistenceCheckTest {
		@Test
		@DisplayName("Returns true when user exists by ID")
		fun existsById_whenUserExists_returnsTrue() {
			whenever(userPort.existsById(1L)) doReturn true

			val result = userService.existsById(1L)

			assertTrue(result)
			verify(userPort).existsById(1L)
		}

		@Test
		@DisplayName("Returns false when user not exists by ID")
		fun existsById_whenUserNotExists_returnsFalse() {
			whenever(userPort.existsById(999L)) doReturn false

			val result = userService.existsById(999L)

			assertFalse(result)
			verify(userPort).existsById(999L)
		}

		@Test
		@DisplayName("Returns true when email exists")
		fun isEmailDuplicated_whenEmailExists_returnsTrue() {
			val email = "existing@example.com"
			whenever(userPort.existsByEmail(email)) doReturn true

			val result = userService.isEmailDuplicated(email)

			assertTrue(result)
			verify(userPort).existsByEmail(email)
		}

		@Test
		@DisplayName("Returns false when email not exists")
		fun isEmailDuplicated_whenEmailNotExists_returnsFalse() {
			val email = "new@example.com"
			whenever(userPort.existsByEmail(email)) doReturn false

			val result = userService.isEmailDuplicated(email)

			assertFalse(result)
			verify(userPort).existsByEmail(email)
		}
	}
}
