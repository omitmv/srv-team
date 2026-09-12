# Refinamento técnico — Temporada

Status: estrutura inicial derivada das regras consolidadas em `mvp-pontuacao.md`; decisões abertas identificadas para fechamento com o proprietário do produto.

## Objetivo

Modelar a `Temporada` como contexto no qual campeonatos compartilhados são selecionados, resultados aprovados são convertidos em pontos e rankings são calculados segundo regras próprias da temporada.

## Situação atual do código

Na branch `release` não existe entidade Java `Temporada`. O modelo atual possui entidades anteriores de competição/pontuação, que não devem ser simplesmente reutilizadas como representação da nova regra sem adequação ao domínio refinado.

## Papel da Temporada no domínio

A temporada não é um campeonato e não é um agrupamento que possui os campeonatos como propriedade exclusiva.

Um campeonato pertence ao catálogo compartilhado e pode participar de várias temporadas.

A temporada define o contexto de pontuação:

```text
Temporada
 ├── criador
 ├── período
 ├── critérios
 ├── profissionais autorizados
 ├── administradores convidados
 ├── campeonatos selecionados
 ├── tabela de pontos
 └── rankings derivados
```

Os pontos não pertencem universalmente ao campeonato. O mesmo resultado pode produzir pontuações diferentes em temporadas diferentes porque cada temporada possui sua própria tabela de pontos.

## Entidade principal proposta

`Temporada`

Campos conceituais iniciais:

- `cdTemporada`
- `dsNome`
- `cdCriador`
- `dtInicio`
- `dtEncerramento`
- `status`
- `criterios`
- `dtCadastro`
- campos de auditoria

`cdCriador` identifica o profissional proprietário funcional da temporada.

## Estado da temporada

Não assumir que `dtEncerramento` congela os dados. O MVP já determina que rankings históricos continuam dinâmicos conforme vínculos, conta do atleta, campeonatos associados e tabela vigente.

Portanto, o estado da temporada deve representar principalmente seu ciclo administrativo e não um snapshot imutável do ranking.

Estados candidatos para refinamento:

- `RASCUNHO`
- `ATIVA`
- `ENCERRADA`
- `CANCELADA`

A necessidade de todos esses estados ainda deve ser confirmada. Não implementar enum definitivo antes do fechamento da regra.

## Criador

- Toda temporada possui exatamente um criador.
- O criador deve ser um usuário elegível como profissional de atendimento.
- O criador possui administração integral da temporada.
- O criador define tabela, campeonatos e permissões da temporada.
- O vínculo de um atleta com administradores convidados não torna esse atleta elegível para a temporada; a elegibilidade esportiva depende do vínculo atual com o criador.

## Permissões da temporada

A permissão administrativa deve ser modelada separadamente do tipo global de acesso do usuário.

Estrutura conceitual sugerida:

`TemporadaPermissao`

- `cdTemporadaPermissao`
- `cdTemporada`
- `cdUsuario`
- `tipoPermissao`
- `dtCadastro`
- auditoria

Tipos mínimos:

- `ADMINISTRACAO`
- `CONSULTA`

O criador não precisa necessariamente de registro redundante nessa associação, pois sua autoridade deriva da própria temporada.

Uma permissão de `ADMINISTRACAO` implica capacidade de consulta da temporada.

Uma permissão de `CONSULTA` não concede administração, aprovação de inscrição ou operação de resultados por si só.

## Campeonatos da temporada

A relação é N:N:

```text
Temporada N ---- N Campeonato
```

Criar associação explícita, conceitualmente `TemporadaCampeonato`, em vez de adicionar `cdTemporada` ao campeonato.

Campos iniciais:

- `cdTemporadaCampeonato`
- `cdTemporada`
- `cdCampeonato`
- `dtAssociacao`
- auditoria

Regras já consolidadas:

- um campeonato pode participar de várias temporadas;
- campeonatos podem ser associados mesmo quando sua data estiver fora do período informado da temporada;
- adicionar campeonato pode alterar elegibilidade e ranking;
- remover associação recalcula o ranking;
- remover associação não cancela nem exclui campeonato, inscrições ou resultados;
- criador e administradores autorizados podem alterar a composição mesmo após existirem resultados.

## Tabela de pontos

A tabela pertence à temporada.

Não reutilizar `Pontuacao` atual como tabela universal por competição sem revisar sua semântica.

