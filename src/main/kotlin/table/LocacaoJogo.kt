package table

import Dto.LocacaoJogoDto
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table

object LocacaoJogo : Table() {
    val Id = uuid("Id").autoGenerate().uniqueIndex().primaryKey()
    val LocacaoId = uuid("LocacaoId").references(Locacao.Id)
    val JogoId = uuid("JogoId").references(Jogo.Id)

    fun mapToDto(it: ResultRow) = LocacaoJogoDto (
        Id = it[Id],
        LocacaoId = it[LocacaoId],
        JogoId = it[JogoId]
    )
}