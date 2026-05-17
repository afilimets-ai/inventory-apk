package com.inventory.sync.serializer

import com.inventory.sync.SyncFormat
import com.inventory.sync.catalogimport.ColumnMappingHeuristic
import com.inventory.sync.catalogimport.ImportPreview
import java.nio.charset.Charset
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
            headers.indices.associate { i -> headers[i] to values.getOrNull(i) }
        }
    }

    private fun escapeCsv(value: String): String {
        return if (value.contains(',') || value.contains('"') || value.contains('\n')) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }

    override fun parsePreview(data: ByteArray, sampleSize: Int): ImportPreview {
        if (data.isEmpty()) return ImportPreview(emptyList(), emptyList(), true, 0)
        val text = decodeWithFallback(data)
        val lines = text.lines().filter { it.isNotBlank() }
        if (lines.isEmpty()) return ImportPreview(emptyList(), emptyList(), true, 0)
        val headerRow = parseCsvLine(lines[0])
        val dataLines = lines.drop(1)
        val sampleRows = dataLines.take(sampleSize).map { line ->
            parseCsvLine(line).map { v -> v.takeIf { it.isNotEmpty() } }
        }
        val rowsForHeuristic = listOf(headerRow.map { it.takeIf { v -> v.isNotEmpty() } }) + sampleRows
        return ImportPreview(
            headerRow = headerRow,
            sampleRows = sampleRows,
            detectedHasHeader = ColumnMappingHeuristic.detectHasHeader(rowsForHeuristic),
            totalRowsEstimate = dataLines.size
        )
    }

    override fun parseRaw(data: ByteArray, treatFirstRowAsHeader: Boolean): List<List<String?>> {
        if (data.isEmpty()) return emptyList()
        val lines = decodeWithFallback(data).lines().filter { it.isNotBlank() }
        val columnCount = lines.firstOrNull()?.let { parseCsvLine(it).size } ?: 0
        val dataLines = if (treatFirstRowAsHeader) lines.drop(1) else lines
        return dataLines.map { line ->
            val cells = parseCsvLine(line)
            List(maxOf(columnCount, cells.size)) { i ->
                cells.getOrNull(i)?.takeIf { it.isNotEmpty() }
            }
        }
    }

    private fun decodeWithFallback(data: ByteArray): String {
        val utf8 = data.toString(Charsets.UTF_8)
        val sample = utf8.take(1024)
        if (sample.isEmpty()) return utf8
        val replacements = sample.count { it == '�' }
        return if (replacements.toDouble() / sample.length >= 0.01)
            data.toString(Charset.forName("windows-1251"))
        else utf8
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
