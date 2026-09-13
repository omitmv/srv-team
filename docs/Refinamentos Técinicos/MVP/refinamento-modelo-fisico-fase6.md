# Refinamento técnico — Fase 6 — Ranking

Status: modelagem técnica consolidada para implementação do MVP.

## Objetivo

Definir o ranking como projeção derivada e recalculável a partir de `Resultado`, `Inscricao`, `Temporada`, vínculos e regras vigentes de pontuação/penalidade.

O ranking não é fonte autoritativa do domínio e não deve possuir tabela própria obrigatória no MVP.

## Princípio central

```text
Resultado = fato esportivo
TemporadaPontuacao / TemporadaPenalidade = regra vigente de interpretação
Ranking = projeção calculada
```

Portanto:

- alterar regra de pontuação não altera `Resultado`;
- corrigir/cancelar resultado muda a projeção;
- alterar elegibilidade muda a projeção;
- temporada `CANCELADA` suspende projeção;
- temporada `ENCERRADA` continua projetando ranking;
- nenhum valor de ranking deve ser considerado verdade persistida independente das fontes.

## Escopo do MVP

O MVP possui ranking geral por temporada.

Ranking por categoria permanece fora do escopo.

O ranking geral agrega todos os resultados elegíveis da temporada, independentemente de categoria ou classe.

## Entrada da projeção

A projeção parte de uma `Temporada` específica.

Pré-condição:

```text
Temporada.status IN (ATIVO, ENCERRADA)
```

Se a temporada estiver `CANCELADA`, a consulta ordinária de ranking deve indicar projeção suspensa/indisponível, sem excluir dados históricos.

## Universo corrente de atletas

A composição corrente do ranking é determinada pelos vínculos ativos com o criador da temporada.

Um atleta pertence ao universo corrente quando:

```text
existe VinculoProfissionalAtleta
  com atleta = atleta consultado
  e profissional = Temporada.cdCriador
  e status = ATIVO
```

Além disso, a conta do atleta deve continuar elegível conforme a regra funcional do sistema.

Importante:

- atleta que não possui mais vínculo ativo com o criador deixa de aparecer na composição corrente;
- isso não apaga contribuições históricas já válidas;
- se voltar a existir relacionamento através de novo vínculo, ele volta ao universo corrente, mas o novo vínculo não torna elegíveis campeonatos ocorridos no intervalo em que não havia vínculo.

## Elegibilidade histórica de cada contribuição

Cada `Resultado` deve ser avaliado individualmente.

Um resultado contribui para uma temporada somente quando todas as condições forem satisfeitas:

```text
Temporada.status != CANCELADA

AND campeonato associado à temporada

AND Resultado.status = APROVADO

AND Resultado.status funcional != CANCELADO

AND Inscricao.status = CONFIRMADA

AND Inscricao não cancelada

AND atleta/conta não cancelado

AND vinculoVigenteNaData(
      atleta,
      Temporada.cdCriador,
      Campeonato.dtInicio
    )
```

A vigência histórica é:

```text
dtInicio <= Campeonato.dtInicio
AND (dtEncerramento IS NULL OR dtEncerramento >= Campeonato.dtInicio)
```

A data de lançamento ou aprovação do resultado não define elegibilidade histórica.

Isso permite resultado tardio referente a campeonato ocorrido quando o vínculo era válido.

## Associação do campeonato

Somente campeonatos atualmente associados à temporada produzem efeito na projeção.

A associação é derivada de `TemporadaCampeonato` vigente.

Consequências:

- associar campeonato pode incluir retroativamente contribuições elegíveis já existentes;
- desvincular campeonato remove suas contribuições da projeção sem cancelar `Inscricao` ou `Resultado`;
- re-associar posteriormente volta a considerar fatos elegíveis conforme as regras atuais;
- campeonato compartilhado por várias temporadas é interpretado independentemente por cada temporada.

## Cálculo de impacto por resultado

Para cada resultado elegível:

### CLASSIFICADO

Buscar a regra atual:

```text
TemporadaPontuacao(
  cdTemporada,
  posicao = Resultado.colocacao,
  tipoClasse = Resultado.classe.tipoClasse
)
```

Se existir:

```text
impacto = pontuacao
```

Se não existir:

```text
impacto = 0
```

### DESCLASSIFICADO

Buscar:

```text
TemporadaPenalidade(
  cdTemporada,
  tipoPenalidade = DESCLASSIFICACAO
)
```

Se existir:

```text
impacto = valorPenalidade
```

Caso contrário:

```text
impacto = 0
```

### AUSENTE

Buscar:

```text
TemporadaPenalidade(
  cdTemporada,
  tipoPenalidade = AUSENCIA
)
```

Se existir:

```text
impacto = valorPenalidade
```

Caso contrário:

```text
impacto = 0
```

Cada resultado aprovado é uma contribuição independente.

Não agrupar penalidade por campeonato, categoria, classe ou situação.

## Fórmula do total

Para cada atleta do universo corrente:

```text
TOTAL_ATLETA = Σ impacto(resultadoElegivel)
```

Usar `BigDecimal` em Java e `DECIMAL(10,3)` ou precisão compatível nas consultas agregadas.

O total pode ser:

- positivo;
- zero;
- negativo.

Não truncar valores negativos em zero.

## Atletas sem contribuição

Um atleta que pertence atualmente à composição da temporada pode aparecer com zero ponto mesmo sem resultado elegível.

Isso é importante para distinguir:

- atleta pertencente à temporada sem pontuação;
- atleta fora da composição corrente.

Portanto a consulta não deve partir exclusivamente de `Resultado`.

A estratégia de leitura deve partir do universo corrente de atletas e realizar agregação opcional das contribuições.

## Empate

Empates de ranking são permitidos.

Usar classificação esportiva do tipo competição:

```text
pontos: 100, 100, 80, 70, 70, 50
posição: 1,   1,   3,  4,  4,  6
```

Sem desempate artificial por:

- nome;
- idade;
- data de cadastro;
- quantidade de campeonatos;
- melhor colocação;
- identificador.

Esses campos podem ser utilizados apenas para ordenação visual estável entre atletas empatados, sem alterar `posicao`.

### Algoritmo

Após ordenar por total decrescente:

```text
posicao = índice baseado em 1 do primeiro atleta com aquele total
```

Ou, em SQL compatível:

```text
RANK() OVER (ORDER BY total DESC)
```

Não usar `DENSE_RANK`, pois produziria `1,1,2` em vez de `1,1,3`.

## Ordenação estável

Resposta recomendada:

```text
ORDER BY
  total DESC,
  nomeAtleta ASC,
  cdAtleta ASC
```

A ordenação secundária não interfere na posição esportiva.

## Visibilidade e autorização

Regra importante:

```text
calcular universo completo autorizado
-> calcular impactos
-> somar totais
-> determinar posições
-> somente depois aplicar filtro de visibilidade do observador
```

Não calcular posição somente sobre os atletas visíveis ao usuário.

Caso contrário, um consultor poderia enxergar posição incorreta por não visualizar todos os concorrentes.

### Criador / ADMINISTRACAO

Podem visualizar o ranking completo da temporada conforme autorização contextual.

### CONSULTA

A posição deve ser calculada no ranking completo.

Depois, a resposta individualizada pode ser filtrada para atletas da temporada com os quais o consultor possua vínculo ativo, conforme regra consolidada.

Exemplo:

```text
ranking real:
1 A
2 B
3 C
4 D

consultor pode visualizar apenas B e D

resposta:
2 B
4 D
```

Nunca recalcular como:

```text
1 B
2 D
```

### Atleta

Quando houver endpoint de consulta do próprio ranking, retornar sua posição real na projeção completa da temporada, não uma posição recalculada isoladamente.

## Estratégia de implementação recomendada

Separar responsabilidades:

```text
RankingQueryService
  -> resolve autorização
  -> obtém universo corrente
  -> carrega contribuições elegíveis
  -> calcula/agrega
  -> determina posição
  -> aplica visibilidade
  -> monta DTO
```

E componentes menores quando útil:

```text
RankingEligibilityQuery
RankingContributionQuery
ResultadoImpactCalculator
RankingPositionCalculator
```

Evitar criar abstrações excessivas se uma query agregada resolver adequadamente o caso.

## Estratégia SQL versus cálculo em Java

### Recomendação inicial

Para MVP, usar abordagem híbrida:

1. banco resolve universo, joins, filtros históricos e agregação de pontos;
2. Java pode calcular posição e aplicar visibilidade final, se isso mantiver clareza e performance aceitáveis.

