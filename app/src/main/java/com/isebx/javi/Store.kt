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
}
