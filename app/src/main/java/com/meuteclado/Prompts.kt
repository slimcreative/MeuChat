package com.meuteclado

object Prompts {

    /** Tema da conversa -> instrução específica para a IA. */
    val topics: LinkedHashMap<String, String> = linkedMapOf(
        "Resposta" to "Responda à última mensagem recebida de forma que mantenha a conversa viva e dê vontade de continuar.",
        "Abertura" to "Escreva a PRIMEIRA mensagem para começar a conversa com esta pessoa, com base no contexto visível (perfil, foto, história, interesses). Curta, natural, sem clichê.",
        "Entrelinhas" to "Leia o que a pessoa pode estar sentindo ou querendo dizer e sugira respostas que respondam ao sentimento, não só ao texto literal.",
        "Flerte" to "Crie interesse leve: um elogio específico, uma provocação brincalhona ou uma pergunta que convide a pessoa a falar de si.",
        "Encontro" to "Proponha ou responda sobre marcar um encontro de forma clara, confiante e fácil de aceitar, com uma opção concreta de dia ou lugar."
    )

    val tones: List<String> = listOf("leve", "confiante", "provocativo", "carinhoso", "engraçado", "direto")

    const val SYSTEM =
        "Você ajuda o usuário a escrever mensagens em conversas de chat. Você recebe um print da conversa. " +
        "As mensagens do usuário ficam à direita; as da outra pessoa, à esquerda. " +
        "Escreva como o usuário escreveria: português do Brasil, informal, curto, como uma pessoa real digita no celular. " +
        "Nada de manipulação, mentira, pressão ou insistência. " +
        "Se a pessoa demonstrar desinteresse ou disser não, a melhor resposta respeita isso."

    fun build(topic: String, tone: String, jsonMode: Boolean): String {
        val task = topics[topic] ?: topics.getValue("Resposta")
        val out = if (jsonMode)
            "Responda APENAS com JSON, sem texto antes ou depois: {\"leitura\": \"...\", \"options\": [\"...\", \"...\", \"...\"]}"
        else
            "Entregue pela ferramenta: 'leitura' com uma frase curta sobre o que a pessoa quis dizer e 'options' com 3 mensagens diferentes entre si."
        return """
            Este é o print da conversa.
            1) Leia as últimas mensagens e entenda o que a outra pessoa quer ou sente.
            2) Tarefa: $task
            3) Tom: $tone.
            $out
        """.trimIndent()
    }
}
