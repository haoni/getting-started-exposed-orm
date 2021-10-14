package service

import Dto.ClienteDto
import com.microsoft.sqlserver.jdbc.SQLServerDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import table.Cliente

class ClienteService() {

    fun CriarClientes() {
        val pseudosNome = arrayOf("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "R", "S", "T", "U", "V", "X", "Z")

        for (x in pseudosNome.indices) {
            for (y in pseudosNome.indices) {
                for (z in pseudosNome.indices) {
                    for (w in pseudosNome.indices) {
                        val n = pseudosNome[x]+pseudosNome[y]+pseudosNome[z]+pseudosNome[w]
                        InserirCliente(n)
                    }
                }
            }
        }
    }

    fun InserirCliente(nomeCliente : String) {
        transaction {
            Cliente.insert {
                it[Nome] = nomeCliente
            }
        }
    }

    fun GetAllClientes() : List<ClienteDto> {
        val clientes = transaction {
            Cliente.selectAll().map { Cliente.mapToDto(it) }
        }

        return clientes
    }
}