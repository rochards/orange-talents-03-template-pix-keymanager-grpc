package br.com.zupacademy.keymanagergrpc.pix

import br.com.zupacademy.keymanagergrpc.pix.ChavePixRequest.TipoChave.*

class ChavePixValidation(private val request: ChavePixRequest) {

    fun errorMessage(): String? {
        return when {
            request.erpClienteId.isBlank() -> "'erpClienteId' não dever ser em branco"
            request.chave.isBlank() && request.tipoChave != RANDOM -> "'chave' não deve estar em branco"
            request.tipoChave == UNKNOWN_CHAVE -> "'tipoChave' inválido"
            request.tipoConta == ChavePixRequest.TipoConta.UNKNOWN_CONTA -> "'tipoConta' inválido"
            !TipoChave.valueOf(request.tipoChave.name).isValid(request.chave) -> "'chave' em formato inválido para " +
                    "'tipoChave'"
            else -> null
        }
    }
}