package com.isebx.javi

import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.random.Random

/**
 * Un bichito que cruza la pantalla: luciérnaga (de noche) o mariposa (de día).
 * Kotlin puro, en unidades Javi.
 */
class Firefly {
    companion object {
        const val FIREFLY = 0
        const val BUTTERFLY = 1
    }

    var active = false
    var kind = FIREFLY
    var x = 0f
    var alt = 0f
    var glow = 1f
    var time = 0f
        private set

    /** La mariposa está posada en la nariz de Javi. */
    var landed = false
        private set

    private var baseAlt = 0f
    private var vx = 0f
    private var hover = 0f
    private var x0 = 0f
    private var x1 = 0f
    private var wantsLand = false
    private var landT = 0f
    private var leaving = false

    /** Entra por un costado, a una altura entre lo alcanzable y lo alto. */
    fun spawn(left: Float, right: Float, maxAlt: Float, k: Int = FIREFLY) {
        kind = k
        x0 = left - 40f
        x1 = right + 40f
        val fromLeft = Random.nextBoolean()
        x = if (fromLeft) x0 else x1
        vx = (if (fromLeft) 1f else -1f) * (38f + Random.nextFloat() * 22f)
        val r = Random.nextFloat()
        baseAlt = when {
            k == BUTTERFLY -> 150f + Random.nextFloat() * 120f
            r < 0.55f -> 125f + Random.nextFloat() * 65f
            r < 0.85f -> 200f + Random.nextFloat() * 90f
            else -> 330f + Random.nextFloat() * 150f
        }.coerceAtMost(maxAlt)
        alt = baseAlt
        time = Random.nextFloat() * 10f
        hover = 3.5f + Random.nextFloat() * 3f
        wantsLand = k == BUTTERFLY
        landed = false
        leaving = false
        landT = 0f
        active = true
    }

    /**
     * Luciérnaga. jx, pawAlt: dónde están Javi y sus patas. Cerca de él se queda
     * revoloteando un rato; si casi la toca, se escapa hacia arriba.
     */
    fun update(dt: Float, jx: Float, pawAlt: Float, swatting: Boolean) {
        if (!active) return
        time += dt
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
        alt = baseAlt + 26f * sin(time * 0.9f) + 9f * sin(time * 2.7f)
        glow = 0.62f + 0.38f * sin(time * 4.2f)
        if (x < x0 - 5f || x > x1 + 5f) active = false
    }

    /**
     * Mariposa. Si Javi está sentado mirándola (host), baja a posarse en su
     * nariz (noseX, noseAlt), se queda un ratito y se va.
     * Devuelve 1 cuando acaba de posarse y 2 cuando acaba de irse; si no, 0.
     */
    fun updateButterfly(dt: Float, host: Boolean, noseX: Float, noseAlt: Float): Int {
        if (!active) return 0
        time += dt
        var event = 0
        if (landed) {
            x = noseX
            alt = noseAlt
            landT -= dt
            if (landT <= 0f || !host) {
                landed = false
                leaving = true
                baseAlt = alt
                event = 2
            }
            return event
        }
        if (leaving) {
            baseAlt += 70f * dt
            x += vx * 1.6f * dt
            alt = baseAlt + 8f * sin(time * 5f)
        } else if (wantsLand && host && abs(x - noseX) < 150f) {
            val dx = noseX - x
            val dy = noseAlt - alt
            val dist = hypot(dx, dy)
            if (dist < 4f) {
                landed = true
                wantsLand = false
                landT = 3.8f
                x = noseX
                alt = noseAlt
                return 1
            }
            val step = 85f * dt / dist
            x += dx * step + sin(time * 7f) * 14f * dt
            alt += dy * step
            baseAlt = alt
        } else {
            x += vx * dt
            alt = baseAlt + 22f * sin(time * 1.3f) + 8f * sin(time * 3.1f)
        }
        if (x < x0 - 5f || x > x1 + 5f || alt > 900f) active = false
        return event
    }
}
