package com.isebx.javi

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.view.MotionEvent
import android.view.View
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

/** La rueda de acciones que aparece al mantener apretado a Javi. */
@SuppressLint("ViewConstructor")
class WheelView(context: Context, private val d: Float) : View(context) {

    var cx = 0f
    var cy = 0f
    var onPick: ((Int) -> Unit)? = null
    var onDismiss: (() -> Unit)? = null

    private var icons = IntArray(0)
    private var labels = arrayOf<String>()
    private val px = FloatArray(8)
    private val py = FloatArray(8)

    private var radius = 118f * d
    private val item = 31f * d

    private val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = 0xFFFFF7EA.toInt()
    }
    private val ring = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f * d
        color = 0xFF5A4332.toInt()
    }
    private val text = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 13f * d
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    fun show(newIcons: IntArray, newLabels: Array<String>) {
        icons = newIcons
        labels = newLabels
        layoutItems()
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        layoutItems()
    }

    private fun layoutItems() {
        val n = icons.size
        if (n == 0) return
        val wide = n >= 6
        radius = (if (n >= 7) 142f else if (wide) 126f else 118f) * d
        val margin = radius + item + 4f * d
        var ccx = cx
        if (width > 0) {
            if (width < margin * 2f) ccx = width / 2f else ccx = ccx.coerceIn(margin, width - margin)
        }
        val ccy = if (cy - radius - item < 30f * d) radius + item + 30f * d else cy
        for (i in 0 until n) {
            val a = when {
                n == 1 -> 270.0
                wide -> 190.0 + 160.0 * i / (n - 1)
                else -> 200.0 + 140.0 * i / (n - 1)
            }
            val r = Math.toRadians(a)
            px[i] = ccx + radius * cos(r).toFloat()
            py[i] = ccy + radius * sin(r).toFloat()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for (i in icons.indices) {
            canvas.drawCircle(px[i], py[i], item, fill)
            canvas.drawCircle(px[i], py[i], item, ring)
            canvas.save()
            canvas.translate(px[i], py[i])
            canvas.scale(d * 1.45f, d * 1.45f)
            Props.draw(canvas, icons[i])
            canvas.restore()

            val ty = py[i] + item + 16f * d
            text.style = Paint.Style.STROKE
            text.strokeWidth = 4f * d
            text.color = 0xFF5A4332.toInt()
            canvas.drawText(labels[i], px[i], ty, text)
            text.style = Paint.Style.FILL
            text.color = 0xFFFFFFFF.toInt()
            canvas.drawText(labels[i], px[i], ty, text)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(e: MotionEvent): Boolean {
        if (e.actionMasked == MotionEvent.ACTION_UP) {
            for (i in icons.indices) {
                if (hypot(e.x - px[i], e.y - py[i]) < item + 8f * d) {
                    onPick?.invoke(i)
                    return true
                }
            }
            onDismiss?.invoke()
        }
        return true
    }
}
