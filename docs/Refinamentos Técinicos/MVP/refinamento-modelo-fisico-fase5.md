# Refinamento técnico — Modelo físico Fase 5: Resultado

Status: modelagem técnica consolidada para implementação do MVP.

## Objetivo

Definir o modelo físico, transacional e de autorização de `Resultado`, incluindo:

- situações esportivas `CLASSIFICADO`, `DESCLASSIFICADO` e `AUSENTE`;
- workflow de aprovação do atleta;
- edição de resultado pendente;
- correção/cancelamento administrativo de resultado aprovado;
- histórico imutável;
- versionamento funcional;
- reserva concorrente de colocação;
- unicidade por `Inscricao + Categoria + Classe`;
- integração com `Temporada` e ranking;
- migração controlada da semântica hoje existente em `PontuacaoHist`.

`Resultado` representa fato esportivo do campeonato. Não pertence a uma temporada específica.

## Fonte autoritativa

```text
Inscricao
 -> identifica atleta + campeonato

Resultado
 -> identifica categoria + classe + situação + colocação

Temporada
 -> interpreta Resultado conforme suas regras atuais de pontuação/penalidade
```

Não persistir pontuação calculada da temporada dentro de `Resultado`.

O mesmo `Resultado` pode gerar impactos diferentes em temporadas diferentes.

## Tabela principal

Nome recomendado:

```text
tbResultado
```

Estrutura conceitual:

```text
cdResultado              BIGINT / INT PK
cdInscricao              FK NOT NULL
cdCategoria              FK NOT NULL
cdClasse                 FK NOT NULL
situacao                  VARCHAR NOT NULL
colocacao                 INT NULL
status                    VARCHAR NOT NULL
versaoNegocio             INT NOT NULL
version                   BIGINT NOT NULL
cdCriadorResultado        FK Usuario NOT NULL
dtCriacao                 DATETIME NOT NULL
cdUltimaAlteracao         FK Usuario NULL
dtUltimaAlteracao         DATETIME NULL
cdDecisor                 FK Usuario NULL
dtDecisao                 DATETIME NULL
motivoReprovacao          VARCHAR NULL
cdResponsavelCancelamento FK Usuario NULL
dtCancelamento            DATETIME NULL
motivoCancelamento        VARCHAR NULL
cdResponsavelCorrecao     FK Usuario NULL
dtCorrecao                DATETIME NULL
motivoCorrecao            VARCHAR NULL
```

A nomenclatura final das colunas pode seguir o padrão físico do projeto, mas a semântica deve permanecer explícita.

## Identidade e relacionamentos

`Resultado` referencia obrigatoriamente:

```text
Inscricao N:1
Categoria N:1
Classe N:1
```

Não duplicar como fonte autoritativa:

- atleta;
- campeonato.

Essas informações derivam de `Inscricao`.

Isso evita inconsistências do tipo:

```text
Resultado.cdCampeonato != Resultado.inscricao.cdCampeonato
```

## Situação esportiva

Enum:

```text
SituacaoResultado
- CLASSIFICADO
- DESCLASSIFICADO
- AUSENTE
```

Persistir como `STRING`.

### CLASSIFICADO

Regras:

- `colocacao` obrigatória;
- `colocacao > 0`;
- qualquer inteiro positivo é válido;
- não existe colocação máxima de negócio;
- ausência de regra de pontuação da temporada implica impacto zero.

### DESCLASSIFICADO

Regras:

- `colocacao = NULL`;
- categoria e classe permanecem obrigatórias;
- impacto depende de `TemporadaPenalidade.DESCLASSIFICACAO` da temporada que estiver interpretando o resultado.

### AUSENTE

Regras:

- `colocacao = NULL`;
- categoria e classe permanecem obrigatórias;
- impacto depende de `TemporadaPenalidade.AUSENCIA`.

`AUSENTE` nunca deve ser inferido automaticamente pela ausência de resultado.

## Status do workflow

