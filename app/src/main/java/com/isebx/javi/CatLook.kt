package com.isebx.javi

/**
 * El aspecto de un gato: todo lo que lo hace único. Kotlin puro.
 * Los colores son ARGB.
 */
class CatLook {
    companion object {
        // Patrón del pelaje
        const val SOLID = 0
        const val SOFT = 1       // rayitas suaves
        const val MACKEREL = 2   // atigrado rayado
        const val CLASSIC = 3    // atigrado clásico, con remolino
        const val SPOTTED = 4    // moteado
        const val TICKED = 5     // agutí, casi sin marcas

        // Blanco
        const val W_NONE = 0
        const val W_LOCKET = 1   // pecho
        const val W_MITTED = 2   // pecho y guantes
        const val W_TUXEDO = 3   // esmoquin
        const val W_BICOLOR = 4  // bicolor

        // Pelo
        const val SHORT = 0
        const val MEDIUM = 1
        const val LONG = 2
        const val HAIRLESS = 3
        const val CURLY = 4

        // Cabeza
        const val ROUND = 0
        const val WEDGE = 1
        const val FLAT = 2
        const val WIDE = 3

        // Orejas
        const val E_NORMAL = 0
        const val E_BIG = 1
        const val E_SMALL = 2
        const val E_FOLD = 3
        const val E_CURL = 4
        const val E_TUFT = 5

        // Cola
        const val T_LONG = 0
        const val T_THIN = 1
        const val T_FLUFFY = 2
        const val T_BOB = 3
        const val T_NONE = 4

        // Manchas
        const val P_NONE = 0
        const val P_COW = 1
        const val P_CALICO = 2
        const val P_TORTIE = 3

        const val WHITE = 0xFFFBF6EE.toInt()

        fun mix(a: Int, b: Int, t: Float): Int {
            val u = t.coerceIn(0f, 1f)
            val al = ((a ushr 24) + ((b ushr 24) - (a ushr 24)) * u).toInt()
            val r = ((a shr 16 and 255) + ((b shr 16 and 255) - (a shr 16 and 255)) * u).toInt()
            val g = ((a shr 8 and 255) + ((b shr 8 and 255) - (a shr 8 and 255)) * u).toInt()
            val bl = ((a and 255) + ((b and 255) - (a and 255)) * u).toInt()
            return (al shl 24) or (r shl 16) or (g shl 8) or bl
        }

        fun darken(c: Int, f: Float): Int {
            val r = ((c shr 16 and 255) * f).toInt().coerceIn(0, 255)
            val g = ((c shr 8 and 255) * f).toInt().coerceIn(0, 255)
            val b = ((c and 255) * f).toInt().coerceIn(0, 255)
            return (c and 0xFF000000.toInt()) or (r shl 16) or (g shl 8) or b
        }

        fun alpha(c: Int, a: Float): Int =
            (c and 0x00FFFFFF) or ((a.coerceIn(0f, 1f) * 255).toInt() shl 24)

        /** Javi, tal cual es. */
        fun javi(): CatLook = CatLook().apply {
            name = "Javi"
            base = 0xFFF0C595.toInt()
            dark = 0xFFE2B181.toInt()
            light = 0xFFF9E8D2.toInt()
            pattern = SOFT
            eyeL = 0xFF3A2418.toInt()
            eyeR = 0xFF3A2418.toInt()
            collar = 0xFF1F1B1A.toInt()
        }
    }

    /** Una mancha de color sobre el cuerpo (part 0) o la cabeza (part 1). */
    class Patch(val part: Int, val x: Float, val y: Float, val rx: Float, val ry: Float, val color: Int)

    var name = "Gato"

    var base = 0xFFEBA862.toInt()
    var dark = 0xFFC97D38.toInt()
    var light = 0xFFF8DFBD.toInt()
    var pattern = SOLID
    var white = W_NONE

    /** Color de las puntas (cara, orejas, patas, cola) si es colorpoint; 0 si no. */
    var point = 0

    /** Manchas: 0 ninguna, 1 vaquita, 2 calicó, 3 carey. Con sus dos colores. */
    var patchStyle = P_NONE
    var patchA = 0xFF3A3536.toInt()
    var patchB = 0xFFE39A4E.toInt()
    var patches: List<Patch> = emptyList()
        private set

    /** Vuelve a armar las manchas según el estilo y los colores elegidos. */
    fun rebuildPatches() {
        patches = when (patchStyle) {
            P_COW -> listOf(
                Patch(0, -22f, -66f, 16f, 13f, patchA), Patch(0, 12f, -52f, 14f, 11f, patchA),
                Patch(0, -8f, -36f, 10f, 8f, patchA),
                Patch(1, -18f, -16f, 20f, 18f, patchA), Patch(1, 26f, -18f, 14f, 14f, patchA)
            )
            P_CALICO -> listOf(
                Patch(0, -24f, -66f, 17f, 13f, patchA), Patch(0, 6f, -70f, 15f, 11f, patchB),
                Patch(0, -6f, -42f, 13f, 10f, patchB), Patch(0, 22f, -52f, 11f, 10f, patchA),
                Patch(1, -18f, -14f, 22f, 20f, patchB), Patch(1, 24f, -20f, 16f, 16f, patchA)
            )
            P_TORTIE -> listOf(
                Patch(0, -26f, -62f, 10f, 8f, patchA), Patch(0, -6f, -74f, 9f, 7f, patchB),
                Patch(0, 10f, -56f, 11f, 8f, patchA), Patch(0, -12f, -44f, 8f, 7f, patchB),
                Patch(0, 24f, -68f, 8f, 7f, patchA),
                Patch(1, -16f, -10f, 12f, 14f, patchA), Patch(1, 20f, 10f, 10f, 9f, patchB),
                Patch(1, 8f, -24f, 8f, 6f, patchA)
            )
            else -> emptyList()
        }
    }

    /** Deriva los tonos de marcas y de panza a partir del color base. */
    fun autoShades() {
        dark = darken(base, 0.8f)
        light = mix(base, WHITE, 0.6f)
    }

    fun copy(): CatLook {
        val o = CatLook()
        o.name = name; o.base = base; o.dark = dark; o.light = light; o.pattern = pattern
        o.white = white; o.point = point; o.patchStyle = patchStyle; o.patchA = patchA; o.patchB = patchB
        o.hair = hair; o.head = head; o.ears = ears; o.tail = tail; o.tailTip = tailTip
        o.bodyW = bodyW; o.bodyL = bodyL; o.headSize = headSize
        o.eyeL = eyeL; o.eyeR = eyeR; o.nose = nose; o.innerEar = innerEar; o.whiskers = whiskers
        o.blush = blush; o.collar = collar
        o.rebuildPatches()
        return o
    }

    var hair = SHORT
    var head = ROUND
    var ears = E_NORMAL
    var tail = T_LONG

    /** La punta de la cola va en el color de las marcas. */
    var tailTip = true

    var bodyW = 1f
    var bodyL = 1f
    var headSize = 1f

    var eyeL = 0xFFE0A030.toInt()
    var eyeR = 0xFFE0A030.toInt()
    var nose = 0xFFE79A94.toInt()
    var innerEar = 0xFFEFB3A8.toInt()
    var whiskers = 0xFFFFFFFF.toInt()
    var blush = true

    /** Color del collar, o 0 si no lleva. */
    var collar = 0
}
