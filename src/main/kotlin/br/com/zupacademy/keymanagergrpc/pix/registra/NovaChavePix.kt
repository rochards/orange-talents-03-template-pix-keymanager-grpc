package br.com.zupacademy.keymanagergrpc.pix.registra

import br.com.zupacademy.keymanagergrpc.pix.ChavePix
import br.com.zupacademy.keymanagergrpc.pix.TipoChave
import br.com.zupacademy.keymanagergrpc.pix.TipoConta
import io.micronaut.core.annotation.Introspected
import java.util.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Introspected
data class NovaChavePix(
    @field:NotBlank
    val erpClienteId: String,

    @field:Size(max = 77)
    val chave: String,

    @field:NotNull
    val tipoChave: TipoChave?,

    @field:NotNull
    val tipoConta: TipoConta?
) {

    fun toModel(): ChavePix {
        return ChavePix(
            erpClienteId = this.erpClienteId,
            chave = when (this.tipoChave) {
                TipoChave.RANDOM -> UUID.randomUUID().toString()
                else -> this.chave
            },
            tipoChave = this.tipoChave!!,
            tipoConta = this.tipoConta!!
        )
    }
}