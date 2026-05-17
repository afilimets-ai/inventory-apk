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
