package br.com.zupacademy.keymanagergrpc.pix

import io.micronaut.data.annotation.Repository
import io.micronaut.data.repository.CrudRepository


@Repository
interface ChavePixRepository : CrudRepository<ChavePix, Long> {

    fun existsByChave(chave: String): Boolean
    fun findByIdAndErpClienteId(id: Long, erpClienteId: String): ChavePix?
    fun findByErpClienteId(erpClienteId: String): List<ChavePix>
}