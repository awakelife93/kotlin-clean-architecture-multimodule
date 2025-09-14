package com.example.demo.common

interface UseCase<in I, out O> {
	fun execute(input: I): O
}

interface NoInputUseCase<out O> {
	fun execute(): O
}

interface NoOutputUseCase<in I> {
	fun execute(input: I)
}
