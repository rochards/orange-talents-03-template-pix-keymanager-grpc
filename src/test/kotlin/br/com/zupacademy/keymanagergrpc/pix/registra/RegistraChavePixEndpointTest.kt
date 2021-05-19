package br.com.zupacademy.keymanagergrpc.pix.registra

import br.com.zupacademy.keymanagergrpc.erp.ClienteErpItau
import br.com.zupacademy.keymanagergrpc.erp.ContaResponse
import br.com.zupacademy.keymanagergrpc.erp.Instituicao
import br.com.zupacademy.keymanagergrpc.erp.Titular
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
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/* Necessário desabilitar o control transacional, ou seja transactional = false, pois o gRPC Server executa em uma
* thread separada e acaba não participando do controle transacional, evitando assim alguns problemas
* */
@MicronautTest(transactional = false)
internal class RegistraChavePixEndpointTest {

    @Inject
    lateinit var repository: ChavePixRepository

    @Inject
    lateinit var clienteErpItau: ClienteErpItau // está sendo mockado abaixo

    @Inject
    lateinit var grpcClient: KeyManagerRegistraServiceGrpc.KeyManagerRegistraServiceBlockingStub

    private val clienteId = UUID.randomUUID().toString()
    private val contaResponse = ContaResponse(
        "CONTA_CORRENTE",
        Instituicao("ITAÚ UNIBANCO S.A.", "60701190"), "0001", "123455",
        Titular(clienteId, "Peter Parker", "86135457004")
    )

    @BeforeEach
    fun setUp() {
        repository.deleteAll()
    }

    @Test
    @DisplayName("deve criar uma nova chave do tipo RANDOM")
    fun registraChavePixTeste01() {
        `when`(clienteErpItau.consultaConta(clienteId, contaResponse.tipo))
            .thenReturn(HttpResponse.ok(contaResponse))

        val response = grpcClient.registraChavePix(
            RegistraChavePixRequest.newBuilder()
                .setErpClienteId(clienteId)
                .setTipoChave(RegistraChavePixRequest.TipoChave.RANDOM)
                .setTipoConta(RegistraChavePixRequest.TipoConta.CONTA_CORRENTE)
                .build()
        )

        assertNotNull(response.id)
        val chaveCriada = repository.findById(response.id).get()
        with(chaveCriada) {
            assertEquals(clienteId, this.erpClienteId)
            assertNotNull(this.chave)
            assertEquals(TipoChave.RANDOM, this.tipoChave)
            assertEquals(TipoConta.CONTA_CORRENTE, this.tipoConta)
        }
    }

    @Test
    @DisplayName("deve criar uma nova chave do tipo CPF")
    fun registraChavePixTeste02() {
        `when`(clienteErpItau.consultaConta(clienteId, contaResponse.tipo))
            .thenReturn(HttpResponse.ok(contaResponse))

        val response = grpcClient.registraChavePix(
            RegistraChavePixRequest.newBuilder()
                .setErpClienteId(clienteId)
                .setChave(contaResponse.titular.cpf)
                .setTipoChave(RegistraChavePixRequest.TipoChave.CPF)
                .setTipoConta(RegistraChavePixRequest.TipoConta.CONTA_CORRENTE)
                .build()
        )

        assertNotNull(response.id)
        val chaveCriada = repository.findById(response.id).get()
        with(chaveCriada) {
            assertEquals(clienteId, this.erpClienteId)
            assertEquals(contaResponse.titular.cpf, this.chave)
            assertEquals(TipoChave.CPF, this.tipoChave)
            assertEquals(TipoConta.CONTA_CORRENTE, this.tipoConta)
        }
    }

    @Test
    @DisplayName("deve criar uma nova chave do tipo TELEFONE_CELULAR")
    fun registraChavePixTeste03() {
        `when`(clienteErpItau.consultaConta(clienteId, contaResponse.tipo))
            .thenReturn(HttpResponse.ok(contaResponse))

        val response = grpcClient.registraChavePix(
            RegistraChavePixRequest.newBuilder()
                .setErpClienteId(clienteId)
                .setChave("+5534996637441")
                .setTipoChave(RegistraChavePixRequest.TipoChave.TELEFONE_CELULAR)
                .setTipoConta(RegistraChavePixRequest.TipoConta.CONTA_CORRENTE)
                .build()
        )

        assertNotNull(response.id)
        val chaveCriada = repository.findById(response.id).get()
        with(chaveCriada) {
            assertEquals( clienteId, this.erpClienteId)
            assertEquals("+5534996637441", this.chave)
            assertEquals(TipoChave.TELEFONE_CELULAR, this.tipoChave)
            assertEquals(TipoConta.CONTA_CORRENTE, this.tipoConta)
        }
    }

