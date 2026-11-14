package com.isebx.javi

import kotlin.math.cos
import kotlin.math.sin

/**
 * Dibuja un gato (su pose viene de Javi, su aspecto de CatLook) con un Pen.
 * Espera el lápiz ya trasladado a sus patas y escalado a "unidades gato".
 * Kotlin puro: el mismo código corre en la app y en las pruebas.
 */
class CatPainter {

    private val M = Pen.M
    private val L = Pen.L
    private val Q = Pen.Q
    private val C = Pen.C
    private val Z = Pen.Z

    private val pink = 0xFFEFB3A8.toInt()
    private val ink = 0xFF3A2418.toInt()
    private val pupil = 0xFF241812.toInt()
    private val tongueC = 0xFFEF7F86.toInt()
    private val heartC = 0xFFE8768A.toInt()
    private val water = 0xFF9AD4F2.toInt()
    private val brown = 0xFF5A4332.toInt()

    // Colores efectivos de este cuadro (ya con el oscurecido de mojado)
    private var base = 0
    private var far = 0
    private var dark = 0
    private var light = 0
    private var white = 0
    private var point = 0
    private var sock = 0
    private var sockFar = 0
    private var chest = 0
    private var muzzle = 0
    private var legC = 0
    private var legFar = 0
    private var earC = 0
    private var tailC = 0
    private var wet = 0f
    private var claws = 0f
    private var trail = 0f
    private var pointed = false

    private val softBody = floatArrayOf(
        M, -18f, -75f, Q, -15f, -68f, -18f, -61f,
        M, -4f, -77f, Q, -1f, -70f, -4f, -63f,
        M, 10f, -75f, Q, 13f, -69f, 10f, -63f
    )
    private val mackerelBody = floatArrayOf(
        M, -26f, -78f, Q, -22f, -69f, -26f, -61f,
        M, -14f, -80f, Q, -10f, -70f, -14f, -61f,
        M, -2f, -80f, Q, 2f, -70f, -2f, -62f,
        M, 10f, -78f, Q, 14f, -69f, 10f, -62f,
        M, 21f, -74f, Q, 24f, -67f, 21f, -61f
    )
    private val classicBody = floatArrayOf(
        M, -22f, -60f, C, -22f, -73f, 0f, -73f, 0f, -60f, C, 0f, -47f, -22f, -47f, -22f, -60f,
        M, -16f, -60f, C, -16f, -67f, -6f, -67f, -6f, -60f,
        M, 8f, -76f, Q, 13f, -66f, 8f, -56f,
        M, 20f, -72f, Q, 24f, -64f, 20f, -57f,
        M, -32f, -72f, Q, -28f, -64f, -31f, -58f
    )
    private val wrinkleBody = floatArrayOf(
        M, -20f, -70f, Q, -12f, -74f, -4f, -70f,
        M, -6f, -74f, Q, 2f, -78f, 10f, -74f,
        M, -24f, -58f, Q, -16f, -62f, -8f, -58f
    )
    private val curlBody = floatArrayOf(
        M, -24f, -64f, Q, -20f, -68f, -16f, -64f, Q, -12f, -60f, -8f, -64f,
        M, -6f, -52f, Q, -2f, -56f, 2f, -52f, Q, 6f, -48f, 10f, -52f,
        M, 4f, -70f, Q, 8f, -74f, 12f, -70f, Q, 16f, -66f, 20f, -70f
    )
    private val spotX = floatArrayOf(-26f, -14f, -2f, 10f, -18f, -6f, 6f, 20f, -28f, 14f)
    private val spotY = floatArrayOf(-64f, -72f, -66f, -72f, -54f, -52f, -58f, -62f, -46f, -48f)

    private val softHead = floatArrayOf(M, -1f, -27f, L, -1f, -21f, M, 7f, -28f, L, 7f, -21f, M, 15f, -27f, L, 15f, -21f)
    private val tabbyHead = floatArrayOf(
        M, -1f, -27f, L, -1f, -21f, M, 7f, -28f, L, 7f, -21f, M, 15f, -27f, L, 15f, -21f,
        M, -20f, 4f, L, -28f, 6f, M, -19f, -4f, L, -27f, -6f
    )
    private val wrinkleHead = floatArrayOf(M, 0f, -22f, Q, 8f, -26f, 16f, -22f, M, 2f, -16f, Q, 8f, -19f, 14f, -16f)