Enum recomendado:

```text
StatusResultado
- PENDENTE_APROVACAO
- APROVADO
- REPROVADO
- CANCELADO
```

Persistir como `STRING`.

Fluxo normal:

```text
PENDENTE_APROVACAO -> APROVADO
PENDENTE_APROVACAO -> REPROVADO
```

Resultado aprovado pode sofrer ação administrativa extraordinária:

```text
APROVADO -> correção administrativa
APROVADO -> CANCELADO
```

Correção não precisa criar um novo `Resultado`; altera o estado atual do agregado e registra snapshot completo no histórico.

Resultado `REPROVADO` ou `CANCELADO` não volta diretamente a `APROVADO` no MVP.

Novo lançamento deve seguir novo ciclo quando aplicável.

## Granularidade da disputa

A chave lógica esportiva é:

```text
(Inscricao, Categoria, Classe)
```

Isso significa que a mesma inscrição pode ter múltiplos resultados no campeonato, desde que sejam em combinações distintas de categoria/classe.

Exemplo válido:

```text
Inscricao #10
Bodybuilding / Master 2
Bodybuilding / Senior Classe 1
Bodybuilding / Overall
```

Não modelar `AUSENTE` ou `DESCLASSIFICADO` de forma global por inscrição.

## Classe pertencente à categoria

Antes de persistir qualquer resultado:

```text
Resultado.cdClasse -> Classe.cdCategoria
```

deve corresponder ao `cdCategoria` informado.

Essa regra deve ser validada na aplicação.

Recomendação adicional de banco: criar FK composta ou mecanismo equivalente apenas se isso não introduzir complexidade desnecessária no modelo físico. O service continua responsável por mensagem de erro de domínio clara.

## Resultado ativo por inscrição/categoria/classe

Para uma combinação:

```text
(cdInscricao, cdCategoria, cdClasse)
```

pode existir no máximo um resultado operacional ativo.

Para esta regra, considerar como ativos:

```text
PENDENTE_APROVACAO
APROVADO
```

`REPROVADO` e `CANCELADO` permanecem históricos e não devem impedir novo lançamento futuro.

## Estratégia MySQL para unicidade condicional

Como MySQL não oferece índice parcial no mesmo formato de PostgreSQL, usar coluna gerada virtual/armazenada para materializar a chave somente quando o status participar da restrição.

Exemplo conceitual:

```sql
active_result_key = CASE
  WHEN status IN ('PENDENTE_APROVACAO', 'APROVADO')
  THEN CONCAT(cdInscricao, ':', cdCategoria, ':', cdClasse)
  ELSE NULL
END
```

Criar unique index sobre `active_result_key`.

Como múltiplos `NULL` são permitidos, resultados históricos deixam de competir pela unicidade.

Preferir uma implementação com colunas geradas tipadas separadamente quando isso resultar em índice mais eficiente e menos sujeito a diferenças de collation/conversão.

## Reserva de colocação

Para `CLASSIFICADO`, a colocação é reservada dentro da disputa:

```text
(Campeonato, Categoria, Classe, colocacao)
```

A reserva existe tanto para:

```text
PENDENTE_APROVACAO
APROVADO
```

Consequência:

- um resultado pendente já ocupa a colocação;
- outro profissional não pode lançar a mesma colocação enquanto a pendência existir;
- aprovação não cria a reserva: apenas confirma uma reserva já existente;
- `REPROVADO` ou `CANCELADO` libera a colocação;
- alteração de pendente deve reservar a nova colocação e liberar a anterior na mesma transação.

## Por que `@Version` não resolve reserva de colocação

`@Version` protege concorrência sobre a mesma linha.

A corrida crítica envolve linhas diferentes:

```text
Tx A cria Resultado A -> 1º lugar
Tx B cria Resultado B -> 1º lugar
```

Nenhuma linha existe antes e as duas podem passar pela validação `exists` simultaneamente.

Portanto, a proteção precisa existir no banco.

