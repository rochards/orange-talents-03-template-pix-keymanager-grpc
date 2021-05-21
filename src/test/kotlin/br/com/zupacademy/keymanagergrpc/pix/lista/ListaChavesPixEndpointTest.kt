package br.com.zupacademy.keymanagergrpc.pix.lista

import br.com.zupacademy.keymanagergrpc.grpc.KeyManagerListaServiceGrpc
import br.com.zupacademy.keymanagergrpc.grpc.ListaChavesPixRequest
import br.com.zupacademy.keymanagergrpc.pix.ChavePix
import br.com.zupacademy.keymanagergrpc.pix.ChavePixRepository
import br.com.zupacademy.keymanagergrpc.pix.TipoChave
import br.com.zupacademy.keymanagergrpc.pix.TipoConta
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class ListaChavesPixEndpointTest {

    @Inject
    lateinit var repository: ChavePixRepository

    @Inject
    lateinit var grpcClient: KeyManagerListaServiceGrpc.KeyManagerListaServiceBlockingStub

    private val erpClienteId = UUID.randomUUID().toString()

    @BeforeEach
    internal fun setUp() {
        val pixRandom = ChavePix(
            erpClienteId, UUID.randomUUID().toString(), TipoChave.RANDOM, TipoConta.CONTA_CORRENTE, LocalDateTime.now()
        )
        val pixEmail = ChavePix(
            erpClienteId, "parker.aranha@gmail.com", TipoChave.EMAIL, TipoConta.CONTA_CORRENTE, LocalDateTime.now()
        )
        val pixCpf = ChavePix(
            erpClienteId, "40764442058", TipoChave.CPF, TipoConta.CONTA_CORRENTE, LocalDateTime.now()
        )
        val pixTelefone = ChavePix(
            erpClienteId, "+5534998877441", TipoChave.CPF, TipoConta.CONTA_CORRENTE, LocalDateTime.now()
        )

        repository.saveAll(listOf(pixRandom, pixEmail, pixCpf, pixTelefone))
    }

    @AfterEach
    internal fun tearDown() {
        repository.deleteAll()
    }

    @Test
    fun `deve retornar todas as chaves cadastradas de um cliente`() {

        val response = grpcClient.listaChavesPix(
            ListaChavesPixRequest.newBuilder()
                .setErpClienteId(erpClienteId)
                .build()
        )

        with(response) {
            assertEquals(erpClienteId, this.erpClienteId)
            assertEquals(4, this.chavesCount)
        }
    }

    @Test
    fun `deve retornar lista vazia se o cliente informado não possuir chaves cadastradas`() {

        val randomErpClienteId = UUID.randomUUID().toString()

        val response = grpcClient.listaChavesPix(
            ListaChavesPixRequest.newBuilder()
                .setErpClienteId(randomErpClienteId)
                .build()
        )

        with(response) {
            assertEquals(randomErpClienteId, this.erpClienteId)
            assertEquals(0, this.chavesCount)
        }
    }

    @Test
    fun `deve retornar erro caso o request não informe um erpClienteId vazio`() {

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.listaChavesPix(
                ListaChavesPixRequest.newBuilder()
                    .setErpClienteId("  ")
                    .build()
            )
        }

        with(exception) {
            assertEquals(Status.INVALID_ARGUMENT.code, this.status.code)
        }
    }

    @Test
    fun `deve retornar error caso a request informe um erpClienteId com UUID inválido`() {

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.listaChavesPix(
                ListaChavesPixRequest.newBuilder()
                    .setErpClienteId("invalid")
                    .build()
            )
        }

        with(exception) {
            assertEquals(Status.INVALID_ARGUMENT.code, this.status.code)
            assertEquals("lista.erpClienteId: não é um formato válido de UUID", this.status.description)
        }
    }

    @Factory
    class Client {
        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel)
                : KeyManagerListaServiceGrpc.KeyManagerListaServiceBlockingStub? {
            return KeyManagerListaServiceGrpc.newBlockingStub(channel)
        }
    }
}