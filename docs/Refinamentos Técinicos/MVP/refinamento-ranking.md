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

Cada resultado válido é interpretado individualmente e gera uma contribuição independente para o total do atleta.

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

Cada resultado `DESCLASSIFICADO` aprovado contribui individualmente para o total.

### AUSENTE

Usar a regra `AUSENCIA` da temporada.

Se não houver regra configurada, impacto = `0`.

Cada resultado `AUSENTE` aprovado contribui individualmente para o total.

`DESCLASSIFICADO` e `AUSENTE` nunca devem ser convertidos artificialmente em colocação zero ou outra posição fictícia.

Ausência de `Resultado` para uma posição não consolidada pela temporada também não deve ser interpretada como `AUSENTE`.

## Soma dos impactos

Não existe agregação de penalidade por campeonato, categoria ou tipo de situação no MVP.

A regra é simples:

```text
para cada Resultado APROVADO válido:
    calcular impacto daquele resultado

TOTAL = soma de todos os impactos
```

Exemplo:

```text
Atleta 1 / Campeonato 1

Classic Physique / Classe 1        -> 1º lugar
Classic Physique / Combate         -> AUSENTE
Classic Physique / Overall         -> 1º lugar
Culturismo Clássico / Classe 1     -> AUSENTE
Culturismo Clássico / Master 1     -> DESCLASSIFICADO
```

A consolidação da temporada é:

```text
TOTAL =
  pontos(1º lugar COMUM)
  + penalidade(AUSENCIA)
  + pontos(1º lugar OVERALL)
  + penalidade(AUSENCIA)
  + penalidade(DESCLASSIFICACAO)
```

Assim, dois resultados `AUSENTE` geram duas aplicações da penalidade de ausência, mesmo pertencendo ao mesmo campeonato.

## Ranking geral

Soma todos os impactos válidos do atleta em todas as categorias, classes e campeonatos associados à temporada.

Inclui:

- resultados de classe `COMUM`;
- resultados de classe `OVERALL`;
- cada penalidade por desclassificação;
- cada penalidade por ausência.

## Categoria no ranking — fora do MVP

A categoria permanece parte do `Resultado` e deve continuar disponível no modelo para consultas, filtros e evolução futura.

Entretanto, **ranking por categoria não faz parte do MVP**.

Nenhuma regra específica de posição, elegibilidade, empate ou consolidação por categoria deve ser implementada agora.

Essa dimensão fica explicitamente mapeada para refinamento posterior.

## Pontuação zero e negativa

Atleta elegível aparece no ranking geral mesmo sem resultados aprovados, inicialmente com zero.

Após aplicação dos resultados, pode permanecer em zero ou ficar negativo.

## Cálculo da posição

O universo esportivo é calculado antes dos filtros de visualização.

Sequência:

```text
1. obter atletas elegíveis
2. obter todos os resultados aprovados válidos
3. calcular individualmente o impacto de cada resultado
4. somar os impactos por atleta
5. ordenar total decrescente
6. atribuir posição esportiva
7. aplicar filtros de visibilidade
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

Devem conseguir distinguir cada contribuição individual:

- pontos de colocação;
- penalidade por desclassificação;
- penalidade por ausência;
- categoria e classe de origem do resultado;
- total final.

Relatório de campeonato continua exibindo o desfecho esportivo original do campeonato; a penalidade é interpretação específica de cada temporada.

Relatórios/rankings específicos por categoria ficam fora do MVP e serão refinados posteriormente.

## Invariantes consolidadas

- ranking pertence à temporada;
- ranking é derivado dinamicamente;
- elegibilidade é calculada antes da pontuação;
- cada `Resultado APROVADO` válido gera uma contribuição independente para o total;
- `CLASSIFICADO` usa tabela por colocação/tipo de classe;
- posição não consolidada pela temporada pode simplesmente não possuir resultado lançado;
- ausência de resultado não equivale a `AUSENTE`;
- cada `DESCLASSIFICADO` usa a penalidade própria da temporada;
- cada `AUSENTE` usa a penalidade própria e independente;
- múltiplos resultados de ausência/desclassificação acumulam individualmente, mesmo no mesmo campeonato;
- não existe agregação ou `modoAplicacao` de penalidades no MVP;
- ausência de regra de penalidade significa impacto zero;
- total do ranking pode ser negativo;
- categoria permanece mapeada no resultado, mas ranking por categoria está fora do MVP;
- visibilidade não altera posição esportiva;
- empate existe no ranking da temporada, não na colocação do campeonato;
- empate do ranking usa padrão `1, 1, 3`;
- não existe desempate adicional no ranking;
- todos os cálculos usam `BigDecimal`.
