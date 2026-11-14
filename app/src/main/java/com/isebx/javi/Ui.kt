package com.isebx.javi

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView

/** Piezas de interfaz hechas a mano para la casita y el editor. */
object Ui {
    const val BROWN = 0xFF5A4332.toInt()
    const val CREAM = 0xFFFBF3E7.toInt()
    const val CARD = 0xFFFFFAF2.toInt()

    fun dp(c: Context, v: Float): Int = (v * c.resources.displayMetrics.density).toInt()

    fun title(c: Context, text: String): TextView = TextView(c).apply {
        this.text = text
        textSize = 15f
        setTextColor(BROWN)
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        setPadding(0, dp(c, 14f), 0, dp(c, 6f))
    }

    private fun chipLook(c: Context, v: TextView, on: Boolean) {
        val bg = GradientDrawable()
        bg.cornerRadius = dp(c, 18f).toFloat()
        bg.setColor(if (on) BROWN else CARD)
        bg.setStroke(dp(c, 1.5f), BROWN)
        v.background = bg
        v.setTextColor(if (on) Color.WHITE else BROWN)
    }

    /** Fila de opciones para elegir una. */
    fun chips(c: Context, labels: Array<String>, selected: Int, onPick: (Int) -> Unit): View {
        val row = LinearLayout(c).apply { orientation = LinearLayout.HORIZONTAL }
        val views = ArrayList<TextView>()
        labels.forEachIndexed { i, label ->
            val v = TextView(c).apply {
                text = label
                textSize = 14f
                setPadding(dp(c, 14f), dp(c, 8f), dp(c, 14f), dp(c, 8f))
            }
            chipLook(c, v, i == selected)
            v.setOnClickListener {
                views.forEachIndexed { j, o -> chipLook(c, o, j == i) }
                onPick(i)
            }
            views.add(v)
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            lp.rightMargin = dp(c, 8f)
            row.addView(v, lp)
        }
        return HorizontalScrollView(c).apply {
            isHorizontalScrollBarEnabled = false
            addView(row)
        }
    }

    private fun dot(c: Context, v: View, color: Int, on: Boolean) {
        val bg = GradientDrawable()
        bg.shape = GradientDrawable.OVAL
        bg.setColor(color)
        bg.setStroke(dp(c, if (on) 4f else 1.5f), if (on) BROWN else 0xFFBFAF9A.toInt())
        v.background = bg
    }

    /**
     * Fila de colores. Si none != null, la primera opción es "ninguno" (devuelve 0).
     * El último círculo abre un selector para armar un color a medida.
     */
    fun palette(c: Context, colors: IntArray, selected: Int, none: String?, onPick: (Int) -> Unit): View {
        val row = LinearLayout(c).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val size = dp(c, 38f)
        val dots = ArrayList<View>()
        val values = ArrayList<Int>()
        var noneView: TextView? = null
        var custom = if (selected != 0 && !colors.contains(selected)) selected else 0xFFC0A080.toInt()

        fun mark(value: Int) {
            dots.forEachIndexed { i, d -> dot(c, d, values[i], values[i] == value) }
            noneView?.let { chipLook(c, it, value == 0) }
        }

        if (none != null) {
            val v = TextView(c).apply {
                text = none
                textSize = 14f
                setPadding(dp(c, 14f), dp(c, 8f), dp(c, 14f), dp(c, 8f))
            }
            v.setOnClickListener {
                mark(0)
                onPick(0)
            }
            noneView = v
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            lp.rightMargin = dp(c, 10f)
            row.addView(v, lp)
        }
        for (col in colors) {
            val v = View(c)
            values.add(col)
            dots.add(v)
            v.setOnClickListener {
                mark(col)
                onPick(col)
            }
            val lp = LinearLayout.LayoutParams(size, size)
            lp.rightMargin = dp(c, 10f)
            row.addView(v, lp)
        }
        // Color a medida
        val plus = TextView(c).apply {
            text = "+"
            textSize = 20f
            gravity = Gravity.CENTER
            setTextColor(BROWN)
        }
        val customDot = View(c)
        values.add(custom)
        dots.add(customDot)
        val wrap = android.widget.FrameLayout(c)
        wrap.addView(customDot, android.widget.FrameLayout.LayoutParams(size, size))
        wrap.addView(plus, android.widget.FrameLayout.LayoutParams(size, size))
        wrap.setOnClickListener {
            colorDialog(c, custom) { picked ->
                custom = picked
                values[values.size - 1] = picked
                mark(picked)
                onPick(picked)
            }
        }
        row.addView(wrap, LinearLayout.LayoutParams(size, size))
        mark(selected)
        return HorizontalScrollView(c).apply {
            isHorizontalScrollBarEnabled = false
            addView(row)
        }
    }

    /** Tres deslizadores (rojo, verde, azul) para armar cualquier color. */
    fun colorDialog(c: Context, start: Int, onOk: (Int) -> Unit) {
        var r = Color.red(start)
        var g = Color.green(start)
        var b = Color.blue(start)
        val box = LinearLayout(c).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(c, 22f), dp(c, 14f), dp(c, 22f), dp(c, 6f))
        }
        val swatch = View(c)
        fun paint() {
            val bg = GradientDrawable()
            bg.cornerRadius = dp(c, 12f).toFloat()
            bg.setColor(Color.rgb(r, g, b))
            bg.setStroke(dp(c, 1.5f), BROWN)
            swatch.background = bg
        }
        box.addView(swatch, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(c, 54f)))
        fun bar(name: String, value: Int, set: (Int) -> Unit) {
            box.addView(TextView(c).apply {
                text = name
                textSize = 13f
                setTextColor(BROWN)
                setPadding(0, dp(c, 10f), 0, 0)
            })
            box.addView(SeekBar(c).apply {
                max = 255
                progress = value
                setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(s: SeekBar?, p: Int, fromUser: Boolean) {
                        set(p)
                        paint()
                    }
                    override fun onStartTrackingTouch(s: SeekBar?) {}
                    override fun onStopTrackingTouch(s: SeekBar?) {}
                })
            })
        }
        bar("Rojo", r) { r = it }
        bar("Verde", g) { g = it }
        bar("Azul", b) { b = it }
        paint()
        AlertDialog.Builder(c)
            .setTitle("Color a medida")
            .setView(box)
            .setPositiveButton("Listo") { _, _ -> onOk(Color.rgb(r, g, b)) }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    /** Deslizador entre min y max. */
    fun slider(c: Context, min: Float, max: Float, value: Float, onChange: (Float) -> Unit): View =
        SeekBar(c).apply {
            this.max = 100
            progress = (((value - min) / (max - min)) * 100f).toInt().coerceIn(0, 100)
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(s: SeekBar?, p: Int, fromUser: Boolean) {
                    onChange(min + (max - min) * p / 100f)
                }
                override fun onStartTrackingTouch(s: SeekBar?) {}
                override fun onStopTrackingTouch(s: SeekBar?) {}
            })
        }
}