## Estratégia física para colocação

Como `Campeonato` é derivado de `Inscricao`, a constraint não consegue usar diretamente um join.

Há duas opções técnicas.

### Opção A — duplicar `cdCampeonato` no Resultado apenas como chave técnica

Não recomendada como primeira escolha, porque cria redundância de domínio e exige garantia forte de consistência com `Inscricao`.

### Opção B — tabela explícita de reserva

Recomendação para o MVP.

Criar:

```text
tbResultadoColocacaoReserva
```

Campos:

```text
cdResultado      PK/FK
cdCampeonato     FK NOT NULL
cdCategoria      FK NOT NULL
cdClasse         FK NOT NULL
colocacao        INT NOT NULL
```

Constraint:

```text
UNIQUE (cdCampeonato, cdCategoria, cdClasse, colocacao)
```

A existência do registro representa a reserva ativa.

Fluxo transacional:

```text
Criar CLASSIFICADO PENDENTE
 -> inserir Resultado
 -> inserir Reserva

Editar colocacao PENDENTE
 -> remover/alterar reserva anterior
 -> tentar nova reserva
 -> atualizar Resultado

Aprovar
 -> manter reserva

Reprovar
 -> remover reserva

Cancelar aprovado
 -> remover reserva

Corrigir aprovado mudando colocacao
 -> trocar reserva atomicamente
```

A tabela de reserva é detalhe técnico de persistência, não agregado de domínio exposto pela API.

## Ordem segura na troca de colocação

Em alteração da colocação, evitar janela em que o resultado fica sem proteção indevidamente.

Recomendação:

1. iniciar transação;
2. lock pessimista no `Resultado` atual;
3. validar situação/status/autorização;
4. tentar adquirir nova chave de reserva;
5. somente após sucesso liberar a reserva antiga, quando necessário;
6. atualizar `Resultado`;
7. gravar histórico;
8. commit.

Em MySQL, pode ser necessário definir estratégia de ordem de locks para minimizar deadlocks.

Se ocorrer unique violation/deadlock concorrente, traduzir para erro de domínio apropriado, preferencialmente HTTP `409 Conflict`.

## Workflow de lançamento

Todo lançamento ordinário nasce:

```text
status = PENDENTE_APROVACAO
```

Independentemente de:

```text
CLASSIFICADO
DESCLASSIFICADO
AUSENTE
```

Não existe lançamento ordinário diretamente `APROVADO` por profissional.

O atleta decide aprovação/reprovação.

## Autorização para lançamento

Profissional pode lançar resultado quando existir ao menos uma temporada contextual válida em que:

```text
Temporada.status IN (ATIVO, ENCERRADA)
AND Campeonato está associado à temporada
AND atleta pertence à composição da temporada
AND (
    profissional = Temporada.cdCriador
    OR profissional possui ADMINISTRACAO ativa na temporada
)
```

Não é necessário o profissional operador possuir vínculo profissional-atleta próprio com o atleta.

`CONSULTA` não lança resultado.

Vínculo direto isolado também não autoriza lançamento.

`CANCELADA` não serve como fundamento ordinário de autorização.

## Pré-condições do lançamento

Antes de criar resultado:

- `Inscricao.status = CONFIRMADA`;
- inscrição não cancelada;
- campeonato não cancelado;
- categoria ativa;
- classe ativa;
- categoria da classe consistente;
- situação/colocação consistente;
- não existir resultado ativo na mesma `(Inscricao,Categoria,Classe)`;
- se classificado, colocação estar disponível;
- existir contexto de autorização válido.

Itens de catálogo inativos continuam válidos para resultados históricos existentes, mas não para novos lançamentos ordinários.

## Edição pendente

Enquanto `status = PENDENTE_APROVACAO`, o profissional autorizado pode alterar os dados esportivos antes da decisão do atleta.

Campos potencialmente editáveis:

- categoria;
- classe;
- situação;
- colocação.

