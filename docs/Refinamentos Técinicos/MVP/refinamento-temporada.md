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
- auxiliar o criador nos lançamentos e operações administrativas da temporada referentes aos atletas que pertencem à temporada.

Administrador convidado possui, no MVP, os mesmos poderes administrativos ordinários do criador sobre a operação da temporada, exceto operações que dependam explicitamente da condição de criador segundo outra regra de negócio.

A permissão administrativa sobre a temporada não cria `VinculoProfissionalAtleta` e não concede ao administrador relação profissional permanente com os atletas do criador fora do contexto daquela temporada.

### CONSULTA

Profissional com `CONSULTA` pode visualizar a temporada e suas informações gerais, porém dados individualizados de atletas são limitados aos atletas da temporada com os quais esse profissional também possua `VinculoProfissionalAtleta` ativo.

Assim, um consultor:

- pode consultar dados gerais da temporada permitidos ao perfil;
- pode consultar informações individualizadas, inscrições, resultados e histórico esportivo somente dos atletas da temporada vinculados ativamente a ele;
- não passa a visualizar dados individualizados de todos os atletas do criador apenas por possuir acesso à temporada.

Não pode:

- alterar dados da temporada;
- alterar pontuação ou penalidades;
- vincular/desvincular campeonatos;
- gerenciar profissionais da temporada;
- lançar, corrigir, aprovar ou cancelar resultados em nome da administração da temporada;
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

## Composição de atletas da temporada

A composição de atletas da temporada é determinada exclusivamente pelos vínculos do criador.

Vínculos do atleta com administradores ou consultores da temporada não adicionam o atleta à temporada.

`VinculoProfissionalAtleta` é a fonte autoritativa da relação entre atleta e profissional, inclusive para consultas históricas. Não deve existir `TemporadaAtleta` apenas para materializar essa relação no MVP.

Exemplo:

```text
Temporada criada pelo Profissional A

Atleta X -> vínculo com A
Atleta Y -> vínculo apenas com B, que é ADMINISTRACAO da temporada

Atleta X pode integrar a temporada.
Atleta Y não integra a temporada apenas porque B administra a temporada.
```

Portanto:

- `TemporadaProfissional` controla acesso profissional à temporada;
- `VinculoProfissionalAtleta` com o criador controla quais atletas podem compor a temporada;
- não deve existir inclusão indireta de atletas por administradores convidados;
- um mesmo atleta pode possuir vínculo com o criador e também com outro profissional vinculado à temporada;
- nesse caso, o segundo vínculo afeta a visibilidade própria desse profissional, mas não a pertença do atleta à temporada.

### Vigência temporal do vínculo

Para operações atuais, um vínculo está disponível quando seu estado atual é `ATIVO`.

Para verificar se atleta e profissional possuíam relação válida em uma data passada, deve-se consultar o intervalo histórico do vínculo, e não exigir que o registro continue atualmente com status `ATIVO`.

Conceitualmente:

```text
vinculoVigenteNaData(atleta, profissional, dataReferencia)

= existe VinculoProfissionalAtleta em que
  dtInicio <= dataReferencia
  E
  (dtEncerramento IS NULL OU dtEncerramento >= dataReferencia)
```

Um vínculo atualmente `ENCERRADO` pode, portanto, comprovar corretamente uma relação que estava vigente no passado.

Quando um vínculo encerrado é retomado, deve ser criado novo `VinculoProfissionalAtleta`, preservando os intervalos históricos independentes.

Exemplo:

```text
Vínculo #1: 01/01 -> 30/06
sem vínculo: 01/07 -> 31/07
Vínculo #2: 01/08 -> atual
```

O novo vínculo não preenche retroativamente o intervalo sem vínculo.

### Visibilidade e operação por tipo de acesso

Para profissional com `CONSULTA`:

```text
Atleta pertence à temporada
    E
possui vínculo ATIVO com o consultor
        -> pode consultar informações individualizadas desse atleta
```

Para profissional com `ADMINISTRACAO`:

```text
Atleta pertence à temporada
        -> administrador pode operar lançamentos da temporada desse atleta
```

O administrador não precisa possuir vínculo profissional-atleta próprio com cada atleta para auxiliar o criador nas operações administrativas daquela temporada.

Essa autorização é contextual:

- vale apenas para a temporada em que possui `ADMINISTRACAO`;
- não cria vínculo profissional-atleta;
- não concede acesso administrativo ao mesmo atleta em outra temporada;
- não concede acesso geral ao cadastro do atleta fora das informações necessárias à operação da temporada;
- encerra-se quando o vínculo administrativo com a temporada deixa de estar ativo.

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

A elegibilidade possui uma dimensão atual e uma dimensão histórica.

### Participação atual no ranking

Enquanto esportivamente utilizável, atleta aparece na composição/ranking corrente da temporada quando:

1. possui `VinculoProfissionalAtleta` atualmente `ATIVO` com o criador da temporada;
2. possui inscrição confirmada e não cancelada em ao menos um campeonato associado;
3. conta do atleta não está cancelada.

Quando o vínculo atual com o criador é encerrado, o atleta deixa imediatamente de compor o ranking corrente. Inscrições, resultados e histórico permanecem preservados.

Se posteriormente for criado novo vínculo `ATIVO` com o mesmo criador, o atleta volta automaticamente a poder compor a temporada, respeitadas as demais regras de elegibilidade.

### Elegibilidade histórica de Resultado

A retomada do vínculo não deve produzir retroatividade sobre campeonatos ocorridos em intervalos em que atleta e criador não possuíam vínculo.

Para decidir se um `Resultado APROVADO` pode contribuir para a temporada, a referência temporal do vínculo é `Campeonato.dtInicio`, e não a data de lançamento ou aprovação do resultado.

Assim, um resultado somente possui elegibilidade temporal para a temporada quando existia vínculo vigente entre atleta e criador em `Campeonato.dtInicio`.

Exemplo:

```text
Vínculo #1: 01/01 -> 30/06
Campeonato A: 10/03 -> elegível
Campeonato B: 15/07 -> não elegível
Vínculo #2: 01/08 -> atual
Campeonato C: 10/08 -> elegível
```

Ao reativar a relação por um novo vínculo em 01/08, o Campeonato B não passa retroativamente a contribuir.

Conceitualmente, um resultado contribui para a temporada quando, além das demais regras esportivas:

```text
Campeonato associado à Temporada
E Resultado APROVADO
E Inscricao CONFIRMADA e não CANCELADA
E conta do atleta não cancelada
E vinculoVigenteNaData(atleta, Temporada.cdCriador, Campeonato.dtInicio)
```

A situação atual do vínculo define a presença do atleta no ranking corrente; a vigência histórica na data do campeonato define quais resultados são esportivamente elegíveis para compor sua pontuação.

Vínculo com profissional administrador ou consultor da temporada não substitui o vínculo com o criador para nenhuma dessas verificações.

## Ranking

Ranking é projeção dinâmica e não entidade autoritativa no MVP.

O total do atleta é a soma algébrica dos impactos de todos os `Resultado APROVADO` válidos e historicamente elegíveis na temporada:

```text
TOTAL = soma(impacto de cada Resultado APROVADO válido e elegível)
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
- somente atletas com vínculo atual `ATIVO` com o criador podem compor o ranking corrente da temporada;
- `VinculoProfissionalAtleta` preserva intervalos históricos de vigência e é a fonte autoritativa da relação atleta/profissional;
- vínculo atualmente `ENCERRADO` pode comprovar relação válida em uma data passada;
- novo vínculo após encerramento cria novo registro e não preenche retroativamente períodos sem vínculo;
- elegibilidade histórica de resultado usa a vigência do vínculo na `Campeonato.dtInicio`;
- encerrar vínculo com o criador remove o atleta do ranking corrente sem apagar inscrições, resultados ou histórico;
- novo vínculo ativo permite retorno ao ranking corrente, mas não torna elegíveis campeonatos ocorridos durante período sem vínculo;
- vínculo de atleta com administrador ou consultor não adiciona esse atleta à temporada;
- qualquer `ADMINISTRACAO` pode gerenciar acessos de outros profissionais;
- administrador pode auxiliar o criador nos lançamentos dos atletas da temporada mesmo sem vínculo profissional-atleta próprio com eles;
- a autorização do administrador sobre esses atletas é estritamente contextual à temporada;
- `CONSULTA` não altera configuração nem composição da temporada;
- consultor somente visualiza informações individualizadas de atletas da temporada que também estejam vinculados ativamente a ele;
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
