package com.github.mejiomah17.megad.kotlin

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
}