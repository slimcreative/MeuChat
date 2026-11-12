package com.meuteclado

object Prompts {

    /** Tema da conversa -> instrução específica para a IA. */
    val topics: LinkedHashMap<String, String> = linkedMapOf(
        "Abertura" to "Escreva a PRIMEIRA mensagem para começar a conversa com esta pessoa, com base no contexto visível (perfil, foto, história, interesses). Curta, natural, sem clichê.",
        "Resposta" to "Responda à última mensagem recebida de forma que mantenha a conversa viva e dê vontade de continuar.",
        "Entrelinhas" to "Primeiro explique em uma frase o que a pessoa pode estar sentindo ou querendo dizer. Depois sugira respostas que respondam ao sentimento e não só ao texto literal.",
        "Flerte" to "Crie interesse leve: um elogio específico, uma provocação brincalhona ou uma pergunta que convide a pessoa a falar de si.",
        "Encontro" to "Proponha ou responda sobre marcar um encontro de forma clara, confiante e fácil de aceitar, com uma opção concreta de dia ou lugar."
    )

    val tones: List<String> = listOf("leve", "confiante", "provocativo", "carinhoso", "engraçado", "direto")

    private const val RULES =
        "Regras: português do Brasil. Nada de manipulação, mentira, pressão ou insistência. " +
        "Se a pessoa demonstrar desinteresse ou disser não, a melhor resposta respeita isso. " +
        "Mensagens curtas, como uma pessoa real digitaria."

    fun build(topic: String, tone: String): String {
        val task = topics[topic].orEmpty()
        return """
            Esta é a tela de um app de mensagens.
            1) Identifique quem enviou a última mensagem e o que essa pessoa quer ou sente.
            2) Tarefa: $task
            3) Tom: $tone.
            $RULES
            Responda APENAS com JSON: {"options": ["...", "...", "..."]}
        """.trimIndent()
    }
}
