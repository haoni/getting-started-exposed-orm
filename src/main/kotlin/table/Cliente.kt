package table

import Dto.ClienteDto
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table

object Cliente : Table() {
    val Id = uuid("Id").autoGenerate().uniqueIndex().primaryKey()
    val Nome = varchar("Nome", 250)

    fun mapToDto(it: ResultRow) = ClienteDto(
        Id = it[Id],
        Nome = it[Nome]
    )
}