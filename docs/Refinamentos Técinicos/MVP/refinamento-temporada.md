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
- auditoria

`cdCriador` identifica de forma permanente o profissional que criou a temporada. No MVP não existe transferência de titularidade da temporada.

## Estado

Estados:

- `RASCUNHO`
- `ATIVA`
- `ENCERRADA`
- `CANCELADA`

`CANCELADA` é terminal. Cancelamento é lógico, não apaga campeonatos, inscrições ou resultados e não permite reativação da mesma temporada. Nova utilização equivalente exige nova temporada e novo ID.

Criador e administradores com `ADMINISTRACAO` podem cancelar enquanto a temporada não estiver cancelada. Permissão `CONSULTA` não permite cancelamento.

## Acesso de profissionais à temporada

O criador da temporada possui autoridade administrativa por definição e não depende de vínculo em `TemporadaProfissional` para exercer seus poderes.

Os demais profissionais acessam a temporada por vínculo explícito.

Entidade conceitual:

```text
TemporadaProfissional
 ├── cdTemporada
 ├── cdProfissional
 ├── tipoPermissao
 ├── cdResponsavelInclusao
 ├── dtInclusao
 ├── dtRemocao
 └── auditoria
```

Tipos de permissão:

- `ADMINISTRACAO`
- `CONSULTA`

### ADMINISTRACAO

Profissional com `ADMINISTRACAO` pode, respeitando as demais regras do ciclo de vida da temporada:

- alterar dados da temporada;
- configurar pontuação por colocação;
- configurar penalidades;
- vincular e desvincular campeonatos;
- incluir outros profissionais na temporada;
- remover outros profissionais da temporada;
- definir ou alterar permissões `ADMINISTRACAO` e `CONSULTA`;
- executar operações administrativas autorizadas sobre inscrições, resultados e demais fluxos relacionados à temporada.

Administrador convidado possui, no MVP, os mesmos poderes administrativos ordinários do criador, exceto operações que dependam explicitamente da condição de criador segundo outra regra de negócio.

### CONSULTA

Profissional com `CONSULTA` pode somente visualizar as informações às quais esteja autorizado no contexto da temporada.

Não pode:

- alterar dados da temporada;
- alterar pontuação ou penalidades;
- vincular/desvincular campeonatos;
- gerenciar profissionais da temporada;
- executar operações administrativas reservadas a `ADMINISTRACAO`.

### Gestão dos acessos

Qualquer profissional com `ADMINISTRACAO` pode incluir, remover, promover ou rebaixar outros profissionais vinculados à temporada.

Regras:

- o criador não pode ser removido da temporada por gerenciamento de acessos;
- o criador não pode ser rebaixado para `CONSULTA`;
- a autoridade do criador decorre de `cdCriador`, e não de `TemporadaProfissional`;
- `cdCriador` é imutável no MVP;
- não existe transferência de titularidade;
- remoções de acesso devem ser lógicas/auditáveis;
- remover acesso de um profissional não apaga nem altera dados históricos anteriormente produzidos por ele.

## Campeonatos

Relação N:N via `TemporadaCampeonato`.

Invariantes:

- não duplicar associação temporada/campeonato;
- campeonato pode estar em várias temporadas;
- datas nominais da temporada não limitam automaticamente associação;
- apenas profissional com `ADMINISTRACAO` pode vincular ou desvincular campeonato;
- adicionar/remover campeonato altera elegibilidade e ranking;
- remover associação não cancela campeonato, inscrição ou resultado;
- temporada cancelada preserva composição apenas para histórico.

## Pontuação por colocação

A tabela de colocação pertence à temporada.

Entidade conceitual:

```text
TemporadaPontuacao
 ├── cdTemporadaPontuacao
 ├── cdTemporada
 ├── posicao
 ├── tipoClasse
 ├── pontuacao
 └── auditoria
```

Tipos de classe:

