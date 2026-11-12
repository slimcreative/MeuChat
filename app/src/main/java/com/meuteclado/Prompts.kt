package com.meuteclado

object Prompts {

    /** Um modo de conversa: muda o estilo da IA, os temas e os tons disponíveis na telinha. */
    class Mode(
        val desc: String,
        val style: String,
        val topics: LinkedHashMap<String, String>,
        val tones: List<String>
    )

    const val DEFAULT_MODE = "Casual"

    val modes: LinkedHashMap<String, Mode> = linkedMapOf(
        "Casual" to Mode(
            desc = "Amigos, família, dia a dia",
            style = "Contexto: conversa casual com amigos, família ou conhecidos. Soe natural e descontraído, como a pessoa falaria no dia a dia.",
            topics = linkedMapOf(
                "Resposta" to "Responda à última mensagem recebida de forma natural, que mantenha a conversa viva.",
                "Puxar assunto" to "Puxe um assunto novo ou retome a conversa de forma leve, usando algo que apareceu na tela.",
                "Entrelinhas" to "Leia o que a pessoa pode estar sentindo ou querendo dizer e sugira respostas que respondam ao sentimento, não só ao texto literal.",
                "Humor" to "Responda com humor leve, uma brincadeira que combine com o clima da conversa.",
                "Combinar" to "Proponha ou responda sobre combinar algo (sair, ligar, se ver) de forma clara e fácil de aceitar."
            ),
            tones = listOf("leve", "engraçado", "carinhoso", "animado", "direto")
        ),
        "Profissional" to Mode(
            desc = "Clientes, trabalho, negócios",
            style = "Contexto: conversa profissional (cliente, fornecedor, chefe ou colega). Escreva com clareza e educação, sem gírias, com português correto, frases objetivas e postura de quem resolve. Pode ser um pouco mais longo que uma mensagem casual quando precisar explicar algo.",
            topics = linkedMapOf(
                "Resposta" to "Responda à última mensagem de forma profissional, clara e resolutiva.",
                "Atender cliente" to "Atenda o cliente: responda a dúvida ou pedido, mostre disposição e indique o próximo passo concreto.",
                "Orçamento" to "Apresente ou responda sobre preço, prazo ou proposta de forma clara e segura, valorizando o serviço. Não invente valores: use os que aparecem na tela ou deixe [valor] para o usuário preencher.",
                "Cobrar" to "Cobre um pagamento, prazo ou retorno pendente de forma educada e firme, sem constranger.",
                "Reclamação" to "Responda a uma reclamação: reconheça o problema, explique com calma o que aconteceu e proponha uma solução, sem assumir culpa que não aparece na conversa.",
                "Dizer não" to "Recuse o pedido de forma educada e firme, com um motivo curto e, se fizer sentido, uma alternativa."
            ),
            tones = listOf("cordial", "formal", "objetivo", "firme", "simpático")
        ),
        "Persuasão" to Mode(
            desc = "Convencer, vender, negociar",
            style = "Contexto: o usuário quer convencer a outra pessoa. Use persuasão honesta: mostre o benefício do ponto de vista dela, use fatos e exemplos concretos que aparecem na conversa, reciprocidade, prova social real, antecipe a objeção e termine com um próximo passo fácil de aceitar. Não invente fatos, números ou urgência que não existem.",
            topics = linkedMapOf(
                "Resposta" to "Responda à última mensagem conduzindo a conversa para o que o usuário quer.",
                "Convencer" to "Apresente o melhor argumento para a pessoa topar a ideia, focando no que ela ganha.",
                "Quebrar objeção" to "Identifique a objeção ou dúvida da pessoa e responda a ela diretamente, sem bater de frente.",
                "Fechar" to "Peça a decisão: proponha o próximo passo de forma simples, com uma pergunta fácil de responder com sim.",
                "Pedir favor" to "Faça um pedido de forma que seja fácil dizer sim: específico, pequeno e com o motivo."
            ),
            tones = listOf("confiante", "amigável", "lógico", "empático", "direto")
        ),
        "Maquiavel" to Mode(
            desc = "Estratégico, frio e calculado",
            style = "Contexto: modo estratégico. Antes de escrever, leia o jogo: o que cada lado quer, quem precisa mais de quem, e o que a outra pessoa está tentando conseguir. Escreva de forma calculada: poucas palavras, calma, sem demonstrar ansiedade nem necessidade, sem revelar mais do que precisa, sem ceder de graça, mantendo as opções do usuário abertas e fazendo a outra pessoa se posicionar primeiro. Elegante e controlado, nunca agressivo. Estratégia aqui é leitura e posicionamento: sem mentira, sem ameaça e sem humilhar ninguém.",
            topics = linkedMapOf(
                "Resposta" to "Responda à última mensagem protegendo a posição do usuário e mantendo o controle da conversa.",
                "Ler o jogo" to "Na 'leitura', diga em uma frase a intenção real por trás da mensagem da pessoa e o que ela quer conseguir. Depois sugira respostas que levem isso em conta.",
                "Negociar" to "Negocie: não aceite a primeira condição, peça algo em troca do que ceder e faça a outra pessoa abrir a proposta dela.",
                "Impor limite" to "Coloque um limite claro, com calma e sem se justificar demais.",
                "Ganhar tempo" to "Responda sem se comprometer agora: deixe a decisão em aberto com educação e naturalidade.",
                "Virar o jogo" to "A pessoa está pressionando ou cobrando o usuário. Responda tirando o usuário da defensiva e devolvendo a pergunta ou a decisão para ela."
            ),
            tones = listOf("frio", "diplomático", "firme", "enigmático", "direto")
        ),
        "Paquera" to Mode(
            desc = "Flerte e encontros",
            style = "Contexto: conversa de paquera. Crie interesse com leveza e confiança, sem parecer carente e sem forçar.",
            topics = linkedMapOf(
                "Resposta" to "Responda à última mensagem recebida de forma que mantenha a conversa viva e dê vontade de continuar.",
                "Abertura" to "Escreva a PRIMEIRA mensagem para começar a conversa com esta pessoa, com base no contexto visível (perfil, foto, história, interesses). Curta, natural, sem clichê.",
                "Entrelinhas" to "Leia o que a pessoa pode estar sentindo ou querendo dizer e sugira respostas que respondam ao sentimento, não só ao texto literal.",
                "Persuasão" to "Conduza a conversa para o que o usuário quer (ela topar o convite, continuar a conversa, dar uma chance): mostre o que ela ganha com isso, use algo que ela mesma disse, antecipe a dúvida dela e termine com um passo fácil de aceitar. Persuasão honesta, sem insistir se ela já disse não.",
                "Flerte" to "Crie interesse leve: um elogio específico, uma provocação brincalhona ou uma pergunta que convide a pessoa a falar de si.",
                "Encontro" to "Proponha ou responda sobre marcar um encontro de forma clara, confiante e fácil de aceitar, com uma opção concreta de dia ou lugar."
            ),
            tones = listOf("leve", "confiante", "provocativo", "carinhoso", "engraçado", "direto")
        )
    )

