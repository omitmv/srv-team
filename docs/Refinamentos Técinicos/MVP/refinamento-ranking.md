# Refinamento técnico — Ranking

Status: regras funcionais consolidadas para o MVP.

## Objetivo

Modelar o ranking de temporada como projeção dinâmica derivada de elegibilidade, inscrições, resultados aprovados, pontuação classificatória e penalidades configuradas na própria temporada.

O ranking não é fonte autoritativa persistida no MVP.

## Elegibilidade para ranking geral

Um atleta integra o ranking geral quando:

1. a temporada está `ATIVO` ou `ENCERRADA`;
2. possui `VinculoProfissionalAtleta` atualmente `ATIVO` com o criador da temporada;
3. possui ao menos uma `Inscricao` `CONFIRMADA` e não cancelada em campeonato associado;
4. sua conta não está cancelada.

Não é necessário possuir resultado aprovado para aparecer no ranking geral.

Temporada `CANCELADA` não possui projeção esportiva ativa enquanto permanecer cancelada.

Encerramento do vínculo atual com o criador remove o atleta da composição corrente do ranking, sem apagar inscrições, resultados ou histórico esportivo já existente.

## Elegibilidade histórica dos resultados

A composição corrente do ranking e a validade histórica de cada resultado usam referências diferentes.

Para participação corrente, exige-se vínculo atualmente `ATIVO` com o criador.

Para um resultado contribuir, o vínculo do atleta com o criador precisa ter estado vigente na data de início do campeonato:

```text
vinculoVigenteNaData(atleta, criador, Campeonato.dtInicio)
= dtInicioVinculo <= Campeonato.dtInicio
  E (
      dtEncerramentoVinculo IS NULL
      OU dtEncerramentoVinculo >= Campeonato.dtInicio
    )
```

Consequências:

- vínculo atualmente encerrado pode comprovar relação válida no passado;
- novo vínculo não torna retroativamente elegível campeonato ocorrido durante intervalo sem vínculo;
- reentrada do atleta por novo vínculo não preenche lacunas históricas;
- `Campeonato.dtInicio` é a referência temporal esportiva; data de lançamento/aprovação do resultado não substitui essa referência.

## Resultados considerados

Um `Resultado` contribui para determinada temporada quando:

```text
Temporada não está CANCELADA
E Campeonato está associado à Temporada
E Resultado está APROVADO
E Inscricao está CONFIRMADA e não CANCELADA
E conta do atleta não está cancelada
E vinculoVigenteNaData(atleta, Temporada.cdCriador, Campeonato.dtInicio)
```

Cada resultado válido é interpretado individualmente e gera contribuição independente para o total do atleta.

`PENDENTE_APROVACAO` e `CANCELADO` nunca produzem pontuação ou penalidade.

### CLASSIFICADO

Pontuação calculada por:

```text
(colocacao, tipoClasse)
```

usando a configuração atual de `TemporadaPontuacao`.

Se não houver regra para aquela colocação/tipo de classe, impacto = `0`.

A temporada pode deliberadamente pontuar somente até determinada colocação. Resultados de posições superiores podem nem ser lançados no sistema, sem que isso gere ausência ou penalidade.

### DESCLASSIFICADO

Usar a regra atual `DESCLASSIFICACAO` da temporada.

Se não houver regra configurada, impacto = `0`.

Cada resultado `DESCLASSIFICADO` aprovado contribui individualmente para o total.

### AUSENTE

Usar a regra atual `AUSENCIA` da temporada.

Se não houver regra configurada, impacto = `0`.

Cada resultado `AUSENTE` aprovado contribui individualmente para o total.

`DESCLASSIFICADO` e `AUSENTE` nunca devem ser convertidos artificialmente em colocação zero ou outra posição fictícia.

Ausência de `Resultado` para posição não lançada também não deve ser interpretada como `AUSENTE`.

## Soma dos impactos

Não existe agregação de penalidade por campeonato, categoria ou tipo de situação no MVP.

```text
para cada Resultado APROVADO válido e historicamente elegível:
    calcular impacto usando a configuração atual da temporada

TOTAL = soma de todos os impactos
```

