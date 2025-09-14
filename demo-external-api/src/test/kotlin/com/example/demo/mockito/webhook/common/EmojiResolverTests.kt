package com.example.demo.mockito.webhook.common

import com.example.demo.webhook.common.EmojiResolver
import com.example.demo.webhook.constant.EmojiKeyword
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@Tag("mockito-unit-test")
@DisplayName("Mockito Unit - EmojiResolver Test")
@ExtendWith(MockitoExtension::class)
class EmojiResolverTests {
	@Nested
	@DisplayName("Title emoji resolution tests")
	inner class TitleEmojiTests {
		@Test
		@DisplayName("should resolve emoji for title with ERROR keyword")
		fun testResolveTitleEmojiWithError() {
			val title = "Application Error Occurred"
			val emoji = EmojiResolver.resolveTitleEmoji(title)
			assertEquals(EmojiKeyword.ERROR.emoji, emoji)
		}

		@Test
		@DisplayName("should resolve emoji for title with DEPLOY keyword")
		fun testResolveTitleEmojiWithDeploy() {
			val title = "Deploy to Production"
			val emoji = EmojiResolver.resolveTitleEmoji(title)
			assertEquals(EmojiKeyword.DEPLOY.emoji, emoji)
		}

		@Test
		@DisplayName("should resolve emoji for title with WARN keyword")
		fun testResolveTitleEmojiWithWarn() {
			val title = "Warning: High Memory Usage"
			val emoji = EmojiResolver.resolveTitleEmoji(title)
			assertEquals(EmojiKeyword.WARN.emoji, emoji)
		}

		@Test
		@DisplayName("should resolve emoji for title with SUCCESS keyword")
		fun testResolveTitleEmojiWithSuccess() {
			val title = "Build Successful"
			val emoji = EmojiResolver.resolveTitleEmoji(title)
			assertEquals(EmojiKeyword.SUCCESS.emoji, emoji)
		}

		@Test
		@DisplayName("should resolve emoji for title with FAIL keyword")
		fun testResolveTitleEmojiWithFail() {
			val title = "Test Failed"
			val emoji = EmojiResolver.resolveTitleEmoji(title)
			assertEquals(EmojiKeyword.FAIL.emoji, emoji)
		}

		@Test
		@DisplayName("should resolve emoji for title with SLOW keyword")
		fun testResolveTitleEmojiWithSlow() {
			val title = "Slow Response Time"
			val emoji = EmojiResolver.resolveTitleEmoji(title)
			assertEquals(EmojiKeyword.SLOW.emoji, emoji)
		}

		@Test
		@DisplayName("should return default emoji for title without keywords")
		fun testResolveTitleEmojiWithNoKeyword() {
			val title = "Regular Update"
			val emoji = EmojiResolver.resolveTitleEmoji(title)
			assertEquals(EmojiKeyword.DEFAULT_TITLE.emoji, emoji)
		}

		@Test
		@DisplayName("should detect keywords case insensitively")
		fun testResolveTitleEmojiCaseInsensitive() {
			val title = "APPLICATION ERROR"
			val emoji = EmojiResolver.resolveTitleEmoji(title)
			assertEquals(EmojiKeyword.ERROR.emoji, emoji)
		}
	}

	@Nested
	@DisplayName("Line emoji resolution tests")
	inner class LineEmojiTests {
		@Test
		@DisplayName("should resolve emoji for line with ERROR keyword")
		fun testResolveLineEmojiWithError() {
			val line = "Error: Connection timeout"
			val emoji = EmojiResolver.resolveLineEmoji(line)
			assertEquals(EmojiKeyword.ERROR.emoji, emoji)
		}

		@Test
		@DisplayName("should resolve emoji for line with DEPLOY keyword")
		fun testResolveLineEmojiWithDeploy() {
			val line = "Deployment completed"
			val emoji = EmojiResolver.resolveLineEmoji(line)
			assertEquals(EmojiKeyword.DEPLOY.emoji, emoji)
		}

		@Test
		@DisplayName("should resolve emoji for line with WARN keyword")
		fun testResolveLineEmojiWithWarn() {
			val line = "WARNING: Low disk space"
			val emoji = EmojiResolver.resolveLineEmoji(line)
			assertEquals(EmojiKeyword.WARN.emoji, emoji)
		}

		@Test
		@DisplayName("should resolve emoji for line with SUCCESS keyword")
		fun testResolveLineEmojiWithSuccess() {
			val line = "Successfully completed"
			val emoji = EmojiResolver.resolveLineEmoji(line)
			assertEquals(EmojiKeyword.SUCCESS.emoji, emoji)
		}

		@Test
		@DisplayName("should resolve emoji for line with FAIL keyword")
		fun testResolveLineEmojiWithFail() {
			val line = "Process failed with exit code 1"
			val emoji = EmojiResolver.resolveLineEmoji(line)
			assertEquals(EmojiKeyword.FAIL.emoji, emoji)
		}

		@Test
		@DisplayName("should resolve emoji for line with SLOW keyword")
		fun testResolveLineEmojiWithSlow() {
			val line = "Response was slow"
			val emoji = EmojiResolver.resolveLineEmoji(line)
			assertEquals(EmojiKeyword.SLOW.emoji, emoji)
		}

		@Test
		@DisplayName("should return default emoji for line without keywords")
		fun testResolveLineEmojiWithNoKeyword() {
			val line = "Processing data"
			val emoji = EmojiResolver.resolveLineEmoji(line)
			assertEquals(EmojiKeyword.DEFAULT_LINE.emoji, emoji)
		}

		@Test
		@DisplayName("should detect keywords case insensitively")
		fun testResolveLineEmojiCaseInsensitive() {
			val line = "operation FAILED"
			val emoji = EmojiResolver.resolveLineEmoji(line)
			assertEquals(EmojiKeyword.FAIL.emoji, emoji)
		}
	}
}
