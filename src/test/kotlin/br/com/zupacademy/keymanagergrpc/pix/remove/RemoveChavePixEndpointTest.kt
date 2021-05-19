package br.com.zupacademy.keymanagergrpc.pix.remove

import br.com.zupacademy.keymanagergrpc.pix.*
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
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
    lateinit var grpcClient: KeyManagerRemoveServiceGrpc.KeyManagerRemoveServiceBlockingStub

    lateinit var chavePix: ChavePix
    private val clienteId = UUID.randomUUID().toString()

    @BeforeEach
    fun setUp() {
        chavePix = ChavePix(clienteId, "parker.aranha@gmail.com", TipoChave.EMAIL, TipoConta.CONTA_CORRENTE)
        repository.save(chavePix)
    }

    @Test
    fun `deve remover chave existente`() {
        val response = grpcClient.removeChavePix(
            RemoveChavePixRequest.newBuilder()
                .setChaveId(chavePix.id!!)
                .setErpClienteId(clienteId)
                .build()
        )

        with(response) {
            assertEquals(clienteId, this.erpClienteId)
            assertEquals("parker.aranha@gmail.com", this.chave)
        }
    }

    @Test
    fun `não deve remover chave se ela não pertencer ao cliente`() {
        val outroClienteId = UUID.randomUUID().toString()

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.removeChavePix(
                RemoveChavePixRequest.newBuilder()
                    .setChaveId(chavePix.id!!)
                    .setErpClienteId(outroClienteId)
                    .build()
            )
        }

        with(exception) {
            assertEquals(Status.NOT_FOUND.code, this.status.code)
            assertEquals(
                "chave '${chavePix.id}' não encontrada para o cliente '$outroClienteId'",
                this.status.description
            )
        }
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