Toda edição:

- revalida contexto de autorização;
- revalida inscrição;
- revalida classe/categoria;
- revalida unicidade `(Inscricao,Categoria,Classe)`;
- revalida reserva de colocação;
- incrementa `versaoNegocio`;
- incrementa `@Version` automaticamente pelo JPA;
- grava `ResultadoHistorico`.

## Decisão do atleta

O atleta vinculado à `Inscricao` pode:

```text
aprovar -> APROVADO
reprovar -> REPROVADO
```

### Aprovação

Ao aprovar:

- revalidar invariantes essenciais;
- manter reserva da colocação quando `CLASSIFICADO`;
- preencher decisor/data;
- gravar histórico;
- invalidar projeções/cache das temporadas potencialmente afetadas.

A revalidação não substitui a constraint física de reserva.

### Reprovação

Ao reprovar:

- justificativa/motivo conforme regra de produto adotada;
- liberar reserva de colocação quando existir;
- gravar decisão e histórico;
- permitir novo lançamento futuro para a mesma granularidade.

## Correção administrativa de resultado aprovado

Correção de `APROVADO` é intervenção extraordinária do proprietário.

Não confundir com edição pendente.

Requisitos:

- proprietário;
- justificativa obrigatória;
- validação integral da nova situação;
- respeito à unicidade de resultado ativo;
- respeito à reserva de colocação;
- atualização de `versaoNegocio`;
- snapshot antes/depois no histórico;
- recalcular/invalidate rankings afetados.

A correção pode alterar:

- categoria;
- classe;
- situação;
- colocação.

Se alterar `CLASSIFICADO` para `AUSENTE`/`DESCLASSIFICADO`, liberar reserva.

Se alterar `AUSENTE`/`DESCLASSIFICADO` para `CLASSIFICADO`, adquirir reserva.

Se alterar a colocação classificada, trocar reserva atomicamente.

Não enviar notificação automática no MVP, conforme decisão de negócio consolidada.

## Cancelamento administrativo

Resultado `APROVADO` pode ser cancelado pelo proprietário com justificativa obrigatória.

Ao cancelar:

- `status = CANCELADO`;
- preservar todos os dados esportivos anteriores;
- liberar reserva ativa de colocação;
- registrar responsável/data/justificativa;
- gravar histórico;
- retirar impacto de todas as temporadas que o interpretavam;
- invalidar/recalcular projeções afetadas.

Cancelamento é lógico. Não excluir fisicamente `Resultado`.

## ResultadoHistorico

Criar entidade/tabela append-only:

```text
tbResultadoHistorico
```

Campos recomendados:

```text
cdResultadoHistorico
cdResultado
versaoNegocio
acao
statusAnterior
statusNovo
situacaoAnterior
situacaoNova
cdCategoriaAnterior
cdCategoriaNova
cdClasseAnterior
cdClasseNova
colocacaoAnterior
colocacaoNova
cdResponsavel
dtEvento
justificativa
snapshotJson opcional
```

Enum `AcaoResultadoHistorico` pode incluir:

```text
CRIACAO
EDICAO_PENDENTE
APROVACAO
REPROVACAO
CORRECAO_ADMINISTRATIVA
CANCELAMENTO_ADMINISTRATIVO
MIGRACAO
```

Histórico não deve ser atualizado nem excluído pela aplicação ordinária.

## `versaoNegocio` versus `@Version`

Manter ambos.

### `@Version`

Controle técnico de optimistic locking da linha.

### `versaoNegocio`

Identificador auditável da versão funcional do conteúdo do resultado.

Exemplo:

```text
v1 criação pendente
v2 edição pendente
v3 aprovação
v4 correção administrativa
v5 cancelamento
```

O histórico deve registrar a `versaoNegocio` resultante de cada mutação relevante.

## Transações

Operações que obrigatoriamente precisam ser transacionais:

