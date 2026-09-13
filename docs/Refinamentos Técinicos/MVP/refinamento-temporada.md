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

Estados do MVP:

- `ATIVO`
- `ENCERRADA`
- `CANCELADA`

Não existe `RASCUNHO`; a temporada é criada diretamente como `ATIVO`.

### ATIVO

Estado operacional normal. Pode produzir efeitos esportivos, receber operações administrativas permitidas e participar da projeção de ranking.

### ENCERRADA

Representa ciclo esportivo concluído, mas não significa congelamento dos fatos esportivos históricos.

Uma temporada `ENCERRADA`:

- continua disponível para consulta;
- continua exibindo e calculando seu ranking;
- aceita lançamento tardio de resultados referentes a campeonatos já vinculados à temporada;
- aceita aprovação/reprovação e correções desses resultados conforme o fluxo normal de `Resultado`;
- recalcula automaticamente o ranking quando um resultado tardio passa a produzir ou deixa de produzir efeito;
- preserva dados e histórico.

O encerramento congela a configuração estrutural da temporada, não os fatos esportivos dos campeonatos que já pertencem a ela.

Enquanto `ENCERRADA`, não é permitido:

- alterar tabela de pontuação;
- alterar penalidades;
- vincular ou desvincular campeonatos;
- realizar outras alterações estruturais da temporada que modifiquem a interpretação esportiva já estabelecida.

Quando uma alteração estrutural for necessária, a temporada deve ser reaberta para `ATIVO`.

Uma temporada `ENCERRADA` pode voltar para `ATIVO`. Essa reabertura exige justificativa obrigatória, responsável e data/hora e deve ser registrada no histórico auditável de transições.

### CANCELADA

Cancelamento é lógico e preserva campeonatos, inscrições, resultados, vínculos profissionais, pontuação, penalidades e histórico. Enquanto `CANCELADA`, a temporada não produz efeito esportivo.

A temporada cancelada pode ser reativada. A reativação deve restaurar o estado que existia imediatamente antes do cancelamento (`ATIVO` ou `ENCERRADA`).

### Justificativa e histórico de status

As seguintes operações exigem sempre justificativa obrigatória, responsável e data/hora:

- cancelamento;
- reativação de cancelamento;
- encerramento;
- reabertura de `ENCERRADA` para `ATIVO`.

Toda transição deve ser auditável e preservar o histórico anterior. Uma nova transição nunca apaga a justificativa, responsável ou data/hora das anteriores.

Criador e profissionais com `ADMINISTRACAO` podem realizar as transições permitidas. `CONSULTA` não altera status.

Fluxos conceituais:

```text
ATIVO -> ENCERRADA
ENCERRADA -> ATIVO

ATIVO -> CANCELADA -> ATIVO
ENCERRADA -> CANCELADA -> ENCERRADA
```

## Acesso de profissionais à temporada

O criador possui autoridade administrativa por definição. Outros profissionais acessam por `TemporadaProfissional`, com `ADMINISTRACAO` ou `CONSULTA`.

`ADMINISTRACAO` permite auxiliar o criador nas configurações, vínculos de campeonatos, gestão de acessos e lançamentos. O administrador pode operar os atletas pertencentes à temporada mesmo sem vínculo profissional-atleta próprio, estritamente dentro do contexto da temporada.

`CONSULTA` é somente leitura. Dados individualizados ficam limitados aos atletas da temporada com os quais o consultor também possua `VinculoProfissionalAtleta` ativo.

O criador não pode ser removido ou rebaixado; `cdCriador` é imutável e não existe transferência de titularidade no MVP.

## Composição de atletas da temporada

A composição é determinada exclusivamente pelos vínculos do criador. `VinculoProfissionalAtleta` é a fonte autoritativa da relação atleta/profissional, inclusive historicamente; não existe `TemporadaAtleta` no MVP.

Vínculo do atleta com administrador ou consultor não adiciona o atleta à temporada.

### Vigência temporal do vínculo

Para operações atuais, o vínculo precisa estar `ATIVO`. Para consultas históricas:

```text
vinculoVigenteNaData(atleta, profissional, dataReferencia)
= dtInicio <= dataReferencia
  E (dtEncerramento IS NULL OU dtEncerramento >= dataReferencia)
```

Um vínculo atualmente `ENCERRADO` pode comprovar relação válida no passado. Retomada cria novo `VinculoProfissionalAtleta` e não preenche retroativamente períodos sem vínculo.

