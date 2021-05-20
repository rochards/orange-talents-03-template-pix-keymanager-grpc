package br.com.zupacademy.keymanagergrpc.integracao.bcb

import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.http.client.annotation.Client

@Client("\${bcb.url}/api/v1/pix/keys")
interface ClienteBcb {

    @Get("/{key}")
    @Produces(MediaType.APPLICATION_XML)
    fun buscaChavePix(@PathVariable key: String): HttpResponse<PixKeyDetailsResponse>

    @Post
    @Produces(MediaType.APPLICATION_XML)
    @Consumes(MediaType.APPLICATION_XML)
    fun registraChavePix(@Body request: CreatePixKeyRequest): HttpResponse<CreatePixKeyResponse>

    @Delete("/{key}")
    @Produces(MediaType.APPLICATION_XML)
    @Consumes(MediaType.APPLICATION_XML)
    fun removeChavePix(@PathVariable key: String, @Body request: DeletePixKeyRequest)
            : HttpResponse<DeletePixKeyResponse>
}