package br.com.zupacademy.keymanagergrpc.pix.registra

import br.com.zupacademy.keymanagergrpc.integracao.erp.ClienteErpItau
import br.com.zupacademy.keymanagergrpc.pix.ChavePix
import br.com.zupacademy.keymanagergrpc.pix.ChavePixRepository
import io.micronaut.http.HttpStatus
import io.micronaut.validation.Validated
import java.lang.IllegalArgumentException
import javax.inject.Singleton
import javax.validation.Valid

@Validated
@Singleton
class NovaChavePixService(
    private val repository: ChavePixRepository,
    private val clienteErpItau: ClienteErpItau
) {
    fun registraChavePix(@Valid novaChave: NovaChavePix): ChavePix {

        if (repository.existsByChave(novaChave.chave))
            throw ChavePixExistenteException("chave pix '${novaChave.chave}' já cadastrada")

        val contaResponse = clienteErpItau.consultaConta(novaChave.erpClienteId, novaChave.tipoConta!!.name)
        if (contaResponse.status == HttpStatus.NOT_FOUND)
            throw IllegalArgumentException("cliente não encontrado no Itaú")

        val chave = novaChave.toModel()
        repository.save(chave)

        return chave
    }

}