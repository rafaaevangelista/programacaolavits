# Programação LAVITS 2026 — app Android + webapp

App nativo (e também um webapp) para navegar a programação do **VII Simpósio
Internacional LAVITS** (26 a 28 de agosto de 2026, Rio de Janeiro).

📄 Página do projeto: https://rafaaevangelista.github.io/programacaolavits/
🌐 Webapp: https://rafaaevangelista.github.io/programacaolavits/app/

Toda a programação está embutida no app: **80 atividades e 144 trabalhos**,
com autores, coautores, coordenação e ministrantes. Funciona **sem internet**
e **não pede nenhuma permissão** — não há acesso a rede, localização ou
qualquer sensor. Os dados vivem em `app/src/main/assets/programacao.json`.

## O que o app faz

- **Três abas de dia** (26, 27, 28 de agosto) com as atividades agrupadas por horário.
- **Busca** por autor, instituição, título, código ou palavra-chave. Cobre também
  os títulos e a autoria dos 144 trabalhos dentro das sessões temáticas, e é
  insensível a acentos e maiúsculas — buscar `goncalves` acha `Gonçalves`.
  A busca varre os três dias de uma vez.
- **Filtros por tipo**: sessões temáticas, sessões livres, oficinas, práticas
  artísticas e tramas, cada uma com sua cor.
- **Minha agenda**: toque na estrela de qualquer atividade para guardá-la.
  A aba mostra suas escolhas em ordem cronológica e **avisa quando há conflito
  de horário** — as trilhas do simpósio são paralelas, então duas atividades
  favoritadas no mesmo horário não podem ser as duas. As escolhas ficam salvas
  no aparelho.
- **Tema claro e escuro**, seguindo a configuração do sistema.

## Como gerar o APK

### Pelo Android Studio (mais simples)

1. Abra o Android Studio → **Open** → selecione esta pasta.
2. Aguarde o Gradle sincronizar (ele baixa as dependências na primeira vez).
3. **Run ▶** com o celular conectado (com *depuração USB* ativada), ou
   **Build → Build Bundle(s) / APK(s) → Build APK(s)** para gerar o arquivo.

O APK sai em `app/build/outputs/apk/debug/app-debug.apk`.

### Pela linha de comando

Requer JDK 17+ e a variável `ANDROID_HOME` apontando para o SDK do Android:

```bash
./gradlew assembleDebug
```

Para instalar direto no celular conectado:

```bash
./gradlew installDebug
```

## Testes

A camada de dados é testada na JVM, sem emulador:

```bash
./gradlew test
```

São 11 testes cobrindo o parsing do JSON, a conversão de horários, a busca
sem acentos e a detecção de conflitos na agenda.

## Estrutura

```
app/src/main/
├── assets/programacao.json          dados completos da programação
├── java/org/lavits/programacao/
│   ├── Model.kt                     tipos + ProgramParser (JSON → objetos)
│   ├── Agenda.kt                    agrupamento e detecção de conflitos
│   ├── Assets.kt                    leitura do asset (única ponte com o Android)
│   ├── Favorites.kt                 favoritos em SharedPreferences
│   ├── Theme.kt                     paleta clara/escura e cores por tipo
│   └── MainActivity.kt              interface em Jetpack Compose
└── res/                             ícone adaptativo, temas, strings

docs/
├── index.html                       landing page do projeto (GitHub Pages)
├── downloads/proglavits.apk         APK para download direto
└── app/
    ├── index.html                   webapp (HTML/CSS/JS puro, sem build)
    └── programacao.json             cópia dos dados usados pelo webapp
```

`Model.kt` e `Agenda.kt` não importam nada do Android de propósito: é o que
permite testá-los numa JVM comum — e é essa mesma lógica (parsing, busca sem
acentos, detecção de conflito de horário) que foi portada para JavaScript em
`docs/app/index.html`.

## Webapp

`docs/app/index.html` é uma versão do app para navegador: mesmas três abas de
dia, busca, filtros por tipo e "Minha agenda" com aviso de conflito. Não usa
nenhuma dependência externa nem build — é HTML/CSS/JS puro, publicado junto
com a landing page pelo GitHub Pages. Os favoritos ficam salvos em
`localStorage`, por navegador/aparelho (não sincronizam com o app Android).

Para testar localmente:

```bash
cd docs && python3 -m http.server 8000
# abra http://localhost:8000/app/
```

Se `app/src/main/assets/programacao.json` for atualizado, copie o arquivo
também para `docs/app/programacao.json` para manter o webapp em sincronia.

## Versões

| Componente   | Versão   |
|--------------|----------|
| Gradle       | 8.9      |
| AGP          | 8.7.2    |
| Kotlin       | 2.0.21   |
| Compose BOM  | 2024.09.00 |
| compileSdk   | 35       |
| minSdk       | 26 (Android 8.0) |

Se o seu Android Studio for mais novo e sugerir atualizar o AGP, pode aceitar —
o projeto não usa nada que dependa dessas versões específicas.

## Atualizar os dados

Se a organização publicar mudanças na programação, basta substituir
`app/src/main/assets/programacao.json` e recompilar. O formato é:

```json
{
  "days":     [ { "key": "26", "label": "Quarta-feira", "date": "26 de agosto de 2026" } ],
  "sessions": [ { "id": "ST1", "type": "ST", "day": "26", "time": "9h00–10h50",
                  "title": "…", "code": "ST1",
                  "works": [ { "t": "título", "a": "Autor(a): …" } ] } ]
}
```

`type` aceita `ST` (sessão temática), `SL` (sessão livre), `OF` (oficina),
`PA` (prática artística), `TR` (trama) e `EV` (evento institucional).
Os campos `code`, `room`, `people` e `works` são opcionais. `room` é o
local/sala exibido no cartão da atividade (ex.: `"FCC/CBAE · Sala 2"`).

---

Dados extraídos de `simposio2026.lavits.org`. As salas foram incorporadas a
partir da atualização publicada pelo site oficial em agosto de 2026
(todas as Tramas ocorrem na mesma sala, o Salão Nobre do FCC/CBAE); só
falta confirmar a sala do encerramento (28/8). Confira a programação
oficial para essa sala pendente e para eventuais mudanças de última hora.
