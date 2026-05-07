package com.inventory.network

import android.content.Context
import com.inventory.security.SecurePreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthTokenProvider @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = SecurePreferences.create(context, "inventory_prefs")

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    fun setToken(token: String) {
        prefs.edit().putString(KEY_TOKEN, token).apply()
    }

    fun clearToken() {
        prefs.edit().remove(KEY_TOKEN).apply()
    }

    fun hasToken(): Boolean = getToken() != null

    companion object {
        private const val KEY_TOKEN = "auth_token"
    }
}
