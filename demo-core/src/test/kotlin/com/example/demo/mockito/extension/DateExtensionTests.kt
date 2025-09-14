package com.example.demo.mockito.extension

import com.example.demo.extension.formatTo
import com.example.demo.extension.toFlexibleLocalDateTime
import com.example.demo.extension.toLocalDateTime
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

@ActiveProfiles("test")
@Tag("mockito-unit-test")
@DisplayName("Mockito Unit - Date Extension Test")
@ExtendWith(MockitoExtension::class)
class DateExtensionTests {
	private val defaultWrongPattern = "wrong_pattern"

	@Nested
	@DisplayName("Convert String to LocalDateTime Test")
	inner class ConvertStringToLocalDateTimeFormatTest {
		@Test
		@DisplayName("Convert current string datetime & current pattern to LocalDateTime")
		fun should_AssertLocalDateTime_when_GivenCurrentStringDateTimeAndCurrentPattern() {
			val stringDateTime =
				DateTimeFormatter
					.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
					.format(LocalDateTime.now().withNano(0))

			val localDateTime = stringDateTime.toLocalDateTime("yyyy-MM-dd'T'HH:mm:ss")

			Assertions.assertEquals(localDateTime.javaClass, LocalDateTime::class.java)
		}

		@Test
		@DisplayName("Failed Convert current string datetime & wrong pattern to LocalDateTime")
		fun should_AssertIllegalArgumentException_when_GivenCurrentStringDateTimeAndWrongPattern() {
			Assertions.assertThrows(
				IllegalArgumentException::class.java
			) {
				LocalDateTime.now().toString().toLocalDateTime(defaultWrongPattern)
			}
		}

		@Test
		@DisplayName("Failed Convert blank string datetime & current pattern to LocalDateTime")
		fun should_AssertDateTimeParseException_when_GivenBlankStringDateTimeAndCurrentPattern() {
			Assertions.assertThrows(
				DateTimeParseException::class.java
			) {
				"".toLocalDateTime("yyyy-MM-dd'T'HH:mm:ss")
			}
		}
	}

	@Nested
	@DisplayName("Convert LocalDateTime to String Test")
	inner class ConvertLocalDateTimeToStringFormatTest {
		@Test
		@DisplayName("Convert current local datetime & current pattern to string datetime")
		fun should_AssertStringDateTime_when_GivenCurrentLocalDateTimeAndCurrentPattern() {
			val stringDateTime = LocalDateTime.now().withNano(0).formatTo("yyyy-MM-dd'T'HH:mm:ss")

			Assertions.assertEquals(stringDateTime.javaClass, String::class.java)
		}

		@Test
		@DisplayName("Failed Convert Current local datetime & wrong pattern to string datetime")
		fun should_AssertIllegalArgumentException_when_GivenWrongLocalDateTimeOrWrongPattern() {
			Assertions.assertThrows(
				IllegalArgumentException::class.java
			) {
				LocalDateTime.now().formatTo(defaultWrongPattern)
			}
		}
	}

	@Nested
	@DisplayName("Convert Flexible String to LocalDateTime Test")
	inner class ConvertFlexibleStringToLocalDateTimeFormatTest {
		@Test
		@DisplayName("Convert string with microseconds to LocalDateTime")
		fun should_AssertLocalDateTime_when_StringContainsMicroseconds() {
			val datetimeWithMicros = "2023-06-13 17:42:55.440101"
			val result = datetimeWithMicros.toFlexibleLocalDateTime()

			Assertions.assertEquals(result.javaClass, LocalDateTime::class.java)
		}

		@Test
		@DisplayName("Convert string without microseconds to LocalDateTime")
		fun should_AssertLocalDateTime_when_StringHasNoMicroseconds() {
			val datetimeWithoutMicros = "2023-06-13 17:42:55"
			val result = datetimeWithoutMicros.toFlexibleLocalDateTime()

			Assertions.assertEquals(result.javaClass, LocalDateTime::class.java)
		}

		@Test
		@DisplayName("Failed to convert blank string to LocalDateTime")
		fun should_ThrowDateTimeParseException_when_BlankStringUsedWithFlexibleConverter() {
			Assertions.assertThrows(
				DateTimeParseException::class.java
			) {
				"".toFlexibleLocalDateTime()
			}
		}
	}
}
