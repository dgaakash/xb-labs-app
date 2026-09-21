package com.xblabs.app.parser

import com.xblabs.app.data.models.Client
import org.json.JSONArray
import org.json.JSONObject

data class ParsedLeadRecord(
    val businessName: String,
    val category: String = "General",
    val rating: Double = 0.0,
    val reviewCount: Int = 0,
    val phone: String,
    val normalizedPhone: String,
    val website: String = "",
    val address: String = "",
    val mapsUrl: String = "",
    val isValid: Boolean = true,
    val invalidReason: String? = null,
    val isDuplicate: Boolean = false,
    val duplicateReason: String? = null
)

data class ImportPreviewResult(
    val records: List<ParsedLeadRecord>,
    val totalCount: Int,
    val validCount: Int,
    val invalidCount: Int,
    val duplicateCount: Int
)

object BusinessReportParser {

    /**
     * Normalizes a phone string by removing spaces, dashes, brackets, keeping digits and leading +.
     */
    fun normalizePhone(rawPhone: String): String {
        val digits = rawPhone.replace(Regex("[^0-9+]"), "")
        return if (digits.startsWith("+")) digits else digits.trimStart('0')
    }

    /**
     * Parses raw input text containing one or many business report entries (Text or JSON).
     */
    fun parseInputText(inputText: String, existingClients: List<Client> = emptyList()): ImportPreviewResult {
        if (inputText.isBlank()) {
            return ImportPreviewResult(emptyList(), 0, 0, 0, 0)
        }

        val trimmed = inputText.trim()
        val recordsToEvaluate = mutableListOf<ParsedLeadRecord>()

        if (trimmed.startsWith("[") || trimmed.startsWith("{")) {
            val jsonRecords = parseJsonBlocks(trimmed)
            if (jsonRecords.isNotEmpty()) {
                recordsToEvaluate.addAll(jsonRecords)
            }
        }

        if (recordsToEvaluate.isEmpty()) {
            val rawBlocks = splitIntoBlocks(inputText)
            for (block in rawBlocks) {
                val record = parseSingleBlock(block)
                if (record.businessName.isNotBlank() || record.phone.isNotBlank()) {
                    recordsToEvaluate.add(record)
                }
            }
        }

        val parsedRecords = mutableListOf<ParsedLeadRecord>()
        val seenPhonesInBatch = mutableSetOf<String>()

        val existingNormalizedPhones = existingClients.map { it.normalizedPhone }.filter { it.isNotBlank() }.toSet()
        val existingNamesAndAddresses = existingClients.map { (it.businessName.lowercase().trim() + "|" + it.address.lowercase().trim()) }.toSet()

        for (record in recordsToEvaluate) {
            // Check validation
            val (valid, reason) = validateRecord(record)
            if (!valid) {
                parsedRecords.add(record.copy(isValid = false, invalidReason = reason))
                continue
            }

            val normPhone = record.normalizedPhone
            val nameAddrKey = record.businessName.lowercase().trim() + "|" + record.address.lowercase().trim()

            // Check duplicate
            var isDup = false
            var dupReason: String? = null

            if (normPhone.isNotBlank() && (existingNormalizedPhones.contains(normPhone) || seenPhonesInBatch.contains(normPhone))) {
                isDup = true
                dupReason = "Duplicate phone number ($normPhone)"
            } else if (record.businessName.isNotBlank() && record.address.isNotBlank() && existingNamesAndAddresses.contains(nameAddrKey)) {
                isDup = true
                dupReason = "Duplicate business name & address"
            }

            if (normPhone.isNotBlank() && !isDup) {
                seenPhonesInBatch.add(normPhone)
            }

            parsedRecords.add(record.copy(isValid = true, isDuplicate = isDup, duplicateReason = dupReason))
        }

        val total = parsedRecords.size
        val validCount = parsedRecords.count { it.isValid && !it.isDuplicate }
        val invalidCount = parsedRecords.count { !it.isValid }
        val duplicateCount = parsedRecords.count { it.isDuplicate }

        return ImportPreviewResult(
            records = parsedRecords,
            totalCount = total,
            validCount = validCount,
            invalidCount = invalidCount,
            duplicateCount = duplicateCount
        )
    }

