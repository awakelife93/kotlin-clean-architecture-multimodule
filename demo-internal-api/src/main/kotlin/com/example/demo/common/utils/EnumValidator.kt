package com.example.demo.common.utils

import com.example.demo.common.annotation.ValidEnum
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

class EnumValidator : ConstraintValidator<ValidEnum, String> {
	private lateinit var acceptedValues: Set<String>
	private var ignoreCase: Boolean = false

	override fun initialize(constraintAnnotation: ValidEnum) {
		ignoreCase = constraintAnnotation.ignoreCase
		acceptedValues =
			constraintAnnotation
				.enumClass
				.java
				.enumConstants
				.map { it.name }
				.toSet()
	}

	override fun isValid(
		value: String,
		constraintValidatorContext: ConstraintValidatorContext
	): Boolean =
		if (ignoreCase) {
			acceptedValues.any { it.equals(value, ignoreCase = true) }
		} else {
			value in acceptedValues
		}
}
