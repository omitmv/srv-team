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

Representa ciclo esportivo concluído, mas não congela fatos esportivos históricos nem a gestão de acesso à temporada.

Continua permitindo:

- consulta;
- cálculo de ranking;
- solicitação, aprovação/reprovação, cadastro direto e cancelamento permitido de `Inscricao` tardia referente a campeonato já associado;
- lançamento/aprovação/reprovação/correção de `Resultado` tardio referente a campeonato já associado;
- recálculo automático do ranking quando esses fatos esportivos mudam;
- inclusão e remoção de profissionais em `TemporadaProfissional`;
- promoção ou rebaixamento entre `ADMINISTRACAO` e `CONSULTA`, respeitadas as regras de proteção do criador.

O encerramento congela a configuração estrutural esportiva da temporada. Enquanto `ENCERRADA`, não se altera tabela de pontuação, penalidades ou composição de campeonatos. Para alteração estrutural esportiva, deve ser reaberta para `ATIVO`.

A gestão de profissionais não exige reabertura, pois é controle de acesso e não altera pontuação, elegibilidade esportiva nem ranking.

`ENCERRADA -> ATIVO` exige justificativa, responsável, data/hora e histórico auditável.

### CANCELADA

Cancelamento é lógico, preserva todos os dados e suspende o efeito esportivo. A reativação restaura o estado imediatamente anterior (`ATIVO` ou `ENCERRADA`).

Cancelamento, reativação, encerramento e reabertura exigem justificativa obrigatória, responsável, data/hora e preservação do histórico de transições.

Criador e `ADMINISTRACAO` podem realizar as transições permitidas. `CONSULTA` não altera status.

## Acesso de profissionais

O criador possui autoridade administrativa por definição. Outros profissionais acessam por `TemporadaProfissional`, com `ADMINISTRACAO` ou `CONSULTA`.

`ADMINISTRACAO` pode auxiliar nas configurações, associações, gestão de acessos e lançamentos e operar atletas pertencentes à temporada sem vínculo profissional-atleta próprio, estritamente no contexto da temporada.

`CONSULTA` é leitura; dados individualizados limitam-se aos atletas da temporada com os quais o consultor também possua vínculo ativo.

### Gestão de acesso em temporada encerrada

A gestão de `TemporadaProfissional` continua permitida quando a temporada está `ENCERRADA`.

Profissional com `ADMINISTRACAO` pode, inclusive após o encerramento:

- incluir novo profissional;
- remover logicamente acesso existente;
- promover `CONSULTA -> ADMINISTRACAO`;
- rebaixar `ADMINISTRACAO -> CONSULTA`;
- manter histórico auditável de inclusão, remoção e alteração de permissão.

Essas operações não exigem `ENCERRADA -> ATIVO` porque não alteram configuração esportiva nem recalculam ranking.

Regras permanentes:

- `cdCriador` é imutável;
- criador não pode ser removido;
- criador não pode ser rebaixado para `CONSULTA`;
- não existe transferência de titularidade no MVP;
- remover ou alterar acesso de profissional não apaga nem modifica atos históricos praticados por ele;
- perda de `ADMINISTRACAO` impede novas operações administrativas a partir da alteração, mas preserva integralmente o histórico anterior.

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

Qualquer inclusão, alteração ou remoção de regra de pontuação ou penalidade provoca recálculo retroativo da projeção sobre todos os resultados aprovados que continuem válidos e elegíveis.

O resultado esportivo não é modificado; muda apenas sua interpretação pela temporada.

O histórico administrativo das alterações de configuração deve ser auditável, mas o ranking corrente sempre utiliza a configuração atual.

Temporada `ENCERRADA` não permite alterar essas regras sem reabertura para `ATIVO`.

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

A data de criação/confirmação da inscrição e a data de lançamento/aprovação do resultado podem ser posteriores ao encerramento. Novo vínculo não torna retroativamente elegíveis campeonatos ocorridos durante intervalo sem vínculo.

## Inscrição e resultado tardios

`Inscricao` e `Resultado` são fatos do campeonato e podem ser registrados ou concluídos posteriormente ao encerramento quando o campeonato já estava associado à temporada.

Sem reabrir uma temporada `ENCERRADA`, permanecem permitidos, conforme as regras específicas de cada agregado:

- solicitação de inscrição pelo atleta;
- aprovação ou reprovação da inscrição;
- cadastro direto por profissional autorizado;
- cancelamento de inscrição quando a regra permitir atuação direta;
- lançamento e edição de resultado pendente;
- aprovação/reprovação de resultado;
- correção/cancelamento administrativo de resultado conforme autorização própria.

Confirmação tardia de inscrição ou alteração válida do seu ciclo pode mudar a elegibilidade e provocar recálculo da projeção das temporadas `ATIVO` ou `ENCERRADA` afetadas.

Resultado tardio aprovado passa a contribuir e recalcula ranking sem necessidade de reabrir a temporada.

Reabertura é necessária somente para alterar estrutura/configuração esportiva da própria temporada.

Temporada `CANCELADA` não serve como fundamento ordinário para operações de inscrição ou resultado enquanto permanecer cancelada.

## Ranking

Ranking é projeção dinâmica, não entidade autoritativa.

```text
TOTAL = soma(impacto atual de cada Resultado APROVADO válido e elegível)
```

O ranking de `ENCERRADA` permanece calculável e pode mudar por inscrição tardia, resultado tardio, correção/cancelamento válido de inscrição ou resultado, ou outros fatos esportivos permitidos.

Alterações de pontuação/penalidade em temporada `ATIVO` recalculam retroativamente todo o ranking afetado usando as regras atuais.

Cada temporada possui projeção própria; compartilhar campeonato não mistura rankings.

`CANCELADA` suspende projeção; reativação recalcula com dados e regras vigentes.

Ranking por categoria permanece fora do MVP.

## Invariantes consolidadas

- estados exclusivamente `ATIVO`, `ENCERRADA`, `CANCELADA`;
- temporada nasce `ATIVO`;
- `ENCERRADA` pode ser reaberta com justificativa/auditoria;
- encerramento congela configuração estrutural esportiva, não fatos esportivos históricos nem gestão de acesso;
- `ENCERRADA` aceita inscrição e resultado tardios e seu ranking continua dinâmico;
- gestão de `TemporadaProfissional` continua permitida em `ENCERRADA` sem reabertura;
- inclusão, remoção, promoção e rebaixamento de profissionais não alteram ranking;
- atos históricos de profissional removido/rebaixado permanecem preservados;
- somente `CANCELADA` suspende efeito esportivo;
- `CANCELADA` não fundamenta operações ordinárias de inscrição/resultado enquanto cancelada;
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
