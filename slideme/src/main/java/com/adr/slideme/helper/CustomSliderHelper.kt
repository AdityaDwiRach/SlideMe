package com.adr.slideme.helper

import android.view.View
import com.adr.slideme.model.Coordinate
import com.adr.slideme.model.SliderTick
import kotlin.math.min

object CustomSliderHelper {

    private fun validateSecondTrackHeight(
        secondTrackHeight: Float,
        isTickVisible: Boolean,
        tickRadius: Float
    ): Float {
        return if (isTickVisible && secondTrackHeight <= (tickRadius * 2)) {
            (tickRadius * 2.1f)
        } else {
            secondTrackHeight
        }
    }

    fun getSecondTrackHeight(
        trackHeight: Float,
        secondTrackHeight: Float,
        isTickVisible: Boolean,
        tickRadius: Float,
        type: Int
    ): Float {
        val tempHeight: Float = if (type == Const.Type.OVERLAY.type && secondTrackHeight != 0f) {
            secondTrackHeight
        } else {
            (trackHeight + 5)
        }

        return validateSecondTrackHeight(tempHeight, isTickVisible, tickRadius)
    }

    fun getSecondTrackColor(type: Int, mainTrackColor: Int, secondTrackColor: Int): Int {
        return if (type == Const.Type.OVERLAY.type) mainTrackColor else secondTrackColor
    }

    fun getThumbColor(type: Int, mainTrackColor: Int, thumbColor: Int): Int {
        return if (type == Const.Type.OVERLAY.type) mainTrackColor else thumbColor
    }

    fun getThumbSize(thumbRadius: Float, mainTrackHeight: Float): Float {
        // Thumb size should not be smaller than main track height
        return if ((thumbRadius * 2) > mainTrackHeight) {
            thumbRadius
        } else {
            (mainTrackHeight * 1.5f)
        }
    }

    fun getTickCoordinate(
        valueFrom: Int,
        valueTo: Int,
        tickInterval: Int,
        measuredWidth: Int,
        measuredHeight: Int,
        thumbRadius: Float,
        orientation: Int
    ): ArrayList<SliderTick> {
        val listCoordinate = arrayListOf<SliderTick>()
        val amount = ((valueTo - valueFrom) / tickInterval)
        val intervalSpace =
            if (orientation == Const.Orientation.VERTICAL.orientation) (measuredHeight.toFloat() - (thumbRadius * 4)) / amount else (measuredWidth.toFloat() - (thumbRadius * 4)) / amount
        var coordinateX =
            if (orientation == Const.Orientation.VERTICAL.orientation) (measuredWidth.toFloat() / 2) else thumbRadius * 2
        var coordinateY =
            if (orientation == Const.Orientation.VERTICAL.orientation) (measuredHeight.toFloat() - (thumbRadius * 2)) else (measuredHeight.toFloat() / 2)
        for ((index, value) in (valueFrom..valueTo step tickInterval).withIndex()) {
            if (orientation == Const.Orientation.VERTICAL.orientation) {
                if (index != 0) {
                    coordinateY -= intervalSpace
                }
            } else {
                if (index != 0) {
                    coordinateX += intervalSpace
                }
            }
            listCoordinate.add(SliderTick(coordinateX, coordinateY, value))
        }
        return listCoordinate
    }

    private fun getNearestTickX(listTickCoordinates: List<SliderTick>, touchX: Float): SliderTick {
        var currentTickCoor: SliderTick
        var currentMinRange: Float
        var leftIndex = 0
        var rightIndex = listTickCoordinates.size - 1
        while (true) {
            val leftMinRange: Float = if (listTickCoordinates[leftIndex].coordinateX < touchX) {
                touchX - listTickCoordinates[leftIndex].coordinateX
            } else {
                listTickCoordinates[leftIndex].coordinateX - touchX
            }

            val rightMinRange: Float =
                if (listTickCoordinates[rightIndex].coordinateX > touchX) {
                    listTickCoordinates[rightIndex].coordinateX - touchX
                } else {
                    touchX - listTickCoordinates[rightIndex].coordinateX
                }

            currentMinRange = minOf(leftMinRange, rightMinRange)
            currentTickCoor = if (currentMinRange == leftMinRange) {
                rightIndex--
                listTickCoordinates[leftIndex]
            } else {
                leftIndex++
                listTickCoordinates[rightIndex]
            }

            if (leftIndex == rightIndex) {
                break
            }
        }

        return currentTickCoor
    }

    private fun getNearestTickY(listTickCoordinates: List<SliderTick>, touchY: Float): SliderTick {
        var currentTickCoor: SliderTick
        var currentMinRange: Float
        var bottomIndex = 0
        var topIndex = listTickCoordinates.size - 1
        while (true) {
            val bottomMinRange: Float = if (listTickCoordinates[bottomIndex].coordinateY > touchY) {
                listTickCoordinates[bottomIndex].coordinateY - touchY
            } else {
                touchY - listTickCoordinates[bottomIndex].coordinateY
            }

            val topMinRange: Float =
                if (listTickCoordinates[topIndex].coordinateY < touchY) {
                    touchY - listTickCoordinates[topIndex].coordinateY
                } else {
                    listTickCoordinates[topIndex].coordinateY - touchY
                }

            currentMinRange = minOf(bottomMinRange, topMinRange)
            currentTickCoor = if (currentMinRange == bottomMinRange) {
                topIndex--
                listTickCoordinates[bottomIndex]
            } else {
                bottomIndex++
                listTickCoordinates[topIndex]
            }

            if (bottomIndex == topIndex) {
                break
            }
        }

        return currentTickCoor
    }

