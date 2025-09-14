package com.example.demo.mockito.persistence.user.repository

import com.example.demo.persistence.config.JpaAuditConfig
import com.example.demo.persistence.config.QueryDslConfig
import com.example.demo.persistence.user.entity.UserEntity
import com.example.demo.persistence.user.repository.UserJpaRepository
import com.example.demo.user.constant.UserRole
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@Tag("mockito-integration-test")
@DisplayName("Mockito Integration - User JPA Repository Test")
@DataJpaTest
@Import(value = [QueryDslConfig::class, JpaAuditConfig::class])
class UserJpaRepositoryTests
	@Autowired
	constructor(
		private val userJpaRepository: UserJpaRepository
	) {
		@Nested
		@DisplayName("Basic CRUD Operations")
		inner class BasicCrudTests {
			@Test
			@DisplayName("should save and retrieve a user by ID")
			fun saveAndFindById() {
				val user =
					UserEntity(
						name = "John Doe",
						email = "john@example.com",
						password = "password123",
						role = UserRole.USER
					)

				val savedUser = userJpaRepository.save(user)
				val foundUser = userJpaRepository.findById(savedUser.id)

				assertTrue(foundUser.isPresent)
				assertEquals("John Doe", foundUser.get().name)
				assertEquals("john@example.com", foundUser.get().email)
				assertEquals("password123", foundUser.get().password)
				assertEquals(UserRole.USER, foundUser.get().role)
			}

			@Test
			@DisplayName("should update existing user")
			fun updateUser() {
				val user =
					UserEntity(
						name = "Original Name",
						email = "original@example.com",
						password = "password123",
						role = UserRole.USER
					)
				val savedUser = userJpaRepository.save(user)

				val userToUpdate = userJpaRepository.findById(savedUser.id).get()
				userToUpdate.name = "Updated Name"
				userToUpdate.role = UserRole.ADMIN
				userJpaRepository.save(userToUpdate)

				val updatedUser = userJpaRepository.findById(savedUser.id).get()
				assertEquals("Updated Name", updatedUser.name)
				assertEquals("original@example.com", updatedUser.email)
				assertEquals(UserRole.ADMIN, updatedUser.role)
			}

			@Test
			@DisplayName("should soft delete user by ID")
			fun softDeleteById() {
				val user =
					UserEntity(
						name = "To Delete",
						email = "delete@example.com",
						password = "password123",
						role = UserRole.USER
					)
				val savedUser = userJpaRepository.save(user)

				userJpaRepository.deleteById(savedUser.id)

				val foundUser = userJpaRepository.findById(savedUser.id)
				assertFalse(foundUser.isPresent)
			}

			@Test
			@DisplayName("should check if user exists by ID")
			fun existsById() {
				val user =
					UserEntity(
						name = "Exists Test",
						email = "exists@example.com",
						password = "password123",
						role = UserRole.USER
					)
				val savedUser = userJpaRepository.save(user)

				assertTrue(userJpaRepository.existsById(savedUser.id))
				assertFalse(userJpaRepository.existsById(99999L))
			}

			@Test
			@DisplayName("should count total users")
			fun countUsers() {
				val users =
					(1..5).map { i ->
						UserEntity(
							name = "User $i",
							email = "user$i@example.com",
							password = "password$i",
							role = UserRole.USER
						)
					}
				userJpaRepository.saveAll(users)

				val count = userJpaRepository.count()

				assertEquals(5, count)
			}
		}

		@Nested
		@DisplayName("findOneById Tests")
		inner class FindOneByIdTests {
			@Test
			@DisplayName("should find user by id")
			fun findOneById() {
				val user =
					UserEntity(
						name = "Test User",
						email = "test@example.com",
						password = "password123",
						role = UserRole.USER
					)
				val savedUser = userJpaRepository.save(user)

				val foundUser = userJpaRepository.findOneById(savedUser.id)

				assertNotNull(foundUser)
				assertEquals("Test User", foundUser?.name)
				assertEquals("test@example.com", foundUser?.email)
			}

			@Test
			@DisplayName("should return null for non-existent user")
			fun findOneByIdNotFound() {
				val foundUser = userJpaRepository.findOneById(99999L)

				assertNull(foundUser)
			}
		}

		@Nested
		@DisplayName("findOneByEmail Tests")
		inner class FindOneByEmailTests {
			@Test
			@DisplayName("should find user by email")
			fun findOneByEmail() {
				val user =
					UserEntity(
						name = "Email Test",
						email = "unique@example.com",
						password = "password123",
						role = UserRole.USER
					)
				userJpaRepository.save(user)

				val foundUser = userJpaRepository.findOneByEmail("unique@example.com")

				assertNotNull(foundUser)
				assertEquals("Email Test", foundUser?.name)
				assertEquals("unique@example.com", foundUser?.email)
			}

			@Test
			@DisplayName("should return null for non-existent email")
			fun findOneByEmailNotFound() {
				val foundUser = userJpaRepository.findOneByEmail("notfound@example.com")

				assertNull(foundUser)
			}

			@Test
			@DisplayName("should be case sensitive for email")
			fun findOneByEmailCaseSensitive() {
				val user =
					UserEntity(
						name = "Case Test",
						email = "test@example.com",
						password = "password123",
						role = UserRole.USER
					)
				userJpaRepository.save(user)

				val foundLower = userJpaRepository.findOneByEmail("test@example.com")
				val foundUpper = userJpaRepository.findOneByEmail("TEST@EXAMPLE.COM")

				assertNotNull(foundLower)
				assertNull(foundUpper)
			}
		}

		@Nested
		@DisplayName("existsByEmail Tests")
		inner class ExistsByEmailTests {
			@Test
			@DisplayName("should check if email exists")
			fun existsByEmail() {
				val user =
					UserEntity(
						name = "Exists Email Test",
						email = "exists@example.com",
						password = "password123",
						role = UserRole.USER
					)
				userJpaRepository.save(user)

				assertTrue(userJpaRepository.existsByEmail("exists@example.com"))
				assertFalse(userJpaRepository.existsByEmail("notexists@example.com"))
			}

			@Test
			@DisplayName("should not find soft deleted user's email")
			fun existsByEmailAfterSoftDelete() {
				val user =
					UserEntity(
						name = "Soft Delete Email Test",
						email = "softdelete@example.com",
						password = "password123",
						role = UserRole.USER
					)
				val savedUser = userJpaRepository.save(user)

				userJpaRepository.deleteById(savedUser.id)

				assertFalse(userJpaRepository.existsByEmail("softdelete@example.com"))
			}
		}

		@Nested
		@DisplayName("hardDeleteById Tests")
		inner class HardDeleteByIdTests {
			@Test
			@DisplayName("should permanently delete user")
			fun hardDeleteById() {
				val user =
					UserEntity(
						name = "Hard Delete Test",
						email = "harddelete@example.com",
						password = "password123",
						role = UserRole.USER
					)
				val savedUser = userJpaRepository.save(user)
				val userId = savedUser.id

				val deletedCount = userJpaRepository.hardDeleteById(userId)
				userJpaRepository.flush()

				assertEquals(1, deletedCount)
				val remainingCount = userJpaRepository.count()
				assertEquals(0, remainingCount)
			}

			@Test
			@DisplayName("should return 0 when user not found")
			fun hardDeleteByIdNotFound() {
				val deletedCount = userJpaRepository.hardDeleteById(99999L)

				assertEquals(0, deletedCount)
			}
		}

		@Nested
		@DisplayName("Pagination and Sorting Tests")
		inner class PaginationAndSortingTests {
			@Test
			@DisplayName("should support pagination")
			fun pagination() {
				val users =
					(1..15).map { i ->
						UserEntity(
							name = "User $i",
							email = "user$i@example.com",
							password = "password$i",
							role = UserRole.USER
						)
					}
				userJpaRepository.saveAll(users)

				val firstPage = userJpaRepository.findAll(PageRequest.of(0, 5))
				val secondPage = userJpaRepository.findAll(PageRequest.of(1, 5))
				val thirdPage = userJpaRepository.findAll(PageRequest.of(2, 5))

				assertEquals(5, firstPage.content.size)
				assertEquals(5, secondPage.content.size)
				assertEquals(5, thirdPage.content.size)
				assertEquals(15, firstPage.totalElements)
				assertEquals(3, firstPage.totalPages)
			}

			@Test
			@DisplayName("should support sorting by name")
			fun sortingByName() {
				val users =
					listOf(
						UserEntity("Charlie", "charlie@example.com", "password", UserRole.USER),
						UserEntity("Alice", "alice@example.com", "password", UserRole.USER),
						UserEntity("Bob", "bob@example.com", "password", UserRole.USER)
					)
				userJpaRepository.saveAll(users)

				val sortedAsc = userJpaRepository.findAll(Sort.by(Sort.Direction.ASC, "name"))
				val sortedDesc = userJpaRepository.findAll(Sort.by(Sort.Direction.DESC, "name"))

				assertEquals("Alice", sortedAsc[0].name)
				assertEquals("Bob", sortedAsc[1].name)
				assertEquals("Charlie", sortedAsc[2].name)

				assertEquals("Charlie", sortedDesc[0].name)
				assertEquals("Bob", sortedDesc[1].name)
				assertEquals("Alice", sortedDesc[2].name)
			}

			@Test
			@DisplayName("should support sorting by multiple fields")
			fun sortingByMultipleFields() {
				val users =
					listOf(
						UserEntity("Alice", "alice2@example.com", "password", UserRole.ADMIN),
						UserEntity("Alice", "alice1@example.com", "password", UserRole.USER),
						UserEntity("Bob", "bob@example.com", "password", UserRole.USER)
					)
				userJpaRepository.saveAll(users)

				val sorted =
					userJpaRepository.findAll(
						Sort.by(
							Sort.Order.asc("name"),
							Sort.Order.asc("email")
						)
					)

				assertEquals("Alice", sorted[0].name)
				assertEquals("alice1@example.com", sorted[0].email)
				assertEquals("Alice", sorted[1].name)
				assertEquals("alice2@example.com", sorted[1].email)
				assertEquals("Bob", sorted[2].name)
			}
		}

		@Nested
		@DisplayName("Soft Delete Tests")
		inner class SoftDeleteTests {
			@Test
			@DisplayName("should handle soft delete")
			fun softDelete() {
				val user =
					UserEntity(
						name = "Soft Delete Test",
						email = "soft@example.com",
						password = "password123",
						role = UserRole.USER
					)
				val savedUser = userJpaRepository.save(user)

				val userToDelete = userJpaRepository.findById(savedUser.id).get()
				userToDelete.deletedDt = java.time.LocalDateTime.now()
				userJpaRepository.save(userToDelete)

				val deletedUser = userJpaRepository.findById(savedUser.id).get()
				assertNotNull(deletedUser.deletedDt)
				assertTrue(deletedUser.deletedDt != null)
			}

			@Test
			@DisplayName("should filter out soft deleted users automatically")
			fun filterSoftDeletedUsers() {
				val activeUser = UserEntity("Active", "active@example.com", "password", UserRole.USER)
				val deletedUser =
					UserEntity("Deleted", "deleted@example.com", "password", UserRole.USER).apply {
						deletedDt = java.time.LocalDateTime.now()
					}

				userJpaRepository.saveAll(listOf(activeUser, deletedUser))

				val allUsers = userJpaRepository.findAll()

				assertEquals(1, allUsers.size)
				assertTrue(allUsers.all { it.deletedDt == null })
			}

			@Test
			@DisplayName("should not find soft deleted user by custom queries")
			fun notFindSoftDeletedUserByCustomQueries() {
				val user =
					UserEntity(
						name = "To Delete",
						email = "todelete@example.com",
						password = "password123",
						role = UserRole.USER
					)
				val savedUser = userJpaRepository.save(user)

				userJpaRepository.deleteById(savedUser.id)

				val byId = userJpaRepository.findOneById(savedUser.id)
				val byEmail = userJpaRepository.findOneByEmail("todelete@example.com")
				val exists = userJpaRepository.existsByEmail("todelete@example.com")

				assertNull(byId)
				assertNull(byEmail)
				assertFalse(exists)
			}
		}

		@Nested
		@DisplayName("Role-based Tests")
		inner class RoleBasedTests {
			@Test
			@DisplayName("should find users by role")
			fun findUsersByRole() {
				val users =
					listOf(
						UserEntity("Admin 1", "admin1@example.com", "password", UserRole.ADMIN),
						UserEntity("User 1", "user1@example.com", "password", UserRole.USER),
						UserEntity("Admin 2", "admin2@example.com", "password", UserRole.ADMIN),
						UserEntity("User 2", "user2@example.com", "password", UserRole.USER),
						UserEntity("User 3", "user3@example.com", "password", UserRole.USER)
					)
				userJpaRepository.saveAll(users)

				val allUsers = userJpaRepository.findAll()
				val adminUsers = allUsers.filter { it.role == UserRole.ADMIN }
				val normalUsers = allUsers.filter { it.role == UserRole.USER }

				assertEquals(2, adminUsers.size)
				assertEquals(3, normalUsers.size)
				assertTrue(adminUsers.all { it.role == UserRole.ADMIN })
				assertTrue(normalUsers.all { it.role == UserRole.USER })
			}
		}

		@Nested
		@DisplayName("Unique Constraint Tests")
		inner class UniqueConstraintTests {
			@Test
			@DisplayName("should enforce unique email constraint")
			fun uniqueEmailConstraint() {
				val user1 =
					UserEntity(
						name = "User 1",
						email = "unique@example.com",
						password = "password1",
						role = UserRole.USER
					)
				userJpaRepository.save(user1)

				val user2 =
					UserEntity(
						name = "User 2",
						email = "unique@example.com",
						password = "password2",
						role = UserRole.USER
					)

				assertThrows<DataIntegrityViolationException> {
					userJpaRepository.save(user2)
					userJpaRepository.flush()
				}
			}

			@Test
			@DisplayName("should allow same email after hard delete")
			fun allowSameEmailAfterHardDelete() {
				val user1 =
					UserEntity(
						name = "User 1",
						email = "reuse@example.com",
						password = "password1",
						role = UserRole.USER
					)
				val savedUser1 = userJpaRepository.save(user1)

				userJpaRepository.hardDeleteById(savedUser1.id)

				val user2 =
					UserEntity(
						name = "User 2",
						email = "reuse@example.com",
						password = "password2",
						role = UserRole.USER
					)
				val savedUser2 = userJpaRepository.save(user2)

				assertNotNull(savedUser2)
				assertEquals("reuse@example.com", savedUser2.email)
			}

			@Test
			@DisplayName("should not allow same email after soft delete")
			fun notAllowSameEmailAfterSoftDelete() {
				val user1 =
					UserEntity(
						name = "User 1",
						email = "softdel@example.com",
						password = "password1",
						role = UserRole.USER
					)
				val savedUser1 = userJpaRepository.save(user1)

				userJpaRepository.deleteById(savedUser1.id)

				val user2 =
					UserEntity(
						name = "User 2",
						email = "softdel@example.com",
						password = "password2",
						role = UserRole.USER
					)

				assertThrows<DataIntegrityViolationException> {
					userJpaRepository.save(user2)
					userJpaRepository.flush()
				}
			}
		}

		@Nested
		@DisplayName("Audit Fields Tests")
		inner class AuditFieldsTests {
			@Test
			@DisplayName("should automatically set audit fields on save")
			fun auditFieldsOnSave() {
				val user =
					UserEntity(
						name = "Audit Test",
						email = "audit@example.com",
						password = "password123",
						role = UserRole.USER
					)

				val savedUser = userJpaRepository.save(user)

				assertNotNull(savedUser.createdDt)
				assertNotNull(savedUser.updatedDt)
				assertNull(savedUser.deletedDt)
				assertEquals(savedUser.createdDt, savedUser.updatedDt)
			}

			@Test
			@DisplayName("should update updatedDt on modification")
			fun auditFieldsOnUpdate() {
				val user =
					UserEntity(
						name = "Update Audit Test",
						email = "updateaudit@example.com",
						password = "password123",
						role = UserRole.USER
					)
				val savedUser = userJpaRepository.save(user)
				userJpaRepository.flush()

				val userToUpdate = userJpaRepository.findById(savedUser.id).get()
				userToUpdate.name = "Modified Name"
				val updatedUser = userJpaRepository.save(userToUpdate)
				userJpaRepository.flush()

				assertEquals(savedUser.createdDt, updatedUser.createdDt)
				assertNotNull(updatedUser.updatedDt)
			}

			@Test
			@DisplayName("should set deletedDt on soft delete")
			fun auditFieldsOnSoftDelete() {
				val user =
					UserEntity(
						name = "Delete Audit Test",
						email = "deleteaudit@example.com",
						password = "password123",
						role = UserRole.USER
					)
				val savedUser = userJpaRepository.save(user)

				savedUser.deletedDt = java.time.LocalDateTime.now()
				val deletedUser = userJpaRepository.save(savedUser)

				assertNotNull(deletedUser.deletedDt)
				assertTrue(deletedUser.deletedDt!!.isAfter(deletedUser.createdDt))
			}
		}
	}
