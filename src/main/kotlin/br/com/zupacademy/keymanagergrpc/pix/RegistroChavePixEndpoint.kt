package br.com.zupacademy.keymanagergrpc.pix

import br.com.zupacademy.keymanagergrpc.erp.ErpService
import br.com.zupacademy.keymanagergrpc.pix.ChavePixRequest.TipoChave.*
import io.grpc.Status
import io.grpc.stub.StreamObserver
import io.micronaut.http.HttpStatus
import java.util.*
import javax.inject.Singleton
import javax.validation.ConstraintViolationException

@Singleton
class RegistroChavePixEndpoint(
    private val repository: ChavePixRepository,
    private val erpService: ErpService
) : KeyManagerServiceGrpc.KeyManagerServiceImplBase() {

    override fun registraChavePix(request: ChavePixRequest, responseObserver: StreamObserver<ChavePixResponse>) {


        val validacoes = ChavePixValidation(request)
        if (!validacoes.errorMessage().isNullOrBlank()) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                .withDescription(validacoes.errorMessage())
                .asRuntimeException())

            return
        }
        if (repository.existsByChave(request.chave)) {
            responseObserver.onError(Status.ALREADY_EXISTS
                .withDescription("'chave' já cadastrada")
                .asRuntimeException())

            return
        }

        val clienteResponse = try {
            erpService.consultaCliente(request.erpClienteId)
        } catch (e: Exception) {
            responseObserver.onError(Status.INTERNAL
                .withDescription(e.localizedMessage)
                .asRuntimeException())

            return
        }

        if (clienteResponse.status == HttpStatus.NOT_FOUND) {
            responseObserver.onError(Status.NOT_FOUND
                .withDescription("'clienteId'  não encontrado")
                .asRuntimeException())

            return
        }

        val chavePix = request.toModel()

        try {
            repository.save(chavePix)
        } catch (e: ConstraintViolationException) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                .withDescription("dados de entrada inválidos")
                .asRuntimeException())

            return
        }

        responseObserver.onNext(ChavePixResponse.newBuilder()
            .setId(chavePix.id!!)
            .build())
        responseObserver.onCompleted()
    }
}

fun ChavePixRequest.toModel(): ChavePix {
    return ChavePix(
        erpClienteId = this.erpClienteId,
        chave = if (this.tipoChave == RANDOM) UUID.randomUUID().toString() else this.chave,
        tipoChave = TipoChave.valueOf(this.tipoChave.name),
        tipoConta = TipoConta.valueOf(this.tipoConta.name)
    )
}