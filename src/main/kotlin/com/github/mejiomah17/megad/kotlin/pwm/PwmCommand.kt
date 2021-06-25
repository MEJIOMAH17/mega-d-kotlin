package com.github.mejiomah17.megad.kotlin.pwm

sealed class PwmCommand {
    class SetLevel(val level: PwmLevel) : PwmCommand()
}