package com.example.demo.extension

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField

fun String.toLocalDateTime(pattern: String): LocalDateTime = LocalDateTime.parse(this, DateTimeFormatter.ofPattern(pattern))

fun String.toFlexibleLocalDateTime(): LocalDateTime {
	val formatter =
		DateTimeFormatterBuilder()
			.appendPattern("yyyy-MM-dd HH:mm:ss")
			.optionalStart()
			.appendFraction(ChronoField.MICRO_OF_SECOND, 1, 6, true)
			.optionalEnd()
			.toFormatter()

	return LocalDateTime.parse(this, formatter)
}

fun LocalDateTime.formatTo(pattern: String): String = this.format(DateTimeFormatter.ofPattern(pattern))
