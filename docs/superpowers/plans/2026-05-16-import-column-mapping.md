# Import Column Mapping Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add an interactive column-mapping wizard so users can map supplier file columns (CSV/XLSX/JSON) to inventory fields with header auto-detection and fuzzy auto-suggestions.

**Architecture:** New `sync/catalogimport/` package (data models + pure heuristic); `SyncSerializer` interface extended with `parsePreview`/`parseRaw`; new `applyMappedImport` on `InventoryRepository` (atomic Room transaction, AUDIT outbox, lookup-or-create FK); Compose wizard `ImportCatalogScreen` reached via NavHost route `"import_catalog"` from a button on `ScanScreen`.

**Tech Stack:** Kotlin, Jetpack Compose (Material3), Hilt, Room v3, Fastexcel, Gson, Mockito-Kotlin (tests), kotlinx.coroutines.test.

**Spec reference:** [docs/superpowers/specs/2026-05-16-import-column-mapping-design.md](../specs/2026-05-16-import-column-mapping-design.md)

---

## File Structure

**New files:**

```
app/src/main/java/com/inventory/sync/catalogimport/
├── TargetType.kt                 enum (4 entries)
├── TargetField.kt                data class + TargetFields registry
├── ColumnMapping.kt              data class (Serializable, persisted in SyncSettings)
├── ImportPreview.kt              data class (headerRow + sampleRows + heuristic flag)
├── ImportReport.kt               data class (insert/update/skip counts + reasons)
└── ColumnMappingHeuristic.kt     pure object (detectHasHeader + fuzzySuggestMapping + levenshtein)

app/src/main/java/com/inventory/ui/importcatalog/
├── ImportCatalogUiState.kt       sealed class (8 variants)
├── ImportCatalogViewModel.kt     @HiltViewModel, StateFlow<ImportCatalogUiState>
└── ImportCatalogScreen.kt        Compose host + 5 step composables
```

**Modified files:**

```
app/src/main/java/com/inventory/sync/serializer/SyncSerializer.kt     + parsePreview / + parseRaw
app/src/main/java/com/inventory/sync/serializer/CsvSerializer.kt      impl + windows-1251 fallback
app/src/main/java/com/inventory/sync/serializer/ExcelSerializer.kt    impl using fastexcel
app/src/main/java/com/inventory/sync/serializer/JsonSerializer.kt     impl using Gson + shared parseJsonObjects
app/src/main/java/com/inventory/data/db/dao/CategoryDao.kt            + getByName
app/src/main/java/com/inventory/data/db/dao/LocationDao.kt            + getByName
app/src/main/java/com/inventory/data/repository/InventoryRepository.kt        + applyMappedImport
app/src/main/java/com/inventory/data/repository/InventoryRepositoryImpl.kt    impl (one Room @Transaction)
app/src/main/java/com/inventory/sync/SyncSettings.kt                  + columnMapping field
app/src/main/java/com/inventory/sync/SyncEngine.kt                    conditional applyImport path
app/src/main/java/com/inventory/app/MainActivity.kt                   + composable("import_catalog")
app/src/main/java/com/inventory/ui/scan/ScanScreen.kt                 + onImportCatalogClick button
```

**Test files:**

```
app/src/test/java/com/inventory/sync/catalogimport/ColumnMappingHeuristicTest.kt
app/src/test/java/com/inventory/sync/serializer/CsvSerializerPreviewTest.kt
app/src/test/java/com/inventory/sync/serializer/ExcelSerializerPreviewTest.kt
app/src/test/java/com/inventory/sync/serializer/JsonSerializerPreviewTest.kt
app/src/test/java/com/inventory/data/repository/InventoryRepositoryMappedImportTest.kt
app/src/test/java/com/inventory/ui/importcatalog/ImportCatalogViewModelTest.kt
app/src/test/java/com/inventory/sync/SyncEngineTest.kt                            + 1 test
```

> **Note on package naming:** The spec uses directory `sync/import/`, but `import` is a Kotlin soft keyword that breaks tooling in package names. We use `com.inventory.sync.catalogimport` for the package while preserving the spec's intent.

---

### Task 1: Core data models

**Files:**
- Create: `app/src/main/java/com/inventory/sync/catalogimport/TargetType.kt`
- Create: `app/src/main/java/com/inventory/sync/catalogimport/TargetField.kt`
- Create: `app/src/main/java/com/inventory/sync/catalogimport/ColumnMapping.kt`
- Create: `app/src/main/java/com/inventory/sync/catalogimport/ImportPreview.kt`
- Create: `app/src/main/java/com/inventory/sync/catalogimport/ImportReport.kt`

Pure Kotlin data classes — no Android dependencies, no tests needed beyond compilation.

- [ ] **Step 1: Create TargetType.kt**

```kotlin
package com.inventory.sync.catalogimport

enum class TargetType { STRING, DOUBLE, LOOKUP_CATEGORY, LOOKUP_LOCATION }
```

- [ ] **Step 2: Create TargetField.kt**

```kotlin
package com.inventory.sync.catalogimport

data class TargetField(
    val id: String,
    val displayName: String,
    val type: TargetType,
    val isRequired: Boolean = false
)

object TargetFields {
    val BARCODE  = TargetField("barcode",      "Штрихкод",     TargetType.STRING,          isRequired = true)
    val NAME     = TargetField("name",         "Назва",        TargetType.STRING,          isRequired = true)
    val QUANTITY = TargetField("quantity",     "Кількість",    TargetType.DOUBLE)
    val UNIT     = TargetField("unit",         "Одиниця",      TargetType.STRING)
    val DESC     = TargetField("description",  "Опис",         TargetType.STRING)
    val MIN_QTY  = TargetField("min_quantity", "Мін. залишок", TargetType.DOUBLE)
    val NOTES    = TargetField("notes",        "Примітки",     TargetType.STRING)
    val CATEGORY = TargetField("category",     "Категорія",    TargetType.LOOKUP_CATEGORY)
    val LOCATION = TargetField("location",     "Локація",      TargetType.LOOKUP_LOCATION)

    val all: List<TargetField> = listOf(BARCODE, NAME, QUANTITY, UNIT, DESC, MIN_QTY, NOTES, CATEGORY, LOCATION)
    fun byId(id: String): TargetField? = all.firstOrNull { it.id == id }
}
```

- [ ] **Step 3: Create ColumnMapping.kt**

```kotlin
package com.inventory.sync.catalogimport

import java.io.Serializable

data class ColumnMapping(
    val treatFirstRowAsHeader: Boolean,
    val mapping: Map<Int, String?>   // file column index → TargetField.id (null = skip)
) : Serializable
```

- [ ] **Step 4: Create ImportPreview.kt**

```kotlin
package com.inventory.sync.catalogimport

data class ImportPreview(
    val headerRow: List<String?>,
    val sampleRows: List<List<String?>>,
    val detectedHasHeader: Boolean,
    val totalRowsEstimate: Int
)
```

- [ ] **Step 5: Create ImportReport.kt**

```kotlin
package com.inventory.sync.catalogimport

data class ImportReport(
    val insertedCount: Int,
    val updatedCount: Int,
    val skippedCount: Int,
    val skipReasons: List<String> = emptyList()
)
```

- [ ] **Step 6: Verify compilation**

Run: `./gradlew :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 7: Commit**

```
git add app/src/main/java/com/inventory/sync/catalogimport/
git commit -m "feat: add catalogimport data models (TargetField, ColumnMapping, ImportPreview, ImportReport)"
```

---

### Task 2: ColumnMappingHeuristic (pure algorithm)

**Files:**
- Create: `app/src/main/java/com/inventory/sync/catalogimport/ColumnMappingHeuristic.kt`
- Create: `app/src/test/java/com/inventory/sync/catalogimport/ColumnMappingHeuristicTest.kt`

- [ ] **Step 1: Write failing test**

File: `app/src/test/java/com/inventory/sync/catalogimport/ColumnMappingHeuristicTest.kt`

```kotlin
package com.inventory.sync.catalogimport

import org.junit.Assert.*
import org.junit.Test

class ColumnMappingHeuristicTest {

    @Test
    fun `detectHasHeader returns true for text headers`() {
        val rows = listOf(
            listOf("barcode", "name", "quantity"),
            listOf("123456789", "Widget", "10")
        )
        assertTrue(ColumnMappingHeuristic.detectHasHeader(rows))
    }

    @Test
    fun `detectHasHeader returns false when 50 percent or more cells in row 0 are numeric`() {
        val rows = listOf(
            listOf("4820001", "5.0", "100"),
            listOf("4820002", "3.0", "50")
        )
        assertFalse(ColumnMappingHeuristic.detectHasHeader(rows))
    }

    @Test
    fun `detectHasHeader returns true when under 50 percent cells in row 0 are numeric`() {
        val rows = listOf(
            listOf("barcode", "name", "5.0"),
            listOf("4820001", "Widget", "10")
        )
        assertTrue(ColumnMappingHeuristic.detectHasHeader(rows))
    }

    @Test
    fun `detectHasHeader returns true for empty rows`() {
        assertTrue(ColumnMappingHeuristic.detectHasHeader(emptyList()))
    }

    @Test
    fun `fuzzySuggestMapping exact id match`() {
        val headers = listOf("barcode", "name", "quantity")
        val result = ColumnMappingHeuristic.fuzzySuggestMapping(headers, TargetFields.all)
        assertEquals("barcode",  result[0])
        assertEquals("name",     result[1])
        assertEquals("quantity", result[2])
    }

    @Test
    fun `fuzzySuggestMapping Ukrainian displayName match`() {
        val headers = listOf("штрихкод", "назва", "кількість")
        val result = ColumnMappingHeuristic.fuzzySuggestMapping(headers, TargetFields.all)
        assertEquals("barcode",  result[0])
        assertEquals("name",     result[1])
        assertEquals("quantity", result[2])
    }

    @Test
    fun `fuzzySuggestMapping levenshtein typo barcod matches barcode`() {
        val result = ColumnMappingHeuristic.fuzzySuggestMapping(listOf("barcod"), TargetFields.all)
        assertEquals("barcode", result[0])
    }

