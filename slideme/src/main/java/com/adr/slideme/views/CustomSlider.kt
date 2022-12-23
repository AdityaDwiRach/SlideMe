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
import com.adr.slideme.helper.CustomSliderHelper
import com.adr.slideme.model.Margin
import com.adr.slideme.model.PaintProp
import com.adr.slideme.model.SliderTick

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
    private var orientation = Const.Orientation.HORIZONTAL.orientation
    private var type = Const.Type.OVERLAY.type
    private var valueFrom = 0
    private var valueTo = 100
    private var trackHeight = 10f
    private var secondTrackHeight = 0f
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
                    Const.Orientation.HORIZONTAL.orientation
                )
                type =
                    getInteger(R.styleable.CustomSlider_csType, Const.Type.OVERLAY.type)
                valueFrom = getInteger(R.styleable.CustomSlider_csValueFrom, 0)
                valueTo = getInteger(R.styleable.CustomSlider_csValueTo, 100)
                trackHeight = getDimension(R.styleable.CustomSlider_csTrackHeight, context.resources.getDimension(R.dimen.defaultTrackHeight))
                secondTrackHeight = getDimension(R.styleable.CustomSlider_csSecondTrackHeight, 0f)
                mainTrackColor =
                    getResourceId(R.styleable.CustomSlider_csMainTrackColor, R.color.white)
                secondTrackColor =
                    getResourceId(R.styleable.CustomSlider_csSecondTrackColor, R.color.black)
                thumbRadius = getDimension(R.styleable.CustomSlider_csThumbRadius, context.resources.getDimension(R.dimen.defaultThumbRadius))
                defaultThumbPosition =
                    getInteger(R.styleable.CustomSlider_csDefaultThumbPosition, 0)
                thumbColor = getResourceId(R.styleable.CustomSlider_csThumbColor, R.color.black)
                isTickVisible = getBoolean(R.styleable.CustomSlider_csIsTickVisible, false)
                tickInterval = getInteger(R.styleable.CustomSlider_csTickInterval, 100)
                tickRadius = getDimension(R.styleable.CustomSlider_csTickRadius, context.resources.getDimension(R.dimen.defaultTickRadius))
                tickColor = getResourceId(R.styleable.CustomSlider_csTickColor, R.color.white)
                isTickDescVisible = getBoolean(R.styleable.CustomSlider_csIsTickDescVisible, false)
                tickDescSize = getDimension(R.styleable.CustomSlider_csTickDescSize, context.resources.getDimension(R.dimen.defaultTickDescSize))
                tickDeskColor =
                    getResourceId(R.styleable.CustomSlider_csTickDescColor, R.color.black)
                isTickTooltipVisible =
                    getBoolean(R.styleable.CustomSlider_csIsTickTooltipVisible, false)
                tooltipWidth = getDimension(R.styleable.CustomSlider_csTickTooltipSize, context.resources.getDimension(R.dimen.defaultTooltipWidth))
                tickTooltipColor =
                    getResourceId(R.styleable.CustomSlider_csTickTooltipColor, R.color.white)
                tooltipDescSize = getDimension(R.styleable.CustomSlider_csTickTooltipDescSize, context.resources.getDimension(R.dimen.defaultTooltipDescSize))
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
            PaintProp(color = ContextCompat.getColor(context, mainTrackColor),
            antiAlias = true,
            style = Paint.Style.FILL,
            strokeWidth = trackHeight,
            strokeCap = Paint.Cap.ROUND)
        )

        // Set paint properties for second line
        secondLinePaint = Paint()
        setPaintProp(
            secondLinePaint,
            PaintProp(color = ContextCompat.getColor(context, CustomSliderHelper.getSecondTrackColor(type, mainTrackColor, secondTrackColor)),
            antiAlias = true,
            style = Paint.Style.FILL,
            strokeWidth = CustomSliderHelper.getSecondTrackHeight(trackHeight, secondTrackHeight, isTickVisible, tickRadius, type),
            strokeCap = Paint.Cap.ROUND)
        )

        // Set paint properties for thumb
        thumbPaint = Paint()
        setPaintProp(
            thumbPaint,
            PaintProp(color = ContextCompat.getColor(context, CustomSliderHelper.getThumbColor(type, mainTrackColor, thumbColor)),
            antiAlias = true,
            style = Paint.Style.FILL)
        )

        // Set paint properties for tick
        tickPaint = Paint()
        setPaintProp(
            tickPaint,
            PaintProp(color = ContextCompat.getColor(context, tickColor),
            antiAlias = true,
            style = Paint.Style.FILL)
        )

        // Set paint properties for tick description
        tickDescPaint = Paint()
        setPaintProp(
            tickDescPaint,
            PaintProp(color = ContextCompat.getColor(context, tickDeskColor),
            antiAlias = true,
            typeface = ResourcesCompat.getFont(context, R.font.roboto_regular),
            textSize = tickDescSize)
        )

        // Set paint properties for tooltip
        tickTooltipPaint = Paint()
        setPaintProp(
            tickTooltipPaint,
            PaintProp(color = ContextCompat.getColor(context, tickTooltipColor),
            antiAlias = true,
            style = Paint.Style.FILL_AND_STROKE)
        )

        // Set paint properties for tooltip description
        tickTooltipDescPaint = Paint()
        setPaintProp(
            tickTooltipDescPaint,
            PaintProp(color = ContextCompat.getColor(context, tickTooltipDescColor),
            antiAlias = true,
            typeface = ResourcesCompat.getFont(context, R.font.roboto_regular),
            textSize = tooltipDescSize)
        )

        setTooltipAnimator(300, tooltipWidth, tooltipDescSize)

        thumbRadius = CustomSliderHelper.getThumbSize(thumbRadius, trackHeight)
        defaultSelectedTick = SliderTick((thumbRadius * 2), centerY, 0)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(widthMeasureSpec, CustomSliderHelper.getHeight(heightMeasureSpec))
    }

    override fun onDraw(canvas: Canvas) {
        canvas.save()
        // Draw tick circle
        if (isTickVisible) {
            if (CustomSliderHelper.isTickIntervalValid(valueFrom, valueTo, tickInterval)) {
                drawTick(canvas)
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

        val thumbStart = getThumbPosition()

        // Draw second line
        canvas.drawLine((thumbRadius * 2), centerY, thumbStart, centerY, secondLinePaint)
        // Draw thumb circle
        canvas.drawCircle(thumbStart, 50f, thumbRadius, thumbPaint)

        // Draw tooltip
        if (isTickTooltipVisible) {
            drawTickToolTip(
                canvas,
                tooltipWidth.toInt(),
                CustomSliderHelper.getPosition(thumbStart, 50f),
                selectedTick.value
            )
        }
        canvas.restore()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.actionMasked) {
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                val x = event.x
                setThumbToTick(x)
                tooltipAnimatorSetRev.start()
            }
            MotionEvent.ACTION_MOVE -> {
                val x = event.x
                thumbCoordinateX = x
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
        paintProp: PaintProp
    ) {
        with(paintProp) {
            color?.let {
                paint.color = it
            }
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
    }

    private fun getThumbPosition(): Float {
        // Set x coordinate for thumb, and second line
        var thumbStart: Float = if (isFirstDraw) {
            isFirstDraw = false
            val sliderTick: SliderTick =
                if (CustomSliderHelper.isDefaultThumbPositionValid(listTickCoordinates, defaultThumbPosition) && listTickCoordinates.isNotEmpty()) {
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

        return thumbStart
    }

    private fun setTickCoordinate() {
        this.listTickCoordinates = CustomSliderHelper.getTickCoordinate(valueFrom, valueTo, tickInterval, measuredWidth, thumbRadius, centerY)
    }

    private fun setThumbToTick(touchX: Float) {
        val currentTickCoor = CustomSliderHelper.getCurrentTickCoor(listTickCoordinates, touchX)

        thumbCoordinateX = currentTickCoor.coordinateX
        sliderValueListener?.invoke(currentTickCoor.value)

        selectedTick = currentTickCoor
    }

    private fun drawTick(canvas: Canvas) {
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
                    CustomSliderHelper.getPosition(coordinate.coordinateX, coordinate.coordinateY),
                    CustomSliderHelper.getMargin(top = (thumbRadius * 2))
                )
            }
        }
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

    private fun drawTriangle(positionX: Float, positionY: Float, width: Int): Path {
        val pathTriangle = Path()
        pathTriangle.moveTo(positionX, positionY) // Initial bottom point
        pathTriangle.lineTo(
            (positionX + (width / 2)),
            (positionY - width)) // To top right point
        pathTriangle.lineTo(
            (positionX - (width / 2)),
            (positionY - width))  // To top left point
        pathTriangle.lineTo(positionX, positionY) // Back to initial bottom point

        return pathTriangle
    }

    private fun drawTickToolTip(
        canvas: Canvas,
        widthToolTip: Int,
        position: Pair<Float, Float>,
        value: Int
    ) {
        val halfSideWidth = widthToolTip / 2
        val pathTooltip = Path()
        val (positionX, positionY) = position

        val pathTriangle = drawTriangle(positionX, positionY, widthToolTip)
        val pathCircle = Path()
        pathCircle.addCircle(
            positionX,
            (positionY - widthToolTip), halfSideWidth.toFloat(), Path.Direction.CW
        )
        pathTooltip.op(pathTriangle, pathCircle, Path.Op.UNION)
        pathCircle.close()
        pathTriangle.close()
        pathTooltip.close()
        canvas.drawPath(pathTooltip, tickTooltipPaint)
        val centerTextY = positionY - widthToolTip

        tickTooltipDescPaint.textSize = tooltipDescSize
        drawText(canvas, "$value", tickTooltipDescPaint, CustomSliderHelper.getPosition(positionX, centerTextY))
    }

    fun resetThumbPosition() {
        isFirstDraw = true
        if (isTickVisible && CustomSliderHelper.isDefaultThumbPositionValid(listTickCoordinates, defaultThumbPosition) && listTickCoordinates.isNotEmpty()) {
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
}