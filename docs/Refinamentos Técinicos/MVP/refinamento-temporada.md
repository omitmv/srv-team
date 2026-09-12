# Refinamento técnico — Temporada

Status: decisões de negócio consolidadas para o MVP; refinamento técnico em andamento.

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
 ├── nome
 ├── período
 ├── estado administrativo
 ├── profissionais autorizados
 ├── administradores convidados
 ├── campeonatos selecionados
 ├── tabela de pontos
 └── rankings derivados
```

Os pontos não pertencem universalmente ao campeonato. O mesmo resultado pode produzir pontuações diferentes em temporadas diferentes porque cada temporada possui sua própria tabela de pontos.

## Entidade principal proposta

`Temporada`

Campos conceituais:

- `cdTemporada`
- `dsNome`
- `cdCriador`
- `dtInicio`
- `dtEncerramento`
- `status`
- `dtCadastro`
- campos de auditoria

Não criar campo genérico `criterios` no MVP. Critérios devem existir apenas quando possuírem semântica de negócio explícita.

`dsNome` é obrigatório, livre e deve possuir limite de tamanho. Não usar ano como identidade exclusiva da temporada.

`cdCriador` identifica o profissional proprietário funcional da temporada.

## Período

- `dtInicio` e `dtEncerramento` representam o período nominal da temporada.
- O período não restringe automaticamente quais campeonatos podem ser associados.
- Campeonatos fora desse período podem ser associados explicitamente.
- Um mesmo profissional pode possuir temporadas com períodos sobrepostos.
- O período não deve ser usado como chave de unicidade da temporada.

## Estado da temporada

O estado representa principalmente o ciclo administrativo, não um snapshot esportivo imutável.

Estados recomendados:

- `RASCUNHO`
- `ATIVA`
- `ENCERRADA`
- `CANCELADA`

### RASCUNHO

Permite configuração antes da disponibilização normal da temporada.

### ATIVA

Permite operação ordinária conforme as permissões do usuário.

### ENCERRADA

- bloqueia alterações ordinárias de configuração para criador e administradores convidados;
- continua disponível para consulta conforme autorização;
- continua sujeita a recálculos derivados decorrentes de alterações externas permitidas pelo domínio, como vínculo, conta do atleta e intervenções administrativas;
- não representa congelamento do ranking;
- o proprietário pode intervir administrativamente e, quando necessário, reabrir a temporada.

### CANCELADA

Representa cancelamento lógico. A temporada e seu histórico não devem ser removidos quando já utilizados.

## Exclusão

Não permitir exclusão física de temporada que já possua qualquer elemento relevante associado, incluindo campeonatos, permissões, tabela de pontos ou participação calculável.

Utilizar cancelamento lógico para preservar integridade referencial e auditoria.

## Criador

- Toda temporada possui exatamente um criador.
- O criador deve ser um usuário elegível como profissional de atendimento.
- O criador possui administração integral da temporada enquanto seu estado permitir operação administrativa ordinária.
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

Criar associação explícita `TemporadaCampeonato`, em vez de adicionar `cdTemporada` ao campeonato.

Campos recomendados:

- `cdTemporadaCampeonato`
- `cdTemporada`
- `cdCompeticao`
- `dtAssociacao`
- campos de auditoria

Invariantes:

- não pode existir associação duplicada entre a mesma temporada e campeonato;
- um campeonato pode participar de várias temporadas;
- campeonatos podem ser associados mesmo quando sua data estiver fora do período nominal da temporada;
- adicionar campeonato pode alterar elegibilidade e ranking;
- remover associação recalcula o ranking;
- remover associação não cancela nem exclui campeonato, inscrições ou resultados;
- durante `RASCUNHO` ou `ATIVA`, criador e administradores autorizados podem alterar a composição;
- em `ENCERRADA`, alterações ordinárias de composição ficam bloqueadas, salvo intervenção administrativa do proprietário.

## Tabela de pontos

### Incompatibilidade com o modelo atual

A entidade atual `Pontuacao` está ligada diretamente a `cdCompeticao`. Essa semântica não atende o domínio consolidado do MVP.

A tabela de pontos pertence à temporada, não ao campeonato.

Um campeonato fornece colocações/resultados. A conversão da colocação em pontos acontece no contexto de cada temporada elegível.

Portanto, `tbPontuacao` não deve permanecer como tabela universal por competição.

### Nova semântica proposta

Criar entidade conceitual `TemporadaPontuacao`:

- `cdTemporadaPontuacao`
- `cdTemporada`
- `posicao`
- `tipoClasse`
- `pontuacao`
- campos de auditoria

Tipos de classe para pontuação no MVP:

- `COMUM`
- `OVERALL`

A categoria esportiva não participa da chave da tabela de pontos. Para uma mesma temporada, posição e tipo de classe, a pontuação é igual em todas as categorias.

### Invariantes da tabela

- unicidade lógica em `(cdTemporada, posicao, tipoClasse)`;
- `posicao` deve ser positiva;
- não permitir duas regras conflitantes para a mesma combinação;
- colocação sem regra cadastrada vale `0` ponto;
- alterações na tabela recalculam rankings, inclusive de resultados anteriores;
- a pontuação calculada não deve ser persistida como valor histórico autoritativo por resultado;
- precisão decimal deve ser preservada; limite e arredondamento devem ser definidos separadamente.

## Relação entre Campeonato e Pontuação

Não existe relação direta de propriedade:

```text
Campeonato
   │
   └── Resultado: atleta + categoria + classe + colocação

Temporada
   ├── associa Campeonato
   └── possui Tabela de Pontos

Resultado aprovado
   + Temporada elegível
   + tipo da classe
   + colocação
   ─────────────────────
          Pontos calculados
```

O mesmo resultado aprovado pode gerar valores diferentes em temporadas distintas.

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

## Invariantes consolidadas

- temporada possui exatamente um criador;
- `dsNome` é obrigatório e livre;
- criador deve ser profissional elegível;
- temporadas do mesmo criador podem possuir períodos sobrepostos;
- campeonato não pertence exclusivamente à temporada;
- não pode existir associação duplicada entre a mesma temporada e campeonato;
- não pode existir permissão duplicada semanticamente para o mesmo usuário/temporada;
- tabela não pode possuir duas regras conflitantes para a mesma combinação de posição e tipo de classe;
- rankings são derivados das regras vigentes;
- encerramento temporal da temporada não congela automaticamente rankings;
- temporada utilizada não deve sofrer exclusão física;
- não existe campo genérico `criterios` enquanto não houver regra concreta que o justifique.

## Próximos pontos de refinamento

1. Definir precisão, escala, arredondamento e domínio válido da pontuação (`zero` e valores negativos).
2. Refinar catálogo de categoria/classe e identificar tecnicamente como distinguir classe comum de `OVERALL`.
3. Refinar migração/aposentadoria da atual `tbPontuacao` e de `tbPontuacaoHist` para as novas entidades do domínio.
4. Refinar `Inscricao` e `Resultado`, que são as próximas fontes autoritativas necessárias para o cálculo do ranking.