    private val earL = floatArrayOf(M, -25f, -14f, L, -19f, -42f, L, -3f, -25f, Z)
    private val earLIn = floatArrayOf(M, -20f, -19f, L, -17f, -34f, L, -8f, -25f, Z)
    private val earR = floatArrayOf(M, 5f, -27f, L, 20f, -42f, L, 25f, -13f, Z)
    private val earRIn = floatArrayOf(M, 10f, -25f, L, 19f, -34f, L, 21f, -19f, Z)
    private val foldL = floatArrayOf(M, -27f, -12f, Q, -30f, -30f, -14f, -27f, Q, -6f, -26f, -6f, -20f, Z)
    private val foldR = floatArrayOf(M, 3f, -22f, Q, 10f, -32f, 22f, -26f, Q, 28f, -22f, 26f, -12f, Z)
    private val foldLines = floatArrayOf(M, -24f, -20f, Q, -16f, -24f, -10f, -22f, M, 8f, -24f, Q, 16f, -27f, 22f, -22f)
    private val curlLines = floatArrayOf(M, -19f, -42f, Q, -11f, -44f, -10f, -36f, M, 20f, -42f, Q, 12f, -44f, 11f, -36f)
    private val blaze = floatArrayOf(M, 9f, -10f, L, -4f, 34f, L, 26f, 34f, Z)
    private val lidL = floatArrayOf(M, -7f, -8.5f, L, 7f, -8.5f, L, 7f, -3.6f, Q, 0f, -5f, -7f, -1.6f, Z)
    private val lidR = floatArrayOf(M, -7f, -8.5f, L, 7f, -8.5f, L, 7f, -1.6f, Q, 0f, -5f, -7f, -3.6f, Z)
    private val whiskerPath = floatArrayOf(
        M, -6f, 9f, L, -24f, 6f, M, -6f, 12f, L, -23f, 14f,
        M, 24f, 9f, L, 38f, 5f, M, 24f, 12f, L, 38f, 13f
    )
    private val heart = floatArrayOf(
        M, 0f, 4f, C, -8f, -3f, -4f, -10f, 0f, -5f, C, 4f, -10f, 8f, -3f, 0f, 4f, Z
    )

    private val backStripes = floatArrayOf(
        M, -17.9f, -58.2f, Q, 0f, -65.2f, 17.9f, -58.2f,
        M, -19.2f, -45.4f, Q, 0f, -52.4f, 19.2f, -45.4f,
        M, -15.4f, -33.3f, Q, 0f, -39.1f, 15.3f, -33.3f
    )
    private val backHeadStripes = floatArrayOf(
        M, -7f, -122.2f, L, -7f, -115.8f, M, 0f, -123.5f, L, 0f, -115.8f, M, 7f, -122.2f, L, 7f, -115.8f
    )
    private val backEarL = floatArrayOf(M, -28.2f, -108.2f, L, -25.6f, -140.2f, L, -2.6f, -122.2f, Z)
    private val backEarLIn = floatArrayOf(M, -23.7f, -114.6f, L, -21.8f, -131.2f, L, -10.2f, -121f, Z)
    private val backEarR = floatArrayOf(M, 28.2f, -108.2f, L, 25.6f, -140.2f, L, 2.6f, -122.2f, Z)
    private val backEarRIn = floatArrayOf(M, 23.7f, -114.6f, L, 21.8f, -131.2f, L, 10.2f, -121f, Z)
    private val backWhiskers = floatArrayOf(
        M, -30.7f, -95.4f, L, -41f, -97.9f, M, -30.7f, -91.5f, L, -40.3f, -89.6f,
        M, 30.7f, -95.4f, L, 41f, -97.9f, M, 30.7f, -91.5f, L, 40.3f, -89.6f
    )

    private val wetSpotX = floatArrayOf(-22f, 2f, -8f, 20f, 30f, -30f)
    private val wetSpotY = floatArrayOf(-58f, -40f, -30f, -92f, -70f, -36f)
    private val tailBuf = FloatArray((Javi.TAIL_N + 1) * 2)

    /** Oscurece un color de pelaje según qué tan mojado está. */
    private fun fur(c: Int): Int = CatLook.mix(c, CatLook.darken(c, 0.84f), wet)

