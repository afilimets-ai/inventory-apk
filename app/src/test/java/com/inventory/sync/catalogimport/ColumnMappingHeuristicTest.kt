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
    fun `fuzzySuggestMapping maps Ukrainian SKU and quantity aliases`() {
        val result = ColumnMappingHeuristic.fuzzySuggestMapping(listOf("артикул", "залишок"), TargetFields.all)
        assertEquals("sku", result[0])
        assertNull(result[1])
    }

    @Test
    fun `fuzzySuggestMapping maps group additional barcode and package columns`() {
        val result = ColumnMappingHeuristic.fuzzySuggestMapping(
            listOf("група", "додаткові штрихкоди", "ваговий", "коефіцієнт упаковки"),
            TargetFields.all
        )

        assertEquals("group", result[0])
        assertEquals("additional_barcodes", result[1])
        assertEquals("is_weighted", result[2])
        assertEquals("package_coefficient", result[3])
    }
}