## Múltiplas temporadas simultâneas

Um profissional pode possuir/criar e administrar mais de uma temporada `ATIVO` simultaneamente.

Não existe restrição de unicidade do tipo "uma temporada ativa por profissional" nem impedimento baseado em sobreposição de datas.

Cada temporada possui configuração, pontuação, penalidades, associações e ranking independentes.

## Campeonatos e temporadas

A relação é N:N via `TemporadaCampeonato`.

O cenário normal pode utilizar campeonatos diferentes entre temporadas simultâneas, porém isso não é restrição de negócio. O mesmo campeonato pode ser associado a duas ou mais temporadas, inclusive temporadas `ATIVO` simultaneamente e administradas/criadas pelo mesmo profissional.

`Campeonato` continua sendo entidade compartilhada. `Inscricao` e `Resultado` pertencem ao campeonato e não são duplicados por temporada. Cada temporada interpreta os mesmos resultados elegíveis segundo suas próprias regras de pontuação e penalidade.

Um mesmo resultado pode, portanto, produzir impactos diferentes em temporadas diferentes.

Invariantes da associação:

- não duplicar o mesmo campeonato dentro da mesma temporada;
- campeonato pode estar em várias temporadas;
- inclusive temporadas simultaneamente `ATIVO`;
- inclusive temporadas do mesmo criador/profissional;
- datas nominais da temporada não limitam automaticamente a associação;
- apenas `ADMINISTRACAO` pode vincular/desvincular enquanto a temporada permitir alteração estrutural;
- temporada `ENCERRADA` não permite alterar suas associações sem reabertura;
- desvincular não cancela campeonato, inscrição ou resultado;
- associação/desassociação recalcula somente as projeções das temporadas afetadas;
- cancelamento preserva associações e histórico, suspendendo apenas seu efeito esportivo.

## Pontuação e penalidades

A tabela de colocação pertence à temporada (`TemporadaPontuacao`) e usa a chave lógica `(cdTemporada, posicao, tipoClasse)`, com `tipoClasse` `COMUM` ou `OVERALL`.

A temporada não precisa pontuar todas as colocações. Colocação informada sem regra correspondente tem impacto zero; colocação não informada não equivale a `AUSENTE` e não gera penalidade.

`TemporadaPenalidade` trata separadamente `DESCLASSIFICACAO` e `AUSENCIA`, com unicidade `(cdTemporada, tipoPenalidade)`. Regra ausente significa impacto zero.

Cada `Resultado APROVADO` produz contribuição independente. Não existe agregação de penalidades por campeonato, categoria ou situação.

Pontuação e penalidade usam `BigDecimal(10,3)`. Pontuação pode ser positiva, zero ou negativa; penalidade pode ser zero ou negativa; ranking final também pode ser negativo.

## Interpretação do Resultado

```text
CLASSIFICADO -> colocacao + tipoClasse -> TemporadaPontuacao
DESCLASSIFICADO -> TemporadaPenalidade.DESCLASSIFICACAO
AUSENTE -> TemporadaPenalidade.AUSENCIA
```

Pontos calculados não são persistidos no `Resultado` como verdade autoritativa.

## Elegibilidade

### Participação no ranking

Atleta pode compor o ranking de uma temporada esportivamente utilizável quando:

1. temporada está `ATIVO` ou `ENCERRADA`;
2. possui `VinculoProfissionalAtleta` atualmente `ATIVO` com o criador para composição corrente;
3. possui inscrição confirmada e não cancelada em ao menos um campeonato associado;
4. conta do atleta não está cancelada.

Encerramento da temporada não remove atletas nem congela seu ranking. Encerramento do vínculo atual com o criador continua removendo o atleta da composição corrente sem apagar histórico.

### Elegibilidade histórica do Resultado

A referência temporal é `Campeonato.dtInicio`, não a data de lançamento/aprovação.

Um resultado contribui quando:

```text
Temporada não está CANCELADA
E Campeonato associado à Temporada
E Resultado APROVADO
E Inscricao CONFIRMADA e não CANCELADA
E conta do atleta não cancelada
E vinculoVigenteNaData(atleta, Temporada.cdCriador, Campeonato.dtInicio)
```

Portanto, `ATIVO` e `ENCERRADA` permitem efeito esportivo. Apenas `CANCELADA` suspende a contribuição da temporada.

