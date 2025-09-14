package com.example.demo.webhook.common

import com.example.demo.webhook.constant.EmojiKeyword

object EmojiResolver {
	private val emojiKeywords =
		mutableListOf(
			EmojiKeyword.ERROR,
			EmojiKeyword.DEPLOY,
			EmojiKeyword.WARN,
			EmojiKeyword.SUCCESS,
			EmojiKeyword.FAIL,
			EmojiKeyword.SLOW
		)

	fun resolveTitleEmoji(title: String): String =
		emojiKeywords
			.firstOrNull { keywordGroup ->
				keywordGroup.keywords.any { title.contains(it, ignoreCase = true) }
			}?.emoji ?: EmojiKeyword.DEFAULT_TITLE.emoji

	fun resolveLineEmoji(line: String): String =
		emojiKeywords
			.firstOrNull { keywordGroup ->
				keywordGroup.keywords.any { line.contains(it, ignoreCase = true) }
			}?.emoji ?: EmojiKeyword.DEFAULT_LINE.emoji
}
