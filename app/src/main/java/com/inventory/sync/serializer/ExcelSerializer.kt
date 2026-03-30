package com.inventory.sync.serializer

import com.inventory.sync.SyncFormat
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
}
