package com.example.demo.mockito.persistence.user.mapper

import com.example.demo.persistence.user.entity.UserEntity
import com.example.demo.persistence.user.mapper.UserMapper
import com.example.demo.user.constant.UserRole
import com.example.demo.user.model.User
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime

@ActiveProfiles("test")
@Tag("mockito-unit-test")
@DisplayName("Mockito Unit - User Mapper Test")
@ExtendWith(MockitoExtension::class)
class UserMapperTests {
	private lateinit var mapper: UserMapper

	@BeforeEach
	fun setUp() {
		mapper = UserMapper()
	}

	@Nested
	@DisplayName("toDomain method tests")
	inner class ToDomainTests {
		@Test
		@DisplayName("should convert UserEntity to User domain model correctly")
		fun `should convert UserEntity to User domain model correctly`() {
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

			assertEquals(entity.id, domain.id)
			assertEquals(entity.name, domain.name)
			assertEquals(entity.email, domain.email)
			assertEquals(entity.password, domain.password)
			assertEquals(entity.role, domain.role)
			assertEquals(entity.createdDt, domain.createdDt)
			assertEquals(entity.updatedDt, domain.updatedDt)
			assertEquals(entity.deletedDt, domain.deletedDt)
		}

		@Test
		@DisplayName("should include deletedDt when converting deleted UserEntity")
		fun `should include deletedDt when converting deleted UserEntity`() {
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

			assertEquals(deletedTime, domain.deletedDt)
			assertTrue(domain.deletedDt != null)
		}

		@Test
		@DisplayName("should handle entity with minimal fields")
		fun `should handle entity with minimal fields`() {
			val entity =
				UserEntity(
					name = "Minimal User",
					email = "minimal@example.com",
					password = "password",
					role = UserRole.USER
				)

			val domain = mapper.toDomain(entity)

			assertEquals(0L, domain.id)
			assertEquals(entity.name, domain.name)
			assertEquals(entity.email, domain.email)
			assertEquals(entity.password, domain.password)
			assertEquals(entity.role, domain.role)
			assertNotNull(domain.createdDt)
			assertNotNull(domain.updatedDt)
			assertNull(domain.deletedDt)
		}

		@Test
		@DisplayName("should correctly map different user roles")
		fun `should correctly map different user roles`() {
			val adminEntity =
				UserEntity(
					name = "Admin User",
					email = "admin@example.com",
					password = "admin_password",
					role = UserRole.ADMIN
				).apply {
					id = 300L
				}

			val userEntity =
				UserEntity(
					name = "Regular User",
					email = "user@example.com",
					password = "user_password",
					role = UserRole.USER
				).apply {
					id = 301L
				}

			val adminDomain = mapper.toDomain(adminEntity)
			val userDomain = mapper.toDomain(userEntity)

			assertEquals(UserRole.ADMIN, adminDomain.role)
			assertEquals(UserRole.USER, userDomain.role)
		}
	}

	@Nested
	@DisplayName("toEntity method tests")
	inner class ToEntityTests {
		@Test
		@DisplayName("should not set ID when converting new User domain model")
		fun `should not set ID when converting new User domain model`() {
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

			assertEquals(0L, entity.id)
			assertEquals(domain.name, entity.name)
			assertEquals(domain.email, entity.email)
			assertEquals(domain.password, entity.password)
			assertEquals(domain.role, entity.role)
			assertEquals(domain.createdDt, entity.createdDt)
			assertEquals(domain.updatedDt, entity.updatedDt)
			assertEquals(domain.deletedDt, entity.deletedDt)
		}

		@Test
		@DisplayName("should set ID when converting existing User domain model")
		fun `should set ID when converting existing User domain model`() {
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

			assertEquals(existingId, entity.id)
			assertEquals(domain.name, entity.name)
			assertEquals(domain.email, entity.email)
			assertEquals(domain.password, entity.password)
			assertEquals(domain.role, entity.role)
			assertEquals(domain.createdDt, entity.createdDt)
			assertEquals(domain.updatedDt, entity.updatedDt)
		}

		@Test
		@DisplayName("should set deletedDt when converting deleted User domain model")
		fun `should set deletedDt when converting deleted User domain model`() {
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

			assertEquals(deletedTime, entity.deletedDt)
			assertTrue(entity.deletedDt != null)
		}

		@Test
		@DisplayName("should handle User with all fields populated")
		fun `should handle User with all fields populated`() {
			val now = LocalDateTime.now()
			val domain =
				User(
					id = 600L,
					name = "Complete User",
					email = "complete@example.com",
					password = "complete_password",
					role = UserRole.ADMIN,
					createdDt = now.minusDays(10),
					updatedDt = now.minusDays(1),
					deletedDt = now
				)

			val entity = mapper.toEntity(domain)

			assertEquals(domain.id, entity.id)
			assertEquals(domain.name, entity.name)
			assertEquals(domain.email, entity.email)
			assertEquals(domain.password, entity.password)
			assertEquals(domain.role, entity.role)
			assertEquals(domain.createdDt, entity.createdDt)
			assertEquals(domain.updatedDt, entity.updatedDt)
			assertEquals(domain.deletedDt, entity.deletedDt)
		}
	}