    private fun prepare(j: Javi, k: CatLook) {
        wet = j.wet
        claws = j.claws
        trail = j.trail
        base = fur(k.base)
        far = fur(CatLook.darken(k.base, 0.92f))
        dark = fur(k.dark)
        light = fur(k.light)
        white = fur(CatLook.WHITE)
        pointed = k.point != 0
        point = if (pointed) fur(k.point) else 0
        val whiteFeet = k.white >= CatLook.W_MITTED
        sock = if (whiteFeet) white else if (pointed) point else light
        sockFar = CatLook.darken(sock, 0.93f)
        chest = if (k.white >= CatLook.W_LOCKET) white else light
        muzzle = if (k.white >= CatLook.W_TUXEDO) white else if (pointed) point else light
        if (k.white == CatLook.W_BICOLOR) {
            legC = white
            legFar = CatLook.darken(white, 0.93f)
        } else {
            legC = base
            legFar = far
        }
        earC = if (pointed) point else base
        tailC = if (pointed) point else base
    }

    private fun tabby(k: CatLook): Boolean =
        k.pattern == CatLook.MACKEREL || k.pattern == CatLook.CLASSIC || k.pattern == CatLook.SPOTTED

    private fun leg(
        p: Pen, x: Float, y: Float, rot: Float, sy: Float, main: Int, sockColor: Int,
        front: Boolean = false
    ) {
        p.save()
        p.translate(x, y)
        p.rotate(rot)
        p.scale(1f, sy)
        if (front && trail > 0.05f) {
            // Marcas de las uñas mientras resbala
            val len = 20f * trail / sy
            for (i in -1..1) p.line(i * 3.6f, 30f, i * 3.6f, 30f + len, 1.3f, CatLook.alpha(0xFFFFFFFF.toInt(), trail * 0.75f))
        }
        if (claws > 0.05f) {
            for (i in -1..1) p.line(i * 3.6f, 28f, i * 3.6f, 28f + 5.5f / sy, 1.5f, CatLook.alpha(ink, claws))
        }
        p.roundRect(-6.5f, -4f, 6.5f, 30f, 6.5f, main)
        // En los colorpoint la media sube hasta media pata
        val top = if (pointed && sockColor != white && sockColor != CatLook.darken(white, 0.93f)) 10f else 21f
        p.roundRect(-6.5f, top, 6.5f, 30f, 4.5f, sockColor)
        p.restore()
    }

    private fun tail(p: Pen, j: Javi, k: CatLook) {
        if (k.tail == CatLook.T_NONE) return
        val n = if (k.tail == CatLook.T_BOB) 2 else Javi.TAIL_N
        var w = when (k.tail) {
            CatLook.T_THIN -> 7f
            CatLook.T_FLUFFY -> 19f
            CatLook.T_BOB -> 13f
            else -> 11f
        }
        if (k.tail == CatLook.T_LONG && k.hair == CatLook.LONG) w = 17f
        if (k.tail == CatLook.T_LONG && k.hair == CatLook.MEDIUM) w = 14f
        for (i in 0..n) {
            tailBuf[i * 2] = j.tailX[i]
            tailBuf[i * 2 + 1] = j.tailY[i]
        }
        p.polyline(tailBuf, n + 1, w, tailC)
        if (n < Javi.TAIL_N) return
        if (tabby(k)) {
            // Anillos
            for (i in intArrayOf(2, 4, 6)) {
                // Anillo angosto en el medio del tramo, para que no se junten
                val ax = j.tailX[i] + (j.tailX[i + 1] - j.tailX[i]) * 0.5f
                val ay = j.tailY[i] + (j.tailY[i + 1] - j.tailY[i]) * 0.5f
                p.save()
                p.clipRect(ax - w, ay - w, ax + w, ay + w)
                p.line(j.tailX[i], j.tailY[i], j.tailX[i + 1], j.tailY[i + 1], w * 0.98f, dark)
                p.restore()
            }
            p.line(j.tailX[7], j.tailY[7], j.tailX[8], j.tailY[8], w, dark)
        } else if (k.tailTip && !pointed) {
            for (i in 5 until Javi.TAIL_N) {
                p.line(j.tailX[i], j.tailY[i], j.tailX[i + 1], j.tailY[i + 1], w, dark)
            }
        }
    }

