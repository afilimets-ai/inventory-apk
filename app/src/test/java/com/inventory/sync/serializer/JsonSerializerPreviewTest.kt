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