    @Test
    @DisplayName("deve criar uma nova chave do tipo EMAIL")
    fun registraChavePixTeste04() {
        `when`(clienteErpItau.consultaConta(clienteId, contaResponse.tipo))
            .thenReturn(HttpResponse.ok(contaResponse))

        val response = grpcClient.registraChavePix(
            RegistraChavePixRequest.newBuilder()
                .setErpClienteId(clienteId)
                .setChave("parker.aranha@gmail.com")
                .setTipoChave(RegistraChavePixRequest.TipoChave.EMAIL)
                .setTipoConta(RegistraChavePixRequest.TipoConta.CONTA_CORRENTE)
                .build()
        )

        assertNotNull(response.id)
        val chaveCriada = repository.findById(response.id).get()
        with(chaveCriada) {
            assertEquals(clienteId, this.erpClienteId)
            assertEquals("parker.aranha@gmail.com", this.chave)
            assertEquals(TipoChave.EMAIL, this.tipoChave)
            assertEquals(TipoConta.CONTA_CORRENTE, this.tipoConta)
        }
    }

    @Test
    @DisplayName("não deve criar chave duplicada")
    fun registraChavePixTeste05() {
        repository.save(ChavePix(clienteId, contaResponse.titular.cpf, TipoChave.CPF, TipoConta.CONTA_CORRENTE))

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.registraChavePix(
                RegistraChavePixRequest.newBuilder()
                    .setErpClienteId(clienteId)
                    .setChave(contaResponse.titular.cpf)
                    .setTipoChave(RegistraChavePixRequest.TipoChave.CPF)
                    .setTipoConta(RegistraChavePixRequest.TipoConta.CONTA_CORRENTE)
                    .build())
        }

        with(exception) {
            assertEquals(Status.ALREADY_EXISTS.code, this.status.code)
            assertEquals("chave pix '${contaResponse.titular.cpf}' já cadastrada", this.status.description,)
        }
    }

    @Test
    @DisplayName("não deve criar chave quando o cliente não for encontrado no Itaú")
    fun registraChavePixTeste06() {
        `when`(clienteErpItau.consultaConta(clienteId, contaResponse.tipo))
            .thenReturn(HttpResponse.notFound())

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.registraChavePix(
                RegistraChavePixRequest.newBuilder()
                    .setErpClienteId(clienteId)
                    .setChave(contaResponse.titular.cpf)
                    .setTipoChave(RegistraChavePixRequest.TipoChave.CPF)
                    .setTipoConta(RegistraChavePixRequest.TipoConta.CONTA_CORRENTE)
                    .build())
        }

        with(exception) {
            assertEquals(Status.INVALID_ARGUMENT.code, this.status.code)
            assertEquals("cliente não encontrado no Itaú", this.status.description)
        }
    }

    @Test
    @DisplayName("não deve criar chave quando os dados estiverem inválidos")
    fun registraChavePixTeste07() {
        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.registraChavePix(
                RegistraChavePixRequest.newBuilder()
                    .build())
        }

        with(exception) {
            assertEquals(Status.INVALID_ARGUMENT.code, this.status.code)
        }
    }


    @MockBean(ClienteErpItau::class)
    fun clienteItau(): ClienteErpItau {
        return mock(ClienteErpItau::class.java)
    }

    @Factory
    class Client {
        /*
        * Devo criar um cliente RPC para fazer as chamadas para o meu servidor, assim preciso fornecer um bean para o
        * grpcClient definido lá em cima. Esse cliente se comportará semelhante ao MockMvc do Spring.
        * */
        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel)
                : KeyManagerRegistraServiceGrpc.KeyManagerRegistraServiceBlockingStub? {
            return KeyManagerRegistraServiceGrpc.newBlockingStub(channel)
        }
        /* em @GrpcChannel("endereco:porta") vc deveria passar o endereço do servidor gRPC e a porta, porém a
        * anotação @MicronautTest levanta o servidor em uma porta aleatória, tornando assim praticamente impossível
        * passar essas informações. Passando então apenas grpc-server ou GrpcServerChannel.NAME isso fica transparente
        *  para vc. */
    }
}