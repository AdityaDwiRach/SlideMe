package com.adr.slideme.model

data class Style(
    val orientation: Int,
    val type: Int,
    val valueFrom: Int,
    val valueTo: Int,
    val track: Track,
    val thumb: Thumb,
    val tick: Tick,
    val tooltip: Tooltip
)
