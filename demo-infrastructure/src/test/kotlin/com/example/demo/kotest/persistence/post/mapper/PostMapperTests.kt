package com.example.demo.kotest.persistence.post.mapper

import com.example.demo.persistence.post.entity.PostEntity
import com.example.demo.persistence.post.mapper.PostMapper
import com.example.demo.post.model.Post
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime

@ActiveProfiles("test")
@Tags("kotest-unit-test")
class PostMapperTests :
	DescribeSpec({

		val mapper = PostMapper()

		describe("PostMapper") {

			describe("toDomain method") {
				it("should convert PostEntity to Post domain model correctly") {
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

					domain.id shouldBe entity.id
					domain.title shouldBe entity.title
					domain.subTitle shouldBe entity.subTitle
					domain.content shouldBe entity.content
					domain.userId shouldBe entity.userId
					domain.createdDt shouldBe entity.createdDt
					domain.updatedDt shouldBe entity.updatedDt
					domain.deletedDt shouldBe entity.deletedDt
				}

				it("should include deletedDt when converting deleted PostEntity") {
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

					domain.deletedDt shouldBe deletedTime
					(domain.deletedDt != null) shouldBe true
				}
			}

			describe("toEntity method") {
				it("should not set ID when converting new Post domain model") {
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

					entity.id shouldBe 0L
					entity.title shouldBe domain.title
					entity.subTitle shouldBe domain.subTitle
					entity.content shouldBe domain.content
					entity.userId shouldBe domain.userId
					entity.createdDt shouldBe domain.createdDt
					entity.updatedDt shouldBe domain.updatedDt
					entity.deletedDt shouldBe domain.deletedDt
				}

				it("should set ID when converting existing Post domain model") {
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

					entity.id shouldBe existingId
					entity.title shouldBe domain.title
					entity.subTitle shouldBe domain.subTitle
					entity.content shouldBe domain.content
					entity.userId shouldBe domain.userId
					entity.createdDt shouldBe domain.createdDt
					entity.updatedDt shouldBe domain.updatedDt
				}

				it("should set deletedDt when converting deleted Post domain model") {
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

					entity.deletedDt shouldBe deletedTime
					(entity.deletedDt != null) shouldBe true
				}
			}

			describe("updateEntity method") {
				it("should update existing PostEntity with domain model data") {
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

					updatedEntity.id shouldBe entity.id
					updatedEntity.title shouldBe updatedDomain.title
					updatedEntity.subTitle shouldBe updatedDomain.subTitle
					updatedEntity.content shouldBe updatedDomain.content
					updatedEntity.userId shouldBe entity.userId
					updatedEntity.createdDt shouldBe originalCreatedDt
					updatedEntity.updatedDt shouldBe newUpdatedDt
					updatedEntity.deletedDt shouldBe updatedDomain.deletedDt
				}

				it("should set deletedDt when updating entity with deleted domain model") {
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

					updatedEntity.deletedDt shouldBe deletedTime
					(updatedEntity.deletedDt != null) shouldBe true
				}
			}

			describe("round-trip conversion tests") {
				it("should maintain data integrity during Domain -> Entity -> Domain conversion") {
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

					convertedDomain shouldBe originalDomain
				}

				it("should maintain data integrity during Entity -> Domain -> Entity conversion") {
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

					convertedEntity.id shouldBe originalEntity.id
					convertedEntity.title shouldBe originalEntity.title
					convertedEntity.subTitle shouldBe originalEntity.subTitle
					convertedEntity.content shouldBe originalEntity.content
					convertedEntity.userId shouldBe originalEntity.userId
					convertedEntity.createdDt shouldBe originalEntity.createdDt
					convertedEntity.updatedDt shouldBe originalEntity.updatedDt
					convertedEntity.deletedDt shouldBe originalEntity.deletedDt
				}
			}

			describe("edge case tests") {
				it("should handle Post with maximum length fields") {
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

					entity.title shouldBe longTitle
					entity.subTitle shouldBe longSubTitle
					entity.content shouldBe longContent
					convertedDomain.title shouldBe longTitle
					convertedDomain.subTitle shouldBe longSubTitle
					convertedDomain.content shouldBe longContent
				}

				it("should handle Post created and updated at the same time") {
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

					entity.createdDt shouldBe sameTime
					entity.updatedDt shouldBe sameTime
					entity.createdDt shouldBe entity.updatedDt
				}

				it("should preserve createdDt after multiple updates") {
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

					entity.createdDt shouldBe originalCreatedDt
					entity.title shouldBe "Third Title"
					entity.updatedDt shouldNotBe originalCreatedDt
				}
			}
		}
	})
