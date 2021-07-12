package com.github.mejiomah17.megad.kotlin.sensor.wall.mount

interface WallMountSensor {
    val i2cDev: String
    val i2cPair: Int
}