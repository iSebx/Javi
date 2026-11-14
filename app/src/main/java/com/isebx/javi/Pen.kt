package com.isebx.javi

/**
 * Un lápiz abstracto. El gato se dibuja contra esta interfaz y no contra el
 * Canvas de Android, así el mismo código se puede probar fuera del teléfono
 * (generando SVG) y ver exactamente lo que va a dibujar la app.
 * Los colores son ARGB; la transparencia va en el propio color.
 */
interface Pen {
    companion object {
        // Códigos para describir trazos: M x y · L x y · Q cx cy x y · C x1 y1 x2 y2 x y · Z
        const val M = 0f
        const val L = 1f
        const val Q = 2f
        const val C = 3f
        const val Z = 4f
    }

    fun save()
    fun restore()
    fun translate(x: Float, y: Float)
    fun rotate(deg: Float)
    fun rotate(deg: Float, px: Float, py: Float)
    fun scale(sx: Float, sy: Float)
    fun clipRect(l: Float, t: Float, r: Float, b: Float)
    fun clipOval(l: Float, t: Float, r: Float, b: Float)

    /** Abre una capa con transparencia; se cierra con restore(). */
    fun layerAlpha(l: Float, t: Float, r: Float, b: Float, alpha: Float)

    fun oval(l: Float, t: Float, r: Float, b: Float, color: Int)
    fun circle(cx: Float, cy: Float, r: Float, color: Int)
    fun rect(l: Float, t: Float, r: Float, b: Float, color: Int)
    fun roundRect(l: Float, t: Float, r: Float, b: Float, rad: Float, color: Int)
    fun line(x0: Float, y0: Float, x1: Float, y1: Float, width: Float, color: Int)
    fun strokeCircle(cx: Float, cy: Float, r: Float, width: Float, color: Int)
    fun arc(l: Float, t: Float, r: Float, b: Float, start: Float, sweep: Float, width: Float, color: Int)
    fun fillPath(cmds: FloatArray, color: Int)
    fun strokePath(cmds: FloatArray, width: Float, color: Int)

    /** Línea quebrada con puntas redondas: pts = x0,y0,x1,y1,... usando los primeros n puntos. */
    fun polyline(pts: FloatArray, n: Int, width: Float, color: Int)

    fun text(s: String, x: Float, y: Float, size: Float, color: Int, outline: Int)

    /** Un dibujito de Props centrado en el origen. */
    fun icon(kind: Int)

    /** Comida o bebida servida (ver Props.drawServing). */
    fun serving(kind: Int)
}
