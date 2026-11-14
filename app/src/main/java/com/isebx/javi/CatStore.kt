package com.isebx.javi

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/** Los gatos guardados en el teléfono y cuál está en pantalla. */
object CatStore {

    class Cat(val id: Long, var look: CatLook)

    private fun prefs(c: Context) = c.getSharedPreferences("javi_cats", Context.MODE_PRIVATE)

    private fun toJson(cat: Cat): JSONObject {
        val k = cat.look
        return JSONObject()
            .put("id", cat.id).put("name", k.name)
            .put("base", k.base).put("dark", k.dark).put("light", k.light)
            .put("pattern", k.pattern).put("white", k.white).put("point", k.point)
            .put("patchStyle", k.patchStyle).put("patchA", k.patchA).put("patchB", k.patchB)
            .put("hair", k.hair).put("head", k.head).put("ears", k.ears).put("tail", k.tail)
            .put("tailTip", k.tailTip)
            .put("bodyW", k.bodyW.toDouble()).put("bodyL", k.bodyL.toDouble())
            .put("headSize", k.headSize.toDouble())
            .put("eyeL", k.eyeL).put("eyeR", k.eyeR).put("nose", k.nose)
            .put("innerEar", k.innerEar).put("whiskers", k.whiskers)
            .put("blush", k.blush).put("collar", k.collar)
    }

    private fun fromJson(o: JSONObject): Cat {
        val d = CatLook()
        val k = CatLook()
        k.name = o.optString("name", d.name)
        k.base = o.optInt("base", d.base)
        k.dark = o.optInt("dark", d.dark)
        k.light = o.optInt("light", d.light)
        k.pattern = o.optInt("pattern", d.pattern)
        k.white = o.optInt("white", d.white)
        k.point = o.optInt("point", d.point)
        k.patchStyle = o.optInt("patchStyle", d.patchStyle)
        k.patchA = o.optInt("patchA", d.patchA)
        k.patchB = o.optInt("patchB", d.patchB)
        k.hair = o.optInt("hair", d.hair)
        k.head = o.optInt("head", d.head)
        k.ears = o.optInt("ears", d.ears)
        k.tail = o.optInt("tail", d.tail)
        k.tailTip = o.optBoolean("tailTip", d.tailTip)
        k.bodyW = o.optDouble("bodyW", d.bodyW.toDouble()).toFloat()
        k.bodyL = o.optDouble("bodyL", d.bodyL.toDouble()).toFloat()
        k.headSize = o.optDouble("headSize", d.headSize.toDouble()).toFloat()
        k.eyeL = o.optInt("eyeL", d.eyeL)
        k.eyeR = o.optInt("eyeR", d.eyeR)
        k.nose = o.optInt("nose", d.nose)
        k.innerEar = o.optInt("innerEar", d.innerEar)
        k.whiskers = o.optInt("whiskers", d.whiskers)
        k.blush = o.optBoolean("blush", d.blush)
        k.collar = o.optInt("collar", d.collar)
        k.rebuildPatches()
        return Cat(o.optLong("id", 1L), k)
    }

    /** Todos los gatos. La primera vez, solo Javi (con el collar que tenía elegido). */
    fun all(c: Context): MutableList<Cat> {
        val raw = prefs(c).getString("list", null)
        val out = ArrayList<Cat>()
        if (raw != null) {
            try {
                val arr = JSONArray(raw)
                for (i in 0 until arr.length()) out.add(fromJson(arr.getJSONObject(i)))
            } catch (e: Exception) {
                out.clear()
            }
        }
        if (out.isEmpty()) {
            val javi = CatLook.javi()
            javi.collar = Store.collar(c)
            out.add(Cat(1L, javi))
            saveAll(c, out)
        }
        return out
    }

    fun saveAll(c: Context, list: List<Cat>) {
        val arr = JSONArray()
        for (cat in list) arr.put(toJson(cat))
        prefs(c).edit().putString("list", arr.toString()).apply()
    }

    /** Guarda un gato: lo reemplaza si ya existe, o lo agrega. */
    fun put(c: Context, cat: Cat) {
        val list = all(c)
        val i = list.indexOfFirst { it.id == cat.id }
        if (i >= 0) list[i] = cat else list.add(cat)
        saveAll(c, list)
    }

    fun remove(c: Context, id: Long) {
        val list = all(c)
        if (list.size <= 1) return
        list.removeAll { it.id == id }
        saveAll(c, list)
        val ids = screenIds(c).filter { it != id }
        saveScreen(c, if (ids.isEmpty()) listOf(list[0].id) else ids)
    }

    fun newId(c: Context): Long = (all(c).maxOfOrNull { it.id } ?: 0L) + 1L

    const val MAX_ON_SCREEN = 3

    private fun saveScreen(c: Context, ids: List<Long>) {
        prefs(c).edit().putString("screen", ids.joinToString(",")).apply()
    }

    /** Qué gatos salen a la pantalla (hasta tres), en orden. Nunca queda vacío. */
    fun screenIds(c: Context): List<Long> {
        val list = all(c)
        val raw = prefs(c).getString("screen", null)
        val ids = if (raw == null) {
            // Antes había un solo gato "elegido"
            listOf(prefs(c).getLong("active", 1L))
        } else {
            raw.split(",").mapNotNull { it.trim().toLongOrNull() }
        }
        val valid = ids.filter { id -> list.any { it.id == id } }.distinct().take(MAX_ON_SCREEN)
        return if (valid.isEmpty()) listOf(list[0].id) else valid
    }

    fun isOnScreen(c: Context, id: Long): Boolean = screenIds(c).contains(id)

    /**
     * Saca un gato a la pantalla o lo guarda. Devuelve false si no se pudo:
     * ya hay tres afuera, o era el último que quedaba.
     */
    fun setOnScreen(c: Context, id: Long, on: Boolean): Boolean {
        val ids = screenIds(c).toMutableList()
        if (on) {
            if (ids.contains(id)) return true
            if (ids.size >= MAX_ON_SCREEN) return false
            ids.add(id)
            // Mientras estuvo guardado no le bajaron las barras
            Care.touch(c, id)
        } else {
            if (!ids.contains(id)) return true
            if (ids.size <= 1) return false
            ids.remove(id)
        }
        saveScreen(c, ids)
        return true
    }

    /** Los gatos que salen a la pantalla, con su aspecto. */
    fun onScreen(c: Context): List<Cat> {
        val list = all(c)
        return screenIds(c).mapNotNull { id -> list.firstOrNull { it.id == id } }
    }

    /** El primero de los que están en pantalla (el que muestra la casita). */
    fun active(c: Context): Cat = onScreen(c)[0]
}
