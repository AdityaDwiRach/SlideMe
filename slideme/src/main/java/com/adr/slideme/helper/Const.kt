package com.adr.slideme.helper

object Const {
    enum class Orientation(val orientation: Int) {
        HORIZONTAL(orientation = 0),
        VERTICAL(orientation = 1)
    }
    enum class Type(val type: Int) {
        // OVERLAY type means fixed second line and thumb color, custom second line height
        OVERLAY(type = 0),
        // DIFF_COLOR type means fixed second line height, custom second line and thumb color
        DIFF_COLOR(type = 1)
    }
    enum class TooltipPosition(val position: Int) {
        LEFT(position = 0),
        TOP(position = 1),
        RIGHT(position = 2),
        BOTTOM(position = 3)
    }
}