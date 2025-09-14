package com.example.demo.kotest.post.usecase

import com.example.demo.post.model.Post
import com.example.demo.post.port.input.GetUserPostsInput
import com.example.demo.post.service.PostService
import com.example.demo.post.usecase.GetUserPostsUseCase
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime

@ActiveProfiles("test")
@Tags("kotest-unit-test")
class GetUserPostsUseCaseTests :
	BehaviorSpec({
		val postService = mockk<PostService>()
		val getUserPostsUseCase = GetUserPostsUseCase(postService)

		val userPosts =
			(1..3).map { id ->
				Post(
					id = id.toLong(),
					userId = 100L,
					title = "User Post $id",
					subTitle = "User SubTitle $id",
					content = "User Content $id",
					createdDt = LocalDateTime.now(),
					updatedDt = LocalDateTime.now()
				)
			}

		Given("Get user's post list") {

			When("Get posts of user with posts") {
				val userId = 100L
				val pageable = PageRequest.of(0, 10)
				val input = GetUserPostsInput(userId = userId, pageable = pageable)
				val page = PageImpl(userPosts, pageable, userPosts.size.toLong())

				every { postService.findAllByUserId(userId, pageable) } returns page

				val result = getUserPostsUseCase.execute(input)

				Then("All user's posts are returned") {
					result.posts.content shouldHaveSize 3
					result.posts.content.forEachIndexed { index, post ->
						post.id shouldBe userPosts[index].id
						post.userId shouldBe userId
						post.title shouldBe "User Post ${index + 1}"
						post.subTitle shouldBe "User SubTitle ${index + 1}"
					}
				}
			}

			When("Get posts of user without posts") {
				val userId = 200L
				val pageable = PageRequest.of(0, 10)
				val input = GetUserPostsInput(userId = userId, pageable = pageable)
				val emptyPage = PageImpl(emptyList<Post>(), pageable, 0)

				every { postService.findAllByUserId(userId, pageable) } returns emptyPage

				val result = getUserPostsUseCase.execute(input)

				Then("Empty list is returned") {
					result.posts.content.shouldBeEmpty()
				}
			}

			When("Get posts with single post") {
				val userId = 100L
				val pageable = PageRequest.of(0, 10)
				val input = GetUserPostsInput(userId = userId, pageable = pageable)
				val singlePost = listOf(userPosts[0])
				val page = PageImpl(singlePost, pageable, 1)

				every { postService.findAllByUserId(userId, pageable) } returns page

				val result = getUserPostsUseCase.execute(input)

				Then("Single post is returned") {
					result.posts.content shouldHaveSize 1
					result.posts.content[0].title shouldBe "User Post 1"
				}
			}
		}
	})