    @Test
    fun `fuzzySuggestMapping contains match product_barcode`() {
        val result = ColumnMappingHeuristic.fuzzySuggestMapping(listOf("product_barcode"), TargetFields.all)
        assertEquals("barcode", result[0])
    }

    @Test
    fun `fuzzySuggestMapping underscore stripped before compare`() {
        val result = ColumnMappingHeuristic.fuzzySuggestMapping(listOf("min_quantity"), TargetFields.all)
        assertEquals("min_quantity", result[0])
    }

    @Test
    fun `fuzzySuggestMapping no match returns null`() {
        val result = ColumnMappingHeuristic.fuzzySuggestMapping(listOf("артикул", "залишок"), TargetFields.all)
        assertNull(result[0])
        assertNull(result[1])
    }
}
```

- [ ] **Step 2: Run test — verify it fails**

Run: `./gradlew :app:testDebugUnitTest --tests "com.inventory.sync.catalogimport.ColumnMappingHeuristicTest"`
Expected: FAILED (compilation error — `ColumnMappingHeuristic` not found)

- [ ] **Step 3: Create ColumnMappingHeuristic.kt**

```kotlin
package com.inventory.sync.catalogimport

object ColumnMappingHeuristic {

    /** Row 0 is treated as data (NOT a header) if at least 50 percent of its cells parse as numbers. */
    fun detectHasHeader(rows: List<List<String?>>): Boolean {
        val row0 = rows.firstOrNull() ?: return true
        if (row0.isEmpty()) return true
        val numericCount = row0.count { it?.trim()?.toDoubleOrNull() != null }
        return numericCount.toDouble() / row0.size < 0.5
    }

    /** For each header column, suggest a TargetField.id (or null when no match). */
    fun fuzzySuggestMapping(
        headers: List<String?>,
        targets: List<TargetField>
    ): Map<Int, String?> = headers.indices.associate { idx ->
        idx to headers[idx]?.let { bestMatch(it, targets) }
    }

    private fun bestMatch(header: String, targets: List<TargetField>): String? {
        val norm = normalize(header)
        // 1. Exact match (id OR displayName) after normalization
        targets.firstOrNull { normalize(it.id) == norm || normalize(it.displayName) == norm }
            ?.let { return it.id }
        // 2. Contains match in either direction
        targets.firstOrNull { t ->
            val nId = normalize(t.id); val nDisp = normalize(t.displayName)
            (nId.isNotEmpty() && (norm.contains(nId) || nId.contains(norm))) ||
            (nDisp.isNotEmpty() && (norm.contains(nDisp) || nDisp.contains(norm)))
        }?.let { return it.id }
        // 3. Levenshtein distance no greater than 2
        targets.firstOrNull { t ->
            levenshtein(norm, normalize(t.id)) <= 2 ||
            levenshtein(norm, normalize(t.displayName)) <= 2
        }?.let { return it.id }
        return null
    }

    private fun normalize(s: String): String = s.lowercase().filter { it.isLetter() }

    private fun levenshtein(a: String, b: String): Int {
        val dp = Array(a.length + 1) { IntArray(b.length + 1) }
        for (i in 0..a.length) dp[i][0] = i
        for (j in 0..b.length) dp[0][j] = j
        for (i in 1..a.length) for (j in 1..b.length)
            dp[i][j] = if (a[i-1] == b[j-1]) dp[i-1][j-1]
                       else 1 + minOf(dp[i-1][j], dp[i][j-1], dp[i-1][j-1])
        return dp[a.length][b.length]
    }
}
```

- [ ] **Step 4: Run test — verify it passes**

Run: `./gradlew :app:testDebugUnitTest --tests "com.inventory.sync.catalogimport.ColumnMappingHeuristicTest"`
Expected: BUILD SUCCESSFUL, 10 tests PASSED

- [ ] **Step 5: Commit**

```
git add app/src/main/java/com/inventory/sync/catalogimport/ColumnMappingHeuristic.kt
git add app/src/test/java/com/inventory/sync/catalogimport/ColumnMappingHeuristicTest.kt
git commit -m "feat: add ColumnMappingHeuristic with header detection and fuzzy mapping"
```

---

### Task 3: SyncSerializer interface + CsvSerializer implementation

**Files:**
- Modify: `app/src/main/java/com/inventory/sync/serializer/SyncSerializer.kt`
- Modify: `app/src/main/java/com/inventory/sync/serializer/CsvSerializer.kt`
- Modify: `app/src/main/java/com/inventory/sync/serializer/ExcelSerializer.kt` (compile stubs)
- Modify: `app/src/main/java/com/inventory/sync/serializer/JsonSerializer.kt` (compile stubs)
- Create: `app/src/test/java/com/inventory/sync/serializer/CsvSerializerPreviewTest.kt`

The interface change forces all 3 implementations to compile. We implement CSV fully here; Excel and JSON get `TODO("Task 4")` stubs to keep the build green.

- [ ] **Step 1: Write failing test for CsvSerializer**

File: `app/src/test/java/com/inventory/sync/serializer/CsvSerializerPreviewTest.kt`

```kotlin
package com.inventory.sync.serializer

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class CsvSerializerPreviewTest {

    private lateinit var serializer: CsvSerializer
    @Before fun setUp() { serializer = CsvSerializer() }

    @Test
    fun `parsePreview returns correct headerRow and sampleRows`() {
        val csv = "barcode,name,quantity\n123,Widget,5\n456,Gadget,10\n"
        val preview = serializer.parsePreview(csv.toByteArray())
        assertEquals(listOf("barcode", "name", "quantity"), preview.headerRow)
        assertEquals(2, preview.sampleRows.size)
        assertEquals(listOf("123", "Widget", "5"), preview.sampleRows[0])
        assertEquals(2, preview.totalRowsEstimate)
        assertTrue(preview.detectedHasHeader)
    }

    @Test
    fun `parsePreview respects sampleSize limit`() {
        val data = (1..20).joinToString("\n") { "$it,Item$it,$it" }
        val csv = "barcode,name,qty\n$data\n"
        val preview = serializer.parsePreview(csv.toByteArray(), sampleSize = 5)
        assertEquals(5, preview.sampleRows.size)
        assertEquals(20, preview.totalRowsEstimate)
    }

    @Test
    fun `parsePreview detects numeric row 0 as no-header`() {
        val csv = "4820001,5.0,100\n4820002,3.0,50\n"
        val preview = serializer.parsePreview(csv.toByteArray())
        assertFalse(preview.detectedHasHeader)
    }

    @Test
    fun `parsePreview returns empty preview for empty bytes`() {
        val preview = serializer.parsePreview(ByteArray(0))
        assertTrue(preview.headerRow.isEmpty())
        assertEquals(0, preview.totalRowsEstimate)
    }

    @Test
    fun `parsePreview falls back to windows-1251 on mojibake`() {
        // Header in UTF-8 plus a row with bytes that are NOT valid UTF-8.
        val header = "barcode,name\n".toByteArray(Charsets.UTF_8)
        // Windows-1251 bytes for some Cyrillic letters — invalid UTF-8 sequence.
        val win1251Row = byteArrayOf(
            0xC0.toByte(), 0xC1.toByte(), ','.code.toByte(),
            0xC2.toByte(), 0xC3.toByte(), '\n'.code.toByte()
        )
        val data = header + win1251Row
        val preview = serializer.parsePreview(data)
        assertEquals(1, preview.totalRowsEstimate)
    }

    @Test
    fun `parseRaw with treatFirstRowAsHeader=true skips row 0`() {
        val csv = "barcode,name,quantity\n123,Widget,5\n456,Gadget,10\n"
        val rows = serializer.parseRaw(csv.toByteArray(), treatFirstRowAsHeader = true)
        assertEquals(2, rows.size)
        assertEquals(listOf("123", "Widget", "5"), rows[0])
    }

    @Test
    fun `parseRaw with treatFirstRowAsHeader=false includes row 0`() {
        val csv = "4820001,Widget,5\n4820002,Gadget,10\n"
        val rows = serializer.parseRaw(csv.toByteArray(), treatFirstRowAsHeader = false)
        assertEquals(2, rows.size)
        assertEquals("4820001", rows[0][0])
    }

    @Test
    fun `parseRaw pads short rows with null up to column count`() {
        val csv = "barcode,name,quantity\n123,Widget\n"
        val rows = serializer.parseRaw(csv.toByteArray(), treatFirstRowAsHeader = true)
        assertEquals(1, rows.size)
        assertEquals(3, rows[0].size)
        assertNull(rows[0][2])
    }
}
```

- [ ] **Step 2: Run test — verify it fails**

Run: `./gradlew :app:testDebugUnitTest --tests "com.inventory.sync.serializer.CsvSerializerPreviewTest"`
Expected: FAILED (compilation error)

- [ ] **Step 3: Extend SyncSerializer interface**

Replace contents of `app/src/main/java/com/inventory/sync/serializer/SyncSerializer.kt`:

```kotlin
package com.inventory.sync.serializer

import com.inventory.sync.SyncFormat
import com.inventory.sync.catalogimport.ImportPreview

interface SyncSerializer {
    val format: SyncFormat
    fun serialize(items: List<Map<String, Any?>>): ByteArray
    fun deserialize(data: ByteArray): List<Map<String, Any?>>
    fun parsePreview(data: ByteArray, sampleSize: Int = 10): ImportPreview
    fun parseRaw(data: ByteArray, treatFirstRowAsHeader: Boolean): List<List<String?>>
}
```

- [ ] **Step 4: Add compile stubs to ExcelSerializer**

In `ExcelSerializer.kt`, add this import:
```kotlin
import com.inventory.sync.catalogimport.ImportPreview
```

Add these methods inside the class (before closing `}`):
```kotlin
    override fun parsePreview(data: ByteArray, sampleSize: Int): ImportPreview = TODO("Implemented in Task 4")
    override fun parseRaw(data: ByteArray, treatFirstRowAsHeader: Boolean): List<List<String?>> = TODO("Implemented in Task 4")
