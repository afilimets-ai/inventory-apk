package com.inventory.sync.serializer

import com.inventory.sync.SyncFormat

/**
 * Інтерфейс серіалізатора для конвертації даних у файл і навпаки.
 */
interface SyncSerializer {
    val format: SyncFormat

    fun serialize(items: List<Map<String, Any?>>): ByteArray

    fun deserialize(data: ByteArray): List<Map<String, Any?>>
}
