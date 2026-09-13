# Refinamento técnico — Temporada

Status: decisões de negócio consolidadas para o MVP; refinamento técnico em andamento.

## Objetivo

Modelar `Temporada` como contexto no qual campeonatos compartilhados são selecionados, resultados aprovados são convertidos em pontuação ou penalidade e rankings são calculados segundo regras próprias da temporada.

## Entidade principal

Campos conceituais:

- `cdTemporada`
- `dsNome`
- `cdCriador`
- `dtInicio`
- `dtEncerramento`
- `status`
- `dtCadastro`
- auditoria

`cdCriador` identifica permanentemente o profissional que criou a temporada. No MVP não existe transferência de titularidade.

## Estado

Estados do MVP: `ATIVO`, `ENCERRADA`, `CANCELADA`.

Não existe `RASCUNHO`; a temporada nasce `ATIVO`.

### ATIVO

Estado operacional normal. Pode produzir efeitos esportivos, receber operações administrativas permitidas e participar da projeção de ranking.

### ENCERRADA

Representa ciclo esportivo concluído, mas não congela fatos esportivos históricos.

Continua permitindo consulta, cálculo de ranking e lançamento/aprovação/reprovação/correção de resultados tardios referentes a campeonatos já associados. Alterações desses resultados recalculam automaticamente o ranking.

O encerramento congela a configuração estrutural da temporada. Enquanto `ENCERRADA`, não se altera tabela de pontuação, penalidades ou composição de campeonatos. Para alteração estrutural, deve ser reaberta para `ATIVO`.

`ENCERRADA -> ATIVO` exige justificativa, responsável, data/hora e histórico auditável.

### CANCELADA

Cancelamento é lógico, preserva todos os dados e suspende o efeito esportivo. A reativação restaura o estado imediatamente anterior (`ATIVO` ou `ENCERRADA`).

Cancelamento, reativação, encerramento e reabertura exigem justificativa obrigatória, responsável, data/hora e preservação do histórico de transições.

Criador e `ADMINISTRACAO` podem realizar as transições permitidas. `CONSULTA` não altera status.

## Acesso de profissionais

O criador possui autoridade administrativa por definição. Outros profissionais acessam por `TemporadaProfissional`, com `ADMINISTRACAO` ou `CONSULTA`.

`ADMINISTRACAO` pode auxiliar nas configurações, associações, gestão de acessos e lançamentos e operar atletas pertencentes à temporada sem vínculo profissional-atleta próprio, estritamente no contexto da temporada.

`CONSULTA` é leitura; dados individualizados limitam-se aos atletas da temporada com os quais o consultor também possua vínculo ativo.

`cdCriador` é imutável; criador não pode ser removido/rebaixado e não existe transferência de titularidade no MVP.

## Composição e vínculo temporal

A composição é determinada exclusivamente pelos vínculos do criador. `VinculoProfissionalAtleta` é a fonte autoritativa; não existe `TemporadaAtleta`.

Para operações atuais, vínculo precisa estar `ATIVO`. Historicamente:

```text
vinculoVigenteNaData(atleta, profissional, dataReferencia)
= dtInicio <= dataReferencia
  E (dtEncerramento IS NULL OU dtEncerramento >= dataReferencia)
```

Retomada após encerramento cria novo vínculo e não preenche retroativamente intervalos sem vínculo.

## Múltiplas temporadas simultâneas

Um profissional pode possuir/criar/administrar várias temporadas `ATIVO` simultaneamente. Não existe unicidade de temporada ativa por profissional nem impedimento por sobreposição de datas.

Cada temporada possui configuração, pontuação, penalidades, associações e ranking independentes.

## Campeonatos e temporadas

Relação N:N via `TemporadaCampeonato`.

O mesmo campeonato pode estar em várias temporadas, inclusive simultaneamente ativas e do mesmo profissional. `Campeonato`, `Inscricao` e `Resultado` não são duplicados por temporada; cada temporada interpreta os resultados elegíveis conforme suas próprias regras.

Invariantes:

- não duplicar campeonato dentro da mesma temporada;
- datas nominais da temporada não limitam automaticamente associação;
- somente `ADMINISTRACAO` vincula/desvincula enquanto a temporada permitir alteração estrutural;
- `ENCERRADA` exige reabertura antes de alterar associações;
- desvincular não cancela campeonato, inscrição ou resultado;
- associação/desassociação recalcula projeções afetadas;
- cancelamento preserva associações e suspende efeito esportivo.

## Pontuação e penalidades

`TemporadaPontuacao` pertence à temporada, chave `(cdTemporada, posicao, tipoClasse)`, com `COMUM` ou `OVERALL`.

A temporada não precisa pontuar todas as colocações. Colocação informada sem regra tem impacto zero; colocação não informada não equivale a `AUSENTE`.

`TemporadaPenalidade` trata `DESCLASSIFICACAO` e `AUSENCIA`, unicidade `(cdTemporada, tipoPenalidade)`. Regra ausente = zero.

Cada `Resultado APROVADO` produz contribuição independente. Não existe agregação por campeonato/categoria/situação.

Pontuação e penalidade usam `BigDecimal(10,3)`; ranking pode ser negativo.

## Alteração das regras de pontuação e penalidade

Enquanto a temporada estiver `ATIVO`, profissionais com `ADMINISTRACAO` podem alterar as regras de `TemporadaPontuacao` e `TemporadaPenalidade`, inclusive quando já existirem resultados aprovados que utilizem essas regras.