    private fun bodyMarks(p: Pen, j: Javi, k: CatLook, rx: Float, ry: Float, cy: Float, sx: Float) {
        p.save()
        p.clipOval(-rx, cy - ry, rx + sx, cy + ry)
        if (k.white == CatLook.W_BICOLOR) p.oval(-58f, -88f, 30f + sx, -44f, base)
        for (pa in k.patches) {
            if (pa.part == 0) p.oval(pa.x - pa.rx, pa.y - pa.ry, pa.x + pa.rx, pa.y + pa.ry, fur(pa.color))
        }
        when (k.pattern) {
            CatLook.SOFT -> p.strokePath(softBody, 4f, dark)
            CatLook.MACKEREL -> p.strokePath(mackerelBody, 4f, dark)
            CatLook.CLASSIC -> p.strokePath(classicBody, 4f, dark)
            CatLook.SPOTTED -> for (i in spotX.indices) {
                p.oval(spotX[i] - 4.2f, spotY[i] - 3.2f, spotX[i] + 4.2f, spotY[i] + 3.2f, dark)
                p.oval(spotX[i] - 1.8f, spotY[i] - 1.3f, spotX[i] + 1.8f, spotY[i] + 1.3f, CatLook.mix(dark, base, 0.55f))
            }
        }
        if (k.hair == CatLook.HAIRLESS) p.strokePath(wrinkleBody, 1.6f, CatLook.darken(base, 0.84f))
        if (k.hair == CatLook.CURLY) p.strokePath(curlBody, 1.8f, CatLook.darken(base, 0.86f))
        if (j.dirt > 0.05f) {
            // Manchitas de tierra
            val d = CatLook.alpha(0xFF8A6A4A.toInt(), j.dirt * 0.6f)
            p.oval(-27f, -49f, -15f, -41f, d)
            p.oval(-7f, -38f, 4f, -30f, d)
            p.oval(11f, -62f, 21f, -55f, d)
            p.oval(-16f, -66f, -8f, -60f, d)
        }
        p.restore()
    }

    /** Color del pelo debajo de un ojo, para pintar el párpado. */
    private fun lidColor(k: CatLook, ex: Float, ey: Float, headBase: Int): Int {
        if (pointed) return point
        var c = headBase
        for (pa in k.patches) {
            if (pa.part == 1) {
                val dx = (ex - pa.x) / pa.rx
                val dy = (ey - pa.y) / pa.ry
                if (dx * dx + dy * dy <= 1f) c = fur(pa.color)
            }
        }
        return c
    }

    private fun eye(p: Pen, x: Float, y: Float, rx: Float, open: Float, iris: Int, lid: FloatArray, lidC: Int) {
        p.save()
        p.translate(x, y)
        p.save()
        p.scale(1f, open)
        p.oval(-rx, -6.4f, rx, 6.4f, iris)
        p.oval(-2.2f, -5.2f, 2.2f, 5.2f, pupil)
        p.circle(1.6f, -1.8f, 1.5f, 0xFFFFFFFF.toInt())
        p.restore()
        p.fillPath(lid, lidC)
        p.restore()
    }

    private fun ears(p: Pen, k: CatLook) {
        if (k.ears == CatLook.E_FOLD) {
            p.fillPath(foldL, earC)
            p.fillPath(foldR, earC)
            p.strokePath(foldLines, 1.6f, CatLook.darken(earC, 0.8f))
            return
        }
        val es = when (k.ears) {
            CatLook.E_BIG -> 1.3f
            CatLook.E_SMALL -> 0.8f
            CatLook.E_TUFT -> 1.12f
            else -> 1f
        }
        p.save()
        p.translate(-14f, -20f); p.scale(es, es); p.translate(14f, 20f)
        p.fillPath(earL, earC)
        p.fillPath(earLIn, k.innerEar)
        if (k.ears == CatLook.E_TUFT) p.line(-20f, -41f, -22f, -50f, 2.4f, dark)
        p.restore()
        p.save()
        p.translate(15f, -20f); p.scale(es, es); p.translate(-15f, 20f)
        p.fillPath(earR, earC)
        p.fillPath(earRIn, k.innerEar)
        if (k.ears == CatLook.E_TUFT) p.line(21f, -41f, 23f, -50f, 2.4f, dark)
        p.restore()
        if (k.ears == CatLook.E_CURL) p.strokePath(curlLines, 4f, earC)
    }

