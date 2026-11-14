package com.isebx.javi

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.view.View

/** Un retrato quieto de un gato, sentado. Para las listas. */
@SuppressLint("ViewConstructor")
class CatThumb(context: Context, private val look: CatLook, private val unit: Float) : View(context) {

    private val painter = CatPainter()
    private val pen = AndroidPen()
    private val pose = Javi().apply {
        setBounds(Javi.HALF_W, 600f)
        debugSet(Javi.St.SIT)
        repeat(90) { update(0.016f) }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.save()
        canvas.translate(width / 2f + 14f * unit, height - 6f * unit)
        canvas.scale(unit, unit)
        pen.canvas = canvas
        painter.draw(pen, pose, look)
        canvas.restore()
    }
}
