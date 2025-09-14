package com.example.demo.user.exception

import com.example.demo.exception.EntityNotFoundException

class UserNotFoundException : EntityNotFoundException {
	constructor(userId: Long) : super("User Not Found userId = $userId")
	constructor(email: String) : super("User Not Found email = $email")
}
