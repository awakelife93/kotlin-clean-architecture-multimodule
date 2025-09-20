package com.example.demo.mockito.persistence.post.adapter

import com.example.demo.persistence.post.adapter.PostRepositoryAdapter
import com.example.demo.persistence.post.entity.PostEntity
import com.example.demo.persistence.post.mapper.PostMapper
import com.example.demo.persistence.post.repository.PostJpaRepository
import com.example.demo.post.model.Post
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.never
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.ActiveProfiles
import java.util.Optional

@ActiveProfiles("test")
@Tag("mockito-unit-test")
@DisplayName("Mockito Unit - Post Repository Adapter Test")
@ExtendWith(MockitoExtension::class)
class PostRepositoryAdapterTests {
	@Mock
	private lateinit var postJpaRepository: PostJpaRepository

	@Mock
	private lateinit var postMapper: PostMapper

	private lateinit var adapter: PostRepositoryAdapter

	@BeforeEach
	fun setUp() {
		adapter = PostRepositoryAdapter(postJpaRepository, postMapper)
	}

	@Nested
	@DisplayName("Save Tests")
	inner class SaveTests {
		@Test
		@DisplayName("should save new post when id is 0")
		fun `should save new post`() {
			val post =
				Post(
					id = 0L,
					title = "New Post",
					subTitle = "Subtitle",
					content = "Content",
					userId = 1L
				)
			val entity =
				PostEntity(
					title = post.title,
					subTitle = post.subTitle,
					content = post.content,
					userId = post.userId
				)
			val savedEntity = entity.apply { id = 100L }
			val savedPost = post.copy(id = 100L)

			whenever(postMapper.toEntity(post)).thenReturn(entity)
			whenever(postJpaRepository.save(entity)).thenReturn(savedEntity)
			whenever(postMapper.toDomain(savedEntity)).thenReturn(savedPost)

			val result = adapter.save(post)

			assertEquals(savedPost, result)
			verify(postMapper, times(1)).toEntity(post)
			verify(postJpaRepository, times(1)).save(entity)
			verify(postMapper, times(1)).toDomain(savedEntity)
			verify(postJpaRepository, never()).findById(any<Long>())
		}

		@Test
		@DisplayName("should update existing post when entity exists")
		fun `should update existing post`() {
			val post =
				Post(
					id = 100L,
					title = "Updated Post",
					subTitle = "Updated Subtitle",
					content = "Updated Content",
					userId = 1L
				)
			val existingEntity =
				PostEntity(
					title = "Old Title",
					subTitle = "Old Subtitle",
					content = "Old Content",
					userId = 1L
				).apply { id = 100L }
			val updatedEntity = existingEntity
			val savedPost = post

			whenever(postJpaRepository.findById(100L)).thenReturn(Optional.of(existingEntity))
			whenever(postMapper.updateEntity(existingEntity, post)).thenReturn(updatedEntity)
			whenever(postJpaRepository.save(updatedEntity)).thenReturn(updatedEntity)
			whenever(postMapper.toDomain(updatedEntity)).thenReturn(savedPost)

			val result = adapter.save(post)

			assertEquals(savedPost, result)
			verify(postJpaRepository, times(1)).findById(100L)
			verify(postMapper, times(1)).updateEntity(existingEntity, post)
			verify(postJpaRepository, times(1)).save(updatedEntity)
		}
	}

	@Nested
	@DisplayName("FindOneById Tests")
	inner class FindOneByIdTests {
		@Test
		@DisplayName("should return post when found")
		fun `should return post when found`() {
			val entity =
				PostEntity(
					title = "Title",
					subTitle = "Subtitle",
					content = "Content",
					userId = 1L
				).apply { id = 100L }
			val post =
				Post(
					id = 100L,
					title = "Title",
					subTitle = "Subtitle",
					content = "Content",
					userId = 1L
				)

			whenever(postJpaRepository.findById(100L)).thenReturn(Optional.of(entity))
			whenever(postMapper.toDomain(entity)).thenReturn(post)

			val result = adapter.findOneById(100L)

			assertNotNull(result)
			assertEquals(post, result)
			verify(postJpaRepository, times(1)).findById(100L)
			verify(postMapper, times(1)).toDomain(entity)
		}

		@Test
		@DisplayName("should return null when not found")
		fun `should return null when not found`() {
			whenever(postJpaRepository.findById(999L)).thenReturn(Optional.empty())

			val result = adapter.findOneById(999L)

			assertNull(result)
			verify(postJpaRepository, times(1)).findById(999L)
			verify(postMapper, never()).toDomain(any<PostEntity>())
		}
	}

