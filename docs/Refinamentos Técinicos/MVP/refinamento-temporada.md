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
- `dtCancelamento`
- `cdResponsavelCancelamento`
- `motivoCancelamento`
- `dtReativacao`
- `cdResponsavelReativacao`
- `motivoReativacao`
- auditoria

`cdCriador` identifica de forma permanente o profissional que criou a temporada. No MVP não existe transferência de titularidade da temporada.

## Estado

Estados do MVP:

- `ATIVO`
- `ENCERRADA`
- `CANCELADA`

Não existe estado `RASCUNHO`. Uma temporada é criada diretamente como `ATIVO`.

### ATIVO

É o estado operacional normal da temporada. Enquanto `ATIVO`, a temporada pode produzir efeitos esportivos, receber as operações administrativas permitidas e participar da projeção de ranking conforme as demais regras de elegibilidade.

### ENCERRADA

Representa uma temporada cujo ciclo esportivo foi concluído. Seus dados e histórico permanecem preservados. As regras exatas sobre quais alterações administrativas continuam permitidas após o encerramento serão refinadas separadamente.

### CANCELADA

`CANCELADA` não é terminal. O cancelamento é lógico e preserva campeonatos, inscrições, resultados, vínculos profissionais, pontuação, penalidades e histórico.

Enquanto `CANCELADA`, a temporada não produz efeito esportivo.

Cancelamento e reativação exigem sempre justificativa obrigatória, responsável e data/hora da transição. Toda transição deve ser auditável e preservar o histórico das transições anteriores; a reativação não apaga os dados do cancelamento anterior.

Criador e administradores com `ADMINISTRACAO` podem cancelar ou reativar a temporada, respeitando as demais regras do ciclo de vida. Permissão `CONSULTA` não permite alteração de status.

Como não existe `RASCUNHO`, a reativação de uma temporada cancelada deverá retornar ao estado operacional aplicável (`ATIVO` ou `ENCERRADA`) conforme a situação anterior ao cancelamento e as regras de ciclo de vida consolidadas.

## Acesso de profissionais à temporada

O criador possui autoridade administrativa por definição. Outros profissionais acessam a temporada por `TemporadaProfissional`, com permissão `ADMINISTRACAO` ou `CONSULTA`.

`ADMINISTRACAO` permite auxiliar o criador nas configurações, vínculos de campeonatos, gestão de acessos e lançamentos da temporada. O administrador pode operar os atletas pertencentes à temporada mesmo sem vínculo profissional-atleta próprio, estritamente dentro do contexto da temporada.

`CONSULTA` é somente leitura. Dados individualizados de atletas ficam limitados aos atletas da temporada com os quais o consultor também possua `VinculoProfissionalAtleta` ativo.

O criador não pode ser removido ou rebaixado por gerenciamento de acessos; `cdCriador` é imutável no MVP e não existe transferência de titularidade.

## Composição de atletas da temporada

A composição é determinada exclusivamente pelos vínculos do criador. `VinculoProfissionalAtleta` é a fonte autoritativa da relação atleta/profissional, inclusive historicamente; não existe `TemporadaAtleta` no MVP.

Vínculo do atleta com administrador ou consultor não adiciona o atleta à temporada.

### Vigência temporal do vínculo

Para operações atuais, o vínculo precisa estar `ATIVO`. Para consultas históricas, utiliza-se o intervalo de vigência:

```text
vinculoVigenteNaData(atleta, profissional, dataReferencia)
= dtInicio <= dataReferencia
  E (dtEncerramento IS NULL OU dtEncerramento >= dataReferencia)
```

Um vínculo atualmente `ENCERRADO` pode comprovar relação válida no passado. Retomada da relação cria novo `VinculoProfissionalAtleta` e não preenche retroativamente períodos sem vínculo.

## Campeonatos

A relação é N:N via `TemporadaCampeonato`.

- não duplicar associação temporada/campeonato;
- campeonato pode estar em várias temporadas;
- datas nominais da temporada não limitam automaticamente a associação;
- apenas `ADMINISTRACAO` pode vincular/desvincular;
- desvincular não cancela campeonato, inscrição ou resultado;
- associação/desassociação recalcula a projeção da temporada;
- cancelamento preserva composição e histórico, suspendendo apenas o efeito esportivo.

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

### Participação atual

Atleta pode compor o ranking corrente quando:

1. temporada está `ATIVO`;
2. possui `VinculoProfissionalAtleta` atualmente `ATIVO` com o criador;
3. possui inscrição confirmada e não cancelada em ao menos um campeonato associado;
4. conta do atleta não está cancelada.

Encerramento do vínculo com o criador remove o atleta da composição corrente sem apagar inscrições, resultados ou histórico. Novo vínculo ativo permite retorno, respeitadas as demais regras.

### Elegibilidade histórica do Resultado

A referência temporal é `Campeonato.dtInicio`, não a data de lançamento/aprovação.

Um resultado contribui quando:

```text
Temporada está ATIVO
E Campeonato associado à Temporada
E Resultado APROVADO
E Inscricao CONFIRMADA e não CANCELADA
E conta do atleta não cancelada
E vinculoVigenteNaData(atleta, Temporada.cdCriador, Campeonato.dtInicio)
```

Novo vínculo não torna retroativamente elegíveis campeonatos ocorridos durante período sem vínculo.

## Ranking

Ranking é projeção dinâmica, não entidade autoritativa. O total é a soma algébrica dos impactos de todos os resultados aprovados, válidos e historicamente elegíveis.

Cancelamento suspende a projeção esportiva sem apagar seus dados. Reativação exige recálculo com os dados e regras vigentes.

Ranking/consolidação por categoria permanece fora do MVP; `Categoria` continua mapeada no domínio por fazer parte de `Resultado`.

## Invariantes consolidadas

- estados da temporada no MVP são exclusivamente `ATIVO`, `ENCERRADA` e `CANCELADA`;
- temporada é criada diretamente como `ATIVO`;
- não existe `RASCUNHO`;
- `cdCriador` é permanente e imutável;
- não existe transferência de titularidade;
- cancelamento é lógico e exige justificativa, responsável e data/hora;
- reativação exige justificativa, responsável e data/hora;
- histórico de cancelamentos e reativações é preservado;
- temporada `CANCELADA` não produz efeito esportivo;
- cancelamento/reativação não altera automaticamente resultados, inscrições, campeonatos ou vínculos;
- somente atletas vinculados ao criador compõem a temporada;
- vínculo atual controla composição corrente e intervalo histórico controla elegibilidade do resultado na data do campeonato;
- novo vínculo não produz retroatividade sobre período sem vínculo;
- `ADMINISTRACAO` pode operar atletas da temporada contextualmente sem criar vínculo profissional-atleta;
- `CONSULTA` somente visualiza dados individualizados dos próprios atletas vinculados;
- campeonato é compartilhável entre temporadas;
- tabela de pontuação e penalidades pertencem à temporada;
- cada resultado aprovado produz contribuição independente;
- ausência de regra de pontuação/penalidade produz impacto zero;
- `CLASSIFICADO` usa colocação/tipo de classe; `DESCLASSIFICADO` e `AUSENTE` usam penalidades específicas;
- ranking pode ser negativo;
- categoria permanece no domínio, mas ranking por categoria está fora do MVP.
