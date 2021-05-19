package br.com.zupacademy.keymanagergrpc.pix.remove

import br.com.zupacademy.keymanagergrpc.integracao.bcb.ClienteBcb
import br.com.zupacademy.keymanagergrpc.integracao.bcb.DeletePixKeyRequest
import br.com.zupacademy.keymanagergrpc.pix.ChavePix
import br.com.zupacademy.keymanagergrpc.pix.ChavePixRepository
import javax.inject.Singleton

@Singleton
class RemoveChavePixService(
    private val repository: ChavePixRepository,
    private val clienteBcb: ClienteBcb
) {

    fun remove(chaveId: Long, erpClienteId: String): ChavePix {

        val chavePix =
            repository.findByIdAndErpClienteId(chaveId, erpClienteId) ?: throw ChavePixInexistenteException(
                "chave '$chaveId' não encontrada para o cliente '$erpClienteId'"
            )

        // 60701190 -> está hard coded pq é o ispb do Itaú
        val deleteChavePixResponse = clienteBcb.removeChavePix(
            chavePix.chave, DeletePixKeyRequest(chavePix.chave,"60701190")
        )
        deleteChavePixResponse.body()
            ?: throw ChavePixInexistenteException("chave corresponde ao id '$chaveId' não encontrada no Banco Central")

        repository.deleteById(chavePix.id!!)

        return chavePix
    }
}
