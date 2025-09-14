package com.example.demo.mockito.common.security

import org.springframework.security.test.context.support.WithSecurityContext

@Retention(AnnotationRetention.RUNTIME)
@WithSecurityContext(factory = WithMockCustomUserSecurityContextFactory::class)
annotation class WithMockCustomUser(
	val id: Long = 1L,
	val email: String = "user@example.com",
	val name: String = "username",
	val role: String = "USER"
)
