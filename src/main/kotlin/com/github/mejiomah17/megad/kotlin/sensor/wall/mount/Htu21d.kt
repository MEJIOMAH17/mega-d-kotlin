package com.github.mejiomah17.megad.kotlin.sensor.wall.mount

/**
 * temperature and humidity sensor
 */
abstract class Htu21d : WallMountSensor {
    override val i2cDev: String = "htu21d"
    val humidityI2cParNumber = 2

    object Temperature : Htu21d() {
        override val i2cPair: Int = 1
    }

    object Humidity : Htu21d() {
        override val i2cPair: Int = 0
    }
}