- criação de resultado;
- edição pendente;
- aprovação;
- reprovação;
- correção administrativa;
- cancelamento administrativo.

Na mesma transação devem ocorrer, conforme o caso:

```text
Resultado
ResultadoColocacaoReserva
ResultadoHistorico
```

Publicação/invalidação externa de cache não deve provocar inconsistência caso ocorra falha pós-commit. Se futuramente houver mensageria, considerar outbox pattern; não é necessário introduzir isso no MVP sem demanda concreta.

## Locking recomendado

Usar `@Version` em `Resultado`.

Para alterações de resultado já existente, considerar lock pessimista quando houver troca de reserva de colocação para tornar a sequência mais previsível.

Para criação de nova linha, a constraint da reserva é a proteção final.

Não implementar apenas:

```java
if (!repository.exists(...)) {
    save(...);
}
```

pois isso é vulnerável a race condition.

## Erros HTTP recomendados

```text
400 Bad Request
 -> combinação inválida de situação/colocação
 -> classe não pertence à categoria

401 Unauthorized
 -> ausência/autenticação inválida

403 Forbidden
 -> usuário autenticado sem autorização contextual

404 Not Found
 -> inscrição/resultado/categoria/classe inexistente ou não visível

409 Conflict
 -> colocação já reservada
 -> resultado ativo já existe para Inscricao/Categoria/Classe
 -> conflito de versão concorrente
 -> estado atual incompatível com transição solicitada

422 Unprocessable Entity
 -> opcional; não necessário se o projeto padronizar regra de negócio em 400/409
```

Preferência do refinamento: usar `409` para violações de invariantes de concorrência/estado.

## Integração com ranking

`Resultado` não persiste pontos.

Quando `APROVADO`, cada temporada elegível interpreta o registro dinamicamente:

```text
CLASSIFICADO
 -> Resultado.colocacao
 -> Classe.tipoClasse
 -> TemporadaPontuacao atual

DESCLASSIFICADO
 -> TemporadaPenalidade.DESCLASSIFICACAO atual

AUSENTE
 -> TemporadaPenalidade.AUSENCIA atual
```

Resultado `PENDENTE_APROVACAO`, `REPROVADO` ou `CANCELADO` produz impacto zero.

Alterações que podem afetar ranking:

- aprovação;
- correção de aprovado;
- cancelamento;
- mudança posterior das regras da temporada.

## Elegibilidade histórica para temporada

Para uma temporada interpretar um resultado:

```text
Temporada.status != CANCELADA
AND Campeonato associado à Temporada
AND Resultado.status = APROVADO
AND Inscricao.status = CONFIRMADA
AND vínculo entre atleta e Temporada.cdCriador vigente em Campeonato.dtInicio
AND conta do atleta válida conforme regra consolidada
```

A data de lançamento/aprovação do resultado não precisa estar dentro do período nominal da temporada.

Resultado tardio pode ser aprovado após temporada `ENCERRADA` e passar a contribuir imediatamente.

## Relação com campeonato cancelado

Cancelamento do campeonato não deve apagar Resultado.

O efeito final no ranking deve seguir a regra consolidada de validade do campeonato/temporada e as decisões de status do campeonato.

A implementação deve centralizar essa elegibilidade em um componente próprio, evitando espalhar verificações em repositories/controllers.

## Services recomendados

Estrutura sugerida:

```text
resultado/
 ├── controller/
 ├── dto/
 ├── model/
 ├── repository/
 ├── service/
 │    ├── ResultadoCommandService
 │    ├── ResultadoDecisionService
 │    ├── ResultadoAdministrativeService
 │    ├── ResultadoAuthorizationService
 │    ├── ResultadoValidationService
 │    └── ResultadoPlacementReservationService
 └── mapper/
```

Não é obrigatório criar uma classe para cada nome acima desde o primeiro commit. A separação deve surgir quando houver responsabilidade real distinta.

Mínimo saudável:

```text
ResultadoService
ResultadoAuthorizationService
ResultadoPlacementReservationService
```

