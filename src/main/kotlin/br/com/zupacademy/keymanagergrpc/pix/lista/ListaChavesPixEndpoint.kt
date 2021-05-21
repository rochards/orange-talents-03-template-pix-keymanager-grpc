package br.com.zupacademy.keymanagergrpc.pix.lista

import br.com.zupacademy.keymanagergrpc.grpc.KeyManagerListaServiceGrpc
import br.com.zupacademy.keymanagergrpc.grpc.ListaChavesPixRequest
import br.com.zupacademy.keymanagergrpc.grpc.ListaChavesPixResponse
import br.com.zupacademy.keymanagergrpc.pix.exception.handler.ExceptionHandler
import io.grpc.stub.StreamObserver
import javax.inject.Singleton

@Singleton
@ExceptionHandler
class ListaChavesPixEndpoint(private val service: ListaChavesPixService) :
    KeyManagerListaServiceGrpc.KeyManagerListaServiceImplBase() {

    override fun listaChavesPix(
        request: ListaChavesPixRequest,
        responseObserver: StreamObserver<ListaChavesPixResponse>
    ) {

        val chaves = service.lista(request.erpClienteId)

        responseObserver.onNext(
            ListaChavesPixResponse.newBuilder()
                .setErpClienteId(request.erpClienteId)
                .addAllChaves(chaves)
                .build()
        )
        responseObserver.onCompleted()
    }
}