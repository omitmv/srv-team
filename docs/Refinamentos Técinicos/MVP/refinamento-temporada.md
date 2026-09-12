# Refinamento técnico — Temporada

Status: decisões de negócio consolidadas para o MVP; refinamento técnico em andamento.

## Objetivo

Modelar a `Temporada` como contexto no qual campeonatos compartilhados são selecionados, resultados aprovados são convertidos em pontos e rankings são calculados segundo regras próprias da temporada.

O ranking não deve ser persistido inicialmente como fonte autoritativa. Ele é resultado de cálculo.

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
- campos de auditoria

`dsNome` é obrigatório, livre e deve possuir limite de tamanho. Não usar ano como identidade exclusiva da temporada.

`cdCriador` identifica o profissional proprietário funcional da temporada.

## Período

- `dtInicio` e `dtEncerramento` representam o período nominal da temporada.
- O período não restringe automaticamente quais campeonatos podem ser associados.
- Campeonatos fora desse período podem ser associados explicitamente.
- Um mesmo profissional pode possuir temporadas com períodos sobrepostos.
- O período não deve ser usado como chave de unicidade da temporada.

## Estado da temporada

Estados do MVP:

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
- continua sujeita a recálculos derivados decorrentes de alterações externas permitidas pelo domínio;
- não representa congelamento do ranking;
- o proprietário pode intervir administrativamente e, quando necessário, reabrir a temporada.

### CANCELADA

`CANCELADA` é um estado terminal.

O cancelamento:

- é exclusivamente lógico;
- preserva integralmente a temporada e seu histórico;
- não exclui campeonatos, inscrições ou resultados;
- não cancela nem altera os `Resultado` lançados nos campeonatos que estavam associados à temporada;
- encerra a utilização daquela identidade de temporada;
- não permite reativação;
- não permite transição de `CANCELADA` para `RASCUNHO`, `ATIVA` ou `ENCERRADA`.

Se o profissional desejar voltar a trabalhar com uma configuração equivalente, deverá criar uma **nova `Temporada`**, que receberá um novo `cdTemporada`.

Mesmo que nome, período, campeonatos, permissões ou tabela de pontos sejam reproduzidos, a nova temporada é uma identidade de domínio distinta e não uma reativação da anterior.

Fluxo:

```text
Temporada #10
ATIVA
  |
  | cancelamento
  v
CANCELADA [terminal]

Nova necessidade
  |
  v
Temporada #25
nova identidade
```

## Quem pode cancelar

O cancelamento pode ser realizado por profissional com permissão de `ADMINISTRACAO` sobre a temporada, incluindo:

- o criador da temporada;
- administrador convidado com permissão administrativa vigente.

O proprietário mantém sua autoridade administrativa global.

A autorização deve ser validada no momento da operação.

Permissão apenas de `CONSULTA` não permite cancelamento.

O cancelamento deve registrar no mínimo:

- responsável pela operação;
- data/hora do cancelamento;
- estado anterior;
- auditoria correspondente.

## Efeito sobre resultados

A temporada é apenas um contexto que interpreta resultados de campeonatos para produzir pontuação e ranking.

Por isso, cancelar uma temporada **não cancela resultados**.

Os `Resultado` pertencem ao ciclo de `Inscricao` no campeonato e preservam seu próprio ciclo de vida independentemente da temporada.

Assim:

```text
Resultado APROVADO
       |
       +---- Temporada A ATIVA      -> pode gerar pontos em A
       |
       +---- Temporada B ATIVA      -> pode gerar pontos em B
       |
       +---- Temporada C CANCELADA  -> resultado continua existindo
```

O cancelamento de C não modifica o `status` do resultado e não interfere em sua utilização por outras temporadas elegíveis.

Também não cancelar automaticamente:

- `Inscricao`;
- `VinculoProfissionalAtleta`;
- `Competicao`/Campeonato;
- `Categoria`;
- `Classe`.

## Exclusão

Não utilizar exclusão física como operação funcional de cancelamento.

Uma temporada cancelada permanece persistida para:

- integridade referencial;
- auditoria;
- histórico administrativo;
- rastreabilidade dos campeonatos e configurações que estavam associados.

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

Uma permissão de `ADMINISTRACAO` implica capacidade de consulta da temporada e permite o cancelamento enquanto a temporada ainda não estiver `CANCELADA`.

Uma permissão de `CONSULTA` não concede administração, aprovação de inscrição, operação de resultados ou cancelamento da temporada por si só.

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
- em `ENCERRADA`, alterações ordinárias de composição ficam bloqueadas, salvo intervenção administrativa do proprietário;
- em `CANCELADA`, a composição fica preservada apenas para histórico e não pode voltar à operação ordinária.

## Tabela de pontos

A tabela de pontos pertence à temporada, não ao campeonato.

Criar entidade conceitual `TemporadaPontuacao`:

- `cdTemporadaPontuacao`
- `cdTemporada`
- `posicao`
- `tipoClasse`
- `pontuacao`
- campos de auditoria

Tipos de classe:

- `COMUM`
- `OVERALL`

A categoria não participa da chave da tabela de pontos.

### Domínio numérico

- utilizar `BigDecimal`;
- `precision = 10`;
- `scale = 3`;
- `pontuacao` obrigatória;
- permitir `0`;
- não permitir negativo;
- `posicao > 0`;
- colocação sem regra cadastrada vale `0` ponto;
- quando necessário arredondar, utilizar `RoundingMode.HALF_UP`.

### Invariantes

- unicidade lógica em `(cdTemporada, posicao, tipoClasse)`;
- não permitir regras conflitantes;
- alterações na tabela recalculam rankings quando a temporada estiver em estado que participe das consultas esportivas;
- pontuação calculada não é persistida como valor histórico autoritativo por resultado.

## Elegibilidade do atleta

Enquanto a temporada estiver esportivamente utilizável, o atleta integra a temporada quando:

1. possui vínculo `ATIVO` com o criador;
2. possui inscrição confirmada e não cancelada em pelo menos um campeonato associado;
3. sua conta não está cancelada.

A elegibilidade é derivada e não uma flag permanente.

## Ranking

O ranking é dado derivado, conforme `refinamento-ranking.md`.

Não criar entidade `Ranking` autoritativa no MVP.

O cancelamento da temporada não altera as entidades esportivas que serviram de origem ao ranking. A política de exibição/consulta histórica de uma temporada `CANCELADA` deve respeitar o estado administrativo sem apagar ou reescrever resultados.

## Invariantes consolidadas

- temporada possui exatamente um criador;
- `dsNome` é obrigatório e livre;
- criador deve ser profissional elegível;
- temporadas do mesmo criador podem possuir períodos sobrepostos;
- campeonato não pertence exclusivamente à temporada;
- não pode existir associação duplicada entre a mesma temporada e campeonato;
- não pode existir permissão duplicada semanticamente para o mesmo usuário/temporada;
- tabela não pode possuir duas regras conflitantes para a mesma combinação de posição e tipo de classe;
- pontuação usa `BigDecimal(10,3)`, aceita zero e rejeita negativos;
- `posicao > 0`;
- rankings são derivados das regras vigentes;
- `CANCELADA` é estado terminal;
- temporada cancelada não pode ser reativada;
- recriação exige nova temporada e novo `cdTemporada`;
- cancelamento é lógico e não remove histórico;
- criador ou administrador da temporada pode cancelar;
- cancelamento da temporada não cancela resultados, inscrições, campeonatos ou vínculos;
- temporada cancelada não deve sofrer exclusão física.
