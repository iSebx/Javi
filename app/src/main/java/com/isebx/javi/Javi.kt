package com.isebx.javi

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.sin
import kotlin.random.Random

/**
 * El "alma" de Javi: estado, comportamiento y pose de cada frame.
 * No depende de Android; trabaja en unidades propias (el origen son sus patas,
 * x hacia adelante, y hacia abajo). JaviPainter lo dibuja.
 */
class Javi {

    enum class St { WALK, CHASE, SIT, GROOM, STRETCH, SLEEP, PURR, POUNCE, EAT, HELD, FALL }

    companion object {
        const val HALF_W = 98f
        const val TOP = 150f
        const val BOTTOM = 6f
        const val TAIL_N = 8

        private const val BL1 = 0
        private const val BL2 = 1
        private const val FL1 = 2
        private const val FL2 = 3
        private const val TORSO = 4
        private const val PY = 5
        private const val HEAD = 6
        private const val HX = 7
        private const val HY = 8
        private const val TA = 9
        private const val TC = 10
        private const val FS1 = 11
        private const val FS2 = 12
        private const val LS = 13
        private const val EYE = 14
        private const val TG = 15
        private const val N = 16

        private const val SPEED = 34f

        //                               bl1   bl2   fl1   fl2  torso  py  head   hx    hy    ta   tc   fs1    fs2    ls   eye   tg
        private val P_WALK = floatArrayOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 34f, -74f, 35f, 9f, 1f, 1f, 1f, 1f, 0f)
        private val P_SIT = floatArrayOf(-85f, -85f, 49f, 49f, -55f, 21f, 4f, 9f, -86f, -30f, 14f, 1.48f, 1.31f, 1f, 1f, 0f)
        private val P_GROOM = floatArrayOf(-85f, -85f, -89f, 49f, -55f, 21f, 26f, 10f, -84f, -30f, 14f, 0.8f, 1.31f, 1f, 0.25f, 1f)
        private val P_STRETCH = floatArrayOf(6f, 6f, -77f, -73f, 15f, 0f, -12f, 44f, -55f, 62f, 6f, 1.29f, 1.33f, 1f, 0.2f, 0f)
        private val P_SLEEP = floatArrayOf(-86f, -86f, -86f, -86f, 0f, 21f, 20f, 34f, -41f, -25f, 12f, 1f, 1f, 1f, 0.08f, 0f)
        private val P_PURR = floatArrayOf(-85f, -85f, 49f, 49f, -55f, 21f, 10f, 9f, -86f, -30f, 15f, 1.48f, 1.31f, 1f, 0.08f, 0f)
        private val P_CROUCH = floatArrayOf(-14f, -14f, -18f, -18f, 6f, 9f, -8f, 36f, -62f, 8f, 2f, 1f, 1f, 0.72f, 1f, 0f)
        private val P_LEAP = floatArrayOf(55f, 45f, -60f, -50f, -8f, 0f, 4f, 36f, -79f, 28f, 3f, 1f, 1f, 1f, 1f, 0f)
        private val P_EAT = floatArrayOf(0f, 0f, -10f, -10f, 10f, 0f, 30f, 48f, -36f, 40f, 8f, 0.78f, 0.82f, 1f, 0.45f, 1f)
        private val P_HELD = floatArrayOf(22f, 30f, -16f, -24f, -24f, 0f, 8f, 28f, -80f, -62f, 3f, 1f, 1f, 1f, 1f, 0f)
    }

    // Posición en el mundo
    var x = 150f
    var dir = 1f
    var alt = 0f
    var night = false

    /** Qué está comiendo o tomando (ver Props). */
    var food = 0

    var st = St.WALK
        private set

    private var now = 0f
    private var t0 = 0f
    private var dur = 5f
    private var goal = Float.NaN
    private var x0 = HALF_W
    private var x1 = 300f
    private var vy = 0f
    private var blinkAt = 2f
    private val s = P_WALK.copyOf()

    private var wWalk = 0f
    private var wGroom = 0f
    private var wSleep = 0f
    private var wCrouch = 0f
    private var wPurr = 0f
    private var wEat = 0f
    private var wRun = 0f
    private var gait = 0f

    // Pose calculada para el frame actual
    var bl1 = 0f; private set
    var bl2 = 0f; private set
    var fl1 = 0f; private set
    var fl2 = 0f; private set
    var torso = 0f; private set
    var py = 0f; private set
    var head = 0f; private set
    var headX = 34f; private set
    var headY = -74f; private set
    var fs1 = 1f; private set
    var fs2 = 1f; private set
    var ls = 1f; private set
    var eye = 1f; private set
    var tongue = 0f; private set
    val tailX = FloatArray(TAIL_N + 1)
    val tailY = FloatArray(TAIL_N + 1)
    val heartX = FloatArray(2)
    val heartY = FloatArray(2)
    val heartS = FloatArray(2)
    val heartA = FloatArray(2)
    var zzX = 0f; private set
    var zzY = 0f; private set
    var zzA = 0f; private set
    var bowlA = 0f; private set

    /** true cuando está quieto durmiendo: se puede animar a menos cuadros por segundo. */
    val calm: Boolean
        get() = st == St.SLEEP && now - t0 > 1.5f

    fun setBounds(a: Float, b: Float) {
        x0 = a
        x1 = max(a, b)
        x = x.coerceIn(x0, x1)
    }

    private fun set(n: St) {
        st = n
        t0 = now
        goal = Float.NaN
        val r = Random.nextFloat()
        dur = when (n) {
            St.WALK -> 5f + r * 5f
            St.CHASE -> 4f
            St.SIT -> 5f + r * 6f
            St.GROOM -> 5f + r * 3f
            St.STRETCH -> 3f
            St.SLEEP -> 15f + r * 20f
            St.PURR -> 4.5f
            St.POUNCE -> 1.5f
            St.EAT -> 6.5f
            St.HELD, St.FALL -> 99999f
        }
    }

    private val busy: Boolean
        get() = st == St.HELD || st == St.FALL

    fun pet() { if (!busy) set(St.PURR) }
    fun feed(kind: Int) {
        if (busy) return
        food = kind
        set(St.EAT)
    }
    fun play() { if (!busy) set(St.POUNCE) }

    fun goTo(gx: Float) {
        if (busy) return
        set(St.WALK)
        goal = gx.coerceIn(x0, x1)
        dur = 99f
        dir = if (goal >= x) 1f else -1f
    }

    /** Corre hacia tx (el ovillo) y salta cuando está cerca. */
    fun chase(tx: Float) {
        if (busy || st == St.POUNCE || st == St.PURR || st == St.EAT) return
        if (st != St.CHASE) set(St.CHASE)
        goal = tx.coerceIn(x0, x1)
    }

    /** true mientras está en el aire durante un salto. */
    var leaping = false
        private set

    fun hold() { if (st != St.HELD) set(St.HELD) }

    fun drop() {
        vy = 0f
        set(St.FALL)
    }

    private fun next() {
        if (st != St.WALK) {
            set(St.WALK)
            return
        }
        val r = Random.nextFloat()
        set(
            when {
                night && r < 0.7f -> St.SLEEP
                r < 0.25f -> St.SIT
                r < 0.45f -> St.GROOM
                r < 0.60f -> St.STRETCH
                r < 0.85f -> St.SLEEP
                else -> St.POUNCE
            }
        )
    }

    private fun toward(current: Float, on: Boolean, f: Float): Float =
        current + ((if (on) 1f else 0f) - current) * f

    fun update(dt: Float) {
        now += dt

        if (st == St.FALL) {
            vy += 1400f * dt
            alt -= vy * dt
            if (alt <= 0f) {
                alt = 0f
                set(St.SIT)
            }
        } else if (st != St.HELD && now - t0 > dur) {
            if (st == St.EAT) set(St.PURR) else next()
        }

        if (st == St.CHASE && !goal.isNaN()) {
            val d = goal - x
            dir = if (d >= 0f) 1f else -1f
            if (abs(d) < 70f) {
                set(St.POUNCE)
            } else {
                x = (x + dir * 95f * dt).coerceIn(x0, x1)
            }
        }

        val t = now - t0
        var leap = false
        var crouch = false
        var u = 0f
        val target: FloatArray = when (st) {
            St.WALK, St.CHASE -> P_WALK
            St.SIT -> P_SIT
            St.GROOM -> P_GROOM
            St.STRETCH -> P_STRETCH
            St.SLEEP -> P_SLEEP
            St.PURR -> P_PURR
            St.EAT -> P_EAT
            St.HELD, St.FALL -> P_HELD
            St.POUNCE -> when {
                t < 0.75f -> { crouch = true; P_CROUCH }
                t < 1.3f -> { leap = true; u = (t - 0.75f) / 0.55f; P_LEAP }
                else -> P_WALK
            }
        }

        val f = 1f - exp(-dt * (if (leap) 16f else 9f))
        for (i in 0 until N) {
            if (i != EYE) s[i] += (target[i] - s[i]) * f
        }
        var et = target[EYE]
        if (now > blinkAt) {
            et = 0.08f
            if (now > blinkAt + 0.14f) blinkAt = now + 2f + Random.nextFloat() * 3f
        }
        s[EYE] += (et - s[EYE]) * (1f - exp(-dt * 28f))

        val wf = 1f - exp(-dt * 8f)
        leaping = leap
        wWalk = toward(wWalk, st == St.WALK || st == St.CHASE, wf)
        wRun = toward(wRun, st == St.CHASE, wf)
        wGroom = toward(wGroom, st == St.GROOM, wf)
        wSleep = toward(wSleep, st == St.SLEEP, wf)
        wCrouch = toward(wCrouch, crouch, wf)
        wPurr = toward(wPurr, st == St.PURR, wf)
        wEat = toward(wEat, st == St.EAT, wf)

        if (st == St.WALK) {
            if (!goal.isNaN()) {
                if (abs(goal - x) < 3f) set(St.SIT) else dir = if (goal > x) 1f else -1f
            }
            if (st == St.WALK) {
                x += dir * SPEED * dt
                if (x > x1) { x = x1; dir = -1f }
                if (x < x0) { x = x0; dir = 1f }
            }
        }

        if (leap) {
            x = (x + dir * 150f * dt).coerceIn(x0, x1)
            alt = 48f * sin(PI.toFloat() * u)
        } else if (!busy) {
            alt += (0f - alt) * f
        }

        gait += dt * (8f + 7f * wRun)
        val p = gait
        val sw = sin(p) * 26f * wWalk
        val g = sin(now * 7f) * wGroom
        bl1 = s[BL1] + sw
        fl2 = s[FL2] + sw
        bl2 = s[BL2] - sw
        fl1 = s[FL1] - sw + g * 8f
        val bob = -abs(sin(p)) * 1.6f * wWalk + sin(now * 1.8f) * 0.9f * wSleep
        py = s[PY] + bob
        torso = s[TORSO] + sin(now * 32f) * 2.5f * wCrouch
        head = s[HEAD] + sin(p * 2f) * 2.5f * wWalk + g * 5f +
            sin(now * 3f) * 4f * wPurr + sin(now * 9f) * 4f * wEat
        headX = s[HX]
        headY = s[HY] + bob
        fs1 = s[FS1]
        fs2 = s[FS2]
        ls = s[LS]
        eye = s[EYE]
        tongue = s[TG]

        // Cola: cadena de segmentos que sale de la cadera y se apoya en el piso
        val r = torso * PI.toFloat() / 180f
        val c = cos(r)
        val sn = sin(r)
        var px = -20f + (-13f) * c - (-13f) * sn
        var pyy = -34f + (-13f) * sn + (-13f) * c + py
        val gy = -5.5f + alt
        val amp = 14f * (1f - 0.7f * wSleep) + 10f * wCrouch
        val sp = 2.4f + 10f * wCrouch
        tailX[0] = px
        tailY[0] = pyy
        for (i in 0 until TAIL_N) {
            val a = (s[TA] + s[TC] * i + amp * sin(now * sp - i * 0.55f) * (i + 1) / TAIL_N) *
                PI.toFloat() / 180f
            px -= 7f * cos(a)
            pyy -= 7f * sin(a)
            if (pyy > gy) pyy = gy
            tailX[i + 1] = px
            tailY[i + 1] = pyy
        }

        for (q in 0..1) {
            val ph = (now * 0.6f + (q + 1) * 0.5f) % 1f
            heartX[q] = dir * 8f + (if (q == 0) -18f else 16f)
            heartY[q] = -118f - ph * 26f
            heartS[q] = 0.7f + ph * 0.5f
            heartA[q] = wPurr * (1f - ph)
        }
        zzX = dir * 34f - 12f
        zzY = -92f - (now * 8f) % 14f
        zzA = wSleep * 0.9f
        bowlA = wEat
    }
}
