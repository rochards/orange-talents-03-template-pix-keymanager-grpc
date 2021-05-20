package br.com.zupacademy.keymanagergrpc.pix.consulta

import br.com.zupacademy.keymanagergrpc.integracao.bcb.ClienteBcb
import br.com.zupacademy.keymanagergrpc.integracao.bcb.PixKeyDetailsResponse
import br.com.zupacademy.keymanagergrpc.integracao.bcb.common.*
import br.com.zupacademy.keymanagergrpc.pix.*
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class ConsultaChavePixEndpointTest {

    @Inject
    lateinit var repository: ChavePixRepository

    @Inject
    lateinit var clienteBcb: ClienteBcb

    @Inject
    lateinit var grpcClient: KeyManagerConsultaServiceGrpc.KeyManagerConsultaServiceBlockingStub

    private val clienteId = UUID.randomUUID().toString()
    private lateinit var chavePixExistente: ChavePix

    // por padrão o método abaixo roda em uma transação isolada
    @BeforeEach
    fun setUp() {
        chavePixExistente = ChavePix(
            clienteId, "parker.aranha@gmail.com", TipoChave.EMAIL, TipoConta.CONTA_CORRENTE, LocalDateTime.now()
        )
        repository.save(chavePixExistente)
    }

    // por padrão o método abaixo roda em uma transação isolada
    @AfterEach
    fun tearDown() {
        repository.deleteAll()
    }

    @Test
    fun `deve retornar uma Chave Pix buscando pelo identificador da chave e do cliente`() {
        val pixKeyDetailsResponse = buildPixKeyDetailsResponse().copy(createdAt = chavePixExistente.registradaNoBcbEm)

        Mockito.`when`(clienteBcb.buscaChavePix("parker.aranha@gmail.com"))
            .thenReturn(HttpResponse.ok(pixKeyDetailsResponse))


        val response = grpcClient.consultaChavePix(
            ConsultaChavePixRequest.newBuilder()
                .setChavePix(
                    ConsultaChavePixRequest.ChavePix.newBuilder()
                        .setId(chavePixExistente.id!!)
                        .setErpClienteId(clienteId)
                        .build()
                )
                .build()
        )


        with(response) {
            assertEquals(chavePixExistente.id.toString(), this.chaveId)
            assertEquals(clienteId, this.erpClienteId)
            assertEquals(ConsultaChavePixResponse.TipoChave.EMAIL, this.tipoChave)
            assertEquals("parker.aranha@gmail.com", this.chave)
            assertEquals(pixKeyDetailsResponse.owner.name, this.titular.nome)
            assertEquals(pixKeyDetailsResponse.owner.taxIdNumber, this.titular.cpf)
            assertEquals("ITAÚ UNIBANCO S.A.", this.conta.nomeInstituicao)
            assertEquals(pixKeyDetailsResponse.bankAccount.branch, this.conta.agencia)
            assertEquals(pixKeyDetailsResponse.bankAccount.accountNumber, this.conta.numero)
            assertEquals(ConsultaChavePixResponse.TipoConta.CONTA_CORRENTE, this.conta.tipoConta)
        }
    }

    @Test
    fun `deve retornar uma Chave Pix buscando apenas pela valor da chave`() {
        val pixKeyDetailsResponse = buildPixKeyDetailsResponse().copy(createdAt = chavePixExistente.registradaNoBcbEm)

        Mockito.`when`(clienteBcb.buscaChavePix("parker.aranha@gmail.com"))
            .thenReturn(HttpResponse.ok(pixKeyDetailsResponse))


        val response = grpcClient.consultaChavePix(
            ConsultaChavePixRequest.newBuilder()
                .setChave("parker.aranha@gmail.com")
                .build()
        )


        with(response) {
            assertTrue(this.chaveId.isBlank())
            assertTrue(this.erpClienteId.isBlank())
            assertEquals(ConsultaChavePixResponse.TipoChave.EMAIL, this.tipoChave)
            assertEquals("parker.aranha@gmail.com", this.chave)
            assertEquals(pixKeyDetailsResponse.owner.name, this.titular.nome)
            assertEquals(pixKeyDetailsResponse.owner.taxIdNumber, this.titular.cpf)
            assertEquals("ITAÚ UNIBANCO S.A.", this.conta.nomeInstituicao)
            assertEquals(pixKeyDetailsResponse.bankAccount.branch, this.conta.agencia)
            assertEquals(pixKeyDetailsResponse.bankAccount.accountNumber, this.conta.numero)
            assertEquals(ConsultaChavePixResponse.TipoConta.CONTA_CORRENTE, this.conta.tipoConta)
        }
    }

    @Test
    fun `não deve retornar uma Chave Pix, buscando pelo identificador da chave e do cliente, quando essa não estiver registrada localmente`() {
        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.consultaChavePix(
                ConsultaChavePixRequest.newBuilder()
                    .setChavePix(
                        ConsultaChavePixRequest.ChavePix.newBuilder()
                            .setId(Long.MAX_VALUE)
                            .setErpClienteId(clienteId)
                            .build()
                    )
                    .build()
            )
        }


        with(exception) {
            assertEquals(Status.NOT_FOUND.code, this.status.code)
            assertEquals(
                "chave '${Long.MAX_VALUE}' não encontrada para o cliente '$clienteId'",
                this.status.description
            )
        }
    }

    @Test
    fun `não deve retornar uma Chave Pix, buscando pelo valor da chave, quando essa não estiver registrada no Banco Central`() {
        Mockito.`when`(clienteBcb.buscaChavePix("parker.aranha@gmail.com"))
            .thenReturn(HttpResponse.notFound())


        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.consultaChavePix(
                ConsultaChavePixRequest.newBuilder()
                    .setChave("parker.aranha@gmail.com")
                    .build()
            )
        }

        with(exception) {
            assertEquals(Status.NOT_FOUND.code, this.status.code)
            assertEquals(
                "chave 'parker.aranha@gmail.com' não encontrada no Banco Central",
                this.status.description
            )
        }
    }

    @Test
    fun `não deve retornar uma Chave Pix quando o cliente não preencheu a requisição corretamente`() {
        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.consultaChavePix(
                ConsultaChavePixRequest.newBuilder().build()
            )
        }

        with(exception) {
            assertEquals(Status.INVALID_ARGUMENT.code, this.status.code)
        }
    }

    private fun buildPixKeyDetailsResponse(): PixKeyDetailsResponse {
        return PixKeyDetailsResponse(
            keyType = KeyType.EMAIL,
            key = "parker.aranha@gmail.com",
            bankAccount = BankAccount(
                participant = "60701190",
                branch = "0001",
                accountNumber = "291900",
                accountType = AccountType.CACC
            ),
            owner = Owner(
                type = OwnerType.NATURAL_PERSON,
                name = "Peter Parker",
                taxIdNumber = "02467781054"
            ),
            createdAt = LocalDateTime.now()
        )
    }

    @MockBean(ClienteBcb::class)
    fun clienteBcb(): ClienteBcb {
        return Mockito.mock(ClienteBcb::class.java)
    }

    @Factory
    class Client {
        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel)
                : KeyManagerConsultaServiceGrpc.KeyManagerConsultaServiceBlockingStub? {
            return KeyManagerConsultaServiceGrpc.newBlockingStub(channel)
        }
    }
}