    private fun parseJsonBlocks(trimmedText: String): List<ParsedLeadRecord> {
        val records = mutableListOf<ParsedLeadRecord>()
        try {
            if (trimmedText.startsWith("[")) {
                val array = JSONArray(trimmedText)
                for (i in 0 until array.length()) {
                    val obj = array.optJSONObject(i) ?: continue
                    records.add(parseSingleJsonObject(obj))
                }
            } else if (trimmedText.startsWith("{")) {
                val rootObj = JSONObject(trimmedText)
                val wrapperKey = listOf("leads", "clients", "records", "data").firstOrNull { rootObj.has(it) && rootObj.optJSONArray(it) != null }
                if (wrapperKey != null) {
                    val array = rootObj.optJSONArray(wrapperKey)!!
                    for (i in 0 until array.length()) {
                        val obj = array.optJSONObject(i) ?: continue
                        records.add(parseSingleJsonObject(obj))
                    }
                } else {
                    records.add(parseSingleJsonObject(rootObj))
                }
            }
        } catch (e: Exception) {
            // Ignore JSON syntax exceptions; fallback to text parsing
        }
        return records
    }

    private fun parseSingleJsonObject(obj: JSONObject): ParsedLeadRecord {
        fun getStringOpt(vararg keys: String): String {
            for (key in keys) {
                if (obj.has(key) && !obj.isNull(key)) {
                    val valStr = obj.optString(key, "").trim()
                    if (valStr.isNotBlank()) return valStr
                }
            }
            return ""
        }

        fun getDoubleOpt(vararg keys: String): Double {
            for (key in keys) {
                if (obj.has(key) && !obj.isNull(key)) {
                    val d = obj.optDouble(key, 0.0)
                    if (!d.isNaN()) return d
                }
            }
            return 0.0
        }

        fun getIntOpt(vararg keys: String): Int {
            for (key in keys) {
                if (obj.has(key) && !obj.isNull(key)) {
                    val i = obj.optInt(key, 0)
                    if (i != 0) return i
                }
            }
            return 0
        }

        val name = getStringOpt("businessName", "business_name", "business", "name", "title")
        val category = getStringOpt("category", "type").ifBlank { "General" }
        val rating = getDoubleOpt("rating", "stars")
        val reviews = getIntOpt("reviewCount", "review_count", "reviews", "number_of_reviews")
        val phone = getStringOpt("phone", "normalizedPhone", "mobile", "contact", "tel")
        var website = getStringOpt("website", "site", "url")
        val address = getStringOpt("address", "location")
        val mapsUrl = getStringOpt("mapsUrl", "maps_url", "maps", "google_maps")

        if (website.equals("N/A", ignoreCase = true) || website.equals("none", ignoreCase = true)) {
            website = ""
        }

        val normPhone = normalizePhone(phone)

        return ParsedLeadRecord(
            businessName = name,
            category = category,
            rating = rating,
            reviewCount = reviews,
            phone = phone,
            normalizedPhone = normPhone,
            website = website,
            address = address,
            mapsUrl = mapsUrl
        )
    }

    private fun splitIntoBlocks(text: String): List<List<String>> {
        val lines = text.lines().map { it.trim() }
        val blocks = mutableListOf<List<String>>()
        var currentBlock = mutableListOf<String>()

        for (line in lines) {
            if (line.isBlank() || line.startsWith("---") || line.startsWith("===")) {
                if (currentBlock.isNotEmpty()) {
                    blocks.add(currentBlock)
                    currentBlock = mutableListOf()
                }
            } else {
                currentBlock.add(line)
            }
        }
        if (currentBlock.isNotEmpty()) {
            blocks.add(currentBlock)
        }

        return blocks
    }

