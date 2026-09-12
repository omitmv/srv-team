# Refinamento técnico — Ranking

Status: regras funcionais consolidadas para o MVP.

## Objetivo

Modelar o ranking de temporada como projeção dinâmica derivada de elegibilidade, inscrições, resultados aprovados, pontuação classificatória e penalidades configuradas na própria temporada.

O ranking não é fonte autoritativa persistida no MVP.

## Elegibilidade para ranking geral

Um atleta integra o ranking geral quando:

1. possui vínculo ativo com o criador da temporada;
2. possui ao menos uma `Inscricao` `CONFIRMADA` e não cancelada em campeonato associado;
3. sua conta não está cancelada.

Não é necessário possuir resultado aprovado para aparecer no ranking geral.

## Resultados considerados

Considerar somente `Resultado` que:

- pertence a inscrição elegível;
- está `APROVADO`;
- não está `CANCELADO`;
- pertence a campeonato associado e esportivamente válido para a temporada.

O impacto do resultado depende de `situacaoResultado`.

### CLASSIFICADO

Pontuação calculada por:

```text
(colocacao, tipoClasse)
```

usando `TemporadaPontuacao`.

Se não houver regra para aquela colocação/tipo de classe, impacto = `0`.

A temporada pode deliberadamente pontuar somente até determinada colocação. Resultados de posições superiores podem nem ser lançados no sistema, sem que isso gere ausência ou penalidade.

### DESCLASSIFICADO

Usar a regra `DESCLASSIFICACAO` da temporada.

Se não houver regra configurada, impacto = `0`.

Quando existir mais de um resultado `DESCLASSIFICADO` do atleta no mesmo campeonato, aplicar o `modoAplicacao` configurado em `TemporadaPenalidade.DESCLASSIFICACAO`.

### AUSENTE

Usar a regra `AUSENCIA` da temporada.

Se não houver regra configurada, impacto = `0`.

Quando existir mais de um resultado `AUSENTE` do atleta no mesmo campeonato, aplicar o `modoAplicacao` configurado em `TemporadaPenalidade.AUSENCIA`.

`DESCLASSIFICADO` e `AUSENTE` nunca devem ser convertidos artificialmente em colocação zero ou outra posição fictícia.

Ausência de `Resultado` para uma posição não consolidada pela temporada também não deve ser interpretada como `AUSENTE`.

## Agregação das penalidades

A multiplicidade da penalidade não é fixa no ranking. Ela é definida pela configuração da temporada.

Modos mínimos:

### POR_RESULTADO

Cada resultado aprovado correspondente contribui individualmente.

```text
AUSENCIA = -2
modoAplicacao = POR_RESULTADO

2 resultados AUSENTE no mesmo campeonato
=> -4
```

### UMA_POR_CAMPEONATO

Para cada atleta, campeonato e tipo de penalidade, um ou mais resultados aprovados correspondentes produzem apenas uma ocorrência da penalidade.

Conceitualmente, antes da soma, agrupar por:

```text
(atleta, campeonato, tipoPenalidade)
```

```text
AUSENCIA = -2
modoAplicacao = UMA_POR_CAMPEONATO

2 resultados AUSENTE no mesmo campeonato
=> -2
```

A agregação de `AUSENCIA` e `DESCLASSIFICACAO` é feita separadamente, pois cada uma possui configuração própria.

## Total do atleta

O ranking utiliza soma algébrica após a aplicação do modo de agregação de cada penalidade:

```text
TOTAL =
  soma(pontuacoes CLASSIFICADO)
  + soma(penalidades DESCLASSIFICADO após agregação)
  + soma(penalidades AUSENTE após agregação)
```

Consequentemente, o total pode ser positivo, zero ou negativo.

## Ranking geral

Soma todos os impactos válidos do atleta em todas as categorias/classes/campeonatos associados, respeitando o modo de aplicação configurado para cada penalidade.

Inclui:

- classe `COMUM`;
- classe `OVERALL`;
- penalidades por desclassificação;
- penalidades por ausência.

## Ranking por categoria

Soma somente resultados aprovados da categoria alvo.

Um atleta entra no ranking da categoria após possuir ao menos um resultado `APROVADO` nela, independentemente de esse resultado ser `CLASSIFICADO`, `DESCLASSIFICADO` ou `AUSENTE`.

Assim, um atleta pode entrar no ranking da categoria com zero ou pontuação negativa.

A aplicação de penalidades no ranking por categoria deve respeitar a mesma configuração da temporada, considerando apenas os resultados que pertencem à categoria consultada.