    fun mode(name: String): Mode = modes[name] ?: modes.getValue(DEFAULT_MODE)

    private const val BASE =
        "Você ajuda o usuário a escrever mensagens em conversas de chat. Você recebe um print da conversa. " +
        "As mensagens do usuário ficam à direita; as da outra pessoa, à esquerda. " +
        "As sugestões precisam parecer escritas pelo próprio usuário: observe as mensagens dele no print e imite o jeito dele " +
        "(tamanho das mensagens, gírias, abreviações, pontuação, maiúsculas e minúsculas, emojis, risadas), " +
        "a não ser que o contexto do modo peça outro registro. Português do Brasil, como uma pessoa real digita no celular. " +
        "Nada de mentira, ameaça, pressão ou insistência. " +
        "Se a pessoa demonstrar desinteresse ou disser não, a melhor resposta respeita isso."

    fun system(modeName: String, style: String): String {
        val sb = StringBuilder(BASE).append(' ').append(mode(modeName).style)
        if (style.isNotBlank()) {
            sb.append(" Instruções do próprio usuário sobre o jeito dele de escrever. Siga à risca em todas as sugestões, elas valem mais que qualquer outra regra de estilo:\n")
            sb.append(style.trim())
        }
        return sb.toString()
    }

    fun build(modeName: String, topic: String, tone: String, ask: String, jsonMode: Boolean): String {
        val m = mode(modeName)
        val task = m.topics[topic] ?: m.topics.values.first()
        val out = if (jsonMode)
            "Responda APENAS com JSON, sem texto antes ou depois: {\"leitura\": \"...\", \"resposta\": \"...\", \"options\": [\"...\", \"...\", \"...\"]}"
        else
            "Entregue pela ferramenta: 'leitura' com uma frase curta sobre o que a pessoa quis dizer e 'options' com 3 mensagens diferentes entre si."
        val extra = if (ask.isBlank()) "" else
            "O usuário escreveu para você: «" + ask.trim() + "». " +
            "Se for uma pergunta ou um pedido de opinião, responda em 'resposta', direto e prático, em até 4 frases, " +
            "e coloque em 'options' mensagens que ele pode enviar, se fizer sentido (pode ficar vazio). " +
            "Se for uma instrução sobre o que ele quer dizer, siga essa instrução ao escrever as 'options' e deixe 'resposta' vazio."
        return listOf(
            "Este é o print da conversa.",
            "1) Leia as últimas mensagens e entenda o que a outra pessoa quer ou sente.",
            "2) Tarefa: $task",
            "3) Tom: $tone.",
            extra,
            out
        ).filter { it.isNotEmpty() }.joinToString("\n")
    }
}
