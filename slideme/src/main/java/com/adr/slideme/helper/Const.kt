package com.adr.slideme.helper

object Const {
    enum class CustomSliderOrientation(val orientation: Int) {
        HORIZONTAL(orientation = 0),
        VERTICAL(orientation = 1)
    }
    enum class CustomSliderType(val type: Int) {
        OVERLAY(type = 0),
        DIFF_COLOR(type = 1)
    }
}