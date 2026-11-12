package com.isebx.javi

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path

/**
 * Dibuja a Javi en un Canvas. Espera el canvas ya trasladado a sus patas
 * y escalado a "unidades Javi".
 */
class JaviPainter {

    private val fur = 0xFFF0C595.toInt()
    private val furFar = 0xFFE0AE7C.toInt()
    private val furDark = 0xFFE2B181.toInt()
    private val cream = 0xFFF9E8D2.toInt()
    private val creamFar = 0xFFECD6B8.toInt()
    private val pink = 0xFFEFB3A8.toInt()
    private val nose = 0xFFE79A94.toInt()
    private val dark = 0xFF3A2418.toInt()
    private val tongueC = 0xFFEF7F86.toInt()
    private val heartC = 0xFFE8768A.toInt()

    private val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val text = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 13f }
    private val path = Path()

    private val bodyStripes = Path().apply {
        moveTo(-18f, -75f); rQuadTo(3f, 8f, 0f, 14f)
        moveTo(-4f, -77f); rQuadTo(3f, 8f, 0f, 14f)
        moveTo(10f, -75f); rQuadTo(3f, 7f, 0f, 12f)
    }
    private val headStripes = Path().apply {
        moveTo(-1f, -27f); rLineTo(0f, 6f)
        moveTo(7f, -28f); rLineTo(0f, 7f)
        moveTo(15f, -27f); rLineTo(0f, 6f)
    }
    private val earL = Path().apply { moveTo(-25f, -14f); lineTo(-19f, -42f); lineTo(-3f, -25f); close() }
    private val earLIn = Path().apply { moveTo(-20f, -19f); lineTo(-17f, -34f); lineTo(-8f, -25f); close() }
    private val earR = Path().apply { moveTo(5f, -27f); lineTo(20f, -42f); lineTo(25f, -13f); close() }
    private val earRIn = Path().apply { moveTo(10f, -25f); lineTo(19f, -34f); lineTo(21f, -19f); close() }
    private val lidL = Path().apply {
        moveTo(-7f, -8f); lineTo(7f, -8f); lineTo(7f, -3.6f); quadTo(0f, -5f, -7f, -1.6f); close()
    }
    private val lidR = Path().apply {
        moveTo(-7f, -8f); lineTo(7f, -8f); lineTo(7f, -1.6f); quadTo(0f, -5f, -7f, -3.6f); close()
    }
    private val nosePath = Path().apply { moveTo(6f, 5f); lineTo(13f, 5f); lineTo(9.5f, 9f); close() }
    private val mouth = Path().apply {
        moveTo(9.5f, 9f); quadTo(6f, 14f, 2f, 11f)
        moveTo(9.5f, 9f); quadTo(13f, 14f, 17f, 11f)
    }
    private val whiskers = Path().apply {
        moveTo(-6f, 9f); lineTo(-24f, 6f)
        moveTo(-6f, 12f); lineTo(-23f, 14f)
        moveTo(24f, 9f); lineTo(38f, 5f)
        moveTo(24f, 12f); lineTo(38f, 13f)
    }
    private val heart = Path().apply {
        moveTo(0f, 4f)
        cubicTo(-8f, -3f, -4f, -10f, 0f, -5f)
        cubicTo(4f, -10f, 8f, -3f, 0f, 4f)
        close()
    }

    private fun leg(c: Canvas, x: Float, y: Float, rot: Float, sy: Float, main: Int, sock: Int) {
        c.save()
        c.translate(x, y)
        c.rotate(rot)
        c.scale(1f, sy)
        fill.color = main
        c.drawRoundRect(-6.5f, -4f, 6.5f, 30f, 6.5f, 6.5f, fill)
        fill.color = sock
        c.drawRoundRect(-6.5f, 21f, 6.5f, 30f, 4.5f, 4.5f, fill)
        c.restore()
    }

    private fun eyeAt(c: Canvas, x: Float, rx: Float, open: Float, lid: Path) {
        c.save()
        c.translate(x, -3f)
        c.save()
        c.scale(1f, open)
        fill.color = dark
        c.drawOval(-rx, -6.2f, rx, 6.2f, fill)
        fill.color = 0xFFFFFFFF.toInt()
        c.drawCircle(1.5f, -1.5f, 1.5f, fill)
        c.restore()
        fill.color = fur
        c.drawPath(lid, fill)
        c.restore()
    }

    fun draw(c: Canvas, j: Javi) {
        c.save()
        c.scale(j.dir, 1f)

        // Cola
        stroke.strokeWidth = 11f
        stroke.color = fur
        path.rewind()
        path.moveTo(j.tailX[0], j.tailY[0])
        for (i in 1..Javi.TAIL_N) path.lineTo(j.tailX[i], j.tailY[i])
        c.drawPath(path, stroke)
        stroke.color = furDark
        path.rewind()
        path.moveTo(j.tailX[5], j.tailY[5])
        for (i in 6..Javi.TAIL_N) path.lineTo(j.tailX[i], j.tailY[i])
        c.drawPath(path, stroke)

        // Pata trasera lejana
        leg(c, -25f, -30f + j.py, j.bl2, j.ls, furFar, creamFar)

        // Torso
        c.save()
        c.translate(0f, j.py)
        c.rotate(j.torso, -20f, -34f)
        leg(c, 15f, -30f, j.fl2, j.ls * j.fs2, furFar, creamFar)
        fill.color = fur
        c.drawOval(-41f, -78f, 41f, -20f, fill)
        stroke.strokeWidth = 4f
        stroke.color = furDark
        c.drawPath(bodyStripes, stroke)
        fill.color = cream
        c.drawOval(11f, -50f, 37f, -22f, fill)
        c.restore()

        // Cabeza
        c.save()
        c.translate(j.headX, j.headY)
        c.rotate(j.head)
        fill.color = fur
        c.drawPath(earL, fill)
        c.drawPath(earR, fill)
        fill.color = pink
        c.drawPath(earLIn, fill)
        c.drawPath(earRIn, fill)
        fill.color = fur
        c.drawOval(-32f, -29f, 32f, 29f, fill)
        stroke.strokeWidth = 3f
        stroke.color = furDark
        c.drawPath(headStripes, stroke)
        fill.color = cream
        c.drawOval(-6f, 1f, 24f, 21f, fill)
        fill.color = pink
        fill.alpha = 178
        c.drawOval(-17f, 6f, -7f, 12f, fill)
        c.drawOval(21f, 5f, 29f, 11f, fill)
        fill.alpha = 255
        eyeAt(c, -4f, 4.6f, j.eye, lidL)
        eyeAt(c, 19f, 4.2f, j.eye, lidR)
        if (j.tongue > 0.02f) {
            fill.color = tongueC
            fill.alpha = (j.tongue.coerceIn(0f, 1f) * 255).toInt()
            c.drawOval(6.5f, 11.5f, 13.5f, 20.5f, fill)
            fill.alpha = 255
        }
        fill.color = nose
        c.drawPath(nosePath, fill)
        stroke.strokeWidth = 1.3f
        stroke.color = dark
        c.drawPath(mouth, stroke)
        stroke.strokeWidth = 1f
        stroke.color = 0xFFFFFFFF.toInt()
        c.drawPath(whiskers, stroke)
        c.restore()

        // Pata delantera cercana (por delante de la cabeza para poder lamerse)
        c.save()
        c.translate(0f, j.py)
        c.rotate(j.torso, -20f, -34f)
        leg(c, 26f, -30f, j.fl1, j.ls * j.fs1, fur, cream)
        c.restore()

        // Pata trasera cercana
        leg(c, -14f, -30f + j.py, j.bl1, j.ls, fur, cream)

        // Comida o bebida servida delante suyo
        if (j.bowlA > 0.02f) {
            val a = (j.bowlA.coerceIn(0f, 1f) * 255).toInt()
            c.saveLayerAlpha(44f, -30f, 90f, 12f, a)
            c.translate(67f, -4f)
            Props.drawServing(c, j.food)
            c.restore()
        }
        c.restore()

        // Corazones
        for (q in 0..1) {
            if (j.heartA[q] > 0.02f) {
                c.save()
                c.translate(j.heartX[q], j.heartY[q])
                c.scale(j.heartS[q], j.heartS[q])
                fill.color = heartC
                fill.alpha = (j.heartA[q].coerceIn(0f, 1f) * 255).toInt()
                c.drawPath(heart, fill)
                fill.alpha = 255
                c.restore()
            }
        }

        // Zzz
        if (j.zzA > 0.02f) {
            val a = (j.zzA.coerceIn(0f, 1f) * 255).toInt()
            text.style = Paint.Style.STROKE
            text.strokeWidth = 2.5f
            text.color = 0xFF5A4332.toInt()
            text.alpha = a / 2
            c.drawText("z z z", j.zzX, j.zzY, text)
            text.style = Paint.Style.FILL
            text.color = 0xFFFFFFFF.toInt()
            text.alpha = a
            c.drawText("z z z", j.zzX, j.zzY, text)
        }
    }
}
