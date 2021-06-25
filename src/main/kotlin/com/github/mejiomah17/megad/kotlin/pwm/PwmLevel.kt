package com.github.mejiomah17.megad.kotlin.pwm

data class PwmLevel(val value: Int) {
    init {
        require(value in 0..255)
    }
}