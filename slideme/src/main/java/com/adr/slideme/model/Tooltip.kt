package com.adr.slideme.model

data class Tooltip(
    val isTickTooltipVisible: Boolean,
    var tooltipWidth: Float,
    val tickTooltipColor: Int,
    var tickTooltipPosition: Int,
    var tooltipDescSize: Float,
    val tickTooltipDescColor: Int
)
