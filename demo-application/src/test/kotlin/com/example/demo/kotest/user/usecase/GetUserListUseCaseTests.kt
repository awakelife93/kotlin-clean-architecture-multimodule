package com.example.demo.kotest.user.usecase

import com.example.demo.user.constant.UserRole
import com.example.demo.user.model.User
import com.example.demo.user.port.input.GetUserListInput
import com.example.demo.user.service.UserService
import com.example.demo.user.usecase.GetUserListUseCase
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
class GetUserListUseCaseTests :
	BehaviorSpec({
		val userService = mockk<UserService>()
		val getUserListUseCase = GetUserListUseCase(userService)

		val testUsers =
			(1..3).map { id ->
				User(
					id = id.toLong(),
					email = "user$id@example.com",
					password = "encoded",
					name = "User $id",
					role = if (id % 2 == 0) UserRole.ADMIN else UserRole.USER,
					createdDt = LocalDateTime.now(),
					updatedDt = LocalDateTime.now()
				)
			}

		Given("Get user list") {

			When("Get paginated user list") {
				val pageable = PageRequest.of(0, 10)
				val input = GetUserListInput(pageable = pageable)
				val userPage = PageImpl(testUsers, pageable, testUsers.size.toLong())

				every { userService.findAll(pageable) } returns userPage

				val result = getUserListUseCase.execute(input)

				Then("Should return user list successfully") {
					result.shouldNotBeNull().also { output ->
						output.users.content shouldHaveSize 3
						output.users.content.forEachIndexed { index, user ->
							user.id shouldBe (index + 1).toLong()
							user.email shouldBe "user${index + 1}@example.com"
						}
					}
				}
			}

			When("Get empty user list") {
				val pageable = PageRequest.of(0, 10)
				val input = GetUserListInput(pageable = pageable)
				val emptyPage = PageImpl<User>(emptyList(), pageable, 0)

				every { userService.findAll(pageable) } returns emptyPage

				val result = getUserListUseCase.execute(input)

				Then("Should return empty list") {
					result.shouldNotBeNull().also { output ->
						output.users.content shouldHaveSize 0
						output.users.content.isEmpty() shouldBe true
					}
				}
			}

			When("Get specific page") {
				val pageable = PageRequest.of(1, 2)
				val input = GetUserListInput(pageable = pageable)
				val secondPageUsers = testUsers.subList(2, 3)
				val userPage = PageImpl(secondPageUsers, pageable, testUsers.size.toLong())

				every { userService.findAll(pageable) } returns userPage

				val result = getUserListUseCase.execute(input)

				Then("Requested page data is returned") {
					result.shouldNotBeNull {
						users.content shouldHaveSize 1
						users.content.first().name shouldBe "User 3"
					}
				}
			}
		}
	})
