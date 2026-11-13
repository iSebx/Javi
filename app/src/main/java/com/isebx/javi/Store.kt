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

    const val SIZE_MIN = 0.35f
    const val SIZE_MAX = 0.95f
    const val SIZE_STEP = 0.07f

    /** Tamaño de Javi en pantalla (multiplicador de la densidad). */
    fun size(c: Context): Float = prefs(c).getFloat("size", 0.55f)

    fun setSize(c: Context, v: Float) {
        prefs(c).edit().putFloat("size", v.coerceIn(SIZE_MIN, SIZE_MAX)).apply()
    }
}
