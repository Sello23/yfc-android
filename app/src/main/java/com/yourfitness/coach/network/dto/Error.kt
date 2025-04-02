package com.yourfitness.coach.network.dto

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import retrofit2.HttpException

data class ErrorResponse(
    @SerializedName("code") val code: Int? = null,
    @SerializedName("message") val message: String? = null
)

private fun replaceErrorMessage(message: String?): String? {
    return when (message) {
        "Incorrect code" -> "The code wasn't entered correctly"
        else -> message
    }
}

fun HttpException.toErrorResponse(): ErrorResponse {
    val type = object : TypeToken<ErrorResponse>(){}.type
    return Gson().fromJson(this.response()?.errorBody()?.string(), type)
}

fun HttpException.toException(): ServerException {
    return ServerException(replaceErrorMessage(toErrorResponse().message), this)
}

class ServerException(message: String?, error: Exception? = null) : Exception(message, error)

class SignUpCodeException(val codeType: String) : Exception()