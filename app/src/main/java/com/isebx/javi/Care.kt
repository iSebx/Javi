package com.isebx.javi

import android.content.Context
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

/**
 * El cuidado de Javi: comida, diversión y limpieza. Bajan despacio con las
 * horas y tienen un piso: nunca llegan a cero ni le pasa nada malo; lo peor
 * que ocurre es que Javi pide lo que le falta.
 */
object Care {
    const val FLOOR = 0.15f
    const val WANT_NONE = 0
    const val WANT_FOOD = 1
    const val WANT_PLAY = 2
    const val WANT_BATH = 3

    var food = 0.8f
        private set
    var play = 0.7f
        private set
    var clean = 0.9f
        private set

    private var water = 0.4f
    private var meals = 0
    private var poopIn = -1f
    private var loaded = false

    /** Pasa el tiempo (en segundos). */
    fun tick(sec: Float) {
        food = max(FLOOR, food - sec * 0.85f / (12f * 3600f))
        play = max(FLOOR, play - sec * 0.85f / (8f * 3600f))
        clean = max(FLOOR, clean - sec * 0.85f / (36f * 3600f))
        water = max(0f, water - sec / (6f * 3600f))
    }

    /** Cuenta regresiva de la caca; solo corre con Javi en pantalla. */
    fun tickAwake(sec: Float) {
        if (poopIn > 0f) poopIn = max(0f, poopIn - sec)
    }

    fun feed(milk: Boolean) {
        food = min(1f, food + 0.3f)
        meals++
        if (milk) water = min(1f, water + 0.25f)
        checkPoop()
    }

    fun drink() {
        water = min(1f, water + 0.5f)
        checkPoop()
    }

    /** Comió bastante y está hidratado: en un rato va a hacer caca. */
    private fun checkPoop() {
        if (poopIn < 0f && meals >= 2 && water >= 0.5f) poopIn = 120f + Random.nextFloat() * 150f
    }

    val poopDue: Boolean
        get() = poopIn == 0f

    fun pooped() {
        meals = 0
        water = max(0f, water - 0.3f)
        poopIn = -1f
    }

    /** Jugó: se divierte y se ensucia un poquito. */
    fun played(amount: Float) {
        play = min(1f, play + amount)
        clean = max(FLOOR, clean - amount * 0.12f)
    }

    fun loved(amount: Float) {
        play = min(1f, play + amount)
    }

    /** Se lavó solo: ayuda, pero muy poquito. */
    fun groomed() {
        clean = min(1f, clean + 0.03f)
    }

    /** Lo bañaron. quality 1 = enjabonado y enjuagado del todo. */
    fun bathed(quality: Float) {
        clean = if (quality >= 0.99f) 1f else min(1f, clean + quality * 0.5f)
    }

    fun poopLeft() {
        clean = max(FLOOR, clean - 0.1f)
    }

    fun poopCleaned() {
        clean = min(1f, clean + 0.04f)
    }

    /** Qué tan sucio se ve (0 limpio, 1 muy sucio). */
    val dirt: Float
        get() = ((0.5f - clean) / 0.35f).coerceIn(0f, 1f)

    /** Lo que más le está faltando, si hay algo bajo. */
    val want: Int
        get() {
            val low = min(food, min(play, clean))
            if (low >= 0.35f) return WANT_NONE
            return when (low) {
                food -> WANT_FOOD
                play -> WANT_PLAY
                else -> WANT_BATH
            }
        }

    val happy: Boolean
        get() = food > 0.7f && play > 0.7f && clean > 0.7f

    val mood: String
        get() = when (want) {
            WANT_FOOD -> "Tiene hambre"
            WANT_PLAY -> "Quiere jugar"
            WANT_BATH -> "Necesita un baño"
            else -> if (happy) "Está feliz" else "Está contento"
        }

    private fun prefs(c: Context) = c.getSharedPreferences("javi_care", Context.MODE_PRIVATE)

    /** Carga lo guardado y descuenta el tiempo que pasó desde entonces. */
    fun load(c: Context) {
        val p = prefs(c)
        if (!loaded) {
            food = p.getFloat("food", food)
            play = p.getFloat("play", play)
            clean = p.getFloat("clean", clean)
            water = p.getFloat("water", water)
            meals = p.getInt("meals", meals)
            poopIn = p.getFloat("poopIn", poopIn)
            loaded = true
        }
        val last = p.getLong("last", 0L)
        val now = System.currentTimeMillis()
        if (last in 1 until now) {
            tick(min((now - last) / 1000f, 14f * 24f * 3600f))
        }
        save(c)
    }

    fun save(c: Context) {
        prefs(c).edit()
            .putFloat("food", food).putFloat("play", play).putFloat("clean", clean)
            .putFloat("water", water).putInt("meals", meals).putFloat("poopIn", poopIn)
            .putLong("last", System.currentTimeMillis())
            .apply()
    }
}
