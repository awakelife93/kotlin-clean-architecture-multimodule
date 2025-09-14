package com.example.demo.persistence.auth

import com.example.demo.persistence.auth.adapter.UserAdapter
import org.springframework.data.domain.AuditorAware
import org.springframework.security.core.context.SecurityContextHolder
import java.util.Optional

class AuditorAwareImpl : AuditorAware<Long> {
	override fun getCurrentAuditor(): Optional<Long> {
		val authentication = SecurityContextHolder.getContext().authentication

		return when {
			authentication == null ||
				!authentication.isAuthenticated ||
				authentication.principal == "anonymousUser" -> Optional.empty()

			authentication.principal is UserAdapter -> {
				val userAdapter = authentication.principal as UserAdapter
				Optional.of(userAdapter.securityUserItem.userId)
			}

			else -> Optional.empty()
		}
	}
}
