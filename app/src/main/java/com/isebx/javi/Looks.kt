package com.isebx.javi

/**
 * Plantillas de aspecto: puntos de partida que después se pueden retocar
 * por completo. La mayoría de los gatos son "comunes", así que primero van
 * los colores y patrones, y después las razas.
 */
object Looks {
    private const val BK = 0xFF3A3536.toInt()
    private const val OR = 0xFFE39A4E.toInt()
    private const val WH = CatLook.WHITE

    private fun patch(part: Int, x: Float, y: Float, rx: Float, ry: Float, c: Int) =
        CatLook.Patch(part, x, y, rx, ry, c)

    private fun look(name: String, f: CatLook.() -> Unit): CatLook = CatLook().apply {
        this.name = name
        f()
    }

    fun presets(): List<CatLook> = listOf(
        look("Naranja atigrado") {
            base = 0xFFEBA862.toInt(); dark = 0xFFC97D38.toInt(); light = 0xFFF8DFBD.toInt()
            pattern = CatLook.MACKEREL
        },
        look("Gris atigrado") {
            base = 0xFFA9A08F.toInt(); dark = 0xFF5A5146.toInt(); light = 0xFFE9E0D0.toInt()
            pattern = CatLook.MACKEREL; eyeL = 0xFF7FB069.toInt(); eyeR = eyeL; nose = 0xFFB56F66.toInt()
        },
        look("Negro") {
            base = BK; dark = 0xFF2A2627.toInt(); light = 0xFF4A4546.toInt()
            eyeL = 0xFFE6C34A.toInt(); eyeR = eyeL; nose = 0xFF2A2627.toInt()
            innerEar = 0xFF6B5A5C.toInt(); whiskers = 0xFFBDB6B7.toInt(); blush = false
        },
        look("Blanco, ojos impares") {
            base = WH; dark = 0xFFE6DFD2.toInt(); light = WH; tailTip = false
            eyeL = 0xFF5AA9E6.toInt(); eyeR = 0xFFE0A030.toInt(); whiskers = 0xFFD9D2C6.toInt()
        },
        look("Esmoquin") {
            base = BK; dark = 0xFF2A2627.toInt(); light = 0xFF4A4546.toInt(); white = CatLook.W_TUXEDO
            eyeL = 0xFF7FB069.toInt(); eyeR = eyeL; innerEar = 0xFF6B5A5C.toInt(); blush = false
        },
        look("Vaquita") {
            base = WH; dark = 0xFFE6DFD2.toInt(); light = WH; tail = CatLook.T_BOB
            eyeL = 0xFFE6C34A.toInt(); eyeR = eyeL; whiskers = 0xFFD9D2C6.toInt()
            patches = listOf(
                patch(0, -22f, -66f, 16f, 13f, BK), patch(0, 12f, -52f, 14f, 11f, BK),
                patch(0, -8f, -36f, 10f, 8f, BK),
                patch(1, -18f, -16f, 20f, 18f, BK), patch(1, 26f, -18f, 14f, 14f, BK)
            )
        },
        look("Calicó") {
            base = WH; dark = 0xFFE6DFD2.toInt(); light = WH; tailTip = false
            eyeL = 0xFF7FB069.toInt(); eyeR = eyeL; whiskers = 0xFFD9D2C6.toInt()
            patches = listOf(
                patch(0, -24f, -66f, 17f, 13f, BK), patch(0, 6f, -70f, 15f, 11f, OR),
                patch(0, -6f, -42f, 13f, 10f, OR), patch(0, 22f, -52f, 11f, 10f, BK),
                patch(1, -18f, -14f, 22f, 20f, OR), patch(1, 24f, -20f, 16f, 16f, BK)
            )
        },
        look("Carey") {
            base = 0xFF34302F.toInt(); dark = 0xFF2A2627.toInt(); light = 0xFF4A4442.toInt()
            innerEar = 0xFF7A6460.toInt(); blush = false
            patches = listOf(
                patch(0, -26f, -62f, 10f, 8f, 0xFFD98A42.toInt()), patch(0, -6f, -74f, 9f, 7f, 0xFFE9B06A.toInt()),
                patch(0, 10f, -56f, 11f, 8f, 0xFFD98A42.toInt()), patch(0, -12f, -44f, 8f, 7f, 0xFFE9B06A.toInt()),
                patch(0, 24f, -68f, 8f, 7f, 0xFFD98A42.toInt()),
                patch(1, -16f, -10f, 12f, 14f, 0xFFD98A42.toInt()), patch(1, 20f, 10f, 10f, 9f, 0xFFE9B06A.toInt()),
                patch(1, 8f, -24f, 8f, 6f, 0xFFD98A42.toInt())
            )
        },
        look("Siamés") {
            base = 0xFFF3E6D2.toInt(); dark = 0xFFE4D6BF.toInt(); light = 0xFFF9F0E2.toInt()
            point = 0xFF5A4336.toInt(); head = CatLook.WEDGE; ears = CatLook.E_BIG; tail = CatLook.T_THIN
            bodyW = 0.88f; eyeL = 0xFF5AA9E6.toInt(); eyeR = eyeL; nose = 0xFF3F2E26.toInt()
            innerEar = 0xFF7A5F52.toInt(); whiskers = 0xFFE9DFD0.toInt()
        },
        look("Persa") {
            base = 0xFFF4E3C6.toInt(); dark = 0xFFE2CDA6.toInt(); light = 0xFFFBF2E0.toInt()
            hair = CatLook.LONG; head = CatLook.FLAT; ears = CatLook.E_SMALL; bodyW = 1.12f
            eyeL = 0xFFC8742C.toInt(); eyeR = eyeL; tailTip = false
        },
        look("Maine Coon") {
            base = 0xFF9C8062.toInt(); dark = 0xFF4F3C2A.toInt(); light = 0xFFEAD8BA.toInt()
            pattern = CatLook.CLASSIC; hair = CatLook.LONG; ears = CatLook.E_TUFT; tail = CatLook.T_FLUFFY
            bodyW = 1.1f; bodyL = 1.06f; headSize = 1.04f
            eyeL = 0xFFB5A04A.toInt(); eyeR = eyeL; nose = 0xFFA5645A.toInt()
        },
        look("Azul cachetón") {
            base = 0xFF8F9BA7.toInt(); dark = 0xFF73808C.toInt(); light = 0xFFA8B2BC.toInt()
            head = CatLook.WIDE; ears = CatLook.E_SMALL; bodyW = 1.1f; tailTip = false
            eyeL = 0xFFC8742C.toInt(); eyeR = eyeL; nose = 0xFF5F6A75.toInt(); innerEar = 0xFFB9A8AE.toInt()
        },
        look("Bengalí") {
            base = 0xFFDBA660.toInt(); dark = 0xFF5F3D20.toInt(); light = 0xFFF4DFB8.toInt()
            pattern = CatLook.SPOTTED; eyeL = 0xFF7FB069.toInt(); eyeR = eyeL; nose = 0xFFA5645A.toInt()
        },
        look("Sin pelo") {
            base = 0xFFE9C6B9.toInt(); dark = 0xFFC49A8C.toInt(); light = 0xFFF1D6CB.toInt()
            hair = CatLook.HAIRLESS; ears = CatLook.E_BIG; head = CatLook.WEDGE; tail = CatLook.T_THIN
            bodyW = 0.9f; tailTip = false
            eyeL = 0xFF6FC7C0.toInt(); eyeR = eyeL; nose = 0xFFD99B93.toInt(); innerEar = 0xFFE0A79D.toInt()
        },
        look("Orejas dobladas") {
            base = 0xFF9AA3AD.toInt(); dark = 0xFF7B8590.toInt(); light = 0xFFB6BEC6.toInt()
            white = CatLook.W_MITTED; ears = CatLook.E_FOLD; head = CatLook.WIDE; tailTip = false
            nose = 0xFFC98D86.toInt()
        },
        look("Ragdoll") {
            base = 0xFFF6ECDC.toInt(); dark = 0xFFE8DCC8.toInt(); light = 0xFFFBF5EA.toInt()
            point = 0xFF8B8F99.toInt(); white = CatLook.W_MITTED; hair = CatLook.LONG; tail = CatLook.T_FLUFFY
            bodyW = 1.08f; eyeL = 0xFF5AA9E6.toInt(); eyeR = eyeL; nose = 0xFFC9A39C.toInt()
            innerEar = 0xFFC9B3B0.toInt(); whiskers = 0xFFE9DFD0.toInt()
        }
    )
}
