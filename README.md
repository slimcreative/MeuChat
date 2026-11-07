# MeuChat

App de mensageria **próprio**, do zero, em Kotlin + Jetpack Compose — feito para estudo.
Reproduz *funcionalidades* no estilo dos mods de WhatsApp (temas, configurações de
privacidade, multi-conta) **sem nenhum código de terceiros**: tudo aqui é original.

> Estado atual: **esqueleto funcional offline**. A UI, os temas e as configurações já
> funcionam e persistem em disco. A camada de rede (mensageria real) é o próximo passo —
> ver "Roteiro".

---

## Como compilar o APK

### Opção A — Android Studio (recomendada)
1. Abra o Android Studio (Koala/2024.1 ou mais novo).
2. **File → Open** e aponte para a pasta `MeuChat`.
3. Espere o *Gradle sync* (ele baixa o SDK/bibliotecas na primeira vez).
4. **Run ▶** num emulador ou celular, ou **Build → Build APK(s)** para gerar o `.apk`.
   - O APK sai em `app/build/outputs/apk/debug/app-debug.apk`.

### Opção B — Linha de comando
Precisa ter o **Android SDK** instalado e a variável `ANDROID_HOME` configurada.
```bash
cd MeuChat
./gradlew assembleDebug        # gera app/build/outputs/apk/debug/app-debug.apk
```
Para instalar direto num aparelho conectado via USB (depuração ativada):
```bash
./gradlew installDebug
```

---

## Estrutura
```
app/src/main/java/com/estudo/meuchat/
├── MainActivity.kt            # navegação + barra inferior (Conversas / Config)
├── data/
│   ├── Models.kt              # Conta, Conversa, Mensagem + dados de exemplo
│   └── SettingsStore.kt       # DataStore: temas e privacidade persistidos
├── ui/theme/Theme.kt          # catálogo de temas (Verde/Azul/Roxo/Âmbar) + modo escuro
└── ui/screens/
    ├── ChatScreens.kt         # lista de conversas + tela de conversa
    └── SettingsScreens.kt     # Privacidade, Aparência, Contas
```

## Funcionalidades já no app (offline)
- **Temas** trocáveis ao vivo + **modo escuro** (sistema/claro/escuro) + **cor dinâmica** (Material You, Android 12+).
- **Privacidade**: ocultar visto por último / online / digitando, confirmação de leitura, anti-revoke, bloqueio biométrico — tudo persistido.
- **Multi-conta**: tela de contas com adicionar.
- **Conversas**: lista e tela de chat com envio local de mensagens.

> As opções de privacidade hoje salvam a preferência. Quando a camada de rede entrar,
> cada toggle passa a controlar o que o app **emite** ao servidor (ex.: não enviar o
> recibo de leitura quando "confirmação de leitura" estiver desligada).

---

## Roteiro (próximas fases)
1. **Backend/protocolo:** integrar o **Matrix SDK** (rede aberta, E2E pronta) apontando
   para um servidor **Synapse** local rodando em Docker.
2. **Login real** + sincronização de salas e mensagens.
3. Ligar os toggles de privacidade ao que o cliente emite (presence, receipts, typing).
4. Anti-revoke de verdade: ao receber uma redação (`m.room.redaction`), guardar o original localmente.
5. Bloqueio biométrico com `androidx.biometric` na frente das conversas.
6. Agendamento de mensagens com `WorkManager`.

## Licença / escopo
Projeto pessoal de estudo. Não reutiliza código do WhatsApp nem de mods — só reimplementa
ideias de funcionalidade, que não são protegidas por direitos autorais.