Múltiplos resultados de ausência ou desclassificação acumulam individualmente, inclusive dentro do mesmo campeonato.

## Regra atual da temporada como fonte autoritativa

A configuração corrente de `TemporadaPontuacao` e `TemporadaPenalidade` é a fonte autoritativa para o ranking.

O MVP não congela no `Resultado` a pontuação/penalidade vigente no momento da aprovação.

Enquanto a temporada estiver `ATIVO`, inclusão, alteração ou remoção de regra de pontuação/penalidade recalcula retroativamente todos os resultados aprovados, válidos e elegíveis da temporada.

Exemplo:

```text
1º COMUM = 10
Resultado R aprovado -> impacto 10

regra alterada para:
1º COMUM = 12

Resultado R permanece inalterado
Ranking recalculado -> impacto 12
```

A mesma lógica vale para penalidades.

Temporada `ENCERRADA` não permite alterar essas regras sem reabertura para `ATIVO`, mas continua utilizando e recalculando com a configuração vigente.

## Ranking geral

Soma todos os impactos válidos do atleta em todas as categorias, classes e campeonatos associados à temporada.

Inclui:

- resultados de classe `COMUM`;
- resultados de classe `OVERALL`;
- cada penalidade por desclassificação;
- cada penalidade por ausência.

## Múltiplas temporadas e campeonato compartilhado

Cada temporada possui ranking independente.

O mesmo campeonato pode estar associado a várias temporadas e o mesmo `Resultado` pode gerar impactos diferentes em cada uma, porque cada temporada possui suas próprias regras de pontuação/penalidade e seu próprio criador/vínculo histórico de elegibilidade.

Não duplicar `Inscricao` ou `Resultado` por temporada.

Compartilhar campeonato não mistura rankings.

## Categoria no ranking — fora do MVP

A categoria permanece parte do `Resultado` e deve continuar disponível para consultas, filtros e evolução futura.

Ranking por categoria não faz parte do MVP.

## Pontuação zero e negativa

Atleta elegível aparece no ranking geral mesmo sem resultados aprovados, inicialmente com zero.

Após aplicação dos resultados, pode permanecer em zero ou ficar negativo.

## Cálculo da posição

O universo esportivo é calculado antes dos filtros de visualização.

```text
1. obter atletas elegíveis da temporada
2. obter resultados APROVADO válidos e historicamente elegíveis
3. interpretar cada resultado com as regras atuais da temporada
4. somar impactos por atleta
5. ordenar total decrescente
6. atribuir posição esportiva
7. aplicar filtros de visibilidade
```

Valores negativos seguem ordenação numérica normal:

```text
5 > 0 > -2 > -10
```

## Empates no ranking da temporada

Empate é permitido no ranking derivado da temporada.

Totais iguais usam ranking de competição:

```text
1, 1, 3, 4...
```

Formalmente:

```text
posicao = 1 + quantidade de atletas com total estritamente maior
```

Isso vale para zero e valores negativos.

Não aplicar desempate esportivo adicional no MVP.

Essa regra não significa empate de colocação no campeonato. `Resultado CLASSIFICADO` possui colocação única por campeonato/categoria/classe, inclusive durante `PENDENTE_APROVACAO` conforme refinamento de `Resultado`.

## Filtros de visibilidade

Filtros são aplicados somente depois da posição real ter sido calculada.

Não renumerar ranking por observador.

## Temporada ENCERRADA e resultado tardio

`ENCERRADA` continua possuindo ranking dinâmico.

Não criar snapshot imutável no MVP.

Resultado pode ser lançado e aprovado depois do encerramento quando o campeonato já estava associado e as demais invariantes forem satisfeitas.

Ao ser aprovado, resultado tardio entra imediatamente na projeção das temporadas `ATIVO` ou `ENCERRADA` para as quais seja elegível.

Não é necessário reabrir a temporada apenas para refletir resultado tardio no ranking.

Exemplo:

```text
Temporada T -> ENCERRADA
Campeonato C -> associado a T
Resultado R -> lançado/aprovado depois do encerramento

se R for elegível:
Ranking(T) é recalculado e passa a incluir R
```

## Temporada CANCELADA e reativação