```

- [ ] **Step 5: Add compile stubs to JsonSerializer**

In `JsonSerializer.kt`, add this import:
```kotlin
import com.inventory.sync.catalogimport.ImportPreview
```

Add these methods inside the class (before closing `}`):
```kotlin
    override fun parsePreview(data: ByteArray, sampleSize: Int): ImportPreview = TODO("Implemented in Task 4")
    override fun parseRaw(data: ByteArray, treatFirstRowAsHeader: Boolean): List<List<String?>> = TODO("Implemented in Task 4")
```

- [ ] **Step 6: Implement CsvSerializer.parsePreview, parseRaw, decodeWithFallback**

Add these imports at top of `CsvSerializer.kt`:
```kotlin
import com.inventory.sync.catalogimport.ColumnMappingHeuristic
import com.inventory.sync.catalogimport.ImportPreview
import java.nio.charset.Charset
```

Add these methods inside `CsvSerializer` class:
```kotlin
    override fun parsePreview(data: ByteArray, sampleSize: Int): ImportPreview {
        if (data.isEmpty()) return ImportPreview(emptyList(), emptyList(), true, 0)
        val text = decodeWithFallback(data)
        val lines = text.lines().filter { it.isNotBlank() }
        if (lines.isEmpty()) return ImportPreview(emptyList(), emptyList(), true, 0)
        val headerRow = parseCsvLine(lines[0])
        val dataLines = lines.drop(1)
        val sampleRows = dataLines.take(sampleSize).map { line ->
            parseCsvLine(line).map { v -> v.takeIf { it.isNotEmpty() } }
        }
        val rowsForHeuristic = listOf(headerRow.map { it.takeIf { v -> v.isNotEmpty() } }) + sampleRows
        return ImportPreview(
            headerRow = headerRow,
            sampleRows = sampleRows,
            detectedHasHeader = ColumnMappingHeuristic.detectHasHeader(rowsForHeuristic),
            totalRowsEstimate = dataLines.size
        )
    }

    override fun parseRaw(data: ByteArray, treatFirstRowAsHeader: Boolean): List<List<String?>> {
        if (data.isEmpty()) return emptyList()
        val lines = decodeWithFallback(data).lines().filter { it.isNotBlank() }
        val columnCount = lines.firstOrNull()?.let { parseCsvLine(it).size } ?: 0
        val dataLines = if (treatFirstRowAsHeader) lines.drop(1) else lines
        return dataLines.map { line ->
            val cells = parseCsvLine(line)
            List(maxOf(columnCount, cells.size)) { i ->
                cells.getOrNull(i)?.takeIf { it.isNotEmpty() }
            }
        }
    }

    private fun decodeWithFallback(data: ByteArray): String {
        val utf8 = data.toString(Charsets.UTF_8)
        val sample = utf8.take(1024)
        if (sample.isEmpty()) return utf8
        val replacements = sample.count { it == '�' }
        return if (replacements.toDouble() / sample.length >= 0.01)
            data.toString(Charset.forName("windows-1251"))
        else utf8
    }
```

- [ ] **Step 7: Run test — verify it passes**

Run: `./gradlew :app:testDebugUnitTest --tests "com.inventory.sync.serializer.CsvSerializerPreviewTest"`
Expected: BUILD SUCCESSFUL, 8 tests PASSED

- [ ] **Step 8: Commit**

```
git add app/src/main/java/com/inventory/sync/serializer/
git add app/src/test/java/com/inventory/sync/serializer/CsvSerializerPreviewTest.kt
git commit -m "feat: extend SyncSerializer interface; implement CsvSerializer parsePreview/parseRaw with windows-1251 fallback"
```

---

### Task 4: ExcelSerializer + JsonSerializer parsePreview/parseRaw

**Files:**
- Modify: `app/src/main/java/com/inventory/sync/serializer/ExcelSerializer.kt`
- Modify: `app/src/main/java/com/inventory/sync/serializer/JsonSerializer.kt`
- Create: `app/src/test/java/com/inventory/sync/serializer/ExcelSerializerPreviewTest.kt`
- Create: `app/src/test/java/com/inventory/sync/serializer/JsonSerializerPreviewTest.kt`

- [ ] **Step 1: Write failing test for ExcelSerializer**

File: `app/src/test/java/com/inventory/sync/serializer/ExcelSerializerPreviewTest.kt`

```kotlin
package com.inventory.sync.serializer

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ExcelSerializerPreviewTest {

    private lateinit var serializer: ExcelSerializer
    @Before fun setUp() { serializer = ExcelSerializer() }

    private fun excel(vararg rows: Map<String, String>): ByteArray =
        serializer.serialize(rows.map { it.toMap<String, Any?>() })

    @Test
    fun `parsePreview returns headerRow and sampleRows`() {
        val data = excel(
            mapOf("barcode" to "123", "name" to "Widget"),
            mapOf("barcode" to "456", "name" to "Gadget")
        )
        val preview = serializer.parsePreview(data)
        assertEquals(listOf("barcode", "name"), preview.headerRow)
        assertEquals(2, preview.sampleRows.size)
        assertEquals("123", preview.sampleRows[0][0])
        assertEquals(2, preview.totalRowsEstimate)
        assertTrue(preview.detectedHasHeader)
    }

    @Test
    fun `parsePreview respects sampleSize`() {
        val rows = (1..15).map { mapOf("barcode" to "$it", "name" to "Item$it") }
        val preview = serializer.parsePreview(serializer.serialize(rows), sampleSize = 5)
        assertEquals(5, preview.sampleRows.size)
        assertEquals(15, preview.totalRowsEstimate)
    }

    @Test
    fun `parseRaw with header=true skips row 0`() {
        val data = excel(
            mapOf("barcode" to "123", "name" to "Widget"),
            mapOf("barcode" to "456", "name" to "Gadget")
        )
        val rows = serializer.parseRaw(data, treatFirstRowAsHeader = true)
        assertEquals(2, rows.size)
        assertEquals("123", rows[0][0])
    }

    @Test
    fun `parseRaw with header=false includes row 0`() {
        val data = excel(
            mapOf("barcode" to "123", "name" to "Widget"),
            mapOf("barcode" to "456", "name" to "Gadget")
        )
        val rows = serializer.parseRaw(data, treatFirstRowAsHeader = false)
        assertEquals(3, rows.size)
        assertEquals("barcode", rows[0][0])
    }
}
```

- [ ] **Step 2: Write failing test for JsonSerializer**

File: `app/src/test/java/com/inventory/sync/serializer/JsonSerializerPreviewTest.kt`

```kotlin
package com.inventory.sync.serializer

import com.google.gson.Gson
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class JsonSerializerPreviewTest {

    private lateinit var serializer: JsonSerializer
    @Before fun setUp() { serializer = JsonSerializer(Gson()) }

    @Test
    fun `parsePreview returns object keys as headerRow`() {
        val json = """[{"barcode":"123","name":"Widget"},{"barcode":"456","name":"Gadget"}]"""
        val preview = serializer.parsePreview(json.toByteArray())
        assertEquals(listOf("barcode", "name"), preview.headerRow)
        assertEquals(1, preview.sampleRows.size)
        assertEquals("456", preview.sampleRows[0][0])
        assertEquals(1, preview.totalRowsEstimate)
        assertTrue(preview.detectedHasHeader)
    }

    @Test
    fun `parsePreview returns empty preview for empty array`() {
        val preview = serializer.parsePreview("[]".toByteArray())
        assertTrue(preview.headerRow.isEmpty())
        assertEquals(0, preview.totalRowsEstimate)
    }

    @Test
    fun `parseRaw returns all objects regardless of treatFirstRowAsHeader`() {
        val json = """[{"barcode":"123","name":"Widget"},{"barcode":"456","name":"Gadget"}]"""
        val rowsTrue  = serializer.parseRaw(json.toByteArray(), treatFirstRowAsHeader = true)
        val rowsFalse = serializer.parseRaw(json.toByteArray(), treatFirstRowAsHeader = false)
        assertEquals(rowsTrue, rowsFalse)
        assertEquals(2, rowsTrue.size)
        assertEquals("123", rowsTrue[0][0])
    }

    @Test
    fun `parseRaw preserves null values`() {
        val json = """[{"barcode":"123","name":null}]"""
        val rows = serializer.parseRaw(json.toByteArray(), treatFirstRowAsHeader = false)
        assertNull(rows[0][1])
    }
}
```

- [ ] **Step 3: Run failing tests**

Run: `./gradlew :app:testDebugUnitTest --tests "com.inventory.sync.serializer.ExcelSerializerPreviewTest" --tests "com.inventory.sync.serializer.JsonSerializerPreviewTest"`
Expected: FAILED (TODO stubs throw)

- [ ] **Step 4: Implement ExcelSerializer.parsePreview and parseRaw**

Add import at top of `ExcelSerializer.kt`:
```kotlin
import com.inventory.sync.catalogimport.ColumnMappingHeuristic
```

Replace the two TODO stubs with:
```kotlin
    override fun parsePreview(data: ByteArray, sampleSize: Int): ImportPreview {
        if (data.isEmpty()) return ImportPreview(emptyList(), emptyList(), true, 0)
        val allRows = readAllRowStrings(data)
        if (allRows.isEmpty()) return ImportPreview(emptyList(), emptyList(), true, 0)
        val headerRow = allRows[0]
        val dataRows = allRows.drop(1)
        val sampleRows = dataRows.take(sampleSize).map { row ->
            row.map { v -> v.takeIf { it.isNotBlank() } }
        }
        val rowsForHeuristic = listOf(headerRow.map { it.takeIf { v -> v.isNotBlank() } }) + sampleRows
        return ImportPreview(
            headerRow = headerRow,
            sampleRows = sampleRows,
            detectedHasHeader = ColumnMappingHeuristic.detectHasHeader(rowsForHeuristic),
            totalRowsEstimate = dataRows.size
        )
    }

    override fun parseRaw(data: ByteArray, treatFirstRowAsHeader: Boolean): List<List<String?>> {
        if (data.isEmpty()) return emptyList()
        val allRows = readAllRowStrings(data)
        val columnCount = allRows.firstOrNull()?.size ?: 0
        val dataRows = if (treatFirstRowAsHeader) allRows.drop(1) else allRows
        return dataRows.map { row ->
            List(maxOf(columnCount, row.size)) { i ->
                row.getOrNull(i)?.takeIf { it.isNotBlank() }
            }
        }
    }

    private fun readAllRowStrings(data: ByteArray): List<List<String>> {
        val result = mutableListOf<List<String>>()
        ReadableWorkbook(ByteArrayInputStream(data)).use { wb ->
            wb.firstSheet.openStream().use { rows ->
                rows.forEach { row ->
                    result.add((0 until row.cellCount).map { i ->
                        row.getCell(i)?.rawValue ?: ""
                    })
                }
            }
        }
        return result
    }
