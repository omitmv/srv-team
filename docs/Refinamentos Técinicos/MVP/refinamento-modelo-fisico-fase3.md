# Refinamento técnico — Modelo físico Fase 3: Pontuação e penalidades da temporada

Status: desenho técnico consolidado para implementação; execução pendente.

## Objetivo

Traduzir as regras funcionais de `TemporadaPontuacao` e `TemporadaPenalidade` para modelo físico/JPA, preservando o princípio central do MVP:

```text
Resultado = fato esportivo
TemporadaPontuacao / TemporadaPenalidade = regra atual de interpretação
Ranking = projeção derivada
```

A pontuação/penalidade não deve ser persistida em `Resultado` como verdade histórica autoritativa.

## Entidades da Fase 3

Criar:

- `TemporadaPontuacao`;
- `TemporadaPenalidade`;
- `TemporadaPontuacaoHistorico`;
- `TemporadaPenalidadeHistorico`.

Os históricos são administrativos/auditáveis e não alteram a regra funcional de que o ranking corrente sempre utiliza a configuração atual.

## TemporadaPontuacao

### Responsabilidade

Representar a pontuação atual aplicada a um resultado `CLASSIFICADO` conforme:

```text
Temporada + colocacao + TipoClasse
```

### Tabela proposta

```text
tbTemporadaPontuacao
- cdTemporadaPontuacao       PK BIGINT/INT AUTO_INCREMENT
- cdTemporada                FK -> tbTemporada
- nrPosicao                  INT NOT NULL
- tpClasse                   VARCHAR(20) NOT NULL
- vlPontuacao                DECIMAL(10,3) NOT NULL
- nrVersao                   BIGINT NOT NULL DEFAULT 0
- dtCadastro                 DATETIME(6) NOT NULL
- dtAtualizacao              DATETIME(6) NOT NULL
- cdResponsavelCadastro      FK -> tbUsuario
- cdResponsavelAtualizacao   FK -> tbUsuario
```

### Regras

- `nrPosicao > 0`;
- `tpClasse IN ('COMUM', 'OVERALL')`;
- `vlPontuacao` usa `DECIMAL(10,3)`;
- somente uma regra atual por `(cdTemporada, nrPosicao, tpClasse)`;
- ausência da regra significa impacto `0`;
- não criar regra implícita para posições não configuradas;
- alteração da regra não modifica `Resultado`.

### Unicidade

Constraint obrigatória:

```text
UNIQUE (cdTemporada, nrPosicao, tpClasse)
```

Como não existe histórico funcional por linha ativa nessa tabela, não é necessária unicidade condicional.

## TemporadaPenalidade

### Responsabilidade

Representar a penalidade atual aplicada a resultados `DESCLASSIFICADO` ou `AUSENTE`.

### Tabela proposta

```text
tbTemporadaPenalidade
- cdTemporadaPenalidade      PK BIGINT/INT AUTO_INCREMENT
- cdTemporada                FK -> tbTemporada
- tpPenalidade               VARCHAR(40) NOT NULL
- vlPenalidade               DECIMAL(10,3) NOT NULL
- nrVersao                   BIGINT NOT NULL DEFAULT 0
- dtCadastro                 DATETIME(6) NOT NULL
- dtAtualizacao              DATETIME(6) NOT NULL
- cdResponsavelCadastro      FK -> tbUsuario
- cdResponsavelAtualizacao   FK -> tbUsuario
```

Enum de domínio:

```text
TipoPenalidade
- DESCLASSIFICACAO
- AUSENCIA
```

### Regras

- unicidade por `(cdTemporada, tpPenalidade)`;
- ausência da regra significa impacto `0`;
- cada `Resultado APROVADO` de situação correspondente recebe a penalidade individualmente;
- não existe agregação por campeonato, categoria ou tipo de situação;
- o total da temporada pode ficar negativo.

Constraint obrigatória:

