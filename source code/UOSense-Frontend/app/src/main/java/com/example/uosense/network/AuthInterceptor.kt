import android.content.Context
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val context: Context) : Interceptor {

    private val tokenManager = TokenManager(context)

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val accessToken = tokenManager.getAccessToken()

        if (accessToken.isNullOrEmpty()) {
            throw IllegalStateException("Access token is missing")
        }

        val modifiedRequest = originalRequest.newBuilder()
            .addHeader("Authorization", "Bearer $accessToken")
            .build()

        val response = chain.proceed(modifiedRequest)

        // Access Token이 만료되었을 경우
        if (response.code == 401) {
            response.close()

            // Suspend 함수 호출을 위한 runBlocking 사용
            val isTokenRefreshed = runBlocking {
                tokenManager.refreshAccessToken()
            }

            if (isTokenRefreshed) {
                val newAccessToken = tokenManager.getAccessToken()
                val newRequest = originalRequest.newBuilder()
                    .addHeader("Authorization", "Bearer $newAccessToken")
                    .build()
                return chain.proceed(newRequest)
            }
        }

        return response
    }
}
