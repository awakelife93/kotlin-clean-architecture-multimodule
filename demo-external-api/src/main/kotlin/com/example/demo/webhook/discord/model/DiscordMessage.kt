package com.example.demo.webhook.discord.model

data class DiscordMessage(
	val title: String,
	val messages: List<String>,
	val embeds: List<DiscordEmbed>? = null
) {
	companion object {
		fun of(
			title: String,
			messages: List<String>,
			embeds: List<DiscordEmbed>? = null
		): DiscordMessage = DiscordMessage(title, messages, embeds)
	}
}

data class DiscordEmbed(
	val title: String? = null,
	val description: String? = null,
	val color: Int? = null,
	val fields: List<DiscordEmbedField>? = null,
	val footer: DiscordEmbedFooter? = null
) {
	companion object {
		fun of(
			title: String? = null,
			description: String? = null,
			color: Int? = null,
			fields: List<DiscordEmbedField>? = null,
			footer: DiscordEmbedFooter? = null
		): DiscordEmbed = DiscordEmbed(title, description, color, fields, footer)
	}
}

data class DiscordEmbedField(
	val name: String,
	val value: String,
	val inline: Boolean = false
) {
	companion object {
		fun of(
			name: String,
			value: String,
			inline: Boolean = false
		): DiscordEmbedField = DiscordEmbedField(name, value, inline)
	}
}

data class DiscordEmbedFooter(
	val text: String
) {
	companion object {
		fun of(text: String): DiscordEmbedFooter = DiscordEmbedFooter(text)
	}
}
