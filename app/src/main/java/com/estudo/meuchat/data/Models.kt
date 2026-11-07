package com.estudo.meuchat.data

data class Conta(
    val id: String,
    val nome: String,
    val numero: String,
    val ativa: Boolean
)

data class Conversa(
    val id: String,
    val nome: String,
    val ultimaMensagem: String,
    val hora: String,
    val naoLidas: Int
)

data class Mensagem(
    val id: String,
    val texto: String,
    val minha: Boolean,
    val hora: String,
    var apagada: Boolean = false
)

/** Dados de exemplo pra o app rodar sem backend. Troque por dados reais quando plugar o Matrix. */
object Mock {
    val contas = listOf(
        Conta("1", "Pessoal", "+55 11 90000-0001", ativa = true),
        Conta("2", "Trabalho", "+55 11 90000-0002", ativa = false),
    )

    val conversas = listOf(
        Conversa("c1", "Ana", "Bom dia! 🌞", "09:12", 2),
        Conversa("c2", "Grupo Família", "Carlos: bora almoçar?", "08:40", 0),
        Conversa("c3", "Bruno", "manda o arquivo", "ontem", 0),
        Conversa("c4", "Marina", "kkkk demais", "ontem", 5),
    )

    fun mensagensIniciais(): MutableList<Mensagem> = mutableListOf(
        Mensagem("m1", "Oi, tudo bem?", minha = false, hora = "09:10"),
        Mensagem("m2", "Tudo e você?", minha = true, hora = "09:11"),
        Mensagem("m3", "Bom dia! 🌞", minha = false, hora = "09:12"),
    )
}
