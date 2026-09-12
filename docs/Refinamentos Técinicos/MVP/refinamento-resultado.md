# Refinamento técnico — Resultado

Status: modelagem técnica consolidada para o MVP; implementação e migração pendentes.

## Objetivo

Modelar `Resultado` como o desfecho esportivo obtido por um atleta em uma combinação de categoria e classe dentro de um ciclo específico de inscrição em campeonato, com validação obrigatória pelo atleta antes de produzir efeitos esportivos.

O resultado pode representar colocação classificatória ou situações esportivas específicas como desclassificação e ausência.

## Relação com Inscrição

`Resultado` referencia `cdInscricao`.

A inscrição identifica de forma autoritativa:

- atleta;
- campeonato;
- ciclo de participação.

Não duplicar `cdAtleta` e `cdCompeticao` em `Resultado` como fontes autoritativas.

## Multiplicidade

Uma inscrição pode possuir vários resultados.

Dentro da mesma inscrição, deve existir no máximo um resultado ativo para cada combinação:

```text
(cdInscricao, cdCategoria, cdClasse)
```

Resultados históricos anteriores da mesma combinação podem permanecer armazenados desde que não sejam simultaneamente ativos.

## Entidade conceitual

Campos recomendados:

- `cdResultado`
- `cdInscricao`
- `cdCategoria`
- `cdClasse`
- `situacaoResultado`
- `colocacao`
- `status`
- `nrVersao`
- `lockVersion`
- `cdResponsavelLancamento`
- `dtLancamento`
- `cdResponsavelDecisao`
- `dtDecisao`
- `motivoReprovacao`
- `dtCancelamento`
- `cdResponsavelCancelamento`
- `motivoCancelamento`
- campos de auditoria

`nrVersao` representa a versão de negócio submetida ao atleta. `lockVersion` representa exclusivamente o controle técnico de concorrência otimista.

## Situação esportiva do resultado

Criar informação explícita `situacaoResultado`.

Valores mínimos do MVP:

- `CLASSIFICADO`
- `DESCLASSIFICADO`
- `AUSENTE`

Essas situações são independentes do `status` de workflow do resultado.

Exemplo:

```text
status = APROVADO
situacaoResultado = DESCLASSIFICADO
```

significa que o atleta aprovou o lançamento e que o desfecho esportivo daquele lançamento foi uma desclassificação.

### CLASSIFICADO

- exige `colocacao > 0`;
- pontuação é obtida normalmente pela tabela de colocação da temporada considerando o tipo da classe.

### DESCLASSIFICADO

- não possui colocação classificatória;
- `colocacao` deve ser `null`;
- pode gerar penalidade específica na temporada;
- não deve ser tratado como colocação zero nem como colocação fora da tabela.

### AUSENTE

- não possui colocação classificatória;
- `colocacao` deve ser `null`;
- pode gerar penalidade específica na temporada;
- deve ser tratado separadamente de `DESCLASSIFICADO`, pois cada temporada poderá definir comportamento e valor distintos.

## Estados do resultado

Estados de workflow do MVP:

- `PENDENTE_APROVACAO`
- `APROVADO`
- `CANCELADO`

Não é necessário manter `REPROVADO` como estado ativo separado.

A reprovação é uma ação do atleta que encerra aquele lançamento e o transforma em `CANCELADO`, preservando no histórico o motivo, o responsável pela decisão e a data.

## Fluxo único de aprovação para todas as situações esportivas

`CLASSIFICADO`, `DESCLASSIFICADO` e `AUSENTE` seguem exatamente o mesmo fluxo funcional de lançamento e aprovação.

Nenhuma dessas situações produz efeito esportivo apenas por ter sido informada pelo profissional.

Fluxo:

```text
Profissional lança Resultado
        |
        | situacaoResultado =
        | CLASSIFICADO | DESCLASSIFICADO | AUSENTE
        v
PENDENTE_APROVACAO
        |
        +-- atleta aprova --> APROVADO
        |
        +-- atleta reprova -> CANCELADO
```

Consequências:

- resultado `CLASSIFICADO` só gera pontos depois da aprovação do atleta;
- resultado `DESCLASSIFICADO` só pode aplicar penalidade da temporada depois da aprovação do atleta;
- resultado `AUSENTE` só pode aplicar penalidade da temporada depois da aprovação do atleta;
- enquanto estiver `PENDENTE_APROVACAO`, nenhuma das três situações altera pontuação ou ranking;
- reprovação do atleta cancela o lançamento independentemente da situação esportiva;
- eventual novo lançamento após reprovação segue novamente o fluxo completo de aprovação.

O atleta aprova o **resultado informado**, incluindo sua `situacaoResultado`. A aprovação não representa concordância com a regra de pontuação da temporada; a penalidade é calculada posteriormente segundo a configuração da temporada aplicável.

## Validade esportiva

Um resultado somente produz efeito esportivo quando estiver `APROVADO` e o campeonato estiver esportivamente ativo para a temporada em análise.

Consequentemente:

- `PENDENTE_APROVACAO` não gera pontuação nem penalidade;
- `CANCELADO` não gera pontuação nem penalidade;
- `APROVADO + CLASSIFICADO` pode gerar pontuação positiva, zero ou outro valor definido para a colocação;
- `APROVADO + DESCLASSIFICADO` pode gerar penalidade definida pela temporada;
- `APROVADO + AUSENTE` pode gerar penalidade definida pela temporada.

A penalidade não é propriedade universal do resultado. Ela pertence à regra da temporada que interpreta aquele resultado.

## Edição enquanto pendente

Enquanto o resultado estiver em `PENDENTE_APROVACAO`, ele pode ser editado por qualquer profissional que possua vínculo ativo com o atleta da inscrição.

A autorização não depende de o profissional ter sido o responsável pelo lançamento original.

Regra de autorização mínima:

```text
Resultado.status == PENDENTE_APROVACAO
AND
existe VinculoProfissionalAtleta ATIVO
para (profissional, atleta da Inscricao)
```

A edição deve:

- manter o mesmo `cdResultado`;
- atualizar dados esportivos, inclusive `situacaoResultado` e `colocacao` quando aplicável;
- incrementar `nrVersao`;
- registrar quem realizou a alteração e quando;
- invalidar qualquer solicitação de aprovação vinculada a versão anterior;
- manter o resultado em `PENDENTE_APROVACAO`;
- exigir aprovação do atleta sobre a versão atualizada antes de qualquer efeito esportivo.

Após `APROVADO`, a permissão ordinária de edição por profissionais deixa de existir.

## Intervenção do proprietário sobre resultado aprovado

O proprietário pode corrigir ou cancelar diretamente um resultado `APROVADO`.

Toda intervenção exige justificativa obrigatória, auditoria integral e não gera notificações.

### Correção direta

Pode alterar categoria, classe, situação esportiva e colocação, respeitando as invariantes:

- `CLASSIFICADO` exige colocação positiva;
- `DESCLASSIFICADO` exige colocação nula;
- `AUSENTE` exige colocação nula.

A correção mantém o mesmo `cdResultado`, incrementa `nrVersao`, mantém `status = APROVADO` e recalcula projeções afetadas.

### Cancelamento administrativo

```text
APROVADO -> CANCELADO
```

O cancelamento preserva histórico, remove imediatamente qualquer efeito esportivo do resultado e libera a combinação para novo lançamento.

## Reprovação pelo atleta

Quando o atleta reprova um resultado:

1. `status` passa para `CANCELADO`;
2. registrar atleta, data e motivo;
3. preservar integralmente o registro;
4. liberar a combinação `(cdInscricao, cdCategoria, cdClasse)` para novo lançamento.

O profissional deverá criar novo `Resultado`; o registro cancelado não é corrigido e reutilizado.

## Controle de versão e concorrência

### Versão de negócio — `nrVersao`

Novo resultado nasce com `nrVersao = 1`.

Toda alteração esportiva relevante incrementa a versão, incluindo:

- categoria;
- classe;
- `situacaoResultado`;
- colocação.

A aprovação/reprovação do atleta deve informar a versão visualizada e falhar com conflito se ela estiver desatualizada.

### Concorrência técnica — `lockVersion`

Usar controle técnico de concorrência otimista, preferencialmente JPA `@Version`.

