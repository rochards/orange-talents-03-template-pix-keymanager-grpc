package br.com.zupacademy.keymanagergrpc.pix.remove

import br.com.zupacademy.keymanagergrpc.grpc.KeyManagerRemoveServiceGrpc
import br.com.zupacademy.keymanagergrpc.grpc.RemoveChavePixRequest
import br.com.zupacademy.keymanagergrpc.grpc.RemoveChavePixResponse
import br.com.zupacademy.keymanagergrpc.pix.exception.handler.ExceptionHandler
import io.grpc.stub.StreamObserver
import javax.inject.Singleton

@Singleton
@ExceptionHandler
class RemoveChavePixEndpoint(private val service: RemoveChavePixService) :
    KeyManagerRemoveServiceGrpc.KeyManagerRemoveServiceImplBase() {

    override fun removeChavePix(
        request: RemoveChavePixRequest,
        responseObserver: StreamObserver<RemoveChavePixResponse>
    ) {

        val chaveRemovida = service.remove(request.chaveId, request.erpClienteId)

        responseObserver.onNext(
            RemoveChavePixResponse.newBuilder()
                .setChave(chaveRemovida.chave)
                .setErpClienteId(chaveRemovida.erpClienteId)
                .build()
        )
        responseObserver.onCompleted()
    }
}