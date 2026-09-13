# Refinamento técnico — Fase 7 — Consultas, relatórios e exportações

Status: modelagem técnica consolidada para o MVP; implementação pendente.

## Objetivo

Definir uma camada única de leitura para:

- consulta em tela;
- relatório de campeonato;
- relatório de temporada;
- exportação PDF;
- exportação XLSX.

A regra central é:

```text
mesma autorização
+ mesmo dataset lógico
+ mesma ordenação
= mesma informação em tela, PDF e XLSX
```

Não criar regras esportivas ou filtros próprios dentro de geradores de PDF/XLSX.

## Princípio arquitetural

Separar claramente:

```text
Campeonato = fato esportivo
Temporada = interpretação daquele fato
```

Consequência:

- relatório de campeonato não possui pontuação universal da temporada;
- relatório de temporada pode converter os mesmos resultados em pontuação/penalidade de acordo com a configuração atual daquela temporada;
- o mesmo `Resultado` pode produzir impactos diferentes em temporadas diferentes;
- relatório não é fonte autoritativa de negócio.

## Pacote recomendado

```text
com.example.srvteam.relatorio
 ├── controller
 ├── application
 ├── query
 ├── dto
 ├── export
 │    ├── pdf
 │    └── xlsx
 └── mapper
```

A camada `query` é a fonte dos read models.

Não reutilizar entidades JPA diretamente como DTO de relatório.

## Read models

Criar read models específicos e imutáveis.

### CampeonatoRelatorioView

Cabeçalho conceitual:

```text
cdCampeonato
nome
organizador
pais
subdivisao
cidade/local complementar
dtInicio
dtFim
status
```

Conteúdo:

```text
categoria
classe
tipoClasse
atleta
situacao
colocacao
statusResultado
```

Somente informações autorizadas para o observador devem chegar ao read model final.

### TemporadaRelatorioView

Cabeçalho:

```text
cdTemporada
nome
criador
dtInicio
dtEncerramento
status
```

Ranking:

```text
posicaoRanking
atleta
pontuacaoTotal
```

Detalhamento opcional por atleta:

```text
campeonato
categoria
classe
situacao
colocacao
impactoTemporada
```

`impactoTemporada` é calculado dinamicamente com a configuração atual da temporada; não é lido de `Resultado` como score persistido.

## Relatório de campeonato

### Fonte dos dados

O relatório usa:

```text
Campeonato
 -> Inscricao
 -> Resultado
 -> Categoria
 -> Classe
 -> Atleta
```

Não depende de uma temporada específica para definir o fato esportivo.

### Resultado exibível

Regra padrão do relatório oficial do campeonato:

- `Resultado.status = APROVADO`;
- resultado não cancelado;
- inscrição válida para exibição;
- dados históricos permanecem disponíveis mesmo que categoria/classe estejam atualmente inativas.

Resultados `PENDENTE_APROVACAO`, `REPROVADO` ou `CANCELADO` não aparecem como resultado oficial.

Podem existir telas administrativas separadas para consulta desses estados, mas elas não devem ser confundidas com relatório oficial.

### Inscrição sem resultado

Uma `Inscricao CONFIRMADA` sem resultado aprovado pode ser exibida como:

```text
Sem resultado
```

Nunca inferir automaticamente:

```text
AUSENTE
```

`AUSENTE` só existe quando lançado explicitamente como `Resultado`.

### Pontuação

O relatório de campeonato não exibe uma “pontuação do campeonato” como verdade global, porque pontuação pertence à temporada.

Se houver necessidade de mostrar pontuação dentro de contexto de temporada, usar relatório de temporada ou endpoint contextual explícito.

## Relatório de temporada

### Fonte dos dados

Reutilizar a mesma projeção lógica do ranking definida na Fase 6.

Fluxo conceitual:

```text
Temporada
 -> Campeonatos associados
 -> Inscricoes válidas
 -> Resultados aprovados válidos
 -> elegibilidade histórica
 -> configuração atual de pontuação/penalidade
 -> impactos
 -> soma por atleta
 -> ranking
```

Não duplicar algoritmo esportivo dentro de `RelatorioTemporadaService`.

A implementação deve reutilizar componentes de ranking, por exemplo:

```text
RankingProjectionService
ResultadoImpactCalculator
```

### Temporada ENCERRADA

Relatório permanece dinâmico.

Resultado tardio, inscrição tardia, correção ou cancelamento válido podem alterar o relatório e o ranking sem reabrir a temporada.

### Temporada CANCELADA

O cancelamento suspende o efeito esportivo.

A consulta administrativa/histórica pode continuar disponível, mas não apresentar ranking corrente como se a temporada estivesse produzindo efeito esportivo.

A API deve distinguir explicitamente:

```text
consulta histórica da temporada cancelada
```

de:

```text
ranking esportivo vigente
```

