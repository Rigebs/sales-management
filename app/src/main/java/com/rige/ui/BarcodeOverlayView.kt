package com.rige.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class BarcodeOverlayView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val paint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 6f
        isAntiAlias = true
    }

    private val transparentPaint = Paint().apply {
        color = Color.BLACK
        alpha = 160
    }

    private val rect = RectF()

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()

        val boxWidth = width * 0.8f
        val boxHeight = height * 0.25f
        val left = (width - boxWidth) / 2
        val top = (height - boxHeight) / 2
        val right = left + boxWidth
        val bottom = top + boxHeight

        rect.set(left, top, right, bottom)

        canvas.drawRect(0f, 0f, width, height, transparentPaint)
        canvas.drawRect(rect, Paint().apply {
            xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        })

        // Dibujar borde blanco
        canvas.drawRoundRect(rect, 20f, 20f, paint)
    }

    override fun setLayerType(layerType: Int, paint: Paint?) {
        super.setLayerType(LAYER_TYPE_HARDWARE, paint)
    }
}