package com.example.demo.common.exception.handler

import com.example.demo.common.response.ErrorResponse
import com.example.demo.exception.BusinessRuleViolationException
import com.example.demo.exception.DomainException
import com.example.demo.exception.EntityAlreadyExistsException
import com.example.demo.exception.EntityNotFoundException
import com.example.demo.exception.NotFoundException
import com.example.demo.exception.UnAuthorizedException
import io.github.oshai.kotlinlogging.KotlinLogging
import io.jsonwebtoken.ExpiredJwtException
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.AuthenticationException
import org.springframework.validation.BindException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.NoHandlerFoundException

private val logger = KotlinLogging.logger {}

@RestControllerAdvice
class GlobalExceptionHandler {
	@ExceptionHandler(EntityNotFoundException::class)
	fun handleEntityNotFoundException(
		exception: EntityNotFoundException,
		httpServletRequest: HttpServletRequest
	): ResponseEntity<ErrorResponse> {
		val response =
			ErrorResponse.of(
				HttpStatus.NOT_FOUND.value(),
				exception.message ?: "Entity not found"
			)

		logger.warn {
			"EntityNotFoundException - ${httpServletRequest.method} ${httpServletRequest.requestURI} ${exception.message}"
		}

		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response)
	}

	@ExceptionHandler(NotFoundException::class)
	fun handleNotFoundException(
		exception: NotFoundException,
		httpServletRequest: HttpServletRequest
	): ResponseEntity<ErrorResponse> {
		val response =
			ErrorResponse.of(
				HttpStatus.NOT_FOUND.value(),
				exception.message ?: "Resource not found"
			)

		logger.warn {
			"NotFoundException - ${httpServletRequest.method} ${httpServletRequest.requestURI} ${exception.message}"
		}

		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response)
	}

	@ExceptionHandler(EntityAlreadyExistsException::class)
	fun handleEntityAlreadyExistsException(
		exception: EntityAlreadyExistsException,
		httpServletRequest: HttpServletRequest
	): ResponseEntity<ErrorResponse> {
		val response =
			ErrorResponse.of(
				HttpStatus.CONFLICT.value(),
				exception.message ?: "Entity already exists"
			)

		logger.warn {
			"EntityAlreadyExistsException - ${httpServletRequest.method} ${httpServletRequest.requestURI} ${exception.message}"
		}

		return ResponseEntity.status(HttpStatus.CONFLICT).body(response)
	}

	@ExceptionHandler(UnAuthorizedException::class)
	fun handleUnauthorizedException(
		exception: UnAuthorizedException,
		httpServletRequest: HttpServletRequest
	): ResponseEntity<ErrorResponse> {
		val response =
			ErrorResponse.of(
				HttpStatus.UNAUTHORIZED.value(),
				exception.message ?: "Unauthorized"
			)

		logger.warn {
			"UnauthorizedException - ${httpServletRequest.method} ${httpServletRequest.requestURI} ${exception.message}"
		}

		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response)
	}

	@ExceptionHandler(BusinessRuleViolationException::class)
	fun handleBusinessRuleViolationException(
		exception: BusinessRuleViolationException,
		httpServletRequest: HttpServletRequest
	): ResponseEntity<ErrorResponse> {
		val response =
			ErrorResponse.of(
				HttpStatus.BAD_REQUEST.value(),
				exception.message ?: "Business rule violation"
			)

		logger.warn {
			"BusinessRuleViolationException - ${httpServletRequest.method} ${httpServletRequest.requestURI} ${exception.message}"
		}

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
	}

	@ExceptionHandler(DomainException::class)
	fun handleDomainException(
		exception: DomainException,
		httpServletRequest: HttpServletRequest
	): ResponseEntity<ErrorResponse> {
		val response =
			ErrorResponse.of(
				HttpStatus.INTERNAL_SERVER_ERROR.value(),
				exception.message ?: "Domain error occurred"
			)

		logger.error(exception) {
			"DomainException - ${httpServletRequest.method} ${httpServletRequest.requestURI} ${exception.message}"
		}

		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response)
	}

	@ExceptionHandler(ExpiredJwtException::class)
	fun handleExpiredJwtException(
		exception: ExpiredJwtException,
		httpServletRequest: HttpServletRequest
	): ResponseEntity<ErrorResponse> {
		val response =
			ErrorResponse.of(
				HttpStatus.UNAUTHORIZED.value(),
				"JWT token has expired: ${exception.message}"
			)

		logger.warn {
			"ExpiredJwtException - ${httpServletRequest.method} ${httpServletRequest.requestURI} ${exception.message}"
		}

		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response)
	}

	@ExceptionHandler(AuthenticationException::class)
	fun handleAuthenticationException(
		exception: AuthenticationException,
		httpServletRequest: HttpServletRequest
	): ResponseEntity<ErrorResponse> {
		val response =
			ErrorResponse.of(
				HttpStatus.UNAUTHORIZED.value(),
				exception.message ?: "Authentication failed"
			)

		logger.warn {
			"AuthenticationException - ${httpServletRequest.method} ${httpServletRequest.requestURI} ${exception.message}"
		}

		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response)
	}

	@ExceptionHandler(AccessDeniedException::class)
	fun handleAccessDeniedException(
		exception: AccessDeniedException,
		httpServletRequest: HttpServletRequest
	): ResponseEntity<ErrorResponse> {
		val response =
			ErrorResponse.of(
				HttpStatus.FORBIDDEN.value(),
				exception.message ?: "Access denied"
			)

		logger.warn {
			"AccessDeniedException - ${httpServletRequest.method} ${httpServletRequest.requestURI} ${exception.message}"
		}

		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response)
	}

	@ExceptionHandler(BindException::class)
	fun handleBindException(
		exception: BindException,
		httpServletRequest: HttpServletRequest
	): ResponseEntity<ErrorResponse> {
		val exceptionMessage =
			exception.bindingResult.fieldErrors.joinToString(", ") {
				"${it.field}: ${it.defaultMessage}"
			}

		val response =
			ErrorResponse.of(
				HttpStatus.BAD_REQUEST.value(),
				exceptionMessage,
				exception.fieldErrors
			)

		logger.warn {
			"BindException - ${httpServletRequest.method} ${httpServletRequest.requestURI} $exceptionMessage"
		}

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
	}

	@ExceptionHandler(MethodArgumentNotValidException::class)
	fun handleMethodArgumentNotValidException(
		exception: MethodArgumentNotValidException,
		httpServletRequest: HttpServletRequest
	): ResponseEntity<ErrorResponse> {
		val exceptionMessage =
			exception.bindingResult.fieldErrors.joinToString(", ") {
				"${it.field}: ${it.defaultMessage}"
			}

		val response =
			ErrorResponse.of(
				HttpStatus.BAD_REQUEST.value(),
				exceptionMessage,
				exception.fieldErrors
			)

		logger.warn {
			"MethodArgumentNotValidException - ${httpServletRequest.method} ${httpServletRequest.requestURI} $exceptionMessage"
		}

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
	}

	@ExceptionHandler(NoHandlerFoundException::class)
	fun handleNoHandlerFoundException(
		exception: NoHandlerFoundException,
		httpServletRequest: HttpServletRequest
	): ResponseEntity<ErrorResponse> {
		val response =
			ErrorResponse.of(
				HttpStatus.NOT_FOUND.value(),
				exception.message ?: "Endpoint not found",
				exception.requestHeaders
			)

		logger.warn {
			"NoHandlerFoundException - ${httpServletRequest.method} ${httpServletRequest.requestURI}"
		}

		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response)
	}

	@ExceptionHandler(Exception::class)
	fun handleException(
		exception: Exception,
		httpServletRequest: HttpServletRequest
	): ResponseEntity<ErrorResponse> {
		val response =
			ErrorResponse.of(
				HttpStatus.INTERNAL_SERVER_ERROR.value(),
				exception.message ?: "An unexpected error occurred"
			)

		logger.error(exception) {
			"Unexpected Exception - ${httpServletRequest.method} ${httpServletRequest.requestURI} ${exception.message}"
		}

		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response)
	}
}
