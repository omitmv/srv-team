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
- deve ser tratado separadamente de `DESCLASSIFICADO`.

## Lançamento manual da situação esportiva

Toda situação esportiva é informada explicitamente por um profissional autorizado.

O sistema não deve inferir automaticamente `CLASSIFICADO`, `DESCLASSIFICADO` ou `AUSENTE` a partir de inscrição, ausência de colocação, falta de lançamento anterior, presença em campeonato ou qualquer outro dado indireto.

Fluxos funcionais equivalentes:

```text
Profissional informa CLASSIFICADO + colocacao
        -> PENDENTE_APROVACAO

Profissional informa DESCLASSIFICADO
        -> PENDENTE_APROVACAO

Profissional informa AUSENTE
        -> PENDENTE_APROVACAO
```

Portanto:

- colocação é lançada manualmente pelo profissional;
- desclassificação é lançada manualmente pelo profissional;
- ausência é lançada manualmente pelo profissional;
- nenhuma dessas situações produz efeito esportivo antes da aprovação do atleta;
- ausência de um lançamento não pode ser interpretada como `AUSENTE`;
- ausência de colocação não pode ser interpretada como `DESCLASSIFICADO`;
- nenhuma penalidade pode ser aplicada com base em inferência automática.

## Unicidade da colocação no campeonato

No resultado oficial do campeonato **não existe empate de colocação** dentro da mesma combinação de campeonato, categoria e classe.

Para resultados `CLASSIFICADO`, uma colocação informada deve ser única no conjunto esportivo correspondente.

Conceitualmente:

```text
(cdCampeonato, cdCategoria, cdClasse, colocacao)
```

não pode pertencer simultaneamente a dois resultados ativos/aprovados de atletas diferentes.

Exemplo válido:

```text
Atleta A -> 1º
Atleta B -> 2º
Atleta C -> 3º
```

Exemplo inválido:

```text
Atleta A -> 1º
Atleta B -> 2º
Atleta C -> 2º
```

Essa regra é diferente do empate no **ranking da temporada**, onde atletas podem possuir o mesmo total de pontos.

### Colocações não informadas

A temporada pode consolidar pontuação somente até determinada posição. Nesse cenário, posições esportivas superiores podem simplesmente não ser lançadas no sistema.

Exemplo:

```text
Temporada pontua até 5º lugar.
Campeonato possui 8 classificados.

Resultados 1º a 5º -> podem ser lançados
Resultados 6º a 8º -> podem não ser informados
```

Consequências:

- não é necessário criar resultados artificiais para posições não utilizadas pela temporada;
- ausência de lançamento de posição superior não equivale a `AUSENTE`;
- ausência de lançamento não gera penalidade;
- não preencher lacunas automaticamente;
- se uma colocação superior for efetivamente lançada, continua sujeita à unicidade esportiva e pode valer zero caso a temporada não possua regra de pontuação para ela.

## Estados do resultado

Estados de workflow do MVP:

- `PENDENTE_APROVACAO`
- `APROVADO`
- `CANCELADO`

Não é necessário manter `REPROVADO` como estado ativo separado.

A reprovação é uma ação do atleta que encerra aquele lançamento e o transforma em `CANCELADO`, preservando no histórico o motivo, o responsável pela decisão e a data.

## Fluxo único de aprovação para todas as situações esportivas

`CLASSIFICADO`, `DESCLASSIFICADO` e `AUSENTE` seguem exatamente o mesmo fluxo funcional de lançamento e aprovação.

```text
Profissional lança Resultado
        |
        v
PENDENTE_APROVACAO
        |
        +-- atleta aprova --> APROVADO
        |
        +-- atleta reprova -> CANCELADO
```

Consequências:

- resultado `CLASSIFICADO` só gera pontos depois da aprovação do atleta;
- resultado `DESCLASSIFICADO` só pode aplicar penalidade depois da aprovação do atleta;
- resultado `AUSENTE` só pode aplicar penalidade depois da aprovação do atleta;
- enquanto `PENDENTE_APROVACAO`, nenhuma situação altera pontuação ou ranking;
- reprovação cancela o lançamento independentemente da situação esportiva.

O atleta aprova o resultado informado, incluindo sua `situacaoResultado`. A aprovação não representa concordância com a regra de pontuação da temporada.

## Validade esportiva

Um resultado somente produz efeito esportivo quando estiver `APROVADO` e o campeonato estiver esportivamente ativo para a temporada em análise.

Consequentemente:

- `PENDENTE_APROVACAO` não gera pontuação nem penalidade;
- `CANCELADO` não gera pontuação nem penalidade;
- `APROVADO + CLASSIFICADO` pode gerar pontuação positiva, zero ou outro valor definido para a colocação;
- `APROVADO + DESCLASSIFICADO` pode gerar penalidade definida pela temporada;
- `APROVADO + AUSENTE` pode gerar penalidade definida pela temporada.

A penalidade não é propriedade universal do resultado. Ela pertence à regra da temporada.

## Edição enquanto pendente

