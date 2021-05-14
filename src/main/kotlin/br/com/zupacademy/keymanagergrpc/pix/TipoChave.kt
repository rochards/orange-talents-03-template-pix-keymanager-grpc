package br.com.zupacademy.keymanagergrpc.pix

enum class TipoChave {
    RANDOM {
        override fun isValid(chave: String): Boolean {
            return true;
        }
    }, CPF {
        override fun isValid(chave: String): Boolean {
            return chave.matches("^[0-9]{11}$".toRegex())
        }
    }, TELEFONE_CELULAR {
        override fun isValid(chave: String): Boolean {
            return chave.matches("^\\+[1-9][0-9]\\d{1,14}$".toRegex())
        }
    }, EMAIL {
        override fun isValid(chave: String): Boolean {
            return chave.matches("^[\\w-.]+@([\\w-]+.)+[\\w-]{2,4}$".toRegex())
        }
    };

    abstract fun isValid(chave: String) : Boolean
}
