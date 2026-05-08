package com.inventory.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object SecurePreferences {
    fun create(context: Context, name: String): SharedPreferences {
        val legacyPrefs = context.getSharedPreferences(name, Context.MODE_PRIVATE)
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        val encryptedPrefs = EncryptedSharedPreferences.create(
            context,
            "${name}_secure",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        if (!encryptedPrefs.getBoolean(KEY_MIGRATED, false)) {
            migrateLegacyPrefs(legacyPrefs, encryptedPrefs)
        }

        return encryptedPrefs
    }

    private fun migrateLegacyPrefs(
        legacyPrefs: SharedPreferences,
        encryptedPrefs: SharedPreferences
    ) {
        val editor = encryptedPrefs.edit()
        for ((key, value) in legacyPrefs.all) {
            when (value) {
                is Boolean -> editor.putBoolean(key, value)
                is Float -> editor.putFloat(key, value)
                is Int -> editor.putInt(key, value)
                is Long -> editor.putLong(key, value)
                is String -> editor.putString(key, value)
                is Set<*> -> editor.putStringSet(key, value.filterIsInstance<String>().toSet())
            }
        }
        if (editor.putBoolean(KEY_MIGRATED, true).commit()) {
            legacyPrefs.edit().clear().apply()
        }
    }

    private const val KEY_MIGRATED = "__legacy_migrated"
}