## DTOs orientados a caso de uso

Evitar um único DTO CRUD genérico.

Exemplos:

```text
CreateResultadoRequest
UpdatePendingResultadoRequest
ApproveResultadoRequest
RejectResultadoRequest
CorrectResultadoRequest
CancelResultadoRequest
ResultadoResponse
ResultadoHistoryResponse
```

`CorrectResultadoRequest` e `CancelResultadoRequest` devem exigir justificativa.

## Repositories

Consultas importantes:

```text
findByIdForUpdate(...)
existsActiveByInscricaoCategoriaClasse(...)
findActiveByInscricaoCategoriaClasse(...)
findByInscricao(...)
findApprovedByCampeonato(...)
findApprovedEligibleForSeasonProjection(...)
```

Para reserva:

```text
ResultadoColocacaoReservaRepository
```

A tentativa de `INSERT` com unique constraint deve ser tratada como mecanismo normal de arbitragem concorrente.

## Índices recomendados

Em `tbResultado`:

```text
INDEX (cdInscricao)
INDEX (cdCategoria, cdClasse)
INDEX (status)
INDEX (cdInscricao, cdCategoria, cdClasse, status)
INDEX (status, situacao)
```

Em `tbResultadoColocacaoReserva`:

```text
PRIMARY/UNIQUE (cdResultado)
UNIQUE (cdCampeonato, cdCategoria, cdClasse, colocacao)
INDEX (cdCampeonato, cdCategoria, cdClasse)
```

Em `tbResultadoHistorico`:

```text
INDEX (cdResultado, versaoNegocio)
INDEX (cdResultado, dtEvento)
INDEX (cdResponsavel, dtEvento)
```

## Check constraints

Se a versão alvo do MySQL aplicar `CHECK` de forma confiável, adicionar:

```text
CLASSIFICADO     -> colocacao IS NOT NULL AND colocacao > 0
DESCLASSIFICADO  -> colocacao IS NULL
AUSENTE          -> colocacao IS NULL
```

Mesmo com check no banco, manter validação de domínio na aplicação.

## Migração de `PontuacaoHist`

A migração não deve simplesmente copiar colunas sem reconstruir semântica.

Antes de migrar, inventariar:

- significado real de cada registro atual;
- relação com atleta/competição;
- categoria/classe disponíveis ou ausentes;
- colocação;
- pontos persistidos;
- registros duplicados/inconsistentes;
- vínculo com `Competidores`/inscrição legada;
- estados implícitos hoje existentes.

### Regra central

O valor histórico de pontos de `PontuacaoHist` **não deve se tornar o valor autoritativo do novo `Resultado`**.

Novo modelo:

```text
Resultado = fato esportivo
TemporadaPontuacao/TemporadaPenalidade = regra atual
Ranking = projeção
```

Se os dados legados contiverem apenas pontos sem informação suficiente para reconstruir o fato esportivo, não inventar colocação/categoria/classe.

Esses casos precisam ser classificados antes da migration e podem demandar:

- regra explícita de transformação;
- preservação apenas para consulta histórica legada;
- intervenção administrativa;
- exclusão documentada do escopo de migração automática.

## Estratégia de rollout

### Etapa 1 — tabelas novas

Criar via Flyway:

```text
tbResultado
tbResultadoHistorico
tbResultadoColocacaoReserva
```

Sem remover `PontuacaoHist`.

### Etapa 2 — leitura/inventário legado

Construir relatório de reconciliação antes de migrar.

### Etapa 3 — migration de dados determinísticos

Migrar somente registros cujo fato esportivo possa ser reconstruído sem ambiguidade.

Para cada item migrado:

- criar/usar `Inscricao` correspondente;
- criar `Resultado` consistente;
- criar reserva se `CLASSIFICADO` ativo;
- criar histórico `MIGRACAO`.

### Etapa 4 — nova escrita