Alternativamente, MySQL 8 permite `RANK()` e pode calcular posição diretamente.

Escolher SQL completo quando a consulta estiver estável e testada.

Escolher agregação + posição em Java quando simplificar manutenção sem trazer volume excessivo de dados.

Não realizar N+1 para cada atleta/resultados.

## Query conceitual

A consulta de contribuição deve relacionar aproximadamente:

```text
Temporada
-> TemporadaCampeonato vigente
-> Campeonato
-> Inscricao CONFIRMADA
-> Resultado APROVADO
-> Categoria
-> Classe
-> VinculoProfissionalAtleta histórico do criador
-> TemporadaPontuacao / TemporadaPenalidade vigentes
```

A composição corrente do ranking é outra dimensão:

```text
Temporada.cdCriador
-> VinculoProfissionalAtleta ATIVO
-> Atleta
```

Isso evita o erro conceitual de usar apenas vínculo atual para validar resultado histórico.

## Índices necessários

Além dos índices já definidos nas fases anteriores, garantir eficiência nas consultas de ranking.

### `tbVinculoProfissionalAtleta`

Índices úteis:

```text
(cdProfissional, status, cdAtleta)
(cdAtleta, cdProfissional, dtInicio, dtEncerramento)
```

### `tbTemporadaCampeonato`

```text
(cdTemporada, dtFim)
(cdCampeonato, dtFim)
```

ou equivalente conforme modelagem física final de associação vigente.

### `tbInscricao`

```text
(cdCampeonato, status, cdAtleta)
(cdAtleta, status)
```

### `tbResultado`

```text
(cdInscricao, status)
(cdCategoria, cdClasse, situacao, status)
```

Avaliar índice cobrindo `status = APROVADO` conforme plano real de execução do MySQL.

### `tbTemporadaPontuacao`

Unicidade/índice:

```text
(cdTemporada, posicao, tipoClasse)
```

### `tbTemporadaPenalidade`

Unicidade/índice:

```text
(cdTemporada, tipoPenalidade)
```

Não criar índices apenas por hipótese; validar com `EXPLAIN ANALYZE` usando volume representativo.

## Cache

Ranking pode ser cacheado como otimização, nunca como fonte autoritativa.

### Chave sugerida

```text
ranking:temporada:{cdTemporada}
```

O cache deve armazenar projeção completa, antes do filtro específico do observador, quando isso for seguro para a arquitetura adotada.

Alternativamente, armazenar somente dados agregados não sensíveis e aplicar autorização sempre na aplicação.

### Eventos que invalidam cache

Invalidar quando houver alteração relevante em:

- `Resultado` aprovado/corrigido/cancelado;
- `Inscricao` que altera elegibilidade;
- `TemporadaPontuacao`;
- `TemporadaPenalidade`;
- `TemporadaCampeonato`;
- status da temporada;
- `VinculoProfissionalAtleta` do criador;
- status/cancelamento da conta do atleta quando afeta elegibilidade.

Componente recomendado:

```text
RankingInvalidationService.invalidate(cdTemporada)
```

Evitar colocar regra de cálculo dentro desse serviço.

## Descoberta das temporadas afetadas

Algumas alterações não possuem `cdTemporada` diretamente.

Exemplo:

```text
Resultado -> Inscricao -> Campeonato
```

Para invalidar cache após alteração de resultado, consultar todas as temporadas não canceladas às quais o campeonato esteja associado e invalidar cada uma.

Como o mesmo campeonato pode pertencer a várias temporadas, nunca assumir uma única temporada afetada.

## Temporada encerrada

`ENCERRADA` continua com ranking dinâmico.

Portanto:

- resultado tardio aprovado recalcula ranking;
- correção de resultado recalcula ranking;
- cancelamento válido recalcula ranking;
- inscrição tardia pode alterar elegibilidade;
- gestão de acesso não altera ranking;
- pontuação/penalidade não pode ser alterada sem reabrir a temporada.

Não persistir snapshot definitivo no momento do encerramento como verdade autoritativa.

Se futuramente houver necessidade jurídica/histórica de “ranking publicado”, isso deve ser outro conceito, por exemplo `RankingPublicacao`, fora do MVP.

## Temporada cancelada e reativação

Ao cancelar:

```text
ranking esportivo = suspenso
```

Não apagar contribuições.

Ao reativar:

```text
recalcular projeção usando dados e configurações vigentes
```

Não restaurar snapshot antigo se fatos válidos tiverem mudado durante o período de cancelamento.

## Consistência transacional

A consulta de ranking não precisa bloquear todo o domínio para produzir um snapshot absolutamente serializável.

Aceitável no MVP:

- consulta transacional consistente conforme isolamento padrão do MySQL/aplicação;
- nova consulta reflete alterações já commitadas;
- cache, se houver, invalidado após commit da mutação.

Evitar invalidar cache antes do commit, pois rollback poderia remover cache válido sem que o dado tivesse realmente mudado.

Preferir callback/evento `AFTER_COMMIT` quando necessário.

## DTO sugerido

```text
RankingTemporadaResponse
- cdTemporada
- statusTemporada
- atualizadoEm
- itens[]

RankingItemResponse
- cdAtleta
- nomeAtleta
- posicao
- pontuacaoTotal
```

Opcionalmente, em endpoint de detalhe:

```text
contribuicoes[]
- cdCampeonato
- campeonato
- categoria
- classe
- situacao
- colocacao
- impacto
```

Não expor detalhes individuais além do permitido pela autorização.

## Endpoints sugeridos

```http
GET /temporadas/{cdTemporada}/ranking
GET /temporadas/{cdTemporada}/ranking/{cdAtleta}
```

Filtros futuros devem ser adicionados apenas quando fizerem parte do produto; ranking por categoria não entra no MVP apenas por existir categoria no domínio.

## Testes obrigatórios

### Pontuação

- classificado com regra existente;
- classificado sem regra => zero;
- COMUM e OVERALL com valores diferentes;
- mudança de regra altera projeção retroativamente.

### Penalidades

- desclassificação com penalidade;
- ausência com penalidade;
- penalidade inexistente => zero;
- múltiplas penalidades/resultados somam independentemente;
- total negativo permitido.

### Elegibilidade histórica

- vínculo vigente na data do campeonato contribui mesmo se encerrado hoje;
- vínculo criado após campeonato não torna resultado elegível;
- lacuna entre vínculos permanece lacuna;
- resultado tardio usa data do campeonato, não data de aprovação.

### Composição corrente

- atleta sem vínculo atual com criador não aparece na composição corrente;
- novo vínculo atual recoloca atleta na composição;
- atleta sem resultados aparece com zero quando elegível ao universo corrente.

### Campeonato/temporada

- campeonato desvinculado deixa de contribuir;
- mesmo campeonato em duas temporadas usa regras diferentes;
- temporada cancelada não projeta ranking;
- reativação recalcula.

### Empates

```text
100, 100, 80 -> posições 1, 1, 3
```

- não usar `DENSE_RANK`;
- ordenação secundária não altera posição.

### Visibilidade

- ADMINISTRACAO vê ranking completo;
- CONSULTA recebe apenas atletas autorizados, preservando posição global;
- filtro de visibilidade ocorre após cálculo da posição.

### Concorrência/cache

- alteração commitada invalida cache;
- rollback não deve publicar projeção baseada em mutação não confirmada;
- alteração em campeonato compartilhado invalida todas as temporadas afetadas.

## Critérios de pronto da Fase 6

- ranking calculado sem tabela autoritativa própria;
- universo corrente separado da elegibilidade histórica de contribuição;
- cada resultado elegível interpretado pelas regras atuais da temporada;
- empate implementado como `RANK` (`1,1,3`);
- total negativo permitido;
- atleta elegível sem contribuição pode aparecer com zero;
- filtro de visibilidade não altera posição esportiva;
- temporada `ENCERRADA` continua dinâmica;
- temporada `CANCELADA` suspende projeção;
- compartilhamento de campeonato entre temporadas funciona de forma independente;
- cache, se adotado, é apenas otimização e possui invalidação pós-commit;
- consultas críticas cobertas por testes com MySQL/Testcontainers;
- plano de execução validado com volume representativo antes de otimizações adicionais.

## Próxima fase

Com as fontes esportivas e o ranking definidos, a próxima etapa deve consolidar a **Fase 7 — consultas, relatórios e exportações**, incluindo:

- relatório de campeonato;
- relatório de temporada;
- autorização de leitura;
- geração PDF/XLSX;
- reutilização das mesmas projeções da tela para evitar divergência entre consulta e exportação.
