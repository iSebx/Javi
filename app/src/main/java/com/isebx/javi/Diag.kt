package com.isebx.javi

import android.app.Activity
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.util.Log

/** Guarda el último error para poder verlo y copiarlo desde la casita. */
object Diag {
    private var installed = false

    private fun prefs(c: Context) = c.getSharedPreferences("javi_diag", Context.MODE_PRIVATE)

    fun install(c: Context) {
        if (installed) return
        installed = true
        val app = c.applicationContext
        val before = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            try {
                note(app, "Cierre inesperado", e)
            } catch (x: Throwable) {
                // sin nada más que hacer
            }
            before?.uncaughtException(t, e)
        }
    }

    fun note(c: Context, what: String, e: Throwable?) {
        val ver = try {
            c.packageManager.getPackageInfo(c.packageName, 0).versionName
        } catch (x: Exception) {
            "?"
        }
        val text = "Javi $ver · Android ${Build.VERSION.RELEASE} · ${Build.MODEL}\n" +
            what + "\n" + (if (e != null) Log.getStackTraceString(e) else "")
        prefs(c).edit().putString("last", text.take(6000)).commit()
    }

    /** Si quedó un error guardado, lo muestra una vez con la opción de copiarlo. */
    fun show(a: Activity) {
        val text = prefs(a).getString("last", null) ?: return
        prefs(a).edit().remove("last").apply()
        AlertDialog.Builder(a)
            .setTitle("Algo falló la última vez")
            .setMessage(text.take(1500))
            .setPositiveButton("Copiar") { _, _ ->
                val cm = a.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                cm.setPrimaryClip(ClipData.newPlainText("Javi", text))
            }
            .setNegativeButton("Cerrar", null)
            .show()
    }
}
