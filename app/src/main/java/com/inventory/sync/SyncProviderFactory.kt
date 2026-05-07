package com.inventory.sync

import com.inventory.sync.provider.EmailProvider
import com.inventory.sync.provider.FtpProvider
import com.inventory.sync.provider.SftpProvider
import com.inventory.sync.provider.GoogleDriveProvider
import com.inventory.sync.provider.HttpApiProvider
import com.inventory.sync.provider.LocalFolderProvider
import com.inventory.sync.provider.OneCProvider
import com.inventory.sync.provider.OneDriveProvider
import com.inventory.sync.provider.TelegramProvider
import com.inventory.sync.provider.WebDavProvider
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.OkHttpClient
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncProviderFactory @Inject constructor(
    private val settingsManager: SyncSettingsManager,
    private val okHttpClient: OkHttpClient,
    @ApplicationContext private val context: Context
) {
    fun create(type: SyncProviderType): SyncProvider {
        val settings = settingsManager.getSettings(type)
        return when (type) {
            SyncProviderType.LOCAL_FOLDER -> LocalFolderProvider(settings, context)
            SyncProviderType.HTTP_API -> HttpApiProvider(settings, okHttpClient)
            SyncProviderType.FTP -> FtpProvider(settings)
            SyncProviderType.SFTP -> SftpProvider(settings = settings, cacheDir = context.cacheDir)
            SyncProviderType.WEBDAV -> WebDavProvider(settings)
            SyncProviderType.ONEDRIVE -> OneDriveProvider(settings)
            SyncProviderType.GOOGLE_DRIVE -> GoogleDriveProvider(settings)
            SyncProviderType.EMAIL -> EmailProvider(settings)
            SyncProviderType.TELEGRAM -> TelegramProvider(settings)
            SyncProviderType.ONE_C -> OneCProvider(settings)
        }
    }
}
