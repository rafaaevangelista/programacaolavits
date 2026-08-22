package org.lavits.programacao

/**
 * Um horário da agenda pessoal. Como as trilhas do simpósio são paralelas,
 * mais de uma atividade favoritada no mesmo dia e horário significa conflito.
 *
 * Sem dependências do Android, para permitir teste em JVM comum.
 */
data class AgendaSlot(
    val day: Day,
    val time: String,
    val sessions: List<Session>
) {
    val hasConflict: Boolean get() = sessions.size > 1
}

/** Agrupa os favoritos por dia e horário, em ordem cronológica. */
fun buildAgenda(program: Program, favoriteIds: Set<String>): List<AgendaSlot> {
    val daysByKey = program.days.associateBy { it.key }

    return program.sessions
        .filter { it.id in favoriteIds }
        .groupBy { it.day to it.time }
        .mapNotNull { (key, sessions) ->
            val day = daysByKey[key.first] ?: return@mapNotNull null
            AgendaSlot(
                day = day,
                time = key.second,
                sessions = sessions.sortedBy { it.type.ordinal }
            )
        }
        .sortedWith(compareBy({ it.day.key }, { it.sessions.first().startMinute }))
}