```

- [ ] **Step 5: Implement JsonSerializer.parsePreview, parseRaw, parseJsonObjects**

Replace the two TODO stubs in `JsonSerializer.kt` with:
```kotlin
    override fun parsePreview(data: ByteArray, sampleSize: Int): ImportPreview {
        val items = parseJsonObjects(data)
        if (items.isEmpty()) return ImportPreview(emptyList(), emptyList(), true, 0)
        val keys = items.first().keys.toList()
        val dataItems = items.drop(1)
        val sampleRows = dataItems.take(sampleSize).map { obj ->
            keys.map { k -> obj[k]?.toString() }
        }
        return ImportPreview(
            headerRow = keys,
            sampleRows = sampleRows,
            detectedHasHeader = true,
            totalRowsEstimate = dataItems.size
        )
    }

    override fun parseRaw(data: ByteArray, treatFirstRowAsHeader: Boolean): List<List<String?>> {
        val items = parseJsonObjects(data)
        if (items.isEmpty()) return emptyList()
        val keys = items.first().keys.toList()
        return items.map { obj -> keys.map { k -> obj[k]?.toString() } }
    }

    private fun parseJsonObjects(data: ByteArray): List<Map<String, Any?>> {
        val type = object : TypeToken<List<Map<String, Any?>>>() {}.type
        return gson.fromJson(data.toString(Charsets.UTF_8), type) ?: emptyList()
    }
```

Refactor existing `deserialize` to reuse the helper (removes duplicate parsing):
```kotlin
    override fun deserialize(data: ByteArray): List<Map<String, Any?>> = parseJsonObjects(data)
```

- [ ] **Step 6: Run tests — verify they pass**

Run: `./gradlew :app:testDebugUnitTest --tests "com.inventory.sync.serializer.ExcelSerializerPreviewTest" --tests "com.inventory.sync.serializer.JsonSerializerPreviewTest"`
Expected: BUILD SUCCESSFUL, all tests PASSED

- [ ] **Step 7: Run full unit test suite to catch regressions**

Run: `./gradlew :app:testDebugUnitTest`
Expected: BUILD SUCCESSFUL

- [ ] **Step 8: Commit**

```
git add app/src/main/java/com/inventory/sync/serializer/
git add app/src/test/java/com/inventory/sync/serializer/
git commit -m "feat: implement parsePreview/parseRaw for ExcelSerializer and JsonSerializer"
```

---

### Task 5: DAO getByName extensions

**Files:**
- Modify: `app/src/main/java/com/inventory/data/db/dao/CategoryDao.kt`
- Modify: `app/src/main/java/com/inventory/data/db/dao/LocationDao.kt`

Needed for lookup-or-create in `applyMappedImport`.

- [ ] **Step 1: Add getByName to CategoryDao**

Add this method inside `CategoryDao` interface (before closing `}`):
```kotlin
    @Query("SELECT * FROM categories WHERE name = :name LIMIT 1")
    suspend fun getByName(name: String): Category?
```

- [ ] **Step 2: Add getByName to LocationDao**

Add this method inside `LocationDao` interface (before closing `}`):
```kotlin
    @Query("SELECT * FROM locations WHERE name = :name LIMIT 1")
    suspend fun getByName(name: String): Location?
```

- [ ] **Step 3: Verify compile (Room kapt regenerates DAO impls)**

Run: `./gradlew :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```
git add app/src/main/java/com/inventory/data/db/dao/CategoryDao.kt
git add app/src/main/java/com/inventory/data/db/dao/LocationDao.kt
git commit -m "feat: add getByName queries to CategoryDao and LocationDao"
```

---

### Task 6: InventoryRepository.applyMappedImport

**Files:**
- Modify: `app/src/main/java/com/inventory/data/repository/InventoryRepository.kt`
- Modify: `app/src/main/java/com/inventory/data/repository/InventoryRepositoryImpl.kt`
- Create: `app/src/test/java/com/inventory/data/repository/InventoryRepositoryMappedImportTest.kt`

- [ ] **Step 1: Write failing test**

File: `app/src/test/java/com/inventory/data/repository/InventoryRepositoryMappedImportTest.kt`

```kotlin
package com.inventory.data.repository

import com.inventory.data.db.InventoryDatabase
import com.inventory.data.db.dao.CategoryDao
import com.inventory.data.db.dao.InventoryItemDao
import com.inventory.data.db.dao.InventoryOperationDao
import com.inventory.data.db.dao.LocationDao
import com.inventory.data.db.dao.OutboxEntryDao
import com.inventory.data.entity.Category
import com.inventory.data.entity.InventoryItem
import com.inventory.sync.catalogimport.ColumnMapping
import com.inventory.sync.catalogimport.TargetFields
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

class InventoryRepositoryMappedImportTest {

    private lateinit var db: InventoryDatabase
    private lateinit var categoryDao: CategoryDao
    private lateinit var locationDao: LocationDao
    private lateinit var itemDao: InventoryItemDao
    private lateinit var operationDao: InventoryOperationDao
    private lateinit var outboxDao: OutboxEntryDao
    private lateinit var repo: InventoryRepositoryImpl

    @Before fun setUp() {
        db = mock(); categoryDao = mock(); locationDao = mock()
        itemDao = mock(); operationDao = mock(); outboxDao = mock()
        // Make Room withTransaction pass-through in tests
        whenever(db.withTransaction<Any>(any())).thenAnswer { inv ->
            runBlocking { (inv.getArgument<suspend () -> Any>(0))() }
        }
        repo = InventoryRepositoryImpl(db, categoryDao, locationDao, itemDao, operationDao, outboxDao)
    }

    @Test
    fun `inserts new item with quantity=0 when barcode not found`() = runBlocking {
        val mapping = ColumnMapping(false, mapOf(0 to "barcode", 1 to "name", 2 to "quantity", 3 to "unit"))
        whenever(itemDao.getByBarcode("4820001")).thenReturn(null)
        whenever(itemDao.insert(any())).thenReturn(42L)
        whenever(operationDao.insert(any())).thenReturn(1L)

        val report = repo.applyMappedImport(
            listOf(listOf("4820001", "Widget", "5.0", "шт")),
            mapping, TargetFields.all
        )

        assertEquals(1, report.insertedCount)
        assertEquals(0, report.skippedCount)
        val captor = argumentCaptor<InventoryItem>()
        verify(itemDao).insert(captor.capture())
        assertEquals("4820001", captor.firstValue.barcode)
        assertEquals("Widget", captor.firstValue.name)
        assertEquals(0.0, captor.firstValue.quantity, 0.0)
        assertEquals("шт", captor.firstValue.unit)
    }

    @Test
    fun `generates AUDIT operation when quantity is mapped and parseable`() = runBlocking {
        val existing = InventoryItem(id = 7L, barcode = "4820001", name = "Widget", quantity = 0.0)
        val mapping = ColumnMapping(false, mapOf(0 to "barcode", 1 to "name", 2 to "quantity"))
        whenever(itemDao.getByBarcode("4820001")).thenReturn(existing)
        whenever(operationDao.insert(any())).thenReturn(1L)

        val report = repo.applyMappedImport(
            listOf(listOf("4820001", "Widget", "15.0")),
            mapping, TargetFields.all
        )

        assertEquals(1, report.updatedCount)
        verify(operationDao).insert(any())
        verify(outboxDao).insert(any())
        verify(itemDao).updateQuantity(eq(7L), eq(15.0), any())
    }

    @Test
    fun `skips row when barcode is null`() = runBlocking {
        val mapping = ColumnMapping(false, mapOf(0 to "barcode", 1 to "name"))
        val report = repo.applyMappedImport(listOf(listOf(null, "Widget")), mapping, TargetFields.all)
        assertEquals(1, report.skippedCount)
        verify(itemDao, never()).insert(any())
    }

    @Test
    fun `skips row when name is null`() = runBlocking {
        val mapping = ColumnMapping(false, mapOf(0 to "barcode", 1 to "name"))
        val report = repo.applyMappedImport(listOf(listOf("4820001", null)), mapping, TargetFields.all)
        assertEquals(1, report.skippedCount)
        verify(itemDao, never()).insert(any())
    }

    @Test
    fun `does not generate AUDIT when quantity is not parseable`() = runBlocking {
        val existing = InventoryItem(id = 7L, barcode = "4820001", name = "Widget", quantity = 3.0)
        val mapping = ColumnMapping(false, mapOf(0 to "barcode", 1 to "name", 2 to "quantity"))
        whenever(itemDao.getByBarcode("4820001")).thenReturn(existing)

        repo.applyMappedImport(listOf(listOf("4820001", "Widget", "не_число")), mapping, TargetFields.all)

        verify(operationDao, never()).insert(any())
    }

    @Test
    fun `lookup-or-create category by name`() = runBlocking {
        val mapping = ColumnMapping(false, mapOf(0 to "barcode", 1 to "name", 2 to "category"))
        whenever(itemDao.getByBarcode("4820001")).thenReturn(null)
        whenever(categoryDao.getByName("Продукти")).thenReturn(null)
        whenever(categoryDao.insert(any())).thenReturn(5L)
        whenever(itemDao.insert(any())).thenReturn(1L)

        repo.applyMappedImport(listOf(listOf("4820001", "Widget", "Продукти")), mapping, TargetFields.all)

        verify(categoryDao).insert(Category(name = "Продукти"))
        val captor = argumentCaptor<InventoryItem>()
        verify(itemDao).insert(captor.capture())
        assertEquals(5L, captor.firstValue.categoryId)
    }

    @Test
    fun `reuses cached category id for duplicate names`() = runBlocking {
        val mapping = ColumnMapping(false, mapOf(0 to "barcode", 1 to "name", 2 to "category"))
        whenever(itemDao.getByBarcode(any())).thenReturn(null)
        whenever(categoryDao.getByName("Продукти")).thenReturn(null)
        whenever(categoryDao.insert(any())).thenReturn(5L)
        whenever(itemDao.insert(any())).thenReturn(1L)

        repo.applyMappedImport(
            listOf(
                listOf("AAA", "Widget A", "Продукти"),
                listOf("BBB", "Widget B", "Продукти")
            ),
            mapping, TargetFields.all
        )

        verify(categoryDao, times(1)).insert(any())
    }
}
```

