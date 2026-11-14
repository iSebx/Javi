package com.isebx.javi

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.view.MotionEvent
import android.view.View

/** Tarjeta con cómo está Javi: comida, diversión y limpieza. Se cierra tocando. */
@SuppressLint("ViewConstructor")
class StatsView(
    context: Context,
    private val care: Care,
    private val name: String,
    private val d: Float
) : View(context) {

    var onExit: (() -> Unit)? = null

    private val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val title = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 22f * d
        textAlign = Paint.Align.CENTER
        color = 0xFF5A4332.toInt()
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    private val label = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 15f * d
        color = 0xFF5A4332.toInt()
    }
    private val small = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 13f * d
        textAlign = Paint.Align.CENTER
        color = 0xFF8A7460.toInt()
    }

    private fun row(c: Canvas, left: Float, right: Float, y: Float, icon: Int, name: String, v: Float, color: Int) {
        c.save()
        c.translate(left + 18f * d, y)
        c.scale(d * 1.15f, d * 1.15f)
        Props.draw(c, icon)
        c.restore()
        label.textAlign = Paint.Align.LEFT
        c.drawText(name, left + 44f * d, y - 6f * d, label)
        label.textAlign = Paint.Align.RIGHT
        c.drawText("${(v * 100).toInt()}%", right, y - 6f * d, label)
        val bx0 = left + 44f * d
        val by0 = y + 2f * d
        val by1 = y + 14f * d
        fill.color = 0xFFEADFCB.toInt()
        c.drawRoundRect(bx0, by0, right, by1, 6f * d, 6f * d, fill)
        fill.color = color
        c.drawRoundRect(bx0, by0, bx0 + (right - bx0) * v.coerceIn(0.04f, 1f), by1, 6f * d, 6f * d, fill)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = minOf(width - 40f * d, 320f * d)
        val h = 284f * d
        val x0 = (width - w) / 2f
        val y0 = (height - h) / 2f
        fill.color = 0xFFFFF7EA.toInt()
        canvas.drawRoundRect(x0, y0, x0 + w, y0 + h, 18f * d, 18f * d, fill)
        stroke.strokeWidth = 2f * d
        stroke.color = 0xFF5A4332.toInt()
        canvas.drawRoundRect(x0, y0, x0 + w, y0 + h, 18f * d, 18f * d, stroke)

        canvas.drawText(name, width / 2f, y0 + 40f * d, title)
        canvas.drawText(care.mood, width / 2f, y0 + 60f * d, small)
        val left = x0 + 22f * d
        val right = x0 + w - 22f * d
        row(canvas, left, right, y0 + 104f * d, Props.POLLO, "Comida", care.food, 0xFFE8893A.toInt())
        row(canvas, left, right, y0 + 156f * d, Props.YARN, "Diversión", care.play, 0xFFD9627A.toInt())
        row(canvas, left, right, y0 + 208f * d, Props.SHOWER, "Limpieza", care.clean, 0xFF6FA8DC.toInt())
        canvas.drawText("Tocá para cerrar", width / 2f, y0 + h - 18f * d, small)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(e: MotionEvent): Boolean {
        if (e.actionMasked == MotionEvent.ACTION_UP) onExit?.invoke()
        return true
    }
}
