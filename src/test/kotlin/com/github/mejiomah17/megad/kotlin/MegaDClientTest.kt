package com.github.mejiomah17.megad.kotlin

import com.github.mejiomah17.megad.kotlin.executor.MegaD16PWM
import com.github.mejiomah17.megad.kotlin.executor.MegaD16RXT
import com.github.mejiomah17.megad.kotlin.pwm.Pwm
import com.github.mejiomah17.megad.kotlin.pwm.PwmLevel
import com.github.mejiomah17.megad.kotlin.relay.Relay
import com.github.mejiomah17.megad.kotlin.relay.RelayCommand
import com.github.mejiomah17.megad.kotlin.relay.RelayStatus
import com.github.mejiomah17.megad.kotlin.sensor.wall.mount.Wallmount
import io.kotest.matchers.shouldBe
import io.ktor.client.HttpClient
import io.ktor.client.engine.java.Java
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class MegaDClientTest {
    private val host = System.getenv("host")
    private val password = System.getenv("password")

    private val client = MegaDClient(
        httpClient = HttpClient(Java),
        host = host,
        password = password
    )

    @Test
    fun `should update relay status`(): Unit = runBlocking {
        client.executeCommand(Relay(7), RelayCommand.OFF) shouldBe RelayStatus.OFF
        client.getRelayStatus(Relay(7)) shouldBe RelayStatus.OFF

        client.executeCommand(Relay(7), RelayCommand.ON) shouldBe RelayStatus.ON
        client.getRelayStatus(Relay(7)) shouldBe RelayStatus.ON

        client.executeCommand(Relay(7), RelayCommand.OFF) shouldBe RelayStatus.OFF
        client.getRelayStatus(Relay(7)) shouldBe RelayStatus.OFF

        client.executeCommand(Relay(7), RelayCommand.SWITCH) shouldBe RelayStatus.ON
        client.getRelayStatus(Relay(7)) shouldBe RelayStatus.ON
    }

    @Test
    fun `should use led`(): Unit = runBlocking {
        for (i in 0..255 step 20) {
            client.setPwmLevel(Pwm(12), PwmLevel(i)) shouldBe PwmLevel(i)
        }
    }

    @Test
    fun `can use wallmount`(): Unit = runBlocking {
        val wallmount = Wallmount(
            sdaPortNumber = 34,
            slcPortNumber = 35
        )
        client.getTemperature(wallmount)
        client.getHumidity(wallmount)
        client.getCO2Level(wallmount)
        client.getLightLevel(wallmount)
    }

    @Test
    fun `can use 16RXT`(): Unit = runBlocking {
        client.configureAs16RXT(42,43)
        val megaD16RXT = MegaD16RXT(sdaPortNumber = 42)
        val relay = Relay(12)
        client.executeCommand(megaD16RXT,relay,RelayCommand.OFF)
        client.getRelayStatus(megaD16RXT, relay) shouldBe RelayStatus.OFF
        client.executeCommand(megaD16RXT,relay,RelayCommand.ON)
        client.getRelayStatus(megaD16RXT, relay) shouldBe RelayStatus.ON
    }

    @Test
    fun `can use 16PWM`(): Unit = runBlocking {
        client.configureAs16PWM(41,43)
        val pwm = MegaD16PWM(41)
        for (i in 0..255 step 1) {
            client.setPwmLevel(pwm,Pwm(0), PwmLevel(i)) shouldBe PwmLevel(i)
            Thread.sleep(50)
        }
    }
}