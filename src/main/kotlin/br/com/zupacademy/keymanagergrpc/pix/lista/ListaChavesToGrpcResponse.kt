package br.com.zupacademy.keymanagergrpc.pix.lista

import br.com.zupacademy.keymanagergrpc.grpc.ListaChavesPixResponse
import br.com.zupacademy.keymanagergrpc.grpc.TipoChave
import br.com.zupacademy.keymanagergrpc.grpc.TipoConta
import br.com.zupacademy.keymanagergrpc.pix.ChavePix
import com.google.protobuf.Timestamp
import java.time.LocalDateTime
import java.time.ZoneId

class ListaChavesToGrpcResponse {

    companion object {
        fun converte(chavesPix: List<ChavePix>): List<ListaChavesPixResponse.ChavePixResponse> {
            return chavesPix.map { chavePix ->
                ListaChavesPixResponse.ChavePixResponse.newBuilder()
                    .setPixId(chavePix.id!!)
                    .setErpClienteId(chavePix.erpClienteId)
                    .setChave(chavePix.chave)
                    .setTipoChave(TipoChave.valueOf(chavePix.tipoChave.name))
                    .setTipoConta(TipoConta.valueOf(chavePix.tipoConta.name))
                    .setCriadaEm(chavePix.registradaNoBcbEm.toGprcTimestamp())
                    .build()
            }
        }

        fun LocalDateTime.toGprcTimestamp(): Timestamp {
            val instante = this.atZone(ZoneId.of("UTC")).toInstant()

            return Timestamp.newBuilder()
                .setSeconds(instante.epochSecond)
                .setNanos(instante.nano)
                .build()
        }
    }
}