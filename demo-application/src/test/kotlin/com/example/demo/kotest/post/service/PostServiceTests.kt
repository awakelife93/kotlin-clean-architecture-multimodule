package com.example.demo.kotest.post.service

import com.example.demo.post.model.Post
import com.example.demo.post.port.PostPort
import com.example.demo.post.port.input.CreatePostInput
import com.example.demo.post.service.PostService
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime

@ActiveProfiles("test")
@Tags("kotest-unit-test")
class PostServiceTests :
	BehaviorSpec({

		val postPort = mockk<PostPort>(relaxed = true)
		val postService = PostService(postPort)

		fun createTestPost(
			id: Long = 1L,
			userId: Long = 100L,
			title: String = "Test Title",
			subTitle: String = "Test SubTitle",
			content: String = "Test Content",
			deletedDt: LocalDateTime? = null
		) = Post(
			id = id,
			userId = userId,
			title = title,
			subTitle = subTitle,
			content = content,
			createdDt = LocalDateTime.now(),
			updatedDt = LocalDateTime.now(),
			deletedDt = deletedDt
		)

		beforeTest {
			clearAllMocks()
		}

		Given("Find post") {
			When("Find post by ID successfully") {
				Then("Post is returned successfully") {
					val post = createTestPost()
					every { postPort.findOneById(1L) } returns post

					val result = postService.findOneById(1L)

					result shouldNotBeNull {
						id shouldBe 1L
						title shouldBe "Test Title"
						subTitle shouldBe "Test SubTitle"
						content shouldBe "Test Content"
						userId shouldBe 100L
					}

					verify(exactly = 1) { postPort.findOneById(1L) }
				}
			}

			When("Find non-existent post") {
				Then("Null is returned") {
					every { postPort.findOneById(999L) } returns null

					postService.findOneById(999L).shouldBeNull()

					verify(exactly = 1) { postPort.findOneById(999L) }
				}
			}

			When("Find all posts with pagination") {
				Then("Paginated post list is returned") {
					val pageable = PageRequest.of(0, 10)
					val posts = listOf(createTestPost())
					every { postPort.findAll(pageable) } returns PageImpl(posts, pageable, 1)

					val result = postService.findAll(pageable)

					result.content shouldHaveSize 1
					result.totalElements shouldBe 1

					verify(exactly = 1) { postPort.findAll(pageable) }
				}
			}

			When("Find posts by user ID") {
				Then("User's post list is returned") {
					val userPosts = listOf(createTestPost())
					every { postPort.findAllByUserId(100L) } returns userPosts

					val result = postService.findAllByUserId(100L)

					result shouldHaveSize 1
					result[0].userId shouldBe 100L

					verify(exactly = 1) { postPort.findAllByUserId(100L) }
				}
			}
		}

		Given("Create post") {
			When("Create new post successfully") {
				Then("Saved post is returned") {
					val input =
						CreatePostInput(
							title = "New Post",
							subTitle = "Test SubTitle",
							content = "Test Content",
							userId = 100L
						)
					val savedPost = createTestPost(id = 2L, title = "New Post")
					every { postPort.save(any<Post>()) } returns savedPost

					val result = postService.createPost(input)

					result shouldNotBeNull {
						id shouldBe 2L
						title shouldBe "New Post"
					}

					verify(exactly = 1) {
						postPort.save(
							match {
								it.title == "New Post" &&
									it.subTitle == "Test SubTitle" &&
									it.content == "Test Content" &&
									it.userId == 100L
							}
						)
					}
				}
			}
		}

		Given("Update post") {
			When("Update post successfully") {
				Then("Updated post is returned") {
					val updatedPost = createTestPost(title = "Updated Title")
					every { postPort.save(updatedPost) } returns updatedPost

					val result = postService.updatePost(updatedPost)

					result shouldNotBeNull {
						id shouldBe 1L
						title shouldBe "Updated Title"
					}

					verify(exactly = 1) { postPort.save(updatedPost) }
				}
			}
		}

		Given("Delete post") {
			When("Delete post successfully") {
				Then("Delete method is called") {
					val post = createTestPost()
					every { postPort.findOneById(1L) } returns post
					every { postPort.deleteById(1L) } returns Unit

					shouldNotThrow<Exception> {
						postService.deletePost(1L)
					}

					verify(exactly = 1) {
						postPort.findOneById(1L)
						postPort.deleteById(1L)
					}
				}
			}

			When("Try to delete non-existent post") {
				Then("Nothing happens") {
					every { postPort.findOneById(999L) } returns null

					shouldNotThrow<Exception> {
						postService.deletePost(999L)
					}

					verify(exactly = 1) { postPort.findOneById(999L) }
					verify(exactly = 0) { postPort.deleteById(999L) }
				}
			}

			When("Delete all posts by user ID") {
				Then("All user's posts are deleted") {
					every { postPort.deleteByUserId(100L) } returns Unit

					shouldNotThrow<Exception> {
						postService.deletePostsByUserId(100L)
					}

					verify(exactly = 1) { postPort.deleteByUserId(100L) }
				}
			}
		}

		Given("Check post existence") {
			When("Post exists") {
				Then("True is returned") {
					every { postPort.existsById(1L) } returns true

					postService.existsById(1L) shouldBe true

					verify(exactly = 1) { postPort.existsById(1L) }
				}
			}

			When("Post does not exist") {
				Then("False is returned") {
					every { postPort.existsById(999L) } returns false

					postService.existsById(999L) shouldBe false

					verify(exactly = 1) { postPort.existsById(999L) }
				}
			}
		}

		Given("Count user's posts") {
			When("User has posts") {
				Then("Post count is returned") {
					val userPosts = (1L..3L).map { createTestPost(id = it) }
					every { postPort.findAllByUserId(100L) } returns userPosts

					postService.countByUserId(100L) shouldBe 3L

					verify(exactly = 1) { postPort.findAllByUserId(100L) }
				}
			}

			When("User has no posts") {
				Then("Zero is returned") {
					every { postPort.findAllByUserId(200L) } returns emptyList()

					postService.countByUserId(200L) shouldBe 0L

					verify(exactly = 1) { postPort.findAllByUserId(200L) }
				}
			}
		}
	})
