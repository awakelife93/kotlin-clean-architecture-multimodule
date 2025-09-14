package com.example.demo.kotest.user.service

import com.example.demo.persistence.auth.provider.TokenProvider
import com.example.demo.post.service.PostService
import com.example.demo.user.exception.UserNotFoundException
import com.example.demo.user.service.UserDeletionService
import com.example.demo.user.service.UserService
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@Tags("kotest-unit-test")
class UserDeletionServiceTests :
	BehaviorSpec({

		Given("Delete user with related data") {

			When("User exists with related data") {
				Then("Should soft delete user and all related data") {
					val userService = mockk<UserService>()
					val postService = mockk<PostService>()
					val tokenProvider = mockk<TokenProvider>()
					val userDeletionService = UserDeletionService(userService, postService, tokenProvider)

					val userId = 1L

					every { userService.existsById(userId) } returns true
					justRun { postService.deletePostsByUserId(userId) }
					justRun { tokenProvider.deleteRefreshToken(userId) }
					justRun { userService.deleteByIdWithoutValidation(userId) }

					shouldNotThrow<Exception> {
						userDeletionService.deleteUserWithRelatedData(userId)
					}

					verifyOrder {
						userService.existsById(userId)
						postService.deletePostsByUserId(userId)
						tokenProvider.deleteRefreshToken(userId)
						userService.deleteByIdWithoutValidation(userId)
					}
				}
			}

			When("User does not exist") {
				Then("Should throw UserNotFoundException") {
					val userService = mockk<UserService>()
					val postService = mockk<PostService>()
					val tokenProvider = mockk<TokenProvider>()
					val userDeletionService = UserDeletionService(userService, postService, tokenProvider)

					val userId = 999L
					every { userService.existsById(userId) } returns false

					val exception =
						shouldThrowExactly<UserNotFoundException> {
							userDeletionService.deleteUserWithRelatedData(userId)
						}

					exception.message shouldBe "User Not Found userId = $userId"

					verify(exactly = 1) { userService.existsById(userId) }
					verify(exactly = 0) {
						postService.deletePostsByUserId(any<Long>())
						tokenProvider.deleteRefreshToken(any<Long>())
						userService.deleteByIdWithoutValidation(any<Long>())
					}
				}
			}

			When("User has no posts") {
				Then("Should handle gracefully") {
					val userService = mockk<UserService>()
					val postService = mockk<PostService>()
					val tokenProvider = mockk<TokenProvider>()
					val userDeletionService = UserDeletionService(userService, postService, tokenProvider)

					val userId = 2L

					every { userService.existsById(userId) } returns true
					justRun { postService.deletePostsByUserId(userId) }
					justRun { tokenProvider.deleteRefreshToken(userId) }
					justRun { userService.deleteByIdWithoutValidation(userId) }

					shouldNotThrow<Exception> {
						userDeletionService.deleteUserWithRelatedData(userId)
					}

					verify(exactly = 1) {
						userService.existsById(userId)
						postService.deletePostsByUserId(userId)
						tokenProvider.deleteRefreshToken(userId)
						userService.deleteByIdWithoutValidation(userId)
					}
				}
			}
		}

		Given("Can delete user") {

			When("User exists") {
				Then("Should return true") {
					val userService = mockk<UserService>()
					val postService = mockk<PostService>()
					val tokenProvider = mockk<TokenProvider>()
					val userDeletionService = UserDeletionService(userService, postService, tokenProvider)

					val userId = 1L
					every { userService.existsById(userId) } returns true

					val result = userDeletionService.canDeleteUser(userId)

					result shouldBe true
					verify(exactly = 1) { userService.existsById(userId) }
				}
			}

			When("User does not exist") {
				Then("Should return false") {
					val userService = mockk<UserService>()
					val postService = mockk<PostService>()
					val tokenProvider = mockk<TokenProvider>()
					val userDeletionService = UserDeletionService(userService, postService, tokenProvider)

					val userId = 999L
					every { userService.existsById(userId) } returns false

					val result = userDeletionService.canDeleteUser(userId)

					result shouldBe false
					verify(exactly = 1) { userService.existsById(userId) }
				}
			}
		}

		Given("Get user deletion summary") {

			When("User has posts") {
				Then("Should return correct deletion summary") {
					val userService = mockk<UserService>()
					val postService = mockk<PostService>()
					val tokenProvider = mockk<TokenProvider>()
					val userDeletionService = UserDeletionService(userService, postService, tokenProvider)

					val userId = 1L
					val postCount = 5L
					every { postService.countByUserId(userId) } returns postCount

					val result = userDeletionService.getUserDeletionSummary(userId)

					result.shouldNotBeNull()
					result.userId shouldBe userId
					result.postCount shouldBe postCount
					verify(exactly = 1) { postService.countByUserId(userId) }
				}
			}

			When("User has no posts") {
				Then("Should return zero post count") {
					val userService = mockk<UserService>()
					val postService = mockk<PostService>()
					val tokenProvider = mockk<TokenProvider>()
					val userDeletionService = UserDeletionService(userService, postService, tokenProvider)

					val userId = 2L
					every { postService.countByUserId(userId) } returns 0L

					val result = userDeletionService.getUserDeletionSummary(userId)

					result.shouldNotBeNull()
					result.userId shouldBe userId
					result.postCount shouldBe 0L
					verify(exactly = 1) { postService.countByUserId(userId) }
				}
			}
		}
	})
