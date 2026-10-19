package com.florachain.core.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class EncryptedTokenManager(context: Context) {

    private val sharedPreferences: SharedPreferences by lazy {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            EncryptedSharedPreferences.create(
                context,
                PREF_FILE_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            // Fallback for emulators/older APIs where Keystore might encounter issues
            context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE)
        }
    }

    fun saveAuthData(token: String, userId: String, email: String, role: String, name: String) {
        sharedPreferences.edit()
            .putString(KEY_AUTH_TOKEN, token)
            .putString(KEY_USER_ID, userId)
            .putString(KEY_USER_EMAIL, email)
            .putString(KEY_USER_ROLE, role)
            .putString(KEY_USER_NAME, name)
            .apply()
    }

    fun getToken(): String? = sharedPreferences.getString(KEY_AUTH_TOKEN, null)

    fun getUserId(): String? = sharedPreferences.getString(KEY_USER_ID, null)

    fun getUserEmail(): String? = sharedPreferences.getString(KEY_USER_EMAIL, null)

    fun getUserRole(): String? = sharedPreferences.getString(KEY_USER_ROLE, null)

    fun getUserName(): String? = sharedPreferences.getString(KEY_USER_NAME, null)

    fun getBaseUrl(): String {
        return sharedPreferences.getString(KEY_BASE_URL, DEFAULT_BASE_URL) ?: DEFAULT_BASE_URL
    }

    fun setBaseUrl(url: String) {
        val cleanUrl = if (url.endsWith("/")) url else "$url/"
        sharedPreferences.edit().putString(KEY_BASE_URL, cleanUrl).apply()
    }

    fun clear() {
        val savedBaseUrl = getBaseUrl()
        sharedPreferences.edit().clear().apply()
        setBaseUrl(savedBaseUrl)
    }

    companion object {
        private const val PREF_FILE_NAME = "florachain_secure_prefs"
        private const val KEY_AUTH_TOKEN = "key_auth_token"
        private const val KEY_USER_ID = "key_user_id"
        private const val KEY_USER_EMAIL = "key_user_email"
        private const val KEY_USER_ROLE = "key_user_role"
        private const val KEY_USER_NAME = "key_user_name"
        private const val KEY_BASE_URL = "key_base_url"

        const val LOCAL_EMULATOR_BASE_URL = "http://10.0.2.2:8080/api/"
        const val CLOUD_BASE_URL = "https://blockchain-botanical-traceability.onrender.com/api/"
        const val DEFAULT_BASE_URL = CLOUD_BASE_URL
        const val WEB_SYNC_URL = "https://blockchain-botanical-traceability.vercel.app/data/products.json"
    }
}
