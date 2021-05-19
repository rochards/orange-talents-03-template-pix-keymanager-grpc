package br.com.zupacademy.keymanagergrpc.integracao.bcb.common

data class BankAccount(
    val participant: String,
    val branch: String,
    val accountNumber: String,
    val accountType: AccountType
)

enum class AccountType {
    CACC, CVGS
}