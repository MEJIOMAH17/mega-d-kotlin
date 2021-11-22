package com.github.mejiomah17.megad.kotlin

import com.github.mejiomah17.megad.kotlin.executor.MegaD16PWM
import com.github.mejiomah17.megad.kotlin.executor.MegaD16RXT
import com.github.mejiomah17.megad.kotlin.pwm.Pwm
import com.github.mejiomah17.megad.kotlin.pwm.PwmLevel
import com.github.mejiomah17.megad.kotlin.relay.Relay
import com.github.mejiomah17.megad.kotlin.relay.RelayCommand
import com.github.mejiomah17.megad.kotlin.relay.RelayStatus
import com.github.mejiomah17.megad.kotlin.sensor.BinarySensor
import com.github.mejiomah17.megad.kotlin.sensor.BinarySensorState
import com.github.mejiomah17.megad.kotlin.sensor.wall.mount.Htu21d
import com.github.mejiomah17.megad.kotlin.sensor.wall.mount.Max44009
import com.github.mejiomah17.megad.kotlin.sensor.wall.mount.T67xx
import com.github.mejiomah17.megad.kotlin.sensor.wall.mount.WallMountSensor
import com.github.mejiomah17.megad.kotlin.sensor.wall.mount.Wallmount
import io.ktor.client.HttpClient
import io.ktor.client.call.receive
import io.ktor.client.features.get
import io.ktor.client.request.get
import io.ktor.client.request.host
import io.ktor.client.request.request
import io.ktor.client.statement.HttpResponse
import java.net.URLEncoder
import kotlinx.coroutines.delay
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

    suspend fun configureAsSLC(slcPortNumber: Byte) {
        getRawPage("pn=$slcPortNumber&pty=4&m=2")
    }

    suspend fun configureAsWallmount(sdaPortNumber: Byte, slcPortNumber: Byte) {
        configureAsSLC(slcPortNumber)
        configureI2C(sdaPortNumber = sdaPortNumber, slcPortNumber = slcPortNumber, suffix = "gr=0&d=0")
    }

    /**
     * 16 relay executor module
     */
    suspend fun configureAs16RXT(sdaPortNumber: Byte, slcPortNumber: Byte) {
        configureAsSLC(slcPortNumber)
        configureI2C(sdaPortNumber = sdaPortNumber, slcPortNumber = slcPortNumber, suffix = "gr=3&d=20&inta=")
        for (port in 0..15) {
            getRawPage("pt=$sdaPortNumber&ext=$port&ety=1&eact=&emode=0")
        }
    }

    /**
     * 16 PWM executor module
     */
    suspend fun configureAs16PWM(sdaPortNumber: Byte, slcPortNumber: Byte) {
        configureAsSLC(slcPortNumber)
        configureI2C(sdaPortNumber = sdaPortNumber, slcPortNumber = slcPortNumber, suffix = "gr=3&d=21&emt=")
    }

    suspend fun configureAsBinarySensor(portNumber: Byte) {
        getRawPage("pn=$portNumber&pty=0&ecmd=&eth=&m=0&emt=")
    }

    suspend fun getBinarySensorStatus(sensor: BinarySensor): BinarySensorState {
        return parseSensorStatus(getRawPage("pt=${sensor.port}"))
    }

    suspend fun getRelayStatus(megaD16RXT: MegaD16RXT, relay: Relay): RelayStatus {
        return parseRelayStatus(
            html = getRawPage("pt=${megaD16RXT.sdaPortNumber}&ext=${relay.number}")
        )
    }

    suspend fun getPwmLevel(megaD16PWM: MegaD16PWM, pwm: Pwm): PwmLevel {
        return parsePwmLevel(
            html = getRawPage("pt=${megaD16PWM.sdaPortNumber}&ext=${pwm.number}")
        )
    }

    suspend fun setPwmLevel(megaD16PWM: MegaD16PWM, pwm: Pwm, level: PwmLevel): PwmLevel {
        return parsePwmLevel(
            html = getRawPage("pt=${megaD16PWM.sdaPortNumber}&ext=${pwm.number}&epwm=${level.value}")
        )
    }

    suspend fun executeCommand(megaD16RXT: MegaD16RXT, relay: Relay, command: RelayCommand): RelayStatus {
        val sdaPortNumber = megaD16RXT.sdaPortNumber
        return parseRelayStatus(
            html = getRawPage("pt=$sdaPortNumber&ext=${relay.number}&cmd=${sdaPortNumber}e${relay.number}:${command.intValue}")
        )
    }

    suspend fun getTemperature(wallmount: Wallmount): Double {
        return getWallmountPage(wallmount, Htu21d.Temperature).toDouble()
    }

    /**
     * percent 0-100
     */
    suspend fun getHumidity(wallmount: Wallmount): Double {
        return getWallmountPage(wallmount, Htu21d.Humidity).toDouble()
    }

    suspend fun getLightLevel(wallmount: Wallmount): Double {
        return getWallmountPage(wallmount, Max44009).toDouble()
    }

    suspend fun getCO2Level(wallmount: Wallmount): Int {
        return getWallmountPage(wallmount, T67xx).toInt()
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
        val response: HttpResponse = httpClient.request {
            host = this@MegaDClient.host
            url.encodedPath = "$password/?$path"
        }
        return response.receive()
    }

    private suspend fun configureI2C(sdaPortNumber: Byte, slcPortNumber: Byte, suffix: String) {
        getRawPage("pn=$sdaPortNumber&pty=4&m=1&misc=$slcPortNumber&$suffix")
        getRawPage("pn=$sdaPortNumber&pty=4&m=1&misc=$slcPortNumber&$suffix")
    }

    private suspend fun getWallmountPage(wallmount: Wallmount, sensor: WallMountSensor): String {
        val path =
            "pt=${wallmount.sdaPortNumber}&scl=${wallmount.slcPortNumber}&i2c_dev=${sensor.i2cDev}&i2c_par=${sensor.i2cPair}"
        var counter = 5
        var result = getRawPage(path)
        while (result.trim().uppercase() == "NA" && counter > 0) {
            counter--
            delay(200)
            result = getRawPage(path)
        }
        return result
    }

    private fun parseRelayStatus(html: String): RelayStatus {
        val rawText = Jsoup.parse(html).body().ownText()
        val rawStatus = rawText.takeLastWhile { it != '/' }
        return RelayStatus.valueOf(rawStatus)
    }

    private fun parseSensorStatus(html: String): BinarySensorState {
        val rawText = Jsoup.parse(html).body().ownText()
        val rawStatus = rawText.substringAfter("/").substringBefore("/")
        return BinarySensorState.valueOf(rawStatus)
    }

    private fun parsePwmLevel(html: String): PwmLevel {
        val level = Jsoup.parse(html).body().allElements.first {
            it.attr("name")?.trim()?.lowercase()?.contains("pwm") ?: false
        }.attr("value") ?: error("can't find pwm level")

        return PwmLevel(level.toInt())
    }
}