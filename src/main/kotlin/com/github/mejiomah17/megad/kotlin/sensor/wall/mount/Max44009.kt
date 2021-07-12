package com.github.mejiomah17.megad.kotlin.sensor.wall.mount

/**
 * Light sensor
 */
object Max44009 : WallMountSensor {
    override val i2cDev: String = "max44009"
    override val i2cPair: Int = 0
}