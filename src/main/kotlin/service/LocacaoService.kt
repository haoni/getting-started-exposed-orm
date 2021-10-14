package service

import Dto.ClienteDto
import Dto.LocacaoDto
import com.microsoft.sqlserver.jdbc.SQLServerDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import table.Locacao
import java.time.LocalDateTime
import java.util.*

class LocacaoService() {

    fun CriarLocacao(clientes: List<ClienteDto>) {
        clientes.forEach {
            InserrirLocacao(it.Id, LocalDateTime.now())
        }
    }

    fun InserrirLocacao(clienteId: UUID, dataLocacao: LocalDateTime) {
        transaction {
            Locacao.insert {
                it[ClienteId] = clienteId
                it[DataLocacao] = dataLocacao
            }
        }
    }

    fun GetAllLocacao() : List<LocacaoDto> {
        val locacoes = transaction {
            Locacao.selectAll().map { Locacao.mapToDto(it) }
        }

        return locacoes
    }
}