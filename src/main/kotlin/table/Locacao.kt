package table

import Dto.LocacaoDto
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.`java-time`.CurrentDateTime
import org.jetbrains.exposed.sql.`java-time`.datetime
import table.Jogo.primaryKey

object Locacao : Table() {
    val Id = uuid("Id").autoGenerate().uniqueIndex().primaryKey()
    val ClienteId = uuid("ClienteId").references(Cliente.Id)
    val DataLocacao = datetime("DataLocacao").defaultExpression(CurrentDateTime())

    fun mapToDto(it: ResultRow) = LocacaoDto (
        Id = it[Id],
        ClienteId = it[ClienteId],
        DataLocacao = it[DataLocacao]
    )
}