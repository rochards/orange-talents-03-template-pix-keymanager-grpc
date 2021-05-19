package br.com.zupacademy.keymanagergrpc.integracao.bcb

import br.com.zupacademy.keymanagergrpc.integracao.bcb.common.BankAccount
import br.com.zupacademy.keymanagergrpc.integracao.bcb.common.KeyType
import br.com.zupacademy.keymanagergrpc.integracao.bcb.common.Owner

data class CreatePixKeyRequest(
    val keyType: KeyType,
    val key: String?,
    val bankAccount: BankAccount,
    val owner: Owner
)