package com.isebx.javi

import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/** Lápiz de pruebas: en vez de dibujar en pantalla, escribe SVG. */
class SvgPen : Pen {
    val sb = StringBuilder()
    private val stack = ArrayList<Int>()
    private var baseOpen = 0
    companion object {
        // Los ids de recorte tienen que ser únicos en toda la hoja, no por gato
        private var ids = 0
    }

    private fun f(v: Float): String = String.format(java.util.Locale.US, "%.2f", v)
    private fun col(c: Int): String {
        val a = (c ushr 24) / 255f
        val rgb = "rgb(${c shr 16 and 255},${c shr 8 and 255},${c and 255})"
        return if (a >= 0.999f) "\"$rgb\"" else "\"$rgb\" fill-opacity=\"${f(a)}\" stroke-opacity=\"${f(a)}\""
    }
    private fun open(attr: String) {
        sb.append("<g $attr>")
        if (stack.isEmpty()) baseOpen++ else stack[stack.size - 1] = stack[stack.size - 1] + 1
    }
    fun finish(): String { repeat(baseOpen) { sb.append("</g>") }; baseOpen = 0; return sb.toString() }

    override fun save() { stack.add(0) }
    override fun restore() { val n = stack.removeAt(stack.size - 1); repeat(n) { sb.append("</g>") } }
    override fun translate(x: Float, y: Float) = open("transform=\"translate(${f(x)},${f(y)})\"")
    override fun rotate(deg: Float) = open("transform=\"rotate(${f(deg)})\"")
    override fun rotate(deg: Float, px: Float, py: Float) = open("transform=\"rotate(${f(deg)} ${f(px)} ${f(py)})\"")
    override fun scale(sx: Float, sy: Float) = open("transform=\"scale(${f(sx)},${f(sy)})\"")
    override fun clipRect(l: Float, t: Float, r: Float, b: Float) {
        ids++
        sb.append("<clipPath id=\"c$ids\"><rect x=\"${f(l)}\" y=\"${f(t)}\" width=\"${f(r - l)}\" height=\"${f(b - t)}\"/></clipPath>")
        open("clip-path=\"url(#c$ids)\"")
    }
    override fun clipOval(l: Float, t: Float, r: Float, b: Float) {
        ids++
        sb.append("<clipPath id=\"c$ids\"><ellipse cx=\"${f((l + r) / 2)}\" cy=\"${f((t + b) / 2)}\" rx=\"${f((r - l) / 2)}\" ry=\"${f((b - t) / 2)}\"/></clipPath>")
        open("clip-path=\"url(#c$ids)\"")
    }
    override fun layerAlpha(l: Float, t: Float, r: Float, b: Float, alpha: Float) { stack.add(0); open("opacity=\"${f(alpha)}\"") }
    override fun oval(l: Float, t: Float, r: Float, b: Float, color: Int) {
        sb.append("<ellipse cx=\"${f((l + r) / 2)}\" cy=\"${f((t + b) / 2)}\" rx=\"${f(abs(r - l) / 2)}\" ry=\"${f(abs(b - t) / 2)}\" fill=${col(color)}/>")
    }
    override fun circle(cx: Float, cy: Float, r: Float, color: Int) { sb.append("<circle cx=\"${f(cx)}\" cy=\"${f(cy)}\" r=\"${f(r)}\" fill=${col(color)}/>") }
    override fun rect(l: Float, t: Float, r: Float, b: Float, color: Int) { sb.append("<rect x=\"${f(l)}\" y=\"${f(t)}\" width=\"${f(r - l)}\" height=\"${f(b - t)}\" fill=${col(color)}/>") }
    override fun roundRect(l: Float, t: Float, r: Float, b: Float, rad: Float, color: Int) { sb.append("<rect x=\"${f(l)}\" y=\"${f(t)}\" width=\"${f(r - l)}\" height=\"${f(b - t)}\" rx=\"${f(rad)}\" fill=${col(color)}/>") }
    override fun line(x0: Float, y0: Float, x1: Float, y1: Float, width: Float, color: Int) { sb.append("<line x1=\"${f(x0)}\" y1=\"${f(y0)}\" x2=\"${f(x1)}\" y2=\"${f(y1)}\" stroke-width=\"${f(width)}\" stroke-linecap=\"round\" stroke=${col(color)}/>") }
    override fun strokeCircle(cx: Float, cy: Float, r: Float, width: Float, color: Int) { sb.append("<circle cx=\"${f(cx)}\" cy=\"${f(cy)}\" r=\"${f(r)}\" fill=\"none\" stroke-width=\"${f(width)}\" stroke=${col(color)}/>") }
    override fun arc(l: Float, t: Float, r: Float, b: Float, start: Float, sweep: Float, width: Float, color: Int) {
        val cx = (l + r) / 2; val cy = (t + b) / 2; val rx = (r - l) / 2; val ry = (b - t) / 2
        val a0 = Math.toRadians(start.toDouble()); val a1 = Math.toRadians((start + sweep).toDouble())
        val x0 = cx + rx * cos(a0).toFloat(); val y0 = cy + ry * sin(a0).toFloat()
        val x1 = cx + rx * cos(a1).toFloat(); val y1 = cy + ry * sin(a1).toFloat()
        sb.append("<path d=\"M${f(x0)} ${f(y0)} A${f(rx)} ${f(ry)} 0 ${if (abs(sweep) > 180f) 1 else 0} ${if (sweep > 0) 1 else 0} ${f(x1)} ${f(y1)}\" fill=\"none\" stroke-width=\"${f(width)}\" stroke-linecap=\"round\" stroke=${col(color)}/>")
    }
    private fun d(c: FloatArray): String {
        val s = StringBuilder(); var i = 0
        while (i < c.size) {
            when (c[i]) {
                Pen.M -> { s.append("M${f(c[i + 1])} ${f(c[i + 2])} "); i += 3 }
                Pen.L -> { s.append("L${f(c[i + 1])} ${f(c[i + 2])} "); i += 3 }
                Pen.Q -> { s.append("Q${f(c[i + 1])} ${f(c[i + 2])} ${f(c[i + 3])} ${f(c[i + 4])} "); i += 5 }
                Pen.C -> { s.append("C${f(c[i + 1])} ${f(c[i + 2])} ${f(c[i + 3])} ${f(c[i + 4])} ${f(c[i + 5])} ${f(c[i + 6])} "); i += 7 }
                else -> { s.append("Z "); i += 1 }
            }
        }
        return s.toString()
    }
    override fun fillPath(cmds: FloatArray, color: Int) { sb.append("<path d=\"${d(cmds)}\" fill=${col(color)}/>") }
    override fun strokePath(cmds: FloatArray, width: Float, color: Int) { sb.append("<path d=\"${d(cmds)}\" fill=\"none\" stroke-width=\"${f(width)}\" stroke-linecap=\"round\" stroke-linejoin=\"round\" stroke=${col(color)}/>") }
    override fun polyline(pts: FloatArray, n: Int, width: Float, color: Int) {
        val s = StringBuilder("M${f(pts[0])} ${f(pts[1])} ")
        for (i in 1 until n) s.append("L${f(pts[i * 2])} ${f(pts[i * 2 + 1])} ")
        sb.append("<path d=\"$s\" fill=\"none\" stroke-width=\"${f(width)}\" stroke-linecap=\"round\" stroke-linejoin=\"round\" stroke=${col(color)}/>")
    }
    override fun text(s: String, x: Float, y: Float, size: Float, color: Int, outline: Int) { sb.append("<text x=\"${f(x)}\" y=\"${f(y)}\" font-size=\"${f(size)}\" font-family=\"sans-serif\" fill=${col(color)} stroke=\"#5a4332\" stroke-width=\"0.4\">$s</text>") }
    override fun icon(kind: Int) { sb.append("<circle r=\"8\" fill=\"#ef9db0\"/>") }
    override fun serving(kind: Int) { sb.append("<ellipse cx=\"0\" cy=\"2\" rx=\"17\" ry=\"5\" fill=\"#d9866f\"/>") }
}
