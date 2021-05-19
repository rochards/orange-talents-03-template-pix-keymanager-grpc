package br.com.zupacademy.keymanagergrpc.pix

import java.time.LocalDateTime
import javax.persistence.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull


@Entity
class ChavePix(
    @field:NotBlank
    @Column(nullable = false)
    val erpClienteId: String,

    @field:NotNull
    @Column(nullable = false, unique = true)
    val chave: String,

    @field:NotNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val tipoChave: TipoChave,

    @field:NotNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val tipoConta: TipoConta,

    @field:NotNull
    @Column(nullable = false, updatable = false)
    val registradaNoBcbEm: LocalDateTime
) {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
}