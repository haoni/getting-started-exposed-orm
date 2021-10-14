import com.microsoft.sqlserver.jdbc.SQLServerDataSource
import orc.OrcFileReader
import orc.OrcFileWriter
import org.apache.hadoop.conf.Configuration
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import service.ClienteService
import service.JogoService
import service.LocacaoJogoService
import service.LocacaoService
import table.Cliente
import table.Jogo
import table.Locacao
import table.LocacaoJogo
import java.util.*


fun main(args: Array<String>){

    //Data Source=172.27.128.1;Initial Catalog=getting-started-exposed-orm;Persist Security Info=True;User ID=sa;Password=***********
    val dataSource = SQLServerDataSource()
    dataSource.serverName = "192.168.0.76"
    dataSource.portNumber = 1433
    dataSource.lockTimeout = 5
    dataSource.databaseName = "getting-started-exposed-orm"
    dataSource.user = "sa"
    dataSource.setPassword("@nuvem1234")

    Database.connect(dataSource)

    transaction {

        SchemaUtils.create(
            Jogo,
            Cliente,
            Locacao,
            LocacaoJogo)
    }
    println("Tabelas criadas!")


    val clienteService = ClienteService()
//    clienteService.CriarClientes()

    val jogoService = JogoService()
//    jogoService.CriarJogos()

//    val clientes = clienteService.GetAllClientes()

    val locacaoService = LocacaoService()
//    locacaoService.CriarLocacao(clientes)

//    val locacoes = locacaoService.GetAllLocacao()

    val jogos = jogoService.GetAllJogos()

    val locacaoJogoService = LocacaoJogoService()
//    locacaoJogoService.CriarLocacaoJogos(locacoes, jogos)

//    val jogosLocados = locacaoJogoService.GetAllLocacaoJogo()

    val data: MutableList<Map<String, Any>> = LinkedList()

    // Este deu ruim: Exception in thread "main" java.lang.OutOfMemoryError: Java heap space
//    jogosLocados.forEach {
//        data.add(java.util.Map.of<String, Any>("Id", it.Id, "LocacaoId", it.LocacaoId, "JogoId", it.LocacaoId))
//        println("Adicionado na Mutablelist: "+ it.Id.toString())
//    }
//    OrcFileWriter.write(Configuration(), "orders.orc", "struct<Id:string,LocacaoId:string,JogoId:string>", data)

    println("Preparando dados...")
    jogos.forEach {
        data.add(java.util.Map.of<String, Any>("Id", it.Id, "Nome", it.Nome))
    }
    println("Dados carregados...")

    println("Criando arquivo .orc")
    OrcFileWriter.write(Configuration(), "poc_orc_eh_nois.orc", "struct<Id:string,Nome:string>", data)
    println("Arquivo.orc criado com sucesso!")

    println("Iniciando a leitura .orc")
    val rows = OrcFileReader.read(Configuration(), "poc_orc_eh_nois.orc")
    for (row in rows) {
        println(row)
    }
    println("Fim do processamento!")
}