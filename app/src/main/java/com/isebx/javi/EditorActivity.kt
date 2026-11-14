package com.isebx.javi

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Choreographer
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import kotlin.math.min

/** El editor: armar un gato a gusto, viéndolo moverse mientras se lo cambia. */
class EditorActivity : Activity() {

    companion object {
        const val EXTRA_ID = "id"
        const val EXTRA_TEMPLATE = "template"

        private val FUR = intArrayOf(
            0xFFFBF6EE.toInt(), 0xFFF4E3C6.toInt(), 0xFFF3E6D2.toInt(), 0xFFF0C595.toInt(),
            0xFFEBA862.toInt(), 0xFFD98A42.toInt(), 0xFFB5764A.toInt(), 0xFF6B4A36.toInt(),
            0xFF9C8062.toInt(), 0xFFD9C2A0.toInt(), 0xFFDBA660.toInt(), 0xFFB9AEB5.toInt(),
            0xFF8F9BA7.toInt(), 0xFF9AA3AD.toInt(), 0xFF6B7178.toInt(), 0xFFA9A08F.toInt(),
            0xFF3A3536.toInt(), 0xFFE9C6B9.toInt()
        )
        private val MARKS = intArrayOf(
            0xFF2A2627.toInt(), 0xFF4F3C2A.toInt(), 0xFF5A5146.toInt(), 0xFF5F3D20.toInt(),
            0xFF73808C.toInt(), 0xFFC97D38.toInt(), 0xFFE2B181.toInt(), 0xFFE39A4E.toInt(),
            0xFFE9B06A.toInt(), 0xFFE2CDA6.toInt(), 0xFFC49A8C.toInt(), 0xFFFBF6EE.toInt()
        )
        private val POINTS = intArrayOf(
            0xFF5A4336.toInt(), 0xFF7A5A48.toInt(), 0xFF8B8F99.toInt(), 0xFFB9AEB5.toInt(),
            0xFFD98A42.toInt(), 0xFFE9C9A0.toInt(), 0xFF3A3536.toInt()
        )
        private val EYES = intArrayOf(
            0xFFE0A030.toInt(), 0xFFE6C34A.toInt(), 0xFFC8742C.toInt(), 0xFFD98A32.toInt(),
            0xFFB5A04A.toInt(), 0xFF7FB069.toInt(), 0xFF3F9B63.toInt(), 0xFF6FC7C0.toInt(),
            0xFF5AA9E6.toInt(), 0xFF3F73C8.toInt(), 0xFF3A2418.toInt()
        )
        private val NOSES = intArrayOf(
            0xFFE79A94.toInt(), 0xFFB56F66.toInt(), 0xFFC9A39C.toInt(), 0xFF5F6A75.toInt(),
            0xFF3F2E26.toInt(), 0xFF2A2627.toInt()
        )
        private val EARS_IN = intArrayOf(
            0xFFEFB3A8.toInt(), 0xFFE0A79D.toInt(), 0xFFC9B3B0.toInt(), 0xFFB9A8AE.toInt(),
            0xFF7A6460.toInt(), 0xFF6B5A5C.toInt()
        )
        private val WHISKERS = intArrayOf(
            0xFFFFFFFF.toInt(), 0xFFE9DFD0.toInt(), 0xFFBDB6B7.toInt(), 0xFF3A3536.toInt()
        )
        private val COLLARS = intArrayOf(
            0xFF1F1B1A.toInt(), 0xFFC8443C.toInt(), 0xFF3F73C8.toInt(), 0xFF3F9B63.toInt(),
            0xFFE07AA0.toInt(), 0xFF7E5BC2.toInt(), 0xFFE0A030.toInt()
        )
    }

    private lateinit var look: CatLook
    private var catId = -1L
    private val javi = Javi()
    private lateinit var preview: PetView
    private var forced = 0
    private var lastNs = 0L
    private var looping = false

