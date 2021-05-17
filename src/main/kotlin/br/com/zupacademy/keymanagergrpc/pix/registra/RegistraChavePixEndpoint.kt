package br.com.zupacademy.keymanagergrpc.pix.registra

import br.com.zupacademy.keymanagergrpc.pix.*
import io.grpc.stub.StreamObserver
import javax.inject.Singleton

@Singleton
class RegistraChavePixEndpoint(private val service: NovaChavePixService) : KeyManagerServiceGrpc
.KeyManagerServiceImplBase() {

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
            RegistraChavePixRequest.TipoChave.UNKNOWN_CHAVE -> null
            else -> TipoChave.valueOf(this.tipoChave.name)
        },
        tipoConta = when (this.tipoConta) {
            RegistraChavePixRequest.TipoConta.UNKNOWN_CONTA -> null
            else -> TipoConta.valueOf(this.tipoConta.name)
        }
    )
}