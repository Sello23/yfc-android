package com.yourfitness.common.network

import android.util.Base64
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import com.yourfitness.common.data.TokenStorage
import com.yourfitness.common.data.dao.ProfileDao
import com.yourfitness.common.domain.analytics.SpoofAnalyticsManager
import com.yourfitness.common.domain.analytics.SpoofCheckEvents
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import timber.log.Timber
import java.security.MessageDigest
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import com.yourfitness.common.BuildConfig.BACKEND_SECURE_KEY

@Singleton
class YFCSecureInterceptor @Inject constructor(
    private val profileDao: ProfileDao,
    private val tokenStorage: TokenStorage,
    private val analyticsManager: SpoofAnalyticsManager,
) : Interceptor {

    private var data: String? = null

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        runBlocking(Dispatchers.IO) {
            data = profileDao.readProfile()?.phoneNumber
            if (data.isNullOrBlank()) {
                trackPossibleSpoof(chain.request())
                data = tokenStorage.accessToken?.let { extractPhoneFromToken(it) }
                trackPossibleSpoof(chain.request(), true)
            }
        }
        if (data == null) return chain.proceed(request)
        val updatedRequest = setSecureHeaders(data.orEmpty(), request)

        return chain.proceed(updatedRequest)
    }

    private fun setSecureHeaders(data: String, originalRequest: Request): Request {
        val guid = UUID.randomUUID()
        val userAgentData = "${LABEL}_$guid"
        val hashInput = "$userAgentData$BACKEND_SECURE_KEY$data"
        val hash = hashInput.sha256().base64().replace("\n", "")

        return originalRequest.newBuilder().header(USER_AGENT, userAgentData)
            .header(AUTHENTICATION, hash).build()
    }

    private fun extractPhoneFromToken(token: String): String? {
        return try {
            String(Base64.decode(token.split(".")[1], Base64.DEFAULT), charset("UTF-8"))
                .fromJwtJson()
                .phone
        } catch (e: Exception) {
            Timber.e(e)
            null
        }
    }

    private suspend fun trackPossibleSpoof(request: Request, isJwt: Boolean = false) = withContext(Dispatchers.IO){
//        if (!data.isNullOrBlank()) return@withContext
        analyticsManager.trackEvent(SpoofCheckEvents.phoneMissed(
            request.url.encodedPath,
            request.headers.joinToString { ";\n" },
            data,
            profileDao.countRows() <= 0,
            isJwt,
        ))
    }

    companion object {
        private const val USER_AGENT = "User-Agent"
        private const val AUTHENTICATION = "Authentication"

        private const val LABEL = "android"
    }
}

private fun ByteArray.toHex(): String {
    return joinToString("") { "%02x".format(it) }
}

private fun String.base64(): String {
    return Base64.encodeToString(this.toByteArray(), Base64.DEFAULT)
}

private fun String.fromJwtJson(): JwtPayload {
    val type = object : TypeToken<JwtPayload>() {}.type
    return Gson().fromJson(this, type)
}

private fun String.sha256(): String {
    val bytes = MessageDigest.getInstance("SHA-256").digest(this.toByteArray())

    return bytes.toHex()
}

private data class JwtPayload(
    @SerializedName("PhoneNumber") val phone: String
)
