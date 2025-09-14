package com.example.demo.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.mail.MailProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.JavaMailSenderImpl

@Configuration
@EnableConfigurationProperties(MailProperties::class)
class MailConfig {
	@Bean
	@ConditionalOnMissingBean(JavaMailSender::class)
	fun javaMailSender(properties: MailProperties): JavaMailSenderImpl =
		JavaMailSenderImpl().apply {
			host = properties.host
			port = properties.port
			username = properties.username
			password = properties.password

			javaMailProperties.putAll(properties.properties)
		}
}
