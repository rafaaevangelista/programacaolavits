package org.lavits.programacao

import android.content.Context

/**
 * Única ponte com o Android na camada de dados: lê o arquivo embutido em
 * assets e entrega o texto ao [ProgramParser]. Nenhum acesso à rede.
 */
object ProgramLoader {

    private const val ASSET_NAME = "programacao.json"

    fun load(context: Context): Program {
        val raw = context.assets.open(ASSET_NAME)
            .bufferedReader(Charsets.UTF_8)
            .use { it.readText() }
        return ProgramParser.parse(raw)
    }
}
