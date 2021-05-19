package br.com.zupacademy.keymanagergrpc.integracao.bcb

data class DeletePixKeyRequest(
    val key: String,
    val participant: String
)