    private val frame = object : Choreographer.FrameCallback {
        override fun doFrame(ns: Long) {
            if (!looping) return
            val dt = if (lastNs == 0L) 0.016f else min(0.05f, (ns - lastNs) / 1e9f)
            lastNs = ns
            // Mantiene la pose elegida para poder mirar bien los cambios
            when (forced) {
                0 -> if (javi.st != Javi.St.SIT) javi.debugSet(Javi.St.SIT)
                1 -> if (javi.st != Javi.St.WALK) javi.debugSet(Javi.St.WALK)
                2 -> {
                    if (javi.st == Javi.St.SLEEP) javi.debugSet(Javi.St.SIT)
                    javi.attend(javi.x + 8f, 160f, 500f)
                }
                3 -> if (javi.st != Javi.St.SLEEP) javi.debugSet(Javi.St.SLEEP)
            }
            javi.update(dt)
            preview.invalidate()
            Choreographer.getInstance().postFrameCallback(this)
        }
    }

    private fun dp(v: Float): Int = Ui.dp(this, v)

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Ui.CREAM
        window.navigationBarColor = Ui.CREAM
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR

        catId = intent.getLongExtra(EXTRA_ID, -1L)
        val template = intent.getIntExtra(EXTRA_TEMPLATE, -1)
        look = if (catId >= 0) {
            (CatStore.all(this).firstOrNull { it.id == catId }?.look ?: CatLook.javi()).copy()
        } else {
            val presets = Looks.presets()
            val base = if (template in presets.indices) presets[template] else CatLook.javi()
            base.copy().apply { if (template < 0) collar = 0; name = "" }
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Ui.CREAM)
            fitsSystemWindows = true
        }

        // Vista previa en vivo
        val unit = resources.displayMetrics.density * 0.95f
        preview = PetView(this, javi, unit, false, look)
        preview.setBackgroundColor(Ui.CARD)
        root.addView(preview, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(205f)))
        preview.addOnLayoutChangeListener { _, l, _, r, _, _, _, _, _ ->
            val w = (r - l) / unit
            javi.setBounds(w * 0.3f, w * 0.7f)
        }
        val poseRow = LinearLayout(this).apply { setPadding(dp(16f), dp(8f), dp(16f), 0) }
        poseRow.addView(Ui.chips(this, arrayOf("Sentado", "Caminando", "De espaldas", "Durmiendo"), 0) { forced = it })
        root.addView(poseRow)

        val col = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16f), 0, dp(16f), dp(16f))
        }
        val scroll = ScrollView(this)
        scroll.addView(col)
        root.addView(scroll, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f))

        fun section(text: String, v: View) {
            col.addView(Ui.title(this, text))
            col.addView(v)
        }

        // Nombre
        val nameBox = EditText(this).apply {
            setText(look.name)
            hint = "¿Cómo se llama?"
            setSingleLine(true)
            setTextColor(Ui.BROWN)
            addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) { look.name = s?.toString()?.trim() ?: "" }
                override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
                override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            })
        }
        section("Nombre", nameBox)

        section("Color del pelaje", Ui.palette(this, FUR, look.base, null) {
            look.base = it
            look.autoShades()
        })
        section("Patrón", Ui.chips(
            this, arrayOf("Liso", "Rayitas suaves", "Atigrado", "Atigrado clásico", "Moteado"),
            look.pattern.coerceIn(0, 4)
        ) { look.pattern = it })
        section("Color de las marcas", Ui.palette(this, MARKS, look.dark, null) { look.dark = it })
        section("Color de panza y hocico", Ui.palette(this, FUR, look.light, null) { look.light = it })
        section("Blanco", Ui.chips(
            this, arrayOf("Nada", "Pecho", "Pecho y guantes", "Esmoquin", "Bicolor"), look.white
        ) { look.white = it })
        section("Manchas", Ui.chips(
            this, arrayOf("Ninguna", "Vaquita", "Calicó", "Carey"), look.patchStyle
        ) {
            look.patchStyle = it
            look.rebuildPatches()
        })
        section("Color de mancha 1", Ui.palette(this, FUR, look.patchA, null) {
            look.patchA = it
            look.rebuildPatches()
        })
        section("Color de mancha 2", Ui.palette(this, FUR, look.patchB, null) {
            look.patchB = it
            look.rebuildPatches()
        })
        section("Puntas de color (cara, orejas, patas y cola)", Ui.palette(this, POINTS, look.point, "No") {
            look.point = it
        })

        section("Pelo", Ui.chips(
            this, arrayOf("Corto", "Semilargo", "Largo", "Sin pelo", "Rizado"), look.hair
        ) { look.hair = it })
        section("Cabeza", Ui.chips(
            this, arrayOf("Redonda", "Triangular", "Chata", "Cachetona"), look.head
        ) { look.head = it })
        section("Orejas", Ui.chips(
            this, arrayOf("Normales", "Grandes", "Chicas", "Dobladas", "Curvadas", "Con pinceles"), look.ears
        ) { look.ears = it })
        section("Cola", Ui.chips(
            this, arrayOf("Larga", "Fina", "Esponjosa", "Corta", "Sin cola"), look.tail
        ) { look.tail = it })
        section("Punta de la cola", Ui.chips(
            this, arrayOf("Del color de las marcas", "Igual al cuerpo"), if (look.tailTip) 0 else 1
        ) { look.tailTip = it == 0 })

        section("Cuerpo: de flaquito a gordito", Ui.slider(this, 0.82f, 1.25f, look.bodyW) { look.bodyW = it })
        section("Cuerpo: de corto a largo", Ui.slider(this, 0.9f, 1.12f, look.bodyL) { look.bodyL = it })
        section("Tamaño de la cabeza", Ui.slider(this, 0.9f, 1.14f, look.headSize) { look.headSize = it })

        section("Ojo izquierdo", Ui.palette(this, EYES, look.eyeL, null) { look.eyeL = it })
        section("Ojo derecho", Ui.palette(this, EYES, look.eyeR, null) { look.eyeR = it })
        section("Nariz", Ui.palette(this, NOSES, look.nose, null) { look.nose = it })
        section("Interior de las orejas", Ui.palette(this, EARS_IN, look.innerEar, null) { look.innerEar = it })
        section("Bigotes", Ui.palette(this, WHISKERS, look.whiskers, null) { look.whiskers = it })
        section("Cachetes rosados", Ui.chips(this, arrayOf("Sí", "No"), if (look.blush) 0 else 1) {
            look.blush = it == 0
        })
        section("Collar", Ui.palette(this, COLLARS, look.collar, "Sin collar") { look.collar = it })

        // Guardar / eliminar
        val bar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(dp(16f), dp(6f), dp(16f), dp(10f))
        }
        if (catId >= 0 && CatStore.all(this).size > 1) {
            bar.addView(Button(this).apply {
                text = "Eliminar"
                setAllCaps(false)
                setOnClickListener { confirmDelete() }
            }, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        }
        bar.addView(Button(this).apply {
            text = "Guardar"
            setAllCaps(false)
            textSize = 16f
            setOnClickListener { save() }
        }, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2f))
        root.addView(bar)

        setContentView(root)
    }

    private fun save() {
        if (look.name.isBlank()) look.name = "Gatito"
        val id = if (catId >= 0) catId else CatStore.newId(this)
        CatStore.put(this, CatStore.Cat(id, look))
        if (PetService.running) {
            startService(Intent(this, PetService::class.java).setAction(PetService.ACTION_REFRESH))
        }
        finish()
    }

    private fun confirmDelete() {
        AlertDialog.Builder(this)
            .setTitle("¿Eliminar a ${look.name}?")
            .setMessage("Se borra de la lista. No se puede deshacer.")
            .setPositiveButton("Eliminar") { _, _ ->
                CatStore.remove(this, catId)
                if (PetService.running) {
                    startService(Intent(this, PetService::class.java).setAction(PetService.ACTION_REFRESH))
                }
                finish()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        looping = true
        lastNs = 0L
        Choreographer.getInstance().postFrameCallback(frame)
    }

    override fun onPause() {
        looping = false
        Choreographer.getInstance().removeFrameCallback(frame)
        super.onPause()
    }
}
