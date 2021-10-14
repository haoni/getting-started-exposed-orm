package service

import Dto.JogoDto
import Dto.LocacaoDto
import com.microsoft.sqlserver.jdbc.SQLServerDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import table.Jogo
import table.Locacao

class JogoService() {

    fun CriarJogos() {
        val pseudosJogoNome = arrayOf("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "R", "S", "T", "U", "V", "X", "Z")

        for (x in pseudosJogoNome.indices) {
            for (y in pseudosJogoNome.indices) {
                val n = pseudosJogoNome[x]+pseudosJogoNome[y]
                InserirJogo("JOGO - ($n)")
            }
        }
    }

    fun InserirJogo(nomeJogo : String) {
        transaction {
            Jogo.insert {
                it[Nome] = nomeJogo
            }
        }
    }

    fun GetAllJogos() : List<JogoDto> {
        val jogos = transaction {
            Jogo.selectAll().map { Jogo.mapToDto(it) }
        }

        return jogos
    }
}