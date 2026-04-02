package com.inventory.sync.serializer

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CsvSerializerTest {

    private lateinit var serializer: CsvSerializer

    @Before
    fun setUp() {
        serializer = CsvSerializer()
    }

    // ── serialize ────────────────────────────────────────────────────────────

    @Test
    fun `serialize empty list returns empty byte array`() {
        val result = serializer.serialize(emptyList())
        assertEquals(0, result.size)
    }

    @Test
    fun `serialize single item produces header and data row`() {
        val items = listOf(mapOf("barcode" to "123", "name" to "Item"))
        val csv = serializer.serialize(items).toString(Charsets.UTF_8)
        val lines = csv.trim().lines()
        assertEquals(2, lines.size)
        assertEquals("barcode,name", lines[0])
        assertEquals("123,Item", lines[1])
    }

    @Test
    fun `serialize value with comma is quoted`() {
        val items = listOf(mapOf("name" to "Milk, 1L"))
        val csv = serializer.serialize(items).toString(Charsets.UTF_8)
        assertTrue(csv.contains("\"Milk, 1L\""))
    }

    @Test
    fun `serialize value with double-quote escapes it`() {
        val items = listOf(mapOf("name" to "He said \"hello\""))
        val csv = serializer.serialize(items).toString(Charsets.UTF_8)
        assertTrue(csv.contains("\"He said \"\"hello\"\"\""))
    }

    @Test
    fun `serialize value with newline is quoted`() {
        val items = listOf(mapOf("desc" to "line1\nline2"))
        val csv = serializer.serialize(items).toString(Charsets.UTF_8)
        assertTrue(csv.contains("\"line1\nline2\""))
    }

    @Test
    fun `serialize null value produces empty field`() {
        val items = listOf(mapOf("barcode" to "123", "notes" to null))
        val csv = serializer.serialize(items).toString(Charsets.UTF_8)
        val dataLine = csv.trim().lines()[1]
        assertEquals("123,", dataLine)
    }

    // ── deserialize ──────────────────────────────────────────────────────────

    @Test
    fun `deserialize empty bytes returns empty list`() {
        val result = serializer.deserialize(ByteArray(0))
        assertTrue(result.isEmpty())
    }

    @Test
    fun `deserialize header-only returns empty list`() {
        val csv = "barcode,name\n"
        val result = serializer.deserialize(csv.toByteArray())
        assertTrue(result.isEmpty())
    }

    @Test
    fun `deserialize basic CSV returns correct map`() {
        val csv = "barcode,name,quantity\n123,Widget,10\n"
        val result = serializer.deserialize(csv.toByteArray())
        assertEquals(1, result.size)
        assertEquals("123", result[0]["barcode"])
        assertEquals("Widget", result[0]["name"])
        assertEquals("10", result[0]["quantity"])
    }

    @Test
    fun `deserialize row with fewer columns fills missing fields with null`() {
        val csv = "barcode,name,quantity\n123,Widget\n"
        val result = serializer.deserialize(csv.toByteArray())
        assertEquals(1, result.size)
        assertEquals("123", result[0]["barcode"])
        assertEquals("Widget", result[0]["name"])
        // "quantity" key must exist with null value — не просто відсутнє
        assertTrue(result[0].containsKey("quantity"))
        assertNull(result[0]["quantity"])
    }

    @Test
    fun `deserialize quoted field with comma`() {
        val csv = "name,price\n\"Milk, 1L\",25\n"
        val result = serializer.deserialize(csv.toByteArray())
        assertEquals("Milk, 1L", result[0]["name"])
    }

    @Test
    fun `deserialize quoted field with escaped double-quote`() {
        val csv = "desc\n\"He said \"\"hello\"\"\"\n"
        val result = serializer.deserialize(csv.toByteArray())
        assertEquals("He said \"hello\"", result[0]["desc"])
    }

    @Test
    fun `serialize then deserialize roundtrip preserves all values`() {
        val original = listOf(
            mapOf("barcode" to "ABC-1", "name" to "Widget, Small", "quantity" to "5.0", "notes" to null)
        )
        val bytes = serializer.serialize(original)
        val result = serializer.deserialize(bytes)

        assertEquals(1, result.size)
        assertEquals("ABC-1", result[0]["barcode"])
        assertEquals("Widget, Small", result[0]["name"])
        assertEquals("5.0", result[0]["quantity"])
    }
}
