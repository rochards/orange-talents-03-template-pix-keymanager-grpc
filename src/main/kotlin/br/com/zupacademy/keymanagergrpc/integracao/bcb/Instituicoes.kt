package br.com.zupacademy.keymanagergrpc.integracao.bcb

class Instituicoes {

    companion object {
        fun nome(ispb: String): String {
            return when (ispb) {
                "00000000" -> "Banco do Brasil S.A"
                "00038166" -> "Banco Central do Brasil"
                "00360305" -> "CAIXA ECONOMICA FEDERAL"
                "17298092" -> "Banco Itaú BBA S.A"
                "60701190" -> "ITAÚ UNIBANCO S.A. "
                else -> "ainda não registrado"
            }
        }
    }
}