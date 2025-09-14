package com.example.demo.kotest.extension

import com.example.demo.extension.formatTo
import com.example.demo.extension.toFlexibleLocalDateTime
import com.example.demo.extension.toLocalDateTime
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.types.shouldBeSameInstanceAs
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

@ActiveProfiles("test")
@Tags("kotest-unit-test")
class DateExtensionTests :
	BehaviorSpec({

		Given("Convert Given String DateTime to LocalDateTime") {
			val pattern = "yyyy-MM-dd'T'HH:mm:ss"
			val stringDateTime = LocalDateTime.now().withNano(0).format(DateTimeFormatter.ofPattern(pattern))

			When("Given Current String datetime & Pattern") {

				val localDateTime = stringDateTime.toLocalDateTime(pattern)

				Then("String Datetime to LocalDatetime") {
					localDateTime::class.java shouldBeSameInstanceAs (LocalDateTime::class.java)
				}
			}

			When("Given Current String datetime & Wrong Pattern") {

				shouldThrowExactly<IllegalArgumentException> {
					stringDateTime.toLocalDateTime("wrong pattern")
				}
			}

			When("Given Wrong String datetime & Current Pattern") {

				shouldThrowExactly<DateTimeParseException> {
					"".toLocalDateTime(pattern)
				}
			}
		}

		Given("Convert Given LocalDateTime to String DateTime") {

			When("Given Current LocalDateTime & Pattern") {
				val stringDateTime = LocalDateTime.now().withNano(0).formatTo("yyyy-MM-dd'T'HH:mm:ss")

				Then("LocalDatetime to String Datetime") {
					stringDateTime::class.java shouldBeSameInstanceAs String::class.java
				}
			}

			When("Given Current LocalDateTime & Wrong Pattern") {

				shouldThrowExactly<IllegalArgumentException> {
					LocalDateTime.now().formatTo(
						"wrong pattern"
					)
				}
			}
		}

		Given("Convert String with optional microsecond to LocalDateTime") {

			When("Given datetime string with microseconds") {
				val datetimeWithMicros = "2023-06-13 17:42:55.440101"
				val localDateTime = datetimeWithMicros.toFlexibleLocalDateTime()

				Then("Should parse successfully with microsecond precision") {
					localDateTime::class.java shouldBeSameInstanceAs LocalDateTime::class.java
				}
			}

			When("Given datetime string without microseconds") {
				val datetimeWithoutMicros = "2023-06-13 17:42:55"
				val localDateTime = datetimeWithoutMicros.toFlexibleLocalDateTime()

				Then("Should parse successfully without microsecond precision") {
					localDateTime::class.java shouldBeSameInstanceAs LocalDateTime::class.java
				}
			}

			When("Given invalid datetime string") {

				Then("Should throw DateTimeParseException") {
					shouldThrowExactly<DateTimeParseException> {
						"".toFlexibleLocalDateTime()
					}
				}
			}
		}
	})
