package com.silverymusic.app.data

/**
 * Failures the UI can actually act on. Platform exceptions (IOException,
 * HttpException, SerializationException) are mapped to these at the repository
 * boundary and never leak past it.
 */
sealed interface DataError {
    /** No connectivity, DNS failure, or the socket dropped. */
    data object Network : DataError
    data object Timeout : DataError

    /** The Jamendo client_id is missing or the app registration was rejected. */
    data object NotConfigured : DataError

    data class Http(val code: Int, val message: String? = null) : DataError
    data class Unknown(val message: String? = null) : DataError
}

sealed interface DataResult<out T> {
    data class Success<T>(val data: T) : DataResult<T>
    data class Failure(val error: DataError) : DataResult<Nothing>
}

inline fun <T, R> DataResult<T>.map(transform: (T) -> R): DataResult<R> = when (this) {
    is DataResult.Success -> DataResult.Success(transform(data))
    is DataResult.Failure -> this
}

fun <T> DataResult<T>.getOrNull(): T? = (this as? DataResult.Success)?.data

fun <T> DataResult<T>.errorOrNull(): DataError? = (this as? DataResult.Failure)?.error
