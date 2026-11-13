package com.isebx.javi

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.view.MotionEvent
import android.view.View
import kotlin.math.hypot
import kotlin.math.sin

/**
 * El plumero: una capa que sigue el dedo por toda la pantalla. Javi corre
 * detrás de la pluma, le tira manotazos y se estira si está alta.
 */
@SuppressLint("ViewConstructor")
class WandView(context: Context, private val d: Float) : View(context) {

    var onExit: (() -> Unit)? = null

    /** Dónde está la pluma, en píxeles. */
    var tipX = 0f
        private set
    var tipY = 0f
        private set

    private var time = 0f
    private var placed = false
    private val item = 31f * d

    private val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val text = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 13f * d
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    private fun buttonX(): Float = width - 56f * d
    private fun buttonY(): Float = 110f * d

    fun step(dt: Float) {
        time += dt
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (!placed) {
            tipX = w * 0.5f
            tipY = h * 0.72f
            placed = true
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // Varita desde arriba hasta la pluma
        stroke.strokeWidth = 3f * d
        stroke.color = 0xFF8A6A4A.toInt()
        canvas.drawLine(tipX + 70f * d, tipY - 190f * d, tipX, tipY, stroke)
        canvas.save()
        canvas.translate(tipX, tipY)
        canvas.rotate(sin(time * 2.6f) * 18f)
        canvas.scale(d, d)
        fill.color = 0xFFD9627A.toInt()
        canvas.drawOval(-6f, -4f, 6f, 24f, fill)
        fill.color = 0xFFF2A6B6.toInt()
        canvas.save()
        canvas.rotate(20f)
        canvas.drawOval(-12f, -2f, -3f, 20f, fill)
        canvas.restore()
        canvas.save()
        canvas.rotate(-20f)
        canvas.drawOval(3f, -2f, 12f, 20f, fill)
        canvas.restore()
        canvas.restore()

        // Botón para volver
        val bx = buttonX()
        val by = buttonY()
        fill.color = 0xFFFFF7EA.toInt()
        canvas.drawCircle(bx, by, item, fill)
        stroke.strokeWidth = 2f * d
        stroke.color = 0xFF5A4332.toInt()
        canvas.drawCircle(bx, by, item, stroke)
        canvas.save()
        canvas.translate(bx, by)
        canvas.scale(d * 1.45f, d * 1.45f)
        Props.draw(canvas, Props.BACK)
        canvas.restore()
        val ly = by + item + 16f * d
        text.style = Paint.Style.STROKE
        text.strokeWidth = 4f * d
        text.color = 0xFF5A4332.toInt()
        canvas.drawText("Volver", bx, ly, text)
        text.style = Paint.Style.FILL
        text.color = 0xFFFFFFFF.toInt()
        canvas.drawText("Volver", bx, ly, text)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(e: MotionEvent): Boolean {
        if (e.actionMasked == MotionEvent.ACTION_DOWN &&
            hypot(e.x - buttonX(), e.y - buttonY()) < item + 10f * d
        ) {
            onExit?.invoke()
            return true
        }
        tipX = e.x
        tipY = e.y
        return true
    }
}