`CANCELADA` suspende a projeção esportiva sem apagar dados, resultados, pontuação, penalidades ou associações.

Enquanto cancelada, seus resultados não produzem efeito naquela temporada.

Na reativação, a temporada retorna ao estado anterior (`ATIVO` ou `ENCERRADA`) e o ranking deve ser integralmente recalculado com os dados e regras vigentes.

## Alterações que exigem recálculo

Incluem, no mínimo:

- aprovação, reprovação/cancelamento ou correção de resultado;
- lançamento/aprovação de resultado tardio;
- mudança de `situacaoResultado` em correção administrativa;
- alteração de `TemporadaPontuacao`;
- alteração de penalidade por desclassificação;
- alteração de penalidade por ausência;
- associação ou remoção de campeonato;
- cancelamento ou reativação de campeonato;
- confirmação ou cancelamento de inscrição;
- criação, encerramento ou novo ciclo de vínculo com o criador quando alterar composição/elegibilidade;
- cancelamento ou reativação da conta do atleta;
- cancelamento ou reativação da temporada.

Alterações de regra em temporada `ATIVO` possuem efeito retroativo sobre todos os resultados válidos/elegíveis afetados.

## Precisão numérica

Usar `BigDecimal` em todos os cálculos.

Não converter para `double`/`float`.

Pontuação, penalidades e totais devem ser comparados numericamente, inclusive negativos.

Evitar arredondamentos intermediários desnecessários.

## Persistência e performance

Ranking permanece projeção calculada.

Materialização/cache futura é permitida apenas se totalmente reconstruível a partir do domínio e corretamente invalidada por todos os eventos relevantes.

Nenhum cache/materialização pode se tornar fonte autoritativa concorrente com `Resultado`, `TemporadaPontuacao`, `TemporadaPenalidade`, `Inscricao`, associações e vínculos.

## Relatórios

Relatórios de temporada usam a mesma projeção e as mesmas regras de elegibilidade histórica.

Devem distinguir cada contribuição individual:

- pontos de colocação;
- penalidade por desclassificação;
- penalidade por ausência;
- campeonato;
- categoria;
- classe;
- total final.

Relatório de campeonato continua exibindo o desfecho esportivo original; pontuação/penalidade é interpretação específica da temporada.

Relatórios/rankings por categoria ficam fora do MVP.

## Invariantes consolidadas

- ranking pertence à temporada;
- ranking é derivado dinamicamente e não é fonte autoritativa;
- `ATIVO` e `ENCERRADA` possuem projeção esportiva; `CANCELADA` suspende projeção;
- atleta corrente exige vínculo atualmente ativo com o criador;
- resultado histórico exige vínculo vigente com o criador em `Campeonato.dtInicio`;
- novo vínculo não produz retroatividade sobre período sem vínculo;
- cada `Resultado APROVADO` válido/elegível gera contribuição independente;
- `PENDENTE_APROVACAO` e `CANCELADO` não produzem efeito;
- `CLASSIFICADO` usa regra atual por colocação/tipo de classe;
- `DESCLASSIFICADO` e `AUSENTE` usam penalidades atuais da temporada;
- ausência de regra produz impacto zero;
- ausência de resultado não equivale a `AUSENTE`;
- múltiplas penalidades acumulam individualmente;
- não existe agregação de penalidades no MVP;
- regra atual de pontuação/penalidade é autoritativa para o ranking;
- alteração de regra em `ATIVO` recalcula retroativamente resultados válidos/elegíveis;
- `ENCERRADA` aceita resultado tardio e continua recalculando ranking;
- reabertura de `ENCERRADA` não é necessária apenas por resultado tardio;
- `CANCELADA` interrompe efeito esportivo e reativação recalcula integralmente;
- mesmo resultado pode impactar temporadas diferentes de formas diferentes;
- campeonato compartilhado não mistura rankings;
- total pode ser negativo;
- categoria permanece mapeada, ranking por categoria fora do MVP;
- visibilidade não altera posição esportiva;
- empate no ranking é permitido e usa `1, 1, 3`;
- empate de ranking não implica empate de colocação no campeonato;
- todos os cálculos usam `BigDecimal`.
