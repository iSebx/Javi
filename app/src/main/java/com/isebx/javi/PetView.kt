package com.isebx.javi

import android.content.Context
import android.graphics.Canvas
import android.view.View

/**
 * Vista que dibuja a Javi.
 * follow = true: la ventana lo sigue (overlay), Javi queda centrado.
 * follow = false: Javi camina dentro de la vista (vista previa en la app).
 */
class PetView(
    context: Context,
    val javi: Javi,
    val unit: Float,
    private val follow: Boolean
) : View(context) {

    private val painter = JaviPainter()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.save()
        if (follow) {
            canvas.translate(width / 2f, Javi.TOP * unit)
        } else {
            canvas.translate(javi.x * unit, height - 14f * unit - javi.alt * unit)
        }
        canvas.scale(unit, unit)
        painter.draw(canvas, javi)
        canvas.restore()
    }
}
