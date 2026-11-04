package com.traceless.privacy.domain

import java.net.URL
import javax.net.ssl.HttpsURLConnection

data class OnlineCheckResult(
    val publicIp: String,
    val edge: String,
    val tls: String,
    val checkedAt: Long = System.currentTimeMillis()
)

object OnlineNetworkCheck {
    fun run(): OnlineCheckResult {
        val connection = URL("https://www.cloudflare.com/cdn-cgi/trace")
            .openConnection() as HttpsURLConnection
        try {
            connection.requestMethod = "GET"
            connection.connectTimeout = 9000
            connection.readTimeout = 9000
            connection.setRequestProperty("Accept", "text/plain")
            connection.setRequestProperty("Cache-Control", "no-cache")
            connection.connect()
            if (connection.responseCode !in 200..299) {
                throw IllegalStateException("Le service distant a répondu avec le code ${connection.responseCode}.")
            }
            val body = connection.inputStream.bufferedReader().use { it.readText() }
            val values = body.lineSequence().mapNotNull { line ->
                val index = line.indexOf('=')
                if (index > 0) line.substring(0, index) to line.substring(index + 1) else null
            }.toMap()
            val ip = values["ip"]?.takeIf { it.isNotBlank() }
                ?: throw IllegalStateException("L’adresse IP publique n’a pas été communiquée.")
            return OnlineCheckResult(
                publicIp = ip,
                edge = values["colo"]?.takeIf { it.isNotBlank() } ?: "Non communiqué",
                tls = values["tls"]?.takeIf { it.isNotBlank() } ?: "Non communiqué"
            )
        } finally {
            connection.disconnect()
        }
    }
}