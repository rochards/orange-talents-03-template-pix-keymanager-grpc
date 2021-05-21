package br.com.zupacademy.keymanagergrpc.pix.lista

import br.com.zupacademy.keymanagergrpc.grpc.ListaChavesPixResponse
import br.com.zupacademy.keymanagergrpc.pix.ChavePixRepository
import br.com.zupacademy.keymanagergrpc.pix.exception.validation.ValidUUID
import io.micronaut.validation.Validated
import javax.inject.Singleton
import javax.validation.constraints.NotBlank

@Validated
@Singleton
class ListaChavesPixService(private val repository: ChavePixRepository) {

    fun lista(@NotBlank @ValidUUID erpClienteId: String): List<ListaChavesPixResponse.ChavePixResponse> {
        val chavesPix = repository.findByErpClienteId(erpClienteId)
        return ListaChavesToGrpcResponse.converte(chavesPix)
    }
}