Novas operações passam a usar exclusivamente `Inscricao`/`Resultado`.

Evitar dual-write prolongado entre novo modelo e `PontuacaoHist`.

### Etapa 5 — comparação

Comparar relatórios/rankings esperados e validar diferenças explicáveis pelo novo modelo autoritativo.

### Etapa 6 — desativação do legado

Somente após estabilização, remover dependência funcional de `PontuacaoHist`.

Remoção física da tabela pode ocorrer em etapa posterior.

## Testes obrigatórios

### Unitários

- validação de situação/colocação;
- transições de status;
- autorização;
- versionamento de negócio;
- cálculo de impacto via componente puro.

### Integração MySQL/Testcontainers

Obrigatórios para:

- dois lançamentos simultâneos da mesma colocação;
- dois resultados ativos simultâneos para mesma inscrição/categoria/classe;
- edição concorrente do mesmo resultado;
- troca simultânea de colocações;
- liberação de colocação após reprovação;
- liberação após cancelamento;
- correção administrativa para colocação já ocupada;
- rollback mantendo reserva consistente;
- migrations Flyway.

### Cenários de negócio mínimos

```text
1. lançar CLASSIFICADO 1º -> PENDENTE -> atleta aprova
2. segundo lançamento 1º na mesma disputa -> 409
3. pendente 1º é reprovado -> reserva liberada
4. outro atleta consegue usar 1º
5. aprovado é cancelado -> reserva liberada
6. AUSENTE e DESCLASSIFICADO não reservam colocação
7. resultado tardio em temporada ENCERRADA é permitido quando autorizado
8. temporada CANCELADA não autoriza operação ordinária
9. mesmo Resultado produz pontuação diferente em temporadas distintas
10. mudança de TemporadaPontuacao altera ranking sem modificar Resultado
```

## Invariantes consolidadas

- `Resultado` pertence a `Inscricao`, não a `Temporada`;
- atleta e campeonato são derivados da inscrição;
- categoria e classe são obrigatórias em todas as situações;
- classe deve pertencer à categoria;
- situações: `CLASSIFICADO`, `DESCLASSIFICADO`, `AUSENTE`;
- somente `CLASSIFICADO` possui colocação;
- colocação é inteiro positivo sem limite máximo de negócio;
- todo lançamento ordinário nasce `PENDENTE_APROVACAO`;
- atleta decide aprovação/reprovação;
- pendente pode ser editado por profissional autorizado;
- resultado ativo é único por `(Inscricao,Categoria,Classe)`;
- colocação de CLASSIFICADO é única por `(Campeonato,Categoria,Classe,colocacao)` entre pendentes/aprovados;
- pendência já reserva colocação;
- reprovação/cancelamento libera reserva;
- aprovação mantém reserva;
- correção administrativa respeita as mesmas invariantes;
- proprietário pode corrigir/cancelar aprovado com justificativa;
- correção/cancelamento não exige notificação no MVP;
- histórico é append-only;
- `@Version` e `versaoNegocio` possuem responsabilidades distintas;
- concorrência entre linhas diferentes deve ser protegida fisicamente no banco;
- `Resultado` não armazena pontuação autoritativa da temporada;
- ranking interpreta dinamicamente resultados aprovados e elegíveis;
- resultado tardio pode afetar temporada `ENCERRADA`;
- `PontuacaoHist` deve ser migrado apenas quando o fato esportivo puder ser reconstruído deterministicamente.

## Próxima fase

Com `Inscricao` e `Resultado` definidos, a próxima etapa é a Fase 6 — `Ranking` e consultas derivadas.

Essa fase deve definir:

- query model da classificação geral;
- elegibilidade corrente e contribuição histórica;
- soma de impactos positivos/negativos;
- empates de ranking no padrão `1,1,3`;
- projeção de temporada `ATIVO` e `ENCERRADA`;
- suspensão de `CANCELADA`;
- estratégia de performance sem transformar ranking em fonte autoritativa persistida.
