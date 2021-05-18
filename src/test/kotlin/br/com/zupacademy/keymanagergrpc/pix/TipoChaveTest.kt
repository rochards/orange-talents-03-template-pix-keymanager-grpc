package br.com.zupacademy.keymanagergrpc.pix

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class TipoChaveTest {

    @Test
    fun `chave RANDOM deve ser válida se fornecida luna ou vazia`() {
        with(TipoChave.RANDOM) {
            assertTrue(isValid(null))
            assertTrue(isValid(""))
        }
    }

    @Test
    fun `chave RANDOM não deve ser válida se fornecida com algum valor`() {
        with(TipoChave.RANDOM) {
            assertFalse(isValid("something here"))
        }
    }

    @Test
    fun `chave CPF deve ser válida se fornecida com valor válido`() {
        with(TipoChave.CPF) {
            assertTrue(isValid("43412520039"))
        }
    }

    @Test
    fun `chave CPF não deve ser válida se fornecida com valor inválido`() {
        with(TipoChave.CPF) {
            assertFalse(isValid("434.125.200-39"))
            assertFalse(isValid("11122233344"))
            assertFalse(isValid(""))
            assertFalse(isValid(null))
        }
    }

    @Test
    fun `chave TELEFONE_CECULAR deve ser válida se fornecida com valor válido`() {
        with(TipoChave.TELEFONE_CELULAR) {
            assertTrue(isValid("+553496637441"))
        }
    }

    @Test
    fun `chave TELEFONE_CECULAR não deve ser válida se fornecida com valor inválido`() {
        with(TipoChave.TELEFONE_CELULAR) {
            assertFalse(isValid("+55349663-7441"))
            assertFalse(isValid(""))
            assertFalse(isValid(null))
        }
    }

    @Test
    fun `chave EMAIL deve ser válida se fornecida com valor válido`() {
        with(TipoChave.EMAIL) {
            assertTrue(isValid("parker.aranha@gmail.com"))
        }
    }

    @Test
    fun `chave EMAIL não deve ser válida se fornecida com valor inválido`() {
        with(TipoChave.EMAIL) {
            assertFalse(isValid("parker.aranhamail.com"))
            assertFalse(isValid(""))
            assertFalse(isValid(null))
        }
    }
}