package br.com.zupacademy.keymanagergrpc.erp

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.client.annotation.Client

@Client("http://localhost:9091/api/v1/clientes")
interface ErpService {

    @Get("{clienteId}")
    fun consultaCliente(@PathVariable clienteId: String): HttpResponse<ClienteResponse>
}