## Autorização — princípio geral

Autorização deve ocorrer antes da geração de qualquer representação.

Nunca confiar em:

- esconder linha no frontend;
- gerar arquivo completo e filtrar depois;
- parâmetro enviado pelo cliente dizendo qual atleta ele pode ver.

O backend define o universo visível.

## Acesso ao relatório de temporada

### Proprietário

Acesso global administrativo.

### Criador

Acesso integral à própria temporada.

### ADMINISTRACAO

Acesso integral no contexto da temporada em que possui permissão administrativa ativa.

### CONSULTA

Acesso somente leitura.

Para dados individualizados de atletas, respeitar a regra consolidada:

```text
atleta pertence à composição da temporada
E consultor possui vínculo próprio ATIVO com o atleta
```

### Regra crítica de ranking

O ranking completo deve ser calculado antes do filtro de visibilidade do consultor.

Exemplo:

```text
ranking real:
1 - Atleta A
2 - Atleta B
3 - Atleta C
4 - Atleta D

consultor pode ver apenas B e D
```

Resposta:

```text
2 - Atleta B
4 - Atleta D
```

Nunca recalcular para `1` e `2` após filtrar.

## Acesso ao relatório de campeonato

Não conceder acesso profissional global a todos os resultados de todos os campeonatos.

Para profissional comum, o acesso deve existir por contexto de uma temporada autorizada que contenha o campeonato.

Autorização conceitual:

```text
exists Temporada T:
  campeonato associado a T
  AND T não está CANCELADA para operação ordinária
  AND (
      usuario = T.criador
      OR usuario possui ADMINISTRACAO ativa em T
      OR usuario possui CONSULTA ativa em T com filtros de visibilidade aplicáveis
  )
```

O proprietário possui acesso global.

### Campeonato associado a várias temporadas

Um profissional precisa de autorização em pelo menos uma temporada que associe aquele campeonato.

A existência de outra temporada à qual ele não possui acesso não reduz sua permissão sobre o universo legitimamente acessível.

Para `CONSULTA`, o filtro final continua limitado aos atletas visíveis no contexto autorizado.

## Campeonato cancelado

Cancelamento não apaga o histórico.

Relatórios administrativos podem continuar consultando fatos registrados.

A representação deve sinalizar status `CANCELADO`; não mascarar o fato como campeonato ativo.

## Query services

Recomendação:

```text
CampeonatoReportQueryService
TemporadaReportQueryService
```

Responsabilidades:

1. validar autorização;
2. carregar read model otimizado;
3. aplicar filtro de visibilidade;
4. devolver estrutura neutra de apresentação.

Geradores de PDF/XLSX recebem o read model pronto.

Não devem consultar repository por conta própria.

## Exportação

Criar fronteira comum:

```text
ReportExporter<T>
```

Implementações:

```text
PdfReportExporter
XlsxReportExporter
```

O objetivo da interface é padronizar geração de representação, não introduzir abstração sobre toda consulta.

### Regra de segurança

A autorização deve ser revalidada na requisição de exportação.

Não aceitar token/ID de um relatório previamente gerado como substituto de autorização atual.

Mudança de permissão entre tela e exportação deve valer imediatamente.

## API sugerida

### Campeonato

```text
GET /campeonatos/{id}/relatorio
GET /campeonatos/{id}/relatorio.pdf
GET /campeonatos/{id}/relatorio.xlsx
```

Para evitar três implementações divergentes:

```text
Controller
 -> CampeonatoReportQueryService
 -> CampeonatoRelatorioView
 -> JSON ou exporter
```

### Temporada

```text
GET /temporadas/{id}/relatorio
GET /temporadas/{id}/relatorio.pdf
GET /temporadas/{id}/relatorio.xlsx
```

Mesma regra:

```text
Controller
 -> TemporadaReportQueryService
 -> TemporadaRelatorioView
 -> JSON ou exporter
```

## Filtros

Filtros funcionais podem existir, como:

```text
categoria
classe
atleta
campeonato
```

Mas filtro solicitado pelo usuário é aplicado somente depois de determinar o universo ao qual ele tem acesso.

Nunca usar filtro de request como mecanismo de autorização.

## Ordenação do relatório de campeonato

Para resultados classificados:

```text
Categoria
Classe
Colocacao ASC
```

Para situações sem colocação (`DESCLASSIFICADO`, `AUSENTE`), definir ordenação determinística adicional, por exemplo:

```text
situacao
nomeAtleta
cdResultado
```

Não depender da ordem incidental do banco.

## Ordenação do relatório de temporada

Ranking:

```text
pontuacaoTotal DESC
posição por RANK()
```

Em empate, aplicar regra da Fase 6:

```text
1, 1, 3
```

Para estabilidade visual entre atletas empatados, pode existir ordenação secundária por nome/ID **somente para apresentação**, sem quebrar o empate esportivo.

