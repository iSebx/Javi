package com.isebx.javi

import kotlin.math.abs
import kotlin.math.min
import kotlin.random.Random

/** El ovillo de lana: rebota, rueda y Javi lo persigue. Kotlin puro, en unidades Javi. */
class Yarn {
    companion object {
        const val R = 14f
        const val LIFE = 25f
    }

    var active = false
    var held = false
    var x = 0f
    var alt = 0f
    var vx = 0f
    var vy = 0f
    var rot = 0f
    var life = 0f

    fun spawn(sx: Float, salt: Float, svx: Float, svy: Float) {
        x = sx
        alt = salt
        vx = svx
        vy = svy
        life = LIFE
        held = false
        active = true
    }

    fun kick(dir: Float) {
        vx = dir * (160f + Random.nextFloat() * 170f)
        vy = 320f + Random.nextFloat() * 220f
    }

    fun update(dt: Float, x0: Float, x1: Float) {
        if (!active) return
        life -= dt
        if (life <= 0f) {
            active = false
            return
        }
        if (held) return
        vy -= 1400f * dt
        alt += vy * dt
        x += vx * dt
        if (alt <= 0f) {
            alt = 0f
            vy = if (abs(vy) > 70f) -vy * 0.5f else 0f
            vx -= vx * min(1f, 1.6f * dt)
        }
        if (x < x0) { x = x0; vx = abs(vx) * 0.6f }
        if (x > x1) { x = x1; vx = -abs(vx) * 0.6f }
        rot += vx * dt * 4f
    }
}
