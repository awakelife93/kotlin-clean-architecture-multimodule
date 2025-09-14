package com.example.demo.user.event

import com.example.demo.user.model.UserDeleteItem

interface UserDeleteEventHandler {
	fun handle(payload: UserDeleteItem)
}
