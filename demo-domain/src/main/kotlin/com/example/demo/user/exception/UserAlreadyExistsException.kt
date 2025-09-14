package com.example.demo.user.exception

import com.example.demo.exception.EntityAlreadyExistsException

class UserAlreadyExistsException : EntityAlreadyExistsException {
	constructor(userId: Long) : super("Already User Exist userId = $userId")
	constructor(email: String) : super("Already User Exist email = $email")
}
