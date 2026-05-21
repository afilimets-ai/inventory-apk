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
    fun `parsePreview strips UTF-8 BOM from first header`() {
        val csv = "\uFEFFbarcode,name,quantity\n123,Widget,5\n"
        val preview = serializer.parsePreview(csv.toByteArray(Charsets.UTF_8))

        assertEquals(listOf("barcode", "name", "quantity"), preview.headerRow)
        assertEquals(listOf("123", "Widget", "5"), preview.sampleRows[0])
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
    fun `parseRaw strips UTF-8 BOM from first data cell`() {
        val csv = "\uFEFF4820001,Widget,5\n"
        val rows = serializer.parseRaw(csv.toByteArray(Charsets.UTF_8), treatFirstRowAsHeader = false)

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
