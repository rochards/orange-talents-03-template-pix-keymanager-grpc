package br.com.zupacademy.keymanagergrpc.pix.remove

import br.com.zupacademy.keymanagergrpc.pix.ChavePix
import br.com.zupacademy.keymanagergrpc.pix.ChavePixRepository
import javax.inject.Singleton

@Singleton
class RemoveChavePixService(private val repository: ChavePixRepository) {

    fun remove(chaveId: Long, erpClienteId: String): ChavePix {

        val chavePix =
            repository.findByIdAndErpClienteId(chaveId, erpClienteId) ?: throw ChavePixInexistenteException(
                "chave '$chaveId' não encontrada para o cliente '$erpClienteId'"
            )

        repository.deleteById(chavePix.id!!)

        return chavePix
    }
}
