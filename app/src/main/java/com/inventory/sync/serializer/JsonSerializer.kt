package com.inventory.sync.serializer

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.inventory.sync.SyncFormat
import com.inventory.sync.catalogimport.ImportPreview
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JsonSerializer @Inject constructor(private val gson: Gson) : SyncSerializer {

    override val format = SyncFormat.JSON

    override fun serialize(items: List<Map<String, Any?>>): ByteArray =
        gson.toJson(items).toByteArray(Charsets.UTF_8)

    override fun deserialize(data: ByteArray): List<Map<String, Any?>> = parseJsonObjects(data)

    override fun parsePreview(data: ByteArray, sampleSize: Int): ImportPreview {
        val items = parseJsonObjects(data)
        if (items.isEmpty()) return ImportPreview(emptyList(), emptyList(), true, 0)
        val keys = items.first().keys.toList()
        val dataItems = items.drop(1)
        val sampleRows = dataItems.take(sampleSize).map { obj ->
            keys.map { k -> obj[k]?.toString() }
        }
        return ImportPreview(
            headerRow = keys,
            sampleRows = sampleRows,
            detectedHasHeader = true,
            totalRowsEstimate = dataItems.size
        )
    }

    override fun parseRaw(data: ByteArray, treatFirstRowAsHeader: Boolean): List<List<String?>> {
        val items = parseJsonObjects(data)
        if (items.isEmpty()) return emptyList()
        val keys = items.first().keys.toList()
        return items.map { obj -> keys.map { k -> obj[k]?.toString() } }
    }

    private fun parseJsonObjects(data: ByteArray): List<Map<String, Any?>> {
        val type = object : TypeToken<List<Map<String, Any?>>>() {}.type
        return gson.fromJson(data.toString(Charsets.UTF_8), type) ?: emptyList()
    }
}
