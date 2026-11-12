package com.isebx.javi

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path

/** Dibujitos de las cosas de Javi: comidas, agua, ovillo e íconos de la rueda. */
object Props {
    const val POLLO = 0
    const val CARNE = 1
    const val PESCADO = 2
    const val LECHE = 3
    const val ZAPALLO = 4
    const val AGUA = 5
    const val YARN = 6
    const val SIZE = 7
    const val MINUS = 8
    const val PLUS = 9
    const val OK = 10
    const val HOME = 11

    val FOOD_NAMES = arrayOf("Pollo", "Carnecita", "Pescado", "Leche", "Zapallo")

    private const val BROWN = 0xFF5A4332.toInt()

    private val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val path = Path()

    private fun f(color: Int): Paint {
        fill.color = color
        return fill
    }

    private fun s(color: Int, width: Float): Paint {
        stroke.color = color
        stroke.strokeWidth = width
        return stroke
    }

    private fun bowl(c: Canvas, bowlColor: Int, rim: Int, liquid: Int) {
        c.drawRoundRect(-13f, -3f, 13f, 9f, 6f, 6f, f(bowlColor))
        c.drawOval(-12f, -7f, 12f, 1f, f(liquid))
        c.drawOval(-12f, -7f, 12f, 1f, s(rim, 1.2f))
    }

    /** Dibuja el ícono centrado en (0,0), dentro de un cuadro de ±14 unidades. */
    fun draw(c: Canvas, kind: Int) {
        when (kind) {
            POLLO -> {
                c.save()
                c.rotate(-35f)
                c.drawLine(3f, 0f, 12f, 0f, s(0xFFFFF6E6.toInt(), 4f))
                c.drawCircle(13f, -2.5f, 2.6f, f(0xFFFFF6E6.toInt()))
                c.drawCircle(13f, 2.5f, 2.6f, f(0xFFFFF6E6.toInt()))
                c.drawOval(-11f, -8f, 5f, 8f, f(0xFFC98A45.toInt()))
                c.drawOval(-8f, -5f, -1f, 1f, f(0xFFDDA561.toInt()))
                c.restore()
            }
            CARNE -> {
                c.drawOval(-13f, -9f, 13f, 9f, f(0xFFC4574E.toInt()))
                c.drawOval(-9f, -6f, 9f, 6f, f(0xFFE08C82.toInt()))
                c.drawOval(-13f, -9f, 13f, 9f, s(0xFFF6D9C9.toInt(), 2f))
                c.drawCircle(-3f, 0f, 3f, f(0xFFFFF1DC.toInt()))
            }
            PESCADO -> {
                path.rewind()
                path.moveTo(4f, 0f)
                path.lineTo(13f, -7f)
                path.lineTo(13f, 7f)
                path.close()
                c.drawPath(path, f(0xFF6A94B5.toInt()))
                c.drawOval(-12f, -6.5f, 6f, 6.5f, f(0xFF7FA9C9.toInt()))
                c.drawCircle(-7f, -1.5f, 1.9f, f(0xFFFFFFFF.toInt()))
                c.drawCircle(-7.3f, -1.5f, 0.9f, f(0xFF3A2418.toInt()))
            }
            LECHE -> bowl(c, 0xFF7BA7D9.toInt(), 0xFF5E8CC2.toInt(), 0xFFFFFFFF.toInt())
            AGUA -> bowl(c, 0xFFD9866F.toInt(), 0xFFB9654F.toInt(), 0xFF9AD4F2.toInt())
            ZAPALLO -> {
                c.drawRoundRect(-2f, -12f, 2f, -5f, 1f, 1f, f(0xFF6E8B3D.toInt()))
                c.drawOval(-13f, -7f, -1f, 9f, f(0xFFE8893A.toInt()))
                c.drawOval(1f, -7f, 13f, 9f, f(0xFFE8893A.toInt()))
                c.drawOval(-7f, -8f, 7f, 10f, f(0xFFF09A47.toInt()))
            }
            YARN -> {
                c.drawCircle(0f, 0f, 12f, f(0xFFD9627A.toInt()))
                path.rewind()
                path.moveTo(-9f, -6f); path.quadTo(0f, -12f, 9f, -6f)
                path.moveTo(-11f, 1f); path.quadTo(0f, -6f, 11f, 1f)
                path.moveTo(-9f, 7f); path.quadTo(0f, 1f, 9f, 7f)
                path.moveTo(-5f, -10f); path.quadTo(-11f, 0f, -5f, 10f)
                c.drawPath(path, s(0xFFF2A6B6.toInt(), 1.6f))
            }
            SIZE -> {
                path.rewind()
                path.moveTo(-9f, 9f); path.lineTo(9f, -9f)
                path.moveTo(3f, -9f); path.lineTo(9f, -9f); path.lineTo(9f, -3f)
                path.moveTo(-3f, 9f); path.lineTo(-9f, 9f); path.lineTo(-9f, 3f)
                c.drawPath(path, s(BROWN, 2.4f))
            }
            MINUS -> c.drawLine(-8f, 0f, 8f, 0f, s(BROWN, 3f))
            PLUS -> {
                c.drawLine(-8f, 0f, 8f, 0f, s(BROWN, 3f))
                c.drawLine(0f, -8f, 0f, 8f, s(BROWN, 3f))
            }
            HOME -> {
                path.rewind()
                path.moveTo(-12f, -1f); path.lineTo(0f, -12f); path.lineTo(12f, -1f); path.close()
                c.drawPath(path, f(0xFFD9866F.toInt()))
                c.drawRect(-8f, -1f, 8f, 10f, f(0xFFF4E3C8.toInt()))
                c.drawRect(-8f, -1f, 8f, 10f, s(BROWN, 1.2f))
                c.drawRect(-2.5f, 3f, 2.5f, 10f, f(BROWN))
            }
            OK -> {
                path.rewind()
                path.moveTo(-8f, 0f); path.lineTo(-2f, 6f); path.lineTo(9f, -6f)
                c.drawPath(path, s(BROWN, 3f))
            }
        }
    }

    /** La comida servida en el piso: en platito, o en cuenco si es líquido. */
    fun drawServing(c: Canvas, kind: Int) {
        if (kind == LECHE || kind == AGUA) {
            draw(c, kind)
            return
        }
        c.drawOval(-17f, 0f, 17f, 7f, f(0xFFF4EFE6.toInt()))
        c.drawOval(-17f, 0f, 17f, 7f, s(0xFFCDBFA8.toInt(), 1f))
        c.save()
        c.translate(0f, -5f)
        c.scale(0.9f, 0.9f)
        draw(c, kind)
        c.restore()
    }
}
