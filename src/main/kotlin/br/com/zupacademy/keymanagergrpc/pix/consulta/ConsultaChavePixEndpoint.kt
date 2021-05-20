package br.com.zupacademy.keymanagergrpc.pix.consulta

import br.com.zupacademy.keymanagergrpc.pix.ConsultaChavePixRequest
import br.com.zupacademy.keymanagergrpc.pix.ConsultaChavePixResponse
import br.com.zupacademy.keymanagergrpc.pix.KeyManagerConsultaServiceGrpc
import br.com.zupacademy.keymanagergrpc.pix.exception.handler.ExceptionHandler
import io.grpc.stub.StreamObserver
import javax.inject.Singleton

@Singleton
@ExceptionHandler
class ConsultaChavePixEndpoint(private val service: ConsultaChavePixService)
    : KeyManagerConsultaServiceGrpc.KeyManagerConsultaServiceImplBase() {

    override fun consultaChavePix(
        request: ConsultaChavePixRequest,
        responseObserver: StreamObserver<ConsultaChavePixResponse>
    ) {

        val consultaChavePixResponse = when {
            !request.chave.isNullOrBlank() -> service.consulta(request.chave)
            else -> service.consulta(request.chavePix.id, request.chavePix.erpClienteId)
        }

        responseObserver.onNext(consultaChavePixResponse)
        responseObserver.onCompleted()
    }
}