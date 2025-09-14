package com.example.demo.user.service

import com.example.demo.persistence.auth.provider.TokenProvider
import com.example.demo.user.constant.UserRole
import com.example.demo.user.event.UserEvent
import com.example.demo.user.exception.UserAlreadyExistsException
import com.example.demo.user.exception.UserNotFoundException
import com.example.demo.user.model.User
import com.example.demo.user.port.UserPort
import com.example.demo.user.port.input.CreateUserInput
import com.example.demo.user.port.output.UserOutput
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class UserService(
	private val userPort: UserPort,
	private val bCryptPasswordEncoder: BCryptPasswordEncoder,
	private val tokenProvider: TokenProvider,
	private val applicationEventPublisher: ApplicationEventPublisher
) {
	fun findOneById(userId: Long): User? = userPort.findOneById(userId)

	fun findOneByIdOrThrow(userId: Long): User = findOneById(userId) ?: throw UserNotFoundException(userId)

	fun findOneByEmail(email: String): User? = userPort.findOneByEmail(email)

	fun findOneByEmailOrThrow(email: String): User = findOneByEmail(email) ?: throw UserNotFoundException(email)

	fun findAll(pageable: Pageable): Page<User> = userPort.findAll(pageable)

	@Transactional
	fun createUser(user: User): User {
		require(!userPort.existsByEmail(user.email)) {
			"Email already exists: ${user.email}"
		}

		return userPort.save(user)
	}

	@Transactional
	fun registerNewUser(input: CreateUserInput): UserOutput.AuthenticatedUserOutput {
		if (isEmailDuplicated(input.email)) {
			throw UserAlreadyExistsException(input.email)
		}

		val user =
			User(
				name = input.name,
				email = input.email,
				password = input.password,
				role = UserRole.USER
			).encodePassword { bCryptPasswordEncoder.encode(it) }

		val savedUser = userPort.save(user)
		val accessToken = tokenProvider.createFullTokens(savedUser)

		applicationEventPublisher.publishEvent(
			UserEvent.WelcomeSignUpEvent.from(savedUser)
		)

		return UserOutput.AuthenticatedUserOutput.from(savedUser, accessToken)
	}

	@Transactional
	fun updateUser(user: User): User = userPort.save(user)

	@Transactional
	fun updateUserInfo(
		userId: Long,
		name: String?
	): User {
		val user = findOneByIdOrThrow(userId)

		return user
			.apply {
				name?.let { update(name = it) }
			}.let(userPort::save)
	}

	@Transactional
	fun deleteById(userId: Long) {
		findOneByIdOrThrow(userId)
		userPort.deleteById(userId)
	}

	@Transactional
	fun deleteByIdWithoutValidation(userId: Long) {
		userPort.deleteById(userId)
	}

	fun existsById(userId: Long): Boolean = userPort.existsById(userId)

	fun isEmailDuplicated(email: String): Boolean = userPort.existsByEmail(email)
}
