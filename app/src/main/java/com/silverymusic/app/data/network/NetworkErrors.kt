package com.silverymusic.app.data.network

import com.silverymusic.app.data.DataError
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * The single place platform exceptions become [DataError]. Callers wrap every
 * network call in [safeCall] so nothing below this line ever escapes the
 * repository boundary.
 */
internal fun Throwable.toDataError(): DataError = when (this) {
    is SocketTimeoutException -> DataError.Timeout
    is UnknownHostException -> DataError.Network
    is HttpException -> when (code()) {
        // Jamendo answers an unusable key with 401/403 rather than the envelope.
        401, 403 -> DataError.NotConfigured
        408, 504 -> DataError.Timeout
        else -> DataError.Http(code(), message())
    }
    is SerializationException -> DataError.Unknown("Unexpected response shape: ${message}")
    is IOException -> DataError.Network
    else -> DataError.Unknown(message)
}
