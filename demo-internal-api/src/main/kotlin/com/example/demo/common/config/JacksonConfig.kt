package com.example.demo.common.config

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder

@Configuration
class JacksonConfig {
	@Bean
	@Primary
	fun objectMapper(): ObjectMapper =
		Jackson2ObjectMapperBuilder
			.json()
			.modules(
				JavaTimeModule(),
				KotlinModule
					.Builder()
					.configure(KotlinFeature.NullToEmptyCollection, false)
					.configure(KotlinFeature.NullToEmptyMap, false)
					.configure(KotlinFeature.NullIsSameAsDefault, false)
					.configure(KotlinFeature.SingletonSupport, false)
					.configure(KotlinFeature.StrictNullChecks, false)
					.build()
			).featuresToDisable(
				SerializationFeature.WRITE_DATES_AS_TIMESTAMPS,
				SerializationFeature.FAIL_ON_EMPTY_BEANS,
				DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
			).propertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE)
			.build()
}
