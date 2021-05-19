package br.com.zupacademy.keymanagergrpc.pix.registra

import br.com.zupacademy.keymanagergrpc.integracao.bcb.ClienteBcb
import br.com.zupacademy.keymanagergrpc.integracao.erp.ClienteErpItau
import br.com.zupacademy.keymanagergrpc.pix.ChavePix
import br.com.zupacademy.keymanagergrpc.pix.ChavePixRepository
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.validation.Validated
import javax.inject.Singleton
import javax.validation.Valid

@Validated
@Singleton
class NovaChavePixService(
    private val repository: ChavePixRepository,
    private val clienteErpItau: ClienteErpItau,
    private val clienteBcb: ClienteBcb
) {
    fun registraChavePix(@Valid novaChave: NovaChavePix): ChavePix {

        if (repository.existsByChave(novaChave.chave))
            throw ChavePixExistenteException("chave pix '${novaChave.chave}' já cadastrada")

        val contaResponse = clienteErpItau.consultaConta(novaChave.erpClienteId, novaChave.tipoConta!!.name)
        if (contaResponse.status == HttpStatus.NOT_FOUND)
            throw IllegalArgumentException("cliente não encontrado no Itaú")

        val createPixKeyResponse = try {
            val conta = contaResponse.body()!!
            clienteBcb.registraChavePix(novaChave.toBcbKeyRequest(conta))
        } catch (e: HttpClientResponseException) {
            if (e.status == HttpStatus.UNPROCESSABLE_ENTITY)
                throw ChavePixExistenteException("chave pix '${novaChave.chave}' já cadastrada no Banco Central")

            throw e
        }

        val chavePix = novaChave.toModel(createPixKeyResponse.body()!!.key!!, createPixKeyResponse.body()!!.createdAt!!)
        repository.save(chavePix)

        return chavePix
    }
}