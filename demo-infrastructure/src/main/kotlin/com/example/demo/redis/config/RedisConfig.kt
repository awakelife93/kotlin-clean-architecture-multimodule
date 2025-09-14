package com.example.demo.redis.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
class RedisConfig(
	@Value("\${spring.data.redis.port}") private val port: Int,
	@Value("\${spring.data.redis.host}") private val host: String
) {
	@Bean
	fun redisConnectionFactory(): RedisConnectionFactory = LettuceConnectionFactory(RedisStandaloneConfiguration(host, port))

	@Bean
	fun redisTemplate(redisConnectionFactory: RedisConnectionFactory): RedisTemplate<String, Any> =
		RedisTemplate<String, Any>().apply {
			keySerializer = StringRedisSerializer()
			valueSerializer = GenericJackson2JsonRedisSerializer()
			connectionFactory = redisConnectionFactory
		}

	@Bean
	fun stringRedisTemplate(redisConnectionFactory: RedisConnectionFactory): StringRedisTemplate = StringRedisTemplate(redisConnectionFactory)
}
