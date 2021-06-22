package com.github.mejiomah17.megad.kotlin.relay

enum class RelayCommand(val intValue:Int) {
    OFF(0),
    ON(1),

    /**
     * If relay in [RelayStatus.ON] switch relay to [RelayStatus.OFF]
     * If relay in [RelayStatus.OFF] switch relay to [RelayStatus.ON]
     */
    SWITCH(2)

}