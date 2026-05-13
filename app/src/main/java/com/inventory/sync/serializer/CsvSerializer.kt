package com.inventory.sync.serializer

import com.inventory.sync.SyncFormat
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CsvSerializer @Inject constructor() : SyncSerializer {

    override val format = SyncFormat.CSV

    override fun serialize(items: List<Map<String, Any?>>): ByteArray {
        if (items.isEmpty()) return ByteArray(0)
        val sb = StringBuilder()
        val headers = items.first().keys.toList()
        sb.appendLine(headers.joinToString(",") { escapeCsv(it) })
        for (item in items) {
            sb.appendLine(headers.joinToString(",") { key ->
                escapeCsv(item[key]?.toString() ?: "")
            })
        }
        return sb.toString().toByteArray(Charsets.UTF_8)
    }

    override fun deserialize(data: ByteArray): List<Map<String, Any?>> {
        val lines = data.toString(Charsets.UTF_8).lines().filter { it.isNotBlank() }
        if (lines.isEmpty()) return emptyList()

        val firstRow = parseCsvLine(lines[0])
        if (looksLikeHeaders(firstRow)) {
            if (lines.size < 2) return emptyList()
            return lines.drop(1).map { line ->
                val values = parseCsvLine(line)
                firstRow.indices.associate { i -> firstRow[i] to values.getOrNull(i) }
            }
        }

        throw MissingHeadersException(
            columnCount = firstRow.size,
            sampleRow = firstRow
        )
    }

    fun deserializeWithHeaders(data: ByteArray, headers: List<String>): List<Map<String, Any?>> {
        val lines = data.toString(Charsets.UTF_8).lines().filter { it.isNotBlank() }
        if (lines.isEmpty()) return emptyList()
        return lines.map { line ->
            val values = parseCsvLine(line)
            headers.indices.associate { i ->
                headers[i] to values.getOrNull(i)
            }.filterKeys { it.isNotBlank() }
        }
    }

    fun peekColumns(data: ByteArray): CsvPreview {
        val lines = data.toString(Charsets.UTF_8).lines().filter { it.isNotBlank() }
        if (lines.isEmpty()) return CsvPreview(0, emptyList(), false)
        val firstRow = parseCsvLine(lines[0])
        return CsvPreview(
            columnCount = firstRow.size,
            sampleRow = firstRow,
            hasHeaders = looksLikeHeaders(firstRow)
        )
    }

    private fun looksLikeHeaders(row: List<String>): Boolean {
        val matches = row.count { it.trim().lowercase() in KNOWN_HEADERS }
        if (matches >= 1) return true
        return row.all { cell ->
            val trimmed = cell.trim()
            trimmed.length in 1..30 && trimmed.none { it.isDigit() }
        }
    }

    private fun escapeCsv(value: String): String {
        return if (value.contains(',') || value.contains('"') || value.contains('\n')) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }

    internal fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        var current = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            when {
                c == '"' && inQuotes && i + 1 < line.length && line[i + 1] == '"' -> {
                    current.append('"')
                    i++
                }
                c == '"' -> inQuotes = !inQuotes
                c == ',' && !inQuotes -> {
                    result.add(current.toString())
                    current = StringBuilder()
                }
                else -> current.append(c)
            }
            i++
        }
        result.add(current.toString())
        return result
    }

    companion object {
        val KNOWN_HEADERS = setOf(
            "barcode", "name", "description", "quantity", "unit",
            "min_quantity", "category_id", "location_id", "notes",
            "id", "updated_at", "created_at"
        )

        val MAPPABLE_COLUMNS = listOf(
            CsvColumn("barcode", "Штрихкод"),
            CsvColumn("name", "Назва"),
            CsvColumn("description", "Опис"),
            CsvColumn("quantity", "Кількість"),
            CsvColumn("unit", "Одиниця виміру"),
            CsvColumn("min_quantity", "Мін. кількість"),
            CsvColumn("notes", "Примітки"),
        )
    }
}

data class CsvColumn(
    val key: String,
    val displayName: String
)

data class CsvPreview(
    val columnCount: Int,
    val sampleRow: List<String>,
    val hasHeaders: Boolean
)

class MissingHeadersException(
    val columnCount: Int,
    val sampleRow: List<String>
) : Exception("CSV file is missing column headers")
