package com.inventory.sync.catalogimport

object ColumnMappingHeuristic {

    private val aliases = mapOf(
        "артикул" to "sku",
        "sku" to "sku",
        "внутрішнійкод" to "sku",
        "кодтовару" to "sku",
        "кодноменклатури" to "sku",
        "група" to "group",
        "товарнагрупа" to "group",
        "номенклатурнагрупа" to "group",
        "group" to "group",
        "productgroup" to "group",
        "додатковіштрихкоди" to "additional_barcodes",
        "додатковийштрихкод" to "additional_barcodes",
        "іншіштрихкоди" to "additional_barcodes",
        "barcodes" to "additional_barcodes",
        "additionalbarcode" to "additional_barcodes",
        "additionalbarcodes" to "additional_barcodes",
        "alternatebarcode" to "additional_barcodes",
        "alternatebarcodes" to "additional_barcodes",
        "ваговий" to "is_weighted",
        "ваговийтовар" to "is_weighted",
        "weighted" to "is_weighted",
        "isweighted" to "is_weighted",
        "упаковка" to "is_package",
        "пакування" to "is_package",
        "ispackage" to "is_package",
        "package" to "is_package",
        "одиницяупаковки" to "package_unit",
        "packageunit" to "package_unit",
        "коефіцієнтупаковки" to "package_coefficient",
        "коефупаковки" to "package_coefficient",
        "packagecoefficient" to "package_coefficient",
        "packcoefficient" to "package_coefficient"
    )

    /** Row 0 is treated as data (NOT a header) if at least 50 percent of its cells parse as numbers. */
    fun detectHasHeader(rows: List<List<String?>>): Boolean {
        val row0 = rows.firstOrNull() ?: return true
        if (row0.isEmpty()) return true
        val numericCount = row0.count { it?.trim()?.toDoubleOrNull() != null }
        return numericCount.toDouble() / row0.size < 0.5
    }

    /** For each header column, suggest a TargetField.id (or null when no match). */
    fun fuzzySuggestMapping(
        headers: List<String?>,
        targets: List<TargetField>
    ): Map<Int, String?> = headers.indices.associate { idx ->
        idx to headers[idx]?.let { bestMatch(it, targets) }
    }

    private fun bestMatch(header: String, targets: List<TargetField>): String? {
        val norm = normalize(header)
        aliases[norm]?.let { targetId ->
            if (targets.any { it.id == targetId }) return targetId
        }
        // 1. Exact match (id OR displayName) after normalization
        targets.firstOrNull { normalize(it.id) == norm || normalize(it.displayName) == norm }
            ?.let { return it.id }
        // 2. Contains match against id only — displayName may be multi-word and produce false positives
        targets.firstOrNull { t ->
            val nId = normalize(t.id)
            nId.isNotEmpty() && (norm.contains(nId) || nId.contains(norm))
        }?.let { return it.id }
        // 3. Levenshtein distance no greater than 2
        targets.firstOrNull { t ->
            levenshtein(norm, normalize(t.id)) <= 2 ||
            levenshtein(norm, normalize(t.displayName)) <= 2
        }?.let { return it.id }
        return null
    }

    private fun normalize(s: String): String = s.lowercase().filter { it.isLetterOrDigit() }

    private fun levenshtein(a: String, b: String): Int {
        val dp = Array(a.length + 1) { IntArray(b.length + 1) }
        for (i in 0..a.length) dp[i][0] = i
        for (j in 0..b.length) dp[0][j] = j
        for (i in 1..a.length) for (j in 1..b.length)
            dp[i][j] = if (a[i-1] == b[j-1]) dp[i-1][j-1]
                       else 1 + minOf(dp[i-1][j], dp[i][j-1], dp[i-1][j-1])
        return dp[a.length][b.length]
    }
}