```text
nrVersao    = versão funcional apresentada ao atleta
lockVersion = versão técnica de persistência
```

Não realizar merge implícito de edições concorrentes. Resposta recomendada para conflito: `409 Conflict`.

## Invariante de resultado ativo

Para `(cdInscricao, cdCategoria, cdClasse)`, no máximo um resultado em:

- `PENDENTE_APROVACAO`
- `APROVADO`

`CANCELADO` libera a combinação.

A regra independe de `situacaoResultado`.

## Histórico funcional

Criar mecanismo explícito `ResultadoHistorico` ou equivalente.

Campos mínimos recomendados:

- `cdResultadoHistorico`
- `cdResultado`
- `nrVersao`
- `tipoEvento`
- `cdInscricao`
- `cdCategoria`
- `cdClasse`
- `situacaoResultado`
- `colocacao`
- `status`
- `cdResponsavelEvento`
- `dtEvento`
- `justificativa`
- auditoria

Eventos mínimos:

- `CRIACAO`
- `EDICAO_PENDENTE`
- `APROVACAO_ATLETA`
- `REPROVACAO_ATLETA`
- `CORRECAO_ADMINISTRATIVA`
- `CANCELAMENTO_ADMINISTRATIVO`

Histórico é imutável e não é fonte de verdade operacional.

## Migração da `tbPontuacaoHist`

A estrutura legada não representa adequadamente categoria, classe, situação esportiva, workflow de aprovação, versões e autoria das decisões.

Somente converter para `Resultado` quando todos os atributos obrigatórios puderem ser reconstruídos sem inferência arbitrária.

Não inferir `DESCLASSIFICADO` ou `AUSENTE` a partir de colocação ausente, nula ou zero em dados legados sem evidência explícita.

Registros não conversíveis permanecem preservados para consulta/auditoria histórica durante a transição.

## Relação com a pontuação da temporada

`Resultado` não persiste valor de pontos ou penalidade como verdade autoritativa.

A temporada interpreta o resultado aprovado:

```text
CLASSIFICADO
  -> (colocacao, tipoClasse)
  -> TemporadaPontuacao

DESCLASSIFICADO
  -> TemporadaPenalidade.DESCLASSIFICACAO

AUSENTE
  -> TemporadaPenalidade.AUSENCIA
```

O mesmo resultado pode:

- valer pontuação diferente em temporadas distintas;
- gerar penalidade diferente em temporadas distintas;
- gerar zero em uma temporada e valor negativo em outra.

## Decisões consolidadas

- Resultado pertence a um ciclo específico de `Inscricao`.
- Uma inscrição pode possuir vários resultados.
- Há no máximo um resultado ativo por `(Inscricao, Categoria, Classe)`.
- Resultado possui `situacaoResultado` explícita.
- Situações mínimas: `CLASSIFICADO`, `DESCLASSIFICADO`, `AUSENTE`.
- `CLASSIFICADO` exige `colocacao > 0`.
- `DESCLASSIFICADO` e `AUSENTE` exigem colocação nula.
- Desclassificação e ausência são conceitos distintos.
- Todo novo lançamento nasce `PENDENTE_APROVACAO`, independentemente da situação esportiva.
- `CLASSIFICADO`, `DESCLASSIFICADO` e `AUSENTE` seguem o mesmo fluxo de aprovação/reprovação pelo atleta.
- Somente `APROVADO` produz efeito esportivo.
- Resultado desclassificado ou ausente aprovado pode gerar penalidade da temporada.
- Penalidade não é persistida no resultado.
- Enquanto pendente, qualquer profissional com vínculo ativo com o atleta pode editar.
- Alterar `situacaoResultado` invalida a versão apresentada anteriormente ao atleta e incrementa `nrVersao`.
- Edição relevante incrementa `nrVersao`.
- `nrVersao` e `lockVersion` permanecem separados.
- Após `APROVADO`, profissionais não editam pelo fluxo ordinário.
- Proprietário pode corrigir/cancelar resultado aprovado mediante justificativa, sem notificação.
- Resultado cancelado permanece histórico e não produz efeito esportivo.
- Histórico funcional é explícito e imutável.
- `tbPontuacaoHist` não é semanticamente equivalente a `Resultado`.
