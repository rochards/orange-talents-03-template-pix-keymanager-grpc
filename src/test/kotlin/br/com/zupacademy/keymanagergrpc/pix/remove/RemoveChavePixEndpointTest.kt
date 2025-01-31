package br.com.zupacademy.keymanagergrpc.pix.remove

import br.com.zupacademy.keymanagergrpc.grpc.KeyManagerRemoveServiceGrpc
import br.com.zupacademy.keymanagergrpc.grpc.RemoveChavePixRequest
import br.com.zupacademy.keymanagergrpc.integracao.bcb.ClienteBcb
import br.com.zupacademy.keymanagergrpc.integracao.bcb.DeletePixKeyRequest
import br.com.zupacademy.keymanagergrpc.integracao.bcb.DeletePixKeyResponse
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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.Mockito.mock
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/* Necessário desabilitar o control transacional, ou seja transactional = false, pois o gRPC Server executa em uma
* thread separada e acaba não participando do controle transacional, evitando assim alguns problemas
* */
@MicronautTest(transactional = false)
internal class RemoveChavePixEndpointTest {

    @Inject
    lateinit var repository: ChavePixRepository

    @Inject
    lateinit var clienteBcb: ClienteBcb

    @Inject
    lateinit var grpcClient: KeyManagerRemoveServiceGrpc.KeyManagerRemoveServiceBlockingStub

    private lateinit var chavePixExistente: ChavePix
    private val clienteId = UUID.randomUUID().toString()

    @BeforeEach
    fun setUp() {
        chavePixExistente = ChavePix(
            clienteId, "parker.aranha@gmail.com", TipoChave.EMAIL, TipoConta.CONTA_CORRENTE, LocalDateTime.now()
        )
        repository.save(chavePixExistente)
    }

    @AfterEach
    fun tearDown() {
        repository.deleteAll()
    }

    @Test
    fun `deve remover chave existente`() {
        val deletePixKeyRequest = DeletePixKeyRequest(chavePixExistente.chave, "60701190")
        val deletePixKeyResponse = DeletePixKeyResponse(chavePixExistente.chave, "60701190", LocalDateTime.now())

        Mockito.`when`(clienteBcb.removeChavePix(chavePixExistente.chave, deletePixKeyRequest))
            .thenReturn(HttpResponse.ok(deletePixKeyResponse))


        val response = grpcClient.removeChavePix(
            RemoveChavePixRequest.newBuilder()
                .setChaveId(chavePixExistente.id!!)
                .setErpClienteId(clienteId)
                .build()
        )


        with(response) {
            assertEquals(clienteId, this.erpClienteId)
            assertEquals("parker.aranha@gmail.com", this.chave)
        }
    }

    @Test
    fun `não deve remover chave se ela for não existir`() {

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.removeChavePix(
                RemoveChavePixRequest.newBuilder()
                    .setChaveId(Long.MAX_VALUE)
                    .setErpClienteId(clienteId)
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
    fun `não deve remvoer chave se ela não for removida do BCB`() {
        val deletePixKeyRequest = DeletePixKeyRequest(chavePixExistente.chave, "60701190")

        Mockito.`when`(clienteBcb.removeChavePix(chavePixExistente.chave, deletePixKeyRequest))
            .thenReturn(HttpResponse.notFound())


        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.removeChavePix(
                RemoveChavePixRequest.newBuilder()
                    .setChaveId(chavePixExistente.id!!)
                    .setErpClienteId(clienteId)
                    .build()
            )
        }

        with(exception) {
            assertEquals(Status.NOT_FOUND.code, this.status.code)
            assertEquals(
                "chave corresponde ao id '${chavePixExistente.id}' não encontrada no Banco Central",
                this.status.description
            )
        }
    }

    @Test
    fun `não deve remover chave se ela não pertencer ao cliente`() {
        val outroClienteId = UUID.randomUUID().toString()

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.removeChavePix(
                RemoveChavePixRequest.newBuilder()
                    .setChaveId(chavePixExistente.id!!)
                    .setErpClienteId(outroClienteId)
                    .build()
            )
        }

        with(exception) {
            assertEquals(Status.NOT_FOUND.code, this.status.code)
            assertEquals(
                "chave '${chavePixExistente.id}' não encontrada para o cliente '$outroClienteId'",
                this.status.description
            )
        }
    }

    @MockBean(ClienteBcb::class)
    fun clienteBcb(): ClienteBcb {
        return mock(ClienteBcb::class.java)
    }

    @Factory
    class Client {
        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel)
                : KeyManagerRemoveServiceGrpc.KeyManagerRemoveServiceBlockingStub? {
            return KeyManagerRemoveServiceGrpc.newBlockingStub(channel)
        }
    }
}