A data em que o resultado foi lançado ou aprovado não precisa estar dentro do período nominal da temporada nem antes do encerramento. O que importa para a elegibilidade histórica é o campeonato já pertencer à temporada e as demais invariantes serem satisfeitas.

Novo vínculo não torna retroativamente elegíveis campeonatos ocorridos durante período sem vínculo.

Quando um campeonato está associado a mais de uma temporada, elegibilidade e impacto são avaliados independentemente para cada temporada.

## Resultado tardio

`Resultado` representa fato esportivo do campeonato e pode ser registrado posteriormente ao encerramento da temporada.

Exemplo:

```text
Temporada T -> ENCERRADA
Campeonato C -> já associado à T
Resultado do Atleta A -> ainda não lançado

posteriormente:
Resultado lançado -> PENDENTE_APROVACAO
Resultado aprovado -> APROVADO
                     -> passa a contribuir para T
                     -> ranking de T é recalculado
```

Não é necessário reabrir a temporada apenas para registrar, aprovar, reprovar ou corrigir resultado tardio de campeonato já associado.

A reabertura é necessária quando a operação pretendida altera a estrutura ou configuração da própria temporada.

## Ranking

Ranking é projeção dinâmica, não entidade autoritativa. O total é a soma algébrica dos impactos de todos os resultados aprovados, válidos e historicamente elegíveis.

Cada temporada possui sua própria projeção. Compartilhar campeonato entre temporadas não compartilha nem mistura rankings.

O ranking de temporada `ENCERRADA` permanece calculável e pode mudar em razão de lançamento, aprovação, reprovação, correção ou cancelamento válido de resultado histórico.

Cancelamento da temporada suspende sua projeção esportiva sem apagar os dados. Reativação exige recálculo com dados e regras vigentes.

Ranking/consolidação por categoria permanece fora do MVP; `Categoria` continua mapeada no domínio por fazer parte de `Resultado`.

## Invariantes consolidadas

- estados são exclusivamente `ATIVO`, `ENCERRADA` e `CANCELADA`;
- temporada nasce `ATIVO`;
- `ENCERRADA` pode ser reaberta para `ATIVO` com justificativa e auditoria;
- encerramento congela configuração estrutural, não fatos esportivos históricos;
- temporada `ENCERRADA` aceita lançamento e processamento de resultados tardios de campeonatos já associados;
- resultado tardio aprovado recalcula e pode alterar ranking de temporada `ENCERRADA`;
- não é necessário reabrir temporada apenas para lançar/processar resultado tardio;
- alteração estrutural de temporada `ENCERRADA` exige reabertura para `ATIVO`;
- cancelamento e reativação exigem justificativa e auditoria;
- temporada cancelada retorna, na reativação, ao estado anterior ao cancelamento;
- somente `CANCELADA` suspende efeito esportivo da temporada;
- `cdCriador` é permanente e imutável;
- não existe transferência de titularidade;
- um profissional pode possuir/administrar múltiplas temporadas `ATIVO` simultaneamente;
- sobreposição temporal entre temporadas do mesmo profissional é permitida;
- temporadas simultâneas podem possuir campeonatos distintos, parcialmente coincidentes ou iguais;
- mesmo campeonato pode pertencer a múltiplas temporadas, inclusive ativas e do mesmo profissional;
- `Inscricao` e `Resultado` não são duplicados por temporada;
- cada temporada interpreta independentemente resultados de campeonato compartilhado;
- somente atletas vinculados ao criador compõem a temporada;
- vínculo atual controla composição corrente e intervalo histórico controla elegibilidade do resultado na data do campeonato;
- novo vínculo não produz retroatividade sobre período sem vínculo;
- `ADMINISTRACAO` pode operar atletas da temporada contextualmente sem criar vínculo profissional-atleta;
- `CONSULTA` somente visualiza dados individualizados dos próprios atletas vinculados;
- tabela de pontuação e penalidades pertencem à temporada;
- cada resultado aprovado produz contribuição independente;
- ausência de regra de pontuação/penalidade produz impacto zero;
- `CLASSIFICADO` usa colocação/tipo de classe; `DESCLASSIFICADO` e `AUSENTE` usam penalidades específicas;
- ranking pode ser negativo;
- categoria permanece no domínio, mas ranking por categoria está fora do MVP.