	@Nested
	@DisplayName("FindAll Tests")
	inner class FindAllTests {
		@Test
		@DisplayName("should return paged posts")
		fun `should return paged posts`() {
			val pageable = PageRequest.of(0, 10)
			val entities =
				listOf(
					PostEntity("Title1", "Sub1", "Content1", 1L).apply { id = 1L },
					PostEntity("Title2", "Sub2", "Content2", 2L).apply { id = 2L }
				)
			val posts =
				listOf(
					Post(1L, "Title1", "Sub1", "Content1", 1L),
					Post(2L, "Title2", "Sub2", "Content2", 2L)
				)
			val entityPage = PageImpl(entities, pageable, 2)

			whenever(postJpaRepository.findAll(pageable)).thenReturn(entityPage)
			entities.forEachIndexed { index, entity ->
				whenever(postMapper.toDomain(entity)).thenReturn(posts[index])
			}

			val result = adapter.findAll(pageable)

			assertEquals(2, result.content.size)
			assertEquals(posts, result.content)
			assertEquals(2, result.totalElements)
			verify(postJpaRepository, times(1)).findAll(pageable)
			verify(postMapper, times(2)).toDomain(any<PostEntity>())
		}

		@Test
		@DisplayName("should return empty page when no posts")
		fun `should return empty page`() {
			val pageable = PageRequest.of(0, 10)
			val emptyPage = PageImpl<PostEntity>(emptyList(), pageable, 0)

			whenever(postJpaRepository.findAll(pageable)).thenReturn(emptyPage)

			val result = adapter.findAll(pageable)

			assertEquals(0, result.content.size)
			assertEquals(0, result.totalElements)
			verify(postJpaRepository, times(1)).findAll(pageable)
			verify(postMapper, never()).toDomain(any<PostEntity>())
		}
	}

	@Nested
	@DisplayName("FindByUserId Tests")
	inner class FindByUserIdTests {
		@Test
		@DisplayName("should return paged posts for user")
		fun `should return paged posts for user`() {
			val userId = 1L
			val pageable = PageRequest.of(0, 5)
			val entities =
				listOf(
					PostEntity("User Post 1", "Sub1", "Content1", userId).apply { id = 1L },
					PostEntity("User Post 2", "Sub2", "Content2", userId).apply { id = 2L }
				)
			val posts =
				listOf(
					Post(1L, "User Post 1", "Sub1", "Content1", userId),
					Post(2L, "User Post 2", "Sub2", "Content2", userId)
				)
			val entityPage = PageImpl(entities, pageable, 2)

			whenever(postJpaRepository.findByUserId(userId, pageable)).thenReturn(entityPage)
			entities.forEachIndexed { index, entity ->
				whenever(postMapper.toDomain(entity)).thenReturn(posts[index])
			}

			val result = adapter.findOneByUserId(userId, pageable)

			assertEquals(2, result.content.size)
			assertEquals(posts, result.content)
			verify(postJpaRepository, times(1)).findByUserId(userId, pageable)
		}

		@Test
		@DisplayName("should return all posts for user")
		fun `should return all posts for user`() {
			val userId = 1L
			val entities =
				listOf(
					PostEntity("Post 1", "Sub1", "Content1", userId).apply { id = 1L },
					PostEntity("Post 2", "Sub2", "Content2", userId).apply { id = 2L },
					PostEntity("Post 3", "Sub3", "Content3", userId).apply { id = 3L }
				)
			val posts =
				listOf(
					Post(1L, "Post 1", "Sub1", "Content1", userId),
					Post(2L, "Post 2", "Sub2", "Content2", userId),
					Post(3L, "Post 3", "Sub3", "Content3", userId)
				)

			whenever(postJpaRepository.findAllByUserId(userId)).thenReturn(entities)
			entities.forEachIndexed { index, entity ->
				whenever(postMapper.toDomain(entity)).thenReturn(posts[index])
			}

			val result = adapter.findAllByUserId(userId)

			assertEquals(3, result.size)
			assertEquals(posts, result)
			verify(postJpaRepository, times(1)).findAllByUserId(userId)
		}

		@Test
		@DisplayName("should return empty list when user has no posts")
		fun `should return empty list for user with no posts`() {
			val userId = 999L
			whenever(postJpaRepository.findAllByUserId(userId)).thenReturn(emptyList())

			val result = adapter.findAllByUserId(userId)

			assertEquals(0, result.size)
			verify(postJpaRepository, times(1)).findAllByUserId(userId)
			verify(postMapper, never()).toDomain(any<PostEntity>())
		}
	}

