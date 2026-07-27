package com.silverymusic.app.data.network

import com.silverymusic.app.data.DataError
import com.silverymusic.app.data.DataResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

/**
 * Runs a Jamendo call on [dispatcher] and folds both transport failures and the
 * envelope's own `headers.status` into [DataResult]. Nothing throws out of here
 * except cancellation.
 */
internal suspend fun <T> jamendoResults(
    dispatcher: CoroutineDispatcher,
    call: suspend () -> JamendoEnvelope<T>,
): DataResult<List<T>> = withContext(dispatcher) {
    try {
        val envelope = call()
        val headers = envelope.headers
        if (headers.isSuccess) {
            DataResult.Success(envelope.results)
        } else {
            DataResult.Failure(headers.toDataError())
        }
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (throwable: Throwable) {
        DataResult.Failure(throwable.toDataError())
    }
}

private fun JamendoHeaders.toDataError(): DataError = when (code) {
    // Code 11 is "your credentials are unknown or suspended" — the one failure
    // the user can actually fix, so it gets its own error.
    JamendoHeaders.CODE_BAD_CREDENTIALS -> DataError.NotConfigured
    null -> DataError.Unknown(errorMessage)
    else -> DataError.Http(code, errorMessage)
}
