package com.example.demo.config

import io.kotest.core.config.AbstractProjectConfig
import io.kotest.extensions.spring.SpringExtension

object KotestSpringConfig : AbstractProjectConfig() {
	override fun extensions() = listOf(SpringExtension)
}
