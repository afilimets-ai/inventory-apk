package com.inventory.ui.settings

import org.junit.Assert.assertEquals
import org.junit.Test

class SyncSettingsViewModelTest {

    @Test
    fun `withUniqueColumnMapping removes duplicate target from previous column`() {
        val mapping = mapOf(
            0 to "barcode",
            1 to "name",
            2 to null
        )

        val updated = mapping.withUniqueColumnMapping(columnIndex = 2, fieldId = "barcode")

        assertEquals(null, updated[0])
        assertEquals("name", updated[1])
        assertEquals("barcode", updated[2])
    }

    @Test
    fun `withUniqueColumnMapping keeps other columns when field is skipped`() {
        val mapping = mapOf(
            0 to "barcode",
            1 to "name"
        )

        val updated = mapping.withUniqueColumnMapping(columnIndex = 1, fieldId = null)

        assertEquals("barcode", updated[0])
        assertEquals(null, updated[1])
    }
}
