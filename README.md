# Resposta IA (MVP)

Bolha flutuante que lê a tela sob demanda, pede respostas à IA por tema e tom, e insere (ou copia) a resposta no campo.

## Uso

1. Abra o app, toque em ⚙ (Configurações), digite sua chave da API da Anthropic e toque em "Salvar chave". A chave fica só no aparelho.
2. Na tela principal, toque no modo que quiser: a bolha liga já nesse modo (na primeira vez, permita "Exibir sobre outros apps" e a captura de tela).
3. Abra a conversa e toque na bolha "IA": a telinha abre ao lado, lê a tela e já gera as respostas com o último tema e tom usados. Tocar na bolha de novo fecha a telinha.
4. Em cada resposta: "Enviar" coloca no campo e toca em enviar (precisa da acessibilidade ativa), "Copiar" copia, e tocar no texto só coloca no campo.
5. Trocar tema ou tom gera de novo. "↻" lê a tela outra vez. "👁" mostra ou esconde a miniatura da captura.
6. Arraste a bolha até o "✕" na parte de baixo para desligar. "Desativar" no app faz o mesmo.

## Modos de conversa

No app, em "Modo de conversa": Casual, Profissional, Persuasão, Maquiavel e Paquera. Cada modo muda o estilo da IA e os temas e tons da telinha. O tema e o tom ficam lembrados por modo.

## Perguntar e ensinar

- Na telinha, o campo "Pergunte ou peça algo à IA" serve para pedir opinião ("o que ela quis dizer?") ou dizer o que você quer falar ("quero chamar pra sair sábado"). O "➤" envia.
- Enquanto digita, "Salvar como meu jeito de responder" guarda o texto como regra fixa de estilo.
- No app, "Meu jeito de responder" mostra e edita essas regras. A IA também imita o jeito das suas mensagens que aparecem na tela.

## Sem compartilhar tela

Com a acessibilidade ativa (Android 11+), a bolha lê a tela pelo próprio serviço de acessibilidade: não aparece o pedido de compartilhamento nem o ícone de transmissão. Em Configurações → "Escolher apps" você marca os apps onde a bolha aparece sozinha; nos outros ela some.

## Tamanho da telinha

Arraste a alça (◢) no canto de baixo da telinha para mudar largura e altura. As letras acompanham e o tamanho fica salvo.

## Gerar o APK

O APK é assinado sempre com a mesma chave (`resposta-ia.jks`), então cada versão nova instala por cima da anterior e mantém a chave da API e as configurações.


O workflow `build-apk` roda a cada push e publica o APK como release rascunho e no artefato `app-debug`.