	@Nested
	@DisplayName("FindExcludeUsersPosts Tests")
	inner class FindExcludeUsersPostsTests {
		@Test
		@DisplayName("should return posts excluding specified users")
		fun `should exclude specified users`() {
			val excludedUserIds = listOf(1L, 2L)
			val pageable = PageRequest.of(0, 10)
			val entities =
				listOf(
					PostEntity("Post 1", "Sub1", "Content1", 3L).apply { id = 1L },
					PostEntity("Post 2", "Sub2", "Content2", 4L).apply { id = 2L }
				)
			val posts =
				listOf(
					Post(1L, "Post 1", "Sub1", "Content1", 3L),
					Post(2L, "Post 2", "Sub2", "Content2", 4L)
				)
			val entityPage = PageImpl(entities, pageable, 2)

			whenever(postJpaRepository.findExcludeUsersPosts(excludedUserIds, pageable))
				.thenReturn(entityPage)
			entities.forEachIndexed { index, entity ->
				whenever(postMapper.toDomain(entity)).thenReturn(posts[index])
			}

			val result = adapter.findExcludeUsersPosts(excludedUserIds, pageable)

			assertEquals(2, result.content.size)
			assertEquals(posts, result.content)
			verify(postJpaRepository, times(1)).findExcludeUsersPosts(excludedUserIds, pageable)
		}
	}

	@Nested
	@DisplayName("Delete Tests")
	inner class DeleteTests {
		@Test
		@DisplayName("should delete post by id")
		fun `should delete by id`() {
			val postId = 100L

			doNothing().whenever(postJpaRepository).deleteById(postId)

			adapter.deleteById(postId)

			verify(postJpaRepository, times(1)).deleteById(postId)
		}

		@Test
		@DisplayName("should not delete when post not found")
		fun `should not delete when not found`() {
			val postId = 999L

			doNothing().whenever(postJpaRepository).deleteById(postId)

			adapter.deleteById(postId)

			verify(postJpaRepository, times(1)).deleteById(postId)
		}

		@Test
		@DisplayName("should soft delete all posts for user")
		fun `should soft delete by user id`() {
			val userId = 1L

			doNothing().whenever(postJpaRepository).deleteByUserId(userId)

			adapter.deleteByUserId(userId)

			verify(postJpaRepository, times(1)).deleteByUserId(userId)
		}

		@Test
		@DisplayName("should handle soft delete when user has no posts")
		fun `should handle soft delete with no posts`() {
			val userId = 999L

			doNothing().whenever(postJpaRepository).deleteByUserId(userId)

			adapter.deleteByUserId(userId)

			verify(postJpaRepository, times(1)).deleteByUserId(userId)
		}

		@Test
		@DisplayName("should physically delete post by id")
		fun `should hard delete by id`() {
			val postId = 100L
			whenever(postJpaRepository.hardDeleteById(postId)).thenReturn(1)

			adapter.hardDeleteById(postId)

			verify(postJpaRepository, times(1)).hardDeleteById(postId)
		}

		@Test
		@DisplayName("should physically delete all posts for user")
		fun `should hard delete by user id`() {
			val userId = 1L
			whenever(postJpaRepository.hardDeleteByUserId(userId)).thenReturn(1)

			adapter.hardDeleteByUserId(userId)

			verify(postJpaRepository, times(1)).hardDeleteByUserId(userId)
		}
	}

	@Nested
	@DisplayName("ExistsById Tests")
	inner class ExistsByIdTests {
		@Test
		@DisplayName("should return true when post exists")
		fun `should return true when exists`() {
			val postId = 100L
			whenever(postJpaRepository.existsById(postId)).thenReturn(true)

			val result = adapter.existsById(postId)

			assertTrue(result)
			verify(postJpaRepository, times(1)).existsById(postId)
		}

		@Test
		@DisplayName("should return false when post does not exist")
		fun `should return false when not exists`() {
			val postId = 999L
			whenever(postJpaRepository.existsById(postId)).thenReturn(false)

			val result = adapter.existsById(postId)

			assertFalse(result)
			verify(postJpaRepository, times(1)).existsById(postId)
		}
	}
}
