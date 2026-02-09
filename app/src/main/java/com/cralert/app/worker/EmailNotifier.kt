package com.cralert.app.worker

import android.content.Context
import android.util.Log
import com.cralert.app.data.Alert
import com.cralert.app.data.local.SettingsRepository
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.concurrent.thread

class EmailNotifier(
    private val settings: SettingsRepository
) {
    fun sendAlertAsync(
        context: Context,
        alert: Alert,
        currentPrice: Double,
        pricesUpdatedAt: Long
    ) {
        if (!settings.isEmailEnabled()) return
        val apiKey = settings.getResendApiKey()
        val from = settings.getEmailFrom()
        val to = settings.getEmailTo()
        if (apiKey.isBlank() || from.isBlank() || to.isBlank()) return

        thread(name = "email-alert") {
            try {
                sendAlert(context, apiKey, from, to, alert, currentPrice, pricesUpdatedAt)
            } catch (ex: Exception) {
                Log.e(TAG, "Email send failed: ${ex.message}", ex)
            }
        }
    }

    private fun sendAlert(
        context: Context,
        apiKey: String,
        from: String,
        to: String,
        alert: Alert,
        currentPrice: Double,
        pricesUpdatedAt: Long
    ) {
        val pair = "${alert.symbol}/${alert.quoteSymbol}"
        val subject = "CRAlert: $pair ${alert.condition.name} ${formatPrice(alert.targetPrice)} ${alert.quoteSymbol}"
        val sentAt = formatDateTime(System.currentTimeMillis())
        val pricesAt = if (pricesUpdatedAt > 0L) formatDateTime(pricesUpdatedAt) else "unknown"
        val html = """
            <p><strong>$pair</strong></p>
            <p>Condition: ${alert.condition.name}</p>
            <p>Target: ${formatPrice(alert.targetPrice)} ${alert.quoteSymbol}</p>
            <p>Current: ${formatPrice(currentPrice)} ${alert.quoteSymbol}</p>
            <p>Sent: $sentAt</p>
            <p>Prices updated: $pricesAt</p>
        """.trimIndent()

        val payload = buildJson(
            from = from,
            to = to,
            subject = subject,
            html = html
        )
        val url = settings.getResendBaseUrl().trimEnd('/') + "/emails"
        val response = postJson(url, apiKey, payload)
        Log.i(TAG, "Email response http=${response.code} body=${response.body.take(200)}")
    }

    private fun buildJson(from: String, to: String, subject: String, html: String): String {
        val toList = to.split(",", ";")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .joinToString(",") { "\"${jsonEscape(it)}\"" }
        return """
            {
              "from": "${jsonEscape(from)}",
              "to": [$toList],
              "subject": "${jsonEscape(subject)}",
              "html": "${jsonEscape(html)}"
            }
        """.trimIndent()
    }

    private fun postJson(urlString: String, apiKey: String, payload: String): HttpResponse {
        val url = URL(urlString)
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = settings.getConnectTimeoutMs()
            readTimeout = settings.getReadTimeoutMs()
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Authorization", "Bearer $apiKey")
        }
        return try {
            connection.outputStream.use { it.write(payload.toByteArray()) }
            val code = connection.responseCode
            val stream = if (code in 200..299) {
                BufferedInputStream(connection.inputStream)
            } else {
                BufferedInputStream(connection.errorStream)
            }
            val reader = BufferedReader(InputStreamReader(stream))
            val response = reader.readText()
            reader.close()
            HttpResponse(code, response)
        } finally {
            connection.disconnect()
        }
    }

    private fun jsonEscape(value: String): String {
        return value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
    }

    private fun formatPrice(value: Double): String {
        return String.format(Locale.US, "%.2f", value)
    }

    private fun formatDateTime(value: Long): String {
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return format.format(Date(value))
    }

    private data class HttpResponse(
        val code: Int,
        val body: String
    )

    companion object {
        private const val TAG = "EmailNotifier"
    }
}
