package com.isebx.javi

import kotlin.math.abs
import kotlin.math.sin
import kotlin.random.Random

/** Una luciérnaga que cruza la pantalla. Kotlin puro, en unidades Javi. */
class Firefly {
    var active = false
    var x = 0f
    var alt = 0f
    var glow = 1f
    private var baseAlt = 0f
    private var vx = 0f
    private var t = 0f
    private var hover = 0f
    private var x0 = 0f
    private var x1 = 0f

    /** Entra por un costado, a una altura entre lo alcanzable y lo alto. */
    fun spawn(left: Float, right: Float, maxAlt: Float) {
        x0 = left - 40f
        x1 = right + 40f
        val fromLeft = Random.nextBoolean()
        x = if (fromLeft) x0 else x1
        vx = (if (fromLeft) 1f else -1f) * (38f + Random.nextFloat() * 22f)
        val r = Random.nextFloat()
        baseAlt = when {
            r < 0.4f -> 120f + Random.nextFloat() * 45f
            r < 0.8f -> 215f + Random.nextFloat() * 130f
            else -> 380f + Random.nextFloat() * 120f
        }.coerceAtMost(maxAlt)
        alt = baseAlt
        t = Random.nextFloat() * 10f
        hover = 3.5f + Random.nextFloat() * 3f
        active = true
    }

    /**
     * jx, pawAlt: dónde están Javi y sus patas. Cerca de él se queda revoloteando
     * un rato; si casi la toca, se escapa hacia arriba.
     */
    fun update(dt: Float, jx: Float, pawAlt: Float, swatting: Boolean) {
        if (!active) return
        t += dt
        var speed = vx
        val near = abs(x - jx) < 55f
        if (near && hover > 0f) {
            hover -= dt
            speed *= 0.12f
        }
        if (swatting && near && alt - pawAlt < 16f) {
            baseAlt += 150f * dt
            speed *= 2.2f
        }
        x += speed * dt
        alt = baseAlt + 26f * sin(t * 0.9f) + 9f * sin(t * 2.7f)
        glow = 0.62f + 0.38f * sin(t * 4.2f)
        if (x < x0 - 5f || x > x1 + 5f) active = false
    }
}
