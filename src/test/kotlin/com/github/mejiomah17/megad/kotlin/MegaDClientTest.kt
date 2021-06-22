package com.github.mejiomah17.megad.kotlin

import com.github.mejiomah17.megad.kotlin.relay.Relay
import com.github.mejiomah17.megad.kotlin.relay.RelayCommand
import com.github.mejiomah17.megad.kotlin.relay.RelayStatus
import io.kotest.matchers.shouldBe
import io.ktor.client.*
import io.ktor.client.engine.java.*
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
    fun `should update relay status`():Unit = runBlocking{
        client.executeCommand(Relay(7), RelayCommand.OFF) shouldBe RelayStatus.OFF
        client.getRelayStatus(Relay(7))  shouldBe RelayStatus.OFF

        client.executeCommand(Relay(7), RelayCommand.ON) shouldBe RelayStatus.ON
        client.getRelayStatus(Relay(7))  shouldBe RelayStatus.ON

        client.executeCommand(Relay(7), RelayCommand.OFF) shouldBe RelayStatus.OFF
        client.getRelayStatus(Relay(7))  shouldBe RelayStatus.OFF

        client.executeCommand(Relay(7), RelayCommand.SWITCH) shouldBe RelayStatus.ON
        client.getRelayStatus(Relay(7))  shouldBe RelayStatus.ON
    }
}