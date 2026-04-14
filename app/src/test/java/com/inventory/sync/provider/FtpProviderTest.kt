package com.inventory.sync.provider

import com.inventory.sync.SyncFormat
import com.inventory.sync.SyncImportResult
import com.inventory.sync.SyncProviderType
import com.inventory.sync.SyncResult
import com.inventory.sync.SyncSettings
import kotlinx.coroutines.runBlocking
import org.apache.commons.net.ftp.FTPClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class FtpProviderTest {

    @Test
    fun `export returns failure when ftp login fails`() = runBlocking {
        val ftpClient = mock<FTPClient>()
        whenever(ftpClient.login("user", "pass")).thenReturn(false)
        whenever(ftpClient.replyString).thenReturn("530 Login incorrect")
        whenever(ftpClient.isConnected).thenReturn(false)

        val provider = FtpProvider(
            settings = SyncSettings(
                providerType = SyncProviderType.FTP,
                host = "localhost",
                username = "user",
                password = "pass"
            ),
            ftpClientFactory = { ftpClient }
        )

        val result = provider.export("data".toByteArray(), SyncFormat.CSV, "inventory")

        assertTrue(result is SyncResult.Failure)
        assertEquals("FTP login failed: 530 Login incorrect", (result as SyncResult.Failure).message)
    }

    @Test
    fun `import returns failure when ftp login fails`() = runBlocking {
        val ftpClient = mock<FTPClient>()
        whenever(ftpClient.login("user", "pass")).thenReturn(false)
        whenever(ftpClient.replyString).thenReturn("530 Login incorrect")
        whenever(ftpClient.isConnected).thenReturn(false)

        val provider = FtpProvider(
            settings = SyncSettings(
                providerType = SyncProviderType.FTP,
                host = "localhost",
                username = "user",
                password = "pass"
            ),
            ftpClientFactory = { ftpClient }
        )

        val result = provider.import(SyncFormat.CSV, "inventory")

        assertTrue(result is SyncImportResult.Failure)
        assertEquals("FTP login failed: 530 Login incorrect", (result as SyncImportResult.Failure).message)
    }
}
