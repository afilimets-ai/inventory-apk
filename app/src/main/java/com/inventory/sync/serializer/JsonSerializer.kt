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

    override fun deserialize(data: ByteArray): List<Map<String, Any?>> {
        val type = object : TypeToken<List<Map<String, Any?>>>() {}.type
        return gson.fromJson(data.toString(Charsets.UTF_8), type) ?: emptyList()
    }

    override fun parsePreview(data: ByteArray, sampleSize: Int): ImportPreview = TODO("Implemented in Task 4")
    override fun parseRaw(data: ByteArray, treatFirstRowAsHeader: Boolean): List<List<String?>> = TODO("Implemented in Task 4")
}
