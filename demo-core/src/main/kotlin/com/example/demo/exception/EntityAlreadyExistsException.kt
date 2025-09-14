package com.example.demo.exception

open class EntityAlreadyExistsException(
	message: String
) : DomainException(message)
