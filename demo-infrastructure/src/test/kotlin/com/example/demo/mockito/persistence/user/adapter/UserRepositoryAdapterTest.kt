package com.example.demo.mockito.persistence.user.adapter

import com.example.demo.persistence.user.adapter.UserRepositoryAdapter
import com.example.demo.persistence.user.entity.UserEntity
import com.example.demo.persistence.user.mapper.UserMapper
import com.example.demo.persistence.user.repository.UserJpaRepository
import com.example.demo.user.constant.UserRole
import com.example.demo.user.exception.UserNotFoundException
import com.example.demo.user.model.User
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.never
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime
import java.util.Optional

@ActiveProfiles("test")
@Tag("mockito-unit-test")
@DisplayName("Mockito Unit - User Repository Adapter Test")
@ExtendWith(MockitoExtension::class)
class UserRepositoryAdapterTest {
	@Mock
	private lateinit var jpaRepository: UserJpaRepository

	@Mock
	private lateinit var userMapper: UserMapper

	private lateinit var adapter: UserRepositoryAdapter

	@BeforeEach
	fun setUp() {
		adapter = UserRepositoryAdapter(jpaRepository, userMapper)
	}

	@Nested
	@DisplayName("FindOneById Tests")
	inner class FindOneByIdTests {
		@Test
		@DisplayName("should return mapped user when user exists")
		fun `should return mapped user when user exists`() {
			val userId = 1L
			val entity = createUserEntity(userId)
			val user = createUser(userId)

			whenever(jpaRepository.findOneById(userId)).thenReturn(entity)
			whenever(userMapper.toDomain(entity)).thenReturn(user)

			val result = adapter.findOneById(userId)

			assertNotNull(result)
			assertEquals(userId, result?.id)
			assertEquals("John Doe", result?.name)
			assertEquals("john@example.com", result?.email)

			verify(jpaRepository, times(1)).findOneById(userId)
			verify(userMapper, times(1)).toDomain(entity)
		}

		@Test
		@DisplayName("should return null when user does not exist")
		fun `should return null when user does not exist`() {
			val userId = 999L
			whenever(jpaRepository.findOneById(userId)).thenReturn(null)

			val result = adapter.findOneById(userId)

			assertNull(result)

			verify(jpaRepository, times(1)).findOneById(userId)
			verify(userMapper, never()).toDomain(any<UserEntity>())
		}
	}

	@Nested
	@DisplayName("FindOneByEmail Tests")
	inner class FindOneByEmailTests {
		@Test
		@DisplayName("should return mapped user when user exists")
		fun `should return mapped user when user exists`() {
			val email = "john@example.com"
			val entity = createUserEntity(1L)
			val user = createUser(1L)

			whenever(jpaRepository.findOneByEmail(email)).thenReturn(entity)
			whenever(userMapper.toDomain(entity)).thenReturn(user)

			val result = adapter.findOneByEmail(email)

			assertNotNull(result)
			assertEquals(email, result?.email)

			verify(jpaRepository, times(1)).findOneByEmail(email)
			verify(userMapper, times(1)).toDomain(entity)
		}

		@Test
		@DisplayName("should return null when user does not exist")
		fun `should return null when user does not exist`() {
			val email = "notfound@example.com"
			whenever(jpaRepository.findOneByEmail(email)).thenReturn(null)

			val result = adapter.findOneByEmail(email)

			assertNull(result)

			verify(jpaRepository, times(1)).findOneByEmail(email)
			verify(userMapper, never()).toDomain(any<UserEntity>())
		}
	}

	@Nested
	@DisplayName("FindAll Tests")
	inner class FindAllTests {
		@Test
		@DisplayName("should return page of mapped users")
		fun `should return page of mapped users`() {
			val pageable = PageRequest.of(0, 10)
			val entities =
				listOf(
					UserEntity(
						name = "User 1",
						email = "user1@example.com",
						password = "password1",
						role = UserRole.USER
					).apply { id = 1L },
					UserEntity(
						name = "User 2",
						email = "user2@example.com",
						password = "password2",
						role = UserRole.USER
					).apply { id = 2L }
				)
			val users =
				listOf(
					User(1L, "User 1", "user1@example.com", "password1", UserRole.USER),
					User(2L, "User 2", "user2@example.com", "password2", UserRole.USER)
				)
			val entityPage = PageImpl(entities, pageable, 2)

			whenever(jpaRepository.findAll(pageable)).thenReturn(entityPage)
			entities.forEachIndexed { index, entity ->
				whenever(userMapper.toDomain(entity)).thenReturn(users[index])
			}

			val result = adapter.findAll(pageable)

			assertEquals(2, result.content.size)
			assertEquals(2, result.totalElements)
			assertEquals(users, result.content)

			verify(jpaRepository, times(1)).findAll(pageable)
			verify(userMapper, times(2)).toDomain(any<UserEntity>())
		}

		@Test
		@DisplayName("should return empty page when no users")
		fun `should return empty page when no users`() {
			val pageable = PageRequest.of(0, 10)
			val emptyPage = PageImpl<UserEntity>(emptyList(), pageable, 0)

			whenever(jpaRepository.findAll(pageable)).thenReturn(emptyPage)

			val result = adapter.findAll(pageable)

			assertEquals(0, result.content.size)
			assertEquals(0, result.totalElements)

			verify(jpaRepository, times(1)).findAll(pageable)
			verify(userMapper, never()).toDomain(any<UserEntity>())
		}
	}

