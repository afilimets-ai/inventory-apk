package com.inventory.sync

import android.content.Context
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

private const val PREFS_NAME = "sync_settings"

@Singleton
class SyncSettingsManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getSettings(type: SyncProviderType): SyncSettings {
        val json = prefs.getString(type.name, null)
        return if (json != null) {
            gson.fromJson(json, SyncSettings::class.java)
        } else {
            SyncSettings(providerType = type)
        }
    }

    fun saveSettings(settings: SyncSettings) {
        prefs.edit()
            .putString(settings.providerType.name, gson.toJson(settings))
            .apply()
    }

    /** Повертає всі провайдери де увімкнено імпорт */
    fun getImportProviders(): List<SyncSettings> =
        SyncProviderType.entries.map { getSettings(it) }.filter { it.isImportEnabled }

    /** Повертає всі провайдери де увімкнено експорт */
    fun getExportProviders(): List<SyncSettings> =
        SyncProviderType.entries.map { getSettings(it) }.filter { it.isExportEnabled }

    /** Зберегти вибір імпорту/експорту для провайдера (не змінюючи інші налаштування) */
    fun setProviderEnabled(type: SyncProviderType, importEnabled: Boolean, exportEnabled: Boolean) {
        val current = getSettings(type)
        saveSettings(current.copy(isImportEnabled = importEnabled, isExportEnabled = exportEnabled))
    }
}
