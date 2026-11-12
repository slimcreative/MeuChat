# Resposta IA (MVP)

Bolha flutuante que lê a tela sob demanda, pede respostas à IA por tema e tom, e insere (ou copia) a resposta no campo.

## Uso

1. Abra o app, digite sua chave da API da Anthropic em "Chave da API" e toque em "Salvar chave". A chave fica só no aparelho.
2. "Ativar bolha" → permita "Exibir sobre outros apps" e a captura de tela.
3. Abra a conversa e toque na bolha "IA": a telinha abre ao lado, lê a tela e já gera as respostas com o último tema e tom usados. Tocar na bolha de novo fecha a telinha.
4. Em cada resposta: "Enviar" coloca no campo e toca em enviar (precisa da acessibilidade ativa), "Copiar" copia, e tocar no texto só coloca no campo.
5. Trocar tema ou tom gera de novo. "↻" lê a tela outra vez. "👁" mostra ou esconde a miniatura da captura.
6. Arraste a bolha até o "✕" na parte de baixo para desligar. "Desativar" no app faz o mesmo.

## Gerar o APK

O workflow `build-apk` roda a cada push e publica o APK como release rascunho e no artefato `app-debug`.