```text
UNIQUE (cdTemporada, tpPenalidade)
```

## JPA

### TemporadaPontuacao

Modelo conceitual:

```java
@Entity
@Table(
    name = "tbTemporadaPontuacao",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_temporada_pontuacao",
        columnNames = {"cdTemporada", "nrPosicao", "tpClasse"}
    )
)
class TemporadaPontuacao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer cdTemporadaPontuacao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cdTemporada", nullable = false)
    Temporada temporada;

    @Column(name = "nrPosicao", nullable = false)
    Integer posicao;

    @Enumerated(EnumType.STRING)
    @Column(name = "tpClasse", nullable = false, length = 20)
    TipoClasse tipoClasse;

    @Column(name = "vlPontuacao", nullable = false, precision = 10, scale = 3)
    BigDecimal pontuacao;

    @Version
    @Column(name = "nrVersao", nullable = false)
    Long versao;
}
```

### TemporadaPenalidade

Mesmo padrão, com `TipoPenalidade` e `BigDecimal`.

## Histórico administrativo

Como a regra atual é autoritativa, o histórico não deve ser lido para calcular ranking corrente.

Criar tabelas append-only:

```text
tbTemporadaPontuacaoHistorico
- cdHistorico
- cdTemporadaPontuacao
- cdTemporada
- nrPosicao
- tpClasse
- vlPontuacaoAnterior
- vlPontuacaoNova
- tpOperacao
- cdResponsavel
- dtOperacao
- dsJustificativa opcional
- auditoria técnica
```

```text
tbTemporadaPenalidadeHistorico
- cdHistorico
- cdTemporadaPenalidade
- cdTemporada
- tpPenalidade
- vlPenalidadeAnterior
- vlPenalidadeNova
- tpOperacao
- cdResponsavel
- dtOperacao
- dsJustificativa opcional
- auditoria técnica
```

Enum técnico recomendado:

```text
TipoOperacaoConfiguracao
- INCLUSAO
- ALTERACAO
- REMOCAO
```

### Remoção da regra

Remover regra de pontuação/penalidade significa remover a configuração atual da tabela principal e registrar histórico `REMOCAO`.

Não manter linha inativa na tabela principal apenas para representar ausência, pois funcionalmente:

```text
regra ausente = impacto 0
```

A exclusão física da configuração atual é aceitável porque a auditoria permanece no histórico append-only.

## Estado da temporada

Alteração de pontuação/penalidade só é permitida quando:

```text
Temporada.status = ATIVO
```

`ENCERRADA` exige reabertura para `ATIVO` antes de qualquer alteração estrutural.

`CANCELADA` não permite alteração ordinária de configuração.

Autorização:

```text
criador da temporada
OU TemporadaProfissional.ADMINISTRACAO ativa
```

`CONSULTA` nunca altera pontuação/penalidade.

## Serviço de aplicação

Separar serviços por responsabilidade:

```text
TemporadaPontuacaoService
TemporadaPenalidadeService
```

Ambos devem reutilizar a política de autorização central:

```text
TemporadaAuthorizationService.requireAdministracao(...)
```

Fluxo de alteração:

```text
1. carregar temporada
2. validar status ATIVO
3. validar autorização
4. validar payload
5. carregar regra atual quando aplicável
6. persistir inclusão/alteração/remoção
7. registrar histórico append-only
8. invalidar/recalcular projeções afetadas
9. commit da transação
```

Histórico e alteração da regra devem estar na mesma transação.

## Recálculo retroativo

Regra funcional:

```text
qualquer inclusão, alteração ou remoção
-> recalcula retroativamente todos os Resultados APROVADOS válidos/elegíveis da temporada afetada
```

Isso não significa necessariamente persistir materialização de ranking.

### MVP sem ranking materializado

Se o ranking continuar calculado sob demanda:

- não existe lote obrigatório de atualização de linhas de ranking;
- a mudança de configuração passa a valer imediatamente nas próximas consultas;
- caches/projections eventualmente existentes devem ser invalidados.

