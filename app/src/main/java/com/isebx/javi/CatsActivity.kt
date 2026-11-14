package com.isebx.javi

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

/** La lista de gatos: elegir cuál sale a la pantalla, editarlos o crear uno nuevo. */
class CatsActivity : Activity() {

    private lateinit var col: LinearLayout

    private fun dp(v: Float): Int = Ui.dp(this, v)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Ui.CREAM
        window.navigationBarColor = Ui.CREAM
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Ui.CREAM)
            setPadding(dp(16f), dp(16f), dp(16f), dp(12f))
            fitsSystemWindows = true
        }
        root.addView(TextView(this).apply {
            text = "Mis gatos"
            textSize = 28f
            setTextColor(Ui.BROWN)
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            setPadding(0, 0, 0, dp(8f))
        })
        col = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val scroll = ScrollView(this)
        scroll.addView(col)
        root.addView(scroll, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f))
        root.addView(Button(this).apply {
            text = "+ Nuevo gato"
            setAllCaps(false)
            textSize = 16f
            setOnClickListener { pickTemplate() }
        }, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = dp(8f) })
        setContentView(root)
    }

    override fun onResume() {
        super.onResume()
        fill()
    }

    @SuppressLint("SetTextI18n")
    private fun fill() {
        col.removeAllViews()
        val active = CatStore.activeId(this)
        val unit = resources.displayMetrics.density * 0.5f
        val cats = CatStore.all(this)
        val many = cats.size > 1
        for (cat in cats) {
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(dp(8f), dp(8f), dp(10f), dp(8f))
                val bg = GradientDrawable()
                bg.cornerRadius = dp(16f).toFloat()
                bg.setColor(Ui.CARD)
                if (cat.id == active) bg.setStroke(dp(2f), Ui.BROWN)
                background = bg
            }
            row.addView(CatThumb(this, cat.look, unit), LinearLayout.LayoutParams(dp(92f), dp(86f)))
            val texts = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(8f), 0, dp(8f), 0)
            }
            texts.addView(TextView(this).apply {
                text = cat.look.name
                textSize = 18f
                setTextColor(Ui.BROWN)
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            })
            texts.addView(TextView(this).apply {
                text = if (cat.id == active) "Es el que sale a la pantalla" else "Guardado"
                textSize = 13f
                setTextColor(0xFF8A7460.toInt())
            })
            row.addView(texts, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
            val buttons = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
            if (cat.id != active) {
                buttons.addView(Button(this).apply {
                    text = "Elegir"
                    setAllCaps(false)
                    textSize = 13f
                    setOnClickListener {
                        CatStore.setActive(this@CatsActivity, cat.id)
                        if (PetService.running) {
                            startService(
                                Intent(this@CatsActivity, PetService::class.java)
                                    .setAction(PetService.ACTION_REFRESH)
                            )
                        }
                        fill()
                    }
                })
            }
            buttons.addView(Button(this).apply {
                text = "Editar"
                setAllCaps(false)
                textSize = 13f
                setOnClickListener {
                    startActivity(
                        Intent(this@CatsActivity, EditorActivity::class.java)
                            .putExtra(EditorActivity.EXTRA_ID, cat.id)
                    )
                }
            })
            if (many) {
                buttons.addView(Button(this).apply {
                    text = "Quitar"
                    setAllCaps(false)
                    textSize = 13f
                    setOnClickListener { confirmRemove(cat) }
                })
            }
            row.addView(buttons)
            col.addView(row, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(10f) })
        }
    }

    private fun confirmRemove(cat: CatStore.Cat) {
        AlertDialog.Builder(this)
            .setTitle("¿Quitar a ${cat.look.name} de la lista?")
            .setMessage("Deja de aparecer acá. No se puede deshacer.")
            .setPositiveButton("Quitar") { _, _ ->
                CatStore.remove(this, cat.id)
                if (PetService.running) {
                    startService(Intent(this, PetService::class.java).setAction(PetService.ACTION_REFRESH))
                }
                fill()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    /** Un gato nuevo arranca desde una plantilla y después se retoca todo. */
    private fun pickTemplate() {
        val presets = Looks.presets()
        val names = Array(presets.size + 1) { i -> if (i == 0) "Parecido a Javi" else presets[i - 1].name }
        AlertDialog.Builder(this)
            .setTitle("¿A cuál se parece?")
            .setItems(names) { _, i ->
                startActivity(
                    Intent(this, EditorActivity::class.java)
                        .putExtra(EditorActivity.EXTRA_ID, -1L)
                        .putExtra(EditorActivity.EXTRA_TEMPLATE, i - 1)
                )
            }
            .show()
    }
}
