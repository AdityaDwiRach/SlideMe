package com.adr.slideme.helper

import android.view.View
import com.adr.slideme.model.Margin
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
        thumbRadius: Float,
        coordinateY: Float
    ): ArrayList<SliderTick> {
        val listCoordinate = arrayListOf<SliderTick>()
        val amount = ((valueTo - valueFrom) / tickInterval)
        val intervalSpace = (measuredWidth.toFloat() - (thumbRadius * 4)) / amount
        var coordinateX = 0f
        for ((index, value) in (valueFrom..valueTo step tickInterval).withIndex()) {
            if (index == 0) {
                coordinateX = thumbRadius * 2
            } else {
                coordinateX += intervalSpace
            }
            listCoordinate.add(SliderTick(coordinateX, coordinateY, value))
        }
        return listCoordinate
    }

    private fun getNearestTick(listTickCoordinates: List<SliderTick>, touchX: Float): SliderTick {
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

    fun getCurrentTickCoor(listTickCoordinates: List<SliderTick>, touchX: Float): SliderTick {
        val findCoor = listTickCoordinates.find { it.coordinateX == touchX }
        findCoor?.let {
            return it
        } ?: run {
            return getNearestTick(listTickCoordinates, touchX)
        }
    }

    fun getPosition(x: Float, y: Float): Pair<Float, Float> {
        return Pair(x, y)
    }

    fun getMargin(
        start: Float? = null,
        top: Float? = null,
        end: Float? = null,
        bottom: Float? = null
    ): Margin {
        return Margin(start, top, end, bottom)
    }

    fun getHeight(heightMeasureSpec: Int): Int {
        val defaultHeight = 100
        val heightMode = View.MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = View.MeasureSpec.getSize(heightMeasureSpec)

        // Measure height
        val height: Int = when (heightMode) {
            View.MeasureSpec.EXACTLY -> {
                // Must be this size
                heightSize
            }
            View.MeasureSpec.AT_MOST -> {
                // Can't be bigger than
                min(defaultHeight, heightSize)
            }
            else -> {
                // Up to you
                defaultHeight
            }
        }
        return height
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
}