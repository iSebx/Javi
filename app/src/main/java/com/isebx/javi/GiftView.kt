package com.isebx.javi

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.view.View

/** El regalito que Javi dejó en el piso. Se toca para aceptarlo. */
@SuppressLint("ViewConstructor")
class GiftView(context: Context, private val unit: Float) : View(context) {

    companion object {
        const val HALF = 20f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.save()
        canvas.translate(width / 2f - 12f * unit, height - 6f * unit)
        canvas.scale(unit * 1.15f, unit * 1.15f)
        canvas.rotate(78f)
        Props.draw(canvas, Props.FLOWER)
        canvas.restore()
    }
}
