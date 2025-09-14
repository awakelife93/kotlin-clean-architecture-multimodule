package com.example.demo.exception

open class BusinessRuleViolationException(
	message: String
) : DomainException(message)
