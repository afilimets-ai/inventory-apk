package com.inventory.barcode

data class BarcodeLookupProduct(
    val barcode: String,
    val name: String,
    val description: String = "",
    val brand: String = "",
    val unit: String = "шт",
    val source: String
)
