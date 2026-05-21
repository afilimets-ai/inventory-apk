package com.inventory.sync.provider

import android.content.Context
import com.inventory.sync.SyncFormat
import com.inventory.sync.SyncImportResult
import com.inventory.sync.SyncResult
import com.inventory.sync.SyncProviderType
import com.inventory.sync.SyncSettings
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.mockito.kotlin.mock

class LocalFolderProviderTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun `import reads file from direct folder path`() = runTest {
        val dir = tempFolder.newFolder("sync")
        dir.resolve("inventory_import.csv").writeText(
            "barcode,name,quantity,unit\n4820001112223,Тестовий цукор,5,кг\n"
        )
        val provider = providerFor(dir.absolutePath)

        val result = provider.import(SyncFormat.CSV, "inventory_import")

        assertTrue(result is SyncImportResult.Success)
        assertEquals(
            "barcode,name,quantity,unit\n4820001112223,Тестовий цукор,5,кг\n",
            (result as SyncImportResult.Success).data.toString(Charsets.UTF_8)
        )
    }

    @Test
    fun `import accepts file name with extension`() = runTest {
        val dir = tempFolder.newFolder("sync")
        dir.resolve("custom_catalog.csv").writeText("barcode,name\n123,Widget\n")
        val provider = providerFor(dir.absolutePath)

        val result = provider.import(SyncFormat.CSV, "custom_catalog.csv")

        assertTrue(result is SyncImportResult.Success)
        assertEquals(
            "barcode,name\n123,Widget\n",
            (result as SyncImportResult.Success).data.toString(Charsets.UTF_8)
        )
    }

    @Test
    fun `export writes file to direct folder path`() = runTest {
        val dir = tempFolder.newFolder("sync")
        val provider = providerFor(dir.absolutePath)

        val result = provider.export("barcode,name\n123,Widget\n".toByteArray(), SyncFormat.CSV, "inventory_export")

        assertEquals(SyncResult.Success, result)
        assertEquals("barcode,name\n123,Widget\n", dir.resolve("inventory_export.csv").readText())
    }

    @Test
    fun `discover import files returns newest direct path csv first`() = runTest {
        val dir = tempFolder.newFolder("sync")
        val older = dir.resolve("older.csv").apply { writeText("barcode,name\n1,A\n") }
        val newer = dir.resolve("newer.csv").apply { writeText("barcode,name\n2,B\n") }
        older.setLastModified(1_000)
        newer.setLastModified(2_000)
        val provider = providerFor(dir.absolutePath)

        assertEquals(listOf("newer", "older"), provider.discoverImportFiles(SyncFormat.CSV))
    }

    @Test
    fun `discover import files ignores hidden and trashed csv files`() = runTest {
        val dir = tempFolder.newFolder("sync")
        val visible = dir.resolve("catalog.csv").apply { writeText("barcode,name\n1,A\n") }
        val trashed = dir.resolve(".trashed-1-catalog.csv").apply { writeText("barcode,name\n2,B\n") }
        visible.setLastModified(1_000)
        trashed.setLastModified(2_000)
        val provider = providerFor(dir.absolutePath)

        assertEquals(listOf("catalog"), provider.discoverImportFiles(SyncFormat.CSV))
    }

    @Test
    fun `import returns actionable error when folder is blank`() = runTest {
        val result = providerFor("").import(SyncFormat.CSV, "inventory_import")

        assertEquals(
            SyncImportResult.Failure("Папку не вибрано. Вкажіть папку у налаштуваннях провайдера."),
            result
        )
    }

    @Test
    fun `import returns actionable error when direct path file is missing`() = runTest {
        val dir = tempFolder.newFolder("sync")

        val result = providerFor(dir.absolutePath).import(SyncFormat.CSV, "inventory_import")

        assertEquals(
            SyncImportResult.Failure("Файл не знайдено: ${dir.resolve("inventory_import.csv").absolutePath}"),
            result
        )
    }

    private fun providerFor(path: String): LocalFolderProvider =
        LocalFolderProvider(
            settings = SyncSettings(
                providerType = SyncProviderType.LOCAL_FOLDER,
                path = path
            ),
            context = mock<Context>()
        )
}