    private fun head(p: Pen, j: Javi, k: CatLook) {
        p.save()
        p.translate(j.headX, j.headY)
        p.rotate(j.head)
        if (k.headSize != 1f) p.scale(k.headSize, k.headSize)
        ears(p, k)
        val hrx = when (k.head) {
            CatLook.WEDGE -> 29f
            CatLook.FLAT -> 34f
            CatLook.WIDE -> 36f
            else -> 32f
        }
        val hry = if (k.head == CatLook.WEDGE) 27f else 29f
        val flat = k.head == CatLook.FLAT
        if (k.collar != 0) p.arc(-31f, -26f, 31f, 33f, 55f, 95f, 5f, k.collar)
        if (k.hair == CatLook.LONG) {
            // Mechones en la mejilla
            p.circle(-28f, 12f, 10f, base)
            p.circle(-18f, 24f, 9f, base)
        }
        // Cara triangular: mentón afinado que asoma por debajo
        if (k.head == CatLook.WEDGE) p.oval(-5f, 6f, 25f, 34f, if (pointed) point else base)
        p.oval(-hrx, -hry, hrx, hry, base)
        p.save()
        p.clipOval(-hrx, -hry, hrx, hry)
        for (pa in k.patches) {
            if (pa.part == 1) p.oval(pa.x - pa.rx, pa.y - pa.ry, pa.x + pa.rx, pa.y + pa.ry, fur(pa.color))
        }
        // La máscara de los colorpoint cubre casi toda la cara, ojos incluidos
        if (pointed) p.oval(-18f, -20f, 34f, 26f, point)
        if (k.white >= CatLook.W_TUXEDO) p.fillPath(blaze, white)
        p.restore()
        if (k.pattern == CatLook.SOFT) p.strokePath(softHead, 3f, dark)
        if (tabby(k)) p.strokePath(tabbyHead, 3f, dark)
        if (k.hair == CatLook.HAIRLESS) p.strokePath(wrinkleHead, 1.5f, CatLook.darken(base, 0.84f))
        if (flat) p.oval(-3f, 1f, 21f, 17f, muzzle) else p.oval(-6f, 1f, 24f, 21f, muzzle)
        if (k.blush && !pointed) {
            p.oval(-17f, 6f, -7f, 12f, CatLook.alpha(pink, 0.6f))
            p.oval(21f, 5f, 29f, 11f, CatLook.alpha(pink, 0.6f))
        }
        val ey = if (flat) -1f else -3f
        eye(p, -4f, ey, 4.9f, j.eye, k.eyeL, lidL, lidColor(k, -4f, ey - 5f, base))
        eye(p, 19f, ey, 4.5f, j.eye, k.eyeR, lidR, lidColor(k, 19f, ey - 5f, base))
        val ny = if (flat) 1f else 5f
        if (j.tongue > 0.02f) p.oval(6.5f, ny + 6.5f, 13.5f, ny + 15.5f, CatLook.alpha(tongueC, j.tongue))
        if (j.yawn > 0.03f) {
            val h = 1f + 8f * j.yawn
            p.oval(4.5f, ny + 9f - h, 14.5f, ny + 9f + h, CatLook.alpha(0xFFA8474F.toInt(), j.yawn * 1.6f))
        }
        p.fillPath(floatArrayOf(M, 6f, ny, L, 13f, ny, L, 9.5f, ny + 4f, Z), k.nose)
        if (j.puff > 0.03f) {
            val pc = CatLook.alpha(water, j.puff)
            p.line(30f, 8f, 42f, 5f, 2f, pc)
            p.line(31f, 13f, 44f, 15f, 2f, pc)
            p.line(29f, 18f, 40f, 24f, 2f, pc)
        }
        if (j.carry) {
            p.save()
            p.translate(14f, 14f)
            p.rotate(80f)
            p.icon(Props.FLOWER)
            p.restore()
        }
        // Sobre un hocico oscuro, la boca se dibuja clara para que se vea
        val lum = (muzzle shr 16 and 255) + (muzzle shr 8 and 255) + (muzzle and 255)
        p.strokePath(
            floatArrayOf(M, 9.5f, ny + 4f, Q, 6f, ny + 9f, 2f, ny + 6f, M, 9.5f, ny + 4f, Q, 13f, ny + 9f, 17f, ny + 6f),
            1.3f, if (lum < 300) 0xFFB8ABA6.toInt() else ink
        )
        if (k.hair != CatLook.HAIRLESS) p.strokePath(whiskerPath, 1f, k.whiskers)
        p.restore()
    }