- [ ] **Step 2: Run test — verify it fails**

Run: `./gradlew :app:testDebugUnitTest --tests "com.inventory.data.repository.InventoryRepositoryMappedImportTest"`
Expected: FAILED (`applyMappedImport` not defined)

- [ ] **Step 3: Add applyMappedImport to InventoryRepository interface**

Add imports at top of `InventoryRepository.kt`:
```kotlin
import com.inventory.sync.catalogimport.ColumnMapping
import com.inventory.sync.catalogimport.ImportReport
import com.inventory.sync.catalogimport.TargetField
```

Add this method (after `importItems`):
```kotlin
    suspend fun applyMappedImport(
        rawRows: List<List<String?>>,
        mapping: ColumnMapping,
        targetFields: List<TargetField>
    ): ImportReport
```

- [ ] **Step 4: Implement applyMappedImport in InventoryRepositoryImpl**

Add imports at top of `InventoryRepositoryImpl.kt`:
```kotlin
import com.google.gson.Gson
import com.inventory.sync.catalogimport.ColumnMapping
import com.inventory.sync.catalogimport.ImportReport
import com.inventory.sync.catalogimport.TargetField
```

Add this method to `InventoryRepositoryImpl` (after `importItems`):
```kotlin
    override suspend fun applyMappedImport(
        rawRows: List<List<String?>>,
        mapping: ColumnMapping,
        targetFields: List<TargetField>
    ): ImportReport = db.withTransaction {
        var inserted = 0; var updated = 0; var skipped = 0
        val skipReasons = mutableListOf<String>()
        val gson = Gson()
        val categoryCache = mutableMapOf<String, Long?>()
        val locationCache = mutableMapOf<String, Long?>()

        fun cellFor(row: List<String?>, fieldId: String): String? =
            mapping.mapping.entries.firstOrNull { it.value == fieldId }?.key
                ?.let { row.getOrNull(it) }?.takeIf { it.isNotBlank() }

        for ((idx, row) in rawRows.withIndex()) {
            val barcode = cellFor(row, "barcode")
            if (barcode == null) { skipped++; skipReasons += "row $idx: empty barcode"; continue }
            val name = cellFor(row, "name")
            if (name == null) { skipped++; skipReasons += "row $idx: empty name"; continue }

            val categoryId: Long? = cellFor(row, "category")?.let { catName ->
                categoryCache.getOrPut(catName) {
                    categoryDao.getByName(catName)?.id ?: categoryDao.insert(Category(name = catName))
                }
            }
            val locationId: Long? = cellFor(row, "location")?.let { locName ->
                locationCache.getOrPut(locName) {
                    locationDao.getByName(locName)?.id ?: locationDao.insert(Location(name = locName))
                }
            }

            val existing = inventoryItemDao.getByBarcode(barcode)
            val itemId: Long
            if (existing != null) {
                inventoryItemDao.update(existing.copy(
                    name        = name,
                    description = cellFor(row, "description") ?: existing.description,
                    unit        = cellFor(row, "unit") ?: existing.unit,
                    notes       = cellFor(row, "notes") ?: existing.notes,
                    minQuantity = cellFor(row, "min_quantity")?.toDoubleOrNull() ?: existing.minQuantity,
                    categoryId  = categoryId ?: existing.categoryId,
                    locationId  = locationId ?: existing.locationId,
                    updatedAt   = System.currentTimeMillis()
                ))
                itemId = existing.id
                updated++
            } else {
                itemId = inventoryItemDao.insert(InventoryItem(
                    barcode     = barcode,
                    name        = name,
                    description = cellFor(row, "description") ?: "",
                    unit        = cellFor(row, "unit") ?: "шт",
                    notes       = cellFor(row, "notes") ?: "",
                    minQuantity = cellFor(row, "min_quantity")?.toDoubleOrNull() ?: 0.0,
                    categoryId  = categoryId,
                    locationId  = locationId,
                    quantity    = 0.0
                ))
                inserted++
            }

            val qty = cellFor(row, "quantity")?.toDoubleOrNull()
            if (qty != null) {
                inventoryItemDao.updateQuantity(itemId, qty)
                val op = InventoryOperation(
                    itemId = itemId, barcode = barcode,
                    operationType = OperationType.AUDIT.name, quantity = qty
                )
                inventoryOperationDao.insert(op)
                outboxEntryDao.insert(OutboxEntry(
                    operationType = OperationType.AUDIT.name,
                    payload = gson.toJson(mapOf(
                        "barcode" to barcode,
                        "quantity" to qty,
                        "operationType" to "AUDIT"
                    ))
                ))
            }
        }
        ImportReport(inserted, updated, skipped, skipReasons)
    }
```

- [ ] **Step 5: Run test — verify it passes**

Run: `./gradlew :app:testDebugUnitTest --tests "com.inventory.data.repository.InventoryRepositoryMappedImportTest"`
Expected: BUILD SUCCESSFUL, 7 tests PASSED

- [ ] **Step 6: Commit**

```
git add app/src/main/java/com/inventory/data/repository/
git add app/src/test/java/com/inventory/data/repository/InventoryRepositoryMappedImportTest.kt
git commit -m "feat: implement applyMappedImport with AUDIT outbox and lookup-or-create FK"
```

---

### Task 7: SyncSettings + SyncEngine conditional path

**Files:**
- Modify: `app/src/main/java/com/inventory/sync/SyncSettings.kt`
- Modify: `app/src/main/java/com/inventory/sync/SyncEngine.kt`
- Modify: `app/src/test/java/com/inventory/sync/SyncEngineTest.kt`

- [ ] **Step 1: Add columnMapping field to SyncSettings**

Add import at top of `SyncSettings.kt`:
```kotlin
import com.inventory.sync.catalogimport.ColumnMapping
```

Add this field to the `SyncSettings` data class (after `importFileName`):
```kotlin
    val columnMapping: ColumnMapping? = null,
```

- [ ] **Step 2: Write the new SyncEngine test**

Add this test to `SyncEngineTest.kt`:

```kotlin
    @Test
    fun `runImport uses applyMappedImport when columnMapping is configured`() = runTest {
        val csvData = "артикул,найменування\n4820001,Widget\n".toByteArray()
        val mapping = com.inventory.sync.catalogimport.ColumnMapping(
            treatFirstRowAsHeader = true,
            mapping = mapOf(0 to "barcode", 1 to "name")
        )
        val settings = SyncSettings(
            providerType = SyncProviderType.LOCAL_FOLDER,
            isImportEnabled = true,
            format = SyncFormat.CSV,
            columnMapping = mapping
        )
        val provider = mock<SyncProvider>()
        whenever(provider.supportsImport).thenReturn(true)
        whenever(provider.discoverImportFiles(any())).thenReturn(emptyList())
        whenever(provider.import(SyncFormat.CSV, "inventory_import"))
            .thenReturn(SyncImportResult.Success(csvData))
        whenever(settingsManager.getImportProviders()).thenReturn(listOf(settings))
        whenever(providerFactory.create(SyncProviderType.LOCAL_FOLDER)).thenReturn(provider)
        whenever(repository.applyMappedImport(any(), any(), any()))
            .thenReturn(com.inventory.sync.catalogimport.ImportReport(1, 0, 0))

        engine.runImport()

        verify(repository).applyMappedImport(any(), any(), any())
        verify(repository, never()).importItems(any())
        assertTrue(engine.state.value is SyncState.Success)
    }
```

Add import at top of `SyncEngineTest.kt`:
```kotlin
import org.mockito.kotlin.never
```

- [ ] **Step 3: Run the new test — verify it fails**

Run: `./gradlew :app:testDebugUnitTest --tests "com.inventory.sync.SyncEngineTest"`
Expected: 1 new test FAILED

- [ ] **Step 4: Refactor SyncEngine to use columnMapping**

Add import at top of `SyncEngine.kt`:
```kotlin
import com.inventory.sync.catalogimport.TargetFields
```

Inside `runImport()`, replace the `SyncImportResult.Success` branch body:
```kotlin
is SyncImportResult.Success -> {
    val rows = applyImport(importResult.data, settings)
    _state.value = SyncState.Success(
        timestamp = System.currentTimeMillis(),
        importSummary = rows.toImportSummary(settings, importName)
    )
    return
}
```

Replace the existing private `applyImport(rows: ...)` with:
```kotlin
private suspend fun applyImport(data: ByteArray, settings: SyncSettings): List<Map<String, Any?>> {
    val columnMapping = settings.columnMapping
    val serializer = getSerializer(settings.format)
    return if (columnMapping != null) {
        val rawRows = serializer.parseRaw(data, columnMapping.treatFirstRowAsHeader)
        repository.applyMappedImport(rawRows, columnMapping, TargetFields.all)
        emptyList()
    } else {
        val rows = serializer.deserialize(data)
        repository.importItems(rows)
        rows
    }
}
```

- [ ] **Step 5: Run all SyncEngine tests**

Run: `./gradlew :app:testDebugUnitTest --tests "com.inventory.sync.SyncEngineTest"`
Expected: BUILD SUCCESSFUL, all tests PASSED

- [ ] **Step 6: Commit**

```
git add app/src/main/java/com/inventory/sync/SyncSettings.kt
git add app/src/main/java/com/inventory/sync/SyncEngine.kt
git add app/src/test/java/com/inventory/sync/SyncEngineTest.kt
git commit -m "feat: add columnMapping to SyncSettings; SyncEngine takes applyMappedImport path when mapping is set"
```

---

### Task 8: ImportCatalogUiState + ViewModel

