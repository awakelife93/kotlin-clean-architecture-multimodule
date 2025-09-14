package com.example.demo.kotest.persistence.user.mapper

import com.example.demo.persistence.user.entity.UserEntity
import com.example.demo.persistence.user.mapper.UserMapper
import com.example.demo.user.constant.UserRole
import com.example.demo.user.model.User
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime

@ActiveProfiles("test")
@Tags("kotest-unit-test")
class UserMapperTests :
	DescribeSpec({

		val mapper = UserMapper()

		describe("UserMapper") {

			describe("toDomain method") {
				it("should convert UserEntity to User domain model correctly") {
					val now = LocalDateTime.now()
					val entity =
						UserEntity(
							name = "John Doe",
							email = "john@example.com",
							password = "encoded_password",
							role = UserRole.USER
						).apply {
							id = 100L
							createdDt = now.minusDays(1)
							updatedDt = now
							deletedDt = null
						}

					val domain = mapper.toDomain(entity)

					domain.id shouldBe entity.id
					domain.name shouldBe entity.name
					domain.email shouldBe entity.email
					domain.password shouldBe entity.password
					domain.role shouldBe entity.role
					domain.createdDt shouldBe entity.createdDt
					domain.updatedDt shouldBe entity.updatedDt
					domain.deletedDt shouldBe entity.deletedDt
				}

				it("should include deletedDt when converting deleted UserEntity") {
					val deletedTime = LocalDateTime.now()
					val entity =
						UserEntity(
							name = "Deleted User",
							email = "deleted@example.com",
							password = "password",
							role = UserRole.ADMIN
						).apply {
							id = 200L
							createdDt = LocalDateTime.now().minusDays(2)
							updatedDt = LocalDateTime.now().minusDays(1)
							deletedDt = deletedTime
						}

					val domain = mapper.toDomain(entity)

					domain.deletedDt shouldBe deletedTime
					(domain.deletedDt != null) shouldBe true
				}

				it("should handle UserEntity with different roles") {
					val adminEntity =
						UserEntity(
							name = "Admin User",
							email = "admin@example.com",
							password = "admin_password",
							role = UserRole.ADMIN
						).apply {
							id = 300L
						}

					val adminDomain = mapper.toDomain(adminEntity)

					adminDomain.role shouldBe UserRole.ADMIN
				}
			}

			describe("toEntity method") {
				it("should not set ID when converting new User domain model") {
					val domain =
						User(
							id = 0L,
							name = "New User",
							email = "new@example.com",
							password = "new_password",
							role = UserRole.USER,
							createdDt = LocalDateTime.now(),
							updatedDt = LocalDateTime.now()
						)

					val entity = mapper.toEntity(domain)

					entity.id shouldBe 0L
					entity.name shouldBe domain.name
					entity.email shouldBe domain.email
					entity.password shouldBe domain.password
					entity.role shouldBe domain.role
					entity.createdDt shouldBe domain.createdDt
					entity.updatedDt shouldBe domain.updatedDt
					entity.deletedDt shouldBe domain.deletedDt
				}

				it("should set ID when converting existing User domain model") {
					val existingId = 400L
					val now = LocalDateTime.now()
					val domain =
						User(
							id = existingId,
							name = "Existing User",
							email = "existing@example.com",
							password = "existing_password",
							role = UserRole.USER,
							createdDt = now.minusDays(5),
							updatedDt = now
						)

					val entity = mapper.toEntity(domain)

					entity.id shouldBe existingId
					entity.name shouldBe domain.name
					entity.email shouldBe domain.email
					entity.password shouldBe domain.password
					entity.role shouldBe domain.role
					entity.createdDt shouldBe domain.createdDt
					entity.updatedDt shouldBe domain.updatedDt
				}

				it("should set deletedDt when converting deleted User domain model") {
					val deletedTime = LocalDateTime.now().minusHours(2)
					val domain =
						User(
							id = 500L,
							name = "Deleted User",
							email = "deleted@example.com",
							password = "deleted_password",
							role = UserRole.USER,
							deletedDt = deletedTime
						)

					val entity = mapper.toEntity(domain)

					entity.deletedDt shouldBe deletedTime
				}

				it("should handle User with ADMIN role") {
					val domain =
						User(
							id = 600L,
							name = "Admin",
							email = "admin@example.com",
							password = "admin_password",
							role = UserRole.ADMIN
						)

					val entity = mapper.toEntity(domain)

					entity.role shouldBe UserRole.ADMIN
				}
			}

			describe("updateEntity method") {
				it("should update existing UserEntity with domain model data") {
					val originalCreatedDt = LocalDateTime.now().minusDays(10)
					val originalUpdatedDt = LocalDateTime.now().minusDays(5)
					val entity =
						UserEntity(
							name = "Original Name",
							email = "original@example.com",
							password = "original_password",
							role = UserRole.USER
						).apply {
							id = 700L
							createdDt = originalCreatedDt
							updatedDt = originalUpdatedDt
							deletedDt = null
						}

					val newUpdatedDt = LocalDateTime.now()
					val updatedDomain =
						User(
							id = 700L,
							name = "Updated Name",
							email = "updated@example.com",
							password = "updated_password",
							role = UserRole.ADMIN,
							createdDt = originalCreatedDt,
							updatedDt = newUpdatedDt
						)

					val updatedEntity = mapper.updateEntity(entity, updatedDomain)

					updatedEntity.id shouldBe entity.id
					updatedEntity.name shouldBe updatedDomain.name
					updatedEntity.email shouldBe updatedDomain.email
					updatedEntity.password shouldBe updatedDomain.password
					updatedEntity.role shouldBe updatedDomain.role
					updatedEntity.createdDt shouldBe originalCreatedDt
					updatedEntity.updatedDt shouldBe newUpdatedDt
					updatedEntity.deletedDt shouldBe updatedDomain.deletedDt
				}

				it("should set deletedDt when updating entity with deleted domain model") {
					val entity =
						UserEntity(
							name = "Before Delete",
							email = "before@example.com",
							password = "password",
							role = UserRole.USER
						).apply {
							id = 800L
							createdDt = LocalDateTime.now().minusDays(3)
							updatedDt = LocalDateTime.now().minusDays(1)
							deletedDt = null
						}

					val deletedTime = LocalDateTime.now()
					val deletedDomain =
						User(
							id = 800L,
							name = entity.name,
							email = entity.email,
							password = entity.password,
							role = entity.role,
							createdDt = entity.createdDt,
							updatedDt = LocalDateTime.now(),
							deletedDt = deletedTime
						)

					val updatedEntity = mapper.updateEntity(entity, deletedDomain)

					updatedEntity.deletedDt shouldBe deletedTime
				}

				it("should update role from USER to ADMIN") {
					val entity =
						UserEntity(
							name = "Role Test",
							email = "role@example.com",
							password = "password",
							role = UserRole.USER
						).apply {
							id = 900L
						}

					val domainWithNewRole =
						User(
							id = 900L,
							name = entity.name,
							email = entity.email,
							password = entity.password,
							role = UserRole.ADMIN,
							createdDt = entity.createdDt,
							updatedDt = LocalDateTime.now()
						)

					val updatedEntity = mapper.updateEntity(entity, domainWithNewRole)

					updatedEntity.role shouldBe UserRole.ADMIN
				}
			}

			describe("round-trip conversion tests") {
				it("should maintain data integrity during Domain -> Entity -> Domain conversion") {
					val originalDomain =
						User(
							id = 1000L,
							name = "Round Trip User",
							email = "roundtrip@example.com",
							password = "round_trip_password",
							role = UserRole.USER,
							createdDt = LocalDateTime.now().minusDays(7),
							updatedDt = LocalDateTime.now().minusDays(2),
							deletedDt = null
						)

					val entity = mapper.toEntity(originalDomain)
					val convertedDomain = mapper.toDomain(entity)

					convertedDomain shouldBe originalDomain
				}

				it("should maintain data integrity during Entity -> Domain -> Entity conversion") {
					val originalEntity =
						UserEntity(
							name = "Entity Round Trip",
							email = "entity.roundtrip@example.com",
							password = "entity_password",
							role = UserRole.ADMIN
						).apply {
							id = 1100L
							createdDt = LocalDateTime.now().minusDays(4)
							updatedDt = LocalDateTime.now().minusDays(1)
							deletedDt = null
						}

					val domain = mapper.toDomain(originalEntity)
					val convertedEntity = mapper.toEntity(domain)

					convertedEntity.id shouldBe originalEntity.id
					convertedEntity.name shouldBe originalEntity.name
					convertedEntity.email shouldBe originalEntity.email
					convertedEntity.password shouldBe originalEntity.password
					convertedEntity.role shouldBe originalEntity.role
					convertedEntity.createdDt shouldBe originalEntity.createdDt
					convertedEntity.updatedDt shouldBe originalEntity.updatedDt
					convertedEntity.deletedDt shouldBe originalEntity.deletedDt
				}
			}

			describe("edge case tests") {
				it("should handle User with special characters in fields") {
					val specialName = "John O'Brien-Smith"
					val specialEmail = "user+test@sub.example.com"
					val specialPassword = "p@ssw0rd!#%&*"

					val domain =
						User(
							id = 1200L,
							name = specialName,
							email = specialEmail,
							password = specialPassword,
							role = UserRole.USER
						)

					val entity = mapper.toEntity(domain)
					val convertedDomain = mapper.toDomain(entity)

					entity.name shouldBe specialName
					entity.email shouldBe specialEmail
					entity.password shouldBe specialPassword
					convertedDomain.name shouldBe specialName
					convertedDomain.email shouldBe specialEmail
					convertedDomain.password shouldBe specialPassword
				}

				it("should handle User with Unicode characters") {
					val unicodeName = "박현우"
					val domain =
						User(
							id = 1300L,
							name = unicodeName,
							email = "unicode@example.com",
							password = "password123",
							role = UserRole.USER
						)

					val entity = mapper.toEntity(domain)
					val convertedDomain = mapper.toDomain(entity)

					entity.name shouldBe unicodeName
					convertedDomain.name shouldBe unicodeName
				}

				it("should handle User created and updated at the same time") {
					val sameTime = LocalDateTime.now()
					val domain =
						User(
							id = 1400L,
							name = "Same Time Test",
							email = "sametime@example.com",
							password = "password",
							role = UserRole.USER,
							createdDt = sameTime,
							updatedDt = sameTime
						)

					val entity = mapper.toEntity(domain)

					entity.createdDt shouldBe sameTime
					entity.updatedDt shouldBe sameTime
					entity.createdDt shouldBe entity.updatedDt
				}

				it("should preserve createdDt after multiple updates") {
					val originalCreatedDt = LocalDateTime.now().minusDays(30)
					var entity =
						UserEntity(
							name = "First Name",
							email = "first@example.com",
							password = "first_password",
							role = UserRole.USER
						).apply {
							id = 1500L
							createdDt = originalCreatedDt
							updatedDt = originalCreatedDt
						}

					val firstUpdate =
						User(
							id = 1500L,
							name = "Second Name",
							email = "second@example.com",
							password = "second_password",
							role = UserRole.USER,
							createdDt = originalCreatedDt,
							updatedDt = LocalDateTime.now().minusDays(15)
						)
					entity = mapper.updateEntity(entity, firstUpdate)

					val secondUpdate =
						User(
							id = 1500L,
							name = "Third Name",
							email = "third@example.com",
							password = "third_password",
							role = UserRole.ADMIN,
							createdDt = originalCreatedDt,
							updatedDt = LocalDateTime.now()
						)
					entity = mapper.updateEntity(entity, secondUpdate)

					entity.createdDt shouldBe originalCreatedDt
					entity.name shouldBe "Third Name"
					entity.email shouldBe "third@example.com"
					entity.role shouldBe UserRole.ADMIN
					entity.updatedDt shouldNotBe originalCreatedDt
				}

				it("should handle BCrypt encoded passwords") {
					val bcryptPassword = "\$2a\$10\$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"
					val domain =
						User(
							id = 1600L,
							name = "BCrypt User",
							email = "bcrypt@example.com",
							password = bcryptPassword,
							role = UserRole.USER
						)

					val entity = mapper.toEntity(domain)
					val convertedDomain = mapper.toDomain(entity)

					entity.password shouldBe bcryptPassword
					convertedDomain.password shouldBe bcryptPassword
				}

				it("should handle empty string in name") {
					val domain =
						User(
							id = 1700L,
							name = "",
							email = "empty.name@example.com",
							password = "password",
							role = UserRole.USER
						)

					val entity = mapper.toEntity(domain)
					val convertedDomain = mapper.toDomain(entity)

					entity.name shouldBe ""
					convertedDomain.name shouldBe ""
				}

				it("should handle very long email addresses") {
					val longEmail = "very.long.email.address.with.many.dots@subdomain.example.com"
					val domain =
						User(
							id = 1800L,
							name = "Long Email User",
							email = longEmail,
							password = "password",
							role = UserRole.USER
						)

					val entity = mapper.toEntity(domain)
					val convertedDomain = mapper.toDomain(entity)

					entity.email shouldBe longEmail
					convertedDomain.email shouldBe longEmail
				}
			}
		}
	})