    private fun parseSingleBlock(lines: List<String>): ParsedLeadRecord {
        var name = ""
        var category = "General"
        var rating = 0.0
        var reviews = 0
        var phone = ""
        var website = ""
        var address = ""
        var mapsUrl = ""

        val map = mutableMapOf<String, String>()

        // First pass: try key-value parsing
        for (line in lines) {
            if (line.contains(":")) {
                val parts = line.split(":", limit = 2)
                val key = parts[0].trim().lowercase()
                val value = parts[1].trim()
                map[key] = value
            }
        }

        if (map.isNotEmpty()) {
            name = map["business name"] ?: map["business"] ?: map["name"] ?: map["title"] ?: ""
            category = map["category"] ?: map["type"] ?: "General"
            phone = map["phone"] ?: map["mobile"] ?: map["contact"] ?: map["tel"] ?: ""
            website = map["website"] ?: map["site"] ?: map["url"] ?: ""
            address = map["address"] ?: map["location"] ?: ""
            mapsUrl = map["maps url"] ?: map["maps"] ?: map["google maps"] ?: ""

            val ratingStr = map["rating"] ?: ""
            if (ratingStr.isNotBlank()) {
                rating = ratingStr.toDoubleOrNull() ?: 0.0
            }

            val reviewsStr = map["number of reviews"] ?: map["reviews"] ?: map["review count"] ?: ""
            if (reviewsStr.isNotBlank()) {
                reviews = reviewsStr.replace(Regex("[^0-9]"), "").toIntOrNull() ?: 0
            }
        }

        // Second pass: fallback if positional multi-line block
        if (name.isBlank() && lines.isNotEmpty()) {
            name = lines[0]
            if (lines.size > 1 && !lines[1].contains(":") && !lines[1].startsWith("http")) {
                category = lines[1]
            }

            for (i in lines.indices) {
                val l = lines[i]
                if (phone.isBlank() && isPhonePattern(l)) {
                    phone = l.replace(Regex("(?i)phone:?"), "").trim()
                }
                if (website.isBlank() && (l.startsWith("http") || l.contains("www.") || l.endsWith(".com"))) {
                    if (l.contains("maps.google") || l.contains("goo.gl/maps") || l.contains("google.com/maps")) {
                        mapsUrl = l
                    } else {
                        website = l
                    }
                }
                if (l.contains("★") || l.lowercase().contains("rating")) {
                    val match = Regex("""(\d\.\d)\s*\(?(\d+)?\)?=""").find(l)
                    if (match != null) {
                        rating = match.groupValues[1].toDoubleOrNull() ?: rating
                        reviews = match.groupValues[2].toIntOrNull() ?: reviews
                    }
                }
                if (address.isBlank() && (l.contains("Rd") || l.contains("Ln") || l.contains("Street") || l.contains("Nagar") || l.contains("Delhi") || l.contains("Market"))) {
                    address = l
                }
            }
        }

        // Clean up Website
        if (website.equals("N/A", ignoreCase = true) || website.equals("none", ignoreCase = true)) {
            website = ""
        }

        val normPhone = normalizePhone(phone)

        return ParsedLeadRecord(
            businessName = name.trim(),
            category = category.trim(),
            rating = rating,
            reviewCount = reviews,
            phone = phone.trim(),
            normalizedPhone = normPhone,
            website = website.trim(),
            address = address.trim(),
            mapsUrl = mapsUrl.trim()
        )
    }

    private fun isPhonePattern(text: String): Boolean {
        val clean = text.replace(Regex("[^0-9+]"), "")
        return clean.length >= 8
    }

    private fun validateRecord(record: ParsedLeadRecord): Pair<Boolean, String?> {
        if (record.businessName.isBlank()) {
            return Pair(false, "Missing business name")
        }
        if (record.phone.isBlank() || record.normalizedPhone.length < 7) {
            return Pair(false, "Missing or invalid phone number")
        }
        return Pair(true, null)
    }
}
