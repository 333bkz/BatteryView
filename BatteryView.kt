package com.test.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.test.R

class BatteryView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private companion object {
        const val HEIGHT_WIDTH_PERCENT = 1.4f
        const val TOP_PAINT_WIDTH_PERCENT = .4f
        const val TOP_PAINT_HEIGHT_PERCENT = .1f
        const val BORDER_STROKE_WIDTH_PERCENT = .12f
    }

    private val backgroundDefColor = context.getColor(R.color.color_battery_background)
    private val percentDefColor = context.getColor(R.color.color_battery_def_percent_backgound)
    private val percentLowColor = context.getColor(R.color.color_battery_low_percent_backgound)
    private val percentMinColor = context.getColor(R.color.color_battery_min_percent_backgound)
    private val percentTextColor = context.getColor(R.color.color_battery_percent_text)

    private val chargeBorderDefColor = context.getColor(R.color.color_battery_def_border_charge)
    private val chargeBorderLowColor = context.getColor(R.color.color_battery_low_border_charge)
    private val chargeBorderMinColor = context.getColor(R.color.color_battery_min_border_charge)
    private val chargeColor = context.getColor(R.color.color_battery_charge)

    private val percentPaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    private val percentTextPaint = TextPaint().apply {
        isAntiAlias = true
        color = this@BatteryView.percentTextColor
        textAlign = Paint.Align.CENTER
    }
    private val topPaint = Paint().apply {
        isAntiAlias = true
    }
    private val borderPaint = Paint().apply {
        isAntiAlias = true
    }
    private val chargePaint = Paint().apply {
        isAntiAlias = true
        color = chargeColor
    }

    private val topRect = RectF()
    private val borderRect = RectF()
    private val percentTextRect = RectF()
    private var chargingRect = RectF()
    private val percentRect = RectF()

    private var radius: Float = 0f
    private var isCharging: Boolean = false
    private var borderStroke: Float = 0f
    private var percentRectTopMin = 0f
    private var percent: Int = 0

    private val chargingBitmap = getBitmap(R.drawable.drawable_battery_charging)

    @SuppressLint("DrawAllocation")
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val measureWidth = getDefaultSize(suggestedMinimumWidth, widthMeasureSpec)
        val measureHeight = (measureWidth * HEIGHT_WIDTH_PERCENT).toInt()
        setMeasuredDimension(measureWidth, measureHeight)
        borderStroke = BORDER_STROKE_WIDTH_PERCENT * measureWidth
        radius = borderStroke
        val topLeft = measureWidth * (1 - TOP_PAINT_WIDTH_PERCENT) / 2
        val topRight = measureWidth - topLeft
        val topBottom = TOP_PAINT_HEIGHT_PERCENT * measureHeight
        topRect.set(topLeft, 0f, topRight, topBottom)
        val borderLeft = borderStroke / 2
        val borderTop = topBottom + borderStroke / 2 + topBottom / 3
        val borderRight = measureWidth - borderStroke / 2
        val borderBottom = measureHeight - borderStroke / 2
        borderRect.set(borderLeft, borderTop, borderRight, borderBottom)
        percentTextRect.set(
            RectF(borderRect).apply {
                inset(borderStroke / 2, borderStroke / 2)
            }
        )
        percentRectTopMin = borderTop
        percentRect.set(borderRect)
        val chargingLeft = borderStroke
        var chargingTop = topBottom + borderStroke
        val chargingRight = measureWidth - borderStroke
        var chargingBottom = measureHeight - borderStroke
        val diff = ((chargingBottom - chargingTop) - (chargingRight - chargingLeft))
        chargingTop += (diff / 2)
        chargingBottom -= (diff / 2)
        chargingRect = RectF(chargingLeft, chargingTop, chargingRight, chargingBottom)
    }

    override fun onDraw(canvas: Canvas) {
        drawTop(canvas)
        drawBody(canvas)
        if (isCharging) {
            drawCharging(canvas)
        } else {
            drawProgress(canvas, percent)
        }
    }

    private fun drawTop(canvas: Canvas) {
        if (isCharging) {
            topPaint.color = getChargeBorderColor(percent)
        } else if (percent == 100) {
            topPaint.color = percentDefColor
        } else {
            topPaint.color = backgroundDefColor
        }
        canvas.drawRoundRect(topRect, radius, radius, topPaint)
    }

    private fun drawBody(canvas: Canvas) {
        if (isCharging) {
            borderPaint.style = Paint.Style.STROKE
            borderPaint.strokeWidth = borderStroke
            borderPaint.color = getChargeBorderColor(percent)
        } else {
            borderPaint.style = Paint.Style.FILL
            borderPaint.color = backgroundDefColor
        }
        canvas.drawRoundRect(borderRect, radius, radius, borderPaint)
    }

    private fun drawProgress(canvas: Canvas, percent: Int) {
        percentPaint.color = getPercentColor(percent)
        percentRect.top = percentRectTopMin + (percentRect.bottom - percentRectTopMin) * (100 - percent) / 100
        canvas.drawRoundRect(percentRect, radius, radius, percentPaint)
        val text = percent.toString()
        percentTextPaint.setTextSizeForWidth(percentTextRect.width(), if (text.length > 2) "888" else "88")
        val fontMetrics = percentTextPaint.fontMetrics
        val bottomLineY = percentTextRect.centerY() - (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.top
        canvas.drawText(text, percentTextRect.centerX(), bottomLineY, percentTextPaint)
    }

    private fun getPercentColor(percent: Int): Int {
        if (percent > 30) {
            return percentDefColor
        }
        if (percent > 15) {
            return percentLowColor
        }
        return percentMinColor
    }

    private fun getChargeBorderColor(percent: Int): Int {
        if (percent > 30) {
            return chargeBorderDefColor
        }
        if (percent > 15) {
            return chargeBorderLowColor
        }
        return chargeBorderMinColor
    }

    private fun drawCharging(canvas: Canvas) {
        chargingBitmap?.let {
            canvas.drawBitmap(it, null, chargingRect, chargePaint)
        }
    }

    private fun getBitmap(drawableId: Int, desireWidth: Int? = null, desireHeight: Int? = null): Bitmap? {
        val drawable = VectorDrawableCompat.create(resources, drawableId, context.theme) ?: return null
        drawable.setTint(this@BatteryView.chargeColor)
        val bitmap = Bitmap.createBitmap(
            desireWidth ?: drawable.intrinsicWidth,
            desireHeight ?: drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    fun setTypeface(typeface: Typeface) {
        percentTextPaint.typeface = typeface
        invalidate()
    }

    fun charge() {
        isCharging = true
        invalidate()
    }

    fun unCharge() {
        isCharging = false
        invalidate()
    }

    fun setPercent(percent: Int) {
        if (percent > 100 || percent < 0) {
            return
        }
        this.percent = percent
        invalidate()
    }

    private fun Paint.setTextSizeForWidth(desiredWidth: Float, text: String) {
        val testTextSize = 48f
        textSize = testTextSize
        val bounds = Rect()
        getTextBounds(text, 0, text.length, bounds)
        val desiredTextSize = testTextSize * desiredWidth / bounds.width()
        textSize = desiredTextSize
    }
}