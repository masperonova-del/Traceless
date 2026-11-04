package com.traceless.privacy.data

import android.content.Context

data class ScanHistoryEntry(
    val timestamp: Long,
    val good: Int,
    val warning: Int,
    val unavailable: Int
)

class LocalPreferences(context: Context) {
    private val prefs = context.getSharedPreferences("traceless_local", Context.MODE_PRIVATE)

    fun isChecked(id: String): Boolean = prefs.getBoolean(id, false)

    fun setChecked(id: String, checked: Boolean) {
        prefs.edit().putBoolean(id, checked).apply()
    }

    fun saveLastReview(timestamp: Long) {
        prefs.edit().putLong("last_review", timestamp).apply()
    }

    fun lastReview(): Long = prefs.getLong("last_review", 0L)

    fun saveLastScan(timestamp: Long) {
        prefs.edit().putLong("last_scan", timestamp).apply()
    }

    fun lastScan(): Long = prefs.getLong("last_scan", 0L)

    fun saveScanSummary(timestamp: Long, good: Int, warning: Int, unavailable: Int) {
        val entry = "$timestamp,$good,$warning,$unavailable"
        val existing = prefs.getString("scan_history", "").orEmpty()
        val updated = (listOf(entry) + existing.lineSequence()
            .filter { it.isNotBlank() }
            .take(19)).joinToString("\n")
        prefs.edit()
            .putLong("last_scan", timestamp)
            .putString("scan_history", updated)
            .apply()
    }

    fun scanHistory(): List<ScanHistoryEntry> =
        prefs.getString("scan_history", "").orEmpty()
            .lineSequence()
            .mapNotNull { line ->
                val parts = line.split(",")
                if (parts.size != 4) return@mapNotNull null
                val values = parts.mapNotNull { it.toLongOrNull() }
                if (values.size != 4) return@mapNotNull null
                ScanHistoryEntry(values[0], values[1].toInt(), values[2].toInt(), values[3].toInt())
            }
            .toList()
}