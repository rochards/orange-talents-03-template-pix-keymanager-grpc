package br.com.zupacademy.keymanagergrpc.pix.consulta

import br.com.zupacademy.keymanagergrpc.integracao.bcb.ClienteBcb
import br.com.zupacademy.keymanagergrpc.integracao.bcb.Instituicoes
import br.com.zupacademy.keymanagergrpc.integracao.bcb.PixKeyDetailsResponse
import br.com.zupacademy.keymanagergrpc.integracao.bcb.common.AccountType
import br.com.zupacademy.keymanagergrpc.integracao.bcb.common.KeyType
import br.com.zupacademy.keymanagergrpc.pix.ChavePixRepository
import br.com.zupacademy.keymanagergrpc.pix.ConsultaChavePixResponse
import br.com.zupacademy.keymanagergrpc.pix.exception.validation.ValidUUID
import br.com.zupacademy.keymanagergrpc.pix.remove.ChavePixInexistenteException
import com.google.protobuf.Timestamp
import io.micronaut.validation.Validated
import java.time.ZoneId
import javax.inject.Singleton
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Validated
@Singleton
class ConsultaChavePixService(
    private val clienteBcb: ClienteBcb,
    private val repository: ChavePixRepository
) {

    fun consulta(@Size(max = 77) chave: String): ConsultaChavePixResponse {
        val pixKeyDetailsResponse = consultaBcb(chave)

        return pixKeyDetailsResponse.toDTO()
    }

    fun consulta(@NotNull chaveId: Long, @NotBlank @ValidUUID erpClienteId: String): ConsultaChavePixResponse {
        val chavePix = repository.findByIdAndErpClienteId(chaveId, erpClienteId)
            ?: throw ChavePixInexistenteException("chave '$chaveId' não encontrada para o cliente '$erpClienteId'")

        val pixKeyDetailsResponse = consultaBcb(chavePix.chave)

        return pixKeyDetailsResponse.toDTO(chaveId.toString(), erpClienteId)
    }

    private fun consultaBcb(chave: String): PixKeyDetailsResponse {
        val bcbResponse = clienteBcb.buscaChavePix(chave)
        return bcbResponse.body()
            ?: throw ChavePixInexistenteException("chave '$chave' não encontrada no Banco Central")
    }
}

fun PixKeyDetailsResponse.toDTO(
    chaveId: String = "",
    erpClienteId: String = ""
): ConsultaChavePixResponse {

    return ConsultaChavePixResponse.newBuilder()
        .setChaveId(chaveId)
        .setErpClienteId(erpClienteId)
        .setTipoChave(
            when (this.keyType) {
                KeyType.RANDOM -> ConsultaChavePixResponse.TipoChave.RANDOM
                KeyType.CPF -> ConsultaChavePixResponse.TipoChave.CPF
                KeyType.EMAIL -> ConsultaChavePixResponse.TipoChave.EMAIL
                KeyType.PHONE -> ConsultaChavePixResponse.TipoChave.TELEFONE_CELULAR
                else -> ConsultaChavePixResponse.TipoChave.UNKNOWN_CHAVE
            }
        )
        .setChave(this.key)
        .setTitular(
            ConsultaChavePixResponse.Titular.newBuilder()
                .setNome(this.owner.name)
                .setCpf(this.owner.taxIdNumber)
                .build()
        )
        .setConta(
            ConsultaChavePixResponse.Conta.newBuilder()
                .setNomeInstituicao(Instituicoes.nome(this.bankAccount.participant))
                .setAgencia(this.bankAccount.branch)
                .setNumero(this.bankAccount.accountNumber)
                .setTipoConta(
                    when (this.bankAccount.accountType) {
                        AccountType.CACC -> ConsultaChavePixResponse.TipoConta.CONTA_CORRENTE
                        AccountType.CVGS -> ConsultaChavePixResponse.TipoConta.CONTA_POUPANCA
                    }
                )
                .build()
        )
        .setCriadaEm(this.createdAt.let {
            val instant = it.atZone(ZoneId.of("UTC")).toInstant()
            Timestamp.newBuilder()
                .setSeconds(instant.epochSecond)
                .setNanos(instant.nano)
                .build()
        })
        .build()
}