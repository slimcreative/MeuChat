# Resposta IA (MVP)

Bolha flutuante que lê a tela sob demanda, pede respostas à IA no tom escolhido e insere (ou copia) a resposta no campo.

## Gerar o APK (sem Android Studio)

1. Suba esta pasta num repositório no GitHub.
2. Em Settings → Secrets and variables → Actions, crie o secret `PROXY_URL` com o endereço do proxy (ex.: `https://seu-proxy.com/reply-screen`).
3. Aba Actions → build-apk → baixe o artefato `app-debug` (contém `app-debug.apk`).
4. Instale no Android (permitir fontes desconhecidas).

## Proxy

```
cd proxy
ANTHROPIC_API_KEY=sk-ant-... PORT=3000 node server.js
```
Hospede em qualquer serviço que rode Node 18+ (Render, Railway, Fly.io, VPS). A chave fica só no servidor.

## Uso

1. Abra o app → "Ativar bolha" → permita "Exibir sobre outros apps" e a captura de tela.
2. A bolha "IA" aparece sobre os apps. Arraste para onde quiser.
3. Abra a conversa, toque na bolha, escolha o tom e toque numa opção.
4. Para inserir direto no campo, ative a acessibilidade do app. Sem ela, a resposta vai para a área de transferência.
5. "Desativar" para a bolha quando não quiser usar.
