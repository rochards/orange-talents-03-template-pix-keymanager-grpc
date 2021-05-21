package br.com.zupacademy.keymanagergrpc.pix.registra

import br.com.zupacademy.keymanagergrpc.integracao.bcb.CreatePixKeyRequest
import br.com.zupacademy.keymanagergrpc.integracao.bcb.common.*
import br.com.zupacademy.keymanagergrpc.integracao.erp.ContaResponse
import br.com.zupacademy.keymanagergrpc.pix.ChavePix
import br.com.zupacademy.keymanagergrpc.pix.TipoChave
import br.com.zupacademy.keymanagergrpc.pix.TipoConta
import br.com.zupacademy.keymanagergrpc.pix.exception.validation.ValidPixKey
import br.com.zupacademy.keymanagergrpc.pix.exception.validation.ValidUUID
import io.micronaut.core.annotation.Introspected
import java.time.LocalDateTime
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@ValidPixKey
@Introspected
data class NovaChavePix(
    @field:NotBlank
    @field:ValidUUID
    val erpClienteId: String,

    @field:Size(max = 77)
    val chave: String,

    @field:NotNull(message = "desconhecida")
    val tipoChave: TipoChave?,

    @field:NotNull(message = "desconhecida")
    val tipoConta: TipoConta?
) {

    fun toModel(key: String, createdAt: LocalDateTime): ChavePix {
        return ChavePix(
            erpClienteId = this.erpClienteId,
            chave = key,
            tipoChave = this.tipoChave!!,
            tipoConta = this.tipoConta!!,
            registradaNoBcbEm = createdAt
        )
    }

    fun toBcbKeyRequest(conta: ContaResponse): CreatePixKeyRequest {

        return CreatePixKeyRequest(
            keyType = when (this.tipoChave!!) {
                TipoChave.TELEFONE_CELULAR -> KeyType.PHONE
                else -> KeyType.valueOf(this.tipoChave.name)
            },
            key = chave,
            bankAccount = BankAccount(
                participant = conta.instituicao.ispb,
                branch = conta.agencia,
                accountNumber = conta.numero,
                accountType = when (this.tipoConta!!) {
                    TipoConta.CONTA_CORRENTE -> AccountType.CACC
                    TipoConta.CONTA_POUPANCA -> AccountType.CVGS
                }
            ),
            owner = Owner(
                type = OwnerType.NATURAL_PERSON, // ATENÇÃO AQUI, por enquanto estamos trabalhando apenas com PF
                name = conta.titular.nome,
                taxIdNumber = conta.titular.cpf
            )
        )
    }
}