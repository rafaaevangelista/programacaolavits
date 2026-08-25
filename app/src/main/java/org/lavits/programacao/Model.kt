package org.lavits.programacao

import org.json.JSONArray
import org.json.JSONObject
import java.text.Normalizer

/** Um trabalho apresentado dentro de uma sessão temática. */
data class Work(
    val title: String,
    val authors: String
)

/** Tipos de atividade da programação. */
enum class SessionType(
    val key: String,
    val label: String,
    val plural: String
) {
    ST("ST", "Sessão Temática", "Sessões Temáticas"),
    SL("SL", "Sessão Livre", "Sessões Livres"),
    OF("OF", "Oficina", "Oficinas"),
    PA("PA", "Prática Artística", "Práticas Artísticas"),
    TR("TR", "Trama", "Tramas"),
    EV("EV", "Evento", "Eventos");

    companion object {
        fun from(key: String): SessionType =
            SessionType.entries.firstOrNull { it.key == key } ?: EV

        /** Tipos oferecidos como filtro, na ordem em que aparecem na interface. */
        val filterable: List<SessionType> = listOf(
            SessionType.ST,
            SessionType.SL,
            SessionType.OF,
            SessionType.PA,
            SessionType.TR
        )
    }
}

/** Uma atividade da programação. */
data class Session(
    val id: String,
    val type: SessionType,
    val code: String?,
    val day: String,
    val time: String,
    val title: String,
    val people: List<String>,
    val works: List<Work>,
    val cancelled: Boolean = false
) {
    /** Minuto inicial do horário, usado para ordenar ("14h00–15h50" -> 840). */
    val startMinute: Int = parseStartMinute(time)

    /** Texto normalizado (sem acentos, minúsculo) com tudo que a busca cobre. */
    val searchIndex: String by lazy(LazyThreadSafetyMode.NONE) {
        buildString {
            append(title).append(' ')
            code?.let { append(it).append(' ') }
            append(type.label).append(' ')
            people.forEach { append(it).append(' ') }
            works.forEach { append(it.title).append(' ').append(it.authors).append(' ') }
        }.normalizeForSearch()
    }

    /** Rótulo curto mostrado no topo do cartão. */
    val badge: String get() = code ?: type.label
}

data class Day(
    val key: String,
    val label: String,
    val date: String
)

data class Program(
    val days: List<Day>,
    val sessions: List<Session>
) {
    fun sessionsOf(day: String): List<Session> = sessions.filter { it.day == day }
    fun byId(id: String): Session? = sessions.firstOrNull { it.id == id }
}

/** Remove acentos e caixa, para busca tolerante ("Joao" acha "João"). */
fun String.normalizeForSearch(): String =
    Normalizer.normalize(this, Normalizer.Form.NFD)
        .replace(COMBINING_MARKS, "")
        .lowercase()

private val COMBINING_MARKS = Regex("\\p{Mn}+")

/** "9h00–10h50" -> 540. Aceita tanto travessão quanto hífen. */
private fun parseStartMinute(time: String): Int {
    val start = time.split('–', '—', '-').first().trim()
    val parts = start.split('h')
    val hours = parts.getOrNull(0)?.trim()?.toIntOrNull() ?: return 0
    val minutes = parts.getOrNull(1)?.trim()?.takeIf { it.isNotEmpty() }?.toIntOrNull() ?: 0
    return hours * 60 + minutes
}

/**
 * Conversão do JSON em objetos. Deliberadamente sem nenhuma dependência do
 * Android: assim esta lógica pode ser testada numa JVM comum.
 */
object ProgramParser {

    fun parse(raw: String): Program {
        val root = JSONObject(raw)

        val days = root.getJSONArray("days").mapObjects { obj ->
            Day(
                key = obj.getString("key"),
                label = obj.getString("label"),
                date = obj.getString("date")
            )
        }

        val sessions = root.getJSONArray("sessions").mapObjects { obj ->
            Session(
                id = obj.getString("id"),
                type = SessionType.from(obj.getString("type")),
                code = obj.optString("code").takeIf { it.isNotBlank() && it != "null" },
                day = obj.getString("day"),
                time = obj.getString("time"),
                title = obj.getString("title"),
                people = obj.optJSONArray("people").mapStrings(),
                works = obj.optJSONArray("works").mapObjects { w ->
                    Work(title = w.getString("t"), authors = w.getString("a"))
                },
                cancelled = obj.optBoolean("cancelled", false)
            )
        }

        return Program(days = days, sessions = sessions)
    }
}

private fun <T> JSONArray?.mapObjects(transform: (JSONObject) -> T): List<T> {
    if (this == null) return emptyList()
    return (0 until length()).map { transform(getJSONObject(it)) }
}

private fun JSONArray?.mapStrings(): List<String> {
    if (this == null) return emptyList()
    return (0 until length()).map { getString(it) }
}
