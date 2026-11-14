package com.isebx.javi

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
import android.view.MotionEvent
import android.view.View
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.random.Random

/**
 * La hora del baño: Javi en grande en el centro, un cepillo para enjabonarlo
 * y una ducha para enjuagarlo.
 */
@SuppressLint("ViewConstructor")
class BathView(context: Context, private val javi: Javi, private val d: Float) : View(context) {

    companion object {
        private const val TOOL_BRUSH = 0
        private const val TOOL_SHOWER = 1
        private const val MAX_FOAM = 170
        private const val MAX_DROPS = 140
    }

    var onExit: (() -> Unit)? = null

    /** Qué tan mojado quedó (0 a 1). */
    var wet = 0f
        private set

    private val painter = JaviPainter()
    private var tool = TOOL_BRUSH
    private val dirt0 = javi.dirt

    /** Qué tan bien quedó bañado: 1 = enjabonado y enjuagado del todo. */
    val quality: Float
        get() = if (soaped && foam == 0 && wet > 0.35f) 1f
        else (wet * 0.3f + (if (soaped) 0.2f else 0f)).coerceIn(0f, 0.9f)
    private var time = 0f

    // Espuma, en unidades de Javi
    private val fx = FloatArray(MAX_FOAM)
    private val fy = FloatArray(MAX_FOAM)
    private val fr = FloatArray(MAX_FOAM)
    private var foam = 0
    private var soaped = false

    // Gotas de la ducha, en píxeles
    private val dx = FloatArray(MAX_DROPS)
    private val dy = FloatArray(MAX_DROPS)
    private val dv = FloatArray(MAX_DROPS)
    private var drops = 0

    private var touching = false
    private var onButton = false
    private var tx = 0f
    private var ty = 0f

    private val item = 31f * d
    private val labels = arrayOf("Cepillo", "Ducha", "Volver")
    private val icons = intArrayOf(Props.BRUSH, Props.SHOWER, Props.BACK)

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
    private val star = Path().apply {
        moveTo(0f, -8f); quadTo(1.2f, -1.2f, 8f, 0f); quadTo(1.2f, 1.2f, 0f, 8f)
        quadTo(-1.2f, 1.2f, -8f, 0f); quadTo(-1.2f, -1.2f, 0f, -8f); close()
    }

    // Javi se dibuja con las patas en (ox, oy) y k píxeles por unidad
    private val k: Float get() = width / 250f
    private val ox: Float get() = width / 2f + 10f * k
    private val oy: Float get() = height * 0.60f

    private fun buttonX(i: Int): Float = width * (0.22f + 0.28f * i)
    private fun buttonY(): Float = height - 96f * d

    /** ¿El punto (en unidades de Javi) cae sobre su cuerpo o su cabeza? */
    private fun onJavi(ux: Float, uy: Float): Boolean {
        val hx = (ux - 9f) / 34f
        val hy = (uy + 86f) / 34f
        if (hx * hx + hy * hy <= 1f) return true
        val bx = (ux + 14f) / 40f
        val by = (uy + 42f) / 44f
        return bx * bx + by * by <= 1f
    }

