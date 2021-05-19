package br.com.zupacademy.keymanagergrpc.pix.registra

import br.com.zupacademy.keymanagergrpc.integracao.bcb.ClienteBcb
import br.com.zupacademy.keymanagergrpc.integracao.bcb.CreatePixKeyRequest
import br.com.zupacademy.keymanagergrpc.integracao.bcb.CreatePixKeyResponse
import br.com.zupacademy.keymanagergrpc.integracao.bcb.common.*
import br.com.zupacademy.keymanagergrpc.integracao.erp.ClienteErpItau
import br.com.zupacademy.keymanagergrpc.integracao.erp.ContaResponse
import br.com.zupacademy.keymanagergrpc.integracao.erp.Instituicao
import br.com.zupacademy.keymanagergrpc.integracao.erp.Titular
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
import java.time.LocalDateTime
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
    lateinit var clienteBcb: ClienteBcb

    @Inject
    lateinit var grpcClient: KeyManagerRegistraServiceGrpc.KeyManagerRegistraServiceBlockingStub

    private val clienteId = UUID.randomUUID().toString()

    @BeforeEach
    fun setUp() {
        repository.deleteAll()
    }

    @Test
    @DisplayName("deve criar uma nova chave do tipo RANDOM")
    fun registraChavePixTeste01() {
        val contaResponse = buildContaResponse()
        val createPixKeyRequest = buildCreatePixKeyRequest(contaResponse)
        val randomKey = UUID.randomUUID().toString();
        val createPixKeyResponse = buildCreatePixKeyResponse(createPixKeyRequest).copy(key = randomKey)


        `when`(clienteErpItau.consultaConta(clienteId, contaResponse.tipo))
            .thenReturn(HttpResponse.ok(contaResponse))

        `when`(clienteBcb.registraChavePix(createPixKeyRequest))
            .thenReturn(HttpResponse.ok(createPixKeyResponse))


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
            assertEquals(this.chave, randomKey)
            assertEquals(TipoChave.RANDOM, this.tipoChave)
            assertEquals(TipoConta.CONTA_CORRENTE, this.tipoConta)
            assertNotNull(this.registradaNoBcbEm)
        }
    }

    @Test
    @DisplayName("deve criar uma nova chave do tipo CPF")
    fun registraChavePixTeste02() {
        val contaResponse = buildContaResponse()
        val createPixKeyRequest = buildCreatePixKeyRequest(contaResponse).copy(
            key = contaResponse.titular.cpf, keyType = KeyType.CPF
        )
        val createPixKeyResponse = buildCreatePixKeyResponse(createPixKeyRequest)


        `when`(clienteErpItau.consultaConta(clienteId, contaResponse.tipo))
            .thenReturn(HttpResponse.ok(contaResponse))

        `when`(clienteBcb.registraChavePix(createPixKeyRequest))
            .thenReturn(HttpResponse.ok(createPixKeyResponse))


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
            assertNotNull(this.registradaNoBcbEm)
        }
    }

    @Test
    @DisplayName("deve criar uma nova chave do tipo TELEFONE_CELULAR")
    fun registraChavePixTeste03() {
        val contaResponse = buildContaResponse()
        val telefone = "+5534996637441"
        val createPixKeyRequest = buildCreatePixKeyRequest(contaResponse).copy(key = telefone, keyType = KeyType.PHONE)
        val createPixKeyResponse = buildCreatePixKeyResponse(createPixKeyRequest)


        `when`(clienteErpItau.consultaConta(clienteId, contaResponse.tipo))
            .thenReturn(HttpResponse.ok(contaResponse))

        `when`(clienteBcb.registraChavePix(createPixKeyRequest))
            .thenReturn(HttpResponse.ok(createPixKeyResponse))


        val response = grpcClient.registraChavePix(
            RegistraChavePixRequest.newBuilder()
                .setErpClienteId(clienteId)
                .setChave(telefone)
                .setTipoChave(RegistraChavePixRequest.TipoChave.TELEFONE_CELULAR)
                .setTipoConta(RegistraChavePixRequest.TipoConta.CONTA_CORRENTE)
                .build()
        )


        assertNotNull(response.id)
        val chaveCriada = repository.findById(response.id).get()
        with(chaveCriada) {
            assertEquals(clienteId, this.erpClienteId)
            assertEquals(telefone, this.chave)
            assertEquals(TipoChave.TELEFONE_CELULAR, this.tipoChave)
            assertEquals(TipoConta.CONTA_CORRENTE, this.tipoConta)
            assertNotNull(this.registradaNoBcbEm)
        }
    }

    @Test
    @DisplayName("deve criar uma nova chave do tipo EMAIL")
    fun registraChavePixTeste04() {
        val contaResponse = buildContaResponse()
        val email = "parker.aranha@gmail.com"
        val createPixKeyRequest = buildCreatePixKeyRequest(contaResponse).copy(key = email, keyType = KeyType.EMAIL)
        val createPixKeyResponse = buildCreatePixKeyResponse(createPixKeyRequest)


        `when`(clienteErpItau.consultaConta(clienteId, contaResponse.tipo))
            .thenReturn(HttpResponse.ok(contaResponse))

        `when`(clienteBcb.registraChavePix(createPixKeyRequest))
            .thenReturn(HttpResponse.ok(createPixKeyResponse))


        val response = grpcClient.registraChavePix(
            RegistraChavePixRequest.newBuilder()
                .setErpClienteId(clienteId)
                .setChave(email)
                .setTipoChave(RegistraChavePixRequest.TipoChave.EMAIL)
                .setTipoConta(RegistraChavePixRequest.TipoConta.CONTA_CORRENTE)
                .build()
        )


        assertNotNull(response.id)
        val chaveCriada = repository.findById(response.id).get()
        with(chaveCriada) {
            assertEquals(clienteId, this.erpClienteId)
            assertEquals(email, this.chave)
            assertEquals(TipoChave.EMAIL, this.tipoChave)
            assertEquals(TipoConta.CONTA_CORRENTE, this.tipoConta)
            assertNotNull(this.registradaNoBcbEm)
        }
    }

    @Test
    @DisplayName("não deve criar chave duplicada")
    fun registraChavePixTeste05() {
        val contaResponse = buildContaResponse()

        repository.save(
            ChavePix(
                clienteId,
                contaResponse.titular.cpf,
                TipoChave.CPF,
                TipoConta.CONTA_CORRENTE,
                LocalDateTime.now()
            )
        )


        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.registraChavePix(
                RegistraChavePixRequest.newBuilder()
                    .setErpClienteId(clienteId)
                    .setChave(contaResponse.titular.cpf)
                    .setTipoChave(RegistraChavePixRequest.TipoChave.CPF)
                    .setTipoConta(RegistraChavePixRequest.TipoConta.CONTA_CORRENTE)
                    .build()
            )
        }


        with(exception) {
            assertEquals(Status.ALREADY_EXISTS.code, this.status.code)
            assertEquals("chave pix '${contaResponse.titular.cpf}' já cadastrada", this.status.description)
        }
    }

    @Test
    @DisplayName("não deve criar chave quando o cliente não for encontrado no Itaú")
    fun registraChavePixTeste06() {
        val contaResponse = buildContaResponse()

        `when`(clienteErpItau.consultaConta(clienteId, contaResponse.tipo))
            .thenReturn(HttpResponse.notFound())


        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.registraChavePix(
                RegistraChavePixRequest.newBuilder()
                    .setErpClienteId(clienteId)
                    .setChave(contaResponse.titular.cpf)
                    .setTipoChave(RegistraChavePixRequest.TipoChave.CPF)
                    .setTipoConta(RegistraChavePixRequest.TipoConta.CONTA_CORRENTE)
                    .build()
            )
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
                    .build()
            )
        }


        with(exception) {
            assertEquals(Status.INVALID_ARGUMENT.code, this.status.code)
        }
    }

    private fun buildContaResponse(): ContaResponse {
        return ContaResponse(
            tipo = "CONTA_CORRENTE",
            instituicao = Instituicao("ITAÚ UNIBANCO S.A.", "60701190"),
            agencia = "0001",
            numero = "123455",
            titular = Titular(clienteId, "Peter Parker", "86135457004")
        )
    }

    private fun buildCreatePixKeyRequest(conta: ContaResponse): CreatePixKeyRequest {
        return CreatePixKeyRequest(
            keyType = KeyType.RANDOM,
            key = "",
            bankAccount = BankAccount(
                participant = conta.instituicao.ispb,
                branch = conta.agencia,
                accountNumber = conta.numero,
                accountType = when (conta.tipo) {
                    "CONTA_CORRENTE" -> AccountType.CACC
                    else -> AccountType.CVGS
                }
            ),
            owner = Owner(
                type = OwnerType.NATURAL_PERSON,
                name = conta.titular.nome,
                taxIdNumber = conta.titular.cpf
            ),
        )
    }

    private fun buildCreatePixKeyResponse(createPixKeyRequest: CreatePixKeyRequest): CreatePixKeyResponse {
        return CreatePixKeyResponse(
            keyType = createPixKeyRequest.keyType,
            key = createPixKeyRequest.key,
            bankAccount = createPixKeyRequest.bankAccount.copy(),
            owner = createPixKeyRequest.owner.copy(),
            createdAt = LocalDateTime.now()
        )
    }

    @MockBean(ClienteErpItau::class)
    fun clienteItau(): ClienteErpItau {
        return mock(ClienteErpItau::class.java)
    }

    @MockBean(ClienteBcb::class)
    fun clienteBcb(): ClienteBcb {
        return mock(ClienteBcb::class.java)
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