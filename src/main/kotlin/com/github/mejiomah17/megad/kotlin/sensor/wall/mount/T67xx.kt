package com.github.mejiomah17.megad.kotlin.sensor.wall.mount

/**
 * CO2 censor
 */
object T67xx : WallMountSensor {
    val maxValue = 655535
    override val i2cDev: String = "t67xx"
    override val i2cPair: Int = 0
}