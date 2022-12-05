package com.adr.slideme.views

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.adr.slideme.R
import com.adr.slideme.helper.Const
import com.adr.slideme.model.Margin
import com.adr.slideme.model.SliderTick
import kotlin.math.min

/**
 * This custom slider was made because author can't use Slider from material.io. Also didn't find
 * third party library that fulfill author needs.
 *
 * @author Aditya Dwi R
 */
class CustomSlider : View {
    private lateinit var baseLinePaint: Paint
    private lateinit var secondLinePaint: Paint
    private lateinit var thumbPaint: Paint
    private lateinit var tickPaint: Paint
    private lateinit var tickDescPaint: Paint
    private lateinit var tickTooltipPaint: Paint
    private lateinit var tickTooltipDescPaint: Paint
    private lateinit var boundRect: Rect
    private lateinit var tooltipAnimatorSet: AnimatorSet
    private lateinit var tooltipAnimatorSetRev: AnimatorSet
    private lateinit var defaultSelectedTick: SliderTick
    private var orientation = Const.CustomSliderOrientation.HORIZONTAL.orientation
    private var type = Const.CustomSliderType.OVERLAY.type
    private var valueFrom = 0
    private var valueTo = 100
    private var trackHeight = 10f
    private var mainTrackColor = R.color.white
    private var secondTrackColor = R.color.black
    private var thumbRadius = 15f
    private var defaultThumbPosition = 0
    private var thumbColor = R.color.black
    private var isTickVisible = false
    private var tickInterval = 100
    private var tickRadius = 7.5f
    private var tickColor = R.color.white
    private var isTickDescVisible = false
    private var tickDescSize = 18f
    private var tickDeskColor = R.color.black
    private var isTickTooltipVisible = false
    private val centerY = 50f
    private var thumbCoordinateX = 0f
    private var listTickCoordinates = listOf<SliderTick>()
    private var selectedTick = SliderTick(0f, 0f, 0)
    private var isFirstDraw = true
    private var tooltipWidth = 0f
    private var tickTooltipColor = R.color.white
    private var tooltipDescSize = 0f
    private var tickTooltipDescColor = R.color.black
    private var sliderValueListener: ((value: Int) -> Unit)? = null

