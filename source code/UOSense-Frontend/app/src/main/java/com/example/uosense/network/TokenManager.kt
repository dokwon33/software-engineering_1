import android.content.Context
import android.util.Log
import com.example.uosense.network.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Base64
import android.content.Intent
import com.example.uosense.StartActivity

import org.json.JSONObject

class TokenManager(private val context: Context) {

    private val sharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    fun getAccessToken(): String? {
        return sharedPreferences.getString("access_token", null)
    }

    fun getRefreshToken(): String? {
        return sharedPreferences.getString("refresh_token", null)
    }

    // 토큰 저장
    fun saveAccessToken(token: String) {
        val editor = sharedPreferences.edit()
        editor.putString("access_token", token)
        editor.apply()
    }

    // 토큰 반납
    fun clearTokens() {
        sharedPreferences.edit().apply {
            remove("access_token")
            remove("refresh_token")
            apply()
        }
    }

    suspend fun refreshAccessToken(): Boolean {
        val refreshToken = getRefreshToken()
        if (refreshToken.isNullOrEmpty()) {
            Log.e("TokenManager", "Refresh token is missing")
            return false
        }

        return try {
            val response = RetrofitInstance.restaurantApi.reissueToken("refresh=$refreshToken")
            if (response.isSuccessful) {
                val newAccessToken = response.headers()["access"]?.removePrefix("Bearer ") ?: ""
                if (newAccessToken.isNotEmpty()) {
                    saveAccessToken(newAccessToken)
                    Log.d("TokenManager", "Access token refreshed successfully")
                    true
                } else {
                    Log.e("TokenManager", "Failed to retrieve new access token")
                    false
                }
            } else {
                Log.e("TokenManager", "Failed to refresh token: ${response.code()}")
                false
            }
        } catch (e: Exception) {
            Log.e("TokenManager", "Error refreshing token: ${e.message}")
            false
        }
    }

    suspend fun ensureValidAccessToken(): String? {
        val accessToken = getAccessToken()

        return if (!accessToken.isNullOrEmpty()) {
            if (refreshAccessToken()) {
                getAccessToken()
            } else {
                navigateToLogin()
                null
            }
        } else {
            navigateToLogin()
            null
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(context, StartActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        context.startActivity(intent)
    }

    fun getUserRoleFromToken(token: String): String? {
        return try {
            val payloadBase64 = token.split(".")[1]
            val payload = String(Base64.decode(payloadBase64, Base64.URL_SAFE))
            val jsonObject = JSONObject(payload)
            jsonObject.getString("role")  // "ADMIN" 또는 "USER"
        } catch (e: Exception) {
            Log.e("TokenManager", "토큰 파싱 오류: ${e.message}")
            null
        }
    }
}