A configuração atual da temporada é a verdade autoritativa para cálculo do ranking. O MVP não versiona a regra de pontuação/penalidade aplicada individualmente a cada resultado.

Consequentemente, qualquer inclusão, alteração ou remoção de regra de pontuação ou penalidade deve provocar recálculo retroativo da projeção da temporada sobre todos os resultados aprovados que continuem válidos e elegíveis.

Exemplo:

```text
Regra inicial:
1º COMUM = 10

Resultado R = 1º COMUM / APROVADO
Ranking = 10

Regra alterada:
1º COMUM = 12

Resultado R permanece o mesmo
Ranking recalculado = 12
```

O resultado esportivo não é modificado; muda apenas sua interpretação pela temporada.

A mesma regra vale para penalidades. Se `AUSENCIA` passar de `-2` para `-3`, todas as ausências aprovadas, válidas e elegíveis daquela temporada passam a contribuir com `-3` no recálculo.

Não persistir no `Resultado` uma cópia da pontuação ou penalidade aplicada como verdade histórica autoritativa. O histórico de alterações administrativas da configuração deve ser auditável, mas o ranking corrente sempre utiliza a configuração atual da temporada.

Temporada `ENCERRADA` não permite alteração dessas regras sem reabertura para `ATIVO`.

## Interpretação do Resultado

```text
CLASSIFICADO -> colocacao + tipoClasse -> TemporadaPontuacao atual
DESCLASSIFICADO -> TemporadaPenalidade.DESCLASSIFICACAO atual
AUSENTE -> TemporadaPenalidade.AUSENCIA atual
```

## Elegibilidade

Atleta pode compor ranking quando temporada está `ATIVO` ou `ENCERRADA`, possui vínculo atual ativo com criador, inscrição confirmada/não cancelada em campeonato associado e conta não cancelada.

Para contribuição histórica:

```text
Temporada não CANCELADA
E Campeonato associado
E Resultado APROVADO
E Inscricao CONFIRMADA e não CANCELADA
E conta do atleta não cancelada
E vinculoVigenteNaData(atleta, Temporada.cdCriador, Campeonato.dtInicio)
```

A data de lançamento/aprovação pode ser posterior ao encerramento. Novo vínculo não torna retroativamente elegíveis campeonatos ocorridos durante intervalo sem vínculo.

## Resultado tardio

Resultado é fato esportivo e pode ser registrado posteriormente ao encerramento. Resultado tardio aprovado de campeonato já associado passa a contribuir e recalcula ranking sem necessidade de reabrir a temporada.

Reabertura é necessária somente para alterar estrutura/configuração da própria temporada.

## Ranking

Ranking é projeção dinâmica, não entidade autoritativa.

```text
TOTAL = soma(impacto atual de cada Resultado APROVADO válido e elegível)
```

O ranking de `ENCERRADA` permanece calculável e pode mudar por resultado tardio, correção/cancelamento válido de resultado ou outros fatos esportivos permitidos.

Alterações de pontuação/penalidade em temporada `ATIVO` recalculam retroativamente todo o ranking afetado usando as regras atuais.

Cada temporada possui projeção própria; compartilhar campeonato não mistura rankings.

`CANCELADA` suspende projeção; reativação recalcula com dados e regras vigentes.

Ranking por categoria permanece fora do MVP.

## Invariantes consolidadas

- estados exclusivamente `ATIVO`, `ENCERRADA`, `CANCELADA`;
- temporada nasce `ATIVO`;
- `ENCERRADA` pode ser reaberta com justificativa/auditoria;
- encerramento congela configuração estrutural, não fatos esportivos históricos;
- `ENCERRADA` aceita resultados tardios e seu ranking continua dinâmico;
- somente `CANCELADA` suspende efeito esportivo;
- cancelamento/reativação/encerramento/reabertura preservam histórico auditável;
- `cdCriador` permanente e imutável; sem transferência;
- profissional pode ter múltiplas temporadas ativas simultâneas e sobrepostas;
- mesmo campeonato pode pertencer a múltiplas temporadas;
- `Inscricao` e `Resultado` não são duplicados por temporada;
- cada temporada interpreta independentemente resultados compartilhados;
- somente atletas vinculados ao criador compõem a temporada;
- vínculo atual controla composição e vínculo histórico na `Campeonato.dtInicio` controla elegibilidade histórica;
- `ADMINISTRACAO` pode operar atletas contextualmente sem vínculo próprio;
- `CONSULTA` somente vê individualmente seus atletas vinculados;
- tabela de pontuação e penalidades pertencem à temporada;
- configuração atual de pontuação/penalidade é autoritativa para o ranking;
- regras de pontuação/penalidade não são versionadas por resultado no MVP;
- alteração de pontuação/penalidade em `ATIVO` recalcula retroativamente todos os resultados válidos/elegíveis afetados;
- resultado esportivo permanece inalterado quando sua pontuação muda por configuração da temporada;
- histórico administrativo das alterações de configuração deve ser auditável;
- `ENCERRADA` exige reabertura antes de alterar pontuação, penalidade ou associações;
- cada resultado aprovado produz contribuição independente;
- ausência de regra produz impacto zero;
- ranking pode ser negativo;
- categoria permanece no domínio, ranking por categoria fora do MVP.