    private fun side(p: Pen, j: Javi, k: CatLook) {
        p.save()
        p.translate(j.jitter, 0f)
        // Al darse vuelta se afina hasta quedar de canto
        p.scale(j.dir * (1f - 2f * j.turn).coerceAtLeast(0.04f), 1f)
        if (j.sink > 0.5f) {
            // Dentro de la cajita: hundido y recortado al ras del piso
            p.clipRect(-130f, -400f, 130f, 0.5f)
            p.translate(0f, j.sink)
        }
        if (j.sway > 0.01f || j.sway < -0.01f) p.rotate(j.sway, -8f, -Javi.HANG_LEN)

        tail(p, j, k)
        leg(p, -25f, -30f + j.py, j.bl2, j.ls, legFar, sockFar)

        // Torso
        p.save()
        p.translate(0f, j.py)
        p.rotate(j.torso, -20f, -34f)
        val sx = 82f * j.stretch
        leg(p, 15f + sx * 0.85f, -30f, j.fl2, j.ls * j.fs2, legFar, sockFar, true)
        val rx = 41f * k.bodyL
        var ry = 29f * k.bodyW
        if (k.hair == CatLook.LONG) ry += 5f
        if (k.hair == CatLook.MEDIUM) ry += 2f
        val cy = -20f - ry
        p.oval(-rx, cy - ry, rx + sx, cy + ry, if (k.white == CatLook.W_BICOLOR) white else base)
        bodyMarks(p, j, k, rx, ry, cy, sx)
        val cx = sx * 0.9f
        if (k.hair == CatLook.LONG) {
            // Pechera esponjosa
            p.oval(7f + cx, -61f, 45f + cx, -19f, chest)
            p.circle(14f + cx, -26f, 8f, chest)
            p.circle(34f + cx, -24f, 8f, chest)
        } else if (k.hair == CatLook.MEDIUM) {
            p.oval(9f + cx, -54f, 41f + cx, -20f, chest)
        } else {
            p.oval(11f + cx, -50f, 37f + cx, -22f, chest)
        }
        p.restore()

        head(p, j, k)

        // Pata delantera cercana (por delante de la cabeza para poder lamerse)
        p.save()
        p.translate(0f, j.py)
        p.rotate(j.torso, -20f, -34f)
        leg(p, 26f + sx * 0.85f, -30f, j.fl1, j.ls * j.fs1, legC, sock, true)
        p.restore()

        leg(p, -14f, -30f + j.py, j.bl1, j.ls, legC, sock)

        // Comida o bebida servida delante suyo
        if (j.bowlA > 0.02f) {
            p.save()
            p.layerAlpha(44f, -30f, 90f, 12f, j.bowlA)
            p.translate(67f, -4f)
            p.serving(j.food)
            p.restore()
            p.restore()
        }
        p.restore()
    }

    private fun backArm(p: Pen, side: Float, off: Float) {
        p.save()
        p.translate(0f, off)
        p.rotate(-16f * side, -20.5f * side, -81.3f)
        val x0 = if (side > 0f) -26.9f else 14.1f
        val x1 = if (side > 0f) -14.1f else 26.9f
        for (i in -1..1) {
            val cx = (x0 + x1) / 2f + i * 3.8f
            p.line(cx, -130f, cx, -136.5f, 1.5f, ink)
        }
        p.roundRect(x0, -129.9f, x1, -80f, 6.4f, legFar)
        p.roundRect(x0, if (pointed) -132.5f else -132.5f, x1, if (pointed && sock == point) -104f else -118.4f, 6.4f, sock)
        p.restore()
    }

    private fun backFoot(p: Pen, cx: Float) {
        p.oval(cx - 9f, -11.9f, cx + 9f, 0.3f, sock)
        p.oval(cx - 3.5f, -6.8f, cx + 3.5f, -2.2f, pink)
        p.circle(cx - 4.8f, -7.7f, 1.3f, pink)
        p.circle(cx, -9f, 1.3f, pink)
        p.circle(cx + 4.8f, -7.7f, 1.3f, pink)
    }