- `COMUM`
- `OVERALL`

Para `Resultado.situacaoResultado = CLASSIFICADO`, a chave lógica é:

```text
(cdTemporada, posicao, tipoClasse)
```

A categoria não participa da chave.

### Limite de colocações pontuadas

A temporada não precisa atribuir pontos a todas as colocações possíveis de um campeonato.

A própria existência de registros em `TemporadaPontuacao` define até quais posições aquela temporada consolida pontuação para cada `tipoClasse`.

Exemplo:

```text
COMUM
1º = 10
2º = 8
3º = 6
4º = 4
5º = 2

6º em diante -> sem regra de pontuação
```

Não existe obrigação de lançar no sistema todas as posições esportivas do campeonato quando a temporada não as utiliza para sua consolidação.

Portanto:

- o campeonato pode possuir atletas classificados além da última posição pontuada pela temporada;
- essas posições superiores podem não ser informadas no sistema;
- posição não informada não deve ser criada artificialmente com pontuação zero;
- posição não informada não equivale a `AUSENTE`;
- ausência de lançamento não gera penalidade;
- caso uma colocação seja informada e não exista regra correspondente em `TemporadaPontuacao`, seu impacto é zero.

A regra de limite pertence à temporada e não altera a realidade esportiva do campeonato.

## Penalidades por situação esportiva

A temporada pode configurar tratamento específico para resultados aprovados em situações que não possuem colocação classificatória.

No MVP, tratar separadamente:

- `DESCLASSIFICACAO`
- `AUSENCIA`

Essas regras pertencem à temporada porque profissionais administradores diferentes podem adotar critérios diferentes.

Entidade conceitual recomendada:

```text
TemporadaPenalidade
 ├── cdTemporadaPenalidade
 ├── cdTemporada
 ├── tipoPenalidade
 ├── valor
 └── auditoria
```

Tipos mínimos:

- `DESCLASSIFICACAO`
- `AUSENCIA`

Unicidade lógica:

```text
(cdTemporada, tipoPenalidade)
```

A ausência de regra cadastrada para determinada situação significa **zero de impacto**, e não erro de cálculo.

### Aplicação por Resultado

Cada `Resultado APROVADO` é interpretado individualmente pela temporada e produz exatamente um impacto correspondente à sua situação esportiva.

Não existe agregação ou limitação por campeonato, categoria ou tipo de penalidade no MVP.

Exemplo:

```text
Atleta 1 / Campeonato 1

Classic Physique / Classe 1        -> CLASSIFICADO / 1º lugar
Classic Physique / Combate         -> AUSENTE
Classic Physique / Overall         -> CLASSIFICADO / 1º lugar
Culturismo Clássico / Classe 1     -> AUSENTE
Culturismo Clássico / Master 1     -> DESCLASSIFICADO
```

A temporada deve interpretar todos os cinco resultados separadamente:

```text
TOTAL =
  pontos(1º lugar COMUM)
  + penalidade(AUSENCIA)
  + pontos(1º lugar OVERALL)
  + penalidade(AUSENCIA)
  + penalidade(DESCLASSIFICACAO)
```

Se `AUSENCIA = -2`, as duas ausências produzem `-4` no total. Se `DESCLASSIFICACAO = -5`, soma-se também `-5`.

O fato de os resultados pertencerem ao mesmo campeonato não reduz nem agrupa seus impactos.

## Domínio numérico

Pontuação e penalidade usam `BigDecimal` com:

- `precision = 10`
- `scale = 3`

A regra anterior que proibia valores negativos está revogada.

Regras atuais:

- `TemporadaPontuacao.pontuacao` pode ser positiva, zero ou negativa;
- `TemporadaPenalidade.valor` pode ser zero ou negativo para desconto;
- o ranking final do atleta também pode resultar em valor negativo;
- não usar `double` ou `float`;
- quando arredondamento for necessário, usar `RoundingMode.HALF_UP`;
- evitar arredondamentos intermediários desnecessários.