Enquanto o resultado estiver em `PENDENTE_APROVACAO`, ele pode ser editado por qualquer profissional que possua vínculo ativo com o atleta da inscrição.

A edição deve:

- manter o mesmo `cdResultado`;
- atualizar dados esportivos, inclusive `situacaoResultado` e `colocacao` quando aplicável;
- incrementar `nrVersao`;
- registrar quem realizou a alteração e quando;
- invalidar qualquer solicitação de aprovação vinculada a versão anterior;
- manter o resultado em `PENDENTE_APROVACAO`;
- exigir aprovação do atleta sobre a versão atualizada.

Após `APROVADO`, a permissão ordinária de edição por profissionais deixa de existir.

## Intervenção do proprietário sobre resultado aprovado

O proprietário pode corrigir ou cancelar diretamente um resultado `APROVADO`.

Toda intervenção exige justificativa obrigatória, auditoria integral e não gera notificações.

### Correção direta

Pode alterar categoria, classe, situação esportiva e colocação, respeitando as invariantes:

- `CLASSIFICADO` exige colocação positiva;
- `DESCLASSIFICADO` exige colocação nula;
- `AUSENTE` exige colocação nula;
- colocação de `CLASSIFICADO` deve continuar única na combinação campeonato/categoria/classe.

A correção mantém o mesmo `cdResultado`, incrementa `nrVersao`, mantém `status = APROVADO` e recalcula projeções afetadas.

### Cancelamento administrativo

```text
APROVADO -> CANCELADO
```

O cancelamento preserva histórico, remove qualquer efeito esportivo e libera a combinação para novo lançamento.

## Reprovação pelo atleta

Quando o atleta reprova um resultado:

1. `status` passa para `CANCELADO`;
2. registrar atleta, data e motivo;
3. preservar integralmente o registro;
4. liberar a combinação `(cdInscricao, cdCategoria, cdClasse)` para novo lançamento.

## Controle de versão e concorrência

Novo resultado nasce com `nrVersao = 1`.

Toda alteração esportiva relevante incrementa a versão, incluindo:

- categoria;
- classe;
- `situacaoResultado`;
- colocação.

A aprovação/reprovação do atleta deve informar a versão visualizada e falhar com conflito se estiver desatualizada.

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

Histórico é imutável e não é fonte de verdade operacional.

## Migração da `tbPontuacaoHist`

A estrutura legada não representa adequadamente categoria, classe, situação esportiva, workflow de aprovação, versões e autoria das decisões.

Somente converter para `Resultado` quando todos os atributos obrigatórios puderem ser reconstruídos sem inferência arbitrária.

Não inferir `DESCLASSIFICADO` ou `AUSENTE` a partir de colocação ausente, nula ou zero em dados legados sem evidência explícita.

## Relação com a pontuação da temporada

`Resultado` não persiste valor de pontos ou penalidade como verdade autoritativa.

```text
CLASSIFICADO
  -> (colocacao, tipoClasse)
  -> TemporadaPontuacao

DESCLASSIFICADO
  -> TemporadaPenalidade.DESCLASSIFICACAO

AUSENTE
  -> TemporadaPenalidade.AUSENCIA
```

## Decisões consolidadas

- Resultado pertence a um ciclo específico de `Inscricao`.
- Uma inscrição pode possuir vários resultados.
- Há no máximo um resultado ativo por `(Inscricao, Categoria, Classe)`.
- Situações mínimas: `CLASSIFICADO`, `DESCLASSIFICADO`, `AUSENTE`.
- `CLASSIFICADO` exige `colocacao > 0`.
- `DESCLASSIFICADO` e `AUSENTE` exigem colocação nula.
- Toda situação esportiva é lançada manualmente por profissional autorizado.
- O sistema não infere ausência ou desclassificação automaticamente.
- Ausência de lançamento não equivale a `AUSENTE`.
- Ausência de colocação não equivale a `DESCLASSIFICADO`.
- Não existe empate de colocação dentro da mesma combinação campeonato/categoria/classe.
- Uma colocação classificatória informada é única nesse conjunto esportivo.
- Posições superiores ao limite consolidado pela temporada podem não ser lançadas.
- Posição não lançada não equivale a ausência e não gera penalidade.
- Todo novo lançamento nasce `PENDENTE_APROVACAO`.
- Todas as situações seguem o mesmo fluxo de aprovação/reprovação pelo atleta.
- Somente `APROVADO` produz efeito esportivo.
- Penalidade não é persistida no resultado.
- Enquanto pendente, qualquer profissional com vínculo ativo com o atleta pode editar.
- Alteração relevante incrementa `nrVersao`.
- `nrVersao` e `lockVersion` permanecem separados.
- Após `APROVADO`, profissionais não editam pelo fluxo ordinário.
- Proprietário pode corrigir/cancelar resultado aprovado mediante justificativa, sem notificação.
- Resultado cancelado permanece histórico e não produz efeito esportivo.
- Histórico funcional é explícito e imutável.
- `tbPontuacaoHist` não é semanticamente equivalente a `Resultado`.
