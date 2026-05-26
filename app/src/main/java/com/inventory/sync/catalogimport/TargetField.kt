package com.inventory.sync.catalogimport

data class TargetField(
    val id: String,
    val displayName: String,
    val type: TargetType,
    val isRequired: Boolean = false
)

object TargetFields {
    val BARCODE  = TargetField("barcode",      "Штрихкод",     TargetType.STRING,          isRequired = true)
    val SKU      = TargetField("sku",          "SKU / артикул", TargetType.STRING)
    val NAME     = TargetField("name",         "Назва",        TargetType.STRING,          isRequired = true)
    val GROUP    = TargetField("group",        "Група",        TargetType.STRING)
    val ADDITIONAL_BARCODES = TargetField("additional_barcodes", "Додаткові штрихкоди", TargetType.STRING)
    val QUANTITY = TargetField("quantity",     "Кількість",    TargetType.DOUBLE)
    val UNIT     = TargetField("unit",         "Одиниця",      TargetType.STRING)
    val IS_WEIGHTED = TargetField("is_weighted", "Ваговий товар", TargetType.BOOLEAN)
    val IS_PACKAGE = TargetField("is_package", "Упаковка", TargetType.BOOLEAN)
    val PACKAGE_UNIT = TargetField("package_unit", "Одиниця упаковки", TargetType.STRING)
    val PACKAGE_COEFFICIENT = TargetField("package_coefficient", "Коефіцієнт упаковки", TargetType.DOUBLE)
    val DESC     = TargetField("description",  "Опис",         TargetType.STRING)
    val MIN_QTY  = TargetField("min_quantity", "Мін. залишок", TargetType.DOUBLE)
    val NOTES    = TargetField("notes",        "Примітки",     TargetType.STRING)
    val CATEGORY = TargetField("category",     "Категорія",    TargetType.LOOKUP_CATEGORY)
    val LOCATION = TargetField("location",     "Локація",      TargetType.LOOKUP_LOCATION)

    val all: List<TargetField> = listOf(
        BARCODE,
        SKU,
        NAME,
        GROUP,
        ADDITIONAL_BARCODES,
        QUANTITY,
        UNIT,
        IS_WEIGHTED,
        IS_PACKAGE,
        PACKAGE_UNIT,
        PACKAGE_COEFFICIENT,
        DESC,
        MIN_QTY,
        NOTES,
        CATEGORY,
        LOCATION
    )
    fun byId(id: String): TargetField? = all.firstOrNull { it.id == id }
}
