package br.com.zupacademy.keymanagergrpc.integracao.bcb

import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.http.client.annotation.Client

@Client("\${bcb.url}/api/v1/pix/keys")
interface ClienteBcb {

    @Post
    @Produces(MediaType.APPLICATION_XML)
    @Consumes(MediaType.APPLICATION_XML)
    fun registraChavePix(@Body request: CreatePixKeyRequest): HttpResponse<CreatePixKeyResponse>
}