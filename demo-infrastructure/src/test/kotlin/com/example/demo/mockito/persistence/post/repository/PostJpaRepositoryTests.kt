package com.example.demo.mockito.persistence.post.repository

import com.example.demo.persistence.config.JpaAuditConfig
import com.example.demo.persistence.config.QueryDslConfig
import com.example.demo.persistence.post.entity.PostEntity
import com.example.demo.persistence.post.repository.PostJpaRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime

@ActiveProfiles("test")
@Tag("mockito-integration-test")
@DisplayName("Mockito Integration - Post JPA Repository Test")
@DataJpaTest
@Import(value = [QueryDslConfig::class, JpaAuditConfig::class])
class PostJpaRepositoryTests
	@Autowired
	constructor(
		private val postJpaRepository: PostJpaRepository
	) {
		@Nested
		@DisplayName("Basic CRUD Operations")
		inner class BasicCrudTests {
			@Test
			@DisplayName("should save and retrieve a post by ID")
			fun saveAndFindById() {
				val post =
					PostEntity(
						title = "Test Title",
						subTitle = "Test Subtitle",
						content = "Test Content",
						userId = 1L
					)

				val savedPost = postJpaRepository.save(post)
				val foundPost = postJpaRepository.findById(savedPost.id)

				assertTrue(foundPost.isPresent)
				assertEquals("Test Title", foundPost.get().title)
				assertEquals("Test Subtitle", foundPost.get().subTitle)
				assertEquals("Test Content", foundPost.get().content)
				assertEquals(1L, foundPost.get().userId)
			}

			@Test
			@DisplayName("should update existing post")
			fun updatePost() {
				val post =
					PostEntity(
						title = "Original Title",
						subTitle = "Original Subtitle",
						content = "Original Content",
						userId = 1L
					)
				val savedPost = postJpaRepository.save(post)

				val postToUpdate = postJpaRepository.findById(savedPost.id).get()
				postToUpdate.title = "Updated Title"
				postToUpdate.subTitle = "Updated Subtitle"
				postToUpdate.content = "Updated Content"
				postJpaRepository.save(postToUpdate)

				val updatedPost = postJpaRepository.findById(savedPost.id).get()
				assertEquals("Updated Title", updatedPost.title)
				assertEquals("Updated Subtitle", updatedPost.subTitle)
				assertEquals("Updated Content", updatedPost.content)
				assertEquals(1L, updatedPost.userId)
			}

			@Test
			@DisplayName("should delete post by ID")
			fun deleteById() {
				val post =
					PostEntity(
						title = "To Delete",
						subTitle = "To Delete Sub",
						content = "To Delete Content",
						userId = 1L
					)
				val savedPost = postJpaRepository.save(post)

				postJpaRepository.deleteById(savedPost.id)

				val foundPost = postJpaRepository.findById(savedPost.id)
				assertFalse(foundPost.isPresent)
			}

			@Test
			@DisplayName("should check if post exists by ID")
			fun existsById() {
				val post =
					PostEntity(
						title = "Exists Test",
						subTitle = "Exists Sub",
						content = "Exists Content",
						userId = 1L
					)
				val savedPost = postJpaRepository.save(post)

				assertTrue(postJpaRepository.existsById(savedPost.id))
				assertFalse(postJpaRepository.existsById(99999L))
			}

			@Test
			@DisplayName("should count total posts")
			fun countPosts() {
				val posts =
					(1..5).map { i ->
						PostEntity(
							title = "Post $i",
							subTitle = "Subtitle $i",
							content = "Content $i",
							userId = 1L
						)
					}
				postJpaRepository.saveAll(posts)

				val count = postJpaRepository.count()

				assertEquals(5, count)
			}
		}

		@Nested
		@DisplayName("findByUserId Tests")
		inner class FindByUserIdTests {
			@Test
			@DisplayName("should find posts by user ID with pagination")
			fun findByUserIdWithPagination() {
				val user1Posts =
					(1..5).map { i ->
						PostEntity(
							title = "User1 Post $i",
							subTitle = "Subtitle $i",
							content = "Content $i",
							userId = 1L
						)
					}
				val user2Posts =
					(1..3).map { i ->
						PostEntity(
							title = "User2 Post $i",
							subTitle = "Subtitle $i",
							content = "Content $i",
							userId = 2L
						)
					}

				postJpaRepository.saveAll(user1Posts + user2Posts)

				val pageable = PageRequest.of(0, 3)
				val page = postJpaRepository.findByUserId(1L, pageable)

				assertEquals(3, page.content.size)
				assertEquals(5, page.totalElements)
				assertEquals(2, page.totalPages)
				assertTrue(page.content.all { it.userId == 1L })
			}

			@Test
			@DisplayName("should return empty page for non-existent user")
			fun findByUserIdEmpty() {
				val posts =
					(1..3).map { i ->
						PostEntity(
							title = "Post $i",
							subTitle = "Subtitle $i",
							content = "Content $i",
							userId = 1L
						)
					}
				postJpaRepository.saveAll(posts)

				val pageable = PageRequest.of(0, 10)
				val page = postJpaRepository.findByUserId(999L, pageable)

				assertEquals(0, page.content.size)
				assertEquals(0, page.totalElements)
			}

			@Test
			@DisplayName("should find all posts by user ID without pagination")
			fun findAllByUserId() {
				val user1Posts =
					(1..3).map { i ->
						PostEntity(
							title = "User1 Post $i",
							subTitle = "Subtitle $i",
							content = "Content $i",
							userId = 1L
						)
					}
				val user2Posts =
					(1..2).map { i ->
						PostEntity(
							title = "User2 Post $i",
							subTitle = "Subtitle $i",
							content = "Content $i",
							userId = 2L
						)
					}

				postJpaRepository.saveAll(user1Posts + user2Posts)

				val user1Results = postJpaRepository.findAllByUserId(1L)
				val user2Results = postJpaRepository.findAllByUserId(2L)
				val user3Results = postJpaRepository.findAllByUserId(999L)

				assertEquals(3, user1Results.size)
				assertEquals(2, user2Results.size)
				assertEquals(0, user3Results.size)
				assertTrue(user1Results.all { it.userId == 1L })
				assertTrue(user2Results.all { it.userId == 2L })
			}
		}

		@Nested
		@DisplayName("deleteByUserId Tests")
		inner class DeleteByUserIdTests {
			@Test
			@DisplayName("should delete all posts by user ID")
			fun deleteByUserId() {
				val user1Posts =
					(1..3).map { i ->
						PostEntity(
							title = "User1 Post $i",
							subTitle = "Subtitle $i",
							content = "Content $i",
							userId = 1L
						)
					}
				val user2Posts =
					(1..2).map { i ->
						PostEntity(
							title = "User2 Post $i",
							subTitle = "Subtitle $i",
							content = "Content $i",
							userId = 2L
						)
					}

				postJpaRepository.saveAll(user1Posts + user2Posts)

				postJpaRepository.deleteByUserId(1L)

				val remainingPosts = postJpaRepository.findAll()
				assertEquals(2, remainingPosts.size)
				assertTrue(remainingPosts.all { it.userId == 2L })
			}

			@Test
			@DisplayName("should handle delete for non-existent user")
			fun deleteByNonExistentUserId() {
				val posts =
					(1..3).map { i ->
						PostEntity(
							title = "Post $i",
							subTitle = "Subtitle $i",
							content = "Content $i",
							userId = 1L
						)
					}
				postJpaRepository.saveAll(posts)

				postJpaRepository.deleteByUserId(999L)

				val remainingPosts = postJpaRepository.findAll()
				assertEquals(3, remainingPosts.size)
			}
		}

		@Nested
		@DisplayName("findExcludeUsersPosts Tests")
		inner class FindExcludeUsersPostsTests {
			@Test
			@DisplayName("should exclude specified users from results")
			fun excludeSpecifiedUsers() {
				val posts =
					listOf(
						PostEntity("Post 1", "Sub 1", "Content 1", 1L),
						PostEntity("Post 2", "Sub 2", "Content 2", 2L),
						PostEntity("Post 3", "Sub 3", "Content 3", 3L),
						PostEntity("Post 4", "Sub 4", "Content 4", 1L),
						PostEntity("Post 5", "Sub 5", "Content 5", 4L)
					)
				postJpaRepository.saveAll(posts)

				val excludedUserIds = listOf(1L, 2L)
				val pageable = PageRequest.of(0, 10)
				val page = postJpaRepository.findExcludeUsersPosts(excludedUserIds, pageable)

				assertEquals(2, page.content.size)
				assertFalse(page.content.any { it.userId == 1L })
				assertFalse(page.content.any { it.userId == 2L })
				assertTrue(page.content.any { it.userId == 3L })
				assertTrue(page.content.any { it.userId == 4L })
			}

			@Test
			@DisplayName("should return all posts when exclude list is empty")
			fun excludeEmptyList() {
				val posts =
					listOf(
						PostEntity("Post 1", "Sub 1", "Content 1", 1L),
						PostEntity("Post 2", "Sub 2", "Content 2", 2L),
						PostEntity("Post 3", "Sub 3", "Content 3", 3L)
					)
				postJpaRepository.saveAll(posts)

				val pageable = PageRequest.of(0, 10)
				val page = postJpaRepository.findExcludeUsersPosts(emptyList(), pageable)

				assertEquals(3, page.content.size)
				assertEquals(3, page.totalElements)
			}

			@Test
			@DisplayName("should not include soft-deleted posts")
			fun excludeSoftDeletedPosts() {
				val posts =
					listOf(
						PostEntity("Active 1", "Sub 1", "Content 1", 1L),
						PostEntity("Deleted", "Sub 2", "Content 2", 2L).apply {
							deletedDt = LocalDateTime.now()
						},
						PostEntity("Active 2", "Sub 3", "Content 3", 3L)
					)
				postJpaRepository.saveAll(posts)

				val pageable = PageRequest.of(0, 10)
				val page = postJpaRepository.findExcludeUsersPosts(emptyList(), pageable)

				assertEquals(2, page.content.size)
				assertTrue(page.content.all { it.deletedDt == null })
			}

			@Test
			@DisplayName("should handle pagination correctly")
			fun excludeWithPagination() {
				val posts =
					(1..10).map { i ->
						PostEntity(
							title = "Post $i",
							subTitle = "Sub $i",
							content = "Content $i",
							userId = if (i <= 3) 1L else 2L
						)
					}
				postJpaRepository.saveAll(posts)

				val excludedUserIds = listOf(1L)
				val pageable = PageRequest.of(0, 3)
				val page = postJpaRepository.findExcludeUsersPosts(excludedUserIds, pageable)

				assertEquals(3, page.content.size)
				assertEquals(7, page.totalElements)
				assertEquals(3, page.totalPages)
				assertTrue(page.content.all { it.userId == 2L })
			}
		}

		@Nested
		@DisplayName("Pagination and Sorting Tests")
		inner class PaginationAndSortingTests {
			@Test
			@DisplayName("should support pagination")
			fun pagination() {
				val posts =
					(1..15).map { i ->
						PostEntity(
							title = "Post $i",
							subTitle = "Subtitle $i",
							content = "Content $i",
							userId = 1L
						)
					}
				postJpaRepository.saveAll(posts)

				val firstPage = postJpaRepository.findAll(PageRequest.of(0, 5))
				val secondPage = postJpaRepository.findAll(PageRequest.of(1, 5))
				val thirdPage = postJpaRepository.findAll(PageRequest.of(2, 5))

				assertEquals(5, firstPage.content.size)
				assertEquals(5, secondPage.content.size)
				assertEquals(5, thirdPage.content.size)
				assertEquals(15, firstPage.totalElements)
				assertEquals(3, firstPage.totalPages)
			}

			@Test
			@DisplayName("should support sorting by title")
			fun sortingByTitle() {
				val posts =
					listOf(
						PostEntity("C Post", "Sub", "Content", 1L),
						PostEntity("A Post", "Sub", "Content", 1L),
						PostEntity("B Post", "Sub", "Content", 1L)
					)
				postJpaRepository.saveAll(posts)

				val sortedAsc = postJpaRepository.findAll(Sort.by(Sort.Direction.ASC, "title"))
				val sortedDesc = postJpaRepository.findAll(Sort.by(Sort.Direction.DESC, "title"))

				assertEquals("A Post", sortedAsc[0].title)
				assertEquals("B Post", sortedAsc[1].title)
				assertEquals("C Post", sortedAsc[2].title)

				assertEquals("C Post", sortedDesc[0].title)
				assertEquals("B Post", sortedDesc[1].title)
				assertEquals("A Post", sortedDesc[2].title)
			}

			@Test
			@DisplayName("should support sorting by multiple fields")
			fun sortingByMultipleFields() {
				val posts =
					listOf(
						PostEntity("A Post", "Z Sub", "Content", 1L),
						PostEntity("A Post", "A Sub", "Content", 2L),
						PostEntity("B Post", "Sub", "Content", 3L)
					)
				postJpaRepository.saveAll(posts)

				val sorted =
					postJpaRepository.findAll(
						Sort.by(
							Sort.Order.asc("title"),
							Sort.Order.asc("subTitle")
						)
					)

				assertEquals("A Post", sorted[0].title)
				assertEquals("A Sub", sorted[0].subTitle)
				assertEquals("A Post", sorted[1].title)
				assertEquals("Z Sub", sorted[1].subTitle)
				assertEquals("B Post", sorted[2].title)
			}
		}

		@Nested
		@DisplayName("Soft Delete Tests")
		inner class SoftDeleteTests {
			@Test
			@DisplayName("should handle soft delete")
			fun softDelete() {
				val post =
					PostEntity(
						title = "Soft Delete Test",
						subTitle = "Sub",
						content = "Content",
						userId = 1L
					)
				val savedPost = postJpaRepository.save(post)

				val postToDelete = postJpaRepository.findById(savedPost.id).get()
				postToDelete.deletedDt = java.time.LocalDateTime.now()
				postJpaRepository.save(postToDelete)

				val deletedPost = postJpaRepository.findById(savedPost.id).get()
				assertNotNull(deletedPost.deletedDt)
				assertTrue(deletedPost.deletedDt != null)
			}

			@Test
			@DisplayName("should filter out soft deleted posts in custom queries")
			fun filterSoftDeletedInCustomQuery() {
				val activePost = PostEntity("Active", "Sub", "Content", 1L)
				val deletedPost =
					PostEntity("Deleted", "Sub", "Content", 1L).apply {
						deletedDt = java.time.LocalDateTime.now()
					}

				postJpaRepository.saveAll(listOf(activePost, deletedPost))

				val results = postJpaRepository.findAllByUserId(1L)

				assertEquals(1, results.size)
				assertTrue(results.all { it.deletedDt == null })
			}
		}
	}