Exemplo:

```text
position = 1, athlete A
position = 1, athlete B
position = 3, athlete C
```

## Consistência transacional

Relatórios são leitura.

Usar transação read-only quando fizer sentido:

```java
@Transactional(readOnly = true)
```

Evitar múltiplas consultas independentes que possam produzir cabeçalho de um instante e ranking de outro quando consistência lógica da mesma resposta for relevante.

Para exportações longas, preferir obter um snapshot lógico/read model e gerar o arquivo a partir dele, em vez de manter uma transação de banco aberta durante renderização do PDF/XLSX.

Fluxo recomendado:

```text
transação curta de leitura
 -> monta read model
 -> encerra transação
 -> gera arquivo
```

## Performance

### Evitar N+1

Não navegar lazy entity por entity durante geração.

Preferir:

- DTO projections;
- JPQL/query específica;
- native SQL quando justificado pela projeção de ranking;
- leitura em lote.

### XLSX

Para volume maior, usar escrita streaming quando necessário.

Não carregar estruturas de apresentação duplicadas sem necessidade.

### PDF

PDF deve consumir o mesmo read model do XLSX/JSON.

## Auditoria de exportação

Não é necessário transformar cada download em fato esportivo.

Se houver necessidade de auditoria operacional, registrar separadamente:

```text
usuario
relatorio
tipoExportacao
dataHora
parametros não sensíveis
```

Isso não interfere em ranking nem domínio esportivo.

## Dados sensíveis

Relatórios não devem expor automaticamente:

- senha/hash;
- segredo JWT;
- dados internos de autenticação;
- justificativas restritas a owner quando a regra funcional não permitir;
- dados pessoais que não façam parte do objetivo do relatório.

Exportação deve seguir exatamente a mesma política da tela.

## Cache

Não introduzir cache de relatório como fonte de verdade.

Se houver cache:

- chave deve incluir identidade do relatório, parâmetros relevantes e contexto de autorização quando necessário;
- invalidação segue os mesmos eventos que afetam ranking/resultados/permissões;
- alteração de permissão não pode deixar arquivo/cache acessível indevidamente;
- conteúdo cacheado é descartável e reconstruível.

No MVP, começar sem cache é preferível até existir evidência de gargalo.

## Testes obrigatórios

### Autorização

Testar:

- owner acessa globalmente;
- criador acessa temporada;
- ADMINISTRACAO acessa contexto autorizado;
- CONSULTA recebe somente atletas permitidos;
- profissional sem temporada autorizada não acessa relatório do campeonato;
- remoção/rebaixamento de acesso afeta imediatamente novas consultas/exportações.

### Consistência de representação

Para os mesmos parâmetros e usuário:

```text
JSON == PDF == XLSX
```

semanticamente.

Os formatos podem diferir visualmente, mas linhas, totais, posições e filtros precisam ser equivalentes.

### Campeonato

Testar:

- pendente não aparece como resultado oficial;
- reprovado não aparece;
- cancelado não aparece;
- confirmado sem resultado pode aparecer como `Sem resultado`;
- ausência nunca é inferida;
- categoria/classe inativa historicamente continua exibível.

### Temporada

Testar:

- pontuação atual da temporada é aplicada;
- penalidades atuais são aplicadas;
- mudança de configuração em temporada ATIVO altera relatório retroativamente;
- resultado tardio em ENCERRADA atualiza relatório;
- cancelamento da temporada suspende ranking corrente;
- empate mantém `1,1,3`;
- filtro de CONSULTA não renumera posições.

## Critério de pronto da Fase 7

A fase está pronta para implementação quando:

- relatório de campeonato usa fatos esportivos sem inventar pontuação universal;
- relatório de temporada reutiliza projeção de ranking;
- tela/PDF/XLSX usam o mesmo read model lógico;
- autorização é aplicada no backend antes da geração;
- CONSULTA não consegue ampliar universo por filtros manipulados;
- posição do ranking é calculada antes do filtro de visibilidade;
- exportação revalida autorização;
- geradores não acessam repositories diretamente;
- não há entidade/tabela autoritativa `Relatorio`;
- queries são projetadas para evitar N+1;
- testes de equivalência entre formatos estão previstos.

## Encadeamento após a Fase 7

Com as Fases 1–7 refinadas, a próxima etapa técnica deve ser uma revisão transversal de implementação para produzir:

1. mapa final de tabelas e dependências;
2. ordem consolidada das migrations Flyway;
3. mapa de aggregates e services;
4. matriz final de autorização;
5. plano incremental de implementação/rollout;
6. checklist de testes unitários, integração e concorrência;
7. identificação de divergências restantes entre documentação consolidada e código legado.

Essa revisão deve ocorrer antes de alterar as entidades produtivas do MVP.