Embora tecnicamente a tabela por colocação também aceite valor negativo, a intenção funcional de punições por desclassificação/ausência deve ser modelada por `TemporadaPenalidade`, e não por colocação artificial.

## Interpretação do Resultado

```text
Resultado APROVADO + CLASSIFICADO
  -> colocacao + tipoClasse
  -> TemporadaPontuacao
  -> uma contribuição para o total

Resultado APROVADO + DESCLASSIFICADO
  -> TemporadaPenalidade.DESCLASSIFICACAO
  -> uma contribuição para o total

Resultado APROVADO + AUSENTE
  -> TemporadaPenalidade.AUSENCIA
  -> uma contribuição para o total
```

Não persistir pontos ou penalidade calculada como verdade autoritativa no `Resultado`.

## Elegibilidade

Enquanto esportivamente utilizável, atleta integra a temporada quando:

1. possui vínculo `ATIVO` com o criador;
2. possui inscrição confirmada e não cancelada em ao menos um campeonato associado;
3. conta do atleta não está cancelada.

Elegibilidade é derivada.

## Ranking

Ranking é projeção dinâmica e não entidade autoritativa no MVP.

O total do atleta é a soma algébrica dos impactos de todos os `Resultado APROVADO` válidos na temporada:

```text
TOTAL = soma(impacto de cada Resultado APROVADO válido)
```

O total pode ser positivo, zero ou negativo.

Alterações nas tabelas de pontuação ou valores de penalidade exigem recálculo das projeções afetadas.

## Categoria no ranking — fora do MVP

A dimensão `Categoria` permanece mapeada no domínio porque faz parte do `Resultado` e poderá futuramente ser utilizada para rankings, filtros ou consolidações específicas por categoria.

Entretanto, **ranking/consolidação por categoria não faz parte do MVP atual**.

No MVP, a consolidação principal da temporada considera todos os resultados válidos do atleta, independentemente da categoria, respeitando a pontuação por tipo de classe e as penalidades por situação esportiva.

Qualquer regra específica de ranking por categoria deve ser refinada em etapa futura, sem alterar os resultados históricos já armazenados.

## Invariantes consolidadas

- temporada possui exatamente um criador permanente no MVP;
- `cdCriador` é imutável;
- não existe transferência de titularidade da temporada no MVP;
- qualquer `ADMINISTRACAO` pode gerenciar acessos de outros profissionais;
- `CONSULTA` não altera configuração nem composição da temporada;
- criador não pode ser removido ou rebaixado por gerenciamento de acesso;
- temporadas do mesmo criador podem sobrepor períodos;
- campeonato não pertence exclusivamente à temporada;
- tabela de colocação pertence à temporada;
- temporada pode limitar as colocações que pontua sem exigir lançamento das posições superiores;
- posição esportiva não informada não equivale a ausência;
- ausência de lançamento não produz penalidade;
- penalidades pertencem à temporada;
- desclassificação e ausência possuem regras independentes;
- cada `Resultado APROVADO` produz uma contribuição independente para o total da temporada;
- múltiplas ausências/desclassificações no mesmo campeonato acumulam suas penalidades individualmente;
- não existe `modoAplicacao` ou agregação de penalidades por campeonato no MVP;
- ausência de penalidade configurada equivale a zero;
- valores negativos são permitidos;
- pontuação e penalidade usam `BigDecimal(10,3)`;
- `CLASSIFICADO` usa colocação/tipo de classe;
- `DESCLASSIFICADO` e `AUSENTE` não usam colocação artificial;
- ranking pode apresentar total negativo;
- categoria permanece mapeada, mas ranking/consolidação por categoria está fora do MVP;
- temporada cancelada não pode ser reativada;
- cancelamento da temporada não cancela resultados, inscrições, campeonatos ou vínculos.
