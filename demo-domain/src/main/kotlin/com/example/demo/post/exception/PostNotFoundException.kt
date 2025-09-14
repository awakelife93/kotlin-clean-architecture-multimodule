package com.example.demo.post.exception

import com.example.demo.exception.EntityNotFoundException

class PostNotFoundException(
	postId: Long
) : EntityNotFoundException("Post not found. postId: $postId")
