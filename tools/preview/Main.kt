package com.isebx.javi

import java.io.File

/** Genera hojas SVG con el dibujo real de la app: poses y aspectos. */
fun pose(st: Javi.St, secs: Float = 1.6f, setup: (Javi) -> Unit = {}): Javi {
    val j = Javi()
    j.setBounds(98f, 600f)
    j.x = 300f
    setup(j)
    if (st != Javi.St.WALK || true) j.debugSet(st)
    var t = 0f
    while (t < secs) {
        if (st == Javi.St.REACH) j.attend(j.x + 10f, 160f, 500f)
        if (st == Javi.St.CLING) j.cling()
        if (st == Javi.St.BOX) j.pet()
        j.update(0.016f); t += 0.016f
    }
    return j
}

fun sheet(file: String, cells: List<Triple<String, Javi, CatLook>>, cols: Int) {
    val cw = 200; val ch = 215
    val rows = (cells.size + cols - 1) / cols
    val out = StringBuilder("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"${cols * cw}\" height=\"${rows * ch}\" viewBox=\"0 0 ${cols * cw} ${rows * ch}\"><rect width=\"100%\" height=\"100%\" fill=\"#f6efe4\"/>")
    val painter = CatPainter()
    cells.forEachIndexed { i, (name, j, look) ->
        val x = (i % cols) * cw; val y = (i / cols) * ch
        val pen = SvgPen()
        painter.draw(pen, j, look)
        out.append("<g transform=\"translate($x,$y)\"><rect x=\"5\" y=\"5\" width=\"${cw - 10}\" height=\"${ch - 10}\" rx=\"12\" fill=\"#fffaf2\"/>")
        out.append("<line x1=\"14\" y1=\"182\" x2=\"${cw - 14}\" y2=\"182\" stroke=\"#d9cdb8\"/>")
        out.append("<g transform=\"translate(${cw / 2},182)\">${pen.finish()}</g>")
        out.append("<text x=\"${cw / 2}\" y=\"203\" text-anchor=\"middle\" font-size=\"12\" font-family=\"sans-serif\" fill=\"#5a4332\">$name</text></g>")
    }
    out.append("</svg>")
    File(file).writeText("<!DOCTYPE html><html><body style=\"margin:0\">$out</body></html>")
}

fun main(args: Array<String>) {
    val dir = args[0]
    val javi = CatLook.javi()
    val S = Javi.St.values()
    val states = listOf(
        "WALK", "SIT", "GROOM", "STRETCH", "SLEEP", "PURR", "EAT", "BEG", "KNEAD", "YAWN",
        "POOP", "HANG", "CLING", "REACH", "BOX", "WASH"
    )
    sheet("$dir/poses.html", states.map { n ->
        val st = S.first { it.name == n }
        Triple(n, pose(st, if (n == "YAWN") 1.3f else 1.6f) { if (n == "EAT") it.food = 0 }, javi)
    }, 4)
    val presets = Looks.presets()
    sheet("$dir/looks.html", presets.map { Triple(it.name, pose(Javi.St.SIT), it) }, 4)
    val pick = listOf("Calicó", "Siamés", "Maine Coon", "Esmoquin", "Sin pelo", "Bengalí").map { n -> presets.first { it.name == n } }
    val mixStates = listOf("WALK", "SLEEP", "REACH", "STRETCH")
    val cells = ArrayList<Triple<String, Javi, CatLook>>()
    for (l in pick) for (n in mixStates) cells.add(Triple("${l.name} · $n", pose(S.first { it.name == n }), l))
    sheet("$dir/mix.html", cells, 4)
    val wetJ = pose(Javi.St.SIT) { it.wet = 1f; it.dirt = 0f }
    val dirtJ = pose(Javi.St.SIT) { it.dirt = 1f }
    sheet("$dir/extra.html", listOf(
        Triple("Javi mojado", wetJ, javi), Triple("Javi sucio", dirtJ, javi),
        Triple("Calicó mojada", wetJ, presets.first { it.name == "Calicó" }),
        Triple("Negro sucio", dirtJ, presets.first { it.name == "Negro" })
    ), 4)
}
