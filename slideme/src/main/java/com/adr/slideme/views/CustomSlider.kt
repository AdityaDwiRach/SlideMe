package com.adr.slideme.views

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.adr.slideme.R
import com.adr.slideme.helper.Const
import com.adr.slideme.helper.CustomSliderHelper
import com.adr.slideme.model.*

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
    private val centerY = 50f
    private var thumbCoordinateX = 0f
    private var thumbCoordinateY = 0f
    private var listTickCoordinates = listOf<SliderTick>()
    private var selectedTick = SliderTick(0f, 0f, 0)
    private var isFirstDraw = true
    private var sliderValueListener: ((value: Int) -> Unit)? = null
    private lateinit var style: Style

    constructor(context: Context) : super(context) {
        initialize(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initialize(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context, attrs, defStyleAttr
    ) {
        initialize(context, attrs)
    }

    private fun initialize(context: Context, attrs: AttributeSet?) {
        context.theme.obtainStyledAttributes(attrs, R.styleable.CustomSlider, 0, 0).apply {
            try {
                getStyle(this, context)
            } finally {
                recycle()
            }
        }

        boundRect = Rect()

        // Set paint properties for base line
        baseLinePaint = Paint()
        setPaintProp(
            baseLinePaint, PaintProp(
                color = ContextCompat.getColor(context, style.track.mainTrackColor),
                antiAlias = true,
                style = Paint.Style.FILL,
                strokeWidth = style.track.trackHeight,
                strokeCap = Paint.Cap.ROUND
            )
        )

        // Set paint properties for second line
        secondLinePaint = Paint()
        setPaintProp(
            secondLinePaint, PaintProp(
                color = ContextCompat.getColor(
                    context,
                    CustomSliderHelper.getSecondTrackColor(
                        style.type,
                        style.track.mainTrackColor,
                        style.track.secondTrackColor
                    )
                ),
                antiAlias = true,
                style = Paint.Style.FILL,
                strokeWidth = CustomSliderHelper.getSecondTrackHeight(
                    style.track.trackHeight,
                    style.track.secondTrackHeight,
                    style.tick.isTickVisible,
                    style.tick.tickRadius,
                    style.type
                ),
                strokeCap = Paint.Cap.ROUND
            )
        )

        // Set paint properties for thumb
        thumbPaint = Paint()
        setPaintProp(
            thumbPaint, PaintProp(
                color = ContextCompat.getColor(
                    context,
                    CustomSliderHelper.getThumbColor(
                        style.type,
                        style.track.mainTrackColor,
                        style.thumb.thumbColor
                    )
                ), antiAlias = true, style = Paint.Style.FILL
            )
        )

        // Set paint properties for tick
        tickPaint = Paint()
        setPaintProp(
            tickPaint, PaintProp(
                color = ContextCompat.getColor(context, style.tick.tickColor),
                antiAlias = true,
                style = Paint.Style.FILL
            )
        )

        // Set paint properties for tick description
        tickDescPaint = Paint()
        setPaintProp(
            tickDescPaint, PaintProp(
                color = ContextCompat.getColor(context, style.tick.tickDescColor),
                antiAlias = true,
                typeface = ResourcesCompat.getFont(context, R.font.roboto_regular),
                textSize = style.tick.tickDescSize
            )
        )

        // Set paint properties for tooltip
        tickTooltipPaint = Paint()
        setPaintProp(
            tickTooltipPaint, PaintProp(
                color = ContextCompat.getColor(context, style.tooltip.tickTooltipColor),
                antiAlias = true,
                style = Paint.Style.FILL_AND_STROKE
            )
        )

        // Set paint properties for tooltip description
        tickTooltipDescPaint = Paint()
        setPaintProp(
            tickTooltipDescPaint, PaintProp(
                color = ContextCompat.getColor(context, style.tooltip.tickTooltipDescColor),
                antiAlias = true,
                typeface = ResourcesCompat.getFont(context, R.font.roboto_regular),
                textSize = style.tooltip.tooltipDescSize
            )
        )

        setTooltipAnimator(300, style.tooltip.tooltipWidth, style.tooltip.tooltipDescSize)

        style.thumb.thumbRadius =
            CustomSliderHelper.getThumbSize(style.thumb.thumbRadius, style.track.trackHeight)

        // Set tick desc and tick tooltip position default value for vertical orientation
        if (style.orientation == Const.Orientation.VERTICAL.orientation) {
            style.tick.tickDescPosition = Const.Position.RIGHT.position
            style.tooltip.tickTooltipPosition = Const.Position.LEFT.position
        }

        validateTickTooltipPosition()
    }

    private fun validateTickTooltipPosition() {
        if (style.tick.isTickDescVisible && style.tooltip.isTickTooltipVisible && !CustomSliderHelper.validateTickTooltipPosition(
                style.tick.tickDescPosition, style.tooltip.tickTooltipPosition
            )
        ) {
            throw java.lang.Exception("Tick description or tick tooltip position not valid.")
        }
    }

    private fun getStyle(typedArray: TypedArray, context: Context) {
        val track = Track(
            typedArray.getDimension(
                R.styleable.CustomSlider_csTrackHeight,
                context.resources.getDimension(R.dimen.defaultTrackHeight)
            ),
            typedArray.getDimension(R.styleable.CustomSlider_csSecondTrackHeight, 0f),
            typedArray.getResourceId(R.styleable.CustomSlider_csMainTrackColor, R.color.white),
            typedArray.getResourceId(R.styleable.CustomSlider_csSecondTrackColor, R.color.black)
        )

        val thumb = Thumb(
            typedArray.getDimension(
                R.styleable.CustomSlider_csThumbRadius,
                context.resources.getDimension(R.dimen.defaultThumbRadius)
            ),
            typedArray.getInteger(R.styleable.CustomSlider_csDefaultThumbPosition, 0),
            typedArray.getResourceId(R.styleable.CustomSlider_csThumbColor, R.color.black)
        )

        val tick = Tick(
            typedArray.getBoolean(R.styleable.CustomSlider_csIsTickVisible, false),
            typedArray.getInteger(R.styleable.CustomSlider_csTickInterval, 100),
            typedArray.getDimension(
                R.styleable.CustomSlider_csTickRadius,
                context.resources.getDimension(R.dimen.defaultTickRadius)
            ),
            typedArray.getResourceId(R.styleable.CustomSlider_csTickColor, R.color.white),
            typedArray.getBoolean(R.styleable.CustomSlider_csIsTickDescVisible, false),
            typedArray.getDimension(
                R.styleable.CustomSlider_csTickDescSize,
                context.resources.getDimension(R.dimen.defaultTickDescSize)
            ),
            typedArray.getResourceId(R.styleable.CustomSlider_csTickDescColor, R.color.black),
            typedArray.getInteger(
                R.styleable.CustomSlider_csTickDescPosition, Const.Position.BOTTOM.position
            )
        )

        val tooltip = Tooltip(
            typedArray.getBoolean(R.styleable.CustomSlider_csIsTickTooltipVisible, false),
            typedArray.getDimension(
                R.styleable.CustomSlider_csTickTooltipSize,
                context.resources.getDimension(R.dimen.defaultTooltipWidth)
            ),
            typedArray.getResourceId(R.styleable.CustomSlider_csTickTooltipColor, R.color.white),
            typedArray.getInteger(
                R.styleable.CustomSlider_csTickTooltipPosition, Const.Position.TOP.position
            ),
            typedArray.getDimension(
                R.styleable.CustomSlider_csTickTooltipDescSize,
                context.resources.getDimension(R.dimen.defaultTooltipDescSize)
            ),
            typedArray.getResourceId(R.styleable.CustomSlider_csTickTooltipDescColor, R.color.black)
        )

        style = Style(
            typedArray.getInteger(
                R.styleable.CustomSlider_csOrientation,
                Const.Orientation.HORIZONTAL.orientation
            ),
            typedArray.getInteger(R.styleable.CustomSlider_csType, Const.Type.OVERLAY.type),
            typedArray.getInteger(R.styleable.CustomSlider_csValueFrom, 0),
            typedArray.getInteger(R.styleable.CustomSlider_csValueTo, 100),
            track,
            thumb,
            tick, tooltip
        )
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (style.orientation == Const.Orientation.VERTICAL.orientation) setMeasuredDimension(
            CustomSliderHelper.getMeasureSpec(style.orientation, widthMeasureSpec),
            heightMeasureSpec
        ) else setMeasuredDimension(
            widthMeasureSpec,
            CustomSliderHelper.getMeasureSpec(style.orientation, heightMeasureSpec)
        )
    }

    override fun onDraw(canvas: Canvas) {
        canvas.save()
        // Draw tick circle
        if (style.tick.isTickVisible) {
            if (CustomSliderHelper.isTickIntervalValid(
                    style.valueFrom,
                    style.valueTo,
                    style.tick.tickInterval
                )
            ) {
                drawTick(canvas)
            } else {
                throw java.lang.Exception("Tick interval value not valid.")
            }
        }

        // Draw base line
        when (style.orientation) {
            Const.Orientation.VERTICAL.orientation -> {
                canvas.drawLine(
                    measuredWidth.toFloat() / 2,
                    (style.thumb.thumbRadius * 2),
                    measuredWidth.toFloat() / 2,
                    measuredHeight.toFloat() - (style.thumb.thumbRadius * 2),
                    baseLinePaint
                )
            }
            Const.Orientation.HORIZONTAL.orientation -> {
                canvas.drawLine(
                    (style.thumb.thumbRadius * 2),
                    centerY,
                    (measuredWidth.toFloat() - (style.thumb.thumbRadius * 2)),
                    centerY,
                    baseLinePaint
                )
            }
        }

        val thumbStart = getThumbPosition()

        // Draw second line
        if (style.orientation == Const.Orientation.VERTICAL.orientation) {
            canvas.drawLine(
                measuredWidth.toFloat() / 2,
                measuredHeight.toFloat() - (style.thumb.thumbRadius * 2),
                measuredWidth.toFloat() / 2,
                thumbStart,
                secondLinePaint
            )
        } else {
            canvas.drawLine(
                (style.thumb.thumbRadius * 2),
                centerY,
                thumbStart,
                centerY,
                secondLinePaint
            )
        }
        // Draw thumb circle
        if (style.orientation == Const.Orientation.VERTICAL.orientation) {
            canvas.drawCircle(
                measuredWidth.toFloat() / 2,
                thumbStart,
                style.thumb.thumbRadius,
                thumbPaint
            )
        } else {
            canvas.drawCircle(
                thumbStart,
                measuredHeight.toFloat() / 2,
                style.thumb.thumbRadius,
                thumbPaint
            )
        }

        // Draw tooltip
        if (style.tooltip.isTickTooltipVisible) {
            drawTickToolTip(
                canvas,
                style.tooltip.tooltipWidth.toInt(),
                thumbStart,
                selectedTick.value
            )
        }
        canvas.restore()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.actionMasked) {
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                setThumbToTick(event.x, event.y)
                tooltipAnimatorSetRev.start()
            }
            MotionEvent.ACTION_MOVE -> {
                thumbCoordinateX = event.x
                thumbCoordinateY = event.y
                invalidate()
            }
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                thumbCoordinateX = event.x
                thumbCoordinateY = event.y
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
                this@CustomSlider.style.tooltip.tooltipWidth = it.animatedValue as Float
                invalidate()
            }
        }

        // Set animator, property and listener for tooltip object.
        val tooltipAnimatorReverse = ValueAnimator.ofFloat(tooltipWidth, 0f).apply {
            this.duration = duration
            this.addUpdateListener {
                this@CustomSlider.style.tooltip.tooltipWidth = it.animatedValue as Float
                invalidate()
            }
        }

        // Set animator, property and listener for tooltip description object.
        val tooltipDescAnimator = ValueAnimator.ofFloat(0f, tooltipDescSize).apply {
            this.duration = duration
            this.addUpdateListener {
                this@CustomSlider.style.tooltip.tooltipDescSize = it.animatedValue as Float
                invalidate()
            }
        }

        // Set animator, property and listener for tooltip description object.
        val tooltipDescAnimatorReverse = ValueAnimator.ofFloat(tooltipDescSize, 0f).apply {
            this.duration = duration
            this.addUpdateListener {
                this@CustomSlider.style.tooltip.tooltipDescSize = it.animatedValue as Float
                invalidate()
            }
        }

        tooltipAnimatorSet.playTogether(tooltipAnimator, tooltipDescAnimator)
        tooltipAnimatorSetRev.playTogether(tooltipAnimatorReverse, tooltipDescAnimatorReverse)
        this.style.tooltip.tooltipWidth = 0f
        this.style.tooltip.tooltipDescSize = 0f
    }

    private fun setPaintProp(
        paint: Paint, paintProp: PaintProp
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
            val sliderTick: SliderTick = if (CustomSliderHelper.isDefaultThumbPositionValid(
                    listTickCoordinates, style.thumb.defaultThumbPosition
                ) && listTickCoordinates.isNotEmpty()
            ) {
                listTickCoordinates[style.thumb.defaultThumbPosition - 1]
            } else {
                listTickCoordinates[0]
            }
            sliderValueListener?.invoke(sliderTick.value)
            if (style.orientation == Const.Orientation.VERTICAL.orientation) sliderTick.coordinateY else sliderTick.coordinateX
        } else {
            if (style.orientation == Const.Orientation.VERTICAL.orientation) thumbCoordinateY else thumbCoordinateX
        }

        thumbStart = CustomSliderHelper.getThumbCoorOnTrack(
            style.orientation, measuredWidth, measuredHeight, thumbStart, style.thumb.thumbRadius
        )

        return thumbStart
    }

    private fun setTickCoordinate() {
        this.listTickCoordinates = CustomSliderHelper.getTickCoordinate(
            style.valueFrom,
            style.valueTo,
            style.tick.tickInterval,
            measuredWidth,
            measuredHeight,
            style.thumb.thumbRadius,
            style.orientation
        )
    }

    private fun setThumbToTick(touchX: Float, touchY: Float) {
        val currentTickCoor =
            CustomSliderHelper.getCurrentTickCoor(
                style.orientation,
                listTickCoordinates,
                touchX,
                touchY
            )
        if (style.orientation == Const.Orientation.VERTICAL.orientation) {
            thumbCoordinateY = currentTickCoor.coordinateY
        } else {
            thumbCoordinateX = currentTickCoor.coordinateX
        }
        sliderValueListener?.invoke(currentTickCoor.value)

        selectedTick = currentTickCoor
    }

    private fun drawTick(canvas: Canvas) {
        setTickCoordinate()
        for (coordinate in listTickCoordinates) {
            canvas.drawCircle(
                coordinate.coordinateX,
                coordinate.coordinateY,
                style.tick.tickRadius,
                tickPaint
            )
            if (style.tick.isTickDescVisible) {
                drawTickDesc(canvas, coordinate)
            }
        }
    }

    private fun drawTickDesc(canvas: Canvas, sliderTick: SliderTick) {
        val desc = "${sliderTick.value} min"
        tickDescPaint.getTextBounds(desc, 0, desc.length, boundRect)
        val x = when (style.tick.tickDescPosition) {
            Const.Position.LEFT.position -> {
                sliderTick.coordinateX - boundRect.width() - (style.thumb.thumbRadius * 2)
            }
            Const.Position.RIGHT.position -> {
                sliderTick.coordinateX + (style.thumb.thumbRadius * 2)
            }
            else -> sliderTick.coordinateX - (boundRect.width() / 2)
        }
        val y = when (style.tick.tickDescPosition) {
            Const.Position.TOP.position -> {
                (sliderTick.coordinateY - (boundRect.height() / 2)) - (style.thumb.thumbRadius * 2)
            }
            Const.Position.BOTTOM.position -> {
                (sliderTick.coordinateY + (boundRect.height() / 2)) + (style.thumb.thumbRadius * 2)
            }
            else -> sliderTick.coordinateY + (boundRect.height() / 2)
        }
        canvas.drawText(desc, x, y, tickDescPaint)
    }

    private fun drawTooltipDescText(
        canvas: Canvas, positionX: Float, positionY: Float, widthToolTip: Int, text: String
    ) {
        tickTooltipDescPaint.getTextBounds(text, 0, text.length, boundRect)
        val (x, y) = when (style.tooltip.tickTooltipPosition) {
            Const.Position.LEFT.position -> {
                Pair(
                    positionX - widthToolTip - (boundRect.width() / 2),
                    positionY + (boundRect.height() / 2)
                )
            }
            Const.Position.TOP.position -> {
                Pair(
                    positionX - (boundRect.width() / 2),
                    positionY - widthToolTip + (boundRect.height() / 2)
                )
            }
            Const.Position.RIGHT.position -> {
                Pair(
                    positionX + widthToolTip - (boundRect.width() / 2),
                    positionY + (boundRect.height() / 2)
                )
            }
            Const.Position.BOTTOM.position -> {
                Pair(
                    positionX - (boundRect.width() / 2),
                    positionY + widthToolTip - (boundRect.height() / 2)
                )
            }
            else -> Pair(positionX, positionY)
        }
        canvas.drawText(text, x, y, tickTooltipDescPaint)
    }

    private fun drawTriangle(positionX: Float, positionY: Float, width: Int): Path {
        val pathTriangle = Path()
        pathTriangle.moveTo(positionX, positionY) // Initial bottom point
        val (first, second) = CustomSliderHelper.getTriangleCoor(
            style.tooltip.tickTooltipPosition, positionX, positionY, width
        )
        pathTriangle.lineTo(
            first.x, first.y
        ) // To top right point
        pathTriangle.lineTo(
            second.x, second.y
        )  // To top left point
        pathTriangle.lineTo(positionX, positionY) // Back to initial bottom point
        return pathTriangle
    }

    private fun drawTickToolTip(
        canvas: Canvas, widthToolTip: Int, thumbCoordinate: Float, value: Int
    ) {
        val pathTooltip = Path()
        val positionX =
            if (style.orientation == Const.Orientation.VERTICAL.orientation) (measuredWidth.toFloat() / 2) else thumbCoordinate
        val positionY =
            if (style.orientation == Const.Orientation.VERTICAL.orientation) thumbCoordinate else (measuredHeight.toFloat() / 2)

        val pathTriangle = drawTriangle(positionX, positionY, widthToolTip)
        val circleCoor = CustomSliderHelper.getCircleCoor(
            style.tooltip.tickTooltipPosition, positionX, positionY, widthToolTip
        )
        val pathCircle = Path().apply {
            addCircle(
                circleCoor.x, circleCoor.y, widthToolTip.toFloat() / 2, Path.Direction.CW
            )
        }

        pathTooltip.op(pathTriangle, pathCircle, Path.Op.UNION)
        pathCircle.close()
        pathTriangle.close()
        pathTooltip.close()
        canvas.drawPath(pathTooltip, tickTooltipPaint)

        tickTooltipDescPaint.textSize = style.tooltip.tooltipDescSize
        drawTooltipDescText(canvas, positionX, positionY, widthToolTip, value.toString())
    }

    fun resetThumbPosition() {
        isFirstDraw = true
        if (style.tick.isTickVisible && CustomSliderHelper.isDefaultThumbPositionValid(
                listTickCoordinates, style.thumb.defaultThumbPosition
            ) && listTickCoordinates.isNotEmpty()
        ) {
            selectedTick = listTickCoordinates[style.thumb.defaultThumbPosition - 1]
        }
        invalidate()
    }

    fun setThumbPosition(value: Int) {
        val selectedTick = listTickCoordinates.find { it.value == value }
        thumbCoordinateX = selectedTick?.coordinateX ?: thumbCoordinateX
        thumbCoordinateY = selectedTick?.coordinateY ?: thumbCoordinateY
        invalidate()
    }

    fun setValueChangeListener(listener: (value: Int) -> Unit) {
        sliderValueListener = listener
    }
}