### Se houver cache/materialização futura

A implementação deve publicar/invocar mecanismo explícito de invalidação/rebuild, mas o cache nunca pode virar fonte autoritativa.

Serviço técnico recomendado:

```text
RankingInvalidationService
```

Responsabilidade:

```text
invalidateTemporada(cdTemporada)
```

Mesmo que inicialmente a implementação seja no-op, a fronteira explícita evita espalhar lógica de cache pelos services de configuração.

## Eventos de domínio/aplicação

Não é obrigatório introduzir broker/event bus no MVP.

Pode-se usar evento de aplicação Spring após alteração bem-sucedida:

```text
TemporadaPontuacaoAlteradaEvent
TemporadaPenalidadeAlteradaEvent
```

Mas somente se isso simplificar desacoplamento.

Critério:

- transação e consistência da regra permanecem síncronas;
- invalidação de cache pode ocorrer após commit;
- não usar evento assíncrono para tornar eventual uma regra que precisa estar imediatamente consistente.

## Endpoints sugeridos

### Pontuação

```text
GET    /temporadas/{cdTemporada}/pontuacoes
POST   /temporadas/{cdTemporada}/pontuacoes
PUT    /temporadas/{cdTemporada}/pontuacoes/{cdTemporadaPontuacao}
DELETE /temporadas/{cdTemporada}/pontuacoes/{cdTemporadaPontuacao}
```

### Penalidades

```text
GET    /temporadas/{cdTemporada}/penalidades
PUT    /temporadas/{cdTemporada}/penalidades/{tipoPenalidade}
DELETE /temporadas/{cdTemporada}/penalidades/{tipoPenalidade}
```

Para penalidades, `PUT` por tipo é aceitável porque existe no máximo uma configuração por tipo/temporada.

## DTOs sugeridos

```text
CreateTemporadaPontuacaoRequest
UpdateTemporadaPontuacaoRequest
TemporadaPontuacaoResponse

UpsertTemporadaPenalidadeRequest
TemporadaPenalidadeResponse
```

Campos monetários/pontuação devem usar `BigDecimal`, nunca `double`/`float`.

## Validações

### Pontuação

- posição obrigatória;
- posição inteira positiva;
- tipo de classe obrigatório;
- valor obrigatório;
- escala máxima 3 casas decimais;
- conflito de unicidade -> `409 Conflict`.

### Penalidade

- tipo obrigatório;
- valor obrigatório;
- escala máxima 3 casas decimais;
- conflito de unicidade -> `409 Conflict`.

## Concorrência

Usar `@Version` para edição concorrente da mesma regra.

A unicidade física protege criação concorrente da mesma chave lógica.

Exemplo:

```text
T1 tenta criar: temporada=1, posição=1, COMUM
T2 tenta criar: temporada=1, posição=1, COMUM
```

Uma operação deve vencer e a outra resultar em conflito controlado.

Não confiar somente em `existsBy...()` antes do insert.

## Índices

### tbTemporadaPontuacao

```text
PK (cdTemporadaPontuacao)
UNIQUE (cdTemporada, nrPosicao, tpClasse)
INDEX (cdTemporada)
INDEX (cdTemporada, tpClasse, nrPosicao)
```

### tbTemporadaPenalidade

```text
PK (cdTemporadaPenalidade)
UNIQUE (cdTemporada, tpPenalidade)
INDEX (cdTemporada)
```

### históricos

```text
INDEX (cdTemporada, dtOperacao)
INDEX (cdTemporadaPontuacao, dtOperacao)
INDEX (cdTemporadaPenalidade, dtOperacao)
```

## Integração com Resultado

O domínio deve calcular impacto sem alterar `Resultado`:

