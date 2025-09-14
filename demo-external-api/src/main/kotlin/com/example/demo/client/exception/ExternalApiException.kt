package com.example.demo.client.exception

class ExternalApiException(
	message: String,
	cause: Throwable? = null
) : RuntimeException(message, cause)
