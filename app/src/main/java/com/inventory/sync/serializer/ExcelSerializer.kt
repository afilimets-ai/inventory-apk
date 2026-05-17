package com.inventory.sync.serializer

import com.inventory.sync.SyncFormat
import com.inventory.sync.catalogimport.ColumnMappingHeuristic
import com.inventory.sync.catalogimport.ImportPreview
import org.dhatim.fastexcel.Workbook
import org.dhatim.fastexcel.reader.ReadableWorkbook
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExcelSerializer @Inject constructor() : SyncSerializer {

    override val format = SyncFormat.EXCEL

    override fun serialize(items: List<Map<String, Any?>>): ByteArray {
        if (items.isEmpty()) return ByteArray(0)
        val out = ByteArrayOutputStream()
        val wb = Workbook(out, "Inventory", "1.0")
        val ws = wb.newWorksheet("Sheet1")
        val headers = items.first().keys.toList()

        // Заголовки
        headers.forEachIndexed { col, name -> ws.value(0, col, name) }

        // Дані
        items.forEachIndexed { rowIdx, item ->
            headers.forEachIndexed { col, key ->
                val v = item[key]
                when (v) {
                    is Number -> ws.value(rowIdx + 1, col, v.toDouble())
                    is Boolean -> ws.value(rowIdx + 1, col, v)
                    else -> ws.value(rowIdx + 1, col, v?.toString() ?: "")
                }
            }
        }

        wb.finish()
        return out.toByteArray()
    }

    override fun deserialize(data: ByteArray): List<Map<String, Any?>> {
        val result = mutableListOf<Map<String, Any?>>()
        ReadableWorkbook(ByteArrayInputStream(data)).use { wb ->
            val sheet = wb.firstSheet
            sheet.openStream().use { rows ->
                var headers: List<String>? = null
                rows.forEach { row ->
                    val cellCount = row.cellCount
                    val cells = (0 until cellCount).map { idx ->
                        row.getCell(idx)?.rawValue ?: ""
                    }
                    if (headers == null) {
                        headers = cells
                    } else {
                        val h = headers!!
                        result.add(h.mapIndexed { i, key -> key to cells.getOrElse(i) { "" } }.toMap())
                    }
                }
            }
        }
        return result
    }

    override fun parsePreview(data: ByteArray, sampleSize: Int): ImportPreview {
        if (data.isEmpty()) return ImportPreview(emptyList(), emptyList(), true, 0)
        val allRows = readAllRowStrings(data)
        if (allRows.isEmpty()) return ImportPreview(emptyList(), emptyList(), true, 0)
        val headerRow = allRows[0]
        val dataRows = allRows.drop(1)
        val sampleRows = dataRows.take(sampleSize).map { row ->
            row.map { v -> v.takeIf { it.isNotBlank() } }
        }
        val rowsForHeuristic = listOf(headerRow.map { it.takeIf { v -> v.isNotBlank() } }) + sampleRows
        return ImportPreview(
            headerRow = headerRow,
            sampleRows = sampleRows,
            detectedHasHeader = ColumnMappingHeuristic.detectHasHeader(rowsForHeuristic),
            totalRowsEstimate = dataRows.size
        )
    }

    override fun parseRaw(data: ByteArray, treatFirstRowAsHeader: Boolean): List<List<String?>> {
        if (data.isEmpty()) return emptyList()
        val allRows = readAllRowStrings(data)
        val columnCount = allRows.firstOrNull()?.size ?: 0
        val dataRows = if (treatFirstRowAsHeader) allRows.drop(1) else allRows
        return dataRows.map { row ->
            List(maxOf(columnCount, row.size)) { i ->
                row.getOrNull(i)?.takeIf { it.isNotBlank() }
            }
        }
    }

    private fun readAllRowStrings(data: ByteArray): List<List<String>> {
        val result = mutableListOf<List<String>>()
        ReadableWorkbook(ByteArrayInputStream(data)).use { wb ->
            wb.firstSheet.openStream().use { rows ->
                rows.forEach { row ->
                    result.add((0 until row.cellCount).map { i ->
                        row.getCell(i)?.rawValue ?: ""
                    })
                }
            }
        }
        return result
    }
}
