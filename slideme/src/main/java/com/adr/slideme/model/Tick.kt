package com.adr.slideme.model

data class Tick(
    val isTickVisible: Boolean,
    val tickInterval: Int,
    val tickRadius: Float,
    val tickColor: Int,
    val isTickDescVisible: Boolean,
    val tickDescSize: Float,
    val tickDescColor: Int,
    var tickDescPosition: Int
)