**Files:**
- Create: `app/src/main/java/com/inventory/ui/importcatalog/ImportCatalogUiState.kt`
- Create: `app/src/main/java/com/inventory/ui/importcatalog/ImportCatalogViewModel.kt`
- Create: `app/src/test/java/com/inventory/ui/importcatalog/ImportCatalogViewModelTest.kt`

- [ ] **Step 1: Create ImportCatalogUiState.kt**

```kotlin
package com.inventory.ui.importcatalog

import com.inventory.sync.SyncSettings
import com.inventory.sync.catalogimport.ColumnMapping
import com.inventory.sync.catalogimport.ImportPreview
import com.inventory.sync.catalogimport.ImportReport
import com.inventory.sync.catalogimport.TargetField

sealed class ImportCatalogUiState {
    object Loading : ImportCatalogUiState()

    data class PickProvider(val providers: List<SyncSettings>) : ImportCatalogUiState()

    data class PickFile(
        val provider: SyncSettings,
        val files: List<String>,
        val isLoading: Boolean = false,
        val error: String? = null
    ) : ImportCatalogUiState()

    data class MapColumns(
        val provider: SyncSettings,
        val fileName: String,
        val fileList: List<String>,
        val preview: ImportPreview,
        val treatFirstRowAsHeader: Boolean,
        val mapping: Map<Int, String?>,
        val targets: List<TargetField>,
        val isLoading: Boolean = false,
        val error: String? = null
    ) : ImportCatalogUiState() {
        val isValid: Boolean
            get() = targets.filter { it.isRequired }
                .all { req -> mapping.values.contains(req.id) }
    }

    data class Confirm(
        val provider: SyncSettings,
        val fileName: String,
        val fileList: List<String>,
        val mapping: ColumnMapping,
        val targets: List<TargetField>,
        val totalRows: Int,
        val saveMapping: Boolean = false
    ) : ImportCatalogUiState()

    data class Importing(
        val provider: SyncSettings,
        val fileName: String
    ) : ImportCatalogUiState()

    data class Result(
        val report: ImportReport,
        val provider: SyncSettings,
        val fileName: String
    ) : ImportCatalogUiState()

    data class Failure(val message: String) : ImportCatalogUiState()
}
```

- [ ] **Step 2: Write failing ViewModel test**

File: `app/src/test/java/com/inventory/ui/importcatalog/ImportCatalogViewModelTest.kt`

```kotlin
package com.inventory.ui.importcatalog

import com.inventory.data.repository.InventoryRepository
import com.inventory.sync.SyncFormat
import com.inventory.sync.SyncImportResult
import com.inventory.sync.SyncProvider
import com.inventory.sync.SyncProviderFactory
import com.inventory.sync.SyncProviderType
import com.inventory.sync.SyncSettings
import com.inventory.sync.SyncSettingsManager
import com.inventory.sync.catalogimport.ImportPreview
import com.inventory.sync.catalogimport.ImportReport
import com.inventory.sync.serializer.CsvSerializer
import com.inventory.sync.serializer.ExcelSerializer
import com.inventory.sync.serializer.JsonSerializer
import com.inventory.test.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.*

@OptIn(ExperimentalCoroutinesApi::class)
class ImportCatalogViewModelTest {

    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    private lateinit var settingsManager: SyncSettingsManager
    private lateinit var providerFactory: SyncProviderFactory
    private lateinit var repository: InventoryRepository
    private lateinit var csvSerializer: CsvSerializer
    private lateinit var excelSerializer: ExcelSerializer
    private lateinit var jsonSerializer: JsonSerializer
    private lateinit var vm: ImportCatalogViewModel

    private val settings = SyncSettings(
        providerType = SyncProviderType.LOCAL_FOLDER,
        isImportEnabled = true, format = SyncFormat.CSV
    )

    @Before fun setUp() {
        settingsManager = mock(); providerFactory = mock(); repository = mock()
        csvSerializer = mock(); excelSerializer = mock(); jsonSerializer = mock()
        whenever(settingsManager.getImportProviders()).thenReturn(listOf(settings))
        vm = ImportCatalogViewModel(
            settingsManager, providerFactory, repository,
            csvSerializer, excelSerializer, jsonSerializer
        )
    }

    @Test
    fun `initial state is PickProvider with active providers`() = runTest(mainDispatcherRule.scheduler) {
        runCurrent()
        val state = vm.uiState.value as ImportCatalogUiState.PickProvider
        assertEquals(1, state.providers.size)
    }

    @Test
    fun `onProviderSelected loads files and transitions to PickFile`() = runTest(mainDispatcherRule.scheduler) {
        val provider = mock<SyncProvider>()
        whenever(providerFactory.create(SyncProviderType.LOCAL_FOLDER)).thenReturn(provider)
        whenever(provider.discoverImportFiles(SyncFormat.CSV)).thenReturn(listOf("a.csv", "b.csv"))
        runCurrent()
        vm.onProviderSelected(settings); runCurrent()
        val state = vm.uiState.value as ImportCatalogUiState.PickFile
        assertEquals(listOf("a.csv", "b.csv"), state.files)
    }

    @Test
    fun `onFileSelected fetches preview and auto-maps columns`() = runTest(mainDispatcherRule.scheduler) {
        val data = "barcode,name\n123,Widget\n".toByteArray()
        val preview = ImportPreview(listOf("barcode", "name"), listOf(listOf("123", "Widget")), true, 1)
        val provider = mock<SyncProvider>()
        whenever(providerFactory.create(any())).thenReturn(provider)
        whenever(provider.discoverImportFiles(any())).thenReturn(listOf("catalog.csv"))
        whenever(provider.import(any(), eq("catalog.csv"))).thenReturn(SyncImportResult.Success(data))
        whenever(csvSerializer.parsePreview(any())).thenReturn(preview)

        runCurrent()
        vm.onProviderSelected(settings); runCurrent()
        vm.onFileSelected("catalog.csv"); runCurrent()

        val state = vm.uiState.value as ImportCatalogUiState.MapColumns
        assertEquals(preview, state.preview)
        assertTrue(state.treatFirstRowAsHeader)
        assertEquals("barcode", state.mapping[0])
        assertEquals("name", state.mapping[1])
    }

    @Test
    fun `onColumnMapped updates mapping`() = runTest(mainDispatcherRule.scheduler) {
        buildMapColumnsState()
        vm.onColumnMapped(0, "quantity")
        assertEquals("quantity", (vm.uiState.value as ImportCatalogUiState.MapColumns).mapping[0])
    }

    @Test
    fun `isValid is false when required field is unmapped`() = runTest(mainDispatcherRule.scheduler) {
        buildMapColumnsState()
        vm.onColumnMapped(0, null)
        assertFalse((vm.uiState.value as ImportCatalogUiState.MapColumns).isValid)
    }

    @Test
    fun `onConfirmMapping transitions to Confirm`() = runTest(mainDispatcherRule.scheduler) {
        buildMapColumnsState()
        vm.onConfirmMapping()
        val state = vm.uiState.value as ImportCatalogUiState.Confirm
        assertEquals(settings, state.provider)
    }

    @Test
    fun `onImport calls applyMappedImport and transitions to Result`() = runTest(mainDispatcherRule.scheduler) {
        buildConfirmState()
        val data = "barcode,name\n123,Widget\n".toByteArray()
        val provider = mock<SyncProvider>()
        whenever(providerFactory.create(any())).thenReturn(provider)
        whenever(provider.import(any(), any())).thenReturn(SyncImportResult.Success(data))
        whenever(csvSerializer.parseRaw(any(), any())).thenReturn(listOf(listOf("123", "Widget")))
        whenever(repository.applyMappedImport(any(), any(), any())).thenReturn(ImportReport(5, 2, 1))

        vm.onImport(); runCurrent()

        val state = vm.uiState.value as ImportCatalogUiState.Result
        assertEquals(5, state.report.insertedCount)
        assertEquals(2, state.report.updatedCount)
    }

    @Test
    fun `onBack from MapColumns restores PickFile with cached file list`() = runTest(mainDispatcherRule.scheduler) {
        buildMapColumnsState()
        vm.onBack()
        val state = vm.uiState.value as ImportCatalogUiState.PickFile
        assertEquals(listOf("catalog.csv"), state.files)
    }

    private fun buildMapColumnsState() {
        val data = "barcode,name\n123,Widget\n".toByteArray()
        val preview = ImportPreview(listOf("barcode", "name"), listOf(listOf("123", "Widget")), true, 1)
        val provider = mock<SyncProvider>()
        whenever(providerFactory.create(any())).thenReturn(provider)
        whenever(provider.discoverImportFiles(any())).thenReturn(listOf("catalog.csv"))
        whenever(provider.import(any(), any())).thenReturn(SyncImportResult.Success(data))
        whenever(csvSerializer.parsePreview(any())).thenReturn(preview)
        kotlinx.coroutines.runBlocking {
            vm.onProviderSelected(settings)
            vm.onFileSelected("catalog.csv")
        }
    }

    private fun buildConfirmState() {
        buildMapColumnsState()
        vm.onConfirmMapping()
    }
}
```

- [ ] **Step 3: Run test — verify it fails**

