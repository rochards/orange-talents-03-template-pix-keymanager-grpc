package br.com.zupacademy.keymanagergrpc.pix

import io.micronaut.validation.validator.constraints.EmailValidator
import org.hibernate.validator.internal.constraintvalidators.hv.br.CPFValidator

enum class TipoChave {
    RANDOM {
        override fun isValid(chave: String?): Boolean {
            return chave.isNullOrBlank()
        }
    }, CPF {
        override fun isValid(chave: String?): Boolean {
            if (chave.isNullOrBlank()) return false

            if (!chave.matches("^[0-9]{11}$".toRegex())) return false

            return CPFValidator().run {
                initialize(null)
                isValid(chave, null)
            }
        }
    }, TELEFONE_CELULAR {
        override fun isValid(chave: String?): Boolean {
            if (chave.isNullOrBlank()) return false

            return chave.matches("^\\+[1-9][0-9]\\d{1,14}$".toRegex())
        }
    }, EMAIL {
        override fun isValid(chave: String?): Boolean {
//            return chave.matches("^[\\w-.]+@([\\w-]+.)+[\\w-]{2,4}$".toRegex())
            if (chave.isNullOrBlank()) return false

            return EmailValidator().run {
                initialize(null)
                isValid(chave, null)
            }
        }
    };

    abstract fun isValid(chave: String?) : Boolean
}
