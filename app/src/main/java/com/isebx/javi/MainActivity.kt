package com.isebx.javi

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Choreographer
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import kotlin.math.abs
import kotlin.math.min

/** La "casita" de Javi: vista previa, cuidados y el botón para sacarlo a la pantalla. */
class MainActivity : Activity() {

    private val javi = Javi()
    private lateinit var preview: PetView
    private lateinit var toggle: Button
    private lateinit var info: TextView
    private lateinit var eyes: Button
    private lateinit var collarBtn: Button
    private var lastNs = 0L
    private var looping = false

    private val frame = object : Choreographer.FrameCallback {
        override fun doFrame(ns: Long) {
            if (!looping) return
            val dt = if (lastNs == 0L) 0.016f else min(0.05f, (ns - lastNs) / 1e9f)
            lastNs = ns
            javi.update(dt)
            preview.invalidate()
            Choreographer.getInstance().postFrameCallback(this)
        }
    }

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()

    @SuppressLint("ClickableViewAccessibility", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val brown = 0xFF5A4332.toInt()
        val bg = 0xFFFBF3E7.toInt()
        window.statusBarColor = bg
        window.navigationBarColor = bg
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bg)
            setPadding(dp(24), dp(24), dp(24), dp(24))
            gravity = Gravity.CENTER_HORIZONTAL
            fitsSystemWindows = true
        }

        root.addView(TextView(this).apply {
            text = "Javi"
            textSize = 34f
            setTextColor(brown)
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            gravity = Gravity.CENTER
        })
        root.addView(TextView(this).apply {
            text = "Mantenelo apretado para ver sus acciones. Acarici\u00e1lo de lado a lado. Soltalo sobre un \u00edcono y se cuelga."
            textSize = 15f
            setTextColor(brown)
            gravity = Gravity.CENTER
            setPadding(0, dp(4), 0, dp(8))
        })

        val unit = resources.displayMetrics.density * 1.05f
        preview = PetView(this, javi, unit, false)
        root.addView(
            preview,
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f)
        )
        preview.addOnLayoutChangeListener { _, l, _, r, _, _, _, _, _ ->
            javi.setBounds(Javi.HALF_W, (r - l) / unit - Javi.HALF_W)
        }
        preview.setOnTouchListener { _, e ->
            if (e.actionMasked == MotionEvent.ACTION_UP) {
                val gx = e.x / unit
                if (abs(gx - javi.x) < 45f) {
                    javi.pet()
                    Store.addMimo(this)
                    refresh()
                } else {
                    javi.goTo(gx)
                }
            }
            true
        }

        info = TextView(this).apply {
            textSize = 15f
            setTextColor(brown)
            gravity = Gravity.CENTER
            setPadding(0, dp(8), 0, dp(12))
        }
        root.addView(info)

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        fun small(label: String, action: () -> Unit): Button = Button(this).apply {
            text = label
            textSize = 13f
            setAllCaps(false)
            setOnClickListener { action() }
        }
        fun weight() = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        row.addView(small("Comida") {
            AlertDialog.Builder(this)
                .setTitle("¿Qué le damos?")
                .setItems(Props.FOOD_NAMES) { _, i -> feed(i) }
                .show()
        }, weight())
        row.addView(small("Agua") { feed(Props.AGUA) }, weight())
        row.addView(small("Mimos") {
            javi.pet()
            Store.addMimo(this)
            send(PetService.ACTION_PET)
            refresh()
        }, weight())
        row.addView(small("Jugar") {
            javi.play()
            send(PetService.ACTION_PLAY)
        }, weight())
        root.addView(row)

        toggle = Button(this).apply {
            setAllCaps(false)
            textSize = 17f
            setOnClickListener { onToggle() }
        }
        root.addView(
            toggle,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dp(12) }
        )

        collarBtn = Button(this).apply {
            setAllCaps(false)
            textSize = 14f
            setOnClickListener {
                AlertDialog.Builder(this@MainActivity)
                    .setTitle("Collar")
                    .setItems(Store.COLLAR_NAMES) { _, i ->
                        Store.setCollarIndex(this@MainActivity, i)
                        send(PetService.ACTION_REFRESH)
                        refresh()
                    }
                    .show()
            }
        }
        root.addView(
            collarBtn,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        eyes = Button(this).apply {
            setAllCaps(false)
            textSize = 14f
            setOnClickListener {
                Toast.makeText(
                    this@MainActivity,
                    "Buscá \"Javi\" en la lista (a veces dentro de \"Apps descargadas\") y activalo",
                    Toast.LENGTH_LONG
                ).show()
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
        }
        val hasEyes = try {
            packageManager.getServiceInfo(ComponentName(this, JaviEyes::class.java), 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
        if (!hasEyes) eyes.visibility = View.GONE
        root.addView(
            eyes,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        setContentView(root)
    }

    override fun onResume() {
        super.onResume()
        refresh()
        looping = true
        lastNs = 0L
        Choreographer.getInstance().postFrameCallback(frame)
    }

    override fun onPause() {
        looping = false
        Choreographer.getInstance().removeFrameCallback(frame)
        super.onPause()
    }

    @SuppressLint("SetTextI18n")
    private fun refresh() {
        toggle.text = if (PetService.running) "Guardar a Javi" else "Sacar a Javi a la pantalla"
        javi.collar = Store.collar(this)
        collarBtn.text = "Collar: " + Store.COLLAR_NAMES[Store.collarIndex(this)]
        Care.load(this)
        info.text = "${Care.mood}\n" +
            "Comida ${(Care.food * 100).toInt()}%  ·  Diversión ${(Care.play * 100).toInt()}%  ·  " +
            "Limpieza ${(Care.clean * 100).toInt()}%\n" +
            "Mimos: ${Store.mimos(this)}  ·  Regalitos: ${Store.regalos(this)}"
        val enabled = Settings.Secure.getString(
            contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: ""
        eyes.text = if (enabled.contains("$packageName/")) {
            "Javi ya ve los íconos"
        } else {
            "Dejar que Javi vea los íconos"
        }
    }

    private fun send(action: String, kind: Int = -1) {
        if (PetService.running) {
            startService(
                Intent(this, PetService::class.java).setAction(action)
                    .putExtra(PetService.EXTRA_KIND, kind)
            )
        }
    }

    private fun feed(kind: Int) {
        javi.feed(kind)
        if (!PetService.running) {
            if (kind == Props.AGUA) Care.drink() else Care.feed(kind == Props.LECHE)
            Care.save(this)
        }
        if (kind != Props.AGUA) Store.addComida(this)
        send(PetService.ACTION_FEED, kind)
        refresh()
    }

    private fun onToggle() {
        if (PetService.running) {
            stopService(Intent(this, PetService::class.java))
            toggle.postDelayed({ refresh() }, 300)
            return
        }
        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(
                this,
                "Activá el permiso para que Javi pueda caminar sobre la pantalla",
                Toast.LENGTH_LONG
            ).show()
            startActivity(
                Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
            )
            return
        }
        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 7)
        }
        startForegroundService(Intent(this, PetService::class.java))
        toggle.postDelayed({ refresh() }, 400)
    }
}
