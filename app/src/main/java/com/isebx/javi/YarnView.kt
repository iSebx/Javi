package com.isebx.javi

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.view.View

/** Ventanita que dibuja el ovillo. */
@SuppressLint("ViewConstructor")
class YarnView(context: Context, private val yarn: Yarn, private val unit: Float) : View(context) {

    companion object {
        const val HALF = 18f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.save()
        canvas.translate(width / 2f, height / 2f)
        canvas.scale(unit, unit)
        canvas.rotate(yarn.rot)
        Props.draw(canvas, Props.YARN)
        canvas.restore()
    }
}
