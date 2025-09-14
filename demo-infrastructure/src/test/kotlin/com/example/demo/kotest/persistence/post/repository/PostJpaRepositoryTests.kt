package com.example.demo.kotest.persistence.post.repository

import com.example.demo.persistence.config.JpaAuditConfig
import com.example.demo.persistence.config.QueryDslConfig
import com.example.demo.persistence.post.entity.PostEntity
import com.example.demo.persistence.post.repository.PostJpaRepository
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime

@ActiveProfiles("test")
@Tags("kotest-integration-test")
@DataJpaTest
@Import(value = [QueryDslConfig::class, JpaAuditConfig::class])
class PostJpaRepositoryTests
	@Autowired
	constructor(
		private val postJpaRepository: PostJpaRepository
	) : DescribeSpec({

			describe("PostJpaRepository Basic CRUD") {

				describe("save and findById") {
					it("should save and retrieve a post") {
						val post =
							PostEntity(
								title = "Test Title",
								subTitle = "Test Subtitle",
								content = "Test Content",
								userId = 1L
							)

						val savedPost = postJpaRepository.save(post)
						val foundPost = postJpaRepository.findById(savedPost.id)

						foundPost.isPresent shouldBe true
						foundPost.get().title shouldBe "Test Title"
						foundPost.get().subTitle shouldBe "Test Subtitle"
						foundPost.get().content shouldBe "Test Content"
						foundPost.get().userId shouldBe 1L
					}
				}

				describe("update") {
					it("should update existing post") {
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
						val updatedPost = postJpaRepository.save(postToUpdate)

						val foundPost = postJpaRepository.findById(updatedPost.id).get()
						foundPost.title shouldBe "Updated Title"
						foundPost.subTitle shouldBe "Updated Subtitle"
						foundPost.content shouldBe "Updated Content"
						foundPost.userId shouldBe 1L
					}
				}

				describe("delete") {
					it("should delete post") {
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
						foundPost.isPresent shouldBe false
					}
				}

				describe("existsById") {
					it("should check if post exists") {
						val post =
							PostEntity(
								title = "Exists Test",
								subTitle = "Exists Sub",
								content = "Exists Content",
								userId = 1L
							)
						val savedPost = postJpaRepository.save(post)

						postJpaRepository.existsById(savedPost.id) shouldBe true
						postJpaRepository.existsById(99999L) shouldBe false
					}
				}
			}

			describe("PostJpaRepository Custom Queries") {

				describe("findByUserId with Pageable") {
					it("should find posts by user id with pagination") {
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

						page.content shouldHaveSize 3
						page.totalElements shouldBe 5
						page.totalPages shouldBe 2
						page.content.all { it.userId == 1L } shouldBe true
					}

					it("should handle empty result for non-existent user") {
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

						page.content shouldHaveSize 0
						page.totalElements shouldBe 0
					}
				}

				describe("findAllByUserId") {
					it("should find all posts by user id") {
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

						user1Results shouldHaveSize 3
						user2Results shouldHaveSize 2
						user1Results.all { it.userId == 1L } shouldBe true
						user2Results.all { it.userId == 2L } shouldBe true
					}
				}

				describe("deleteByUserId") {
					it("should delete all posts by user id") {
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
						remainingPosts shouldHaveSize 2
						remainingPosts.all { it.userId == 2L } shouldBe true
					}
				}

				describe("findExcludeUsersPosts") {
					it("should exclude specified users from results") {
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

						page.content shouldHaveSize 2
						page.content.map { it.userId } shouldNotContain 1L
						page.content.map { it.userId } shouldNotContain 2L
						page.content.map { it.userId } shouldContain 3L
						page.content.map { it.userId } shouldContain 4L
					}

					it("should return all posts when exclude list is empty") {
						val posts =
							listOf(
								PostEntity("Post 1", "Sub 1", "Content 1", 1L),
								PostEntity("Post 2", "Sub 2", "Content 2", 2L),
								PostEntity("Post 3", "Sub 3", "Content 3", 3L)
							)
						postJpaRepository.saveAll(posts)

						val pageable = PageRequest.of(0, 10)
						val page = postJpaRepository.findExcludeUsersPosts(emptyList(), pageable)

						page.content shouldHaveSize 3
						page.totalElements shouldBe 3
					}

					it("should not include soft-deleted posts") {
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

						page.content shouldHaveSize 2
						page.content.all { it.deletedDt == null } shouldBe true
					}
				}
			}

			describe("PostJpaRepository Pagination and Sorting") {

				it("should support pagination") {
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

					firstPage.content shouldHaveSize 5
					secondPage.content shouldHaveSize 5
					thirdPage.content shouldHaveSize 5
					firstPage.totalElements shouldBe 15
					firstPage.totalPages shouldBe 3
				}

				it("should support sorting") {
					val posts =
						listOf(
							PostEntity("C Post", "Sub", "Content", 1L),
							PostEntity("A Post", "Sub", "Content", 1L),
							PostEntity("B Post", "Sub", "Content", 1L)
						)
					postJpaRepository.saveAll(posts)

					val sortedAsc = postJpaRepository.findAll(Sort.by(Sort.Direction.ASC, "title"))
					val sortedDesc = postJpaRepository.findAll(Sort.by(Sort.Direction.DESC, "title"))

					sortedAsc[0].title shouldBe "A Post"
					sortedAsc[1].title shouldBe "B Post"
					sortedAsc[2].title shouldBe "C Post"

					sortedDesc[0].title shouldBe "C Post"
					sortedDesc[1].title shouldBe "B Post"
					sortedDesc[2].title shouldBe "A Post"
				}
			}

			describe("PostJpaRepository Soft Delete") {

				it("should handle soft delete") {
					val post =
						PostEntity(
							title = "Soft Delete Test",
							subTitle = "Sub",
							content = "Content",
							userId = 1L
						)
					val savedPost = postJpaRepository.save(post)

					val postToDelete = postJpaRepository.findById(savedPost.id).get()
					postToDelete.deletedDt = LocalDateTime.now()
					val deletedPost = postJpaRepository.save(postToDelete)

					val foundPost = postJpaRepository.findById(deletedPost.id).get()
					foundPost.deletedDt shouldNotBe null
					(foundPost.deletedDt != null) shouldBe true
				}

				it("should filter out soft deleted posts automatically") {
					val activePost = PostEntity("Active", "Sub", "Content", 1L)
					val deletedPost =
						PostEntity("Deleted", "Sub", "Content", 1L).apply {
							deletedDt = LocalDateTime.now()
						}

					postJpaRepository.saveAll(listOf(activePost, deletedPost))

					val results = postJpaRepository.findAllByUserId(1L)

					results shouldHaveSize 1
					results.all { it.deletedDt == null } shouldBe true
				}
			}
		})