Estrutura conceitual sugerida:

`TemporadaPontuacao`

- `cdTemporadaPontuacao`
- `cdTemporada`
- `posicao`
- `tipoClasse`
- `pontuacao`
- auditoria

A chave lógica deve impedir regras ambíguas para a mesma combinação de temporada, posição e tipo de classe.

Alterações na tabela recalculam rankings existentes; o MVP não congela a pontuação histórica no momento em que o resultado é aprovado.

## Elegibilidade do atleta

O atleta integra uma temporada somente quando todas as condições vigentes forem satisfeitas:

1. possui vínculo profissional-atleta `ATIVO` com o criador da temporada;
2. possui inscrição confirmada e não cancelada em pelo menos um campeonato associado à temporada;
3. sua conta não está cancelada.

A elegibilidade deve ser calculada a partir do estado atual do domínio, não armazenada como flag permanente na temporada.

Consequências:

- novo vínculo pode tornar resultados anteriores elegíveis;
- encerramento do vínculo pode retirar o atleta de rankings inclusive de temporada encerrada;
- cancelamento da conta retira temporariamente o atleta dos rankings;
- reativação pode reinseri-lo;
- adicionar/remover campeonato pode alterar o conjunto de atletas elegíveis.

## Ranking

O ranking é dado derivado.

Não criar inicialmente uma entidade `Ranking` como fonte de verdade sem necessidade comprovada de materialização/cache.

A fonte de verdade deve permanecer nas entidades de domínio:

- temporada;
- campeonatos associados;
- tabela de pontos;
- vínculos ativos;
- inscrições confirmadas;
- resultados aprovados e não cancelados;
- situação da conta do atleta.

Caso performance exija materialização posterior, tratar como projeção/cache recalculável, e não como origem autoritativa do dado.

## Invariantes iniciais

- temporada possui exatamente um criador;
- criador deve ser profissional elegível;
- campeonato não pertence exclusivamente à temporada;
- não pode existir associação duplicada entre a mesma temporada e campeonato;
- não pode existir permissão duplicada semanticamente para o mesmo usuário/temporada;
- tabela não pode possuir duas regras conflitantes para a mesma combinação de posição e tipo de classe;
- rankings são derivados das regras vigentes;
- encerramento temporal da temporada não congela automaticamente rankings.

## Decisões abertas para fechamento

### T1 — Nome da temporada

Definir se `dsNome` é obrigatório e livre (ex.: `Temporada 2026`, `Ranking 2026`) ou se existe uma convenção de nomenclatura.

Recomendação: nome obrigatório, livre, com limite de tamanho; não derivar identidade apenas do ano.

### T2 — Sobreposição de períodos

Definir se um mesmo profissional pode criar temporadas cujos períodos se sobreponham.

Recomendação: permitir. Como campeonatos e critérios são selecionados explicitamente, impedir sobreposição introduziria uma restrição que não parece necessária ao domínio.

### T3 — Significado de encerramento

Definir quais operações ficam bloqueadas quando a temporada está `ENCERRADA`.

A regra já consolidada permite que rankings históricos mudem e afirma que tabela/composição podem ser alteradas mesmo após resultados lançados, mas ainda precisamos decidir se isso continua permitido depois do encerramento formal.

Recomendação: `ENCERRADA` bloquear alterações ordinárias de configuração para profissionais, mantendo consulta e recálculos derivados automáticos. O proprietário poderia reabrir/intervir administrativamente. Isso evita que uma temporada encerrada seja editada acidentalmente sem transformar ranking em snapshot histórico.

### T4 — Exclusão ou cancelamento

Definir se temporada já utilizada pode ser excluída fisicamente.

Recomendação: não permitir exclusão física após possuir campeonatos, permissões ou participação calculável. Usar cancelamento lógico e preservar histórico/auditoria.

### T5 — Critérios adicionais

O `mvp-pontuacao.md` menciona critérios próprios da temporada, mas seu conteúdo ainda não foi especificado.

Precisamos decidir se há algum critério além de:

- vínculo com o criador;
- inscrição confirmada em campeonato associado;
- conta elegível;
- tabela de pontos;
- categoria/classe dos resultados.

Se não houver regra adicional necessária para o MVP, remover o conceito genérico `criterios` da entidade em vez de criar JSON ou campo textual sem semântica definida.
