package com.example.demo.auth.exception

import com.example.demo.exception.UnAuthorizedException

class APIKeyNotFoundException(
	requestURI: String
) : UnAuthorizedException("API Key Not Found requestURI = $requestURI")