    fun step(dt: Float) {
        time += dt
        if (touching && !onButton) {
            if (tool == TOOL_BRUSH) {
                val ux = (tx - ox) / k
                val uy = (ty - oy) / k
                if (onJavi(ux, uy)) {
                    repeat(2) {
                        if (foam < MAX_FOAM) {
                            fx[foam] = ux + (Random.nextFloat() - 0.5f) * 18f
                            fy[foam] = uy + (Random.nextFloat() - 0.5f) * 18f
                            fr[foam] = 5f + Random.nextFloat() * 6f
                            foam++
                        }
                    }
                    if (foam > 25) soaped = true
                }
            } else {
                repeat(3) {
                    if (drops < MAX_DROPS) {
                        dx[drops] = tx + (Random.nextFloat() - 0.5f) * 64f * d
                        dy[drops] = ty - 44f * d
                        dv[drops] = (420f + Random.nextFloat() * 160f) * d
                        drops++
                    }
                }
            }
        }

        var i = 0
        while (i < drops) {
            dv[i] += 2400f * d * dt
            dy[i] += dv[i] * dt
            val ux = (dx[i] - ox) / k
            val uy = (dy[i] - oy) / k
            val hit = onJavi(ux, uy)
            if (hit) {
                wet = (wet + 0.012f).coerceAtMost(1f)
                var j = 0
                while (j < foam) {
                    if (hypot(fx[j] - ux, fy[j] - uy) < 15f) {
                        fr[j] -= 3.2f
                        if (fr[j] < 2f) {
                            foam--
                            fx[j] = fx[foam]
                            fy[j] = fy[foam]
                            fr[j] = fr[foam]
                            continue
                        }
                    }
                    j++
                }
            }
            if (hit || dy[i] > height) {
                drops--
                dx[i] = dx[drops]
                dy[i] = dy[drops]
                dv[i] = dv[drops]
                continue
            }
            i++
        }
        javi.wet = wet
        javi.dirt = dirt0 * (1f - (if (soaped) wet else wet * 0.3f)).coerceIn(0f, 1f)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Javi
        canvas.save()
        canvas.translate(ox, oy)
        canvas.scale(k, k)
        painter.draw(canvas, javi)

        // Espuma
        for (i in 0 until foam) {
            fill.color = 0xFFFFFFFF.toInt()
            fill.alpha = 238
            canvas.drawCircle(fx[i], fy[i], fr[i], fill)
            stroke.strokeWidth = 0.8f
            stroke.color = 0xFFCFE3F7.toInt()
            canvas.drawCircle(fx[i], fy[i], fr[i], stroke)
        }
        fill.alpha = 255

        // Brillitos cuando quedó limpio
        if (soaped && foam == 0 && wet > 0.35f) {
            for (i in 0..3) {
                val a = time * 0.8f + i * 1.57f
                val s = 0.6f + 0.5f * sin(time * 4f + i * 2f)
                canvas.save()
                canvas.translate(-10f + 62f * cos(a), -70f + 58f * sin(a))
                canvas.scale(s, s)
                fill.color = 0xFFFFF3B0.toInt()
                canvas.drawPath(star, fill)
                canvas.restore()
            }
        }
        canvas.restore()

        // Gotas
        stroke.strokeWidth = 3f * d
        stroke.color = 0xFF8CCBF2.toInt()
        for (i in 0 until drops) {
            canvas.drawLine(dx[i], dy[i] - dv[i] * 0.018f, dx[i], dy[i], stroke)
        }

        // Herramienta en la mano
        if (touching && !onButton) {
            canvas.save()
            if (tool == TOOL_BRUSH) {
                canvas.translate(tx, ty - 34f * d)
                canvas.scale(d * 2.1f, d * 2.1f)
                Props.draw(canvas, Props.BRUSH)
            } else {
                canvas.translate(tx, ty - 66f * d)
                canvas.scale(d * 2.6f, d * 2.6f)
                Props.draw(canvas, Props.SHOWER)
            }
            canvas.restore()
        }

        // Botones
        val by = buttonY()
        for (i in 0..2) {
            val bx = buttonX(i)
            val selected = i == tool
            fill.color = if (selected) 0xFFFFE2B0.toInt() else 0xFFFFF7EA.toInt()
            canvas.drawCircle(bx, by, item, fill)
            stroke.strokeWidth = (if (selected) 4f else 2f) * d
            stroke.color = 0xFF5A4332.toInt()
            canvas.drawCircle(bx, by, item, stroke)
            canvas.save()
            canvas.translate(bx, by)
            canvas.scale(d * 1.45f, d * 1.45f)
            Props.draw(canvas, icons[i])
            canvas.restore()
            val ly = by + item + 16f * d
            text.style = Paint.Style.STROKE
            text.strokeWidth = 4f * d
            text.color = 0xFF5A4332.toInt()
            canvas.drawText(labels[i], bx, ly, text)
            text.style = Paint.Style.FILL
            text.color = 0xFFFFFFFF.toInt()
            canvas.drawText(labels[i], bx, ly, text)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(e: MotionEvent): Boolean {
        tx = e.x
        ty = e.y
        when (e.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                touching = true
                onButton = false
                for (i in 0..2) {
                    if (hypot(e.x - buttonX(i), e.y - buttonY()) < item + 10f * d) {
                        onButton = true
                        if (i == 2) onExit?.invoke() else tool = i
                    }
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                touching = false
                onButton = false
            }
        }
        return true
    }
}
