package com.andreromano.devjobboard.core

sealed class Outcome<out T> {
    data class Success<out T>(val data: T) : Outcome<T>()
    data class Failure(val error: Throwable) : Outcome<Nothing>()

    fun dataOrNull(): T? = (this as? Success)?.data
    fun errorOrNull(): Throwable? = (this as? Failure)?.error
}