	@Nested
	@DisplayName("ExistsByEmail Tests")
	inner class ExistsByEmailTests {
		@Test
		@DisplayName("should return true when email exists")
		fun `should return true when email exists`() {
			val email = "john@example.com"
			whenever(jpaRepository.existsByEmail(email)).thenReturn(true)

			val result = adapter.existsByEmail(email)

			assertTrue(result)

			verify(jpaRepository, times(1)).existsByEmail(email)
		}

		@Test
		@DisplayName("should return false when email does not exist")
		fun `should return false when email does not exist`() {
			val email = "notfound@example.com"
			whenever(jpaRepository.existsByEmail(email)).thenReturn(false)

			val result = adapter.existsByEmail(email)

			assertFalse(result)

			verify(jpaRepository, times(1)).existsByEmail(email)
		}
	}

	@Nested
	@DisplayName("Save Tests")
	inner class SaveTests {
		@Test
		@DisplayName("should save new user when id is 0")
		fun `should save new user when id is 0`() {
			val newUser = createUser(0L)
			val entity = createUserEntity(0L)
			val savedEntity =
				createUserEntity(1L).apply {
					createdDt = entity.createdDt
					updatedDt = entity.updatedDt
				}
			val savedUser = createUser(1L)

			whenever(userMapper.toEntity(newUser)).thenReturn(entity)
			whenever(jpaRepository.save(entity)).thenReturn(savedEntity)
			whenever(userMapper.toDomain(savedEntity)).thenReturn(savedUser)

			val result = adapter.save(newUser)

			assertNotNull(result)
			assertEquals(savedUser, result)
			assertEquals(1L, result.id)
			assertEquals("John Doe", result.name)

			verify(userMapper, times(1)).toEntity(newUser)
			verify(jpaRepository, times(1)).save(entity)
			verify(userMapper, times(1)).toDomain(savedEntity)
			verify(jpaRepository, never()).findById(any<Long>())
		}

		@Test
		@DisplayName("should update existing user when entity exists")
		fun `should update existing user when entity exists`() {
			val userId = 1L
			val existingUser = createUser(userId)
			val existingEntity = createUserEntity(userId)
			val updatedEntity = existingEntity
			val savedUser = existingUser

			whenever(jpaRepository.findById(userId)).thenReturn(Optional.of(existingEntity))
			whenever(userMapper.updateEntity(existingEntity, existingUser)).thenReturn(updatedEntity)
			whenever(jpaRepository.save(updatedEntity)).thenReturn(updatedEntity)
			whenever(userMapper.toDomain(updatedEntity)).thenReturn(savedUser)

			val result = adapter.save(existingUser)

			assertNotNull(result)
			assertEquals(savedUser, result)
			assertEquals(userId, result.id)

			verify(jpaRepository, times(1)).findById(userId)
			verify(userMapper, times(1)).updateEntity(existingEntity, existingUser)
			verify(jpaRepository, times(1)).save(updatedEntity)
			verify(userMapper, times(1)).toDomain(updatedEntity)
		}

		@Test
		@DisplayName("should throw exception when updating non-existent user")
		fun `should throw exception when updating non-existent user`() {
			val userId = 999L
			val user = createUser(userId)

			whenever(jpaRepository.findById(userId)).thenReturn(Optional.empty())

			val exception =
				assertThrows(UserNotFoundException::class.java) {
					adapter.save(user)
				}

			assertEquals("User Not Found userId = $userId", exception.message)

			verify(jpaRepository, times(1)).findById(userId)
			verify(userMapper, never()).updateEntity(any<UserEntity>(), any<User>())
			verify(jpaRepository, never()).save(any<UserEntity>())
		}
	}

	@Nested
	@DisplayName("DeleteById Tests")
	inner class DeleteByIdTests {
		@Test
		@DisplayName("should perform soft delete")
		fun `should perform soft delete`() {
			val userId = 1L
			doNothing().whenever(jpaRepository).deleteById(userId)

			adapter.deleteById(userId)

			verify(jpaRepository, times(1)).deleteById(userId)
		}
	}

	@Nested
	@DisplayName("HardDeleteById Tests")
	inner class HardDeleteByIdTests {
		@Test
		@DisplayName("should perform hard delete")
		fun `should perform hard delete`() {
			val userId = 1L
			whenever(jpaRepository.hardDeleteById(userId)).thenReturn(1)

			adapter.hardDeleteById(userId)

			verify(jpaRepository, times(1)).hardDeleteById(userId)
		}
	}

	companion object {
		fun createUserEntity(id: Long): UserEntity =
			UserEntity(
				name = "John Doe",
				email = "john@example.com",
				password = "encoded_password",
				role = UserRole.USER
			).apply {
				this.id = id
				this.createdDt = LocalDateTime.now()
				this.updatedDt = LocalDateTime.now()
			}

		fun createUser(id: Long): User =
			User(
				id = id,
				name = "John Doe",
				email = "john@example.com",
				password = "encoded_password",
				role = UserRole.USER,
				createdDt = LocalDateTime.now(),
				updatedDt = LocalDateTime.now()
			)
	}
}
