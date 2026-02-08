package com.cralert.app.data.remote

data class ApiResult<T>(
    val data: T,
    val httpCode: Int?,
    val error: String?,
    val success: Boolean
) {
    companion object {
        fun <T> success(data: T, httpCode: Int?): ApiResult<T> {
            return ApiResult(data = data, httpCode = httpCode, error = null, success = true)
        }

        fun <T> failure(data: T, httpCode: Int?, error: String?): ApiResult<T> {
            return ApiResult(data = data, httpCode = httpCode, error = error, success = false)
        }
    }
}
