package com.example.demo.mockito.persistence.post.mapper

import com.example.demo.persistence.post.entity.PostEntity
import com.example.demo.persistence.post.mapper.PostMapper
import com.example.demo.post.model.Post
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
@DisplayName("Mockito Unit - Post Mapper Test")
@ExtendWith(MockitoExtension::class)
class PostMapperTests {
	private lateinit var mapper: PostMapper

	@BeforeEach
	fun setUp() {
		mapper = PostMapper()
	}

	@Nested
	@DisplayName("toDomain method tests")
	inner class ToDomainTests {
		@Test
		@DisplayName("should convert PostEntity to Post domain model correctly")
		fun `should convert PostEntity to Post domain model correctly`() {
			val now = LocalDateTime.now()
			val entity =
				PostEntity(
					title = "Test Title",
					subTitle = "Test Subtitle",
					content = "Test Content",
					userId = 1L
				).apply {
					id = 100L
					createdDt = now.minusDays(1)
					updatedDt = now
					deletedDt = null
				}

			val domain = mapper.toDomain(entity)

			assertEquals(entity.id, domain.id)
			assertEquals(entity.title, domain.title)
			assertEquals(entity.subTitle, domain.subTitle)
			assertEquals(entity.content, domain.content)
			assertEquals(entity.userId, domain.userId)
			assertEquals(entity.createdDt, domain.createdDt)
			assertEquals(entity.updatedDt, domain.updatedDt)
			assertEquals(entity.deletedDt, domain.deletedDt)
		}

		@Test
		@DisplayName("should include deletedDt when converting deleted PostEntity")
		fun `should include deletedDt when converting deleted PostEntity`() {
			val deletedTime = LocalDateTime.now()
			val entity =
				PostEntity(
					title = "Deleted Post",
					subTitle = "Deleted Subtitle",
					content = "Deleted Content",
					userId = 2L
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
				PostEntity(
					title = "Title",
					subTitle = "Subtitle",
					content = "Content",
					userId = 3L
				)

			val domain = mapper.toDomain(entity)

			assertEquals(0L, domain.id)
			assertEquals(entity.title, domain.title)
			assertEquals(entity.subTitle, domain.subTitle)
			assertEquals(entity.content, domain.content)
			assertEquals(entity.userId, domain.userId)
			assertNotNull(domain.createdDt)
			assertNotNull(domain.updatedDt)
			assertNull(domain.deletedDt)
		}
	}

	@Nested
	@DisplayName("toEntity method tests")
	inner class ToEntityTests {
		@Test
		@DisplayName("should not set ID when converting new Post domain model")
		fun `should not set ID when converting new Post domain model`() {
			val domain =
				Post(
					id = 0L,
					title = "New Post",
					subTitle = "New Subtitle",
					content = "New Content",
					userId = 3L,
					createdDt = LocalDateTime.now(),
					updatedDt = LocalDateTime.now()
				)

			val entity = mapper.toEntity(domain)

			assertEquals(0L, entity.id)
			assertEquals(domain.title, entity.title)
			assertEquals(domain.subTitle, entity.subTitle)
			assertEquals(domain.content, entity.content)
			assertEquals(domain.userId, entity.userId)
			assertEquals(domain.createdDt, entity.createdDt)
			assertEquals(domain.updatedDt, entity.updatedDt)
			assertEquals(domain.deletedDt, entity.deletedDt)
		}

		@Test
		@DisplayName("should set ID when converting existing Post domain model")
		fun `should set ID when converting existing Post domain model`() {
			val existingId = 300L
			val now = LocalDateTime.now()
			val domain =
				Post(
					id = existingId,
					title = "Existing Post",
					subTitle = "Existing Subtitle",
					content = "Existing Content",
					userId = 4L,
					createdDt = now.minusDays(5),
					updatedDt = now
				)

			val entity = mapper.toEntity(domain)

			assertEquals(existingId, entity.id)
			assertEquals(domain.title, entity.title)
			assertEquals(domain.subTitle, entity.subTitle)
			assertEquals(domain.content, entity.content)
			assertEquals(domain.userId, entity.userId)
			assertEquals(domain.createdDt, entity.createdDt)
			assertEquals(domain.updatedDt, entity.updatedDt)
		}

		@Test
		@DisplayName("should set deletedDt when converting deleted Post domain model")
		fun `should set deletedDt when converting deleted Post domain model`() {
			val deletedTime = LocalDateTime.now().minusHours(2)
			val domain =
				Post(
					id = 400L,
					title = "Deleted Post",
					subTitle = "Deleted Subtitle",
					content = "Deleted Content",
					userId = 5L,
					deletedDt = deletedTime
				)

			val entity = mapper.toEntity(domain)

			assertEquals(deletedTime, entity.deletedDt)
			assertTrue(entity.deletedDt != null)
		}

		@Test
		@DisplayName("should handle Post with all fields populated")
		fun `should handle Post with all fields populated`() {
			val now = LocalDateTime.now()
			val domain =
				Post(
					id = 500L,
					title = "Complete Post",
					subTitle = "Complete Subtitle",
					content = "Complete Content",
					userId = 6L,
					createdDt = now.minusDays(10),
					updatedDt = now.minusDays(1),
					deletedDt = now
				)

			val entity = mapper.toEntity(domain)

			assertEquals(domain.id, entity.id)
			assertEquals(domain.title, entity.title)
			assertEquals(domain.subTitle, entity.subTitle)
			assertEquals(domain.content, entity.content)
			assertEquals(domain.userId, entity.userId)
			assertEquals(domain.createdDt, entity.createdDt)
			assertEquals(domain.updatedDt, entity.updatedDt)
			assertEquals(domain.deletedDt, entity.deletedDt)
		}
	}

	@Nested
	@DisplayName("updateEntity method tests")
	inner class UpdateEntityTests {
		@Test
		@DisplayName("should update existing PostEntity with domain model data")
		fun `should update existing PostEntity with domain model data`() {
			val originalCreatedDt = LocalDateTime.now().minusDays(10)
			val originalUpdatedDt = LocalDateTime.now().minusDays(5)
			val entity =
				PostEntity(
					title = "Original Title",
					subTitle = "Original Subtitle",
					content = "Original Content",
					userId = 6L
				).apply {
					id = 500L
					createdDt = originalCreatedDt
					updatedDt = originalUpdatedDt
					deletedDt = null
				}

			val newUpdatedDt = LocalDateTime.now()
			val updatedDomain =
				Post(
					id = 500L,
					title = "Updated Title",
					subTitle = "Updated Subtitle",
					content = "Updated Content",
					userId = 6L,
					createdDt = originalCreatedDt,
					updatedDt = newUpdatedDt
				)

			val updatedEntity = mapper.updateEntity(entity, updatedDomain)

			assertEquals(entity.id, updatedEntity.id)
			assertEquals(updatedDomain.title, updatedEntity.title)
			assertEquals(updatedDomain.subTitle, updatedEntity.subTitle)
			assertEquals(updatedDomain.content, updatedEntity.content)
			assertEquals(entity.userId, updatedEntity.userId)
			assertEquals(originalCreatedDt, updatedEntity.createdDt)
			assertEquals(newUpdatedDt, updatedEntity.updatedDt)
			assertEquals(updatedDomain.deletedDt, updatedEntity.deletedDt)
		}

		@Test
		@DisplayName("should set deletedDt when updating entity with deleted domain")
		fun `should set deletedDt when updating entity with deleted domain`() {
			val entity =
				PostEntity(
					title = "Before Delete Title",
					subTitle = "Before Delete Subtitle",
					content = "Before Delete Content",
					userId = 7L
				).apply {
					id = 600L
					createdDt = LocalDateTime.now().minusDays(3)
					updatedDt = LocalDateTime.now().minusDays(1)
					deletedDt = null
				}

			val deletedTime = LocalDateTime.now()
			val deletedDomain =
				Post(
					id = 600L,
					title = entity.title,
					subTitle = entity.subTitle,
					content = entity.content,
					userId = 7L,
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
			val entityId = 700L
			val entity =
				PostEntity(
					title = "Old Title",
					subTitle = "Old Subtitle",
					content = "Old Content",
					userId = 8L
				).apply {
					id = entityId
				}

			val domain =
				Post(
					id = 999L,
					title = "New Title",
					subTitle = "New Subtitle",
					content = "New Content",
					userId = 8L
				)

			val updatedEntity = mapper.updateEntity(entity, domain)

			assertEquals(entityId, updatedEntity.id)
			assertEquals(domain.title, updatedEntity.title)
		}
	}

	@Nested
	@DisplayName("Round-trip conversion tests")
	inner class RoundTripTests {
		@Test
		@DisplayName("should maintain data integrity during Domain -> Entity -> Domain conversion")
		fun `should maintain data integrity during Domain to Entity to Domain conversion`() {
			val originalDomain =
				Post(
					id = 700L,
					title = "Round Trip Title",
					subTitle = "Round Trip Subtitle",
					content = "Round Trip Content",
					userId = 8L,
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
				PostEntity(
					title = "Entity Round Trip",
					subTitle = "Entity Round Subtitle",
					content = "Entity Round Content",
					userId = 9L
				).apply {
					id = 800L
					createdDt = LocalDateTime.now().minusDays(4)
					updatedDt = LocalDateTime.now().minusDays(1)
					deletedDt = null
				}

			val domain = mapper.toDomain(originalEntity)
			val convertedEntity = mapper.toEntity(domain)

			assertEquals(originalEntity.id, convertedEntity.id)
			assertEquals(originalEntity.title, convertedEntity.title)
			assertEquals(originalEntity.subTitle, convertedEntity.subTitle)
			assertEquals(originalEntity.content, convertedEntity.content)
			assertEquals(originalEntity.userId, convertedEntity.userId)
			assertEquals(originalEntity.createdDt, convertedEntity.createdDt)
			assertEquals(originalEntity.updatedDt, convertedEntity.updatedDt)
			assertEquals(originalEntity.deletedDt, convertedEntity.deletedDt)
		}
	}

	@Nested
	@DisplayName("Edge case tests")
	inner class EdgeCaseTests {
		@Test
		@DisplayName("should handle Post with maximum length fields")
		fun `should handle Post with maximum length fields`() {
			val longTitle = "A".repeat(20)
			val longSubTitle = "B".repeat(40)
			val longContent = "C".repeat(500)

			val domain =
				Post(
					id = 900L,
					title = longTitle,
					subTitle = longSubTitle,
					content = longContent,
					userId = 10L
				)

			val entity = mapper.toEntity(domain)
			val convertedDomain = mapper.toDomain(entity)

			assertEquals(longTitle, entity.title)
			assertEquals(longSubTitle, entity.subTitle)
			assertEquals(longContent, entity.content)
			assertEquals(longTitle, convertedDomain.title)
			assertEquals(longSubTitle, convertedDomain.subTitle)
			assertEquals(longContent, convertedDomain.content)
		}

		@Test
		@DisplayName("should handle Post created and updated at the same time")
		fun `should handle Post created and updated at the same time`() {
			val sameTime = LocalDateTime.now()
			val domain =
				Post(
					id = 1000L,
					title = "Same Time Test",
					subTitle = "Same Time Subtitle",
					content = "Same Time Content",
					userId = 11L,
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
				PostEntity(
					title = "First Title",
					subTitle = "First Subtitle",
					content = "First Content",
					userId = 12L
				).apply {
					id = 1100L
					createdDt = originalCreatedDt
					updatedDt = originalCreatedDt
				}

			val firstUpdate =
				Post(
					id = 1100L,
					title = "Second Title",
					subTitle = "Second Subtitle",
					content = "Second Content",
					userId = 12L,
					createdDt = originalCreatedDt,
					updatedDt = LocalDateTime.now().minusDays(15)
				)
			entity = mapper.updateEntity(entity, firstUpdate)

			val secondUpdate =
				Post(
					id = 1100L,
					title = "Third Title",
					subTitle = "Third Subtitle",
					content = "Third Content",
					userId = 12L,
					createdDt = originalCreatedDt,
					updatedDt = LocalDateTime.now()
				)
			entity = mapper.updateEntity(entity, secondUpdate)

			assertEquals(originalCreatedDt, entity.createdDt)
			assertEquals("Third Title", entity.title)
			assertNotEquals(originalCreatedDt, entity.updatedDt)
		}

		@Test
		@DisplayName("should handle empty strings in text fields")
		fun `should handle empty strings in text fields`() {
			val domain =
				Post(
					id = 1200L,
					title = "",
					subTitle = "",
					content = "",
					userId = 13L
				)

			val entity = mapper.toEntity(domain)
			val convertedDomain = mapper.toDomain(entity)

			assertEquals("", entity.title)
			assertEquals("", entity.subTitle)
			assertEquals("", entity.content)
			assertEquals("", convertedDomain.title)
			assertEquals("", convertedDomain.subTitle)
			assertEquals("", convertedDomain.content)
		}

		@Test
		@DisplayName("should handle special characters in text fields")
		fun `should handle special characters in text fields`() {
			val specialTitle = "Title with @#$%^&*() special chars"
			val specialSubTitle = "Subtitle"
			val specialContent = "Content with \n\r\t tabs and lines"

			val domain =
				Post(
					id = 1300L,
					title = specialTitle,
					subTitle = specialSubTitle,
					content = specialContent,
					userId = 14L
				)

			val entity = mapper.toEntity(domain)
			val convertedDomain = mapper.toDomain(entity)

			assertEquals(specialTitle, entity.title)
			assertEquals(specialSubTitle, entity.subTitle)
			assertEquals(specialContent, entity.content)
			assertEquals(specialTitle, convertedDomain.title)
			assertEquals(specialSubTitle, convertedDomain.subTitle)
			assertEquals(specialContent, convertedDomain.content)
		}
	}
}
