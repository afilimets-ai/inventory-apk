package com.inventory.sync.provider

import com.inventory.sftp.SftpClientI
import com.inventory.sync.SyncFormat
import com.inventory.sync.SyncImportResult
import com.inventory.sync.SyncProviderType
import com.inventory.sync.SyncResult
import com.inventory.sync.SyncSettings
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class SftpProviderTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val settings = SyncSettings(
        providerType = SyncProviderType.SFTP,
        host = "sftp.example.com",
        port = 22,
        username = "user",
        password = "pass",
        path = "/uploads"
    )

    @Test
    fun `export returns failure when init fails`() = runBlocking {
        val client = mock<SftpClientI>()
        whenever(client.init()).thenReturn(false)
        whenever(client.lastError()).thenReturn("Init error")

        val provider = SftpProvider(settings, client, tempFolder.root)
        val result = provider.export("data".toByteArray(), SyncFormat.CSV, "inventory")

        assertTrue(result is SyncResult.Failure)
        assertEquals("SFTP init failed: Init error", (result as SyncResult.Failure).message)
    }

    @Test
    fun `export returns failure when connect fails`() = runBlocking {
        val client = mock<SftpClientI>()
        whenever(client.init()).thenReturn(true)
        whenever(client.connect("sftp.example.com", 22, "user", "pass")).thenReturn(false)
        whenever(client.lastError()).thenReturn("Connection refused")

        val provider = SftpProvider(settings, client, tempFolder.root)
        val result = provider.export("data".toByteArray(), SyncFormat.CSV, "inventory")

        assertTrue(result is SyncResult.Failure)
        assertTrue((result as SyncResult.Failure).message.contains("Connection refused"))
    }

    @Test
    fun `export returns success on happy path`() = runBlocking {
        val client = mock<SftpClientI>()
        whenever(client.init()).thenReturn(true)
        whenever(client.connect(any(), any(), any(), any())).thenReturn(true)
        whenever(client.putFile(any(), any(), any())).thenReturn(true)
        whenever(client.disconnect()).thenReturn(true)
        whenever(client.free()).thenReturn(true)

        val provider = SftpProvider(settings, client, tempFolder.root)
        val result = provider.export("test,data\n".toByteArray(), SyncFormat.CSV, "inventory")

        assertEquals(SyncResult.Success, result)
    }

    @Test
    fun `export returns failure when putFile fails`() = runBlocking {
        val client = mock<SftpClientI>()
        whenever(client.init()).thenReturn(true)
        whenever(client.connect(any(), any(), any(), any())).thenReturn(true)
        whenever(client.putFile(any(), any(), any())).thenReturn(false)
        whenever(client.lastError()).thenReturn("Permission denied")
        whenever(client.disconnect()).thenReturn(true)
        whenever(client.free()).thenReturn(true)

        val provider = SftpProvider(settings, client, tempFolder.root)
        val result = provider.export("data".toByteArray(), SyncFormat.CSV, "inventory")

        assertTrue(result is SyncResult.Failure)
        assertTrue((result as SyncResult.Failure).message.contains("Permission denied"))
    }

    @Test
    fun `import returns success when getFile succeeds`() = runBlocking {
        val client = mock<SftpClientI>()
        whenever(client.init()).thenReturn(true)
        whenever(client.connect(any(), any(), any(), any())).thenReturn(true)
        whenever(client.getFile(any(), any(), any())).thenAnswer { invocation ->
            val localPath = invocation.getArgument<String>(1)
            java.io.File(localPath).writeText("col1,col2\nA,B")
            true
        }
        whenever(client.disconnect()).thenReturn(true)
        whenever(client.free()).thenReturn(true)

        val provider = SftpProvider(settings, client, tempFolder.root)
        val result = provider.import(SyncFormat.CSV, "inventory")

        assertTrue(result is SyncImportResult.Success)
        val content = String((result as SyncImportResult.Success).data)
        assertEquals("col1,col2\nA,B", content)
    }

    @Test
    fun `import returns failure when getFile fails`() = runBlocking {
        val client = mock<SftpClientI>()
        whenever(client.init()).thenReturn(true)
        whenever(client.connect(any(), any(), any(), any())).thenReturn(true)
        whenever(client.getFile(any(), any(), any())).thenReturn(false)
        whenever(client.lastError()).thenReturn("No such file")
        whenever(client.disconnect()).thenReturn(true)
        whenever(client.free()).thenReturn(true)

        val provider = SftpProvider(settings, client, tempFolder.root)
        val result = provider.import(SyncFormat.CSV, "inventory")

        assertTrue(result is SyncImportResult.Failure)
        assertTrue((result as SyncImportResult.Failure).message.contains("No such file"))
    }
}
