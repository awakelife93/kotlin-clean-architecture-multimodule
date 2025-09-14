package com.example.demo.exception

open class DomainException(
	message: String,
	cause: Throwable? = null
) : RuntimeException(message, cause)