    fun getCurrentTickCoor(
        orientation: Int,
        listTickCoordinates: List<SliderTick>,
        touchX: Float,
        touchY: Float
    ): SliderTick {
        val findCoor =
            if (orientation == Const.Orientation.VERTICAL.orientation) listTickCoordinates.find { it.coordinateY == touchY } else listTickCoordinates.find { it.coordinateX == touchX }
        findCoor?.let {
            return it
        } ?: run {
            return if (orientation == Const.Orientation.VERTICAL.orientation) getNearestTickY(
                listTickCoordinates,
                touchY
            ) else getNearestTickX(listTickCoordinates, touchX)
        }
    }

    fun getMeasureSpec(orientation: Int, measureSpec: Int): Int {
        val default = if (orientation == Const.Orientation.VERTICAL.orientation) 210 else 100
        val mode = View.MeasureSpec.getMode(measureSpec)
        val size = View.MeasureSpec.getSize(measureSpec)

        // Measure
        val finalSize: Int = when (mode) {
            View.MeasureSpec.EXACTLY -> {
                // Must be this size
                size
            }
            View.MeasureSpec.AT_MOST -> {
                // Can't be bigger than
                min(default, size)
            }
            else -> {
                // Up to you
                default
            }
        }
        return finalSize
    }

    fun isDefaultThumbPositionValid(
        listTickCoordinates: List<SliderTick>,
        defaultThumbPosition: Int
    ): Boolean {
        return (defaultThumbPosition <= listTickCoordinates.size - 1) && (defaultThumbPosition > 0)
    }

    fun isTickIntervalValid(valueFrom: Int, valueTo: Int, tickInterval: Int): Boolean {
        val rangeValue = valueTo - valueFrom
        return (rangeValue % tickInterval) == 0
    }

    fun validateTickTooltipPosition(tickPosition: Int, tooltipPosition: Int): Boolean {
        return when (tickPosition) {
            Const.Position.LEFT.position -> {
                tooltipPosition == Const.Position.RIGHT.position
            }
            Const.Position.TOP.position -> {
                tooltipPosition == Const.Position.BOTTOM.position
            }
            Const.Position.RIGHT.position -> {
                tooltipPosition == Const.Position.LEFT.position
            }
            Const.Position.BOTTOM.position -> {
                tooltipPosition == Const.Position.TOP.position
            }
            else -> false
        }
    }

    fun getThumbCoorOnTrack(orientation: Int, measuredWidth: Int, measuredHeight: Int, currentThumbCoor: Float, thumbRadius: Float): Float {
        // Thumb, second line, and tooltip stay on the base line
        var thumbStart = currentThumbCoor
        if (orientation == Const.Orientation.VERTICAL.orientation) {
            if (thumbStart >= measuredHeight.toFloat() - (thumbRadius * 2)) {
                thumbStart = measuredHeight.toFloat() - (thumbRadius * 2)
            } else if (thumbStart <= thumbRadius * 2) {
                thumbStart = (thumbRadius * 2)
            }
        } else {
            if (thumbStart <= (thumbRadius * 2)) {
                thumbStart = thumbRadius * 2
            } else if (thumbStart >= (measuredWidth.toFloat() - (thumbRadius * 2))) {
                thumbStart = measuredWidth.toFloat() - (thumbRadius * 2)
            }
        }
        return thumbStart
    }

    fun getTriangleCoor(tickTooltipPosition: Int, positionX: Float, positionY: Float, width: Int): Pair<Coordinate, Coordinate> {
        val first = Coordinate()
        val second = Coordinate()
        when (tickTooltipPosition) {
            Const.Position.LEFT.position -> {
                first.apply {
                    x = positionX - width
                    y = positionY - (width / 2)
                }
                second.apply {
                    x = positionX - width
                    y = positionY + (width / 2)
                }
            }
            Const.Position.TOP.position -> {
                first.apply {
                    x = positionX + (width / 2)
                    y = positionY - width
                }
                second.apply {
                    x = positionX - (width / 2)
                    y = positionY - width
                }
            }
            Const.Position.RIGHT.position -> {
                first.apply {
                    x = positionX + width
                    y = positionY + (width / 2)
                }
                second.apply {
                    x = positionX + width
                    y = positionY - (width / 2)
                }
            }
            Const.Position.BOTTOM.position -> {
                first.apply {
                    x = positionX - (width / 2)
                    y = positionY + width
                }
                second.apply {
                    x = positionX + (width / 2)
                    y = positionY + width
                }
            }
        }

        return Pair(first, second)
    }

    fun getCircleCoor(tickTooltipPosition: Int, positionX: Float, positionY: Float, widthToolTip: Int): Coordinate {
        val coor = Coordinate()
        when (tickTooltipPosition) {
            Const.Position.LEFT.position -> {
                coor.apply {
                    x = positionX - widthToolTip
                    y = positionY
                }
            }
            Const.Position.TOP.position -> {
                coor.apply {
                    x = positionX
                    y = positionY - widthToolTip
                }
            }
            Const.Position.RIGHT.position -> {
                coor.apply {
                    x = positionX + widthToolTip
                    y = positionY
                }
            }
            Const.Position.BOTTOM.position -> {
                coor.apply {
                    x = positionX
                    y = positionY + widthToolTip
                }
            }
        }
        return coor
    }
}