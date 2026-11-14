package com.isebx.javi

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.view.View

/** El montoncito que deja Javi. Tapado con piedritas; se toca para juntarlo. */
@SuppressLint("ViewConstructor")
class PoopView(context: Context, private val unit: Float) : View(context) {

    companion object {
        const val HALF = 22f
    }

    /** Cuánto lo tapó con piedritas (0 a 1). */
    var covered = 0f

    private val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val stoneX = floatArrayOf(-13f, -7f, 0f, 8f, 14f, -4f, 5f, -10f, 11f)
    private val stoneY = floatArrayOf(-3f, -8f, -11f, -8f, -3f, -4f, -4f, -1f, -1f)
    private val stoneR = floatArrayOf(4f, 4.4f, 4f, 4.6f, 4f, 3.6f, 3.8f, 3.2f, 3.2f)

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.save()
        canvas.translate(width / 2f, height - 3f * unit)
        canvas.scale(unit, unit)
        fill.color = 0xFF8A5A3C.toInt()
        canvas.drawOval(-11f, -8f, 11f, 0f, fill)
        canvas.drawOval(-7f, -13f, 7f, -5f, fill)
        canvas.drawOval(-3.5f, -17f, 3.5f, -10f, fill)
        val n = (covered.coerceIn(0f, 1f) * stoneX.size).toInt()
        for (i in 0 until n) {
            fill.color = if (i % 2 == 0) 0xFFB9BEC4.toInt() else 0xFF9AA1A8.toInt()
            canvas.drawCircle(stoneX[i], stoneY[i], stoneR[i], fill)
        }
        canvas.restore()
    }
}