    constructor(context: Context) : super(context) {
        initialize(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initialize(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initialize(context, attrs)
    }

    private fun initialize(context: Context, attrs: AttributeSet?) {
        context.theme.obtainStyledAttributes(attrs, R.styleable.CustomSlider, 0, 0).apply {
            try {
                orientation = getInteger(
                    R.styleable.CustomSlider_csOrientation,
                    Const.CustomSliderOrientation.HORIZONTAL.orientation
                )
                type =
                    getInteger(R.styleable.CustomSlider_csType, Const.CustomSliderType.OVERLAY.type)
                valueFrom = getInteger(R.styleable.CustomSlider_csValueFrom, 0)
                valueTo = getInteger(R.styleable.CustomSlider_csValueTo, 100)
                trackHeight = getDimension(R.styleable.CustomSlider_csTrackHeight, 10f)
                mainTrackColor =
                    getResourceId(R.styleable.CustomSlider_csMainTrackColor, R.color.white)
                secondTrackColor =
                    getResourceId(R.styleable.CustomSlider_csSecondTrackColor, R.color.black)
                thumbRadius = getDimension(R.styleable.CustomSlider_csThumbRadius, 15f)
                defaultThumbPosition =
                    getInteger(R.styleable.CustomSlider_csDefaultThumbPosition, 0)
                thumbColor = getResourceId(R.styleable.CustomSlider_csThumbColor, R.color.black)
                isTickVisible = getBoolean(R.styleable.CustomSlider_csIsTickVisible, false)
                tickInterval = getInteger(R.styleable.CustomSlider_csTickInterval, 100)
                tickRadius = getDimension(R.styleable.CustomSlider_csTickRadius, 7f)
                tickColor = getResourceId(R.styleable.CustomSlider_csTickColor, R.color.white)
                isTickDescVisible = getBoolean(R.styleable.CustomSlider_csIsTickDescVisible, false)
                tickDescSize = getDimension(R.styleable.CustomSlider_csTickDescSize, 18f)
                tickDeskColor =
                    getResourceId(R.styleable.CustomSlider_csTickDescColor, R.color.black)
                isTickTooltipVisible =
                    getBoolean(R.styleable.CustomSlider_csIsTickTooltipVisible, false)
                tooltipWidth = getDimension(R.styleable.CustomSlider_csTickTooltipSize, 30f)
                tickTooltipColor =
                    getResourceId(R.styleable.CustomSlider_csTickTooltipColor, R.color.white)
                tooltipDescSize = getDimension(R.styleable.CustomSlider_csTickTooltipDescSize, 18f)
                tickTooltipDescColor =
                    getResourceId(R.styleable.CustomSlider_csTickTooltipDescColor, R.color.black)
            } finally {
                recycle()
            }
        }

        boundRect = Rect()
        // Set paint properties for base line
        baseLinePaint = Paint()
        setPaintProp(
            baseLinePaint,
            ContextCompat.getColor(context, mainTrackColor),
            true,
            style = Paint.Style.FILL,
            strokeWidth = trackHeight,
            strokeCap = Paint.Cap.ROUND
        )

        // Set paint properties for second line
        secondLinePaint = Paint()
        setPaintProp(
            secondLinePaint,
            ContextCompat.getColor(context, secondTrackColor),
            true,
            style = Paint.Style.FILL,
            strokeWidth = trackHeight + 5,
            strokeCap = Paint.Cap.ROUND
        )

        // Set paint properties for thumb
        thumbPaint = Paint()
        setPaintProp(
            thumbPaint,
            ContextCompat.getColor(context, thumbColor),
            true,
            style = Paint.Style.FILL
        )

        // Set paint properties for tick
        tickPaint = Paint()
        setPaintProp(
            tickPaint,
            ContextCompat.getColor(context, tickColor),
            true,
            style = Paint.Style.FILL
        )

        // Set paint properties for tick description
        tickDescPaint = Paint()
        setPaintProp(
            tickDescPaint,
            ContextCompat.getColor(context, tickDeskColor),
            true,
            typeface = ResourcesCompat.getFont(context, R.font.roboto_regular),
            textSize = tickDescSize
        )

        // Set paint properties for tooltip
        tickTooltipPaint = Paint()
        setPaintProp(
            tickTooltipPaint,
            ContextCompat.getColor(context, tickTooltipColor),
            true,
            style = Paint.Style.FILL_AND_STROKE
        )

        // Set paint properties for tooltip description
        tickTooltipDescPaint = Paint()
        setPaintProp(
            tickTooltipDescPaint,
            ContextCompat.getColor(context, tickTooltipDescColor),
            true,
            typeface = ResourcesCompat.getFont(context, R.font.roboto_regular),
            textSize = tooltipDescSize
        )

        setTooltipAnimator(300, tooltipWidth, tooltipDescSize)

        defaultSelectedTick = SliderTick((thumbRadius * 2), centerY, 0)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val defaultHeight = 100
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        var height = 0
        // Measure height
        height = when(heightMode) {
            MeasureSpec.EXACTLY -> {
                // Must be this size
                heightSize
            }
            MeasureSpec.AT_MOST -> {
                // Can't be bigger than
                min(defaultHeight, heightSize)
            }
            else -> {
                // Up to you
                defaultHeight
            }
        }
        setMeasuredDimension(widthMeasureSpec, height)
    }

    override fun onDraw(canvas: Canvas) {
        canvas.save()
        // Draw tick circle
        if (isTickVisible) {
            if (isTickIntervalValid()) {
                setTickCoordinate()
                for (coordinate in listTickCoordinates) {
                    canvas.drawCircle(
                        coordinate.coordinateX,
                        coordinate.coordinateY,
                        tickRadius,
                        tickPaint
                    )
                    if (isTickDescVisible) {
                        val desc = "${coordinate.value} min"
                        drawText(
                            canvas,
                            desc,
                            tickDescPaint,
                            getPosition(coordinate.coordinateX, coordinate.coordinateY),
                            getMargin(top = (thumbRadius * 2))
                        )
                    }
                }
            } else {
                throw java.lang.Exception("Tick interval value not valid.")
            }
        }

        // Draw base line
        canvas.drawLine(
            (thumbRadius * 2),
            centerY,
            (measuredWidth.toFloat() - (thumbRadius * 2)),
            centerY,
            baseLinePaint
        )

        // Set x coordinate for thumb, and second line
        var thumbStart: Float = if (isFirstDraw) {
            isFirstDraw = false
            val sliderTick: SliderTick =
                if (isDefaultThumbPositionValid() && listTickCoordinates.isNotEmpty()) {
                    listTickCoordinates[defaultThumbPosition - 1]
                } else {
                    defaultSelectedTick
                }
            sliderValueListener?.invoke(sliderTick.value)
            sliderTick.coordinateX
        } else {
            thumbCoordinateX
        }

        // Thumb, second line, and tooltip stay on the base line
        if (thumbStart <= (thumbRadius * 2)) {
            thumbStart = thumbRadius * 2
        } else if (thumbStart >= (measuredWidth.toFloat() - (thumbRadius * 2))) {
            thumbStart = (measuredWidth.toFloat() - (thumbRadius * 2))
        }

        // Draw second line
        canvas.drawLine((thumbRadius * 2), centerY, thumbStart, centerY, secondLinePaint)
        // Draw thumb circle
        canvas.drawCircle(thumbStart, 50f, thumbRadius, thumbPaint)

        // Draw tooltip
        if (isTickTooltipVisible) {
            drawTickToolTip(
                canvas,
                tooltipWidth.toInt(),
                getPosition(thumbStart, 50f),
                selectedTick.value
            )
        }
        canvas.restore()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.actionMasked) {
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                val x = event.x
                setThumbToTick(x, event.actionMasked)
                tooltipAnimatorSetRev.start()
            }
            MotionEvent.ACTION_MOVE -> {
                val x = event.x
                setThumbToTick(x, event.actionMasked)
                invalidate()
            }
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                val x = event.x
                thumbCoordinateX = x
                tooltipAnimatorSet.start()
            }
        }
        return true
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if (visibility == GONE) {
            resetThumbPosition()
        }
    }

    private fun setTooltipAnimator(duration: Long, tooltipWidth: Float, tooltipDescSize: Float) {
        tooltipAnimatorSet = AnimatorSet()
        tooltipAnimatorSetRev = AnimatorSet()

        // Set animator, property and listener for tooltip object.
        val tooltipAnimator = ValueAnimator.ofFloat(0f, tooltipWidth).apply {
            this.duration = duration
            this.addUpdateListener {
                this@CustomSlider.tooltipWidth = it.animatedValue as Float
                invalidate()
            }
        }

        // Set animator, property and listener for tooltip object.
        val tooltipAnimatorReverse = ValueAnimator.ofFloat(tooltipWidth, 0f).apply {
            this.duration = duration
            this.addUpdateListener {
                this@CustomSlider.tooltipWidth = it.animatedValue as Float
                invalidate()
            }
        }

        // Set animator, property and listener for tooltip description object.
        val tooltipDescAnimator = ValueAnimator.ofFloat(0f, tooltipDescSize).apply {
            this.duration = duration
            this.addUpdateListener {
                this@CustomSlider.tooltipDescSize = it.animatedValue as Float
                invalidate()
            }
        }

        // Set animator, property and listener for tooltip description object.
        val tooltipDescAnimatorReverse = ValueAnimator.ofFloat(tooltipDescSize, 0f).apply {
            this.duration = duration
            this.addUpdateListener {
                this@CustomSlider.tooltipDescSize = it.animatedValue as Float
                invalidate()
            }
        }

        tooltipAnimatorSet.playTogether(tooltipAnimator, tooltipDescAnimator)
        tooltipAnimatorSetRev.playTogether(tooltipAnimatorReverse, tooltipDescAnimatorReverse)
        this.tooltipWidth = 0f
        this.tooltipDescSize = 0f
    }

    private fun setPaintProp(
        paint: Paint,
        color: Int,
        antiAlias: Boolean,
        style: Paint.Style? = null,
        strokeWidth: Float? = null,
        strokeCap: Paint.Cap? = null,
        typeface: Typeface? = null,
        textSize: Float? = null
    ) {
        paint.color = color
        paint.isAntiAlias = antiAlias
        style?.let {
            paint.style = it
        }
        strokeWidth?.let {
            paint.strokeWidth = it
        }
        strokeCap?.let {
            paint.strokeCap = it
        }
        typeface?.let {
            paint.typeface = it
        }
        textSize?.let {
            paint.textSize = it
        }
    }

    private fun setTickCoordinate() {
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
            listCoordinate.add(SliderTick(coordinateX, centerY, value))
        }
        this.listTickCoordinates = listCoordinate
    }

    private fun isTickIntervalValid(): Boolean {
        val rangeValue = valueTo - valueFrom
        return (rangeValue % tickInterval) == 0
    }

    private fun isDefaultThumbPositionValid(): Boolean {
        return (defaultThumbPosition <= listTickCoordinates.size - 1) && (defaultThumbPosition > 0)
    }

    private fun setThumbToTick(touchX: Float, actionType: Int) {
        var currentTickCoor = SliderTick(0f, 0f, 0)
        var currentMinRange: Float
        var leftIndex = 0
        var rightIndex = listTickCoordinates.size - 1
        val findCoor = listTickCoordinates.find { it.coordinateX == touchX }
        findCoor?.let {
            currentTickCoor = it
            when (actionType) {
                MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                    thumbCoordinateX = it.coordinateX
                    sliderValueListener?.invoke(it.value)
                }
                MotionEvent.ACTION_MOVE -> thumbCoordinateX = touchX
                else -> Unit
            }

        } ?: run {
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

            when (actionType) {
                MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                    thumbCoordinateX = currentTickCoor.coordinateX
                    sliderValueListener?.invoke(currentTickCoor.value)
                }
                MotionEvent.ACTION_MOVE -> thumbCoordinateX = touchX
                else -> Unit
            }
        }

        selectedTick = currentTickCoor
    }

    private fun drawText(
        canvas: Canvas,
        text: String,
        paint: Paint,
        position: Pair<Float, Float>,
        margin: Margin? = null
    ) {
        val (positionX, positionY) = position
        paint.getTextBounds(text, 0, text.length, boundRect)
        val y = when {
            margin?.top != null -> {
                (positionY + (boundRect.height() / 2)) + margin.top
            }
            else -> (positionY + (boundRect.height() / 2))
        }
        canvas.drawText(
            text,
            (positionX - (boundRect.width() / 2)),
            y,
            paint
        )
    }

    private fun drawTickToolTip(
        canvas: Canvas,
        widthToolTip: Int,
        position: Pair<Float, Float>,
        value: Int
    ) {
        val halfSideWidth = widthToolTip / 2
        val path = Path()
        val (positionX, positionY) = position

        val pathTriangle = Path()
        pathTriangle.moveTo(positionX, positionY) // Initial bottom point
        pathTriangle.lineTo(
            (positionX + halfSideWidth),
            (positionY - widthToolTip)
        ) // To top right point
        pathTriangle.lineTo(
            (positionX - halfSideWidth),
            (positionY - widthToolTip)
        )  // To top left point
        pathTriangle.lineTo(positionX, positionY) // Back to initial bottom point
        val pathCircle = Path()
        pathCircle.addCircle(
            positionX,
            (positionY - widthToolTip), halfSideWidth.toFloat(), Path.Direction.CW
        )
        path.op(pathTriangle, pathCircle, Path.Op.UNION)
        pathCircle.close()
        pathTriangle.close()
        path.close()
        canvas.drawPath(path, tickTooltipPaint)
        val centerTextY = positionY - widthToolTip

        tickTooltipDescPaint.textSize = tooltipDescSize
        drawText(canvas, "$value", tickTooltipDescPaint, getPosition(positionX, centerTextY))
    }

    fun resetThumbPosition() {
        isFirstDraw = true
        if (isTickVisible && isDefaultThumbPositionValid() && listTickCoordinates.isNotEmpty()) {
            selectedTick = listTickCoordinates[defaultThumbPosition - 1]
        }
        invalidate()
    }

    fun setThumbPosition(value: Int) {
        val selectedTick = listTickCoordinates.find { it.value == value }
        thumbCoordinateX = selectedTick?.coordinateX ?: thumbCoordinateX
        invalidate()
    }

    fun setValueChangeListener(listener: (value: Int) -> Unit) {
        sliderValueListener = listener
    }

    private fun getPosition(x: Float, y: Float): Pair<Float, Float> {
        return Pair(x, y)
    }

    private fun getMargin(
        start: Float? = null,
        top: Float? = null,
        end: Float? = null,
        bottom: Float? = null
    ): Margin {
        return Margin(start, top, end, bottom)
    }
}