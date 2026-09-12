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

### DESCLASSIFICADO

Usar a regra `DESCLASSIFICACAO` da temporada.

Se não houver regra configurada, impacto = `0`.

### AUSENTE

Usar a regra `AUSENCIA` da temporada.

Se não houver regra configurada, impacto = `0`.

`DESCLASSIFICADO` e `AUSENTE` nunca devem ser convertidos artificialmente em colocação zero ou outra posição fictícia.

## Total do atleta

O ranking utiliza soma algébrica:

```text
TOTAL =
  soma(pontuacoes CLASSIFICADO)
  + soma(penalidades DESCLASSIFICADO)
  + soma(penalidades AUSENTE)
```

Consequentemente, o total pode ser:

- positivo;
- zero;
- negativo.

Exemplo:

```text
1º lugar COMUM = +10
DESCLASSIFICACAO = -5
AUSENCIA = -2

Atleta A:
+10 -5 = 5

Atleta B:
-5 -2 = -7
```

## Ranking geral

Soma todos os impactos válidos do atleta em todas as categorias/classes/campeonatos associados.

Inclui:

- classe `COMUM`;
- classe `OVERALL`;
- penalidades por desclassificação;
- penalidades por ausência.

## Ranking por categoria

Soma somente resultados aprovados da categoria alvo.

Um atleta entra no ranking da categoria após possuir ao menos um resultado `APROVADO` nela, independentemente de esse resultado ser:

- `CLASSIFICADO`;
- `DESCLASSIFICADO`;
- `AUSENTE`.

Assim, um atleta pode entrar no ranking da categoria já com zero ou pontuação negativa.

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
2. calcular todos os impactos válidos
3. somar total de cada atleta
4. ordenar total decrescente
5. atribuir posição esportiva
6. aplicar filtros de visibilidade
```

Valores negativos seguem a ordenação numérica normal:

```text
5 > 0 > -2 > -10
```

## Empates

Totais iguais permanecem empatados.

Usar ranking de competição:

```text
1, 1, 3, 4...
```

Formalmente:

```text
posicao = 1 + quantidade de atletas com total estritamente maior
```

Isso vale também para empates em zero ou em valores negativos.

Não aplicar critérios esportivos adicionais de desempate no MVP.

## Filtros de visibilidade

Filtros são aplicados somente depois da posição real ter sido calculada.

Não renumerar ranking por observador.

## Alterações que exigem recálculo

Incluem:

- aprovação/cancelamento/correção de resultado;
- mudança de `situacaoResultado` em correção administrativa;
- alteração de `TemporadaPontuacao`;
- alteração de penalidade por desclassificação;
- alteração de penalidade por ausência;
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
- total final.

Relatório de campeonato continua exibindo o desfecho esportivo original do campeonato; a penalidade é interpretação específica de cada temporada.

## Invariantes consolidadas

- ranking pertence à temporada;
- ranking é derivado dinamicamente;
- elegibilidade é calculada antes da pontuação;
- resultado aprovado pode produzir ponto positivo, zero ou penalidade negativa;
- `CLASSIFICADO` usa tabela por colocação/tipo de classe;
- `DESCLASSIFICADO` usa penalidade própria da temporada;
- `AUSENTE` usa penalidade própria e independente;
- ausência de regra de penalidade significa impacto zero;
- total do ranking pode ser negativo;
- ranking por categoria aceita atleta com total zero ou negativo após primeiro resultado aprovado;
- visibilidade não altera posição esportiva;
- empate usa padrão `1, 1, 3`;
- não existe desempate adicional;
- todos os cálculos usam `BigDecimal`.