	@Nested
	@DisplayName("updateEntity method tests")
	inner class UpdateEntityTests {
		@Test
		@DisplayName("should update existing UserEntity with domain model data")
		fun `should update existing UserEntity with domain model data`() {
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

			assertEquals(entity.id, updatedEntity.id)
			assertEquals(updatedDomain.name, updatedEntity.name)
			assertEquals(updatedDomain.email, updatedEntity.email)
			assertEquals(updatedDomain.password, updatedEntity.password)
			assertEquals(updatedDomain.role, updatedEntity.role)
			assertEquals(originalCreatedDt, updatedEntity.createdDt)
			assertEquals(newUpdatedDt, updatedEntity.updatedDt)
			assertEquals(updatedDomain.deletedDt, updatedEntity.deletedDt)
		}

		@Test
		@DisplayName("should set deletedDt when updating entity with deleted domain")
		fun `should set deletedDt when updating entity with deleted domain`() {
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

			assertEquals(deletedTime, updatedEntity.deletedDt)
			assertTrue(updatedEntity.deletedDt != null)
		}

		@Test
		@DisplayName("should preserve entity ID during update")
		fun `should preserve entity ID during update`() {
			val entityId = 900L
			val entity =
				UserEntity(
					name = "Old Name",
					email = "old@example.com",
					password = "old_password",
					role = UserRole.USER
				).apply {
					id = entityId
				}

			val domain =
				User(
					id = 999L,
					name = "New Name",
					email = "new@example.com",
					password = "new_password",
					role = UserRole.ADMIN
				)

			val updatedEntity = mapper.updateEntity(entity, domain)

			assertEquals(entityId, updatedEntity.id)
			assertEquals(domain.name, updatedEntity.name)
		}

		@Test
		@DisplayName("should update role correctly")
		fun `should update role correctly`() {
			val entity =
				UserEntity(
					name = "Role Test",
					email = "role@example.com",
					password = "password",
					role = UserRole.USER
				).apply {
					id = 1000L
				}

			val domainWithNewRole =
				User(
					id = 1000L,
					name = entity.name,
					email = entity.email,
					password = entity.password,
					role = UserRole.ADMIN,
					createdDt = entity.createdDt,
					updatedDt = LocalDateTime.now()
				)

			val updatedEntity = mapper.updateEntity(entity, domainWithNewRole)

			assertEquals(UserRole.ADMIN, updatedEntity.role)
		}
	}

