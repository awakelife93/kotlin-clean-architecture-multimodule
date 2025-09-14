package com.example.demo.mockito.persistence.auth.adapter

import com.example.demo.persistence.auth.adapter.UserAdapter
import com.example.demo.persistence.auth.adapter.UserDetailsServiceAdapter
import com.example.demo.persistence.user.entity.UserEntity
import com.example.demo.persistence.user.mapper.UserMapper
import com.example.demo.persistence.user.repository.UserJpaRepository
import com.example.demo.user.constant.UserRole
import com.example.demo.user.exception.UserNotFoundException
import com.example.demo.user.model.User
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@Tag("mockito-unit-test")
@DisplayName("Mockito Unit - User Details Service Adapter Test")
@ExtendWith(MockitoExtension::class)
class UserDetailsServiceAdapterTests {
	@Mock
	private lateinit var userJpaRepository: UserJpaRepository

	@Mock
	private lateinit var userMapper: UserMapper

	@Mock
	private lateinit var userEntity: UserEntity

	@InjectMocks
	private lateinit var userDetailsServiceAdapter: UserDetailsServiceAdapter

	@Nested
	@DisplayName("loadUserByUsername method tests")
	inner class LoadUserByUsernameTests {
		@Test
		@DisplayName("should return UserDetails successfully when user exists")
		fun `should load user successfully when user exists`() {
			val userId = 1L
			val user =
				User(
					id = userId,
					name = "Test User",
					email = "test@example.com",
					password = "encodedPassword",
					role = UserRole.USER
				)

			whenever(userJpaRepository.findOneById(userId)).thenReturn(userEntity)
			whenever(userMapper.toDomain(userEntity)).thenReturn(user)

			val userDetails = userDetailsServiceAdapter.loadUserByUsername(userId.toString())

			assertNotNull(userDetails)
			assertTrue(userDetails is UserAdapter)

			val userAdapter = userDetails as UserAdapter
			assertEquals(userId.toString(), userAdapter.username)
			assertEquals(1, userAdapter.authorities.size)
			assertEquals("ROLE_USER", userAdapter.authorities.first().authority)

			verify(userJpaRepository).findOneById(userId)
			verify(userMapper).toDomain(userEntity)
		}

		@Test
		@DisplayName("should throw UserNotFoundException when user does not exist")
		fun `should throw UserNotFoundException when user does not exist`() {
			val userId = 999L
			whenever(userJpaRepository.findOneById(userId)).thenReturn(null)

			val exception =
				assertThrows<UserNotFoundException> {
					userDetailsServiceAdapter.loadUserByUsername(userId.toString())
				}

			assertEquals("User Not Found userId = $userId", exception.message)

			verify(userJpaRepository).findOneById(userId)
			verify(userMapper, never()).toDomain(any<UserEntity>())
		}

		@Test
		@DisplayName("should handle ADMIN role user correctly")
		fun `should handle admin user role correctly`() {
			val userId = 2L
			val adminUser =
				User(
					id = userId,
					name = "Admin User",
					email = "admin@example.com",
					password = "encodedPassword",
					role = UserRole.ADMIN
				)

			whenever(userJpaRepository.findOneById(userId)).thenReturn(userEntity)
			whenever(userMapper.toDomain(userEntity)).thenReturn(adminUser)

			val userDetails = userDetailsServiceAdapter.loadUserByUsername(userId.toString())

			assertNotNull(userDetails)
			val userAdapter = userDetails as UserAdapter
			assertEquals("ROLE_ADMIN", userAdapter.authorities.first().authority)

			verify(userJpaRepository).findOneById(userId)
			verify(userMapper).toDomain(userEntity)
		}

		@Test
		@DisplayName("should throw NumberFormatException for invalid userId format")
		fun `should throw exception for invalid userId format`() {
			val invalidUserId = "invalid-id"

			assertThrows<NumberFormatException> {
				userDetailsServiceAdapter.loadUserByUsername(invalidUserId)
			}

			verify(userJpaRepository, never()).findOneById(any<Long>())
			verify(userMapper, never()).toDomain(any<UserEntity>())
		}
	}
}
