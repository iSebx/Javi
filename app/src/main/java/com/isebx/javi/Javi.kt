package com.isebx.javi

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
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

    enum class St {
        WALK, CHASE, SIT, GROOM, STRETCH, SLEEP, PURR, POUNCE, EAT, BEG, RUB,
        HELD, FALL, JUMPTO, PERCH, HANG, SLIDE, CLING, BATH, SHAKE,
        WATCH, REACH, CLIMB,
        KNEAD, YAWN, SNEEZE, WASH, DROP, BOX, STARE, SWAT, POOP, COVER
    }

    companion object {
        const val HALF_W = 98f
        const val TOP = 175f
        const val BOTTOM = 6f
        const val TAIL_N = 8

        /** Distancia de las patas traseras a las delanteras cuando cuelga. */
        const val HANG_LEN = 123f

        /** Hasta dónde llegan sus patas cuando se estira contra la pantalla. */
        const val REACH_LEN = 150f

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
        private val P_BEG = floatArrayOf(-85f, -85f, -20f, 49f, -55f, 21f, -16f, 9f, -87f, -20f, 16f, 1.0f, 1.31f, 1f, 1.15f, 0f)
        private val P_RUB = floatArrayOf(0f, 0f, 0f, 0f, -4f, 0f, 20f, 36f, -70f, 78f, 3f, 1f, 1f, 1f, 0.08f, 0f)
        private val P_WATCH = floatArrayOf(-85f, -85f, 49f, 49f, -55f, 21f, 0f, 9f, -86f, -30f, 14f, 1.48f, 1.31f, 1f, 1.15f, 0f)
        private val P_REACH = floatArrayOf(12f, 4f, -102f, -102f, -74f, 0f, 0f, 24f, -84f, 20f, 4f, 1.9f, 2.25f, 1f, 1.15f, 0f)
        private val P_POOP = floatArrayOf(-48f, -48f, 22f, 22f, -22f, 10f, 2f, 30f, -80f, 80f, 0f, 1.2f, 1.12f, 1f, 0.5f, 0f)
        private val P_KNEAD = floatArrayOf(0f, 0f, -12f, -12f, 9f, 0f, 16f, 42f, -58f, 50f, 6f, 0.88f, 0.9f, 1f, 0.25f, 0f)
        private val P_BOX = floatArrayOf(-85f, -85f, 49f, 49f, -55f, 21f, 0f, 9f, -86f, 70f, 10f, 1.48f, 1.31f, 1f, 1.15f, 0f)
        private val P_STARE = floatArrayOf(-85f, -85f, 49f, 49f, -55f, 21f, 6f, 9f, -86f, -30f, 14f, 1.48f, 1.31f, 1f, 1.25f, 0f)
        private val P_BATH = floatArrayOf(-85f, -85f, 49f, 49f, -55f, 21f, 8f, 9f, -86f, -30f, 15f, 1.48f, 1.31f, 1f, 0.6f, 0f)
        private val P_CLING = floatArrayOf(50f, 40f, -55f, -45f, 0f, 12f, 6f, 34f, -60f, 85f, 0f, 1f, 1f, 0.8f, 1.25f, 0f)
        private val P_HANG = floatArrayOf(8f, -6f, -100f, -104f, -80f, 0f, -14f, 14f, -74f, -70f, 22f, 1.5f, 1.87f, 1f, 1f, 0f)
    }

    // Posición en el mundo
    var x = 150f
    var dir = 1f
    var alt = 0f
    var night = false

    /** Qué está comiendo o tomando (ver Props). */
    var food = 0

    /** Qué tan mojado está (0 a 1). Se va secando solo. */
    var wet = 0f

    /** Reloj interno, para animaciones del dibujo. */
    val time: Float
        get() = now

    var st = St.WALK
        private set

    private var now = 0f
    private var t0 = 0f
    private var dur = 5f
    private var goal = Float.NaN
    private var afterGoal = St.SIT
    private var x0 = HALF_W
    private var x1 = 300f
    private var vy = 0f
    private var blinkAt = 2f
    private val s = P_WALK.copyOf()

    private var jumpX0 = 0f
    private var jumpA0 = 0f
    private var jumpX1 = 0f
    private var jumpA1 = 0f
    private var jumpHang = false
    private var queuedFood = -1
    private var leaveQueued = false
    private var leaving = false
    private var slip = false
    private var slideV = 0f
    private var loveT = 0f

    private var wWalk = 0f
    private var wGroom = 0f
    private var wSleep = 0f
    private var wCrouch = 0f
    private var wLove = 0f
    private var wPurr = 0f
    private var wEat = 0f
    private var wRun = 0f
    private var wRub = 0f
    private var wHang = 0f
    private var wCling = 0f
    private var wSlide = 0f
    private var wShake = 0f
    private var wLook = 0f
    private var wReach = 0f
    private var wClimb = 0f
    private var wBack = 0f
    private var wYawn = 0f
    private var wKnead = 0f
    private var wWash = 0f
    private var wSwat = 0f
    private var wBox = 0f
    private var boxSink = 62f
    private var boxPeek = false
    private var boxT = 0f
    private var carrying = false
    private var giftReady = false
    private var poopEvent = 0
    private var groomedFlag = false
    private var wantVar = 0
    private var wCover = 0f
    private var flyQuick = false
    private var flyReach = true
    private var flyFollow = true

    // Lo que está mirando (la luciérnaga)
    private var flyX = 0f
    private var flyAlt = 0f
    private var flySeen = -99f
    private var climbCool = 0f
    private var climbMax = 300f
    private var look = 0f
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
    var sway = 0f; private set
    var jitter = 0f; private set
    var claws = 0f; private set
    var trail = 0f; private set
    var shaking = 0f; private set

    /** Cuánto se alarga el cuerpo al estirarse (0 = normal). */
    var stretch = 0f; private set

    /** 0 = de costado, 1 = de espaldas estirado contra la pantalla. */
    var turn = 0f; private set

    /** Hacia qué lado está lo que mira (para inclinar la cabeza de espaldas). */
    var lookDx = 0f; private set

    /** Bostezo: cuánto abre la boca (0 a 1). */
    var yawn = 0f; private set

    /** Estornudo: el soplidito (0 a 1). */
    var puff = 0f; private set

    /** Qué tan sucio se ve (0 a 1). Lo pone quien lo cuida. */
    var dirt = 0f

    /** Lo que le está faltando (ver Care.WANT_*). Lo pone quien lo cuida. */
    var needs = 0

    /** Lo que está pidiendo ahora mismo, para dibujar el globito. */
    var want = 0; private set

    /** Lleva un regalito en la boca. */
    var carry = false; private set

    /** Cuánto está hundido dentro de la cajita, y qué tan visible es la caja. */
    var sink = 0f; private set
    var boxA = 0f; private set
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

    /** true mientras está en el aire durante un salto corto. */
    var leaping = false
        private set

    /** true cuando está quieto durmiendo: se puede animar a menos cuadros por segundo. */
    val calm: Boolean
        get() = st == St.SLEEP && now - t0 > 1.5f

    /** En manos del usuario o en pleno vuelo: no acepta órdenes. */
    private val busy: Boolean
        get() = st == St.HELD || st == St.FALL || st == St.JUMPTO ||
            st == St.SLIDE || st == St.CLING || st == St.BATH || st == St.CLIMB

    /** Subido a un ícono (sentado o colgado). */
    val perched: Boolean
        get() = st == St.PERCH || st == St.HANG

    /** Estados en los que la altura no vuelve sola al piso. */
    private val aloft: Boolean
        get() = busy || perched

    /** Libre para que se le ocurra hacer algo nuevo. */
    val idle: Boolean
        get() = !leaving && !carrying &&
            (st == St.WALK || st == St.SIT || st == St.GROOM || st == St.STRETCH)

    /** Ya salió caminando de la pantalla (volvió a su casa). */
    val gone: Boolean
        get() = leaving && (x < x0 - HALF_W * 2.2f || x > x1 + HALF_W * 2.2f)

    fun setBounds(a: Float, b: Float) {
        x0 = a
        x1 = max(a, b)
        x = x.coerceIn(x0, x1)
    }

    private fun set(n: St) {
        st = n
        t0 = now
        goal = Float.NaN
        leaving = false
        carrying = false
        wantVar = 0
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
            St.BEG -> 5f
            St.RUB -> 0.9f
            St.PERCH -> 7f + r * 6f
            St.HANG -> 6f + r * 5f
            St.JUMPTO -> 1f
            St.CLING -> 1.3f
            St.SHAKE -> 1.4f
            St.WATCH, St.REACH, St.SWAT -> 99999f
            St.KNEAD -> 3.6f
            St.YAWN -> 2.8f
            St.SNEEZE -> 2.2f
            St.WASH -> 3.6f
            St.DROP -> 0.9f
            St.BOX -> 14f + r * 6f
            St.STARE -> 7f
            St.POOP -> 2.8f
            St.COVER -> 2.6f
            St.CLIMB -> 9f
            St.HELD, St.FALL, St.SLIDE, St.BATH -> 99999f
        }
    }

    /** Solo para las pruebas de dibujo: pone un estado a mano. */
    fun debugSet(n: St) {
        set(n)
    }

    fun pet() {
        if (busy) return
        if (perched || st == St.BOX) {
            loveT = 2.5f
            if (st == St.BOX) {
                boxPeek = true
                boxT = 3f
            }
            return
        }
        set(St.PURR)
    }

    fun feed(kind: Int) {
        if (busy) return
        if (perched) {
            queuedFood = kind
            drop()
            return
        }
        food = kind
        set(St.EAT)
    }

    fun play() {
        if (busy) return
        if (perched) {
            drop()
            return
        }
        set(St.POUNCE)
    }

    fun goTo(gx: Float, after: St = St.SIT) {
        if (aloft) return
        set(St.WALK)
        goal = gx.coerceIn(x0, x1)
        afterGoal = after
        dur = 99f
        dir = if (goal >= x) 1f else -1f
    }

    /** Corre hacia tx (el ovillo) y salta cuando está cerca. */
    fun chase(tx: Float) {
        if (busy || st == St.POUNCE || st == St.PURR || st == St.EAT || st == St.RUB) return
        if (perched) {
            drop()
            return
        }
        if (st != St.CHASE) set(St.CHASE)
        goal = tx.coerceIn(x0, x1)
    }

    /** Lo están acariciando de lado a lado: se frota contra el dedo. */
    fun rub(fingerX: Float) {
        if (aloft) {
            if (perched) loveT = 2.5f
            return
        }
        if (st != St.RUB) set(St.RUB)
        t0 = now
        if (abs(fingerX - x) > 12f) dir = if (fingerX >= x) 1f else -1f
    }

    /** Salta hasta (tx, talt): se sienta ahí arriba o queda colgado. */
    fun jumpTo(tx: Float, talt: Float, hang: Boolean) {
        if (aloft) return
        jumpX0 = x
        jumpA0 = alt
        jumpX1 = tx.coerceIn(x0, x1)
        jumpA1 = max(0f, talt)
        jumpHang = hang
        slip = false
        dir = if (jumpX1 >= x) 1f else -1f
        set(St.JUMPTO)
        val dist = abs(jumpX1 - jumpX0) + abs(jumpA1 - jumpA0)
        dur = 0.55f + dist / 1100f
    }

    fun hold() { if (st != St.HELD) set(St.HELD) }

    /** Lo soltaron sobre un ícono: queda colgado de ahí. */
    fun grabAt(tx: Float, talt: Float) {
        x = tx.coerceIn(x0, x1)
        alt = max(0f, talt)
        slip = false
        set(St.HANG)
    }

    /** Lo soltaron en lo alto sin nada de donde agarrarse: aguanta un toque y resbala. */
    fun grabAndSlip() {
        slip = true
        set(St.HANG)
        dur = 1.4f
    }

    /**
     * Hay una luciérnaga en (fx, falt). Javi la sigue con la mirada; si pasa
     * cerca se para en dos patas para manotearla, y si está más alta trepa.
     * maxAlt es hasta dónde puede trepar.
     */
    fun attend(
        fx: Float, falt: Float, maxAlt: Float,
        quick: Boolean = false, reach: Boolean = true, follow: Boolean = true
    ) {
        flyQuick = quick
        flyReach = reach
        flyFollow = follow
        flyX = fx
        flyAlt = falt
        climbMax = maxAlt
        flySeen = now
        if (leaving) return
        if (st == St.WALK || st == St.SIT || st == St.GROOM || st == St.STRETCH || st == St.BEG) {
            set(St.WATCH)
        }
    }

    /** Hace caca: se agacha, y después se da vuelta y la tapa. */
    fun startPoop(): Boolean {
        if (!idle) return false
        set(St.POOP)
        return true
    }

    /** 1 cuando acaba de hacer, 2 cuando terminó de taparla; si no, 0. Se lee una sola vez. */
    fun takePoopEvent(): Int {
        val e = poopEvent
        poopEvent = 0
        return e
    }

    /** true una sola vez, cuando acaba de lavarse solo. */
    fun takeGroomed(): Boolean {
        val g = groomedFlag
        groomedFlag = false
        return g
    }

    /** Cuánto avanzó tapando (0 a 1). */
    val coverProgress: Float
        get() = if (st == St.COVER) ((now - t0) / dur).coerceIn(0f, 1f) else 0f

    /** Se sienta mirándote y pide algo con un globito. */
    fun ask(what: Int) {
        if (!idle) return
        set(St.BEG)
        wantVar = what
    }

    /** Se mete en una cajita y espía. */
    fun startBox() {
        if (aloft) return
        boxSink = 62f
        boxPeek = false
        boxT = 0.9f
        set(St.BOX)
    }

    /** Va a buscar un regalito y lo trae hasta tx. */
    fun startGift(tx: Float) {
        if (!idle) return
        goTo(tx, St.DROP)
        carrying = true
    }

    /** true una sola vez, cuando acaba de dejar el regalito en el piso. */
    fun takeGift(): Boolean {
        val g = giftReady
        giftReady = false
        return g
    }

    /** Dónde está su nariz cuando está sentado (para la mariposa). */
    val noseX: Float
        get() = x + dir * 20f
    val noseAlt: Float
        get() = alt + 84f

    /** Una mariposa se le posó en la nariz: se queda duro mirándola. */
    fun stare(): Boolean {
        if (st != St.WATCH && st != St.SIT) return false
        set(St.STARE)
        return true
    }

    /** La mariposa se fue: estornuda. */
    fun endStare() {
        if (st == St.STARE) set(St.SNEEZE)
    }

    /** Altura de sus patas delanteras cuando está estirado hacia arriba. */
    val pawAlt: Float
        get() = alt + REACH_LEN

    /** true si está intentando alcanzar la luciérnaga. */
    val swatting: Boolean
        get() = st == St.REACH

    private fun stepAttention(dt: Float) {
        if (climbCool > 0f) climbCool -= dt
        if (st != St.WATCH && st != St.REACH && st != St.CLIMB && st != St.SWAT) return
        val lost = now - flySeen > 0.4f
        val d = flyX - x
        val ad = abs(d)
        if (st == St.CLIMB) {
            val tooLong = now - t0 > dur
            if (lost || ad > 120f || tooLong) {
                climbCool = 25f
                slip = true
                set(St.HANG)
                dur = 1.2f
                return
            }
            if (pawAlt < flyAlt - 6f && alt < climbMax) alt += 62f * dt
            if (ad > 6f) x = (x + (if (d > 0f) 1f else -1f) * 16f * dt).coerceIn(x0, x1)
            return
        }
        if (lost) {
            set(St.SIT)
            return
        }
        if (ad > 10f) dir = if (d > 0f) 1f else -1f
        if (st == St.REACH) {
            if (ad > 85f || flyAlt > 330f || flyAlt < 80f) set(St.WATCH)
            return
        }
        if (st == St.SWAT) {
            if (ad > 90f || flyAlt > 112f) set(St.WATCH)
            return
        }
        // WATCH
        if (ad > 115f) {
            if (flyFollow) x = (x + dir * (if (flyQuick) 95f else SPEED) * dt).coerceIn(x0, x1)
        } else if (ad < 70f && flyReach) {
            // Se estira contra la pantalla, con las patas de atrás en el piso. No trepa.
            if (flyAlt in 95f..300f) set(St.REACH)
            else if (flyAlt < 95f) set(St.SWAT)
        }
    }

    /** Empieza el baño: se sienta quieto, en el piso. */
    fun startBath() {
        alt = 0f
        dir = 1f
        leaveQueued = false
        queuedFood = -1
        set(St.BATH)
    }

    /** Termina el baño: se sacude el agua. */
    fun endBath() {
        if (st != St.BATH) return
        set(St.SHAKE)
    }

    /** Sacudieron el teléfono: se agarra con las uñas hasta que pare. */
    fun cling() {
        if (st == St.HELD || st == St.BATH) return
        if (st != St.CLING) set(St.CLING)
        t0 = now
    }

    /** Vuelve a su casa: camina hasta salir de la pantalla. */
    fun leave() {
        if (st == St.HELD) return
        if (aloft) {
            leaveQueued = true
            if (perched) drop()
            return
        }
        startLeave()
    }

    private fun startLeave() {
        set(St.WALK)
        leaving = true
        dur = 99999f
        dir = if (x > (x0 + x1) / 2f) 1f else -1f
    }

    private fun land() {
        alt = 0f
        if (leaveQueued) {
            leaveQueued = false
            startLeave()
        } else if (queuedFood >= 0) {
            food = queuedFood
            queuedFood = -1
            set(St.EAT)
        } else {
            set(St.SIT)
        }
    }

    fun drop() {
        vy = 0f
        set(St.FALL)
    }

    private fun next() {
        if (st == St.HANG && slip) {
            slideV = 40f
            set(St.SLIDE)
            return
        }
        if (perched) {
            drop()
            return
        }
        if (st == St.CLING) {
            if (alt > 5f) drop() else land()
            return
        }
        when (st) {
            St.EAT -> { set(St.WASH); return }
            St.WASH -> { groomedFlag = true; set(St.PURR); return }
            St.RUB -> { set(St.PURR); return }
            St.GROOM -> groomedFlag = true
            St.POOP -> {
                poopEvent = 1
                dir = -dir
                set(St.COVER)
                return
            }
            St.COVER -> {
                poopEvent = 2
                dir = -dir
                set(St.WALK)
                return
            }
            St.KNEAD -> { set(St.SLEEP); return }
            St.SLEEP -> { set(St.YAWN); return }
            St.STARE -> { set(St.SNEEZE); return }
            St.DROP -> { giftReady = true; set(St.PURR); return }
            else -> {}
        }
        if (st != St.WALK) {
            set(St.WALK)
            return
        }
        if (needs != 0 && Random.nextFloat() < 0.35f) {
            ask(needs)
            return
        }
        val r = Random.nextFloat()
        if (night && r < 0.7f) {
            set(St.KNEAD)
            return
        }
        when {
            r < 0.22f -> set(St.SIT)
            r < 0.40f -> set(St.GROOM)
            r < 0.52f -> set(St.STRETCH)
            r < 0.76f -> set(St.KNEAD)
            r < 0.82f -> set(St.SNEEZE)
            r < 0.88f -> startBox()
            else -> set(St.POUNCE)
        }
    }

    /** Sube de 0 a 1 entre a y b, se mantiene hasta c y baja hasta d. */
    private fun ramp(t: Float, a: Float, b: Float, c: Float, d: Float): Float = when {
        t < a -> 0f
        t < b -> (t - a) / (b - a)
        t < c -> 1f
        t < d -> 1f - (t - c) / (d - c)
        else -> 0f
    }

    private fun toward(current: Float, on: Boolean, f: Float): Float =
        current + ((if (on) 1f else 0f) - current) * f

    fun update(dt: Float) {
        now += dt
        if (loveT > 0f) loveT -= dt

        if (st == St.FALL) {
            vy += 1400f * dt
            alt -= vy * dt
            if (alt <= 0f) land()
        } else if (st == St.SLIDE) {
            slideV = (slideV + 260f * dt).coerceAtMost(420f)
            alt -= slideV * dt
            if (alt <= 0f) land()
        } else if (st == St.JUMPTO) {
            val u = ((now - t0) / dur).coerceIn(0f, 1f)
            x = jumpX0 + (jumpX1 - jumpX0) * u
            alt = jumpA0 + (jumpA1 - jumpA0) * u + 70f * 4f * u * (1f - u)
            if (u >= 1f) {
                x = jumpX1
                alt = jumpA1
                set(if (jumpHang) St.HANG else St.PERCH)
            }
        } else if (st != St.HELD && now - t0 > dur) {
            next()
        }

        stepAttention(dt)

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
            St.SIT, St.PERCH -> P_SIT
            St.GROOM -> P_GROOM
            St.STRETCH -> P_STRETCH
            St.SLEEP -> P_SLEEP
            St.PURR -> P_PURR
            St.EAT -> P_EAT
            St.BEG -> P_BEG
            St.RUB -> P_RUB
            St.HANG, St.SLIDE, St.CLIMB -> P_HANG
            St.WATCH -> if (flyFollow && abs(flyX - x) > 115f) P_WALK else P_WATCH
            St.KNEAD, St.COVER -> P_KNEAD
            St.POOP -> P_POOP
            St.YAWN, St.SNEEZE -> P_SIT
            St.WASH -> P_GROOM
            St.DROP -> P_EAT
            St.BOX -> P_BOX
            St.STARE -> P_STARE
            St.SWAT -> P_BEG
            St.REACH -> P_WATCH
            St.CLING -> P_CLING
            St.BATH -> P_BATH
            St.SHAKE -> P_WALK
            St.JUMPTO -> P_LEAP
            St.HELD, St.FALL -> P_HELD
            St.POUNCE -> when {
                t < 0.75f -> { crouch = true; P_CROUCH }
                t < 1.3f -> { leap = true; u = (t - 0.75f) / 0.55f; P_LEAP }
                else -> P_WALK
            }
        }

        val quick = leap || st == St.JUMPTO || st == St.HANG
        val f = 1f - exp(-dt * (if (quick) 16f else 9f))
        for (i in 0 until N) {
            if (i != EYE) s[i] += (target[i] - s[i]) * f
        }
        val tq = now - t0
        var yw = 0f
        var snA = 0f
        var snS = 0f
        var pf = 0f
        if (st == St.YAWN) yw = ramp(tq, 0.4f, 1f, 1.9f, 2.5f)
        if (st == St.SNEEZE) {
            snA = ramp(tq, 0.2f, 1f, 1f, 1.02f)
            snS = ramp(tq, 1f, 1.06f, 1.2f, 1.7f)
            pf = ramp(tq, 1.02f, 1.08f, 1.2f, 1.5f)
        }
        wYawn += (yw - wYawn) * (1f - exp(-dt * 16f))
        puff += (pf - puff) * (1f - exp(-dt * 30f))
        yawn = wYawn
        var et = target[EYE]
        if (yw > 0.3f) et = 0.08f
        if (snS > 0.2f) et = 0.05f else if (snA > 0.1f) et = 0.45f
        if (now > blinkAt) {
            et = 0.08f
            if (now > blinkAt + 0.14f) blinkAt = now + 2f + Random.nextFloat() * 3f
        }
        s[EYE] += (et - s[EYE]) * (1f - exp(-dt * 28f))

        val wf = 1f - exp(-dt * 8f)
        leaping = leap
        val stalking = st == St.WATCH && flyFollow && abs(flyX - x) > 115f
        wWalk = toward(wWalk, st == St.WALK || st == St.CHASE || stalking, wf)
        wRun = toward(wRun, st == St.CHASE || leaving || (stalking && flyQuick), wf)
        wGroom = toward(wGroom, st == St.GROOM, wf)
        wSleep = toward(wSleep, st == St.SLEEP, wf)
        wCrouch = toward(wCrouch, crouch, wf)
        wPurr = toward(wPurr, st == St.PURR, wf)
        wLove = toward(wLove, st == St.PURR || st == St.RUB || loveT > 0f, wf)
        wEat = toward(wEat, st == St.EAT, wf)
        wRub = toward(wRub, st == St.RUB, wf)
        wHang = toward(wHang, st == St.HANG, wf)
        wCling = toward(wCling, st == St.CLING, 1f - exp(-dt * 20f))
        wSlide = toward(wSlide, st == St.SLIDE, wf)
        wLook = toward(wLook, st == St.WATCH || st == St.REACH || st == St.CLIMB || st == St.SWAT, wf)
        wKnead = toward(wKnead, st == St.KNEAD, wf)
        wWash = toward(wWash, st == St.WASH, wf)
        wSwat = toward(wSwat, st == St.SWAT, wf)
        wBox = toward(wBox, st == St.BOX, wf)
        wCover = toward(wCover, st == St.COVER, wf)
        want = if (st == St.BEG) wantVar else 0
        wReach = toward(wReach, st == St.REACH, wf)
        wClimb = toward(wClimb, st == St.CLIMB, wf)
        // Ángulo de la mirada hacia la luciérnaga (negativo = hacia arriba)
        val up = flyAlt - (alt + 86f)
        val want = (-atan2(up, max(20f, abs(flyX - x))) * 180f / PI.toFloat()).coerceIn(-42f, 24f)
        look += (want - look) * (1f - exp(-dt * 6f))
        wShake = toward(wShake, st == St.SHAKE, 1f - exp(-dt * 14f))
        if (st == St.SHAKE) wet = if (wet > 0.45f) max(0.45f, wet - dt * 0.25f) else wet
        else if (st != St.BATH && wet > 0f) wet = max(0f, wet - dt / 150f)

        if (st == St.WALK) {
            if (!goal.isNaN()) {
                if (abs(goal - x) < 3f) set(afterGoal) else dir = if (goal > x) 1f else -1f
            }
            if (st == St.WALK && leaving) {
                x += dir * 75f * dt
            } else if (st == St.WALK) {
                x += dir * SPEED * dt
                if (x > x1) { x = x1; dir = -1f }
                if (x < x0) { x = x0; dir = 1f }
            }
        }

        if (leap) {
            x = (x + dir * 150f * dt).coerceIn(x0, x1)
            alt = 48f * sin(PI.toFloat() * u)
        } else if (!aloft) {
            alt += (0f - alt) * f
        }

        gait += dt * (8f + 7f * wRun)
        val p = gait
        val sw = sin(p) * 26f * wWalk
        val g = sin(now * 7f) * wGroom
        val rb = sin(now * 5f) * wRub
        val cl = sin(now * 8f) * wClimb
        val swat = 0.7f * wReach + 0.6f * wClimb
        bl1 = s[BL1] + sw + sin(now * 2.2f) * 7f * wHang - cl * 14f
        fl2 = s[FL2] + sw + sin(now * 9f + 2.2f) * 13f * swat - cl * 8f - sin(now * 6f) * 13f * wKnead - sin(now * 11f) * 20f * wCover
        bl2 = s[BL2] - sw - sin(now * 2.2f + 0.6f) * 7f * wHang + cl * 14f
        fl1 = s[FL1] - sw + g * 8f + sin(now * 9f) * 15f * swat + cl * 8f +
            sin(now * 6f) * 13f * wKnead + sin(now * 11f) * 20f * wCover +
            (sin(now * 5f) * 14f - 6f) * wWash +
            (sin(now * 7f) * 22f - 8f) * wSwat
        val bob = -abs(sin(p)) * 1.6f * wWalk + sin(now * 1.8f) * 0.9f * wSleep
        py = s[PY] + bob + 3f * snS
        val shk = sin(now * 38f) * wShake
        torso = s[TORSO] + sin(now * 32f) * 2.5f * wCrouch + rb * 3f + shk * 5f
        head = s[HEAD] + sin(p * 2f) * 2.5f * wWalk + g * 5f +
            sin(now * 3f) * 4f * wPurr + sin(now * 9f) * 4f * wEat + rb * 12f +
            sin(now * 38f + 1.2f) * 14f * wShake + look * wLook +
            (sin(now * 5f + 0.6f) * 8f + 4f) * wWash - 18f * wYawn - 15f * snA + 24f * snS

        // Cajita: se esconde y asoma
        if (st == St.BOX) {
            boxT -= dt
            if (boxT <= 0f) {
                boxPeek = !boxPeek
                boxT = if (boxPeek) 1.6f + Random.nextFloat() * 1.8f else 0.8f + Random.nextFloat()
            }
            boxSink += ((if (boxPeek) 30f else 62f) - boxSink) * (1f - exp(-dt * 9f))
            if (boxPeek) head += sin(now * 3f) * 8f
        }
        sink = boxSink * wBox
        boxA = wBox
        carry = carrying || st == St.DROP
        headX = s[HX] + rb * 4f
        headY = s[HY] + bob
        fs1 = s[FS1]
        fs2 = s[FS2]
        ls = s[LS]
        eye = s[EYE]
        tongue = s[TG]
        sway = sin(now * 2.2f) * 6f * wHang * (1f - wSlide) * (1f - wClimb)
        jitter = sin(now * 62f) * 1.6f * wCling + sin(now * 45f) * 0.8f * wSlide + shk * 2.5f
        shaking = wShake
        stretch = 0f
        wBack = toward(wBack, st == St.REACH, 1f - exp(-dt * 7f))
        turn = wBack
        lookDx = flyX - x
        claws = max(max(wCling, wSlide), wClimb)
        trail = wSlide

        // Cola: cadena de segmentos que sale de la cadera y se apoya en el piso
        val r = torso * PI.toFloat() / 180f
        val c = cos(r)
        val sn = sin(r)
        var px = -20f + (-13f) * c - (-13f) * sn
        var pyy = -34f + (-13f) * sn + (-13f) * c + py
        val gy = -5.5f + alt
        val amp = 14f * (1f - 0.7f * wSleep) + 10f * wCrouch + 8f * wRub - 10f * wCling
        val sp = 2.4f + 10f * wCrouch + 6f * wRub
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
            heartA[q] = wLove * (1f - ph)
        }
        zzX = dir * 34f - 12f
        zzY = -92f - (now * 8f) % 14f
        zzA = wSleep * 0.9f
        bowlA = wEat
    }
}
