package com.isebx.javi

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.view.View

/** Ventanita que dibuja la luciérnaga con su brillo. */
@SuppressLint("ViewConstructor")
class FireflyView(context: Context, private val fly: Firefly, private val unit: Float) :
    View(context) {

    companion object {
        const val HALF = 22f
    }

    private val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.save()
        canvas.translate(width / 2f, height / 2f)
        canvas.scale(unit, unit)
        val g = fly.glow.coerceIn(0f, 1f)
        fill.color = 0xFFF6F59A.toInt()
        fill.alpha = (40 * g).toInt()
        canvas.drawCircle(0f, 0f, 20f, fill)
        fill.alpha = (80 * g).toInt()
        canvas.drawCircle(0f, 0f, 13f, fill)
        fill.alpha = (150 * g).toInt()
        canvas.drawCircle(0f, 0f, 8f, fill)
        fill.color = 0xFFFFFDE0.toInt()
        fill.alpha = (200 + 55 * g).toInt()
        canvas.drawCircle(0f, 1f, 4f, fill)
        fill.color = 0xFF4A3B2A.toInt()
        fill.alpha = 255
        canvas.drawCircle(0f, -3f, 2.2f, fill)
        fill.color = 0xFFFFFFFF.toInt()
        fill.alpha = 150
        canvas.drawOval(-7f, -6f, -1f, -2f, fill)
        canvas.drawOval(1f, -6f, 7f, -2f, fill)
        canvas.restore()
    }
}
