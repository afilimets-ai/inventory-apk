package com.inventory.network

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val code: Int, val message: String) : ApiResult<Nothing>()
    data class NetworkError(val cause: Throwable) : ApiResult<Nothing>()

    val isSuccess get() = this is Success
    val isNetworkError get() = this is NetworkError

    fun getOrNull(): T? = (this as? Success)?.data
}

suspend fun <T> safeApiCall(call: suspend () -> T): ApiResult<T> {
    return try {
        ApiResult.Success(call())
    } catch (e: retrofit2.HttpException) {
        ApiResult.Error(
            code = e.code(),
            message = e.response()?.errorBody()?.string() ?: e.message()
        )
    } catch (e: java.io.IOException) {
        ApiResult.NetworkError(cause = e)
    } catch (e: Exception) {
        ApiResult.NetworkError(cause = e)
    }
}
