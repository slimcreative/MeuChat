# Resposta IA (MVP)

Bolha flutuante que lê a tela sob demanda, pede respostas à IA por tema e tom, e insere (ou copia) a resposta no campo.

## Uso

1. Abra o app, digite sua chave da API da Anthropic em "Chave da API" e toque em "Salvar chave". A chave fica só no aparelho.
2. "Ativar bolha" → permita "Exibir sobre outros apps" e a captura de tela.
3. Abra a conversa, toque na bolha "IA", escolha o tema (Abertura, Resposta, Entrelinhas, Flerte, Encontro) e depois o tom.
4. Toque numa opção para inserir. Sem acessibilidade ativa, a resposta vai para a área de transferência.
5. "Desativar" remove a bolha.

## Gerar o APK

O workflow `build-apk` roda a cada push e publica o APK como release rascunho e no artefato `app-debug`.