	@Nested
	@DisplayName("Round-trip conversion tests")
	inner class RoundTripTests {
		@Test
		@DisplayName("should maintain data integrity during Domain -> Entity -> Domain conversion")
		fun `should maintain data integrity during Domain to Entity to Domain conversion`() {
			val originalDomain =
				User(
					id = 1100L,
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

			assertEquals(originalDomain, convertedDomain)
		}

		@Test
		@DisplayName("should maintain data integrity during Entity -> Domain -> Entity conversion")
		fun `should maintain data integrity during Entity to Domain to Entity conversion`() {
			val originalEntity =
				UserEntity(
					name = "Entity Round Trip",
					email = "entity.roundtrip@example.com",
					password = "entity_password",
					role = UserRole.ADMIN
				).apply {
					id = 1200L
					createdDt = LocalDateTime.now().minusDays(4)
					updatedDt = LocalDateTime.now().minusDays(1)
					deletedDt = null
				}

			val domain = mapper.toDomain(originalEntity)
			val convertedEntity = mapper.toEntity(domain)

			assertEquals(originalEntity.id, convertedEntity.id)
			assertEquals(originalEntity.name, convertedEntity.name)
			assertEquals(originalEntity.email, convertedEntity.email)
			assertEquals(originalEntity.password, convertedEntity.password)
			assertEquals(originalEntity.role, convertedEntity.role)
			assertEquals(originalEntity.createdDt, convertedEntity.createdDt)
			assertEquals(originalEntity.updatedDt, convertedEntity.updatedDt)
			assertEquals(originalEntity.deletedDt, convertedEntity.deletedDt)
		}
	}

	@Nested
	@DisplayName("Edge case tests")
	inner class EdgeCaseTests {
		@Test
		@DisplayName("should handle User with special characters in fields")
		fun `should handle User with special characters in fields`() {
			val specialName = "John O'Brien-Smith"
			val specialEmail = "user+test@sub.example.com"
			val specialPassword = "p@ssw0rd!#%&*"

			val domain =
				User(
					id = 1300L,
					name = specialName,
					email = specialEmail,
					password = specialPassword,
					role = UserRole.USER
				)

			val entity = mapper.toEntity(domain)
			val convertedDomain = mapper.toDomain(entity)

			assertEquals(specialName, entity.name)
			assertEquals(specialEmail, entity.email)
			assertEquals(specialPassword, entity.password)
			assertEquals(specialName, convertedDomain.name)
			assertEquals(specialEmail, convertedDomain.email)
			assertEquals(specialPassword, convertedDomain.password)
		}

		@Test
		@DisplayName("should handle User with Unicode characters")
		fun `should handle User with Unicode characters`() {
			val unicodeName = "Kim Cheolsu Yamada Taro Müller"
			val domain =
				User(
					id = 1400L,
					name = unicodeName,
					email = "unicode@example.com",
					password = "password123",
					role = UserRole.USER
				)

			val entity = mapper.toEntity(domain)
			val convertedDomain = mapper.toDomain(entity)

			assertEquals(unicodeName, entity.name)
			assertEquals(unicodeName, convertedDomain.name)
		}

		@Test
		@DisplayName("should handle User created and updated at the same time")
		fun `should handle User created and updated at the same time`() {
			val sameTime = LocalDateTime.now()
			val domain =
				User(
					id = 1500L,
					name = "Same Time Test",
					email = "sametime@example.com",
					password = "password",
					role = UserRole.USER,
					createdDt = sameTime,
					updatedDt = sameTime
				)

			val entity = mapper.toEntity(domain)

			assertEquals(sameTime, entity.createdDt)
			assertEquals(sameTime, entity.updatedDt)
			assertEquals(entity.createdDt, entity.updatedDt)
		}

		@Test
		@DisplayName("should preserve createdDt after multiple updates")
		fun `should preserve createdDt after multiple updates`() {
			val originalCreatedDt = LocalDateTime.now().minusDays(30)
			var entity =
				UserEntity(
					name = "First Name",
					email = "first@example.com",
					password = "first_password",
					role = UserRole.USER
				).apply {
					id = 1600L
					createdDt = originalCreatedDt
					updatedDt = originalCreatedDt
				}

			val firstUpdate =
				User(
					id = 1600L,
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
					id = 1600L,
					name = "Third Name",
					email = "third@example.com",
					password = "third_password",
					role = UserRole.ADMIN,
					createdDt = originalCreatedDt,
					updatedDt = LocalDateTime.now()
				)
			entity = mapper.updateEntity(entity, secondUpdate)

			assertEquals(originalCreatedDt, entity.createdDt)
			assertEquals("Third Name", entity.name)
			assertEquals("third@example.com", entity.email)
			assertEquals(UserRole.ADMIN, entity.role)
			assertNotEquals(originalCreatedDt, entity.updatedDt)
		}

		@Test
		@DisplayName("should handle empty string in name")
		fun `should handle empty string in name`() {
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

			assertEquals("", entity.name)
			assertEquals("", convertedDomain.name)
		}

		@Test
		@DisplayName("should handle BCrypt encoded passwords")
		fun `should handle BCrypt encoded passwords`() {
			val bcryptPassword = "\$2a\$10\$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"
			val domain =
				User(
					id = 1800L,
					name = "BCrypt User",
					email = "bcrypt@example.com",
					password = bcryptPassword,
					role = UserRole.USER
				)

			val entity = mapper.toEntity(domain)
			val convertedDomain = mapper.toDomain(entity)

			assertEquals(bcryptPassword, entity.password)
			assertEquals(bcryptPassword, convertedDomain.password)
		}

		@Test
		@DisplayName("should handle very long email addresses")
		fun `should handle very long email addresses`() {
			val longEmail = "very.long.email.address.with.many.dots@subdomain.example.com"
			val domain =
				User(
					id = 1900L,
					name = "Long Email User",
					email = longEmail,
					password = "password",
					role = UserRole.USER
				)

			val entity = mapper.toEntity(domain)
			val convertedDomain = mapper.toDomain(entity)

			assertEquals(longEmail, entity.email)
			assertEquals(longEmail, convertedDomain.email)
		}

		@Test
		@DisplayName("should handle password with whitespace")
		fun `should handle password with whitespace`() {
			val passwordWithSpace = "pass word 123"
			val domain =
				User(
					id = 2000L,
					name = "Space Password User",
					email = "space@example.com",
					password = passwordWithSpace,
					role = UserRole.USER
				)

			val entity = mapper.toEntity(domain)
			val convertedDomain = mapper.toDomain(entity)

			assertEquals(passwordWithSpace, entity.password)
			assertEquals(passwordWithSpace, convertedDomain.password)
		}

		@Test
		@DisplayName("should correctly handle role transitions")
		fun `should correctly handle role transitions`() {
			val userEntity =
				UserEntity(
					name = "Role Transition",
					email = "transition@example.com",
					password = "password",
					role = UserRole.USER
				).apply {
					id = 2100L
				}

			val domain = mapper.toDomain(userEntity)
			val adminDomain = domain.copy(role = UserRole.ADMIN)
			val adminEntity = mapper.toEntity(adminDomain)

			assertEquals(UserRole.USER, userEntity.role)
			assertEquals(UserRole.ADMIN, adminEntity.role)
		}
	}
}
