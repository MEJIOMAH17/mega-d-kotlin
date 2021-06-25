package com.github.mejiomah17.megad.kotlin

import com.github.mejiomah17.megad.kotlin.pwm.Pwm
import com.github.mejiomah17.megad.kotlin.pwm.PwmLevel
import com.github.mejiomah17.megad.kotlin.relay.Relay
import com.github.mejiomah17.megad.kotlin.relay.RelayCommand
import com.github.mejiomah17.megad.kotlin.relay.RelayStatus
import io.ktor.client.HttpClient
import io.ktor.client.call.receive
import io.ktor.client.features.get
import io.ktor.client.request.get
import io.ktor.client.request.host
import io.ktor.client.request.request
import io.ktor.client.statement.HttpResponse
import java.net.URLEncoder
import org.jsoup.Jsoup

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

    /**
     * PWD - ШИМ
     */
    suspend fun configureAsPwd(deviceNumber: Int) {
        getRawPage("pn=$deviceNumber&pty=1&d=0&m=1&grp=")
    }

    /**
     * SW - ordinal relay
     */
    suspend fun configureAsSW(deviceNumber: Int) {
        getRawPage("pn=$deviceNumber&pty=1&d=0&m=0&pwmm=0&grp=&fr=0")
    }

    suspend fun getPwmLevel(pwm: Pwm): PwmLevel {
        return parsePwmLevel(
            html = getRawPage("pt=${pwm.number}")
        )
    }

    suspend fun setPwmLevel(pwm: Pwm, level: PwmLevel): PwmLevel {
        return parsePwmLevel(
            html = getRawPage("pt=${pwm.number}&pwm=${level.value}")
        )
    }

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

    private fun parsePwmLevel(html: String): PwmLevel {
        val level = Jsoup.parse(html).body().allElements.first {
            it.attr("name")?.trim()?.lowercase() == "pwm"
        }.attr("value") ?: error("can't find pwm level")

        return PwmLevel(level.toInt())
    }
}