package com.jpvj.controlcarrito.domain.model
enum class MoveDirection(val apiValue: String) {
    FORWARD("forward"),
    BACKWARD("backward"),
    LEFT("left"),
    RIGHT("right"),
    STOP("stop"),
    LIGHTS("lights")   // luego vemos cómo usas esto en el ESP32
}
