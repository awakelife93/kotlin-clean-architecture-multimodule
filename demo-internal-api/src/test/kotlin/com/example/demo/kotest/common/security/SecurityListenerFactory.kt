package com.example.demo.kotest.common.security

import com.example.demo.persistence.auth.adapter.UserAdapter
import com.example.demo.security.model.SecurityUserItem
import com.example.demo.user.constant.UserRole
import com.example.demo.user.model.User
import io.kotest.core.Tag
import io.kotest.core.listeners.TestListener
import io.kotest.core.test.TestCase
import io.kotest.core.test.TestResult
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

class SecurityListenerFactory(
	private val userId: Long = 1L,
	private val userEmail: String = "user@example.com",
	private val userName: String = "username",
	private val userRole: UserRole = UserRole.USER
) : TestListener {
	object NonSecurityOption : Tag()

	private fun getCurrentSecurityScope(prefix: String?): Boolean =
		when (prefix) {
			"Then: ", "Describe: " -> true
			else -> false
		}

	override suspend fun beforeTest(testCase: TestCase) {
		super.beforeTest(testCase)

		val isCurrentSecurityScope = getCurrentSecurityScope(testCase.name.prefix)
		if (isCurrentSecurityScope && NonSecurityOption !in testCase.config.tags) {
			val user =
				User(
					id = userId,
					email = userEmail,
					name = userName,
					password = "password123!@#",
					role = userRole
				)

			val securityUserItem = SecurityUserItem.from(user)
			val userAdapter = UserAdapter(securityUserItem)

			SecurityContextHolder.getContext().authentication =
				UsernamePasswordAuthenticationToken(
					userAdapter,
					null,
					userAdapter.authorities
				)
		}
	}

	override suspend fun afterTest(
		testCase: TestCase,
		result: TestResult
	) {
		super.afterTest(testCase, result)

		SecurityContextHolder.clearContext()
	}
}
