package com.example.demo.persistence.auth.adapter

import com.example.demo.security.model.SecurityUserItem
import com.example.demo.user.constant.UserRole
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User

data class UserAdapter(
	val securityUserItem: SecurityUserItem
) : User(
		securityUserItem.userId.toString(),
		"",
		authorities(securityUserItem.role)
	) {
	companion object {
		private fun authorities(role: UserRole): Collection<GrantedAuthority> {
			val authorities: MutableCollection<GrantedAuthority> = ArrayList()
			authorities.add(SimpleGrantedAuthority("ROLE_$role"))

			return authorities
		}
	}
}
