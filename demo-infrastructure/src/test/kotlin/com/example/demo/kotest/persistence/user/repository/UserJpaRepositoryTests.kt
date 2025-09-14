package com.example.demo.kotest.persistence.user.repository

import com.example.demo.persistence.config.JpaAuditConfig
import com.example.demo.persistence.config.QueryDslConfig
import com.example.demo.persistence.user.entity.UserEntity
import com.example.demo.persistence.user.repository.UserJpaRepository
import com.example.demo.user.constant.UserRole
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@Tags("kotest-integration-test")
@DataJpaTest
@Import(value = [QueryDslConfig::class, JpaAuditConfig::class])
class UserJpaRepositoryTests
	@Autowired
	constructor(
		private val userJpaRepository: UserJpaRepository
	) : DescribeSpec({

			describe("UserJpaRepository Basic CRUD") {

				describe("save and findById") {
					it("should save and retrieve a user") {
						val user =
							UserEntity(
								name = "John Doe",
								email = "john@example.com",
								password = "password123",
								role = UserRole.USER
							)

						val savedUser = userJpaRepository.save(user)
						val foundUser = userJpaRepository.findById(savedUser.id)

						foundUser.isPresent shouldBe true
						foundUser.get().name shouldBe "John Doe"
						foundUser.get().email shouldBe "john@example.com"
						foundUser.get().password shouldBe "password123"
						foundUser.get().role shouldBe UserRole.USER
					}
				}

				describe("update") {
					it("should update existing user") {
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
						val updatedUser = userJpaRepository.save(userToUpdate)

						val foundUser = userJpaRepository.findById(updatedUser.id).get()
						foundUser.name shouldBe "Updated Name"
						foundUser.email shouldBe "original@example.com"
						foundUser.role shouldBe UserRole.ADMIN
					}
				}

				describe("delete") {
					it("should soft delete user") {
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
						foundUser.isPresent shouldBe false
					}
				}

				describe("existsById") {
					it("should check if user exists") {
						val user =
							UserEntity(
								name = "Exists Test",
								email = "exists@example.com",
								password = "password123",
								role = UserRole.USER
							)
						val savedUser = userJpaRepository.save(user)

						userJpaRepository.existsById(savedUser.id) shouldBe true
						userJpaRepository.existsById(99999L) shouldBe false
					}
				}

				describe("count") {
					it("should count total users") {
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

						count shouldBe 5
					}
				}
			}

			describe("UserJpaRepository Custom Queries") {

				describe("findOneById") {
					it("should find user by id") {
						val user =
							UserEntity(
								name = "Test User",
								email = "test@example.com",
								password = "password123",
								role = UserRole.USER
							)
						val savedUser = userJpaRepository.save(user)

						val foundUser = userJpaRepository.findOneById(savedUser.id)

						foundUser.shouldNotBeNull()
						foundUser.name shouldBe "Test User"
						foundUser.email shouldBe "test@example.com"
					}

					it("should return null for non-existent user") {
						val foundUser = userJpaRepository.findOneById(99999L)

						foundUser.shouldBeNull()
					}
				}

				describe("findOneByEmail") {
					it("should find user by email") {
						val user =
							UserEntity(
								name = "Email Test",
								email = "unique@example.com",
								password = "password123",
								role = UserRole.USER
							)
						userJpaRepository.save(user)

						val foundUser = userJpaRepository.findOneByEmail("unique@example.com")

						foundUser.shouldNotBeNull()
						foundUser.name shouldBe "Email Test"
						foundUser.email shouldBe "unique@example.com"
					}

					it("should return null for non-existent email") {
						val foundUser = userJpaRepository.findOneByEmail("notfound@example.com")

						foundUser.shouldBeNull()
					}

					it("should be case sensitive for email") {
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

						foundLower.shouldNotBeNull()
						foundUpper.shouldBeNull()
					}
				}

				describe("existsByEmail") {
					it("should check if email exists") {
						val user =
							UserEntity(
								name = "Exists Email Test",
								email = "exists@example.com",
								password = "password123",
								role = UserRole.USER
							)
						userJpaRepository.save(user)

						userJpaRepository.existsByEmail("exists@example.com") shouldBe true
						userJpaRepository.existsByEmail("notexists@example.com") shouldBe false
					}

					it("should not find soft deleted user's email") {
						val user =
							UserEntity(
								name = "Soft Delete Email Test",
								email = "softdelete@example.com",
								password = "password123",
								role = UserRole.USER
							)
						val savedUser = userJpaRepository.save(user)

						userJpaRepository.deleteById(savedUser.id)

						userJpaRepository.existsByEmail("softdelete@example.com") shouldBe false
					}
				}

				describe("hardDeleteById") {
					it("should permanently delete user") {
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

						deletedCount shouldBe 1
						val remainingCount = userJpaRepository.count()
						remainingCount shouldBe 0
					}

					it("should return 0 when user not found") {
						val deletedCount = userJpaRepository.hardDeleteById(99999L)

						deletedCount shouldBe 0
					}
				}
			}

			describe("UserJpaRepository Pagination and Sorting") {

				it("should support pagination") {
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

					firstPage.content shouldHaveSize 5
					secondPage.content shouldHaveSize 5
					thirdPage.content shouldHaveSize 5
					firstPage.totalElements shouldBe 15
					firstPage.totalPages shouldBe 3
				}

				it("should support sorting by name") {
					val users =
						listOf(
							UserEntity("Charlie", "charlie@example.com", "password", UserRole.USER),
							UserEntity("Alice", "alice@example.com", "password", UserRole.USER),
							UserEntity("Bob", "bob@example.com", "password", UserRole.USER)
						)
					userJpaRepository.saveAll(users)

					val sortedAsc = userJpaRepository.findAll(Sort.by(Sort.Direction.ASC, "name"))
					val sortedDesc = userJpaRepository.findAll(Sort.by(Sort.Direction.DESC, "name"))

					sortedAsc[0].name shouldBe "Alice"
					sortedAsc[1].name shouldBe "Bob"
					sortedAsc[2].name shouldBe "Charlie"

					sortedDesc[0].name shouldBe "Charlie"
					sortedDesc[1].name shouldBe "Bob"
					sortedDesc[2].name shouldBe "Alice"
				}

				it("should support sorting by multiple fields") {
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

					sorted[0].name shouldBe "Alice"
					sorted[0].email shouldBe "alice1@example.com"
					sorted[1].name shouldBe "Alice"
					sorted[1].email shouldBe "alice2@example.com"
					sorted[2].name shouldBe "Bob"
				}
			}

			describe("UserJpaRepository Soft Delete") {

				it("should handle soft delete") {
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
					val deletedUser = userJpaRepository.save(userToDelete)

					val foundUser = userJpaRepository.findById(deletedUser.id).get()
					foundUser.deletedDt shouldNotBe null
					(foundUser.deletedDt != null) shouldBe true
				}

				it("should filter out soft deleted users automatically") {
					val activeUser = UserEntity("Active", "active@example.com", "password", UserRole.USER)
					val deletedUser =
						UserEntity("Deleted", "deleted@example.com", "password", UserRole.USER).apply {
							deletedDt = java.time.LocalDateTime.now()
						}

					userJpaRepository.saveAll(listOf(activeUser, deletedUser))

					val allUsers = userJpaRepository.findAll()

					allUsers shouldHaveSize 1
					allUsers.all { it.deletedDt == null } shouldBe true
				}

				it("should not find soft deleted user by custom queries") {
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

					byId.shouldBeNull()
					byEmail.shouldBeNull()
					exists shouldBe false
				}
			}

			describe("UserJpaRepository Role-based Queries") {

				it("should find users by role") {
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

					adminUsers shouldHaveSize 2
					normalUsers shouldHaveSize 3
					adminUsers.all { it.role == UserRole.ADMIN } shouldBe true
					normalUsers.all { it.role == UserRole.USER } shouldBe true
				}
			}

			describe("UserJpaRepository Unique Constraints") {

				it("should enforce unique email constraint") {
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

					try {
						userJpaRepository.save(user2)
						userJpaRepository.flush()
						throw AssertionError("Expected DataIntegrityViolationException")
					} catch (exception: Exception) {
						exception.javaClass.simpleName shouldBe "DataIntegrityViolationException"
					}
				}
			}
		})
