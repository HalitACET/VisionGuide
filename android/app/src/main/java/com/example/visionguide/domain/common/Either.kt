package com.example.visionguide.domain.common

sealed class Either<out L, out R> {
    data class Left<out L>(val value: L) : Either<L, Nothing>()
    data class Right<out R>(val value: R) : Either<Nothing, R>()

    inline fun <T> map(transform: (R) -> T): Either<L, T> = when (this) {
        is Left -> Left(this.value) as Either<L, T>
        is Right -> Right(transform(value))
    }

    inline fun <T> mapLeft(transform: (L) -> T): Either<T, R> = when (this) {
        is Left -> Left(transform(value))
        is Right -> Right(this.value) as Either<T, R>
    }

    inline fun <T> flatMap(transform: (R) -> Either<@UnsafeVariance L, T>): Either<L, T> = when (this) {
        is Left -> Left(this.value) as Either<L, T>
        is Right -> transform(value)
    }

    inline fun <T> fold(leftOp: (L) -> T, rightOp: (R) -> T): T = when (this) {
        is Left -> leftOp(value)
        is Right -> rightOp(value)
    }

    fun isLeft(): Boolean = this is Left
    fun isRight(): Boolean = this is Right

    fun getOrNull(): R? = (this as? Right)?.value
    fun errorOrNull(): L? = (this as? Left)?.value

    companion object {
        fun <L, R> left(value: L): Either<L, R> = Left(value)
        fun <L, R> right(value: R): Either<L, R> = Right(value)
    }
}
