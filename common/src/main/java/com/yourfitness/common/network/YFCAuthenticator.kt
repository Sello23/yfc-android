package com.yourfitness.common.network

import com.yourfitness.common.domain.session.TokenManager
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject

class YFCAuthenticator @Inject constructor(
    private val tokenManager: TokenManager
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        val accessToken = tokenManager.refreshToken()
        return if (accessToken != null) {
            newRequestWithAccessToken(response.request, accessToken)
        } else {
            null
        }
    }

    private fun newRequestWithAccessToken(request: Request, accessToken: String): Request {
        return request.newBuilder()
            .header("Authorization", "Bearer $accessToken")
            .build()
    }
}