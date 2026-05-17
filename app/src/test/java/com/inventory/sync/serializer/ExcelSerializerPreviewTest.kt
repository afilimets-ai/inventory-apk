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
