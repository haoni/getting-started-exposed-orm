package service

import Dto.JogoDto
import Dto.LocacaoDto
import Dto.LocacaoJogoDto
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import table.LocacaoJogo

class LocacaoJogoService() {

    fun CriarLocacaoJogos(locacoes : List<LocacaoDto>, jogos : List<JogoDto>) {
        locacoes.forEach {
            InserirLocacaoJogo(it, jogos)
        }
    }

    fun InserirLocacaoJogo(locacao : LocacaoDto, jogos: List<JogoDto>) {
        transaction {
            jogos.forEach { p ->
                LocacaoJogo.insert {
                    it[LocacaoId] = locacao.Id
                    it[JogoId] = p.Id
                }

                println("Inserido a locação do jogo: "+ p.Nome)
            }
        }
    }

    fun GetAllLocacaoJogo() : List<LocacaoJogoDto> {
        val locacaoJogos = transaction {
            LocacaoJogo.selectAll().map { LocacaoJogo.mapToDto(it) }
        }
        return locacaoJogos
    }
}