package com.example.demo.kotest.webhook.common

import com.example.demo.webhook.common.EmojiResolver
import com.example.demo.webhook.constant.EmojiKeyword
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@Tags("kotest-unit-test")
class EmojiResolverTests :
	DescribeSpec({
		describe("Emoji resolver test") {
			context("resolveTitleEmoji method test") {
				it("should return emoji for title containing ERROR keyword") {
					val emoji = EmojiResolver.resolveTitleEmoji("Error occurred in application")
					emoji shouldBe EmojiKeyword.ERROR.emoji
				}

				it("should return emoji for title containing DEPLOY keyword") {
					val emoji = EmojiResolver.resolveTitleEmoji("Deploy to production completed")
					emoji shouldBe EmojiKeyword.DEPLOY.emoji
				}

				it("should return emoji for title containing WARN keyword") {
					val emoji = EmojiResolver.resolveTitleEmoji("Warning: Performance issue detected")
					emoji shouldBe EmojiKeyword.WARN.emoji
				}

				it("should return emoji for title containing SUCCESS keyword") {
					val emoji = EmojiResolver.resolveTitleEmoji("Build successful")
					emoji shouldBe EmojiKeyword.SUCCESS.emoji
				}

				it("should return emoji for title containing FAIL keyword") {
					val emoji = EmojiResolver.resolveTitleEmoji("Test failed")
					emoji shouldBe EmojiKeyword.FAIL.emoji
				}

				it("should return emoji for title containing SLOW keyword") {
					val emoji = EmojiResolver.resolveTitleEmoji("Response is slow")
					emoji shouldBe EmojiKeyword.SLOW.emoji
				}

				it("should return default emoji for title without keywords") {
					val emoji = EmojiResolver.resolveTitleEmoji("Regular notification")
					emoji shouldBe EmojiKeyword.DEFAULT_TITLE.emoji
				}

				it("should match keyword case insensitively") {
					val emoji = EmojiResolver.resolveTitleEmoji("ERROR in uppercase")
					emoji shouldBe EmojiKeyword.ERROR.emoji
				}
			}

			context("resolveLineEmoji method test") {
				it("should return emoji for line containing ERROR keyword") {
					val emoji = EmojiResolver.resolveLineEmoji("Error in processing")
					emoji shouldBe EmojiKeyword.ERROR.emoji
				}

				it("should return emoji for line containing DEPLOY keyword") {
					val emoji = EmojiResolver.resolveLineEmoji("Deployment started")
					emoji shouldBe EmojiKeyword.DEPLOY.emoji
				}

				it("should return emoji for line containing WARN keyword") {
					val emoji = EmojiResolver.resolveLineEmoji("Warning: Low memory")
					emoji shouldBe EmojiKeyword.WARN.emoji
				}

				it("should return emoji for line containing SUCCESS keyword") {
					val emoji = EmojiResolver.resolveLineEmoji("Build success")
					emoji shouldBe EmojiKeyword.SUCCESS.emoji
				}

				it("should return emoji for line containing FAIL keyword") {
					val emoji = EmojiResolver.resolveLineEmoji("Test failed")
					emoji shouldBe EmojiKeyword.FAIL.emoji
				}

				it("should return emoji for line containing SLOW keyword") {
					val emoji = EmojiResolver.resolveLineEmoji("Performance is slow")
					emoji shouldBe EmojiKeyword.SLOW.emoji
				}

				it("should return default emoji for line without keywords") {
					val emoji = EmojiResolver.resolveLineEmoji("Regular line content")
					emoji shouldBe EmojiKeyword.DEFAULT_LINE.emoji
				}

				it("should match keyword case insensitively") {
					val emoji = EmojiResolver.resolveLineEmoji("SUCCESS in uppercase")
					emoji shouldBe EmojiKeyword.SUCCESS.emoji
				}
			}
		}
	})
