package br.com.zupacademy.keymanagergrpc.pix.registra

import br.com.zupacademy.keymanagergrpc.grpc.KeyManagerRegistraServiceGrpc
import br.com.zupacademy.keymanagergrpc.grpc.RegistraChavePixRequest
import br.com.zupacademy.keymanagergrpc.grpc.RegistraChavePixResponse
import br.com.zupacademy.keymanagergrpc.grpc.TipoChave.UNKNOWN_CHAVE
import br.com.zupacademy.keymanagergrpc.grpc.TipoConta.UNKNOWN_CONTA
import br.com.zupacademy.keymanagergrpc.pix.*
import br.com.zupacademy.keymanagergrpc.pix.exception.handler.ExceptionHandler
import io.grpc.stub.StreamObserver
import javax.inject.Singleton

@Singleton
@ExceptionHandler
class RegistraChavePixEndpoint(private val service: NovaChavePixService)
    : KeyManagerRegistraServiceGrpc.KeyManagerRegistraServiceImplBase() {

    override fun registraChavePix(
        request: RegistraChavePixRequest,
        responseObserver: StreamObserver<RegistraChavePixResponse>
    ) {

        val novaChave = request.toModel()
        val chaveCriada = service.registraChavePix(novaChave)

        responseObserver.onNext(RegistraChavePixResponse.newBuilder()
            .setId(chaveCriada.id!!)
            .build())
        responseObserver.onCompleted()
    }
}

fun RegistraChavePixRequest.toModel(): NovaChavePix {
    return NovaChavePix(
        erpClienteId = this.erpClienteId,
        chave = this.chave,
        tipoChave = when (this.tipoChave) {
            UNKNOWN_CHAVE -> null
            else -> TipoChave.valueOf(this.tipoChave.name)
        },
        tipoConta = when (this.tipoConta) {
            UNKNOWN_CONTA -> null
            else -> TipoConta.valueOf(this.tipoConta.name)
        }
    )
}