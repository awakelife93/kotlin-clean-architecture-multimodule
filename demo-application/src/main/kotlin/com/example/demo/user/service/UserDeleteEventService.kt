package com.example.demo.user.service

import com.example.demo.post.port.input.HardDeletePostsByUserIdInput
import com.example.demo.post.usecase.HardDeletePostsByUserIdUseCase
import com.example.demo.user.event.UserDeleteEventHandler
import com.example.demo.user.model.UserDeleteItem
import com.example.demo.user.port.input.HardDeleteUserByIdInput
import com.example.demo.user.usecase.HardDeleteUserByIdUseCase
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val logger = KotlinLogging.logger {}

@Service
@Transactional
class UserDeleteEventService(
	private val hardDeletePostsByUserIdUseCase: HardDeletePostsByUserIdUseCase,
	private val hardDeleteUserByIdUseCase: HardDeleteUserByIdUseCase
) : UserDeleteEventHandler {
	override fun handle(payload: UserDeleteItem) {
		logger.info {
			"Processing hard delete for user: ${payload.name} (${payload.email}), " +
				"Role: ${payload.role}, Deleted at: ${payload.deletedDt}"
		}

		logger.debug { "Deleting all posts for user ID: ${payload.id}" }
		hardDeletePostsByUserIdUseCase.execute(
			HardDeletePostsByUserIdInput(userId = payload.id)
		)

		logger.debug { "Deleting user record for ID: ${payload.id}" }
		hardDeleteUserByIdUseCase.execute(
			HardDeleteUserByIdInput(userId = payload.id)
		)

		logger.info { "Successfully completed hard delete for user: ${payload.email}" }
	}
}
