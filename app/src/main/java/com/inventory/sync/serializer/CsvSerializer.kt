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
        if (lines.size < 2) return emptyList()
        val headers = parseCsvLine(lines[0])
        return lines.drop(1).map { line ->
            val values = parseCsvLine(line)
            headers.zip(values).toMap()
        }
    }

    private fun escapeCsv(value: String): String {
        return if (value.contains(',') || value.contains('"') || value.contains('\n')) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }

    private fun parseCsvLine(line: String): List<String> {
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
}
