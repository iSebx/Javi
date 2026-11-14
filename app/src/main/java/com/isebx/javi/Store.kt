package com.isebx.javi

import android.content.Context

/** Recuerdos chiquitos que se guardan en el teléfono. */
object Store {
    private const val FILE = "javi"
    private const val MIMOS = "mimos"
    private const val COMIDAS = "comidas"

    private fun prefs(c: Context) = c.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun mimos(c: Context): Int = prefs(c).getInt(MIMOS, 0)

    fun addMimo(c: Context) {
        prefs(c).edit().putInt(MIMOS, mimos(c) + 1).apply()
    }

    fun comidas(c: Context): Int = prefs(c).getInt(COMIDAS, 0)

    fun addComida(c: Context) {
        prefs(c).edit().putInt(COMIDAS, comidas(c) + 1).apply()
    }

    fun regalos(c: Context): Int = prefs(c).getInt("regalos", 0)

    fun addRegalo(c: Context) {
        prefs(c).edit().putInt("regalos", regalos(c) + 1).apply()
    }

    /** Collares: es optativo y se elige el color. El negro es en su recuerdo. */
    val COLLAR_NAMES = arrayOf("sin collar", "negro", "rojo", "azul", "verde", "rosa", "violeta")
    private val COLLAR_COLORS = intArrayOf(
        0, 0xFF1F1B1A.toInt(), 0xFFC8443C.toInt(), 0xFF3F73C8.toInt(),
        0xFF3F9B63.toInt(), 0xFFE07AA0.toInt(), 0xFF7E5BC2.toInt()
    )

    /** Cuál lleva puesto (posición en COLLAR_NAMES). Javi viene con el negro. */
    fun collarIndex(c: Context): Int =
        prefs(c).getInt("collarColor", 1).coerceIn(0, COLLAR_COLORS.size - 1)

    fun setCollarIndex(c: Context, i: Int) {
        prefs(c).edit().putInt("collarColor", i.coerceIn(0, COLLAR_COLORS.size - 1)).apply()
    }

    /** Color del collar puesto (ARGB), o 0 si no lleva. */
    fun collar(c: Context): Int = COLLAR_COLORS[collarIndex(c)]

    const val SIZE_MIN = 0.35f
    const val SIZE_MAX = 0.95f
    const val SIZE_STEP = 0.07f

    /** Tamaño de Javi en pantalla (multiplicador de la densidad). */
    fun size(c: Context): Float = prefs(c).getFloat("size", 0.55f)

    fun setSize(c: Context, v: Float) {
        prefs(c).edit().putFloat("size", v.coerceIn(SIZE_MIN, SIZE_MAX)).apply()
    }
}
