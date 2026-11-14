package com.isebx.javi

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path

/** El lápiz de verdad: dibuja en un Canvas de Android. */
class AndroidPen : Pen {
    var canvas: Canvas? = null

    private val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val path = Path()

    private val c: Canvas get() = canvas!!

    private fun f(color: Int): Paint {
        fill.color = color
        return fill
    }

    private fun s(width: Float, color: Int): Paint {
        stroke.strokeWidth = width
        stroke.color = color
        return stroke
    }

    private fun build(cmds: FloatArray) {
        path.rewind()
        var i = 0
        while (i < cmds.size) {
            when (cmds[i]) {
                Pen.M -> { path.moveTo(cmds[i + 1], cmds[i + 2]); i += 3 }
                Pen.L -> { path.lineTo(cmds[i + 1], cmds[i + 2]); i += 3 }
                Pen.Q -> { path.quadTo(cmds[i + 1], cmds[i + 2], cmds[i + 3], cmds[i + 4]); i += 5 }
                Pen.C -> {
                    path.cubicTo(cmds[i + 1], cmds[i + 2], cmds[i + 3], cmds[i + 4], cmds[i + 5], cmds[i + 6])
                    i += 7
                }
                else -> { path.close(); i += 1 }
            }
        }
    }

    override fun save() { c.save() }
    override fun restore() { c.restore() }
    override fun translate(x: Float, y: Float) { c.translate(x, y) }
    override fun rotate(deg: Float) { c.rotate(deg) }
    override fun rotate(deg: Float, px: Float, py: Float) { c.rotate(deg, px, py) }
    override fun scale(sx: Float, sy: Float) { c.scale(sx, sy) }
    override fun clipRect(l: Float, t: Float, r: Float, b: Float) { c.clipRect(l, t, r, b) }

    override fun clipOval(l: Float, t: Float, r: Float, b: Float) {
        path.rewind()
        path.addOval(l, t, r, b, Path.Direction.CW)
        c.clipPath(path)
    }

    override fun layerAlpha(l: Float, t: Float, r: Float, b: Float, alpha: Float) {
        c.saveLayerAlpha(l, t, r, b, (alpha.coerceIn(0f, 1f) * 255).toInt())
    }

    override fun oval(l: Float, t: Float, r: Float, b: Float, color: Int) { c.drawOval(l, t, r, b, f(color)) }
    override fun circle(cx: Float, cy: Float, r: Float, color: Int) { c.drawCircle(cx, cy, r, f(color)) }
    override fun rect(l: Float, t: Float, r: Float, b: Float, color: Int) { c.drawRect(l, t, r, b, f(color)) }

    override fun roundRect(l: Float, t: Float, r: Float, b: Float, rad: Float, color: Int) {
        c.drawRoundRect(l, t, r, b, rad, rad, f(color))
    }

    override fun line(x0: Float, y0: Float, x1: Float, y1: Float, width: Float, color: Int) {
        c.drawLine(x0, y0, x1, y1, s(width, color))
    }

    override fun strokeCircle(cx: Float, cy: Float, r: Float, width: Float, color: Int) {
        c.drawCircle(cx, cy, r, s(width, color))
    }

    override fun arc(l: Float, t: Float, r: Float, b: Float, start: Float, sweep: Float, width: Float, color: Int) {
        c.drawArc(l, t, r, b, start, sweep, false, s(width, color))
    }

    override fun fillPath(cmds: FloatArray, color: Int) {
        build(cmds)
        c.drawPath(path, f(color))
    }

    override fun strokePath(cmds: FloatArray, width: Float, color: Int) {
        build(cmds)
        c.drawPath(path, s(width, color))
    }

    override fun polyline(pts: FloatArray, n: Int, width: Float, color: Int) {
        if (n < 2) return
        path.rewind()
        path.moveTo(pts[0], pts[1])
        for (i in 1 until n) path.lineTo(pts[i * 2], pts[i * 2 + 1])
        c.drawPath(path, s(width, color))
    }

    override fun text(s: String, x: Float, y: Float, size: Float, color: Int, outline: Int) {
        textPaint.textSize = size
        if (outline != 0) {
            textPaint.style = Paint.Style.STROKE
            textPaint.strokeWidth = size * 0.2f
            textPaint.color = outline
            c.drawText(s, x, y, textPaint)
        }
        textPaint.style = Paint.Style.FILL
        textPaint.color = color
        c.drawText(s, x, y, textPaint)
    }

    override fun icon(kind: Int) { Props.draw(c, kind) }
    override fun serving(kind: Int) { Props.drawServing(c, kind) }
}
