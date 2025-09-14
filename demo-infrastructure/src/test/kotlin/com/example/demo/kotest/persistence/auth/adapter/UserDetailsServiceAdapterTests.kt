package com.example.demo.kotest.persistence.auth.adapter

import com.example.demo.persistence.auth.adapter.UserAdapter
import com.example.demo.persistence.auth.adapter.UserDetailsServiceAdapter
import com.example.demo.persistence.user.entity.UserEntity
import com.example.demo.persistence.user.mapper.UserMapper
import com.example.demo.persistence.user.repository.UserJpaRepository
import com.example.demo.user.constant.UserRole
import com.example.demo.user.exception.UserNotFoundException
import com.example.demo.user.model.User
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@Tags("kotest-unit-test")
class UserDetailsServiceAdapterTests :
	FunSpec({

		lateinit var userJpaRepository: UserJpaRepository
		lateinit var userMapper: UserMapper
		lateinit var userDetailsServiceAdapter: UserDetailsServiceAdapter

		beforeTest {
			userJpaRepository = mockk()
			userMapper = mockk()
			userDetailsServiceAdapter =
				UserDetailsServiceAdapter(
					userJpaRepository = userJpaRepository,
					userMapper = userMapper
				)
		}

		context("loadUserByUsername") {
			test("should load user successfully when user exists") {
				val userId = 1L
				val userEntity = mockk<UserEntity>()
				val user =
					User(
						id = userId,
						name = "Test User",
						email = "test@example.com",
						password = "encodedPassword",
						role = UserRole.USER
					)

				every { userJpaRepository.findOneById(userId) } returns userEntity
				every { userMapper.toDomain(userEntity) } returns user

				val userDetails = userDetailsServiceAdapter.loadUserByUsername(userId.toString())

				userDetails shouldNotBe null
				userDetails.shouldBeInstanceOf<UserAdapter>()

				val userAdapter = userDetails as UserAdapter
				userAdapter.username shouldBe userId.toString()
				userAdapter.authorities.size shouldBe 1
				userAdapter.authorities.first().authority shouldBe "ROLE_USER"

				verify(exactly = 1) { userJpaRepository.findOneById(userId) }
				verify(exactly = 1) { userMapper.toDomain(userEntity) }
			}

			test("should throw UserNotFoundException when user does not exist") {
				val userId = 999L
				every { userJpaRepository.findOneById(userId) } returns null

				val exception =
					shouldThrow<UserNotFoundException> {
						userDetailsServiceAdapter.loadUserByUsername(userId.toString())
					}

				exception.message shouldBe "User Not Found userId = $userId"

				verify(exactly = 1) { userJpaRepository.findOneById(userId) }
				verify(exactly = 0) { userMapper.toDomain(any<UserEntity>()) }
			}

			test("should handle admin user role correctly") {
				val userId = 2L
				val userEntity = mockk<UserEntity>()
				val adminUser =
					User(
						id = userId,
						name = "Admin User",
						email = "admin@example.com",
						password = "encodedPassword",
						role = UserRole.ADMIN
					)

				every { userJpaRepository.findOneById(userId) } returns userEntity
				every { userMapper.toDomain(userEntity) } returns adminUser

				val userDetails = userDetailsServiceAdapter.loadUserByUsername(userId.toString())

				userDetails shouldNotBe null
				val userAdapter = userDetails as UserAdapter
				userAdapter.authorities.first().authority shouldBe "ROLE_ADMIN"

				verify(exactly = 1) { userJpaRepository.findOneById(userId) }
				verify(exactly = 1) { userMapper.toDomain(userEntity) }
			}

			test("should throw exception for invalid userId format") {
				val invalidUserId = "invalid-id"

				shouldThrow<NumberFormatException> {
					userDetailsServiceAdapter.loadUserByUsername(invalidUserId)
				}

				verify(exactly = 0) { userJpaRepository.findOneById(any<Long>()) }
				verify(exactly = 0) { userMapper.toDomain(any<UserEntity>()) }
			}
		}
	})
