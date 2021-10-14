package table

import Dto.JogoDto
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table

object Jogo : Table() {
    val Id = uuid("Id").autoGenerate().uniqueIndex().primaryKey()
    val Nome = varchar("Nome", 250)

    fun mapToDto(it: ResultRow) = JogoDto(
        Id = it[Id],
        Nome = it[Nome]
    )
}