```text
CLASSIFICADO
-> resultado.colocacao
-> resultado.classe.tipoClasse
-> buscar TemporadaPontuacao atual
-> ausente = 0

DESCLASSIFICADO
-> buscar TemporadaPenalidade.DESCLASSIFICACAO atual
-> ausente = 0

AUSENTE
-> buscar TemporadaPenalidade.AUSENCIA atual
-> ausente = 0
```

Nunca persistir em `Resultado`:

- pontuação aplicada;
- penalidade aplicada;
- snapshot da regra como fonte autoritativa.

## Componente de cálculo

Criar componente puro/testável:

```text
ResultadoImpactCalculator
```

Entrada conceitual:

```text
Resultado
Temporada
configuração atual da temporada
```

Saída:

```text
BigDecimal impacto
TipoContribuicao
```

Enum útil para relatórios:

```text
TipoContribuicao
- PONTUACAO
- PENALIDADE_DESCLASSIFICACAO
- PENALIDADE_AUSENCIA
```

Esse componente não persiste nada e não decide autorização.

## Precisão

Persistência:

```text
DECIMAL(10,3)
```

Java:

```text
BigDecimal
```

Regras:

- não converter para `double`;
- comparação por valor numérico apropriado;
- evitar arredondamento intermediário;
- escala de API deve ser normalizada/validada, não silenciosamente truncada.

## Migration Flyway

Ordem recomendada:

```text
1. criar tbTemporadaPontuacao
2. criar tbTemporadaPenalidade
3. criar históricos
4. constraints/FKs/índices
5. validar dados existentes que possam ser migrados de Pontuacao legado
```

### Legado Pontuacao

Não migrar automaticamente `Pontuacao` legado para `TemporadaPontuacao` sem conseguir determinar inequivocamente:

- temporada de destino;
- posição;
- tipo de classe (`COMUM`/`OVERALL`);
- valor.

Quando essa semântica não existir no legado, manter o dado antigo apenas como histórico/compatibilidade e iniciar a nova temporada com configuração explícita.

## Testes obrigatórios

### Unitários

- `CLASSIFICADO` com regra -> valor correto;
- `CLASSIFICADO` sem regra -> zero;
- `DESCLASSIFICADO` com/sem penalidade;
- `AUSENTE` com/sem penalidade;
- valor negativo;
- múltiplos resultados acumulam individualmente;
- tipo `OVERALL` usa tabela diferente de `COMUM`.

### Integração MySQL/Testcontainers

- unicidade concorrente de pontuação;
- unicidade concorrente de penalidade;
- `@Version` em alteração simultânea;
- histórico criado na mesma transação;
- rollback não deixa histórico órfão;
- `ENCERRADA` bloqueia alteração;
- `CANCELADA` bloqueia alteração ordinária;
- somente criador/ADMINISTRACAO altera.

## Critério de pronto da Fase 3

A Fase 3 está pronta para implementação quando estiverem definidos/implementados:

- tabelas e migrations;
- entidades JPA;
- enums;
- repositories;
- services de configuração;
- autorização contextual;
- históricos append-only;
- constraints de unicidade;
- `@Version`;
- invalidação da projeção/ranking;
- componente puro de cálculo de impacto;
- testes unitários e integração MySQL.

## Invariantes consolidadas

- configuração atual é autoritativa para ranking;
- regra de pontuação é única por temporada/posição/tipoClasse;
- penalidade é única por temporada/tipo;
- regra ausente = impacto zero;
- alterações só em temporada `ATIVO`;
- `ENCERRADA` exige reabertura;
- `CANCELADA` bloqueia alteração ordinária;
- somente criador ou `ADMINISTRACAO` altera;
- cada resultado aprovado contribui independentemente;
- alteração é retroativa para todos os resultados elegíveis;
- histórico administrativo é append-only;
- histórico não é usado para reconstruir ranking corrente;
- `Resultado` não armazena pontuação/penalidade autoritativa;
- ranking permanece projeção reconstruível;
- todos os cálculos usam `BigDecimal`.
