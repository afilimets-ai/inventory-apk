package com.inventory.sync.serializer

import com.inventory.sync.SyncFormat
import com.inventory.sync.catalogimport.ImportPreview

/**
 * Інтерфейс серіалізатора для конвертації даних у файл і навпаки.
 */
interface SyncSerializer {
    val format: SyncFormat

    fun serialize(items: List<Map<String, Any?>>): ByteArray

    fun deserialize(data: ByteArray): List<Map<String, Any?>>

    fun parsePreview(data: ByteArray, sampleSize: Int = 10): ImportPreview

    fun parseRaw(data: ByteArray, treatFirstRowAsHeader: Boolean): List<List<String?>>
}
