package com.isebx.javi

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.view.View

/**
 * Vista que dibuja a un gato.
 * follow = true: la ventana lo sigue (overlay), el gato queda centrado.
 * follow = false: el gato camina dentro de la vista (vista previa en la app).
 */
@SuppressLint("ViewConstructor")
class PetView(
    context: Context,
    val javi: Javi,
    val unit: Float,
    private val follow: Boolean,
    var look: CatLook
) : View(context) {

    private val painter = CatPainter()
    private val pen = AndroidPen()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.save()
        if (follow) {
            canvas.translate(width / 2f, Javi.TOP * unit)
        } else {
            canvas.translate(javi.x * unit, height - 14f * unit - javi.alt * unit)
        }
        canvas.scale(unit, unit)
        pen.canvas = canvas
        painter.draw(pen, javi, look)
        canvas.restore()
    }
}
