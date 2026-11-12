package com.isebx.javi

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

/**
 * Los "ojos" de Javi: le dicen dónde están los íconos del escritorio y dónde
 * tocó la persona. Solo mira posiciones; no lee textos, no guarda ni envía nada.
 */
class JaviEyes : AccessibilityService() {

    companion object {
        /** Rectángulos (en píxeles de pantalla) de los íconos visibles del escritorio. */
        @Volatile
        var icons: List<Rect> = emptyList()

        @Volatile
        var connected = false
    }

    private val handler = Handler(Looper.getMainLooper())
    private var homePkg: String? = null
    private var onHome = false
    private val scan = Runnable { scanIcons() }

    override fun onServiceConnected() {
        super.onServiceConnected()
        connected = true
        val home = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        homePkg = packageManager
            .resolveActivity(home, PackageManager.MATCH_DEFAULT_ONLY)
            ?.activityInfo?.packageName
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val e = event ?: return
        val pkg = e.packageName?.toString() ?: return
        if (pkg == packageName) return
        when (e.eventType) {
            AccessibilityEvent.TYPE_VIEW_CLICKED,
            AccessibilityEvent.TYPE_VIEW_LONG_CLICKED -> {
                val src = e.source ?: return
                val r = Rect()
                src.getBoundsInScreen(r)
                if (!r.isEmpty) {
                    PetService.instance?.onScreenTap(r.exactCenterX(), r.exactCenterY())
                }
            }
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                if (pkg == "com.android.systemui") return
                onHome = pkg == homePkg
                if (!onHome) icons = emptyList()
                schedule(350L)
            }
            AccessibilityEvent.TYPE_VIEW_SCROLLED -> {
                if (onHome && pkg == homePkg) schedule(500L)
            }
        }
    }

    private fun schedule(delay: Long) {
        handler.removeCallbacks(scan)
        handler.postDelayed(scan, delay)
    }

    private fun scanIcons() {
        if (!onHome) {
            icons = emptyList()
            return
        }
        val root = try {
            rootInActiveWindow
        } catch (e: Exception) {
            null
        }
        if (root == null || root.packageName?.toString() != homePkg) {
            icons = emptyList()
            return
        }
        val sw = resources.displayMetrics.widthPixels
        val out = ArrayList<Rect>()
        val budget = intArrayOf(700)
        collect(root, sw, out, budget, 0)
        icons = out
    }

    private fun collect(
        node: AccessibilityNodeInfo?,
        sw: Int,
        out: ArrayList<Rect>,
        budget: IntArray,
        depth: Int
    ) {
        if (node == null || depth > 25 || budget[0] <= 0 || out.size >= 48) return
        budget[0]--
        if (node.isVisibleToUser && node.isClickable) {
            val named = !node.text.isNullOrBlank() || !node.contentDescription.isNullOrBlank()
            if (named) {
                val r = Rect()
                node.getBoundsInScreen(r)
                val w = r.width()
                val h = r.height()
                if (w >= sw * 0.10f && w <= sw * 0.32f && h >= sw * 0.10f && h <= sw * 0.40f) {
                    out.add(r)
                    return
                }
            }
        }
        for (i in 0 until node.childCount) {
            val child = try {
                node.getChild(i)
            } catch (e: Exception) {
                null
            }
            collect(child, sw, out, budget, depth + 1)
        }
    }

    override fun onInterrupt() {
        // nada que interrumpir
    }

    override fun onUnbind(intent: Intent?): Boolean {
        connected = false
        icons = emptyList()
        handler.removeCallbacks(scan)
        return super.onUnbind(intent)
    }
}
