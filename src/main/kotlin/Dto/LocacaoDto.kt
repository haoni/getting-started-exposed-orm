package Dto

import java.time.LocalDateTime
import java.util.*

data class LocacaoDto (
    val Id : UUID,
    val ClienteId : UUID,
    val DataLocacao : LocalDateTime
)