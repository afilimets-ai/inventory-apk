package com.inventory.sync.catalogimport

import java.io.Serializable

data class ColumnMapping(
    val treatFirstRowAsHeader: Boolean,
    val mapping: Map<Int, String?>   // file column index → TargetField.id (null = skip)
) : Serializable
