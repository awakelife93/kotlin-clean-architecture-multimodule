package com.example.demo.mockito.common.security

import com.example.demo.persistence.auth.adapter.UserAdapter
import com.example.demo.security.model.SecurityUserItem
import com.example.demo.user.constant.UserRole
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.test.context.support.WithSecurityContextFactory

class WithMockCustomUserSecurityContextFactory : WithSecurityContextFactory<WithMockCustomUser> {
	override fun createSecurityContext(annotation: WithMockCustomUser): SecurityContext {
		val securityUserItem =
			SecurityUserItem(
				userId = annotation.id,
				email = annotation.email,
				name = annotation.name,
				role = UserRole.valueOf(annotation.role)
			)

		val authentication =
			UsernamePasswordAuthenticationToken(
				UserAdapter(securityUserItem),
				null,
				UserAdapter(securityUserItem).authorities
			)

		return SecurityContextHolder.createEmptyContext().apply {
			this.authentication = authentication
		}
	}
}
