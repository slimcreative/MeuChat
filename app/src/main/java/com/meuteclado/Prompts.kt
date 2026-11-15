package com.meuteclado

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Prompts {

    /** Um modo de conversa: muda o estilo da IA, os temas e os tons disponíveis na telinha. */
    class Mode(
        val desc: String,
        val style: String,
        val topics: LinkedHashMap<String, String>,
        val tones: List<String>
    )

    const val DEFAULT_MODE = "Casual"

    /** Tema que começa uma conversa do zero, sem depender das últimas mensagens. */
    const val START = "Iniciar"

    val modes: LinkedHashMap<String, Mode> = linkedMapOf(
        "Casual" to Mode(
            desc = "Amigos, família, dia a dia",
            style = "Contexto: conversa casual com amigos, família ou conhecidos. Soe natural e descontraído, como a pessoa falaria no dia a dia.",
            topics = linkedMapOf(
                "Resposta" to "Responda à última mensagem recebida de forma natural, que mantenha a conversa viva.",
                START to "Uma mensagem leve para puxar conversa com um amigo, familiar ou conhecido.",
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
                START to "Uma mensagem educada para iniciar o contato com um cliente, fornecedor ou colega: cumprimento e o motivo do contato em uma linha.",
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
                START to "Uma mensagem de abertura que desperte interesse e dê vontade de responder, preparando o terreno para o que o usuário quer propor.",
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
                START to "Uma mensagem de abertura calma e segura, que faça a outra pessoa responder sem o usuário parecer que precisa dela.",
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
                START to "Uma mensagem para puxar papo com a pessoa: natural, sem clichê e sem parecer carente. Se o perfil, a foto ou os interesses dela aparecerem na tela, use isso.",
                "Entrelinhas" to "Leia o que a pessoa pode estar sentindo ou querendo dizer e sugira respostas que respondam ao sentimento, não só ao texto literal.",
                "Persuasão" to "Conduza a conversa para o que o usuário quer (ela topar o convite, continuar a conversa, dar uma chance): mostre o que ela ganha com isso, use algo que ela mesma disse, antecipe a dúvida dela e termine com um passo fácil de aceitar. Persuasão honesta, sem insistir se ela já disse não.",
                "Flerte" to "Crie interesse leve: um elogio específico, uma provocação brincalhona ou uma pergunta que convide a pessoa a falar de si.",
                "Encontro" to "Proponha ou responda sobre marcar um encontro de forma clara, confiante e fácil de aceitar, com uma opção concreta de dia ou lugar."
            ),
            tones = listOf("leve", "confiante", "provocativo", "carinhoso", "engraçado", "direto")
        )
    )

    fun mode(name: String): Mode = modes[name] ?: modes.getValue(DEFAULT_MODE)

    /** Ajustes de estilo que o usuário liga e desliga nas Configurações: chave -> (nome, instrução). */
    val rules: LinkedHashMap<String, Pair<String, String>> = linkedMapOf(
        "humano" to ("Mais humano, menos engessado" to
            "Escreva como gente de verdade no WhatsApp: frase solta, natural, pode ser incompleta, do jeito que sai na hora. " +
            "Proibido soar como texto pronto, atendimento ou coach: nada de frase de efeito, conselho, entusiasmo forçado, " +
            "'que legal!', 'adorei saber', 'fico feliz', 'espero que'. Mensagem curta não leva ponto final."),
        "sem_perguntas" to ("Não fazer perguntas" to
            "Não faça perguntas à pessoa e não termine a mensagem com pergunta. Comente, reaja ou afirme. " +
            "Só pergunte se o usuário pedir isso no campo de texto."),
        "curtas" to ("Respostas curtas" to
            "Cada mensagem tem no máximo uma linha curta, até umas 12 palavras."),
        "sem_emoji" to ("Sem emoji" to "Não use emoji nem emoticon."),
        "sem_elogio" to ("Sem elogio e sem bajular" to
            "Não elogie a pessoa, não bajule e não concorde só para agradar."),
        "sem_desculpa" to ("Sem se explicar nem pedir desculpa" to
            "Não se justifique, não peça desculpas e não explique demais.")
    )
    val defaultRules: Set<String> = setOf("humano")

    private const val BASE =
        "Você ajuda o usuário a escrever mensagens em conversas de chat. Você recebe um print da conversa.\n" +
        "COMO LER O PRINT: balões encostados na ESQUERDA são da OUTRA PESSOA. Balões encostados na DIREITA são do USUÁRIO " +
        "(mensagens que ele mesmo enviou, normalmente com os tiquinhos de entrega e outra cor). " +
        "O lado do balão é a única coisa que define quem enviou. " +
        "Um balão pode trazer no topo um bloco de citação, que é a mensagem antiga sendo respondida, com o rótulo 'Você' ou um nome: " +
        "isso é só referência, não é mensagem nova e não muda quem enviou o balão. " +
        "Nunca trate uma mensagem da direita como se fosse da outra pessoa, e nunca responda a uma mensagem do próprio usuário. " +
        "O ALVO da resposta é sempre a ÚLTIMA MENSAGEM DA OUTRA PESSOA (esquerda). As mensagens do usuário servem só de contexto: " +
        "se ele já escreveu algo depois dela, leve isso em conta para não repetir, mas continue respondendo ao que ELA disse por último. " +
        "Quando o pedido vier com uma transcrição lida pelo app, os lados dessa transcrição estão certos (foram medidos pela posição na tela): " +
        "confie nela acima da sua leitura visual.\n" +
        "COMO ESCREVER: as sugestões precisam parecer escritas pelo próprio usuário. Observe as mensagens dele (direita) e imite o jeito dele " +
        "(tamanho, gírias, abreviações, pontuação, maiúsculas e minúsculas, emojis, risadas), a não ser que o modo peça outro registro. " +
        "Português do Brasil, como uma pessoa real digita no celular. Acompanhe o tamanho das mensagens da conversa. " +
        "As opções têm que ser diferentes de verdade entre si (uma bem curta, uma média, uma por outro ângulo) e não podem começar todas igual. " +
        "Não use travessão nem ponto e vírgula. Responda ao que a pessoa disse de fato, sem mudar de assunto e sem inventar coisas que não estão na conversa.\n" +
        "Nada de mentira, ameaça, pressão ou insistência. Se a pessoa demonstrar desinteresse ou disser não, a melhor resposta respeita isso."

    fun system(modeName: String, style: String, activeRules: Set<String>, avoid: String): String {
        val sb = StringBuilder(BASE).append("\n").append(mode(modeName).style)
        val on = rules.filterKeys { it in activeRules }.values.map { it.second }
        if (on.isNotEmpty()) {
            sb.append("\nAJUSTES LIGADOS PELO USUÁRIO (obrigatórios em todas as sugestões):")
            on.forEach { sb.append("\n- ").append(it) }
        }
        if (avoid.isNotBlank()) {
            sb.append("\nO QUE O USUÁRIO NÃO QUER NAS SUGESTÕES (nunca faça):\n").append(avoid.trim())
        }
        if (style.isNotBlank()) {
            sb.append("\nJEITO DE ESCREVER DO USUÁRIO, nas palavras dele. Siga à risca, vale mais que qualquer outra regra de estilo:\n")
            sb.append(style.trim())
        }
        return sb.toString()
    }

    /** Bloco com a conversa lida pelo app (lados certos) e a mensagem que deve ser respondida. */
    private fun chatBlock(chat: List<ChatLine>, withTarget: Boolean): String {
        if (chat.isEmpty()) return ""
        val sb = StringBuilder("TRANSCRIÇÃO LIDA PELO APP (lados medidos pela posição do balão, confiáveis; só mensagens de texto):")
        chat.forEach { sb.append("\n").append(if (it.mine) "EU: " else "ELA: ").append(it.text) }
        if (withTarget) {
            val lastTheirs = chat.indexOfLast { !it.mine }
            if (lastTheirs >= 0) {
                // junta as mensagens seguidas dela no último turno
                var from = lastTheirs
                while (from > 0 && !chat[from - 1].mine) from--
                val target = chat.subList(from, lastTheirs + 1).joinToString(" / ") { it.text }
                sb.append("\nALVO: responda a esta, que é a última mensagem de texto DELA: «").append(target).append("».")
                if (lastTheirs < chat.size - 1) {
                    sb.append(" O usuário já escreveu depois disso; não responda à mensagem dele, e não repita o que ele já disse.")
                }
            } else {
                sb.append("\nNão há mensagem de texto dela visível; veja no print se ela mandou foto, áudio ou figurinha.")
            }
            sb.append("\nSe o print mostrar algo mais recente dela que não é texto (foto, áudio, figurinha), considere isso também.")
        }
        return sb.toString()
    }

    fun build(modeName: String, topic: String, tone: String, ask: String, chat: List<ChatLine>, jsonMode: Boolean): String {
        val m = mode(modeName)
        val task = m.topics[topic] ?: m.topics.values.first()
        val out = if (jsonMode)
            "Responda APENAS com JSON, sem texto antes ou depois: {\"ultimas\": [\"ELA: ...\", \"EU: ...\"], \"leitura\": \"...\", \"resposta\": \"...\", \"options\": [\"...\", \"...\", \"...\"]}"
        else
            "Entregue pela ferramenta. Primeiro preencha 'ultimas' com as últimas mensagens visíveis, em ordem, marcando quem enviou pelo lado do balão. Depois 'leitura' com uma frase curta sobre o que a pessoa quis dizer e 'options' com 3 mensagens diferentes entre si."
        val extra = if (ask.isBlank()) "" else
            "O usuário escreveu para você: «" + ask.trim() + "». " +
            "Se for uma pergunta ou um pedido de opinião, responda em 'resposta', direto e prático, em até 4 frases, " +
            "e coloque em 'options' mensagens que ele pode enviar, se fizer sentido (pode ficar vazio). " +
            "Se for uma instrução sobre o que ele quer dizer, siga essa instrução ao escrever as 'options' e deixe 'resposta' vazio."
        if (topic == START) {
            val now = SimpleDateFormat("EEEE, HH:mm", Locale("pt", "BR")).format(Date())
            return listOf(
                "O usuário quer INICIAR uma conversa agora. Não responda à última mensagem.",
                "Agora é $now: use o cumprimento certo para o horário (bom dia, boa tarde, boa noite) quando combinar.",
                "Tarefa: $task",
                "Se o print mostrar com quem é a conversa ou um histórico, personalize (nome, último assunto, tempo sem se falar). " +
                    "Se não mostrar conversa nenhuma, escreva boas aberturas mesmo assim e deixe 'leitura' vazio.",
                "Tom: $tone.",
                chatBlock(chat, withTarget = false),
                extra,
                out
            ).filter { it.isNotEmpty() }.joinToString("\n")
        }
        return listOf(
            "Este é o print da conversa.",
            "1) Leia as últimas mensagens, separe pelo lado quem disse o quê e entenda o que a outra pessoa (esquerda) quer ou sente.",
            "2) Tarefa: $task",
            "3) Tom: $tone.",
            chatBlock(chat, withTarget = true),
            extra,
            out,
            "Se o print não mostrar uma conversa de chat, não invente: deixe 'options' vazio, 'leitura' vazio e diga isso em 'resposta' em uma frase curta."
        ).filter { it.isNotEmpty() }.joinToString("\n")
    }
}
