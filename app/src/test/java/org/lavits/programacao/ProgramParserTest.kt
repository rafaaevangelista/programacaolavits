package org.lavits.programacao

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Testes da camada de dados. Rodam na JVM (`./gradlew test`), sem emulador:
 * [ProgramParser] e [buildAgenda] não dependem do Android de propósito.
 */
class ProgramParserTest {

    private val program: Program by lazy {
        // Em testes de unidade o diretório de trabalho é o módulo :app.
        val file = File("src/main/assets/programacao.json")
        assertTrue("asset não encontrado em ${file.absolutePath}", file.exists())
        ProgramParser.parse(file.readText(Charsets.UTF_8))
    }

    private fun search(term: String): List<Session> {
        val needle = term.normalizeForSearch()
        return program.sessions.filter { it.searchIndex.contains(needle) }
    }

    @Test
    fun `carrega a programacao completa`() {
        assertEquals(3, program.days.size)
        assertEquals(80, program.sessions.size)
        assertEquals(144, program.sessions.sumOf { it.works.size })
    }

    @Test
    fun `ids sao unicos`() {
        val ids = program.sessions.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun `todos os tipos sao reconhecidos`() {
        assertEquals(24, program.sessions.count { it.type == SessionType.ST })
        assertEquals(19, program.sessions.count { it.type == SessionType.SL })
        assertEquals(23, program.sessions.count { it.type == SessionType.OF })
        assertEquals(7, program.sessions.count { it.type == SessionType.PA })
        assertEquals(4, program.sessions.count { it.type == SessionType.TR })
    }

    @Test
    fun `horarios sao convertidos em minutos`() {
        val manha = program.sessions.first { it.time.startsWith("9h00") }
        val tarde = program.sessions.first { it.time.startsWith("14h00") }
        assertEquals(540, manha.startMinute)
        assertEquals(840, tarde.startMinute)
        assertTrue(program.sessions.none { it.startMinute == 0 })
    }

    @Test
    fun `busca ignora acentos e caixa`() {
        val comAcento = search("Gonçalves").map { it.id }.toSet()
        val semAcento = search("Goncalves").map { it.id }.toSet()
        assertTrue(comAcento.isNotEmpty())
        assertEquals(comAcento, semAcento)
        assertEquals(search("unicamp").size, search("UNICAMP").size)
    }

    @Test
    fun `busca alcanca autores dentro dos trabalhos`() {
        assertTrue(search("Izabela Domingues").any { it.id == "ST1" })
    }

    @Test
    fun `busca alcanca coordenacao de sessao livre`() {
        assertTrue(search("Rômulo Silveira").any { it.type == SessionType.SL })
    }

    @Test
    fun `termo inexistente nao retorna nada`() {
        assertTrue(search("zzzqqqxyz").isEmpty())
    }

    @Test
    fun `atividades simultaneas viram conflito na agenda`() {
        val simultaneas = program.sessions
            .filter { it.day == "26" && it.time.startsWith("9h00") }
            .take(2)
            .map { it.id }
            .toSet()

        val agenda = buildAgenda(program, simultaneas)
        assertEquals(1, agenda.size)
        assertTrue(agenda.single().hasConflict)
    }

    @Test
    fun `horarios distintos nao geram conflito`() {
        val distintas = setOf(
            program.sessions.first { it.day == "26" && it.time.startsWith("9h00") }.id,
            program.sessions.first { it.day == "27" && it.time.startsWith("14h00") }.id
        )

        val agenda = buildAgenda(program, distintas)
        assertEquals(2, agenda.size)
        assertTrue(agenda.none { it.hasConflict })
        assertEquals(agenda.map { it.day.key }, agenda.map { it.day.key }.sorted())
    }

    @Test
    fun `agenda vazia e id desconhecido sao tratados`() {
        assertTrue(buildAgenda(program, emptySet()).isEmpty())
        assertTrue(buildAgenda(program, setOf("NAO_EXISTE")).isEmpty())
    }

    @Test
    fun `favoritar tudo preserva todas as atividades`() {
        val agenda = buildAgenda(program, program.sessions.map { it.id }.toSet())
        assertEquals(80, agenda.sumOf { it.sessions.size })
        assertFalse(agenda.isEmpty())
    }
}