    /**
     * De espaldas, estirado contra la pantalla: patas de atrás rectas, las de
     * adelante arriba manoteando. Mismas proporciones que de costado.
     */
    private fun back(p: Pen, j: Javi, k: CatLook) {
        val t = j.time
        p.save()
        p.translate(j.jitter, 0f)
        p.scale((2f * j.turn - 1f).coerceIn(0.04f, 1f), 1f)

        if (k.tail != CatLook.T_NONE) {
            val w = when (k.tail) {
                CatLook.T_THIN -> 7f
                CatLook.T_FLUFFY -> 17f
                else -> if (k.hair == CatLook.LONG) 15f else 10.5f
            }
            p.save()
            p.rotate(sin(t * 2.4f) * 6f, 9f, -32.6f)
            if (k.tail == CatLook.T_BOB) {
                p.line(9f, -32.6f, 24f, -34f, 12f, tailC)
            } else {
                p.strokePath(floatArrayOf(M, 9f, -32.6f, Q, 38.4f, -28.8f, 41f, -49.3f, Q, 41.6f, -59.5f, 33.3f, -60.8f), w, tailC)
                if (!pointed && (k.tailTip || tabby(k))) {
                    p.strokePath(floatArrayOf(M, 41f, -48f, Q, 41.6f, -59.5f, 33.3f, -60.8f), w, dark)
                }
            }
            p.restore()
        }

        p.roundRect(-19.2f, -37.8f, -5.1f, -4.5f, 7f, legFar)
        p.roundRect(5.1f, -37.8f, 19.2f, -4.5f, 7f, legFar)
        backFoot(p, -12.8f)
        backFoot(p, 12.8f)

        val sw = sin(t * 7f) * 3.5f
        backArm(p, 1f, 3.5f + sw)
        backArm(p, -1f, 3.5f - sw)

        val bw = 28.2f * k.bodyW + (if (k.hair == CatLook.LONG) 4f else 0f)
        p.oval(-bw, -85.1f, bw, -23.7f, base)
        p.save()
        p.clipOval(-bw, -85.1f, bw, -23.7f)
        for (pa in k.patches) {
            if (pa.part == 0) {
                // Las manchas del cuerpo, reacomodadas sobre el lomo
                val px = pa.x * 0.6f
                val py = -54f + (pa.y + 49f) * 0.9f
                p.oval(px - pa.rx * 0.7f, py - pa.ry, px + pa.rx * 0.7f, py + pa.ry, fur(pa.color))
            }
        }
        if (k.pattern == CatLook.SOFT || k.pattern == CatLook.MACKEREL || k.pattern == CatLook.CLASSIC) {
            p.strokePath(backStripes, 3.2f, dark)
        }
        if (k.pattern == CatLook.SPOTTED) {
            for (i in spotX.indices) p.oval(spotX[i] * 0.7f - 3.4f, spotY[i] - 3f, spotX[i] * 0.7f + 3.4f, spotY[i] + 3f, dark)
        }
        p.restore()

        p.save()
        p.rotate((j.lookDx * 0.22f).coerceIn(-9f, 9f) + sin(t * 1.3f) * 2f, 0f, -81.3f)
        if (k.ears == CatLook.E_FOLD) {
            p.oval(-30f, -128f, -8f, -112f, earC)
            p.oval(8f, -128f, 30f, -112f, earC)
        } else {
            val es = when (k.ears) {
                CatLook.E_BIG -> 1.25f
                CatLook.E_SMALL -> 0.8f
                else -> 1f
            }
            p.save()
            p.translate(0f, -110f); p.scale(es, es); p.translate(0f, 110f)
            p.fillPath(backEarL, earC)
            p.fillPath(backEarR, earC)
            p.fillPath(backEarLIn, CatLook.darken(earC, 0.9f))
            p.fillPath(backEarRIn, CatLook.darken(earC, 0.9f))
            if (k.ears == CatLook.E_TUFT) {
                p.line(-25.6f, -139f, -27f, -147f, 2.2f, dark)
                p.line(25.6f, -139f, 27f, -147f, 2.2f, dark)
            }
            p.restore()
        }
        if (k.collar != 0) p.arc(-31f, -124f, 31f, -67f, 25f, 130f, 5f, k.collar)
        val hw = when (k.head) {
            CatLook.WIDE -> 35f
            CatLook.WEDGE -> 30f
            else -> 32f
        }
        p.oval(-hw, -126.1f, hw, -72.3f, base)
        p.save()
        p.clipOval(-hw, -126.1f, hw, -72.3f)
        for (pa in k.patches) {
            if (pa.part == 1) {
                val py = -99.2f + pa.y * 0.9f
                p.oval(pa.x - pa.rx, py - pa.ry, pa.x + pa.rx, py + pa.ry, fur(pa.color))
            }
        }
        p.restore()
        if (k.pattern == CatLook.SOFT || tabby(k)) p.strokePath(backHeadStripes, 2.9f, dark)
        if (k.hair != CatLook.HAIRLESS) p.strokePath(backWhiskers, 1f, k.whiskers)
        p.restore()

        p.restore()
    }

