package com.example.demo.kotest.persistence.user.adapter

import com.example.demo.persistence.user.adapter.UserRepositoryAdapter
import com.example.demo.persistence.user.entity.UserEntity
import com.example.demo.persistence.user.mapper.UserMapper
import com.example.demo.persistence.user.repository.UserJpaRepository
import com.example.demo.user.constant.UserRole
import com.example.demo.user.exception.UserNotFoundException
import com.example.demo.user.model.User
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime
import java.util.Optional

@ActiveProfiles("test")
@Tags("kotest-unit-test")
class UserRepositoryAdapterTest :
	FunSpec({

		lateinit var jpaRepository: UserJpaRepository
		lateinit var userMapper: UserMapper
		lateinit var adapter: UserRepositoryAdapter

		beforeTest {
			jpaRepository = mockk()
			userMapper = mockk()
			adapter = UserRepositoryAdapter(jpaRepository, userMapper)
		}

		context("findOneById") {
			test("should return mapped user when user exists") {
				val userId = 1L
				val entity = createUserEntity(userId)
				val domain = createUser(userId)

				every { jpaRepository.findOneById(userId) } returns entity
				every { userMapper.toDomain(entity) } returns domain

				val result = adapter.findOneById(userId)

				result.shouldNotBeNull()
				result.id shouldBe userId
				result.name shouldBe "John Doe"
				result.email shouldBe "john@example.com"

				verify(exactly = 1) { jpaRepository.findOneById(userId) }
				verify(exactly = 1) { userMapper.toDomain(entity) }
			}

			test("should return null when user does not exist") {
				val userId = 999L
				every { jpaRepository.findOneById(userId) } returns null

				val result = adapter.findOneById(userId)

				result.shouldBeNull()

				verify(exactly = 1) { jpaRepository.findOneById(userId) }
				verify(exactly = 0) { userMapper.toDomain(any<UserEntity>()) }
			}
		}

		context("findOneByEmail") {
			test("should return mapped user when user exists") {
				val email = "john@example.com"
				val entity = createUserEntity(1L)
				val domain = createUser(1L)

				every { jpaRepository.findOneByEmail(email) } returns entity
				every { userMapper.toDomain(entity) } returns domain

				val result = adapter.findOneByEmail(email)

				result.shouldNotBeNull()
				result.email shouldBe email

				verify(exactly = 1) { jpaRepository.findOneByEmail(email) }
				verify(exactly = 1) { userMapper.toDomain(entity) }
			}

			test("should return null when user does not exist") {
				val email = "notfound@example.com"
				every { jpaRepository.findOneByEmail(email) } returns null

				val result = adapter.findOneByEmail(email)

				result.shouldBeNull()

				verify(exactly = 1) { jpaRepository.findOneByEmail(email) }
				verify(exactly = 0) { userMapper.toDomain(any<UserEntity>()) }
			}
		}

		context("findAll") {
			test("should return page of mapped users") {
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

				every { jpaRepository.findAll(pageable) } returns entityPage
				entities.forEachIndexed { index, entity ->
					every { userMapper.toDomain(entity) } returns users[index]
				}

				val result = adapter.findAll(pageable)

				result.content shouldHaveSize 2
				result.content shouldBe users
				result.totalElements shouldBe 2

				verify(exactly = 1) { jpaRepository.findAll(pageable) }
				verify(exactly = 2) { userMapper.toDomain(any<UserEntity>()) }
			}

			test("should return empty page when no users") {
				val pageable = PageRequest.of(0, 10)
				val emptyPage = PageImpl<UserEntity>(emptyList(), pageable, 0)

				every { jpaRepository.findAll(pageable) } returns emptyPage

				val result = adapter.findAll(pageable)

				result.content shouldHaveSize 0
				result.totalElements shouldBe 0
				verify(exactly = 1) { jpaRepository.findAll(pageable) }
				verify(exactly = 0) { userMapper.toDomain(any<UserEntity>()) }
			}
		}

		context("existsByEmail") {
			test("should return true when email exists") {
				val email = "john@example.com"
				every { jpaRepository.existsByEmail(email) } returns true

				val result = adapter.existsByEmail(email)

				result shouldBe true

				verify(exactly = 1) { jpaRepository.existsByEmail(email) }
			}

			test("should return false when email does not exist") {
				val email = "notfound@example.com"
				every { jpaRepository.existsByEmail(email) } returns false

				val result = adapter.existsByEmail(email)

				result shouldBe false

				verify(exactly = 1) { jpaRepository.existsByEmail(email) }
			}
		}

		context("save") {
			test("should save new user when id is 0") {
				val newUser = createUser(0L)
				val entity = createUserEntity(0L)
				val savedEntity =
					createUserEntity(1L).apply {
						createdDt = entity.createdDt
						updatedDt = entity.updatedDt
					}
				val savedUser = createUser(1L)

				every { userMapper.toEntity(newUser) } returns entity
				every { jpaRepository.save(entity) } returns savedEntity
				every { userMapper.toDomain(savedEntity) } returns savedUser

				val result = adapter.save(newUser)

				result shouldBe savedUser
				result.id shouldBe 1L
				result.name shouldBe "John Doe"

				verify(exactly = 1) { userMapper.toEntity(newUser) }
				verify(exactly = 1) { jpaRepository.save(entity) }
				verify(exactly = 1) { userMapper.toDomain(savedEntity) }
				verify(exactly = 0) { jpaRepository.findById(any<Long>()) }
			}

			test("should update existing user when id is not 0 and entity exists") {
				val userId = 1L
				val existingUser = createUser(userId)
				val existingEntity = createUserEntity(userId)
				val updatedEntity = existingEntity
				val savedUser = existingUser

				every { jpaRepository.findById(userId) } returns Optional.of(existingEntity)
				every { userMapper.updateEntity(existingEntity, existingUser) } returns updatedEntity
				every { jpaRepository.save(updatedEntity) } returns updatedEntity
				every { userMapper.toDomain(updatedEntity) } returns savedUser

				val result = adapter.save(existingUser)

				result shouldBe savedUser
				result.id shouldBe userId

				verify(exactly = 1) { jpaRepository.findById(userId) }
				verify(exactly = 1) { userMapper.updateEntity(existingEntity, existingUser) }
				verify(exactly = 1) { jpaRepository.save(updatedEntity) }
				verify(exactly = 1) { userMapper.toDomain(updatedEntity) }
			}

			test("should throw exception when updating non-existent user") {
				val userId = 999L
				val user = createUser(userId)

				every { jpaRepository.findById(userId) } returns Optional.empty()

				try {
					adapter.save(user)
					throw AssertionError("Expected User not found exception")
				} catch (e: UserNotFoundException) {
					e.message shouldBe "User Not Found userId = $userId"
				}

				verify(exactly = 1) { jpaRepository.findById(userId) }
				verify(exactly = 0) { userMapper.updateEntity(any<UserEntity>(), any<User>()) }
				verify(exactly = 0) { jpaRepository.save(any<UserEntity>()) }
			}
		}

		context("deleteById") {
			test("should perform soft delete") {
				val userId = 1L
				every { jpaRepository.deleteById(userId) } just Runs

				adapter.deleteById(userId)

				verify(exactly = 1) { jpaRepository.deleteById(userId) }
			}
		}

		context("hardDeleteById") {
			test("should perform hard delete") {
				val userId = 1L
				every { jpaRepository.hardDeleteById(userId) } returns 1

				adapter.hardDeleteById(userId)

				verify(exactly = 1) { jpaRepository.hardDeleteById(userId) }
			}
		}
	}) {
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
