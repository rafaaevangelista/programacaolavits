package org.lavits.programacao

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Favoritos ("Minha agenda") guardados em SharedPreferences.
 *
 * [ids] é um estado observável do Compose: alterá-lo recompõe a interface
 * automaticamente, e cada alteração é gravada em disco na hora.
 */
class FavoritesStore(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var ids: Set<String> by mutableStateOf(
        prefs.getStringSet(KEY_IDS, emptySet())?.toSet() ?: emptySet()
    )
        private set

    val count: Int get() = ids.size

    fun contains(id: String): Boolean = id in ids

    fun toggle(id: String) {
        val updated = if (id in ids) ids - id else ids + id
        ids = updated
        // Cópia defensiva: o Set devolvido por getStringSet não deve ser reusado.
        prefs.edit().putStringSet(KEY_IDS, HashSet(updated)).apply()
    }

    fun clear() {
        ids = emptySet()
        prefs.edit().remove(KEY_IDS).apply()
    }

    private companion object {
        const val PREFS_NAME = "lavits_favoritos"
        const val KEY_IDS = "ids"
    }
}