    fun draw(p: Pen, j: Javi, k: CatLook) {
        prepare(j, k)
        if (j.turn < 0.5f) side(p, j, k) else back(p, j, k)

        // La cajita, por delante
        if (j.boxA > 0.02f) {
            val a = j.boxA
            p.line(-64f, -55f, -80f, -65f, 6f, CatLook.alpha(0xFFB5855A.toInt(), a))
            p.line(64f, -55f, 80f, -65f, 6f, CatLook.alpha(0xFFB5855A.toInt(), a))
            p.rect(-64f, -55f, 64f, 0.5f, CatLook.alpha(0xFFC99A6B.toInt(), a))
            p.rect(-64f, -55f, 64f, -49f, CatLook.alpha(0xFFB5855A.toInt(), a))
            p.rect(-8f, -49f, 8f, 0.5f, CatLook.alpha(0xFFDFC39A.toInt(), a * 0.7f))
        }

        // Mojado: gotitas que brillan y gotean
        if (j.wet > 0.12f) {
            for (i in wetSpotX.indices) {
                val ph = (j.time * 0.55f + i * 0.37f) % 1f
                val x = wetSpotX[i] * j.dir
                val y = wetSpotY[i] + ph * 22f
                p.oval(x - 1.8f, y - 2.6f, x + 1.8f, y + 2.6f, CatLook.alpha(water, j.wet * 0.9f * (1f - ph)))
            }
        }

        // Se sacude: salpica para todos lados
        if (j.shaking > 0.05f && j.wet > 0.05f) {
            for (i in 0..9) {
                val ph = (j.time * 2.6f + i * 0.17f) % 1f
                val ang = i * 0.628f + 0.3f
                val dist = 34f + 62f * ph
                p.circle(
                    4f * j.dir + cos(ang) * dist, -58f + sin(ang) * dist * 0.8f, 2.6f,
                    CatLook.alpha(water, (1f - ph) * j.shaking * 0.94f)
                )
            }
        }

        // Una mosquita cuando está muy sucio
        if (j.dirt > 0.6f) {
            val fx = j.dir * 6f + cos(j.time * 3.1f) * 34f
            val fy = -118f + sin(j.time * 5.3f) * 12f
            p.oval(fx - 4f, fy - 3.5f, fx, fy - 0.5f, CatLook.alpha(0xFFFFFFFF.toInt(), 0.67f))
            p.oval(fx, fy - 3.5f, fx + 4f, fy - 0.5f, CatLook.alpha(0xFFFFFFFF.toInt(), 0.67f))
            p.circle(fx, fy, 1.8f, ink)
        }

        // Globito con lo que pide
        if (j.want != 0) {
            val bx = j.dir * 44f
            p.circle(j.dir * 30f, -128f, 3f, 0xFFFFFFFF.toInt())
            p.circle(j.dir * 36f, -137f, 4.5f, 0xFFFFFFFF.toInt())
            p.circle(bx, -155f, 16f, 0xFFFFFFFF.toInt())
            p.strokeCircle(bx, -155f, 16f, 1.2f, brown)
            p.save()
            p.translate(bx, -155f)
            p.scale(0.85f, 0.85f)
            p.icon(
                when (j.want) {
                    1 -> Props.POLLO
                    2 -> Props.YARN
                    else -> Props.SHOWER
                }
            )
            p.restore()
        }

        // Corazones
        for (q in 0..1) {
            if (j.heartA[q] > 0.02f) {
                p.save()
                p.translate(j.heartX[q], j.heartY[q])
                p.scale(j.heartS[q], j.heartS[q])
                p.fillPath(heart, CatLook.alpha(heartC, j.heartA[q]))
                p.restore()
            }
        }

        // Zzz
        if (j.zzA > 0.02f) {
            p.text("z z z", j.zzX, j.zzY, 13f, CatLook.alpha(0xFFFFFFFF.toInt(), j.zzA), CatLook.alpha(brown, j.zzA * 0.5f))
        }
    }
}
