package br.com.zupacademy.keymanagergrpc.erp

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.client.annotation.Client

@Client("\${erp-itau.url}")
interface ErpService {

    @Get("/api/v1/clientes/{clienteId}")
    fun consultaCliente(@PathVariable clienteId: String): HttpResponse<ClienteResponse>
}