## Pontuação zero e negativa

### Ranking geral

Atleta elegível aparece mesmo sem resultados aprovados, inicialmente com zero.

Após aplicação dos resultados, pode permanecer em zero ou ficar negativo.

### Ranking por categoria

Se houver resultado aprovado na categoria, o atleta aparece mesmo que o total seja zero ou negativo.

## Cálculo da posição

O universo esportivo é calculado antes dos filtros de visualização.

Sequência:

```text
1. obter atletas elegíveis
2. obter resultados válidos
3. calcular pontuações classificatórias
4. agregar penalidades conforme modoAplicacao
5. somar total de cada atleta
6. ordenar total decrescente
7. atribuir posição esportiva
8. aplicar filtros de visibilidade
```

Valores negativos seguem a ordenação numérica normal:

```text
5 > 0 > -2 > -10
```

## Empates no ranking da temporada

Empate é permitido **no ranking derivado da temporada**, porque atletas distintos podem terminar com o mesmo total de pontos.

Totais iguais permanecem empatados e usam ranking de competição:

```text
1, 1, 3, 4...
```

Formalmente:

```text
posicao = 1 + quantidade de atletas com total estritamente maior
```

Isso vale também para empates em zero ou valores negativos.

Não aplicar critérios esportivos adicionais de desempate no MVP.

Importante: essa regra de empate do ranking da temporada **não significa empate de colocação no resultado do campeonato**. A colocação esportiva informada para uma mesma combinação de campeonato/categoria/classe é única.

## Filtros de visibilidade

Filtros são aplicados somente depois da posição real ter sido calculada.

Não renumerar ranking por observador.

## Alterações que exigem recálculo

Incluem:

- aprovação/cancelamento/correção de resultado;
- mudança de `situacaoResultado` em correção administrativa;
- alteração de `TemporadaPontuacao`;
- alteração do valor de penalidade por desclassificação;
- alteração do valor de penalidade por ausência;
- alteração de `modoAplicacao` de qualquer penalidade;
- associação/remoção de campeonato;
- cancelamento/reativação de campeonato;
- confirmação/cancelamento de inscrição;
- criação/encerramento de vínculo com criador;
- cancelamento/reativação da conta do atleta.

## Precisão numérica

Usar `BigDecimal` em todos os cálculos.

Não converter para `double`/`float`.

Pontuação, penalidades e totais devem ser comparados numericamente, inclusive valores negativos.

Evitar arredondamentos intermediários desnecessários.

## Persistência e performance

Ranking permanece projeção calculada.

Materialização/cache futura é permitida apenas se totalmente reconstruível a partir do domínio e corretamente invalidada pelos eventos relevantes.

## Ranking encerrado

Temporada `ENCERRADA` continua consultável e sujeita às regras dinâmicas já definidas.

Não criar snapshot imutável no MVP.

## Relatórios

Relatórios de temporada usam a mesma projeção.

Devem conseguir distinguir:

- pontos de colocação;
- penalidade por desclassificação;
- penalidade por ausência;
- modo de aplicação utilizado;
- total final.

Relatório de campeonato continua exibindo o desfecho esportivo original do campeonato; a penalidade é interpretação específica de cada temporada.

## Invariantes consolidadas

- ranking pertence à temporada;
- ranking é derivado dinamicamente;
- elegibilidade é calculada antes da pontuação;
- resultado aprovado pode produzir ponto positivo, zero ou penalidade negativa;
- `CLASSIFICADO` usa tabela por colocação/tipo de classe;
- posição não consolidada pela temporada pode simplesmente não possuir resultado lançado;
- ausência de resultado não equivale a `AUSENTE`;
- `DESCLASSIFICADO` usa penalidade própria da temporada;
- `AUSENTE` usa penalidade própria e independente;
- cada penalidade possui `valor` e `modoAplicacao` configuráveis;
- `POR_RESULTADO` aplica penalidade para cada resultado aprovado correspondente;
- `UMA_POR_CAMPEONATO` aplica no máximo uma penalidade por atleta/campeonato/tipo;
- desclassificação e ausência podem usar modos diferentes;
- ausência de regra de penalidade significa impacto zero;
- total do ranking pode ser negativo;
- ranking por categoria aceita atleta com total zero ou negativo após primeiro resultado aprovado;
- visibilidade não altera posição esportiva;
- empate existe no ranking da temporada, não na colocação do campeonato;
- empate do ranking usa padrão `1, 1, 3`;
- não existe desempate adicional no ranking;
- todos os cálculos usam `BigDecimal`.
