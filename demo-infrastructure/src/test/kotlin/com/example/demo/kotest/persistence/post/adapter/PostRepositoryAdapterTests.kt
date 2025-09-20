package com.example.demo.kotest.persistence.post.adapter

import com.example.demo.persistence.post.adapter.PostRepositoryAdapter
import com.example.demo.persistence.post.entity.PostEntity
import com.example.demo.persistence.post.mapper.PostMapper
import com.example.demo.persistence.post.repository.PostJpaRepository
import com.example.demo.post.model.Post
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@Tags("kotest-unit-test")
class PostRepositoryAdapterTests :
	FunSpec({

		lateinit var postJpaRepository: PostJpaRepository
		lateinit var postMapper: PostMapper
		lateinit var adapter: PostRepositoryAdapter

		beforeTest {
			postJpaRepository = mockk()
			postMapper = mockk()
			adapter = PostRepositoryAdapter(postJpaRepository, postMapper)
		}

		context("save") {
			test("should save new post when id is 0") {
				val post =
					Post(
						id = 0L,
						title = "New Post",
						subTitle = "Subtitle",
						content = "Content",
						userId = 1L
					)
				val entity =
					PostEntity(
						title = post.title,
						subTitle = post.subTitle,
						content = post.content,
						userId = post.userId
					)
				val savedEntity = entity.apply { id = 100L }
				val savedPost = post.copy(id = 100L)

				every { postMapper.toEntity(post) } returns entity
				every { postJpaRepository.save(entity) } returns savedEntity
				every { postMapper.toDomain(savedEntity) } returns savedPost

				val result = adapter.save(post)

				result shouldBe savedPost
				verify(exactly = 1) { postMapper.toEntity(post) }
				verify(exactly = 1) { postJpaRepository.save(entity) }
				verify(exactly = 1) { postMapper.toDomain(savedEntity) }
				verify(exactly = 0) { postJpaRepository.findByIdOrNull(any<Long>()) }
			}

			test("should update existing post when id is not 0 and entity exists") {
				val post =
					Post(
						id = 100L,
						title = "Updated Post",
						subTitle = "Updated Subtitle",
						content = "Updated Content",
						userId = 1L
					)
				val existingEntity =
					PostEntity(
						title = "Old Title",
						subTitle = "Old Subtitle",
						content = "Old Content",
						userId = 1L
					).apply { id = 100L }
				val updatedEntity = existingEntity
				val savedPost = post

				every { postJpaRepository.findByIdOrNull(100L) } returns existingEntity
				every { postMapper.updateEntity(existingEntity, post) } returns updatedEntity
				every { postJpaRepository.save(updatedEntity) } returns updatedEntity
				every { postMapper.toDomain(updatedEntity) } returns savedPost

				val result = adapter.save(post)

				result shouldBe savedPost
				verify(exactly = 1) { postJpaRepository.findByIdOrNull(100L) }
				verify(exactly = 1) { postMapper.updateEntity(existingEntity, post) }
				verify(exactly = 1) { postJpaRepository.save(updatedEntity) }
			}
		}

		context("findOneById") {
			test("should return post when found") {
				val entity =
					PostEntity(
						title = "Title",
						subTitle = "Subtitle",
						content = "Content",
						userId = 1L
					).apply { id = 100L }
				val post =
					Post(
						id = 100L,
						title = "Title",
						subTitle = "Subtitle",
						content = "Content",
						userId = 1L
					)

				every { postJpaRepository.findByIdOrNull(100L) } returns entity
				every { postMapper.toDomain(entity) } returns post

				val result = adapter.findOneById(100L)

				result.shouldNotBeNull()
				result.id shouldBe 100L
				result.title shouldBe "Title"
				result.subTitle shouldBe "Subtitle"
				result.content shouldBe "Content"
				result.userId shouldBe 1L
				verify(exactly = 1) { postJpaRepository.findByIdOrNull(100L) }
				verify(exactly = 1) { postMapper.toDomain(entity) }
			}

			test("should return null when not found") {
				every { postJpaRepository.findByIdOrNull(999L) } returns null

				val result = adapter.findOneById(999L)

				result shouldBe null
				verify(exactly = 1) { postJpaRepository.findByIdOrNull(999L) }
				verify(exactly = 0) { postMapper.toDomain(any<PostEntity>()) }
			}
		}

		context("findAll") {
			test("should return paged posts") {
				val pageable = PageRequest.of(0, 10)
				val entities =
					listOf(
						PostEntity("Title1", "Sub1", "Content1", 1L).apply { id = 1L },
						PostEntity("Title2", "Sub2", "Content2", 2L).apply { id = 2L }
					)
				val posts =
					listOf(
						Post(1L, "Title1", "Sub1", "Content1", 1L),
						Post(2L, "Title2", "Sub2", "Content2", 2L)
					)
				val entityPage = PageImpl(entities, pageable, 2)

				every { postJpaRepository.findAll(pageable) } returns entityPage
				entities.forEachIndexed { index, entity ->
					every { postMapper.toDomain(entity) } returns posts[index]
				}

				val result = adapter.findAll(pageable)

				result.content shouldHaveSize 2
				result.content shouldBe posts
				result.totalElements shouldBe 2
				verify(exactly = 1) { postJpaRepository.findAll(pageable) }
				verify(exactly = 2) { postMapper.toDomain(any<PostEntity>()) }
			}

			test("should return empty page when no posts") {
				val pageable = PageRequest.of(0, 10)
				val emptyPage = PageImpl<PostEntity>(emptyList(), pageable, 0)

				every { postJpaRepository.findAll(pageable) } returns emptyPage

				val result = adapter.findAll(pageable)

				result.content shouldHaveSize 0
				result.totalElements shouldBe 0
				verify(exactly = 1) { postJpaRepository.findAll(pageable) }
				verify(exactly = 0) { postMapper.toDomain(any<PostEntity>()) }
			}
		}

		context("findOneByUserId") {
			test("should return paged posts for user") {
				val userId = 1L
				val pageable = PageRequest.of(0, 5)
				val entities =
					listOf(
						PostEntity("User Post 1", "Sub1", "Content1", userId).apply { id = 1L },
						PostEntity("User Post 2", "Sub2", "Content2", userId).apply { id = 2L }
					)
				val posts =
					listOf(
						Post(1L, "User Post 1", "Sub1", "Content1", userId),
						Post(2L, "User Post 2", "Sub2", "Content2", userId)
					)
				val entityPage = PageImpl(entities, pageable, 2)

				every { postJpaRepository.findByUserId(userId, pageable) } returns entityPage
				entities.forEachIndexed { index, entity ->
					every { postMapper.toDomain(entity) } returns posts[index]
				}

				val result = adapter.findOneByUserId(userId, pageable)

				result.content shouldHaveSize 2
				result.content shouldBe posts
				verify(exactly = 1) { postJpaRepository.findByUserId(userId, pageable) }
			}
		}

		context("findAllByUserId") {
			test("should return all posts for user") {
				val userId = 1L
				val entities =
					listOf(
						PostEntity("Post 1", "Sub1", "Content1", userId).apply { id = 1L },
						PostEntity("Post 2", "Sub2", "Content2", userId).apply { id = 2L },
						PostEntity("Post 3", "Sub3", "Content3", userId).apply { id = 3L }
					)
				val posts =
					listOf(
						Post(1L, "Post 1", "Sub1", "Content1", userId),
						Post(2L, "Post 2", "Sub2", "Content2", userId),
						Post(3L, "Post 3", "Sub3", "Content3", userId)
					)

				every { postJpaRepository.findAllByUserId(userId) } returns entities
				entities.forEachIndexed { index, entity ->
					every { postMapper.toDomain(entity) } returns posts[index]
				}

				val result = adapter.findAllByUserId(userId)

				result shouldHaveSize 3
				result shouldBe posts
				verify(exactly = 1) { postJpaRepository.findAllByUserId(userId) }
			}

			test("should return empty list when user has no posts") {
				val userId = 999L
				every { postJpaRepository.findAllByUserId(userId) } returns emptyList()

				val result = adapter.findAllByUserId(userId)

				result shouldHaveSize 0
				verify(exactly = 1) { postJpaRepository.findAllByUserId(userId) }
				verify(exactly = 0) { postMapper.toDomain(any<PostEntity>()) }
			}
		}

		context("findExcludeUsersPosts") {
			test("should return posts excluding specified users") {
				val excludedUserIds = listOf(1L, 2L)
				val pageable = PageRequest.of(0, 10)
				val entities =
					listOf(
						PostEntity("Post 1", "Sub1", "Content1", 3L).apply { id = 1L },
						PostEntity("Post 2", "Sub2", "Content2", 4L).apply { id = 2L }
					)
				val posts =
					listOf(
						Post(1L, "Post 1", "Sub1", "Content1", 3L),
						Post(2L, "Post 2", "Sub2", "Content2", 4L)
					)
				val entityPage = PageImpl(entities, pageable, 2)

				every { postJpaRepository.findExcludeUsersPosts(excludedUserIds, pageable) } returns entityPage
				entities.forEachIndexed { index, entity ->
					every { postMapper.toDomain(entity) } returns posts[index]
				}

				val result = adapter.findExcludeUsersPosts(excludedUserIds, pageable)

				result.content shouldHaveSize 2
				result.content shouldBe posts
				verify(exactly = 1) { postJpaRepository.findExcludeUsersPosts(excludedUserIds, pageable) }
			}
		}

		context("deleteById") {
			test("should delete post by id") {
				val postId = 100L

				every { postJpaRepository.deleteById(postId) } just Runs

				adapter.deleteById(postId)

				verify(exactly = 1) { postJpaRepository.deleteById(postId) }
			}

			test("should not delete when post not found") {
				val postId = 999L

				every { postJpaRepository.deleteById(postId) } just Runs

				adapter.deleteById(postId)

				verify(exactly = 1) { postJpaRepository.deleteById(postId) }
			}
		}

		context("deleteByUserId") {
			test("should soft delete all posts for user") {
				val userId = 1L

				every { postJpaRepository.deleteByUserId(userId) } just Runs

				adapter.deleteByUserId(userId)

				verify(exactly = 1) { postJpaRepository.deleteByUserId(userId) }
			}

			test("should handle when user has no posts") {
				val userId = 999L

				every { postJpaRepository.deleteByUserId(userId) } just Runs

				adapter.deleteByUserId(userId)

				verify(exactly = 1) { postJpaRepository.deleteByUserId(userId) }
			}
		}

		context("hardDeleteById") {
			test("should physically delete post by id") {
				val postId = 100L
				every { postJpaRepository.hardDeleteById(postId) } returns 1

				adapter.hardDeleteById(postId)

				verify(exactly = 1) { postJpaRepository.hardDeleteById(postId) }
			}
		}

		context("hardDeleteByUserId") {
			test("should physically delete all posts for user") {
				val userId = 1L
				every { postJpaRepository.hardDeleteByUserId(userId) } returns 1

				adapter.hardDeleteByUserId(userId)

				verify(exactly = 1) { postJpaRepository.hardDeleteByUserId(userId) }
			}
		}

		context("existsById") {
			test("should return true when post exists") {
				val postId = 100L
				every { postJpaRepository.existsById(postId) } returns true

				val result = adapter.existsById(postId)

				result shouldBe true
				verify(exactly = 1) { postJpaRepository.existsById(postId) }
			}

			test("should return false when post does not exist") {
				val postId = 999L
				every { postJpaRepository.existsById(postId) } returns false

				val result = adapter.existsById(postId)

				result shouldBe false
				verify(exactly = 1) { postJpaRepository.existsById(postId) }
			}
		}
	})
