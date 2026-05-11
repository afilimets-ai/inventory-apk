package com.inventory.sync

import java.io.Serializable

/**
 * Налаштування CSV-імпорту: роздільник, наявність заголовка та порядок колонок.
 *
 * Користувач задає порядок колонок ліворуч → праворуч; кожна колонка може бути
 * семантичним полем ([CsvFieldType.BARCODE], [CsvFieldType.NAME] тощо) або
 * [CsvFieldType.IGNORE] — таку колонку пропускаємо.
 *
 * Якщо у `SyncSettings.csvImportConfigJson` не збережено конфіг — використовуємо
 * старий стандарт: кома-роздільник, перший рядок = заголовок з очікуваними
 * іменами `barcode/name/quantity/unit/description`.
 */
data class CsvImportConfig(
    val delimiter: CsvDelimiter = CsvDelimiter.COMMA,
    val ignoreFirstRow: Boolean = true,
    val columns: List<CsvFieldType> = DEFAULT_COLUMNS,
) : Serializable {

    companion object {
        val DEFAULT_COLUMNS: List<CsvFieldType> = listOf(
            CsvFieldType.BARCODE,
            CsvFieldType.NAME,
            CsvFieldType.QUANTITY,
            CsvFieldType.UNIT,
        )

        val DEFAULT: CsvImportConfig = CsvImportConfig()
    }
}

enum class CsvDelimiter(val char: Char, val displayName: String) {
    COMMA(',', ","),
    SEMICOLON(';', ";"),
    PIPE('|', "|"),
    TAB('\t', "Tab"),
}

enum class CsvFieldType(val key: String?, val displayName: String) {
    BARCODE("barcode", "Штрих-код"),
    NAME("name", "Назва товару"),
    QUANTITY("quantity", "Кількість"),
    UNIT("unit", "Одиниці виміру"),
    DESCRIPTION("description", "Опис"),
    LOCATION("location", "Локація / Відділ"),
    CATEGORY("category", "Категорія / Група"),
    IGNORE(null, "— ігнорувати —"),
}
