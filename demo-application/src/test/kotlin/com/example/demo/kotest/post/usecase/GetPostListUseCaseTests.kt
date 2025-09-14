package com.example.demo.kotest.post.usecase

import com.example.demo.post.model.Post
import com.example.demo.post.port.input.GetPostListInput
import com.example.demo.post.service.PostService
import com.example.demo.post.usecase.GetPostListUseCase
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime

@ActiveProfiles("test")
@Tags("kotest-unit-test")
class GetPostListUseCaseTests :
	BehaviorSpec({
		val postService = mockk<PostService>()
		val getPostListUseCase = GetPostListUseCase(postService)

		val testPosts =
			(1..3).map { id ->
				Post(
					id = id.toLong(),
					userId = 100L,
					title = "Test Title $id",
					subTitle = "Test SubTitle $id",
					content = "Test Content $id",
					createdDt = LocalDateTime.now(),
					updatedDt = LocalDateTime.now()
				)
			}

		Given("Get post list") {

			When("Get paginated post list") {
				val pageable = PageRequest.of(0, 10)
				val input = GetPostListInput(pageable = pageable)
				val postPage = PageImpl(testPosts, pageable, testPosts.size.toLong())

				every { postService.findAll(pageable) } returns postPage

				val result = getPostListUseCase.execute(input)

				Then("PostListOutput is returned successfully") {
					result.shouldNotBeNull().also { output ->
						output.posts.content shouldHaveSize 3
						output.posts.content.forEachIndexed { index, post ->
							post.id shouldBe (index + 1).toLong()
							post.title shouldBe "Test Title ${index + 1}"
							post.subTitle shouldBe "Test SubTitle ${index + 1}"
						}
					}
				}
			}

			When("Get empty post list") {
				val pageable = PageRequest.of(0, 10)
				val input = GetPostListInput(pageable = pageable)
				val emptyPage = PageImpl<Post>(emptyList(), pageable, 0)

				every { postService.findAll(pageable) } returns emptyPage

				val result = getPostListUseCase.execute(input)

				Then("Empty PostListOutput is returned") {
					result.shouldNotBeNull().also { output ->
						output.posts.content shouldHaveSize 0
						output.posts.content.isEmpty() shouldBe true
					}
				}
			}

			When("Get specific page") {
				val pageable = PageRequest.of(1, 2)
				val input = GetPostListInput(pageable = pageable)
				val secondPagePosts = testPosts.subList(2, 3)
				val postPage = PageImpl(secondPagePosts, pageable, testPosts.size.toLong())

				every { postService.findAll(pageable) } returns postPage

				val result = getPostListUseCase.execute(input)

				Then("Requested page data is returned") {
					result.shouldNotBeNull {
						posts.content shouldHaveSize 1
						posts.content.first().title shouldBe "Test Title 3"
					}
				}
			}
		}
	})
