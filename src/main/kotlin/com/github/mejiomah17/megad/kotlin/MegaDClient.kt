package com.github.mejiomah17.megad.kotlin

import com.github.mejiomah17.megad.kotlin.relay.Relay
import com.github.mejiomah17.megad.kotlin.relay.RelayCommand
import com.github.mejiomah17.megad.kotlin.relay.RelayStatus
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.*
import okhttp3.OkHttpClient
import org.jsoup.Jsoup
import java.net.URLEncoder

/**
 * Use host and password to specify prefix path for client
 * see [get]
 */
class MegaDClient(
    private val httpClient: HttpClient = HttpClient(),
    /**
     * host name, or ip address
     * mymega.com (if you have DNS, which resolve mymega.com to your mega)
     * or
     * 192.168.0.14
     */
    private val host: String,
    /**
     * password from MEGAD default password is sec https://ab-log.ru/smart-house/ethernet/megad-2561
     */
    private val password: String,
) {

    suspend fun getRelayStatus(relay: Relay): RelayStatus {
        return parseRelayStatus(
            html = getRawPage("pt=${relay.number}")
        )
    }

    suspend fun executeCommand(relay: Relay, command: RelayCommand): RelayStatus {
        return parseRelayStatus(
            html = getRawPage("pt=${relay.number}&cmd=${relay.number}:${command.intValue}")
        )
    }

    suspend fun getRawPage(path: String): String {
        val url = URLEncoder.encode("http://$host/$password/?$path", "UTF-8");
        val response: HttpResponse = httpClient.request {
            host = this@MegaDClient.host
            this.url.encodedPath = "$password/?$path"
        }
        return response.receive()
    }

    private fun parseRelayStatus(html: String): RelayStatus {
        val rawText = Jsoup.parse(html).body().ownText()
        val rawStatus = rawText.takeLastWhile { it != '/' }
        return RelayStatus.valueOf(rawStatus)
    }
}