Run: `./gradlew :app:testDebugUnitTest --tests "com.inventory.ui.importcatalog.ImportCatalogViewModelTest"`
Expected: FAILED (compilation error — ViewModel doesn't exist)

- [ ] **Step 4: Create ImportCatalogViewModel.kt**

```kotlin
package com.inventory.ui.importcatalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inventory.data.repository.InventoryRepository
import com.inventory.sync.SyncFormat
import com.inventory.sync.SyncImportResult
import com.inventory.sync.SyncProviderFactory
import com.inventory.sync.SyncSettings
import com.inventory.sync.SyncSettingsManager
import com.inventory.sync.catalogimport.ColumnMapping
import com.inventory.sync.catalogimport.ColumnMappingHeuristic
import com.inventory.sync.catalogimport.ImportPreview
import com.inventory.sync.catalogimport.TargetFields
import com.inventory.sync.serializer.CsvSerializer
import com.inventory.sync.serializer.ExcelSerializer
import com.inventory.sync.serializer.JsonSerializer
import com.inventory.sync.serializer.SyncSerializer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ImportCatalogViewModel @Inject constructor(
    private val settingsManager: SyncSettingsManager,
    private val providerFactory: SyncProviderFactory,
    private val repository: InventoryRepository,
    private val csvSerializer: CsvSerializer,
    private val excelSerializer: ExcelSerializer,
    private val jsonSerializer: JsonSerializer,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ImportCatalogUiState>(ImportCatalogUiState.Loading)
    val uiState: StateFlow<ImportCatalogUiState> = _uiState

    init {
        viewModelScope.launch {
            _uiState.value = ImportCatalogUiState.PickProvider(settingsManager.getImportProviders())
        }
    }

    fun onProviderSelected(settings: SyncSettings) {
        viewModelScope.launch {
            _uiState.value = ImportCatalogUiState.PickFile(settings, emptyList(), isLoading = true)
            try {
                val files = providerFactory.create(settings.providerType)
                    .discoverImportFiles(settings.format)
                _uiState.value = ImportCatalogUiState.PickFile(settings, files)
            } catch (e: Exception) {
                _uiState.value = ImportCatalogUiState.PickFile(settings, emptyList(), error = e.message)
            }
        }
    }

    fun onFileSelected(fileName: String) {
        val cur = uiState.value as? ImportCatalogUiState.PickFile ?: return
        val settings = cur.provider
        val fileList = cur.files
        viewModelScope.launch {
            _uiState.value = cur.copy(isLoading = true, error = null)
            try {
                val result = providerFactory.create(settings.providerType)
                    .import(settings.format, fileName)
                if (result is SyncImportResult.Failure) {
                    _uiState.value = cur.copy(isLoading = false, error = result.message)
                    return@launch
                }
                val data = (result as SyncImportResult.Success).data
                val preview = getSerializer(settings.format).parsePreview(data)
                val autoMapping = ColumnMappingHeuristic.fuzzySuggestMapping(preview.headerRow, TargetFields.all)
                _uiState.value = ImportCatalogUiState.MapColumns(
                    provider = settings, fileName = fileName, fileList = fileList,
                    preview = preview, treatFirstRowAsHeader = preview.detectedHasHeader,
                    mapping = autoMapping, targets = TargetFields.all
                )
            } catch (e: Exception) {
                _uiState.value = cur.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun onToggleHeader(hasHeader: Boolean) {
        val s = uiState.value as? ImportCatalogUiState.MapColumns ?: return
        _uiState.value = s.copy(treatFirstRowAsHeader = hasHeader)
    }

    fun onColumnMapped(columnIndex: Int, targetFieldId: String?) {
        val s = uiState.value as? ImportCatalogUiState.MapColumns ?: return
        _uiState.value = s.copy(mapping = s.mapping + (columnIndex to targetFieldId))
    }

    fun onConfirmMapping() {
        val s = uiState.value as? ImportCatalogUiState.MapColumns ?: return
        _uiState.value = ImportCatalogUiState.Confirm(
            provider = s.provider, fileName = s.fileName, fileList = s.fileList,
            mapping = ColumnMapping(s.treatFirstRowAsHeader, s.mapping),
            targets = s.targets, totalRows = s.preview.totalRowsEstimate
        )
    }

    fun onToggleSaveMapping(save: Boolean) {
        val s = uiState.value as? ImportCatalogUiState.Confirm ?: return
        _uiState.value = s.copy(saveMapping = save)
    }

    fun onImport() {
        val s = uiState.value as? ImportCatalogUiState.Confirm ?: return
        viewModelScope.launch {
            _uiState.value = ImportCatalogUiState.Importing(s.provider, s.fileName)
            try {
                val result = providerFactory.create(s.provider.providerType)
                    .import(s.provider.format, s.fileName)
                if (result is SyncImportResult.Failure) {
                    _uiState.value = ImportCatalogUiState.Failure(result.message)
                    return@launch
                }
                val data = (result as SyncImportResult.Success).data
                val rawRows = getSerializer(s.provider.format)
                    .parseRaw(data, s.mapping.treatFirstRowAsHeader)
                val report = repository.applyMappedImport(rawRows, s.mapping, s.targets)
                if (s.saveMapping) {
                    settingsManager.saveSettings(s.provider.copy(columnMapping = s.mapping))
                }
                _uiState.value = ImportCatalogUiState.Result(report, s.provider, s.fileName)
            } catch (e: Exception) {
                _uiState.value = ImportCatalogUiState.Failure(e.message ?: "Unknown error")
            }
        }
    }

    fun onBack() {
        when (val s = uiState.value) {
            is ImportCatalogUiState.PickFile ->
                _uiState.value = ImportCatalogUiState.PickProvider(settingsManager.getImportProviders())
            is ImportCatalogUiState.MapColumns ->
                _uiState.value = ImportCatalogUiState.PickFile(s.provider, s.fileList)
            is ImportCatalogUiState.Confirm ->
                _uiState.value = ImportCatalogUiState.MapColumns(
                    provider = s.provider, fileName = s.fileName, fileList = s.fileList,
                    preview = ImportPreview(emptyList(), emptyList(),
                        s.mapping.treatFirstRowAsHeader, s.totalRows),
                    treatFirstRowAsHeader = s.mapping.treatFirstRowAsHeader,
                    mapping = s.mapping.mapping, targets = s.targets
                )
            else -> Unit
        }
    }

    fun onDismiss() {
        _uiState.value = ImportCatalogUiState.PickProvider(settingsManager.getImportProviders())
    }

    private fun getSerializer(format: SyncFormat): SyncSerializer = when (format) {
        SyncFormat.CSV -> csvSerializer
        SyncFormat.JSON -> jsonSerializer
        SyncFormat.EXCEL -> excelSerializer
    }
}
```

- [ ] **Step 5: Run test — verify it passes**

Run: `./gradlew :app:testDebugUnitTest --tests "com.inventory.ui.importcatalog.ImportCatalogViewModelTest"`
Expected: BUILD SUCCESSFUL, 8 tests PASSED

- [ ] **Step 6: Commit**

```
git add app/src/main/java/com/inventory/ui/importcatalog/ImportCatalogUiState.kt
git add app/src/main/java/com/inventory/ui/importcatalog/ImportCatalogViewModel.kt
git add app/src/test/java/com/inventory/ui/importcatalog/ImportCatalogViewModelTest.kt
git commit -m "feat: add ImportCatalogViewModel and sealed UiState"
```

---

### Task 9: ImportCatalogScreen (Compose UI)

**Files:**
- Create: `app/src/main/java/com/inventory/ui/importcatalog/ImportCatalogScreen.kt`

Compose UI driven by the sealed `ImportCatalogUiState`. No unit tests (visual surface) — covered indirectly by the ViewModel test plus the smoke build in Task 10.

- [ ] **Step 1: Create ImportCatalogScreen.kt**

```kotlin
@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.inventory.ui.importcatalog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.inventory.sync.SyncSettings
import com.inventory.sync.catalogimport.TargetField
import com.inventory.sync.catalogimport.TargetFields

@Composable
fun ImportCatalogScreen(
    onBack: () -> Unit,
    viewModel: ImportCatalogViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    when (val s = state) {
        is ImportCatalogUiState.Loading      -> CenteredProgress()
        is ImportCatalogUiState.PickProvider -> PickProviderStep(s, viewModel::onProviderSelected, onBack)
        is ImportCatalogUiState.PickFile     -> PickFileStep(s, viewModel::onFileSelected, viewModel::onBack)
        is ImportCatalogUiState.MapColumns   -> MapColumnsStep(s,
            viewModel::onToggleHeader, viewModel::onColumnMapped,
            viewModel::onConfirmMapping, viewModel::onBack)
        is ImportCatalogUiState.Confirm      -> ConfirmStep(s,
            viewModel::onToggleSaveMapping, viewModel::onImport, viewModel::onBack)
        is ImportCatalogUiState.Importing    -> ImportingStep(s.fileName)
        is ImportCatalogUiState.Result       -> ResultStep(s, onBack, viewModel::onDismiss)
        is ImportCatalogUiState.Failure      -> FailureStep(s.message, viewModel::onDismiss)
    }
}

@Composable
private fun CenteredProgress() {
    Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
}

@Composable
private fun PickProviderStep(
    state: ImportCatalogUiState.PickProvider,
    onProviderClick: (SyncSettings) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Оберіть провайдер") },
            navigationIcon = { TextButton(onClick = onBack) { Text("←") } }
        )
    }) { padding ->
        if (state.providers.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                Text("Немає налаштованих імпорт-провайдерів")
            }
        } else {
            LazyColumn(contentPadding = padding) {
                items(state.providers) { provider ->
                    ListItem(
                        headlineContent = { Text(provider.providerType.displayName) },
                        modifier = Modifier.fillMaxWidth().clickable { onProviderClick(provider) }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun PickFileStep(
    state: ImportCatalogUiState.PickFile,
    onFileClick: (String) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Оберіть файл") },
            navigationIcon = { TextButton(onClick = onBack) { Text("←") } }
        )
    }) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading      -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                state.error != null  -> Text("Помилка: ${state.error}",
                    Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.error)
                state.files.isEmpty() -> Text("Файлів не знайдено", Modifier.align(Alignment.Center))
                else -> LazyColumn {
                    items(state.files) { file ->
                        ListItem(
                            headlineContent = { Text(file) },
                            modifier = Modifier.fillMaxWidth().clickable { onFileClick(file) }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun MapColumnsStep(
    state: ImportCatalogUiState.MapColumns,
    onToggleHeader: (Boolean) -> Unit,
    onColumnMapped: (Int, String?) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Налаштування колонок") },
                navigationIcon = { TextButton(onClick = onBack) { Text("←") } }
            )
        },
        bottomBar = {
            Button(
                onClick = onNext, enabled = state.isValid,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) { Text("Далі") }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = state.treatFirstRowAsHeader,
                        onCheckedChange = onToggleHeader
                    )
                    Text("Перший рядок містить назви колонок")
                }
            }
            itemsIndexed(state.preview.headerRow) { colIdx, header ->
                ColumnMappingCard(
                    colIdx = colIdx,
                    headerLabel = if (state.treatFirstRowAsHeader) header ?: "Колонка $colIdx"
                                  else "Колонка $colIdx",
                    sampleValues = state.preview.sampleRows.mapNotNull { it.getOrNull(colIdx) }.take(3),
                    selectedFieldId = state.mapping[colIdx],
                    targets = state.targets,
                    isValid = state.isValid,
                    onMap = { onColumnMapped(colIdx, it) }
                )
            }
        }
    }
}

@Composable
private fun ColumnMappingCard(
    colIdx: Int,
    headerLabel: String,
    sampleValues: List<String?>,
    selectedFieldId: String?,
    targets: List<TargetField>,
    isValid: Boolean,
    onMap: (String?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = selectedFieldId
        ?.let { id -> targets.firstOrNull { it.id == id }?.displayName }
        ?: "Не імпортувати"
    val showError = !isValid && targets.any { it.isRequired && it.id == selectedFieldId }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Text(headerLabel, style = MaterialTheme.typography.labelLarge)
            if (sampleValues.isNotEmpty()) {
                Text(
                    sampleValues.filterNotNull().joinToString(", "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(4.dp))
            Box {
                OutlinedButton(
                    onClick = { expanded = true },
                    colors = if (showError)
                        ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    else ButtonDefaults.outlinedButtonColors()
                ) { Text(selectedLabel) }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    DropdownMenuItem(
                        text = { Text("Не імпортувати") },
                        onClick = { onMap(null); expanded = false }
                    )
                    targets.forEach { target ->
                        DropdownMenuItem(
                            text = {
                                Row {
                                    Text(target.displayName)
                                    if (target.isRequired) {
                                        Spacer(Modifier.width(4.dp))
                                        Text("*", color = MaterialTheme.colorScheme.error)
                                    }
                                }
                            },
                            onClick = { onMap(target.id); expanded = false }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ConfirmStep(
    state: ImportCatalogUiState.Confirm,
    onToggleSave: (Boolean) -> Unit,
    onImport: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Підтвердіть імпорт") },
                navigationIcon = { TextButton(onClick = onBack) { Text("←") } }
            )
        },
        bottomBar = {
            Button(
                onClick = onImport,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) { Text("Імпортувати") }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text("Файл: ${state.fileName}")
            Text("Провайдер: ${state.provider.providerType.displayName}")
            Text("Рядків: ~${state.totalRows}")
            Spacer(Modifier.height(16.dp))
            Text("Мапінг:", style = MaterialTheme.typography.titleMedium)
            state.mapping.mapping.entries.sortedBy { it.key }.forEach { (colIdx, fieldId) ->
                val target = fieldId?.let { TargetFields.byId(it)?.displayName } ?: "Не імпортувати"
                Text("Колонка $colIdx → $target")
            }
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = state.saveMapping, onCheckedChange = onToggleSave)
                Text("Зберегти налаштування мапінгу для цього провайдера")
            }
        }
    }
}

@Composable
private fun ImportingStep(fileName: String) {
    Box(Modifier.fillMaxSize(), Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(Modifier.height(16.dp))
            Text("Імпортуємо $fileName…")
        }
    }
}

@Composable
private fun ResultStep(
    state: ImportCatalogUiState.Result,
    onDone: () -> Unit,
    onImportAnother: () -> Unit
) {
    Scaffold(topBar = { TopAppBar(title = { Text("Результат імпорту") }) }) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(32.dp))
            Text("Додано: ${state.report.insertedCount}", style = MaterialTheme.typography.titleLarge)
            Text("Оновлено: ${state.report.updatedCount}", style = MaterialTheme.typography.titleLarge)
            Text("Пропущено: ${state.report.skippedCount}", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(32.dp))
            Button(onClick = onDone, modifier = Modifier.fillMaxWidth()) { Text("Готово") }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = onImportAnother, modifier = Modifier.fillMaxWidth()) {
                Text("Імпортувати ще файл")
            }
        }
    }
}

@Composable
private fun FailureStep(message: String, onDismiss: () -> Unit) {
    Box(Modifier.fillMaxSize(), Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Помилка: $message", color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(16.dp))
            Button(onClick = onDismiss) { Text("Повернутись") }
        }
    }
}
```

- [ ] **Step 2: Verify compile**

Run: `./gradlew :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```
git add app/src/main/java/com/inventory/ui/importcatalog/ImportCatalogScreen.kt
git commit -m "feat: add ImportCatalogScreen with 5-step wizard UI"
```

---

### Task 10: Navigation wiring + ScanScreen entry point

**Files:**
- Modify: `app/src/main/java/com/inventory/app/MainActivity.kt`
- Modify: `app/src/main/java/com/inventory/ui/scan/ScanScreen.kt`

- [ ] **Step 1: Add import_catalog route to MainActivity**

Add this import to `MainActivity.kt`:
```kotlin
import com.inventory.ui.importcatalog.ImportCatalogScreen
```

Update the `composable("scan") { ... }` block to pass a new handler:
```kotlin
composable("scan") {
    ScanScreen(
        themeMode = themeMode,
        onThemeToggle = { themePreferenceManager.cycleTheme() },
        onSyncSettingsClick = { navController.navigate("sync_settings") },
        onReceivingClick = { navController.navigate("receiving") },
        onAuditClick = { navController.navigate("audit") },
        onImportCatalogClick = { navController.navigate("import_catalog") }
    )
}
```

Add a new composable block inside the `NavHost` (e.g. after the `audit` route):
```kotlin
composable("import_catalog") {
    ImportCatalogScreen(onBack = { navController.popBackStack() })
}
```

- [ ] **Step 2: Add onImportCatalogClick parameter and button to ScanScreen**

In `ScanScreen.kt`, add the new lambda parameter to the `ScanScreen` function signature (alongside the other `onXxxClick` params):
```kotlin
onImportCatalogClick: () -> Unit = {},
```

Locate the existing action buttons (`onAuditClick` / `onReceivingClick` / `onSyncSettingsClick` are wired to `IndustrialButton` / `IndustrialOutlinedButton`). Add a new button next to them:
```kotlin
IndustrialOutlinedButton(
    text = "Імпорт каталогу",
    onClick = onImportCatalogClick,
    modifier = Modifier.fillMaxWidth()
)
```

(If the existing pattern uses a different button style or wraps buttons in a `Row`, follow the existing pattern — the goal is one new tappable element labelled "Імпорт каталогу" that calls `onImportCatalogClick`.)

- [ ] **Step 3: Run full unit test suite**

Run: `./gradlew :app:testDebugUnitTest`
Expected: BUILD SUCCESSFUL, all tests PASSED

- [ ] **Step 4: Build the debug APK to confirm the whole project compiles**

Run: `./gradlew :app:assembleDebug`
Expected: BUILD SUCCESSFUL, APK generated

- [ ] **Step 5: Final commit**

```
git add app/src/main/java/com/inventory/app/MainActivity.kt
git add app/src/main/java/com/inventory/ui/scan/ScanScreen.kt
git commit -m "feat: wire import_catalog NavHost route and Імпорт каталогу button on ScanScreen"
```

---

## Self-Review

### Spec coverage check

| Spec requirement | Covered in |
|---|---|
| PickProvider step listing active import providers | Tasks 8, 9 |
| PickFile via `provider.discoverImportFiles(format)` | Tasks 8, 9 |
| MapColumns: switch + dropdowns + required-highlight + sample rows | Tasks 8, 9 |
| `fuzzySuggestMapping` auto-fill | Tasks 2, 8 |
| `detectHasHeader` heuristic (≥50 % numeric → no header) | Tasks 2, 3 |
| Confirm step with `saveMapping` checkbox | Tasks 8, 9 |
| Result step with `ImportReport` | Tasks 8, 9 |
| Per-row AUDIT operation through outbox semantics | Task 6 |
| Lookup-or-create category/location by name | Tasks 5, 6 |
| `SyncSettings.columnMapping` persistence | Task 7 |
| `SyncEngine` conditional path for saved mapping | Task 7 |
| Windows-1251 fallback for Ukrainian CSV | Task 3 |
| `parsePreview` / `parseRaw` for CSV / Excel / JSON | Tasks 3, 4 |
| `TargetField` dynamic registry (extensible for TZ 8.2) | Task 1 |
| Entry point button on `ScanScreen` and NavHost route | Task 10 |
| Build-green stubs between dependent tasks | Task 3 stubs replaced in Task 4 |

No gaps found.

### Placeholder scan

- Task 3 introduces `TODO("Implemented in Task 4")` stubs in Excel/Json — replaced in Task 4. No other placeholders.
- All test code is concrete (real CSV strings, real mocks, real assertions).
- All implementation code is concrete (no `// add error handling here` comments).

### Type-consistency check

- `ColumnMapping(treatFirstRowAsHeader, mapping: Map<Int, String?>)` — defined Task 1, used Tasks 6, 7, 8 ✓
- `ImportPreview(headerRow, sampleRows, detectedHasHeader, totalRowsEstimate)` — defined Task 1, returned from `parsePreview` in Tasks 3, 4, consumed by ViewModel in Task 8 ✓
- `ImportReport(insertedCount, updatedCount, skippedCount, skipReasons)` — defined Task 1, returned from `applyMappedImport` in Task 6, displayed in `Result` in Tasks 8, 9 ✓
- `applyMappedImport(rawRows: List<List<String?>>, mapping: ColumnMapping, targetFields: List<TargetField>): ImportReport` — signature consistent across Tasks 6, 7, 8 ✓
- `parseRaw(data, treatFirstRowAsHeader)` consistent across Tasks 3, 4 (CSV / Excel: drops row 0 conditionally; JSON: keys serve as header, flag ignored) ✓
- `TargetFields.all` referenced in Tasks 7, 8 — defined in Task 1 ✓

No type drift found.

### Scope check

This is one cohesive feature (column-mapping wizard). All 10 tasks contribute to it. No unrelated refactoring. Spec's "out of scope" items (TZ 8.2 schema extension, BAF protocol, SAF picker) are deferred to the post-mapping backlog.

---

## Execution Handoff

Plan complete. Two execution options:

**1. Subagent-Driven (recommended)** — dispatch a fresh subagent per task with two-stage review between tasks. Best for keeping context clean across the 10 tasks and reviewing each commit individually.

**2. Inline Execution** — execute tasks in this session via `superpowers:executing-plans`, with checkpoints for review. Faster end-to-end but more context pressure.
