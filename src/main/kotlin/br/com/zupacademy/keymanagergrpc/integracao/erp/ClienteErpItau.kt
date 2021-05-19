package br.com.zupacademy.keymanagergrpc.integracao.erp

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client

@Client("\${erp-itau.url}")
interface ClienteErpItau {

    @Get("/api/v1/clientes/{clienteId}/contas")
    fun consultaConta(@PathVariable clienteId: String, @QueryValue tipo: String): HttpResponse<ContaResponse>
}